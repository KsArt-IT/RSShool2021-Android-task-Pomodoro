package ru.ksart.pomodoro.model.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.presentation.extensions.displayTime
import ru.ksart.pomodoro.presentation.main.MainActivity
import ru.ksart.pomodoro.utils.DebugHelper
import ru.ksart.pomodoro.utils.isAndroid8

class TimerForegroundService : Service() {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, NotificationChannels.TIMER_CHANNEL_ID)
            .setContentTitle("Pomodoro Timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_alarm)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        DebugHelper.log("Service|onCreate")
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugHelper.log("Service|onStartCommand")
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    private fun processCommand(intent: Intent?) {
        intent?.extras?.getString(COMMAND_ID)?.let {
            DebugHelper.log("Service|processCommand Command=$it")
            when (it) {
                COMMAND_START -> {
                    val startTime = intent.extras?.getLong(STARTED_TIMER_TIME_MS)
                        ?: SystemClock.elapsedRealtime()
                    val timerTime = intent.extras?.getLong(TIMER_TIME_MS) ?: return
                    commandStart(startTime, timerTime)
                }
                COMMAND_STOP -> commandStop()
                else -> return
            }
        }
    }

    private fun commandStart(startTime: Long, timerTime: Long) {
        DebugHelper.log("Service|commandStart")
        if (isServiceStarted) return
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer(startTime, timerTime)
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(startTime: Long, timerTime: Long) {
        coroutineScope.launch {
            DebugHelper.log("Service|continueTimer startTime=$startTime timerTime=$timerTime")
            var timeCurrent = startTime
            var timeStart = timeCurrent
            var time = timerTime
            while (time > 250) {
                DebugHelper.log("Service|continueTimer time=$time")
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        time.displayTime()
                    )
                )
                delay(INTERVAL)
                timeCurrent = SystemClock.elapsedRealtime()
                time -= (timeCurrent - timeStart)
                timeStart = timeCurrent
            }
            // пошлем завершающий 0
            notificationManager?.notify(
                NOTIFICATION_ID,
                getNotification(0L.displayTime())
            )
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) return
        DebugHelper.log("Service|commandStop")
        try {
            coroutineScope.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        DebugHelper.log("Service|getPendingIntent")
        val resultIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(this, 0, resultIntent, FLAG_UPDATE_CURRENT)
    }

    private fun moveToStartedState() {
        if (isAndroid8) {
            DebugHelper.log("Service|moveToStartedState Running on  Android 8 or higher")
            startForegroundService(Intent(this, TimerForegroundService::class.java))
        } else {
            DebugHelper.log("Service|moveToStartedState Running on Android N or lower")
            startService(Intent(this, TimerForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        DebugHelper.log("Service|startForegroundAndShowNotification")
        val notification = getNotification("Start timer")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String): Notification {
        return builder.setContentText(content)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 145145
        private const val INTERVAL = 1000L

        const val INVALID = "INVALID"
        const val COMMAND_START = "COMMAND_START"
        const val COMMAND_STOP = "COMMAND_STOP"
        const val COMMAND_ID = "COMMAND_ID"
        const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
        const val TIMER_TIME_MS = "TIMER_TIME"
    }
}