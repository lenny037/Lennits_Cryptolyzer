package com.lennit.cryptolyzer.eventbus

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RetryPolicyTest {

    private val policy = RetryPolicy(maxAttempts = 5, baseDelayMillis = 1_000, maxDelayMillis = 60_000)

    @Test
    fun `backoff grows and then saturates at the ceiling`() {
        val delays = (1..10).map { policy.delayFor(it, "event-a") }
        assertTrue(delays[0] < delays[1], "expected growth, got $delays")
        assertTrue(delays[1] < delays[2], "expected growth, got $delays")
        assertTrue(delays.all { it <= 60_000 }, "ceiling breached: $delays")
        assertEquals(delays[8], delays[9], "delays must saturate rather than grow unbounded")
    }

    @Test
    fun `jitter is deterministic for a given event`() {
        assertEquals(policy.delayFor(3, "event-a"), policy.delayFor(3, "event-a"))
    }

    @Test
    fun `jitter differs across events so retries do not synchronise`() {
        val a = policy.delayFor(4, "event-a")
        val b = policy.delayFor(4, "a-different-event-id")
        assertNotEquals(a, b)
    }

    @Test
    fun `retry budget is exhausted at max attempts`() {
        assertTrue(policy.hasBudgetLeft(4))
        assertTrue(!policy.hasBudgetLeft(5))
        assertTrue(!policy.hasBudgetLeft(6))
    }
}
