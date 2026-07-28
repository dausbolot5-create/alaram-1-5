package com.example.examping.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.examping.data.model.TriggerEntity

object AlarmScheduler {

    const val ACTION_EXAM_ALARM = "com.example.examping.ACTION_EXAM_ALARM"

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleTrigger(context: Context, trigger: TriggerEntity, examName: String, triggerTypeLabel: String) {
        if (trigger.sudahBunyi || trigger.waktu <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_EXAM_ALARM
            putExtra("EXTRA_TRIGGER_ID", trigger.id)
            putExtra("EXTRA_EXAM_ID", trigger.examId)
            putExtra("EXTRA_TITLE", "ExamPing AI — $triggerTypeLabel")
            putExtra("EXTRA_BODY", "$examName: $triggerTypeLabel")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            trigger.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        trigger.waktu,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, trigger.waktu, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    trigger.waktu,
                    pendingIntent
                )
            }
        } catch (_: Exception) {}
    }

    fun cancelTrigger(context: Context, triggerId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_EXAM_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
