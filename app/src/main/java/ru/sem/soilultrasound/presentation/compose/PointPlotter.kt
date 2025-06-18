package ru.sem.soilultrasound.presentation.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.*

@Composable
fun PointPlotter(points: List<Offset>) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val gestureHandler = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.1f, 10f)
            offset += pan
        }
    }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }
            val width = maxX - minX
            val height = maxY - minY
            if (width > 0 && height > 0) {
                scale = 1f
                offset = Offset(-minX, -minY)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize().then(gestureHandler)) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        if (points.isNotEmpty()) {
            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }
            val width = maxX - minX
            val height = maxY - minY
            if (width > 0 && height > 0) {
                scale = minOf(canvasWidth / width, canvasHeight / height) * 0.9f
                offset = Offset(
                    -minX * scale + (canvasWidth - width * scale) / 2,
                    -minY * scale + (canvasHeight - height * scale) / 2
                )
            }
        }

        drawRect(Color.White)

        val visibleMinX = -offset.x / scale
        val visibleMaxX = (canvasWidth - offset.x) / scale
        val visibleMinY = -offset.y / scale
        val visibleMaxY = (canvasHeight - offset.y) / scale

        val desiredLines = 10
        val rangeX = visibleMaxX - visibleMinX
        val rangeY = visibleMaxY - visibleMinY
        val gridStepX = 10.0.pow(floor(log10(rangeX / desiredLines)).toDouble()).toFloat()
        val gridStepY = 10.0.pow(floor(log10(rangeY / desiredLines)).toDouble()).toFloat()

        withTransform({
            translate(offset.x, offset.y)
            scale(scale, scale)
        }) {
            val startX = floor(visibleMinX / gridStepX) * gridStepX
            val endX = ceil(visibleMaxX / gridStepX) * gridStepX
            var x = startX
            while (x <= endX) {
                val canvasX = x
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(canvasX, visibleMinY),
                    end = Offset(canvasX, visibleMaxY),
                    strokeWidth = 1f / scale
                )
                x += gridStepX
            }

            val startY = floor(visibleMinY / gridStepY) * gridStepY
            val endY = ceil(visibleMaxY / gridStepY) * gridStepY
            var y = startY
            while (y <= endY) {
                val canvasY = y
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(visibleMinX, canvasY),
                    end = Offset(visibleMaxX, canvasY),
                    strokeWidth = 1f / scale
                )
                y += gridStepY
            }
        }

        drawIntoCanvas { canvas ->
            // Метки для оси X
            val xLabelsYPosition = canvasHeight - 20f
            val startX = floor(visibleMinX / gridStepX) * gridStepX
            val endX = ceil(visibleMaxX / gridStepX) * gridStepX
            val xLabels = generateSequence(startX) { it + gridStepX }
                .takeWhile { it <= endX }
                .map { it.roundToInt().toString() }
                .toList()
            val xPositions = xLabels.indices.map { idx ->
                val dataX = startX + idx * gridStepX
                (dataX * scale + offset.x).coerceIn(0f, canvasWidth)
            }
            xLabels.forEachIndexed { idx, label ->
                val screenX = xPositions[idx]
                canvas.nativeCanvas.drawText(
                    label,
                    screenX,
                    xLabelsYPosition,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }

            // Метки для оси Y
            val yLabelsXPosition = 20f
            val startY = floor(visibleMinY / gridStepY) * gridStepY
            val endY = ceil(visibleMaxY / gridStepY) * gridStepY
            val yLabels = generateSequence(startY) { it + gridStepY }
                .takeWhile { it <= endY }
                .map { it.roundToInt().toString() }
                .toList()
            val yPositions = yLabels.indices.map { idx ->
                val dataY = startY + idx * gridStepY
                (dataY * scale + offset.y).coerceIn(0f, canvasHeight)
            }
            yLabels.forEachIndexed { idx, label ->
                val screenY = yPositions[idx]
                canvas.nativeCanvas.drawText(
                    label,
                    yLabelsXPosition,
                    screenY,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 20f
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }
        }

        points.forEach { point ->
            val x = (point.x * scale) + offset.x
            val y = (point.y * scale) + offset.y
            drawCircle(
                color = Color.Black,
                radius = 5f / scale,
                center = Offset(x, y)
            )
        }
    }
}