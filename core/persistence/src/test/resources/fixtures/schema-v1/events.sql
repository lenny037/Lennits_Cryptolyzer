-- Retained fixture: a version 1 event database, as shipped.
--
-- Held as SQL rather than a `.db` file on purpose. The repository guards reject tracked binaries,
-- and a binary fixture is unreviewable: nobody can see in a diff that a "small fixture refresh"
-- quietly changed the schema the compatibility test is supposed to be defending. This text is
-- replayed into a fresh database by SchemaFixtureCompatibilityTest.
--
-- DO NOT EDIT to make a test pass. This file records what an installed application from the
-- version 1 release actually contains. If a change breaks it, the change breaks users' databases.
--
-- The DDL below is a byte-for-byte copy of Migrations version 1 (durable_event_log), with the
-- pre-ledger `schema_meta` table that build wrote, and no `schema_migrations` ledger — because that
-- table did not exist yet. Opening this database must therefore also exercise the ledger backfill.

CREATE TABLE IF NOT EXISTS events (
    event_id         TEXT    PRIMARY KEY,
    type             TEXT    NOT NULL,
    schema_version   INTEGER NOT NULL,
    producer         TEXT    NOT NULL,
    occurred_at      INTEGER NOT NULL,
    recorded_at      INTEGER NOT NULL,
    idempotency_key  TEXT    NOT NULL UNIQUE,
    payload          TEXT    NOT NULL,
    trace_id         TEXT,
    status           TEXT    NOT NULL,
    attempt          INTEGER NOT NULL DEFAULT 0,
    next_attempt_at  INTEGER NOT NULL,
    lease_expires_at INTEGER,
    last_error       TEXT
);
CREATE INDEX IF NOT EXISTS idx_events_claim ON events (status, next_attempt_at);
CREATE INDEX IF NOT EXISTS idx_events_lease ON events (status, lease_expires_at);
CREATE INDEX IF NOT EXISTS idx_events_type_time ON events (type, occurred_at);

CREATE TABLE IF NOT EXISTS schema_meta (version INTEGER NOT NULL);
INSERT INTO schema_meta (version) VALUES (1);

-- Rows use the frozen fixture event contracts from
-- com.lennit.cryptolyzer.eventbus.testing.RegistryFixtures, not production event types: a fixture
-- must not depend on payload shapes that later phases are still free to ratify.
--
-- Payloads are in the PayloadCodec wire format: key=value;key=value, where `\e` is an escaped `=`
-- and `\s` an escaped `;`.

-- Pending, oldest schema version of a type that has since moved to version 3.
INSERT INTO events VALUES (
    'evt-0001', 'test.widget_observed', 1, 'M08',
    1700000000000, 1700000000000, 'idem-0001',
    'source=rpc;value_raw=42', 'trace-a',
    'Pending', 0, 1700000000000, NULL, NULL
);

-- Pending after a failed attempt: attempt counter and backoff must survive the upgrade.
INSERT INTO events VALUES (
    'evt-0002', 'test.widget_observed', 1, 'M08',
    1700000001000, 1700000001000, 'idem-0002',
    'source=indexer;value_raw=0', NULL,
    'Pending', 2, 1700000060000, NULL, 'upstream flaked'
);

-- Processed. A decimal amount as a string, never a float (ADR-0003).
INSERT INTO events VALUES (
    'evt-0003', 'test.ledger_entry', 1, 'M02',
    1700000002000, 1700000002000, 'idem-0003',
    'amount=12.50', 'trace-b',
    'Processed', 1, 1700000002000, NULL, NULL
);

-- Dead-lettered. Retained forever; a migration that drops this row destroys the audit trail.
INSERT INTO events VALUES (
    'evt-0004', 'test.sensor_reading', 1, 'M00',
    1700000003000, 1700000003000, 'idem-0004',
    'sensor=s\e1;reading=7;note=a\sb', NULL,
    'DeadLettered', 5, 1700000003000, NULL, 'retry budget exhausted'
);

-- In flight when the process died. The lease is in the past, so recovery must reclaim it.
INSERT INTO events VALUES (
    'evt-0005', 'test.sensor_reading', 1, 'M00',
    1700000004000, 1700000004000, 'idem-0005',
    'sensor=s2;reading=9', 'trace-c',
    'InFlight', 1, 1700000004000, 1700000034000, NULL
);
