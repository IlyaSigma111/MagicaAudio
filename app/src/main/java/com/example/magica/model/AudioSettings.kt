package com.example.magica.model

data class AudioSettings(
    var pitchSemitones: Float = 0f,
    var speedRatio: Float = 1.0f,
    var volume: Float = 1.0f,
    var eqBands: MutableList<Float> = MutableList(10) { 0f },
    var reverbMix: Float = 0f,
    var reverbRoomSize: Float = 0.5f,
    var reverbDamping: Float = 0.5f,
    var chorusMix: Float = 0f,
    var distortionGain: Float = 0f,
    var robotEffect: Boolean = false,
    var lowPassCutoff: Float = 20000f,
    var highPassCutoff: Float = 20f,
) {
    val eqFrequencies: List<Float>
        get() = listOf(31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)

    val eqLabels: List<String>
        get() = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    fun copy(): AudioSettings = AudioSettings(
        pitchSemitones = pitchSemitones,
        speedRatio = speedRatio,
        volume = volume,
        eqBands = eqBands.toMutableList(),
        reverbMix = reverbMix,
        reverbRoomSize = reverbRoomSize,
        reverbDamping = reverbDamping,
        chorusMix = chorusMix,
        distortionGain = distortionGain,
        robotEffect = robotEffect,
        lowPassCutoff = lowPassCutoff,
        highPassCutoff = highPassCutoff,
    )

    fun reset() {
        pitchSemitones = 0f
        speedRatio = 1.0f
        volume = 1.0f
        for (i in eqBands.indices) eqBands[i] = 0f
        reverbMix = 0f
        reverbRoomSize = 0.5f
        reverbDamping = 0.5f
        chorusMix = 0f
        distortionGain = 0f
        robotEffect = false
        lowPassCutoff = 20000f
        highPassCutoff = 20f
    }
}
