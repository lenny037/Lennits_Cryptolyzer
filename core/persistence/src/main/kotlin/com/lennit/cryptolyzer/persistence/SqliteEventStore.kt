package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.contracts.ModuleId
import com.lennit.cryptolyzer.contracts.Outcome
import com.lennit.cryptolyzer.contracts.PlatformError
import com.lennit.cryptolyzer.eventbus.AppendResult
import com.lennit.cryptolyzer.eventbus.EventEnvelope
import com.lennit.cryptolyzer.eventbus.EventStatus
import com.lennit.cryptolyzer.eventbus.EventStore
import com.lennit.cryptolyzer.eventbus.EventStoreStats
import com.lennit.cryptolyzer.eventbus.EventType
import com.lennit.cryptolyzer.eventbus.StoredEvent
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Durable [EventStore] on SQLite.
 *
 * This is the JVM-side implementation of the same schema the Android build will open through
 * Room. Keeping it here, on plain JDBC, means the durability, idempotency and lease-recovery
 * behaviour is verified by the shared contract suite on every CI run without an emulator, which
 * is the difference between "the event log is tested" and "the event log is tested on a device
 * we hope someone plugs in".
 *
 * Durability choices, stated explicitly because they are a tradeoff:
 *  - `journal_mode=WAL`: concurrent reads during a write, and a smaller fsync cost per commit.
 *  - `synchronous=FULL`: survives OS-level crash and power loss. Slower than NORMAL, and correct
 *    for a log that records financial intent.
 */
