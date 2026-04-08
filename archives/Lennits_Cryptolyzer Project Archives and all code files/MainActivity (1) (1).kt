package com.lennit.core

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val webView = WebView(this)
        setContentView(webView)

        // Enable 2026-spec features for the Command Cockpit
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Required for WebGPU/WASM
        webView.settings.allowFileAccess = true
        
        webView.webViewClient = WebViewClient()
        
        // Load the local "Admin Console" from your assets folder
        webView.loadUrl("file:///android_asset/index.html")
    }
}
