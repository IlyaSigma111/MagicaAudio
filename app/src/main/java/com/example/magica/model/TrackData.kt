package com.example.magica.model

import android.net.Uri

data class TrackData(
    val id: Long,
    val uri: Uri,
    val fileName: String,
    val filePath: String,
    var startTimeMs: Long = 0L,
    var durationMs: Long = 0L,
    var volume: Float = 1.0f,
    var muted: Boolean = false,
    var solo: Boolean = false,
    var colorIndex: Int = 0,
    val settings: AudioSettings = AudioSettings(),
) {
    val displayName: String
        get() = fileName.removeSuffix(".wav").removeSuffix(".mp3")
            .removeSuffix(".ogg").removeSuffix(".m4a")
            .removeSuffix(".aac").removeSuffix(".flac")
            .take(20)
}
