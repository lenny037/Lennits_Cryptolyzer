package com.lennit.cryptolyzer.contracts

/**
 * Time is an injected dependency, never an ambient one.
 *
 * Phase 5 of the plan requires that identical inputs produce identical canonical output. Any
 * module that reads the wall clock directly breaks that guarantee, so time enters the system
 * only through this port. Tests use [MutableClock]; Android supplies a system implementation.
 */
public fun interface Clock {
    /** Milliseconds since the Unix epoch, UTC. */
    public fun nowEpochMillis(): Long

    public companion object {
        public fun system(): Clock = Clock { System.currentTimeMillis() }
    }
}

/** Deterministic clock for tests and replay. */
public class MutableClock(private var current: Long = 0L) : Clock {
    override fun nowEpochMillis(): Long = current

    public fun advanceBy(millis: Long) {
        require(millis >= 0) { "Clock cannot move backwards" }
        current += millis
    }

    public fun setTo(millis: Long) {
        current = millis
    }
}

/** Monotonic, collision-free identifier source. Injected for the same reason as [Clock]. */
public fun interface IdGenerator {
    public fun newId(): String

    public companion object {
        public fun uuid(): IdGenerator = IdGenerator { java.util.UUID.randomUUID().toString() }
    }
}
