package ru.ksart.pomodoro.presentation.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.databinding.ItemTimerBinding
import ru.ksart.pomodoro.model.data.TimerAction
import ru.ksart.pomodoro.model.data.TimerWatch
import ru.ksart.pomodoro.utils.DebugHelper
import ru.ksart.pomodoro.utils.isAndroid6
import ru.ksart.pomodoro.presentation.extensions.displayTime

class TimerViewHolder(
    private val binding: ItemTimerBinding,
    onTimerListener: (timerId: Int, action: TimerAction) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private var id = -1

    init {
        binding.startPauseButton.setOnClickListener {
            DebugHelper.log("TimerViewHolder|startPauseButton id=$id")
            onTimerListener(
                id,
                if (binding.blinkingIndicator.isInvisible) {
                    binding.blinkingIndicator.isInvisible = false
                    TimerAction.START
                } else {
                    binding.blinkingIndicator.isInvisible = true
                    TimerAction.STOP
                }
            )
        }
        binding.deleteButton.setOnClickListener {
            DebugHelper.log("TimerViewHolder|deleteButton id=$id")
            onTimerListener(id, TimerAction.DELETE)
        }
    }

    private fun changeBackgroundColor(finished: Boolean, context: Context) {
        val backgroundColor = if (finished) R.color.red_400 else R.color.red_50

        DebugHelper.log("TimerViewHolder|changeBackgroundColor finished=$finished color=$backgroundColor")
        binding.root.setCardBackgroundColor(
            if (isAndroid6) context.getColor(backgroundColor)
            else context.resources.getColor(backgroundColor)
        )
    }

    private fun changeButton(started: Boolean, context: Context) {
        DebugHelper.log("TimerViewHolder|changeButton started=$started")
        if (started) {
            binding.startPauseButton.text = context.getString(R.string.pause_button_text)
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        } else {
            binding.startPauseButton.text = context.getString(R.string.start_button_text)
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun bind(timer: TimerWatch) {
        DebugHelper.log("----------------------------------------------")
        DebugHelper.log("TimerViewHolder|bind id=$id ? timer.id=${timer.id}")
        val isTimerChanged = id != timer.id
        val isTimerChangedStart = timer.isStartedOld != timer.isStarted
        val isTimerChangedFinished = timer.isFinishedOld != timer.isFinished
        id = timer.id
        timer.currentOld = timer.current
        timer.isStartedOld = timer.isStarted
        timer.isFinishedOld = timer.isFinished
        // отобразим время
        binding.timer.text = timer.current.displayTime()
        // отобразим прогресс
        binding.progress.setCurrent(timer.current)
        // изменился элемент, обновить все: цвет фона, иконка кнопки, надпись кнопки
        if (isTimerChanged) {
            binding.progress.setPeriod(timer.time)
            binding.progress.setCurrent(timer.current)
            DebugHelper.log("TimerViewHolder|isTimerChanged ----------------------------------")
            changeBackgroundColor(timer.isFinished, binding.root.context)
            changeButton(timer.isStarted, binding.root.context)
        } else {
            if (isTimerChangedFinished) {
                DebugHelper.log("TimerViewHolder|isFinished ----------------------------------")
                changeBackgroundColor(timer.isFinished, binding.root.context)
            }
            if (isTimerChangedStart) {
                DebugHelper.log("TimerViewHolder|isTimerChangedStart ----------------------------------")
                changeButton(timer.isStarted, binding.root.context)
            }
        }
    }
}