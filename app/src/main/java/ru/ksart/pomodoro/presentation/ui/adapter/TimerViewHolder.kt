package ru.ksart.pomodoro.presentation.ui.adapter

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.databinding.ItemTimerBinding
import ru.ksart.pomodoro.model.data.TimerAction
import ru.ksart.pomodoro.model.data.TimerWatch
import ru.ksart.pomodoro.presentation.extensions.displayTime
import ru.ksart.pomodoro.utils.DebugHelper
import ru.ksart.pomodoro.utils.isAndroid6

class TimerViewHolder(
    private val binding: ItemTimerBinding,
    onTimerListener: (timerId: Long, action: TimerAction) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private var id = -1L
    private var isStarted = false
    private var isFinished = false

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

    private fun changeButton(started: Boolean) {
        DebugHelper.log("TimerViewHolder|changeButton started=$started")
        if (started) {
            binding.startPauseButton.setText(R.string.pause_button_text)
            binding.blinkingIndicator.isInvisible = false
            (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
        } else {
            binding.startPauseButton.setText(R.string.start_button_text)
            binding.blinkingIndicator.isInvisible = true
            (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
        }
    }

    fun bind(timer: TimerWatch) {
        DebugHelper.log("----------------------------------------------")
        DebugHelper.log("TimerViewHolder|bind id=$id ? timer.id=${timer.id}")
        if (id != timer.id) {
            // обновляем все
            id = timer.id
            isStarted = timer.isStarted
            isFinished = timer.isFinished

            binding.progress.setPeriod(timer.startTime)

            DebugHelper.log("TimerViewHolder|isTimerChanged ----------------------------------")

            changeBackgroundColor(timer.isFinished, binding.root.context)
            changeButton(timer.isStarted)
        } else {
            // обновляем только изменения
            if (isFinished != timer.isFinished) {
                isFinished = timer.isFinished
                DebugHelper.log("TimerViewHolder|isFinished ----------------------------------")
                changeBackgroundColor(timer.isFinished, binding.root.context)
            }
            if (isStarted != timer.isStarted) {
                isStarted = timer.isStarted
                DebugHelper.log("TimerViewHolder|isTimerChangedStart ----------------------------------")
                changeButton(timer.isStarted)
            }
        }
        // отобразим время
        binding.timer.text = timer.current.displayTime()
        // отобразим прогресс
        binding.progress.setCurrent(timer.current)
    }
}