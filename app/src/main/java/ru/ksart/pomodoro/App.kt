package ru.ksart.pomodoro

import android.app.Application
import android.os.StrictMode
import ru.ksart.pomodoro.model.service.NotificationChannels
import ru.ksart.pomodoro.utils.DebugHelper

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugHelper.log("Application - init")
        // создадим канал
        NotificationChannels.create(this)
        // режим отладки
/*
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyDeath()
                    .build()
            )
        }
*/
    }
}
