package ru.ksart.pomodoro.model.data

data class TimerWatch(
    val id: Int,
    var time: Long,
    var current: Long,
    var currentOld: Long,
    var isStarted: Boolean,
    var isStartedOld: Boolean,
    var isFinished: Boolean = false,
    var isFinishedOld: Boolean = false,
)
