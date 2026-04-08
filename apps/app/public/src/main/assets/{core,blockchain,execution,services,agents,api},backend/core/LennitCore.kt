package com.lennit.core
import android.app.*
import android.content.Intent
import android.os.*
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class GuardianService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "G_PULSE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Genesis", NotificationManager.IMPORTANCE_MAX)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        startForeground(8821, NotificationCompat.Builder(this, channelId)
            .setContentTitle("LENNIT SOVEREIGN").setContentText("EXECUTION_READY").setSmallIcon(android.R.drawable.ic_lock_power_save).build())
        return START_STICKY 
    }
    override fun onBind(intent: Intent?) = null
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, GuardianService::class.java))
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(webView)
    }
}