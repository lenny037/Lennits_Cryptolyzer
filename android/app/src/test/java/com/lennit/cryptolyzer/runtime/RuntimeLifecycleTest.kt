package com.lennit.cryptolyzer.runtime

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RuntimeLifecycleTest {
    @Test
    fun `runtime permits work only while running`() {
        val lifecycle = RuntimeLifecycle()

        assertFalse(lifecycle.permitsDomainWork())
        lifecycle.start()
        assertTrue(lifecycle.permitsDomainWork())
        lifecycle.pause()
        assertFalse(lifecycle.permitsDomainWork())
        lifecycle.resume()
        assertTrue(lifecycle.permitsDomainWork())
        lifecycle.stop()
        assertFalse(lifecycle.permitsDomainWork())
    }

    @Test
    fun `stopped runtime can be restarted`() {
        val lifecycle = RuntimeLifecycle()

        lifecycle.start()
        lifecycle.stop()
        lifecycle.start()

        assertTrue(lifecycle.permitsDomainWork())
    }
}
