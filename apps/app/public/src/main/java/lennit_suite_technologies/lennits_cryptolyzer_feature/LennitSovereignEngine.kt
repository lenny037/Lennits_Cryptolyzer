package lennit_suite_technologies.lennits_cryptolyzer_feature
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class GuardianService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "LENNIT_PULSE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Genesis Core", NotificationManager.IMPORTANCE_MAX)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("LENNIT_06: ACTIVE")
            .setContentText("Sovereign Bargaining Loop: SECURE")
            .setSmallIcon(android.R.drawable.ic_lock_power_save)
            .setOngoing(true).build()
        startForeground(8821, notification)
        return START_STICKY 
    }
    override fun onBind(intent: Intent?) = null
}

class MainActivity : AppCompatActivity() {
    init { System.loadLibrary("genesis_brain") }
    private external fun initNpu(thermalLimit: Int): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, GuardianService::class.java))
        initNpu(41)
        val webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl("file:///android_asset/index.html")
        }
        setContentView(webView)
    }
}