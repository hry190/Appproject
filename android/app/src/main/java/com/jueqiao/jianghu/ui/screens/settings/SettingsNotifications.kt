package com.jueqiao.jianghu.ui.screens.settings

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jueqiao.jianghu.MainActivity
import com.jueqiao.jianghu.R
import java.util.Calendar

object SettingsNotifications {
    private const val LEARNING_CHANNEL_ID = "learning_reminders"
    private const val LEARNING_NOTIFICATION_ID = 1101
    private const val LEARNING_REQUEST_CODE = 2101

    fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LEARNING_CHANNEL_ID,
                "学习提醒",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "每日任务、连续学习与休息提示"
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        apply(context, SettingsPreferences(context).readSnapshot())
    }

    fun apply(context: Context, snapshot: SettingsSnapshot) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = learningPendingIntent(context)
        alarmManager.cancel(pendingIntent)
        if (!snapshot.messageEnabled || !snapshot.learningReminder) return

        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent,
        )
    }

    fun notificationsAllowed(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    fun showLearningReminder(context: Context) {
        val snapshot = SettingsPreferences(context).readSnapshot()
        if (!snapshot.messageEnabled ||
            !snapshot.learningReminder ||
            !notificationsAllowed(context) ||
            isQuietHour(snapshot)
        ) {
            return
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, LEARNING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("今日修炼尚未完成")
            .setContentText("回到机巧江湖完成一个小任务，保持探索节奏。")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context)
                .notify(LEARNING_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify call.
        }
    }

    private fun learningPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            LEARNING_REQUEST_CODE,
            Intent(context, LearningReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun isQuietHour(snapshot: SettingsSnapshot): Boolean {
        if (!snapshot.quietHours) return false
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 22 || hour < 8
    }
}

class LearningReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        SettingsNotifications.showLearningReminder(context)
    }
}

class SettingsBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            SettingsNotifications.initialize(context)
        }
    }
}
