package com.example.magica

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.magica.model.BuiltInPresets
import com.example.magica.ui.screens.EditorScreen
import com.example.magica.ui.screens.MainScreen
import com.example.magica.ui.screens.PresetsScreen
import com.example.magica.ui.theme.MagicaTheme
import com.example.magica.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicaTheme {
                MagicaApp()
            }
        }
    }
}

enum class Screen {
    MAIN, EDITOR, PRESETS
}

@Composable
fun MagicaApp() {
    val viewModel: MainViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }

    when (currentScreen) {
        Screen.MAIN -> {
            MainScreen(
                tracks = viewModel.tracks,
                isProcessing = viewModel.isProcessing,
                onLoadAudio = { uri ->
                    viewModel.loadAudio(uri)
                    currentScreen = Screen.EDITOR
                },
                onOpenEditor = { currentScreen = Screen.EDITOR },
                onOpenPresets = { currentScreen = Screen.PRESETS }
            )
        }

        Screen.EDITOR -> {
            EditorScreen(
                tracks = viewModel.tracks,
                selectedTrackId = viewModel.selectedTrackId,
                totalDurationMs = viewModel.totalDurationMs,
                playbackPositionMs = viewModel.playbackPositionMs,
                isPlaying = viewModel.isPlaying,
                isProcessing = viewModel.isProcessing,
                progressPercent = viewModel.progressPercent,
                errorMessage = viewModel.errorMessage,
                previewPath = viewModel.previewPath,
                onSelectTrack = viewModel::selectTrack,
                onRemoveTrack = viewModel::removeTrack,
                onToggleMute = viewModel::toggleMute,
                onToggleSolo = viewModel::toggleSolo,
                onUpdateSettings = viewModel::updateTrackSettings,
                onPlayPause = viewModel::playPause,
                onStop = viewModel::stopPlayback,
                onSeek = viewModel::seekTo,
                onGeneratePreview = viewModel::generatePreview,
                onExport = viewModel::exportAudio,
                onResetAll = viewModel::resetAllSettings,
                onResetTrack = viewModel::resetTrackSettings,
                onOpenPresets = { currentScreen = Screen.PRESETS },
                onBack = { currentScreen = Screen.MAIN },
                onClearError = viewModel::clearError,
            )
        }

        Screen.PRESETS -> {
            PresetsScreen(
                presets = BuiltInPresets.presets,
                onApplyPreset = viewModel::applyPreset,
                onBack = {
                    currentScreen = if (viewModel.tracks.isNotEmpty()) Screen.EDITOR else Screen.MAIN
                }
            )
        }
    }
}
