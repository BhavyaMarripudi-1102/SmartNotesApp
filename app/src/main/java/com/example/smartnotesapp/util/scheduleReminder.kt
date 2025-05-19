package com.example.smartnotesapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.example.smartnotesapp.data.Note
import com.example.smartnotesapp.receiver.ReminderReceiver

fun scheduleReminder(context: Context, note: Note, reminderTimeMillis: Long) {
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("noteTitle", note.title)
        putExtra("noteContent", note.content)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        note.id.hashCode(), // ensure uniqueness
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
                Log.d("Reminder", "Alarm set on Android 12+")
            } else {
                Toast.makeText(context, "Enable exact alarm permission", Toast.LENGTH_LONG).show()
                val intentSettings = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intentSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intentSettings)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMillis,
                pendingIntent
            )
            Log.d("Reminder", "Alarm set on Android 6+")
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTimeMillis,
                pendingIntent
            )
            Log.d("Reminder", "Alarm set on older Android")
        }

    } catch (e: SecurityException) {
        Log.e("Reminder", "SecurityException while setting alarm: ${e.message}")
    }
}