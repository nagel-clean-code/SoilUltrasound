package ru.sem.soilultrasound.presentation.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun PointPlotter(pointObj: PointsWrapper) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val gestureHandler = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.1f, 10f)
            offset += pan
        }
    }
    BoxWithConstraints {
        val canvasWidth = constraints.maxWidth.toFloat()
        val canvasHeight = constraints.maxHeight.toFloat()

        val gridParams = remember(scale, offset, constraints) {
            val visibleMinX = -offset.x / scale
            val visibleMaxX = (canvasWidth - offset.x) / scale
            val visibleMinY = -offset.y / scale
            val visibleMaxY = (canvasHeight - offset.y) / scale
            val desiredLines = 10
            val rangeX = visibleMaxX - visibleMinX
            val rangeY = visibleMaxY - visibleMinY
            val gridStepX = 10.0.pow(floor(log10(rangeX / desiredLines)).toDouble()).toFloat()
            val gridStepY = 10.0.pow(floor(log10(rangeY / desiredLines)).toDouble()).toFloat()
            GridParams(gridStepX, gridStepY, visibleMinX, visibleMaxX, visibleMinY, visibleMaxY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureHandler)
        ) {
            drawRect(Color.White)

            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale)
            }) {
                val startX =
                    floor(gridParams.visibleMinX / gridParams.gridStepX) * gridParams.gridStepX
                val endX =
                    ceil(gridParams.visibleMaxX / gridParams.gridStepX) * gridParams.gridStepX
                var x = startX
                while (x <= endX) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x, gridParams.visibleMinY),
                        end = Offset(x, gridParams.visibleMaxY),
                        strokeWidth = 1f / scale
                    )
                    x += gridParams.gridStepX
                }

                val startY =
                    floor(gridParams.visibleMinY / gridParams.gridStepY) * gridParams.gridStepY
                val endY =
                    ceil(gridParams.visibleMaxY / gridParams.gridStepY) * gridParams.gridStepY
                var y = startY
                while (y <= endY) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(gridParams.visibleMinX, y),
                        end = Offset(gridParams.visibleMaxX, y),
                        strokeWidth = 1f / scale
                    )
                    y += gridParams.gridStepY
                }
            }

            drawIntoCanvas { canvas ->
                val xLabelsYPosition = size.height - 20f
                val startX =
                    floor(gridParams.visibleMinX / gridParams.gridStepX) * gridParams.gridStepX
                val endX =
                    ceil(gridParams.visibleMaxX / gridParams.gridStepX) * gridParams.gridStepX
                val xLabels = generateSequence(startX) { it + gridParams.gridStepX }
                    .takeWhile { it <= endX }
                    .map { it.roundToInt().toString() }
                    .toList()
                val xPositions = xLabels.indices.map { idx ->
                    val dataX = startX + idx * gridParams.gridStepX
                    (dataX * scale + offset.x).coerceIn(0f, size.width)
                }
                xLabels.forEachIndexed { idx, label ->
                    canvas.nativeCanvas.drawText(
                        label,
                        xPositions[idx],
                        xLabelsYPosition,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 20f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }

                val yLabelsXPosition = 20f
                val startY =
                    floor(gridParams.visibleMinY / gridParams.gridStepY) * gridParams.gridStepY
                val endY =
                    ceil(gridParams.visibleMaxY / gridParams.gridStepY) * gridParams.gridStepY
                val yLabels = generateSequence(startY) { it + gridParams.gridStepY }
                    .takeWhile { it <= endY }
                    .map { it.roundToInt().toString() }
                    .toList()
                val yPositions = yLabels.indices.map { idx ->
                    val dataY = startY + idx * gridParams.gridStepY
                    (dataY * scale + offset.y).coerceIn(0f, size.height)
                }
                yLabels.forEachIndexed { idx, label ->
                    canvas.nativeCanvas.drawText(
                        label,
                        yLabelsXPosition,
                        yPositions[idx],
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 20f
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }
            }

            pointObj.points.forEach { point ->
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
}

private data class GridParams(
    val gridStepX: Float,
    val gridStepY: Float,
    val visibleMinX: Float,
    val visibleMaxX: Float,
    val visibleMinY: Float,
    val visibleMaxY: Float
)

data class PointsWrapper(val points: List<Offset>)