package com.lennit.cryptolyzer.persistence

import com.lennit.cryptolyzer.domain.events.DomainEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalEventStoreTest {
    @Test
    fun duplicate_idempotency_key_is_rejected() = runTest {
        val store = LocalEventStore()
        val first = DomainEvent("1", "test", null, 1L, "{}", "same")
        val second = DomainEvent("2", "test", null, 2L, "{}", "same")

        assertTrue(store.append(first))
        assertFalse(store.append(second))
    }
}
