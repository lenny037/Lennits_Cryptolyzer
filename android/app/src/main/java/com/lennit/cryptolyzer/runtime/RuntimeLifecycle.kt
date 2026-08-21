package com.lennit.cryptolyzer.runtime

/**
 * Process-local lifecycle contract for the mobile runtime.
 *
 * The runtime is deliberately modeled as a small state machine so background
 * work can determine whether domain processing is currently permitted without
 * depending on Android UI state.
 */
class RuntimeLifecycle {
    @Volatile
    var state: State = State.NEW
        private set

    @Synchronized
    fun start() {
        check(state == State.NEW || state == State.STOPPED) {
            "Runtime can only start from NEW or STOPPED; current state=$state"
        }
        state = State.RUNNING
    }

    @Synchronized
    fun pause() {
        check(state == State.RUNNING) {
            "Runtime can only pause from RUNNING; current state=$state"
        }
        state = State.PAUSED
    }

    @Synchronized
    fun resume() {
        check(state == State.PAUSED) {
            "Runtime can only resume from PAUSED; current state=$state"
        }
        state = State.RUNNING
    }

    @Synchronized
    fun stop() {
        check(state != State.NEW) {
            "Runtime cannot stop before it has started"
        }
        state = State.STOPPED
    }

    fun permitsDomainWork(): Boolean = state == State.RUNNING

    enum class State {
        NEW,
        RUNNING,
        PAUSED,
        STOPPED
    }
}
