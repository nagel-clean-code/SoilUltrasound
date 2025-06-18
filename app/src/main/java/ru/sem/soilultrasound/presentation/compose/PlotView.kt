//package ru.sem.soilultrasound.presentation.compose
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.gestures.detectTransformGestures
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.drawscope.DrawScope
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.IntSize
//import kotlin.math.ceil
//import kotlin.math.floor
//import kotlin.math.max
//import kotlin.math.min
//
//data class Point(val x: Float, val y: Float)
//
//@Composable
//fun PlotView(
//    points: List<Point>,
//    modifier: Modifier = Modifier
//) {
//    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
//    var scale by remember { mutableStateOf(1f) }
//    var offsetX by remember { mutableStateOf(0f) }
//    var offsetY by remember { mutableStateOf(0f) }
//
//    // Auto-scaling calculations
//    val (contentRect, setContentRect) = remember {
//        mutableStateOf(ContentRect(0f, 0f, 0f, 0f))
//    }
//
//    LaunchedEffect(points, canvasSize) {
//        if (points.isEmpty() || canvasSize.isEmpty) return@LaunchedEffect
//
//        // Calculate content bounds with padding
//        val padding = 0.1f
//        val minX = points.minOf { it.x }
//        val maxX = points.maxOf { it.x }
//        val minY = points.minOf { it.y }
//        val maxY = points.maxOf { it.y }
//
//        val width = maxX - minX
//        val height = maxY - minY
//
//        val paddedMinX = minX - width * padding
//        val paddedMaxX = maxX + width * padding
//        val paddedMinY = minY - height * padding
//        val paddedMaxY = maxY + height * padding
//
//        // Calculate scale and offset
//        val scaleX = canvasSize.width.toFloat() / (paddedMaxX - paddedMinX)
//        val scaleY = canvasSize.height.toFloat() / (paddedMaxY - paddedMinY)
//        val newScale = min(scaleX, scaleY)
//
//        val newOffsetX = (canvasSize.width - (paddedMaxX - paddedMinX) * newScale) / 2
//        val newOffsetY = (canvasSize.height - (paddedMaxY - paddedMinY) * newScale) / 2
//
//        scale = newScale
//        offsetX = newOffsetX
//        offsetY = newOffsetY
//        setContentRect(ContentRect(paddedMinX, paddedMinY, paddedMaxX, paddedMaxY))
//    }
//
//    // Gesture handling
//    val gesture = Modifier.pointerInput(Unit) {
//        detectTransformGestures { centroid, pan, zoom, _ ->
//            // Handle pan
//            offsetX += pan.x
//            offsetY += pan.y
//
//            // Handle zoom
//            val newScale = (scale * zoom).coerceIn(0.1f, 10f)
//            val scaleFactor = newScale / scale
//
//            offsetX = centroid.x - (centroid.x - offsetX) * scaleFactor
//            offsetY = centroid.y - (centroid.y - offsetY) * scaleFactor
//            scale = newScale
//        }
//    }
//
//    Box(
//        modifier = modifier
//            .fillMaxSize()
//            .onSizeChanged { canvasSize = it }
//    ) {
//        Canvas(
//            modifier = Modifier
//                .fillMaxSize()
//                .then(gesture)
//        ) {
//            // Draw background
//            drawRect(Color.White)
//
//            // Draw scale markers
//            drawScaleMarkers(contentRect)
//
//            // Draw points
//            drawPoints(points, contentRect)
//        }
//    }
//}
//
//@Composable
//private fun Modifier.onSizeChanged(
//    action: (IntSize) -> Unit
//): Modifier {
//    val density = LocalDensity.current
//    return this.then(
//        Modifier.onSizeChanged {
//            with(density) {
//                action(IntSize(it.width.toPx().toInt(), it.height.toPx().toInt()))
//            }
//        }
//    )
//}
//
//private fun DrawScope.drawScaleMarkers(contentRect: ContentRect) {
//    val dashInterval = 50f // Marker interval in data coordinates
//    val markerWidth = 20f
//
//    val minY = contentRect.minY
//    val maxY = contentRect.maxY
//
//    var currentY = ceil(minY / dashInterval) * dashInterval
//    while (currentY <= maxY) {
//        val yPos = (currentY - contentRect.minY) * scale + offsetY
//        drawLine(
//            color = Color.LightGray,
//            start = Offset(0f, yPos),
//            end = Offset(markerWidth, yPos),
//            strokeWidth = 1f,
//            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
//        )
//        currentY += dashInterval
//    }
//}
//
//private fun DrawScope.drawPoints(
//    points: List<Point>,
//    contentRect: ContentRect
//) {
//    val pointRadius = 3f
//    val scaleX = scale
//    val scaleY = scale
//
//    points.forEach { point ->
//        val x = (point.x - contentRect.minX) * scaleX + offsetX
//        val y = (point.y - contentRect.minY) * scaleY + offsetY
//        drawCircle(
//            color = Color.Black,
//            radius = pointRadius,
//            center = Offset(x, y)
//        )
//    }
//}
//
//private data class ContentRect(
//    val minX: Float,
//    val minY: Float,
//    val maxX: Float,
//    val maxY: Float
//)