public class SqliteEventStore(
    private val connection: Connection,
) : EventStore, AutoCloseable {

    private val lock = ReentrantLock()

    init {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA journal_mode=WAL")
            statement.execute("PRAGMA synchronous=FULL")
            statement.execute("PRAGMA foreign_keys=ON")
            statement.execute("PRAGMA busy_timeout=5000")
        }
        Migrations.apply(connection)
    }

    override fun append(envelope: EventEnvelope): Outcome<AppendResult> = guarded("append") {
        lock.withLock {
            existingIdFor(envelope.idempotencyKey)?.let { existing ->
                return@withLock Outcome.success(AppendResult.Duplicate(existing))
            }
            connection.prepareStatement(
                "INSERT INTO events (event_id, type, schema_version, producer, occurred_at, recorded_at, " +
                    "idempotency_key, payload, trace_id, status, attempt, next_attempt_at, " +
                    "lease_expires_at, last_error) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,NULL,NULL)",
            ).use { statement ->
                statement.setString(1, envelope.eventId)
                statement.setString(2, envelope.type.name)
                statement.setInt(3, envelope.schemaVersion)
                statement.setString(4, envelope.producer.code)
                statement.setLong(5, envelope.occurredAtEpochMillis)
                statement.setLong(6, envelope.recordedAtEpochMillis)
                statement.setString(7, envelope.idempotencyKey)
                statement.setString(8, PayloadCodec.encode(envelope.payload))
                statement.setString(9, envelope.traceId)
                statement.setString(10, EventStatus.Pending.name)
                statement.setInt(11, 0)
                statement.setLong(12, envelope.recordedAtEpochMillis)
                statement.executeUpdate()
            }
            Outcome.success(AppendResult.Appended(envelope.eventId))
        }
    }

    override fun claimPending(
        limit: Int,
        nowEpochMillis: Long,
        leaseMillis: Long,
    ): Outcome<List<StoredEvent>> {
        require(limit > 0) { "limit must be positive" }
        return guarded("claimPending") {
            lock.withLock {
                connection.autoCommit = false
                try {
                    val candidates = mutableListOf<String>()
                    connection.prepareStatement(
                        "SELECT event_id FROM events WHERE " +
                            "(status = ? AND next_attempt_at <= ?) OR " +
                            // Lease expiry is the crash-recovery path: work leased by a process
                            // that died becomes claimable again instead of being stranded.
                            "(status = ? AND lease_expires_at IS NOT NULL AND lease_expires_at <= ?) " +
                            "ORDER BY occurred_at ASC, rowid ASC LIMIT ?",
                    ).use { statement ->
                        statement.setString(1, EventStatus.Pending.name)
                        statement.setLong(2, nowEpochMillis)
                        statement.setString(3, EventStatus.InFlight.name)
                        statement.setLong(4, nowEpochMillis)
                        statement.setInt(5, limit)
                        statement.executeQuery().use { rows ->
                            while (rows.next()) candidates += rows.getString(1)
                        }
                    }
                    if (candidates.isEmpty()) {
                        connection.commit()
                        return@withLock Outcome.success(emptyList())
                    }
                    connection.prepareStatement(
                        "UPDATE events SET status = ?, lease_expires_at = ? WHERE event_id = ?",
                    ).use { statement ->
                        candidates.forEach { id ->
                            statement.setString(1, EventStatus.InFlight.name)
                            statement.setLong(2, nowEpochMillis + leaseMillis)
                            statement.setString(3, id)
                            statement.addBatch()
                        }
                        statement.executeBatch()
                    }
                    connection.commit()
                    Outcome.success(candidates.mapNotNull { readById(it) })
                } catch (error: Throwable) {
                    connection.rollback()
                    throw error
                } finally {
                    connection.autoCommit = true
                }
            }
        }
    }

    override fun markProcessed(eventId: String, nowEpochMillis: Long): Outcome<Unit> =
        update(eventId) { statement ->
            statement.setString(1, EventStatus.Processed.name)
            statement.setInt(2, attemptOf(eventId))
            statement.setLong(3, nowEpochMillis)
            statement.setNull(4, java.sql.Types.INTEGER)
            statement.setNull(5, java.sql.Types.VARCHAR)
        }

    override fun markForRetry(
        eventId: String,
        nextAttemptAtEpochMillis: Long,
        error: String,
    ): Outcome<Unit> = update(eventId) { statement ->
        statement.setString(1, EventStatus.Pending.name)
        statement.setInt(2, attemptOf(eventId) + 1)
        statement.setLong(3, nextAttemptAtEpochMillis)
        statement.setNull(4, java.sql.Types.INTEGER)
        statement.setString(5, error.take(MAX_ERROR_LENGTH))
    }

    override fun markDeadLettered(
        eventId: String,
        reason: String,
        nowEpochMillis: Long,
    ): Outcome<Unit> = update(eventId) { statement ->
        statement.setString(1, EventStatus.DeadLettered.name)
        statement.setInt(2, attemptOf(eventId))
        statement.setLong(3, nowEpochMillis)
        statement.setNull(4, java.sql.Types.INTEGER)
        statement.setString(5, reason.take(MAX_ERROR_LENGTH))
    }

    override fun releaseLease(eventId: String, nextAttemptAtEpochMillis: Long): Outcome<Unit> =
        update(eventId) { statement ->
            statement.setString(1, EventStatus.Pending.name)
            // A released lease does not consume an attempt: nothing was actually tried.
            statement.setInt(2, attemptOf(eventId))
            statement.setLong(3, nextAttemptAtEpochMillis)
            statement.setNull(4, java.sql.Types.INTEGER)
            statement.setNull(5, java.sql.Types.VARCHAR)
        }

    override fun find(eventId: String): StoredEvent? = lock.withLock { readById(eventId) }

    override fun stats(): EventStoreStats = lock.withLock {
        val counts = mutableMapOf<String, Int>()
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT status, COUNT(*) FROM events GROUP BY status").use { rows ->
                while (rows.next()) counts[rows.getString(1)] = rows.getInt(2)
            }
        }
        EventStoreStats(
            pending = counts[EventStatus.Pending.name] ?: 0,
            inFlight = counts[EventStatus.InFlight.name] ?: 0,
            processed = counts[EventStatus.Processed.name] ?: 0,
            deadLettered = counts[EventStatus.DeadLettered.name] ?: 0,
        )
    }

    override fun deadLetters(limit: Int): List<StoredEvent> = lock.withLock {
        val result = mutableListOf<StoredEvent>()
        connection.prepareStatement(
            "SELECT * FROM events WHERE status = ? ORDER BY recorded_at DESC LIMIT ?",
        ).use { statement ->
            statement.setString(1, EventStatus.DeadLettered.name)
            statement.setInt(2, limit)
            statement.executeQuery().use { rows ->
                while (rows.next()) readRow(rows)?.let(result::add)
            }
        }
        result
    }

    override fun close() {
        lock.withLock { connection.close() }
    }

    // ------------------------------------------------------------------ internals

    private fun existingIdFor(idempotencyKey: String): String? =
        connection.prepareStatement("SELECT event_id FROM events WHERE idempotency_key = ?").use { statement ->
            statement.setString(1, idempotencyKey)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getString(1) else null }
        }

    private fun attemptOf(eventId: String): Int =
        connection.prepareStatement("SELECT attempt FROM events WHERE event_id = ?").use { statement ->
            statement.setString(1, eventId)
            statement.executeQuery().use { rows -> if (rows.next()) rows.getInt(1) else 0 }
        }

    private inline fun update(
        eventId: String,
        crossinline bind: (java.sql.PreparedStatement) -> Unit,
    ): Outcome<Unit> = guarded("update") {
        lock.withLock {
            connection.prepareStatement(
                "UPDATE events SET status = ?, attempt = ?, next_attempt_at = ?, " +
                    "lease_expires_at = ?, last_error = ? WHERE event_id = ?",
            ).use { statement ->
                bind(statement)
                statement.setString(6, eventId)
                val changed = statement.executeUpdate()
                if (changed == 0) {
                    Outcome.failure(PlatformError.Storage("Unknown eventId: $eventId"))
                } else {
                    Outcome.success(Unit)
                }
            }
        }
    }

    private fun readById(eventId: String): StoredEvent? =
        connection.prepareStatement("SELECT * FROM events WHERE event_id = ?").use { statement ->
            statement.setString(1, eventId)
            statement.executeQuery().use { rows -> if (rows.next()) readRow(rows) else null }
        }

    private fun readRow(rows: ResultSet): StoredEvent? {
        val payload = PayloadCodec.decode(rows.getString("payload")).getOrNull() ?: return null
        val leaseExpiry = rows.getLong("lease_expires_at").let { if (rows.wasNull()) null else it }
        return StoredEvent(
            envelope = EventEnvelope(
                eventId = rows.getString("event_id"),
                type = EventType(rows.getString("type")),
                schemaVersion = rows.getInt("schema_version"),
                producer = ModuleId.byCode(rows.getString("producer")),
                occurredAtEpochMillis = rows.getLong("occurred_at"),
                recordedAtEpochMillis = rows.getLong("recorded_at"),
                idempotencyKey = rows.getString("idempotency_key"),
                payload = payload,
                traceId = rows.getString("trace_id"),
            ),
            status = EventStatus.valueOf(rows.getString("status")),
            attempt = rows.getInt("attempt"),
            nextAttemptAtEpochMillis = rows.getLong("next_attempt_at"),
            leaseExpiresAtEpochMillis = leaseExpiry,
            lastError = rows.getString("last_error"),
        )
    }

    private inline fun <T> guarded(operation: String, block: () -> Outcome<T>): Outcome<T> =
        try {
            block()
        } catch (error: java.sql.SQLException) {
            Outcome.failure(PlatformError.Storage("SQLite $operation failed: ${error.message}", cause = error))
        }

    public companion object {
        private const val MAX_ERROR_LENGTH = 2_000

        /** Opens or creates a store at [path]. Use [inMemory] for tests. */
        public fun open(path: String): SqliteEventStore =
            SqliteEventStore(DriverManager.getConnection("jdbc:sqlite:$path"))

        public fun inMemory(): SqliteEventStore =
            SqliteEventStore(DriverManager.getConnection("jdbc:sqlite::memory:"))

        public val schemaVersion: Int get() = Migrations.currentVersion
    }
}
