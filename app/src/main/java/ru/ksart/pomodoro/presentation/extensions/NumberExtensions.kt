package ru.ksart.pomodoro.presentation.extensions

const val START_TIME = "00:00:00"
private const val TO_SECONDS = 1000

fun Long.displayTime(): String {
    if (this <= 0L) {
        return START_TIME
    }
    val time = (if (this % TO_SECONDS == 0L) this else this + TO_SECONDS) / TO_SECONDS
    val h = time / 3600
    val m = time % 3600 / 60
    val s = time % 60

    return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
}

private fun displaySlot(count: Long): String {
    return if (count / 10L > 0) {
        "$count"
    } else {
        "0$count"
    }
}

