package com.example.smartnotesapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.smartnotesapp.ui.theme.NoteScreen
import com.example.smartnotesapp.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.smartnotesapp.ui.theme.SmartNotesAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Toast.makeText(this, "Notification permission not granted", Toast.LENGTH_SHORT).show()
                }
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            SmartNotesAppTheme(darkTheme = isDarkTheme) {
                NoteScreen(themeViewModel = themeViewModel)
            }
        }
    }
}