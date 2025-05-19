package com.example.smartnotesapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.smartnotesapp.data.Note
import com.example.smartnotesapp.util.scheduleReminder

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra("noteId", -1)
        val noteTitle = intent.getStringExtra("noteTitle") ?: ""
        val noteContent = intent.getStringExtra("noteContent") ?: ""

        // 10 minutes later
        val snoozeTime = System.currentTimeMillis() + 2 * 60 * 1000L
        val snoozedNote = Note(
            id = noteId,
            title = noteTitle,
            content = noteContent,
            reminderTime = snoozeTime
        )
        scheduleReminder(context, snoozedNote, snoozeTime)
        Toast.makeText(context, "Reminder snoozed for 2 minutes!", Toast.LENGTH_SHORT).show()
    }
}