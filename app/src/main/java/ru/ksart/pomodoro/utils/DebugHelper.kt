package ru.ksart.pomodoro.utils

import android.util.Log
import ru.ksart.pomodoro.BuildConfig

object DebugHelper {
    private const val DEBUG = "pomodoro145"

    fun log(msg: String) {
        if (BuildConfig.DEBUG) Log.d(DEBUG, msg)
    }

    fun log(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) Log.e(DEBUG, msg, tr)
    }
}
