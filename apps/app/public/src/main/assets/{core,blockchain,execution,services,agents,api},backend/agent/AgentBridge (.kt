package com.lennit.core

import android.util.Log

class AgentBridge {
    companion object {
        // Load the Rust library compiled via NDK
        init {
            try {
                System.loadLibrary("genesis_brain")
                Log.i("LENNIT_BRIDGE", "> NATIVE BRAIN LOADED SECURELY")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("LENNIT_BRIDGE", "> FATAL: FAILED TO LOAD NATIVE BRAIN", e)
            }
        }
    }

    // Initialize the Snapdragon 865 NPU with the 41C thermal limit
    external fun initializeNpu(thermalLimit: Int): Boolean

    // Pass a serialized tensor (or agent command) to Rust for processing
    external fun executeInference(inputData: FloatArray): FloatArray
    
    // Trigger the ZK-Proof generation for the "Society" network
    external fun generateSolvencyProof(balance: Long): ByteArray
}
