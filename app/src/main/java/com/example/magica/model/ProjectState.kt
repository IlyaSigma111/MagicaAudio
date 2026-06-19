package com.example.magica.model

data class ProjectState(
    val tracks: MutableList<TrackData> = mutableListOf(),
    val masterVolume: Float = 1.0f,
    val projectLengthMs: Long = 0L,
    val selectedTrackId: Long? = null,
    var isPlaying: Boolean = false,
    var playbackPositionMs: Long = 0L,
    var isProcessing: Boolean = false,
    var progressPercent: Int = 0,
) {
    companion object {
        private var nextId: Long = 1L
        fun generateId(): Long = nextId++
    }
}
