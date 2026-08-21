package com.lennit.cryptolyzer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * MODULE 01: Application class
 * Hilt DI entry point.
 */
@HiltAndroidApp
class CryptolyzerApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
