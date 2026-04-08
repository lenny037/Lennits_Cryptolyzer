package com.lennit.cryptolyzer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lennit.cryptolyzer.security.BiometricGatekeeper
import com.lennit.cryptolyzer.service.GuardianService
import dagger.hilt.android.AndroidEntryPoint

/**
 * MODULE 01: Single-activity host.
 * Hosts the PWA WebView and exposes the LennitBridge JS interface.
 * Also launches the GuardianService foreground service.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var gatekeeper: BiometricGatekeeper

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gatekeeper = BiometricGatekeeper(this)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        startService(Intent(this, GuardianService::class.java))
        setupWebView()
        setContentView(webView)

        val prefs    = getSharedPreferences("lennit_prefs", MODE_PRIVATE)
        val endpoint = prefs.getString("api_endpoint", "") ?: ""
        webView.loadUrl(if (endpoint.isNotBlank()) endpoint else "file:///android_asset/index.html")
    }

    private fun setupWebView() {
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled        = true
                domStorageEnabled        = true
                allowFileAccess          = true
                allowContentAccess       = true
                mixedContentMode         = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                cacheMode                = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort          = true
                loadWithOverviewMode     = true
            }
            addJavascriptInterface(LennitBridge(), "LennitBridge")
            webViewClient = object : android.webkit.WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: android.webkit.WebResourceRequest,
                ) = false
            }
        }
    }

    // ── JavaScript Bridge — MODULE 10 ─────────────────────────────────────
    inner class LennitBridge {

        @JavascriptInterface
        fun triggerSafeShutdown() {
            runOnUiThread {
                gatekeeper.authenticate { success ->
                    if (success) {
                        stopService(Intent(this@MainActivity, GuardianService::class.java))
                        webView.evaluateJavascript(
                            "window.dispatchEvent(new CustomEvent('lennit:shutdown'))", null
                        )
                    }
                }
            }
        }

        @JavascriptInterface
        fun triggerSafeMode() {
            runOnUiThread {
                gatekeeper.authenticate { success ->
                    if (success) {
                        webView.evaluateJavascript(
                            "window.dispatchEvent(new CustomEvent('lennit:safemode'))", null
                        )
                    }
                }
            }
        }

        @JavascriptInterface
        fun saveSetting(key: String, value: String) {
            val safeKey = key.take(64)
            getSharedPreferences("lennit_prefs", MODE_PRIVATE)
                .edit().putString(safeKey, value).apply()
        }

        @JavascriptInterface
        fun getSetting(key: String): String {
            return getSharedPreferences("lennit_prefs", MODE_PRIVATE)
                .getString(key.take(64), "") ?: ""
        }

        @JavascriptInterface
        fun getDeviceInfo(): String {
            return """{"model":"${Build.MODEL}","sdk":${Build.VERSION.SDK_INT},"brand":"${Build.BRAND}"}"""
        }

        @JavascriptInterface
        fun triggerBiometricAuth(callback: String) {
            runOnUiThread {
                gatekeeper.authenticateWithResult { result ->
                    val js = when (result) {
                        is BiometricGatekeeper.AuthResult.Success ->
                            "$callback({\"success\":true,\"error\":null})"
                        is BiometricGatekeeper.AuthResult.Failure ->
                            "$callback({\"success\":false,\"error\":\"biometric_failure\"})"
                        is BiometricGatekeeper.AuthResult.Error ->
                            "$callback({\"success\":false,\"error\":\"${result.message}\"})"
                    }
                    webView.evaluateJavascript(js, null)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
