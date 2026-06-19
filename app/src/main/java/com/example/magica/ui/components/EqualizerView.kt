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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EqualizerView(
    bands: List<Float>,
    labels: List<String>,
    frequencies: List<Float>,
    onBandChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    color = onSurface.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(bands) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                            val bandWidth = size.width.toFloat() / bands.size
                            val bandIndex = (change.position.x / bandWidth).toInt()
                                .coerceIn(0, bands.size - 1)
                            val relativeY = 1f - (change.position.y / size.height.toFloat())
                            val newValue = (relativeY * 24f - 12f).coerceIn(-12f, 12f)
                            onBandChanged(bandIndex, newValue)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val bandWidth = w / bands.size
                val centerY = h / 2f

                drawLine(
                    color = Color.White.copy(alpha = 0.1f),
                    start = Offset(0f, centerY),
                    end = Offset(w, centerY),
                    strokeWidth = 1f
                )

                for (i in 0..4) {
                    val y = centerY - (i + 1) * h / 6f
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                    val y2 = centerY + (i + 1) * h / 6f
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y2),
                        end = Offset(w, y2),
                        strokeWidth = 1f
                    )
                }

                val eqPath = Path()
                for (i in bands.indices) {
                    val x = i * bandWidth + bandWidth / 2f
                    val gain = bands[i].coerceIn(-12f, 12f)
                    val y = centerY - (gain / 12f) * (h / 2f - 8f)
                    if (i == 0) eqPath.moveTo(x, y)
                    else eqPath.lineTo(x, y)
                }

                for (i in bands.indices) {
                    val x = i * bandWidth + bandWidth / 2f
                    val gain = bands[i].coerceIn(-12f, 12f)
                    val y = centerY - (gain / 12f) * (h / 2f - 8f)

                    val color = when {
                        gain > 2f -> Color(0xFF4CAF50)
                        gain < -2f -> Color(0xFFF44336)
                        else -> primary
                    }

                    val barH = kotlin.math.abs(y - centerY)
                    drawRect(
                        color = color.copy(alpha = 0.3f),
                        topLeft = Offset(x - bandWidth * 0.3f, minOf(y, centerY)),
                        size = Size(bandWidth * 0.6f, barH.coerceAtLeast(1f))
                    )

                    drawCircle(color = color, radius = 8f, center = Offset(x, y))
                    drawCircle(color = Color.White, radius = 4f, center = Offset(x, y))
                }

                drawPath(path = eqPath, color = primary, style = Stroke(width = 2f))
            }
        }
    }
}
