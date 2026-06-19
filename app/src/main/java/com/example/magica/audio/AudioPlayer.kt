package com.example.magica.audio

import android.media.MediaPlayer
import android.media.PlaybackParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

class AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    fun prepare(filePath: String) {
        release()
        if (!File(filePath).exists()) return
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener {
                    isPrepared = true
                    _duration.value = it.duration.toLong()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                }
                prepareAsync()
            }
        } catch (_: Exception) {}
    }

    fun play() {
        if (!isPrepared) return
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.reset()
            }
            isPrepared = false
            _isPlaying.value = false
            _currentPosition.value = 0L
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
        _currentPosition.value = positionMs
    }

    fun setVolume(vol: Float) {
        mediaPlayer?.setVolume(vol, vol)
    }

    fun updatePosition() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                _currentPosition.value = it.currentPosition.toLong()
            }
        }
    }

    fun release() {
        mediaPlayer?.let {
            try {
                it.stop()
                it.release()
            } catch (_: Exception) {}
        }
        mediaPlayer = null
        isPrepared = false
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }
}
