package ru.ksart.pomodoro.presentation.ui.model

import android.os.CountDownTimer

class DecCountDownTimer(
    private val onTick: () -> Unit,
    private val onFinish: () -> Unit
) {
    private var countDownTimer: CountDownTimer? = null
    private var current = 0L

    fun initTimer(timer: Long) {
        cancel()
        current = timer
        countDownTimer = getCountDownTimer()
    }

    fun start() {
        countDownTimer?.start()
    }

    fun cancel() {
        countDownTimer?.cancel()
    }

    private fun getCountDownTimer(): CountDownTimer {
        return object : CountDownTimer(current, TIMER_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                current -= TIMER_INTERVAL
                onTick
            }

            override fun onFinish() {
                onFinish
            }
        }
    }

    companion object {
        const val TIMER_INTERVAL = 100L
    }
}