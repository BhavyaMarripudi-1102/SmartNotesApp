package com.example.smartnotesapp.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.smartnotesapp.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra("noteTitle") ?: "Reminder"
        val noteContent = intent.getStringExtra("noteContent") ?: ""
        val noteId = intent.getStringExtra("NoteId") ?: ""

        val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("noteTitle", noteTitle)
            putExtra("noteContent", noteContent)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            noteId.hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "note_reminder_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(noteTitle)
            .setContentText(noteContent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}