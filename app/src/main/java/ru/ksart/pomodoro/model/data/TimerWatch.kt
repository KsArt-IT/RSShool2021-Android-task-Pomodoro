package ru.ksart.pomodoro.model.data

data class TimerWatch(
    val id: Long,
    var startTime: Long,
    var current: Long,
    var isStarted: Boolean = false,
    var isFinished: Boolean = false,
)
