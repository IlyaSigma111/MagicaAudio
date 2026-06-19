package com.example.magica.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.magica.audio.AudioPlayer
import com.example.magica.audio.AudioProcessor
import com.example.magica.audio.AudioUtils
import com.example.magica.model.TrackData
import com.example.magica.model.VoicePreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val tracks = mutableStateListOf<TrackData>()
    var selectedTrackId by mutableLongStateOf(-1L)
    var isPlaying by mutableStateOf(false)
    var playbackPositionMs by mutableLongStateOf(0L)
    var isProcessing by mutableStateOf(false)
    var progressPercent by mutableStateOf(0)
    var errorMessage by mutableStateOf<String?>(null)
    var previewPath by mutableStateOf<String?>(null)
    var exportPath by mutableStateOf<String?>(null)
    var showPresets by mutableStateOf(false)
    var showExportDialog by mutableStateOf(false)
    var currentSourcePath by mutableStateOf<String?>(null)

    private val player = AudioPlayer()
    private var positionUpdateJob: Job? = null

    val selectedTrack: TrackData?
        get() = tracks.find { it.id == selectedTrackId }

    val totalDurationMs: Long
        get() = tracks.maxOfOrNull { it.startTimeMs + it.durationMs } ?: 0L

    val activeTracks: List<TrackData>
        get() {
            val hasSolo = tracks.any { it.solo }
            return tracks.filter { t ->
                if (hasSolo) t.solo
                else !t.muted
            }
        }

    fun loadAudio(uri: Uri) {
        val app = getApplication<Application>()
        val fileName = AudioUtils.getFileName(app, uri)
        val filePath = AudioUtils.copyUriToInternal(app, uri, fileName)
        val duration = AudioProcessor.getFileDurationMs(filePath)

        val track = TrackData(
            id = ProjectState.generateId(),
            uri = uri,
            fileName = fileName,
            filePath = filePath,
            startTimeMs = totalDurationMs.coerceAtLeast(0L),
            durationMs = duration,
            colorIndex = tracks.size % 8,
        )
        tracks.add(track)
        selectedTrackId = track.id
    }

    fun selectTrack(id: Long) {
        selectedTrackId = id
    }

    fun removeTrack(id: Long) {
        val idx = tracks.indexOfFirst { it.id == id }
        tracks.removeAll { it.id == id }
        if (selectedTrackId == id) {
            selectedTrackId = tracks.lastOrNull()?.id ?: -1L
        }
    }

    fun updateTrackSettings(id: Long, update: (AudioSettings) -> Unit) {
        val track = tracks.find { it.id == id } ?: return
        update(track.settings)
        tracks[tracks.indexOf(track)] = track
    }

    fun toggleMute(id: Long) {
        tracks.find { it.id == id }?.let { t ->
            t.muted = !t.muted
            val idx = tracks.indexOf(t)
            tracks[idx] = t
        }
    }

    fun toggleSolo(id: Long) {
        tracks.find { it.id == id }?.let { t ->
            t.solo = !t.solo
            val idx = tracks.indexOf(t)
            tracks[idx] = t
        }
    }

    fun applyPreset(preset: VoicePreset) {
        val track = selectedTrack ?: return
        preset.applyTo(track.settings)
        val idx = tracks.indexOf(track)
        tracks[idx] = track
        showPresets = false
    }

    fun playPause() {
        val path = previewPath ?: return
        if (isPlaying) {
            player.pause()
            isPlaying = false
            positionUpdateJob?.cancel()
        } else {
            if (player.currentPosition.value > 0L && player.currentPosition.value < player.duration.value) {
                player.play()
                isPlaying = true
                startPositionUpdates()
            } else {
                player.prepare(path)
                player.play()
                isPlaying = true
                startPositionUpdates()
            }
        }
    }

    fun stopPlayback() {
        player.stop()
        isPlaying = false
        playbackPositionMs = 0L
        positionUpdateJob?.cancel()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        playbackPositionMs = positionMs
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = viewModelScope.launch {
            while (true) {
                player.updatePosition()
                playbackPositionMs = player.currentPosition.value
                delay(50)
            }
        }
    }

    fun generatePreview() {
        val app = getApplication<Application>()
        val track = selectedTrack ?: return
        isProcessing = true
        errorMessage = null
        viewModelScope.launch {
            val result = AudioProcessor.processTrackSegment(app, track, 5)
            if (result != null) {
                previewPath = result
                player.prepare(result)
            } else {
                errorMessage = "Ошибка генерации превью"
            }
            isProcessing = false
        }
    }

    fun exportAudio(format: String = "mp3") {
        val app = getApplication<Application>()
        isProcessing = true
        errorMessage = null
        showExportDialog = false
        viewModelScope.launch {
            val result = AudioProcessor.exportMix(
                context = app,
                tracks = activeTracks,
                format = format,
                onProgress = { p -> progressPercent = p }
            )
            if (result != null) {
                exportPath = result
            } else {
                errorMessage = "Ошибка экспорта"
            }
            isProcessing = false
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetAllSettings() {
        tracks.forEach { it.settings.reset() }
        tracks.toList().let { list ->
            tracks.clear()
            tracks.addAll(list)
        }
    }

    fun resetTrackSettings(id: Long) {
        tracks.find { it.id == id }?.settings?.reset()
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        positionUpdateJob?.cancel()
    }

    private object ProjectState {
        private var nextId: Long = 1L
        fun generateId(): Long = nextId++
    }
}
