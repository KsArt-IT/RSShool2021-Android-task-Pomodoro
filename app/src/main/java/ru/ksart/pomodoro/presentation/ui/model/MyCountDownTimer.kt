package ru.ksart.pomodoro.presentation.ui.model

import android.os.CountDownTimer

class MyCountDownTimer(
    timeMs: Long,
    onTickTimer: (time: Long) -> Unit,
    onFinishTimer: () -> Unit
) {
    private var countDownTimer: CountDownTimer? = null

    init {
        countDownTimer = getCountDownTimer(timeMs, onTickTimer, onFinishTimer)
    }

    fun start() {
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun getCountDownTimer(
        timeMs: Long,
        onTickTimer: (time: Long) -> Unit,
        onFinishTimer: () -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(timeMs, TIMER_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                onTickTimer(millisUntilFinished)
            }

            override fun onFinish() {
                onFinishTimer()
            }
        }
    }

    companion object {
        const val TIMER_INTERVAL = 100L
    }
}
