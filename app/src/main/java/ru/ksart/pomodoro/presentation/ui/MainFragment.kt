package ru.ksart.pomodoro.presentation.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.Animatable
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.TRANSLATION_Y
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.databinding.FragmentMainBinding
import ru.ksart.pomodoro.model.service.TimerForegroundService
import ru.ksart.pomodoro.presentation.extensions.toast
import ru.ksart.pomodoro.presentation.ui.adapter.TimerAdapter
import ru.ksart.pomodoro.presentation.ui.model.TimerViewModel
import ru.ksart.pomodoro.utils.DebugHelper
import kotlin.system.exitProcess

class MainFragment : Fragment(), LifecycleObserver {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = requireNotNull(_binding)
    private var backPressedHold = 0L

    private val viewModel by viewModels<TimerViewModel>()

    private val timerAdapter
        get() = checkNotNull(binding.recycler.adapter as TimerAdapter) {
            "TimerAdapter isn't initialized"
        }

    // кнопки и смещения относительно друг друга
    private var isFabExpanded = false
    private var offsetFab1 = 0f
    private var offsetFab2 = 0f
    private var offsetFab3 = 0f
    private var offsetFab4 = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DebugHelper.log("MainFragment|onCreate")
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFab()
        initAdapter()
        bindViewModel()
        bindAddTimerFromDialog()
        initBack()
//        test()
    }

/*
    private fun test() {
        lifecycleScope.launchWhenStarted {
            delay(1000)
            var i = 1000
            while (i >= 0) {
                i--
                viewModel.addTimer(60)
                delay(1)
            }
        }
    }
*/

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }

    private fun initBack() {
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            if (backPressedHold + 3000 > System.currentTimeMillis()) {
                // выход из приложения
                activity?.finish()
                exitProcess(0)
            } else {
                toast(R.string.press_back_exit)
                backPressedHold = System.currentTimeMillis()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        DebugHelper.log("MainFragment|onAppBackgrounded")
        viewModel.startTimerBackground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        DebugHelper.log("MainFragment|onAppForegrounded")
        viewModel.stopTimerBackground()
    }

    private fun initAdapter() {
        binding.recycler.run {
            adapter = TimerAdapter(viewModel::onTimerListener)
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
        }
    }

    // настроим обсерверы на Flow
    private fun bindViewModel() {
        // корутина всегда запущена, пока приложение в состоянии Started
        lifecycleScope.launchWhenStarted {
            viewModel.listTimers.collect {
                DebugHelper.log("MainFragment|bindViewModel set list")
                timerAdapter.submitList(it)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.isUseFab.collect(::rollupFab)
        }
        lifecycleScope.launchWhenCreated {
            viewModel.timerBackgrounded.collect(::startStopTimerService)
        }
        lifecycleScope.launchWhenCreated {
            viewModel.isFinished.collect {
                if (it) {
                    context?.run {
                        RingtoneManager
                            .getRingtone(
                                this,
                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            )
                            .play()
                    }
                }
            }
        }
    }

    // запустим или остановим сервис с таймером
    private fun startStopTimerService(time: Pair<Long, Long>) {
        DebugHelper.log("MainFragment|timerBackgrounded startTime=${time.first} timer=${time.second}")
        if (time.second == 0L) return
        val intent = Intent(requireContext(), TimerForegroundService::class.java)
        if (time.second > 0) {
            intent.putExtra(
                TimerForegroundService.COMMAND_ID,
                TimerForegroundService.COMMAND_START
            )
            intent.putExtra(TimerForegroundService.STARTED_TIMER_TIME_MS, time.first)
            intent.putExtra(TimerForegroundService.TIMER_TIME_MS, time.second)
        } else {
            intent.putExtra(
                TimerForegroundService.COMMAND_ID,
                TimerForegroundService.COMMAND_STOP
            )
        }
        requireContext().startService(intent)
    }

    private fun bindAddTimerFromDialog() {
        // навигация с получением результата
        val navController = findNavController()
        // тут нужен текущий фрагмент, его стек мы обрабатываем
        val navBackStackEntry = navController.getBackStackEntry(R.id.mainFragment)
        // очистим состояние при пересоздании
        navBackStackEntry.savedStateHandle.remove<Long>(KEY_ADD_TIMER)

        navBackStackEntry.savedStateHandle.run {
            getLiveData<Long>(KEY_ADD_TIMER).observe(viewLifecycleOwner) { time ->
                // добавим таймер
                if (time > 0) viewModel.addTimer(time)
            }
        }
    }

    private fun initFab() {
        // необходимо узнать смещения для кнопок используем .post или viewTreeObserver
        binding.fabContainer.run {
            fab.post {
                offsetFab1 = fab.y - fabActionAdd.y
                fabActionAdd.translationY = offsetFab1

                offsetFab2 = fab.y - fabAction25.y
                fabAction25.translationY = offsetFab2

                offsetFab3 = fab.y - fabAction5.y
                fabAction5.translationY = offsetFab3

                offsetFab4 = fab.y - fabAction1.y
                fabAction1.translationY = offsetFab4
            }
            // обработка нажатия кнопки
            fab.setOnClickListener {
                useFab()
            }
            // Добавление значения
            fabActionAdd.setOnClickListener {
                findNavController().navigate(R.id.addTimerDialogFragment)
            }
            // Добавление 25 мин
            fabAction25.setOnClickListener {
                viewModel.addTimer(1500)
            }
            // Добавление 5 мин
            fabAction5.setOnClickListener {
                viewModel.addTimer(300)
            }
            // Добавление 1 мин
            fabAction1.setOnClickListener {
                viewModel.addTimer(60)
            }
        }
    }

    // анимация развертывания и свертывания
    private fun useFab() {
        isFabExpanded = !isFabExpanded
        if (isFabExpanded) viewModel.rollupFab()
        binding.fabContainer.run {
            fab.setImageResource(if (isFabExpanded) R.drawable.animated_plus else R.drawable.animated_minus)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                createFabAnimator(fabActionAdd, offsetFab1, isFabExpanded),
                createFabAnimator(fabAction25, offsetFab2, isFabExpanded),
                createFabAnimator(fabAction5, offsetFab3, isFabExpanded),
                createFabAnimator(fabAction1, offsetFab4, isFabExpanded),
            )
            animatorSet.start()
            // приведение к типу и выполнить
            (fab.drawable as? Animatable)?.start()
        }
    }

    // свернуть
    private suspend fun rollupFab(use: Boolean) {
        DebugHelper.log("MainFragment|bindViewModel isUseFab isFabExpanded=$isFabExpanded - $use")
        if (!isFabExpanded || !use) return
        binding.fabContainer.fab.run {
            isFocusableInTouchMode = true
            isFocusable = true
            requestFocus()
            performClick()
            delay(50)
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    // создание анимации для Fab
    private fun createFabAnimator(view: View, offset: Float, isExpanded: Boolean): Animator {
        return ObjectAnimator.ofFloat(
            view,
            TRANSLATION_Y,
            if (isExpanded) offset else 0f,
            if (isExpanded) 0f else offset
        )
            .setDuration(resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    companion object {
        const val KEY_ADD_TIMER = "key_add_timer"
    }
}