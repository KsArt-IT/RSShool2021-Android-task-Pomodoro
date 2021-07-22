package ru.ksart.pomodoro.presentation.ui.model

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.ksart.pomodoro.model.data.TimerAction
import ru.ksart.pomodoro.model.data.TimerWatch
import ru.ksart.pomodoro.utils.DebugHelper

class TimerViewModel : ViewModel() {
    private val timers = mutableListOf<TimerWatch>()
    private var nextId = 0L
    private var countDownTimer: MyCountDownTimer? = null
    private var timerCurrent: TimerWatch? = null
    private var fabJob: Job? = null

    // для передачи списока таймеров
    private val _listTimers = MutableStateFlow<List<TimerWatch>>(emptyList())
    val listTimers: StateFlow<List<TimerWatch>> = _listTimers.asStateFlow()

    // установка времени таймера для сервиса
    private val _timerBackgrounded = MutableStateFlow(0L to 0L)
    val timerBackgrounded: StateFlow<Pair<Long, Long>> = _timerBackgrounded.asStateFlow()

    // для сворачивания кнопок
    private val _isUseFab = MutableStateFlow(false)
    val isUseFab: StateFlow<Boolean> = _isUseFab.asStateFlow()

/*
    fun init() {
        timers.clear()
        nextId = 0
        decCountDownTimer?.cancel()
        decCountDownTimer = null
        timerCurrent = null
        fabJob?.cancel()
        fabJob = null
    }
*/

    fun startTimerBackground() {
        timerCurrent?.takeIf { it.isStarted }?.run {
            DebugHelper.log("TimerViewModel|startTimerBackground")
            _timerBackgrounded.value = SystemClock.elapsedRealtime() to current
        }
    }

    fun stopTimerBackground() {
        if (_timerBackgrounded.value.second > 0) {
            DebugHelper.log("TimerViewModel|stopTimerBackground")
            _timerBackgrounded.value = 0L to -1
        }
    }

    fun addTimer(sec: Long) {
        if (nextId == Int.MAX_VALUE.toLong()) return
        // свернуть кнопку FAB
        rollupFab()
        val time = sec * 1000
        val timer = TimerWatch(
            id = nextId++,
            startTime = time,
            current = time,
        )
        DebugHelper.log("TimerViewModel|addTimer id=${timer.id} id=${timer.startTime}")
        timers.add(timer)
        setList()
    }

    fun rollupFab() {
//        DebugHelper.log("TimerViewModel|rollupFab in")
        _isUseFab.value = false
        fabJob?.cancel()
        fabJob = viewModelScope.launch(Dispatchers.Default) {
            delay(6000)
            _isUseFab.value = true
//            DebugHelper.log("TimerViewModel|rollupFab use")
        }
    }

    fun onTimerListener(timerId: Long, action: TimerAction) {
        timers.firstOrNull { it.id == timerId }?.let { timerWatch ->
            when (action) {
                TimerAction.START -> startTimer(timerWatch)
                TimerAction.STOP -> stopTimerCurrent(timerWatch.id)
                TimerAction.RESTART -> return //restartTimer(timerWatch)
                TimerAction.DELETE -> deleteTimer(timerWatch)
            }
        }
    }

    private fun stopTimerCurrent(update: Boolean = true) {
        DebugHelper.log("TimerViewModel|stopTimerCurrent")
        timerCurrent?.run {
            countDownTimer?.cancel()
            isStarted = false
            if (update) setListByTimerCurrent()
        }
    }

    private fun stopTimerCurrent(id: Long, update: Boolean = true) {
        DebugHelper.log("TimerViewModel|stopTimerCurrent id=$id")
        timerCurrent?.takeIf { it.id == id }?.let {
            stopTimerCurrent(update)
        }
    }

    private fun startTimer(timerWatch: TimerWatch) {
        DebugHelper.log("TimerViewModel|startTimer id=${timerWatch.id}")
        stopTimerCurrent()
        timerCurrent = timerWatch.copy(
            isStarted = true,
            isFinished = false,
        )
        timerCurrent?.run {
            countDownTimer = MyCountDownTimer(current, ::onTickTimer, ::onFinishTimer)
            countDownTimer?.start()

            setListByTimerCurrent()
        }
    }

    private fun onTickTimer(time: Long) {
        viewModelScope.launch {
            timerCurrent?.run {
                DebugHelper.log("TimerViewModel|onTickTimer time=$time current=$current")
                current = time

                setListByTimerCurrent()
            }
        }
    }

    private fun onFinishTimer() {
        viewModelScope.launch {
            timerCurrent?.run {
                DebugHelper.log("TimerViewModel|onFinishTimer current=$current")
                // если текущее значение не 0, установим 0
                if (current != 0L) onTickTimer(0)
                // небольшая задержка перед сбросом таймера
                delay(250)
                isStarted = false
                isFinished = true
                current = startTime

                setListByTimerCurrent()
            }
        }
    }

    private fun deleteTimer(timerWatch: TimerWatch) {
        DebugHelper.log("TimerViewModel|deleteTimer id=${timerWatch.id}")
        stopTimerCurrent(timerWatch.id, update = false)
        timers.remove(timerWatch)
        setList()
    }

    private fun setListByTimerCurrent() {
        timerCurrent?.run {
            timers.indexOfFirst { it.id == id }.takeIf { it >= 0 }?.let { index ->
                DebugHelper.log("TimerViewModel|setListByTimerCurrent index=$index id=$id")
                timers[index] = copy()
                setList()
            }
        }
    }

    private fun setList() {
        DebugHelper.log("TimerViewModel|setList")
        _listTimers.value = timers.toList()
    }

    override fun onCleared() {
        stopTimerBackground()
        countDownTimer?.cancel()
        fabJob?.cancel()
        super.onCleared()
    }
}