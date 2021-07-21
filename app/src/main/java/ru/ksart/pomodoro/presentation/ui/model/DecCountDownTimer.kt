package ru.ksart.pomodoro.presentation.ui.model

import android.os.CountDownTimer

class DecCountDownTimer(
    private var timeMs: Long,
    private val onTickTimer: (time: Long) -> Unit,
    private val onFinishTimer: () -> Unit
) {
    private var countDownTimer: CountDownTimer? = null

    init {
        countDownTimer = getCountDownTimer()
    }

    fun start() {
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(timeMs, TIMER_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                timeMs -= TIMER_INTERVAL
                onTickTimer(timeMs)
            }

            override fun onFinish() {
                onFinishTimer
            }
        }
    }

    companion object {
        const val TIMER_INTERVAL = 100L
    }
}