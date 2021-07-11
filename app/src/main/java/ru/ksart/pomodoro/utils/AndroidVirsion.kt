package ru.ksart.pomodoro.utils

import android.os.Build

// Проверка на SDK M-Android 6 API 23
val isAndroid6: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

// Проверка на SDK N-Android 7 API 24
val isAndroid7: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

// Проверка на SDK O-Android 8 API 26
val isAndroid8: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

// Проверка на SDK Q-Android 10 API 29
val isAndroidQ: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

// Проверка на SDK R-Android 11 API 30
val isAndroidR: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
