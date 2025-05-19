@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.smartnotesapp.ui.theme

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartnotesapp.util.scheduleReminder
import com.example.smartnotesapp.viewmodel.NotesViewModel
import com.example.smartnotesapp.data.Note
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartnotesapp.viewmodel.ThemeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.example.smartnotesapp.util.exportNotesToTxt
import com.example.smartnotesapp.util.exportNotesToPdf

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = viewModel()
) {
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()
    val isDark by themeViewModel.isDarkTheme.collectAsState()
    val scrollState = rememberLazyListState()

    // Filter dialog state and criteria
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyReminders by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var datePickerSelectedMillis by remember { mutableStateOf<Long?>(null) }

    // Note creation state
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var editedNote by remember { mutableStateOf<Note?>(null) }
    var editedTitle by remember { mutableStateOf("") }
    var editedContent by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Dark mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDark,
                onCheckedChange = { themeViewModel.toggleTheme() }
            )
        }

        // Filter icon top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showFilterDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "Filter",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Note creation UI
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Button(onClick = {
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        reminderTime = calendar.timeInMillis

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            val alarmManager =
                                context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                            if (!alarmManager.canScheduleExactAlarms()) {
                                val intent =
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                context.startActivity(intent)
                            }
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }) {
                Text("Set Reminder")
            }

            Button(onClick = {
                val newNote = Note(
                    title = title,
                    content = content,
                    reminderTime = reminderTime
                )
                viewModel.addNote(newNote)
                reminderTime?.let {
                    scheduleReminder(context, newNote, it)
                    Toast.makeText(context, "Reminder set!", Toast.LENGTH_SHORT).show()
                }
                title = ""
                content = ""
                reminderTime = null
            }) {
                Text("Add Note")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apply all filters here:
        val filteredNotes = notes.filter { note ->
            val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true)
            val matchesReminder = !showOnlyReminders || note.reminderTime != null
            val matchesDate = if (selectedDate != null && note.reminderTime != null) {
                val noteLocalDate = Instant.ofEpochMilli(note.reminderTime!!)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                noteLocalDate.isEqual(selectedDate)
            } else if (selectedDate != null) {
                false
            } else true
            matchesSearch && matchesReminder && matchesDate
        }

        LazyColumn(state = scrollState, modifier = Modifier.weight(1f)) {
            items(filteredNotes) { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = note.title, fontWeight = FontWeight.Bold)
                        Text(text = note.content)
                        note.reminderTime?.let {
                            val formattedTime = SimpleDateFormat(
                                "dd MMM yyyy, hh:mm a",
                                Locale.getDefault()
                            ).format(Date(it))
                            Text(
                                text = "Reminder: $formattedTime",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    IconButton(onClick = {
                        editedNote = note
                        editedTitle = note.title
                        editedContent = note.content
                        title = note.title
                        content = note.content
                    }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Note")
                    }

                    IconButton(onClick = {
                        viewModel.deleteNote(note)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note"
                        )
                    }
                }
                Divider()
            }
        }

        // Filter/Export Dialog
        if (showFilterDialog) {
            FilterDialog(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showOnlyReminders = showOnlyReminders,
                onShowOnlyRemindersChange = { showOnlyReminders = it },
                selectedDate = selectedDate,
                onDateSelected = { date, millis ->
                    selectedDate = date
                    datePickerSelectedMillis = millis
                },
                datePickerSelectedMillis = datePickerSelectedMillis,
                onDatePickerMillisChange = { datePickerSelectedMillis = it },
                onExportTxt = { exportNotesToTxt(context, filteredNotes) },
                onExportPdf = { exportNotesToPdf(context, filteredNotes) },
                onDismiss = { showFilterDialog = false },
                filteredNotes = filteredNotes // <--- live preview
            )
        }
    }
}

// --- FilterDialog Composable ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showOnlyReminders: Boolean,
    onShowOnlyRemindersChange: (Boolean) -> Unit,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?, Long?) -> Unit,
    datePickerSelectedMillis: Long?,
    onDatePickerMillisChange: (Long?) -> Unit,
    onExportTxt: () -> Unit,
    onExportPdf: () -> Unit,
    onDismiss: () -> Unit,
    filteredNotes: List<Note>
) {
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Export") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showOnlyReminders,
                        onCheckedChange = onShowOnlyRemindersChange
                    )
                    Text("Show only notes with reminders")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Date Picker Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Filter by Date: ")
                    Button(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDate != null) Color(0xFFD1B3FF) else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(selectedDate?.toString() ?: "Choose Date")
                    }
                    if (selectedDate != null) {
                        IconButton(onClick = {
                            onDateSelected(null, null)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear date filter")
                        }
                    }
                }
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = datePickerSelectedMillis
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDatePicker = false
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val correctedMillis = millis + 12 * 60 * 60 * 1000
                                        onDatePickerMillisChange(millis)
                                        val date = Instant.ofEpochMilli(correctedMillis)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                        onDateSelected(date, millis)
                                    }
                                }
                            ) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Export Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onExportTxt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text("Export Filtered Notes to TXT")
                    }
                    Button(
                        onClick = onExportPdf,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Filtered Notes to PDF")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()

                // --- Notes Preview List ---
                Text(
                    "Filtered Notes (${filteredNotes.size}):",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                if (filteredNotes.isEmpty()) {
                    Text(
                        text = "No notes match the selected filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    // Show a small scrollable preview list
                    val previewNotes = if (filteredNotes.size > 5) filteredNotes.take(5) else filteredNotes
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        previewNotes.forEach { note ->
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(note.title, fontWeight = FontWeight.Bold)
                                if (note.content.isNotBlank())
                                    Text(note.content, maxLines = 1)
                                note.reminderTime?.let {
                                    val formattedTime = SimpleDateFormat(
                                        "dd MMM yyyy, hh:mm a",
                                        Locale.getDefault()
                                    ).format(Date(it))
                                    Text("Reminder: $formattedTime", style = MaterialTheme.typography.labelSmall)
                                }
                                Divider(thickness = 0.5.dp, color = Color.LightGray)
                            }
                        }
                        if (filteredNotes.size > 5) {
                            Text(
                                "+${filteredNotes.size - 5} more...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}