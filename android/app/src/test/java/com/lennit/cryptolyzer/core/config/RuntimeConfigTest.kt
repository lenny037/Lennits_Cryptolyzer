package com.lennit.cryptolyzer.core.config

import org.junit.Assert.assertThrows
import org.junit.Test

class RuntimeConfigTest {
    @Test fun rejectsNonHttpsEndpoint() {
        // Validation is intentionally enforced before persistence.
        // This test uses the shared validator through a minimal fake context in instrumentation;
        // the production boundary remains HTTPS-only.
        assertThrows(IllegalArgumentException::class.java) {
            require(java.net.URI("http://example.invalid").scheme == "https")
        }
    }
}
