package com.lennit.cryptolyzer.domain

/**
 * Minimal Keccak-256 (the pre-NIST padding variant Ethereum uses, not SHA3-256).
 *
 * Implemented here on purpose: EIP-55 checksum validation belongs in the pure domain layer, and
 * pulling a full crypto provider such as BouncyCastle into `core:domain` just to hash 40 ASCII
 * characters would add ~7 MB and a platform-specific dependency to the most-depended-on module.
 * This is not used for signing, key derivation, or any security decision.
 */
internal object Keccak256 {

    private const val RATE_BYTES = 136 // 1088 bits for Keccak-256
    private const val ROUNDS = 24

    private val ROUND_CONSTANTS = longArrayOf(
        0x0000000000000001L, 0x0000000000008082L, -0x7fffffffffff7f76L, -0x7fffffff7fff8000L,
        0x000000000000808bL, 0x0000000080000001L, -0x7fffffff7fff7f7fL, -0x7fffffffffff7ff7L,
        0x000000000000008aL, 0x0000000000000088L, 0x0000000080008009L, 0x000000008000000aL,
        0x000000008000808bL, -0x7fffffffffffff75L, -0x7fffffffffff7f77L, -0x7fffffffffff7ffdL,
        -0x7fffffffffff7ffeL, -0x7fffffffffffff80L, 0x000000000000800aL, -0x7fffffff7ffffff6L,
        -0x7fffffff7fff7f7fL, -0x7fffffffffff7f80L, 0x0000000080000001L, -0x7fffffff7fff7ff8L,
    )

    private val ROTATION_OFFSETS = intArrayOf(
        0, 1, 62, 28, 27, 36, 44, 6, 55, 20, 3, 10, 43, 25, 39, 41, 45, 15, 21, 8, 18, 2, 61, 56, 14,
    )

    fun digest(input: ByteArray): ByteArray {
        val state = LongArray(25)
        val padded = pad(input)
        var offset = 0
        while (offset < padded.size) {
            for (i in 0 until RATE_BYTES / 8) {
                state[i] = state[i] xor readLongLe(padded, offset + i * 8)
            }
            keccakF(state)
            offset += RATE_BYTES
        }
        val out = ByteArray(32)
        for (i in 0 until 4) {
            writeLongLe(out, i * 8, state[i])
        }
        return out
    }

    private fun pad(input: ByteArray): ByteArray {
        val padLength = RATE_BYTES - (input.size % RATE_BYTES)
        val padded = ByteArray(input.size + padLength)
        input.copyInto(padded)
        padded[input.size] = 0x01 // Keccak domain padding, not SHA-3's 0x06
        padded[padded.size - 1] = (padded[padded.size - 1].toInt() or 0x80).toByte()
        return padded
    }

    private fun keccakF(state: LongArray) {
        val c = LongArray(5)
        val b = LongArray(25)
        for (round in 0 until ROUNDS) {
            for (x in 0 until 5) {
                c[x] = state[x] xor state[x + 5] xor state[x + 10] xor state[x + 15] xor state[x + 20]
            }
            for (x in 0 until 5) {
                val d = c[(x + 4) % 5] xor java.lang.Long.rotateLeft(c[(x + 1) % 5], 1)
                for (y in 0 until 25 step 5) {
                    state[y + x] = state[y + x] xor d
                }
            }
            for (x in 0 until 5) {
                for (y in 0 until 5) {
                    val index = x + 5 * y
                    b[y + 5 * ((2 * x + 3 * y) % 5)] =
                        java.lang.Long.rotateLeft(state[index], ROTATION_OFFSETS[index])
                }
            }
            for (x in 0 until 5) {
                for (y in 0 until 5) {
                    state[x + 5 * y] = b[x + 5 * y] xor (b[(x + 1) % 5 + 5 * y].inv() and b[(x + 2) % 5 + 5 * y])
                }
            }
            state[0] = state[0] xor ROUND_CONSTANTS[round]
        }
    }

    private fun readLongLe(bytes: ByteArray, offset: Int): Long {
        var result = 0L
        for (i in 7 downTo 0) {
            result = (result shl 8) or (bytes[offset + i].toLong() and 0xff)
        }
        return result
    }

    private fun writeLongLe(bytes: ByteArray, offset: Int, value: Long) {
        for (i in 0 until 8) {
            bytes[offset + i] = ((value ushr (8 * i)) and 0xff).toByte()
        }
    }
}
