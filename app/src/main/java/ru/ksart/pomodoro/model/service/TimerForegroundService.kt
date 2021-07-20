package ru.ksart.pomodoro.model.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.presentation.extensions.displayTime
import ru.ksart.pomodoro.presentation.main.MainActivity
import ru.ksart.pomodoro.utils.DebugHelper

class TimerForegroundService: Service() {
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

    override fun onBind(intent: Intent?): IBinder?  = null

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
                    val startTime = intent.extras?.getLong(STARTED_TIMER_TIME_MS) ?: return
                    commandStart(startTime)
                }
                COMMAND_STOP -> commandStop()
                else -> return
            }
        }
    }

    private fun commandStart(startTime: Long) {
        DebugHelper.log("Service|commandStart")
        if (isServiceStarted) return
        try {
            startForegroundAndShowNotification()
            continueTimer(startTime)
        } finally {
            isServiceStarted = true
        }
    }

    private fun continueTimer(startTime: Long) {
        coroutineScope.launch {
            DebugHelper.log("Service|continueTimer startTime=$startTime")
            var i = startTime
            while (i >= 0) {
                DebugHelper.log("Service|continueTimer i=$i")
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        i.displayTime()
                    )
                )
                delay(INTERVAL)
                i -= INTERVAL
            }
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
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT)
    }

    private fun startForegroundAndShowNotification() {
        DebugHelper.log("Service|startForegroundAndShowNotification")
        val notification = getNotification("Start timer")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()

    companion object {
        private const val NOTIFICATION_ID = 145145
        private const val INTERVAL = 1000L

        const val INVALID = "INVALID"
        const val COMMAND_START = "COMMAND_START"
        const val COMMAND_STOP = "COMMAND_STOP"
        const val COMMAND_ID = "COMMAND_ID"
        const val STARTED_TIMER_TIME_MS = "STARTED_TIMER_TIME"
    }
}