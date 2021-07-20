package ru.ksart.pomodoro.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.ksart.pomodoro.databinding.DialogAddTimerBinding
import ru.ksart.pomodoro.utils.DebugHelper

class AddTimerDialogFragment : BottomSheetDialogFragment() {
    private var _binding: DialogAddTimerBinding? = null
    private val binding: DialogAddTimerBinding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTimerBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTimerPickers(savedInstanceState)
        initListener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putInt(KEY_HOURS, binding.hoursPicker.value)
            putInt(KEY_MINUTES, binding.minutesPicker.value)
            putInt(KEY_SECONDS, binding.secondsPicker.value)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // настроим переключатель
    private fun initTimerPickers(savedInstanceState: Bundle?) {
        binding.hoursPicker.run {
            minValue = 0
            maxValue = 23
            value = savedInstanceState?.getInt(KEY_HOURS, 0) ?: 0
            wrapSelectorWheel = true
        }
        binding.minutesPicker.run {
            minValue = 0
            maxValue = 59
            value = savedInstanceState?.getInt(KEY_MINUTES, 0) ?: 0
            wrapSelectorWheel = true
        }
        binding.secondsPicker.run {
            minValue = 0
            maxValue = 59
            value = savedInstanceState?.getInt(KEY_SECONDS, 0) ?: 0
            wrapSelectorWheel = true
        }
    }

    private fun initListener() {
        binding.addButton.setOnClickListener {
            var time = binding.hoursPicker.value * 3600L
            time += binding.minutesPicker.value * 60
            time += binding.secondsPicker.value
            DebugHelper.log("AddTimerDialogFragment|initListener time=$time")
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                MainFragment.KEY_ADD_TIMER,
                time
            )
        }
    }

    private companion object {
        private const val KEY_HOURS = "key_hours_picker"
        private const val KEY_MINUTES = "key_minutes_picker"
        private const val KEY_SECONDS = "key_seconds_picker"
    }
}