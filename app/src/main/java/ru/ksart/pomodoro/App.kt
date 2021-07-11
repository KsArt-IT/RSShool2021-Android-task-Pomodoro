package ru.ksart.pomodoro

import android.app.Application
import android.os.StrictMode
import androidx.viewbinding.BuildConfig
import com.jakewharton.threetenabp.AndroidThreeTen
import ru.ksart.pomodoro.utils.DebugHelper

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DebugHelper.log("Application - init")
        // инициализируем библиотеку для работы с датой
        AndroidThreeTen.init(this)
        // режим отладки
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
    }
}
