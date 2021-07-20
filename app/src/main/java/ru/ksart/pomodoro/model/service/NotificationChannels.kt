package ru.ksart.pomodoro.model.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import ru.ksart.pomodoro.utils.isAndroid8

object NotificationChannels {

    const val TIMER_CHANNEL_ID = "timer_content"

    fun create(context: Context) {
        // создаем каналы для Android 8+ (O)
        if (isAndroid8) {
            createPlayChannel(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPlayChannel(context: Context) {
        val name = "Pomodoro"
        val channelDescription = "Timer notification show"
        val priority = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(TIMER_CHANNEL_ID, name, priority).apply {
            description = channelDescription
            // включим вибрацию
            enableVibration(true)
        }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

}
