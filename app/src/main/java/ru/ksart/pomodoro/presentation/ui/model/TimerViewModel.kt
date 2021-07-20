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
    private var countDownTimer: CountDownTimer? = null
    private val timerInterval = 1000L // 1 раз в секунду
    private var timerCurrent: TimerWatch? = null
    private var fabJob: Job? = null

    // список таймеров
    private val _listTimers =
        MutableStateFlow<Pair<Boolean, List<TimerWatch>>>(false to emptyList())
    val listTimers: StateFlow<Pair<Boolean, List<TimerWatch>>> = _listTimers.asStateFlow()

    private val _timerBackgrounded = MutableStateFlow<Long>(0)
    val timerBackgrounded: StateFlow<Long> = _timerBackgrounded.asStateFlow()

    private val _isUseFab = MutableStateFlow<Boolean>(false)
    val isUseFab: StateFlow<Boolean> = _isUseFab.asStateFlow()

//    private val _timerForegrounded = MutableStateFlow<Long>(-1)
//    val timerForegrounded: StateFlow<Long> = _timerForegrounded.asStateFlow()

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
            currentOld = time,
            isStarted = false,
            isStartedOld = false,
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
                TimerAction.STOP -> stopTimer(timerWatch)
                TimerAction.RESTART -> restartTimer(timerWatch)
                TimerAction.DELETE -> deleteTimer(timerWatch)
            }
        }
    }

    private fun stopTimerCurrent() {
        timerCurrent?.run {
            countDownTimer?.cancel()
            isStarted = false
        }
    }

    private fun stopTimerCurrent(timerWatch: TimerWatch) {
        timerCurrent?.takeIf { it.id == timerWatch.id }?.let {
            stopTimerCurrent()
        }
    }

    private fun startTimer(timerWatch: TimerWatch) {
        stopTimerCurrent()
        timerWatch.isStarted = true
        timerWatch.isFinished = false
        countDownTimer = getCountDownTimer(timerWatch)
        countDownTimer?.start()
        timerCurrent = timerWatch
        setList()
    }

    private fun getCountDownTimer(timerWatch: TimerWatch): CountDownTimer {
        return object : CountDownTimer(timerWatch.current, timerInterval) {

            override fun onTick(millisUntilFinished: Long) {
                if (timerWatch.isStarted) {
                    timerWatch.current -= timerInterval
                    DebugHelper.log("TimerViewModel|onTick set list id=${timerWatch.id}, time=${timerWatch.current}")
                    setList()
                }
            }

            override fun onFinish() {
                DebugHelper.log("TimerViewModel|onFinish set list id=${timerWatch.id}, time=${timerWatch.current}")
                timerWatch.isStarted = false
                timerWatch.isFinished = true
                timerWatch.current = timerWatch.time
                setList()
            }
        }
    }

    private fun stopTimer(timerWatch: TimerWatch) {
        stopTimerCurrent(timerWatch)
        timerWatch.isStarted = false
        setList()
    }

    private fun restartTimer(timerWatch: TimerWatch) {
        timerWatch.isStarted = false
        timerWatch.current = timerWatch.time
        startTimer(timerWatch)
    }

    private fun deleteTimer(timerWatch: TimerWatch) {
        stopTimerCurrent(timerWatch)
        timers.remove(timerWatch)
        setList()
    }

    private fun setList() {
        DebugHelper.log("TimerViewModel|setList")
        _listTimers.value = _listTimers.value.first.not() to timers.toList()
    }

    override fun onCleared() {
        countDownTimer?.cancel()
        fabJob?.cancel()
        super.onCleared()
    }
}