package ru.ksart.pomodoro.presentation.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ru.ksart.pomodoro.databinding.ItemTimerBinding
import ru.ksart.pomodoro.model.data.TimerAction
import ru.ksart.pomodoro.model.data.TimerWatch
import ru.ksart.pomodoro.presentation.extensions.inflate

class TimerAdapter(
    private val onTimerListener: (timerId: Long, action: TimerAction) -> Unit,
) : ListAdapter<TimerWatch, TimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        return TimerViewHolder(
            parent.inflate(ItemTimerBinding::inflate),
            onTimerListener,
        )
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {

        private val itemComparator = object : DiffUtil.ItemCallback<TimerWatch>() {

            override fun areItemsTheSame(oldItem: TimerWatch, newItem: TimerWatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TimerWatch, newItem: TimerWatch): Boolean {
                return oldItem.current == newItem.current &&
                        oldItem.isStarted == newItem.isStarted &&
                        oldItem.isFinished == newItem.isFinished
            }

            // override fun getChangePayload(oldItem: TimerWatch, newItem: TimerWatch) = Any()
            override fun getChangePayload(oldItem: TimerWatch, newItem: TimerWatch): Any? {
                return if (oldItem.current != newItem.current) Any() else null
            }
        }
    }
}