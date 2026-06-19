package com.example.magica.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.magica.model.TrackData

private val trackColors = listOf(
    Color(0xFF7C4DFF),
    Color(0xFF448AFF),
    Color(0xFF00BCD4),
    Color(0xFF4CAF50),
    Color(0xFFFFC107),
    Color(0xFFFF5722),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
)

@Composable
fun TimelineView(
    tracks: List<TrackData>,
    selectedTrackId: Long?,
    totalDurationMs: Long,
    currentPositionMs: Long,
    isPlaying: Boolean,
    onSelectTrack: (Long) -> Unit,
    onMoveTrack: (Long, Long) -> Unit,
    onToggleMute: (Long) -> Unit,
    onToggleSolo: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Timeline",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            val maxMs = totalDurationMs.coerceAtLeast(1000L)
            Canvas(modifier = Modifier.fillMaxWidth().height(32.dp)) {
                val w = size.width
                val segmentMs = when {
                    maxMs <= 10000 -> 1000L
                    maxMs <= 30000 -> 5000L
                    maxMs <= 60000 -> 10000L
                    else -> 30000L
                }
                val pixelsPerMs = w / maxMs
                var t = 0L
                while (t <= maxMs) {
                    val x = (t * pixelsPerMs).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = if (t % 10000 == 0L) 1.5f else 0.5f
                    )
                    val sec = t / 1000
                    if (t % 10000 == 0L) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "${sec / 60}:%02d".format(sec % 60),
                            x + 3f,
                            20f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(100, 255, 255, 255)
                                textSize = 22f
                            }
                        )
                    }
                    t += segmentMs
                }

                if (currentPositionMs > 0) {
                    val playX = (currentPositionMs * pixelsPerMs).toFloat()
                    drawLine(
                        color = Color.White,
                        start = Offset(playX, 0f),
                        end = Offset(playX, size.height),
                        strokeWidth = 2f
                    )
                }
            }
        }

        tracks.forEach { track ->
            val color = trackColors[track.colorIndex % trackColors.size]
            val isSelected = track.id == selectedTrackId

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .pointerInput(track.id) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.width(3.dp).height(32.dp)) {
                        drawRoundRect(
                            color = color,
                            cornerRadius = CornerRadius(2f),
                            size = Size(size.width, size.height)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = track.displayName,
                        color = if (track.muted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(72.dp)
                    )

                    val totalPixels = size.width - 120f
                    val pixelsPerMs = if (totalDurationMs > 0) totalPixels / totalDurationMs else 0f
                    val trackStart = (track.startTimeMs * pixelsPerMs).toFloat()
                    val trackWidth = (track.durationMs * pixelsPerMs).coerceAtLeast(20f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
                            val rectOffset = trackStart.coerceAtLeast(0f)
                            val rectWidth = trackWidth.coerceAtMost(size.width - rectOffset - 4f).coerceAtLeast(4f)
                            drawRoundRect(
                                color = if (track.muted) color.copy(alpha = 0.2f) else color.copy(alpha = 0.6f),
                                cornerRadius = CornerRadius(4f),
                                size = Size(rectWidth, 22f),
                                topLeft = Offset(rectOffset, 3f)
                            )
                            val nameText = track.displayName
                            drawContext.canvas.nativeCanvas.drawText(
                                nameText,
                                rectOffset + 6f,
                                18f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 18f
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (track.muted) "M" else if (track.solo) "S" else "",
                        color = if (track.muted) Color(0xFFFF5252) else if (track.solo) Color(0xFF69F0AE) else Color.Transparent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(1.dp))
        }
    }
}
