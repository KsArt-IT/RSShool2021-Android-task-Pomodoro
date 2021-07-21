package ru.ksart.pomodoro.presentation.ui.model

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
    private var decCountDownTimer: DecCountDownTimer? = null
    private var timerCurrent: TimerWatch? = null
    private var fabJob: Job? = null

    // для передачи списока таймеров
    private val _listTimers = MutableStateFlow<List<TimerWatch>>(emptyList())
    val listTimers: StateFlow<List<TimerWatch>> = _listTimers.asStateFlow()

    // установка времени таймера для сервиса
    private val _timerBackgrounded = MutableStateFlow(0L)
    val timerBackgrounded: StateFlow<Long> = _timerBackgrounded.asStateFlow()

    // для сворачивания кнопок
    private val _isUseFab = MutableStateFlow(false)
    val isUseFab: StateFlow<Boolean> = _isUseFab.asStateFlow()

    fun init() {
        timers.clear()
        nextId = 0
        decCountDownTimer?.cancel()
        decCountDownTimer = null
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
            decCountDownTimer?.cancel()
            isStarted = false
            setListByTimerCurrent()
        }
    }

    private fun stopTimerCurrent(id: Int) {
        timerCurrent?.takeIf { it.id == id }?.let {
            stopTimerCurrent()
        }
    }

    private fun startTimer(timerWatch: TimerWatch) {
        stopTimerCurrent()
        timerCurrent = timerWatch.copy(
            isStarted = true,
            isFinished = false,
        )
        timerCurrent?.run {
            decCountDownTimer = DecCountDownTimer(current, ::onTickTimer, ::onFinishTimer)
            decCountDownTimer?.start()

            setListByTimerCurrent()
        }
    }

    private fun onTickTimer(time: Long) {
        timerCurrent?.run {
            current = time

            setListByTimerCurrent()
        }
    }

    private fun onFinishTimer() {
        timerCurrent?.run {
            isStarted = false
            isFinished = true
            current = time

            setListByTimerCurrent()
        }
    }

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
    }

    override fun onCleared() {
        decCountDownTimer?.cancel()
        fabJob?.cancel()
        super.onCleared()
    }
}