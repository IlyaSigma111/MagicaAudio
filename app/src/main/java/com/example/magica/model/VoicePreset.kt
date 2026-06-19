package com.example.magica.model

data class VoicePreset(
    val name: String,
    val icon: String,
    val description: String,
    val pitchSemitones: Float = 0f,
    val speedRatio: Float = 1.0f,
    val eqBands: List<Float> = List(10) { 0f },
    val reverbMix: Float = 0f,
    val reverbRoomSize: Float = 0.5f,
    val reverbDamping: Float = 0.5f,
    val chorusMix: Float = 0f,
    val distortionGain: Float = 0f,
    val robotEffect: Boolean = false,
    val volume: Float = 1.0f,
    val lowPassCutoff: Float = 20000f,
    val highPassCutoff: Float = 20f,
) {
    fun applyTo(settings: AudioSettings) {
        settings.pitchSemitones = pitchSemitones
        settings.speedRatio = speedRatio
        for (i in eqBands.indices) {
            if (i < settings.eqBands.size) settings.eqBands[i] = eqBands[i]
        }
        settings.reverbMix = reverbMix
        settings.reverbRoomSize = reverbRoomSize
        settings.reverbDamping = reverbDamping
        settings.chorusMix = chorusMix
        settings.distortionGain = distortionGain
        settings.robotEffect = robotEffect
        settings.volume = volume
        settings.lowPassCutoff = lowPassCutoff
        settings.highPassCutoff = highPassCutoff
    }
}

object BuiltInPresets {
    val presets: List<VoicePreset> = listOf(
        VoicePreset(
            name = "Чипмунк",
            icon = "\uD83D\uDC39",
            description = "Высокий голос как у бурундука",
            pitchSemitones = 8f,
            speedRatio = 1.15f
        ),
        VoicePreset(
            name = "Глубокий бас",
            icon = "\uD83E\uDDD1\u200D\uD83E\uDDBC",
            description = "Низкий бархатистый бас",
            pitchSemitones = -6f,
            speedRatio = 0.9f
        ),
        VoicePreset(
            name = "Робот",
            icon = "\uD83E\uDD16",
            description = "Металлический роботизированный голос",
            pitchSemitones = 2f,
            eqBands = listOf(-6f, -4f, -2f, 0f, 2f, 4f, 6f, 6f, 4f, 2f),
            robotEffect = true,
            chorusMix = 0.3f
        ),
        VoicePreset(
            name = "Пришелец",
            icon = "\uD83D\uDC7D",
            description = "Космический модулирующий голос",
            pitchSemitones = 5f,
            chorusMix = 0.5f,
            reverbMix = 0.4f
        ),
        VoicePreset(
            name = "Эхо-зал",
            icon = "\uD83C\uDFB6",
            description = "Голос с эффектом концертного зала",
            reverbMix = 0.7f,
            reverbRoomSize = 0.9f,
            reverbDamping = 0.3f
        ),
        VoicePreset(
            name = "Монстр",
            icon = "\uD83D\uDC79",
            description = "Глубокий низкий монструозный голос",
            pitchSemitones = -10f,
            speedRatio = 0.8f,
            distortionGain = 0.5f,
            eqBands = listOf(8f, 6f, 4f, 2f, 0f, -2f, -4f, -6f, -8f, -10f)
        ),
        VoicePreset(
            name = "Ребёнок",
            icon = "\uD83D\uDC76",
            description = "Тонкий детский голосок",
            pitchSemitones = 10f,
            speedRatio = 1.1f,
            eqBands = listOf(-4f, -2f, 0f, 2f, 3f, 4f, 5f, 5f, 4f, 3f)
        ),
        VoicePreset(
            name = "Старик",
            icon = "\uD83D\uDC74",
            description = "Дрожащий старческий голос",
            pitchSemitones = -3f,
            speedRatio = 0.85f,
            reverbMix = 0.2f,
            eqBands = listOf(3f, 2f, 1f, 0f, -1f, -2f, -3f, -4f, -5f, -6f),
            highPassCutoff = 80f
        ),
        VoicePreset(
            name = "Радио FM",
            icon = "\uD83D\uDCFB",
            description = "Голос как из старого радиоприёмника",
            eqBands = listOf(-8f, -6f, -4f, -2f, 0f, 2f, 3f, 2f, -2f, -8f),
            lowPassCutoff = 5000f,
            highPassCutoff = 300f
        ),
        VoicePreset(
            name = "Шёпот",
            icon = "\uD83E\uDD2B",
            description = "Приглушённый шепчущий голос",
            pitchSemitones = 2f,
            eqBands = listOf(-10f, -8f, -6f, -4f, -2f, 0f, 2f, 4f, 6f, 8f),
            reverbMix = 0.1f,
            volume = 0.6f,
            highPassCutoff = 500f
        ),
        VoicePreset(
            name = "Автотюн",
            icon = "\uD83C\uDFA4",
            description = "Эффект автонастройки как у T-Pain",
            pitchSemitones = 0f,
            chorusMix = 0.6f,
            reverbMix = 0.3f,
            eqBands = listOf(-2f, -1f, 0f, 1f, 2f, 3f, 4f, 4f, 3f, 2f)
        ),
        VoicePreset(
            name = "Демон",
            icon = "\uD83D\uDC7F",
            description = "Низкий искажённый демонический голос",
            pitchSemitones = -8f,
            speedRatio = 0.75f,
            distortionGain = 0.7f,
            reverbMix = 0.5f,
            eqBands = listOf(6f, 5f, 4f, 2f, 0f, -2f, -4f, -6f, -8f, -10f)
        ),
        VoicePreset(
            name = "Эльф",
            icon = "\uD83E\uDDDD\u200D\u2640\uFE0F",
            description = "Воздушный высокий голос",
            pitchSemitones = 6f,
            speedRatio = 1.05f,
            reverbMix = 0.3f,
            reverbRoomSize = 0.7f,
            eqBands = listOf(-6f, -4f, -2f, 0f, 2f, 4f, 6f, 8f, 8f, 6f)
        ),
        VoicePreset(
            name = "Телефон",
            icon = "\uD83D\uDCDE",
            description = "Голос как через телефонную линию",
            eqBands = listOf(-12f, -10f, -8f, -4f, 0f, 3f, 4f, 3f, -4f, -12f),
            lowPassCutoff = 4000f,
            highPassCutoff = 300f
        ),
        VoicePreset(
            name = "Под водой",
            icon = "\uD83C\uDF0A",
            description = "Голос как будто под водой",
            pitchSemitones = -4f,
            reverbMix = 0.6f,
            reverbRoomSize = 0.3f,
            reverbDamping = 0.8f,
            eqBands = listOf(6f, 4f, 2f, 0f, -2f, -4f, -6f, -8f, -10f, -12f),
            lowPassCutoff = 4000f
        ),
    )
}
