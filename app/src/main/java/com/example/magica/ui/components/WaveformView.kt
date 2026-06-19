package com.example.magica.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WaveformView(
    waveform: FloatArray?,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    progress: Float = 0f,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        if (waveform == null || waveform.isEmpty()) {
            drawRect(color = backgroundColor)
            return@Canvas
        }

        drawRect(color = backgroundColor)

        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val halfH = h / 2f - 4f

        val path = Path()
        val progressX = w * progress

        for (i in waveform.indices) {
            val x = (i.toFloat() / waveform.size) * w
            val amp = waveform[i].coerceIn(0f, 1f) * halfH

            if (i == 0) {
                path.moveTo(x, centerY - amp)
            } else {
                path.lineTo(x, centerY - amp)
            }
        }
        for (i in waveform.indices.reversed()) {
            val x = (i.toFloat() / waveform.size) * w
            val amp = waveform[i].coerceIn(0f, 1f) * halfH
            path.lineTo(x, centerY + amp)
        }
        path.close()

        drawPath(
            path = path,
            color = accentColor.copy(alpha = 0.3f),
        )

        val linePath = Path()
        for (i in waveform.indices) {
            val x = (i.toFloat() / waveform.size) * w
            val amp = waveform[i].coerceIn(0f, 1f) * halfH

            if (i == 0) {
                linePath.moveTo(x, centerY - amp)
            } else {
                linePath.lineTo(x, centerY - amp)
            }
        }
        drawPath(
            path = linePath,
            color = accentColor,
            style = Stroke(width = 2f)
        )

        for (i in waveform.indices) {
            val x = (i.toFloat() / waveform.size) * w
            val amp = waveform[i].coerceIn(0f, 1f) * halfH

            if (i == 0) {
                linePath.moveTo(x, centerY + amp)
            } else {
                linePath.lineTo(x, centerY + amp)
            }
        }
        drawPath(
            path = linePath,
            color = accentColor,
            style = Stroke(width = 2f)
        )

        if (progress > 0f) {
            drawLine(
                color = Color.White,
                start = Offset(progressX, 0f),
                end = Offset(progressX, h),
                strokeWidth = 2f
            )
        }
    }
}
