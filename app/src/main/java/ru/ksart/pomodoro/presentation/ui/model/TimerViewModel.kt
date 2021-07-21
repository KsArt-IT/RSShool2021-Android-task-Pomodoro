package ru.ksart.pomodoro.presentation.ui.model

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.ksart.pomodoro.model.data.TimerAction
import ru.ksart.pomodoro.model.data.TimerWatch
import ru.ksart.pomodoro.utils.DebugHelper

class TimerViewModel : ViewModel() {
    private val timers = mutableListOf<TimerWatch>()
    private var nextId = 0
    private val TIMER_INTERVAL = 100L // 1 раз в секунду
    private var countDownTimer: CountDownTimer? = null
    private var timerCurrent: TimerWatch? = null
    private var fabJob: Job? = null

    // для передачи списока таймеров
    private val _listTimers = MutableStateFlow<List<TimerWatch>>(emptyList())
    val listTimers: StateFlow<List<TimerWatch>> = _listTimers.asStateFlow()

    private val _timerBackgrounded = MutableStateFlow<Long>(0)
    val timerBackgrounded: StateFlow<Long> = _timerBackgrounded.asStateFlow()

    private val _isUseFab = MutableStateFlow<Boolean>(false)
    val isUseFab: StateFlow<Boolean> = _isUseFab.asStateFlow()

    fun init() {
        timers.clear()
        nextId = 0
        countDownTimer?.cancel()
        countDownTimer = null
        timerCurrent = null
        fabJob?.cancel()
        fabJob = null
    }

    fun startTimerBackground() {
        timerCurrent?.takeIf { it.isStarted }?.run {
            DebugHelper.log("TimerViewModel|startTimerBackground")
            _timerBackgrounded.value = current
        }
    }

    fun stopTimerBackground() {
        if (_timerBackgrounded.value > 0) {
            DebugHelper.log("TimerViewModel|stopTimerBackground")
            _timerBackgrounded.value = -1
        }
    }

    fun addTimer(sec: Long) {
        // свернуть кнопку FAB
        rollupFab()
        if (nextId == Int.MAX_VALUE) return
        val time = sec * 1000
        val timer = TimerWatch(
            id = nextId++,
            time = time,
            current = time,
        )
        timers.add(timer)
        setList()
    }

    fun rollupFab() {
        DebugHelper.log("TimerViewModel|rollupFab in")
        _isUseFab.value = false
        fabJob?.cancel()
        fabJob = viewModelScope.launch(Dispatchers.Default) {
            delay(7000)
            _isUseFab.value = true
            DebugHelper.log("TimerViewModel|rollupFab use")
        }
    }

    fun onTimerListener(timerId: Int, action: TimerAction) {
        timers.firstOrNull { it.id == timerId }?.let { timerWatch ->
            when (action) {
                TimerAction.START -> startTimer(timerWatch)
                TimerAction.STOP -> stopTimerCurrent(timerWatch.id)
                TimerAction.RESTART -> return //restartTimer(timerWatch)
                TimerAction.DELETE -> deleteTimer(timerWatch)
            }
        }
    }

    private fun stopTimerCurrent() {
        timerCurrent?.run {
            countDownTimer?.cancel()
            isStarted = false
            setListByTimerCurrent()
        }
    }

    private fun stopTimerCurrent(id: Int) {
        timerCurrent?.takeIf { it.id == id }?.let {
            stopTimerCurrent()
//            setListByTimerCurrent()
        }
    }

    private fun startTimer(timerWatch: TimerWatch) {
        stopTimerCurrent()
        timerCurrent = timerWatch.copy(
            isStarted = true,
            isFinished = false,
        )
        timerCurrent?.run {
            countDownTimer = getCountDownTimer(this)
            countDownTimer?.start()

            setListByTimerCurrent()
        }
    }

    private fun getCountDownTimer(timerWatch: TimerWatch): CountDownTimer {
        return object : CountDownTimer(timerWatch.current, TIMER_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                if (timerWatch.isStarted) {
                    timerWatch.current -= TIMER_INTERVAL
                    DebugHelper.log("TimerViewModel|onTick set list id=${timerWatch.id}, time=${timerWatch.current}")
                    setListByTimerCurrent()
                }
            }

            override fun onFinish() {
                DebugHelper.log("TimerViewModel|onFinish set list id=${timerWatch.id}, time=${timerWatch.current}")
                timerWatch.run {
                    isStarted = false
                    isFinished = true
                    current = time
                }
                setListByTimerCurrent()
            }
        }
    }

/*
    private fun stopTimer(timerWatch: TimerWatch) {
        stopTimerCurrent(timerWatch)
        timerWatch.isStarted = false
        setList()
    }
*/

/*
    private fun restartTimer(timerWatch: TimerWatch) {
        timerWatch.isStarted = false
        timerWatch.current = timerWatch.time
        startTimer(timerWatch)
    }
*/

    private fun deleteTimer(timerWatch: TimerWatch) {
        stopTimerCurrent(timerWatch.id)
        timers.remove(timerWatch)
        setList()
    }

    private fun setListByTimerCurrent() {
        timerCurrent?.run {
            val index = timers.indexOfFirst { it.id == id }
            timers[index] = copy(
                current = current,
                isStarted = isStarted,
                isFinished = isFinished,
            )
            setList()
        }
    }

    private fun setList() {
        DebugHelper.log("TimerViewModel|setList")
        _listTimers.value = timers.toList()
//        _listTimers.value = _listTimers.value.first.not() to timers.toList()
    }

    override fun onCleared() {
        countDownTimer?.cancel()
        fabJob?.cancel()
        super.onCleared()
    }
}