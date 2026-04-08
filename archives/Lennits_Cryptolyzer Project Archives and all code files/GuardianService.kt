package com.lennit.core

import android.app.*
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class GuardianService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "LENNIT_PULSE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Genesis Core", NotificationManager.IMPORTANCE_MAX)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        // START_STICKY: Ensures OS resurrection if memory is cleared
        startForeground(8821, NotificationCompat.Builder(this, channelId)
            .setContentTitle("LENNIT_06: ACTIVE")
            .setContentText("Bargaining Loop: SECURE")
            .setSmallIcon(android.R.drawable.ic_lock_power_save)
            .setOngoing(true)
            .build())

        return START_STICKY
    }
    override fun onBind(intent: Intent?) = null
}
