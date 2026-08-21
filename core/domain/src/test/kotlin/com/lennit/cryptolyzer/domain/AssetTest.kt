package com.lennit.cryptolyzer.domain

import com.lennit.cryptolyzer.contracts.Outcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AssetTest {

    @Test
    fun `a native asset is distinguished from a token`() {
        assertEquals(true, ethOnBase.isNative)
        assertEquals(false, usdcOnBase.isNative)
        assertEquals("8453/native", ethOnBase.canonicalId)
    }

    @Test
    fun `the same token on two chains is not the same asset`() {
        val usdcOnEthereum = usdcOnBase.copy(chainId = ChainId.ETHEREUM)
        assertNotEquals(usdcOnBase, usdcOnEthereum)
        assertNotEquals(usdcOnBase.canonicalId, usdcOnEthereum.canonicalId)
    }

    @Test
    fun `canonical identity is checksum-case independent`() {
        val lowercased = usdcOnBase.copy(contract = address(usdcOnBase.contract!!.lowercase))
        assertEquals(usdcOnBase.canonicalId, lowercased.canonicalId)
    }

    @Test
    fun `rejects an implausible asset definition`() {
        assertThrows(IllegalArgumentException::class.java) { ethOnBase.copy(symbol = " ") }
        assertThrows(IllegalArgumentException::class.java) { ethOnBase.copy(decimals = -1) }
        assertThrows(IllegalArgumentException::class.java) { ethOnBase.copy(decimals = 99) }
    }

    @Test
    fun `adding quantities of different assets is refused as an invariant violation`() {
        val sum = AssetAmount(ethOnBase, amount("1")) + AssetAmount(usdcOnBase, amount("1"))
        assertEquals(true, sum is Outcome.Failure)
    }

    @Test
    fun `adding quantities of the same asset succeeds exactly`() {
        val sum = (AssetAmount(usdcOnBase, amount("10.25")) + AssetAmount(usdcOnBase, amount("0.75"))).expect()
        assertEquals(amount("11"), sum.amount)
    }
}
