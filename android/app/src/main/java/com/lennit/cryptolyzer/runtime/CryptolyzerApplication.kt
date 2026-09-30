package com.lennit.cryptolyzer.runtime

import android.app.Application
import com.lennit.cryptolyzer.persistence.SqliteEventDatabase

class CryptolyzerApplication : Application() {
    lateinit var runtimeLifecycle: RuntimeLifecycle
        private set

    lateinit var eventDatabase: SqliteEventDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        runtimeLifecycle = RuntimeLifecycle().also { it.start() }
        eventDatabase = SqliteEventDatabase(this)
    }

    override fun onTerminate() {
        if (::runtimeLifecycle.isInitialized && runtimeLifecycle.state != RuntimeLifecycle.State.STOPPED) {
            runtimeLifecycle.stop()
        }
        if (::eventDatabase.isInitialized) eventDatabase.close()
        super.onTerminate()
    }
}
