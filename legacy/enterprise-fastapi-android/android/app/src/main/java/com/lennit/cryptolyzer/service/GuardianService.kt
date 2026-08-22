package com.lennit.cryptolyzer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lennit.cryptolyzer.MainActivity
import com.lennit.cryptolyzer.R

/**
 * MODULE 10: GuardianService — foreground service keeping agents alive.
 * Runs as a persistent notification so Android cannot kill the process.
 */
class GuardianService : Service() {

    companion object {
        private const val CHANNEL_ID      = "LENNIT_GUARDIAN"
        private const val NOTIFICATION_ID = 8821
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createChannel()

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lennit CryptoLyzer")
            .setContentText("⚡ Sovereign Bargaining Loop: ACTIVE")
            .setSmallIcon(R.drawable.ic_guardian)
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Guardian Monitor", NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Keeps trading agents alive in background"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
