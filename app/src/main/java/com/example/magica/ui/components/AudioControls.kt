package com.example.magica.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlaybackControlBar(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onStop: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            if (durationMs > 0) {
                                val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                                onSeek((ratio * durationMs).toLong())
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    cornerRadius = CornerRadius(4f),
                    size = Size(w, 6f),
                    topLeft = Offset(0f, h / 2f - 3f)
                )

                val progress = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
                if (progress > 0f) {
                    drawRoundRect(
                        color = primaryColor,
                        cornerRadius = CornerRadius(4f),
                        size = Size(w * progress, 6f),
                        topLeft = Offset(0f, h / 2f - 3f)
                    )
                }

                val thumbX = w * progress
                drawCircle(color = primaryColor, radius = 8f, center = Offset(thumbX, h / 2f))
                drawCircle(color = Color.White, radius = 4f, center = Offset(thumbX, h / 2f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(positionMs),
                color = surfaceColor.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
            Text(
                text = formatTime(durationMs),
                color = surfaceColor.copy(alpha = 0.5f),
                fontSize = 11.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onStop) {
                Icon(Icons.Default.Stop, "Stop", tint = surfaceColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(56.dp)
                    .background(primaryColor, CircleShape)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun EffectSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 1f,
    showLabel: Boolean = true,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, color = onSurface, fontSize = 12.sp)
                Text(
                    text = String.format("%.0f%%", value * 100),
                    color = primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            val newValue = ratio * (max - min) + min
                            onValueChange(newValue.coerceIn(min, max))
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val normalized = ((value - min) / (max - min)).coerceIn(0f, 1f)

                drawRoundRect(
                    color = Color.White.copy(alpha = 0.1f),
                    cornerRadius = CornerRadius(4f),
                    size = Size(w, 8f),
                    topLeft = Offset(0f, h / 2f - 4f)
                )

                if (normalized > 0f) {
                    val fillColor = when {
                        label.contains("Pitch") || label.contains("Тон") -> primary
                        label.contains("Speed") || label.contains("Скор") -> secondary
                        else -> primary
                    }
                    drawRoundRect(
                        color = fillColor,
                        cornerRadius = CornerRadius(4f),
                        size = Size(w * normalized, 8f),
                        topLeft = Offset(0f, h / 2f - 4f)
                    )
                }

                drawCircle(color = Color.White, radius = 6f, center = Offset(w * normalized, h / 2f))
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
