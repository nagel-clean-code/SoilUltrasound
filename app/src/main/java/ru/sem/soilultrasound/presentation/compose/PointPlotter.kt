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
import androidx.compose.ui.graphics.ImageBitmap
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
fun PointPlotter(pointBitmap: ImageBitmap?) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val gestureHandler = Modifier.pointerInput(Unit) {
        detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(0.1f, 10f)
            offset += pan
        }
    }

    BoxWithConstraints {
        val canvasHeight = constraints.maxHeight.toFloat()
        val gridParams = remember(scale, offset, constraints) {
            val visibleMinY = -offset.y / scale
            val visibleMaxY = (canvasHeight - offset.y) / scale
            val desiredLines = 10
            val rangeY = visibleMaxY - visibleMinY
            val gridStepY = 10.0.pow(floor(log10(rangeY / desiredLines)).toDouble()).toFloat()
            GridParams(gridStepY, visibleMinY, visibleMaxY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureHandler)
        ) {
            drawRect(Color.White)

            // Отрисовка меток
            drawIntoCanvas { canvas ->
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

            // Отрисовка растрового изображения точек
            pointBitmap?.let {
                withTransform({
                    translate(offset.x, offset.y)
                    scale(scale, scale)
                }) {
                    drawImage(
                        image = pointBitmap,
                        topLeft = Offset.Zero
                    )
                }
            }
        }
    }
}

private data class GridParams(
    val gridStepY: Float,
    val visibleMinY: Float,
    val visibleMaxY: Float
)