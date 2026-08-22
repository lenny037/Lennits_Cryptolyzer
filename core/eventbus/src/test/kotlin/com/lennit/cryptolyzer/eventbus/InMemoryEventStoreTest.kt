package com.lennit.cryptolyzer.eventbus

import com.lennit.cryptolyzer.eventbus.testing.EventStoreContract

class InMemoryEventStoreTest : EventStoreContract() {
    override fun newStore(): EventStore = InMemoryEventStore()
}
