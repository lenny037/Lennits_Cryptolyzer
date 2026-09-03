package com.lennit.cryptolyzer.core.agents

import java.math.BigInteger
import org.junit.Assert.assertEquals
import org.junit.Test

class EvmNumericValidationTest {
    @Test fun uint256MaximumIsRepresentable() {
        val max = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE)
        assertEquals(78, max.toString(10).length)
    }
}
