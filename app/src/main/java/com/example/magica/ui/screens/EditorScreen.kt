package com.example.magica.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.ResetTv
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magica.model.TrackData
import com.example.magica.ui.components.EffectSlider
import com.example.magica.ui.components.EqualizerView
import com.example.magica.ui.components.PlaybackControlBar
import com.example.magica.ui.components.TimelineView
import com.example.magica.ui.components.WaveformView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    tracks: List<TrackData>,
    selectedTrackId: Long,
    totalDurationMs: Long,
    playbackPositionMs: Long,
    isPlaying: Boolean,
    isProcessing: Boolean,
    progressPercent: Int,
    errorMessage: String?,
    previewPath: String?,
    onSelectTrack: (Long) -> Unit,
    onRemoveTrack: (Long) -> Unit,
    onToggleMute: (Long) -> Unit,
    onToggleSolo: (Long) -> Unit,
    onUpdateSettings: (Long, (com.example.magica.model.AudioSettings) -> Unit) -> Unit,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    onGeneratePreview: () -> Unit,
    onExport: (String) -> Unit,
    onResetAll: () -> Unit,
    onResetTrack: (Long) -> Unit,
    onOpenPresets: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
) {
    val selectedTrack = tracks.find { it.id == selectedTrackId }
    val scrollState = rememberScrollState()
    var showExportSheet by remember { mutableStateOf(false) }
    var showEq by remember { mutableStateOf(false) }
    var showEffects by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "Редактор",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenPresets) {
                    Icon(Icons.Default.Mic, "Presets", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showExportSheet = true }) {
                    Icon(Icons.Default.FileDownload, "Export", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(scrollState)
            ) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C))
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }

                TimelineView(
                    tracks = tracks,
                    selectedTrackId = selectedTrackId,
                    totalDurationMs = totalDurationMs,
                    currentPositionMs = playbackPositionMs,
                    isPlaying = isPlaying,
                    onSelectTrack = onSelectTrack,
                    onMoveTrack = { _, _ -> },
                    onToggleMute = onToggleMute,
                    onToggleSolo = onToggleSolo,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedTrack != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedTrack.displayName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    IconButton(onClick = { onResetTrack(selectedTrack.id) }) {
                                        Icon(Icons.Default.ResetTv, "Reset", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = { onRemoveTrack(selectedTrack.id) }) {
                                        Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            WaveformView(
                                waveform = null,
                                accentColor = MaterialTheme.colorScheme.primary,
                                progress = if (totalDurationMs > 0) (playbackPositionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            PlaybackControlBar(
                                isPlaying = isPlaying,
                                positionMs = playbackPositionMs,
                                durationMs = previewPath?.let { 5000L } ?: 0L,
                                onPlayPause = onPlayPause,
                                onStop = onStop,
                                onSeek = onSeek
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = onGeneratePreview,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Генерация... $progressPercent%")
                                } else {
                                    Icon(Icons.Default.Preview, null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Предпросмотр (5 сек)")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tune, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Основные настройки",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            EffectSlider(
                                label = "Тон (полутона)",
                                value = (selectedTrack.settings.pitchSemitones + 12f) / 24f,
                                onValueChange = { v ->
                                    val semitones = v * 24f - 12f
                                    onUpdateSettings(selectedTrack.id) { it.pitchSemitones = semitones }
                                },
                                min = 0f, max = 1f
                            )
                            Text(
                                text = "${selectedTrack.settings.pitchSemitones.toInt()} полутонов",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            EffectSlider(
                                label = "Скорость",
                                value = (selectedTrack.settings.speedRatio - 0.5f) / 1.5f,
                                onValueChange = { v ->
                                    val speed = v * 1.5f + 0.5f
                                    onUpdateSettings(selectedTrack.id) { it.speedRatio = speed }
                                },
                                min = 0f, max = 1f
                            )
                            Text(
                                text = "%.2fx".format(selectedTrack.settings.speedRatio),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            EffectSlider(
                                label = "Громкость",
                                value = selectedTrack.settings.volume,
                                onValueChange = { v ->
                                    onUpdateSettings(selectedTrack.id) { it.volume = v }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showEq = !showEq },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Equalizer, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Эквалайзер", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    if (showEq) "▲" else "▼",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }

                            AnimatedVisibility(visible = showEq) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EqualizerView(
                                        bands = selectedTrack.settings.eqBands,
                                        labels = selectedTrack.settings.eqLabels,
                                        frequencies = selectedTrack.settings.eqFrequencies,
                                        onBandChanged = { index, value ->
                                            onUpdateSettings(selectedTrack.id) {
                                                if (index < it.eqBands.size) it.eqBands[index] = value
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showEffects = !showEffects },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PrecisionManufacturing, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Эффекты", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    if (showEffects) "▲" else "▼",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }

                            AnimatedVisibility(visible = showEffects) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    EffectSlider(
                                        label = "Реверберация",
                                        value = selectedTrack.settings.reverbMix,
                                        onValueChange = { v ->
                                            onUpdateSettings(selectedTrack.id) { it.reverbMix = v }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    EffectSlider(
                                        label = "Хорус",
                                        value = selectedTrack.settings.chorusMix,
                                        onValueChange = { v ->
                                            onUpdateSettings(selectedTrack.id) { it.chorusMix = v }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    EffectSlider(
                                        label = "Дисторшн",
                                        value = selectedTrack.settings.distortionGain,
                                        onValueChange = { v ->
                                            onUpdateSettings(selectedTrack.id) { it.distortionGain = v }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Робот",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        androidx.compose.material3.Switch(
                                            checked = selectedTrack.settings.robotEffect,
                                            onCheckedChange = { v ->
                                                onUpdateSettings(selectedTrack.id) { it.robotEffect = v }
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    EffectSlider(
                                        label = "Фильтр ВЧ",
                                        value = (selectedTrack.settings.lowPassCutoff - 20f) / 19980f,
                                        onValueChange = { v ->
                                            onUpdateSettings(selectedTrack.id) { it.lowPassCutoff = v * 19980f + 20f }
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    EffectSlider(
                                        label = "Фильтр НЧ",
                                        value = (selectedTrack.settings.highPassCutoff - 20f) / 1980f,
                                        onValueChange = { v ->
                                            onUpdateSettings(selectedTrack.id) { it.highPassCutoff = v * 1980f + 20f }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Выберите трек для редактирования",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (isProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Обработка аудио...", color = MaterialTheme.colorScheme.onSurface)
                        Text("$progressPercent%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showExportSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.FileDownload, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Экспорт аудио", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Выберите формат:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showExportSheet = false
                                onExport("wav")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WAV", fontWeight = FontWeight.Bold)
                            Text("Lossless", fontSize = 10.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showExportSheet = false
                                onExport("mp3")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MP3", fontWeight = FontWeight.Bold)
                            Text("192 kbps", fontSize = 10.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                showExportSheet = false
                                onExport("ogg")
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("OGG", fontWeight = FontWeight.Bold)
                            Text("Vorbis", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
