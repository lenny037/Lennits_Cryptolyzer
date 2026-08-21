package com.lennit.cryptolyzer

import android.app.Activity
import android.os.Bundle
import com.lennit.cryptolyzer.runtime.RuntimeLifecycle

class MainActivity : Activity() {
    private val runtimeLifecycle = RuntimeLifecycle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtimeLifecycle.start()
    }

    override fun onDestroy() {
        if (runtimeLifecycle.state != RuntimeLifecycle.State.NEW) {
            runtimeLifecycle.stop()
        }
        super.onDestroy()
    }
}
