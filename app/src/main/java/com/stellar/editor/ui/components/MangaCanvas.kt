package com.stellar.editor.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stellar.editor.data.AlignmentMode
import com.stellar.editor.data.BubbleShape
import com.stellar.editor.data.Page
import com.stellar.editor.data.TextItem
import com.stellar.editor.ui.theme.BgDark
import com.stellar.editor.ui.theme.IndigoAccent
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

private enum class DragMode { NONE, MOVE, RESIZE }

@OptIn(ExperimentalTextApi::class)
@Composable
fun MangaCanvas(
    page: Page,
    selectedItem: TextItem?,
    onSelectItem: (String?) -> Unit,
    onMoveItem: (String, Float, Float) -> Unit,
    onResizeItem: (String, Float, Float) -> Unit,
    onDragStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    var bgImageBitmap by remember(page.imagePath, page.imageUri) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(page.imagePath, page.imageUri) {
        var loaded: ImageBitmap? = null
        if (!page.imagePath.isNullOrBlank()) {
            try {
                val f = File(page.imagePath)
                if (f.exists()) {
                    val bm = BitmapFactory.decodeFile(f.absolutePath)
                    loaded = bm?.asImageBitmap()
                }
            } catch (_: Exception) { }
        }
        if (loaded == null && !page.imageUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(page.imageUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bm = BitmapFactory.decodeStream(stream)
                    loaded = bm?.asImageBitmap()
                }
            } catch (_: Exception) { }
        }
        bgImageBitmap = loaded
    }

    var activeDragMode by remember { mutableStateOf(DragMode.NONE) }
    var targetItemId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Gesture 1: Pinch zoom and two-finger pan
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 4f)
                        panOffset += pan
                    }
                }
                // Gesture 2: Tap to select/deselect
                .pointerInput(page.textItems, scale, panOffset) {
                    detectTapGestures { tapOffset ->
                        val canvasX = (tapOffset.x - panOffset.x) / scale
                        val canvasY = (tapOffset.y - panOffset.y) / scale

                        val hit = page.textItems.lastOrNull { item ->
                            canvasX >= item.x && canvasX <= item.x + item.width &&
                                    canvasY >= item.y && canvasY <= item.y + item.height
                        }
                        onSelectItem(hit?.id)
                    }
                }
                // Gesture 3: Drag to Move or Resize
                .pointerInput(page.textItems, selectedItem, scale, panOffset) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val canvasX = (startOffset.x - panOffset.x) / scale
                            val canvasY = (startOffset.y - panOffset.y) / scale

                            if (selectedItem != null) {
                                val handleRadius = 24f / scale
                                val handleCenterX = selectedItem.x + selectedItem.width + 4f
                                val handleCenterY = selectedItem.y + selectedItem.height + 4f
                                val distSq = (canvasX - handleCenterX) * (canvasX - handleCenterX) +
                                        (canvasY - handleCenterY) * (canvasY - handleCenterY)

                                if (distSq <= handleRadius * handleRadius * 4) {
                                    activeDragMode = DragMode.RESIZE
                                    targetItemId = selectedItem.id
                                    onDragStart()
                                    return@detectDragGestures
                                }
                            }

                            val hit = page.textItems.lastOrNull { item ->
                                canvasX >= item.x && canvasX <= item.x + item.width &&
                                        canvasY >= item.y && canvasY <= item.y + item.height
                            }

                            if (hit != null) {
                                activeDragMode = DragMode.MOVE
                                targetItemId = hit.id
                                onSelectItem(hit.id)
                                onDragStart()
                            } else {
                                activeDragMode = DragMode.NONE
                                targetItemId = null
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val id = targetItemId ?: return@detectDragGestures
                            val scaledDx = dragAmount.x / scale
                            val scaledDy = dragAmount.y / scale

                            when (activeDragMode) {
                                DragMode.MOVE -> onMoveItem(id, scaledDx, scaledDy)
                                DragMode.RESIZE -> onResizeItem(id, scaledDx, scaledDy)
                                DragMode.NONE -> {
                                    panOffset += dragAmount
                                }
                            }
                        },
                        onDragEnd = {
                            activeDragMode = DragMode.NONE
                            targetItemId = null
                        },
                        onDragCancel = {
                            activeDragMode = DragMode.NONE
                            targetItemId = null
                        }
                    )
                }
        ) {
            withTransform({
                translate(panOffset.x, panOffset.y)
                scale(scale, scale, Offset.Zero)
            }) {
                val bg = bgImageBitmap
                if (bg != null) {
                    val canvasRatio = size.width / size.height
                    val imageRatio = bg.width.toFloat() / bg.height.toFloat()

                    val dstWidth: Float
                    val dstHeight: Float
                    val dstLeft: Float
                    val dstTop: Float

                    if (imageRatio > canvasRatio) {
                        dstWidth = size.width
                        dstHeight = size.width / imageRatio
                        dstLeft = 0f
                        dstTop = (size.height - dstHeight) / 2f
                    } else {
                        dstHeight = size.height
                        dstWidth = size.height * imageRatio
                        dstLeft = (size.width - dstWidth) / 2f
                        dstTop = 0f
                    }

                    drawImage(
                        image = bg,
                        dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                        dstSize = IntSize(dstWidth.toInt(), dstHeight.toInt())
                    )
                } else {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset.Zero,
                        size = size
                    )
                }

                for (item in page.textItems) {
                    val isSelected = (item.id == selectedItem?.id)
                    val itemSize = Size(item.width, item.height)
                    val itemOffset = Offset(item.x, item.y)

                    if (item.hasBubble) {
                        val bubbleColor = Color(item.bubbleColor)
                        val strokeColor = Color(item.strokeColor)

                        if (item.hasShadow) {
                            val shadowColor = Color(item.shadowColor)
                            val sOffset = Offset(item.x + item.shadowOffsetX, item.y + item.shadowOffsetY)
                            drawBubbleShape(item.bubbleShape, sOffset, itemSize, shadowColor)
                        }

                        drawBubbleShape(item.bubbleShape, itemOffset, itemSize, bubbleColor)

                        if (item.strokeWidth > 0f) {
                            drawBubbleShapeOutline(item.bubbleShape, itemOffset, itemSize, strokeColor, item.strokeWidth)
                        }
                    }

                    val textAlign = when (item.alignment) {
                        AlignmentMode.LEFT -> TextAlign.Left
                        AlignmentMode.CENTER -> TextAlign.Center
                        AlignmentMode.RIGHT -> TextAlign.Right
                    }

                    val fontWeight = if (item.isBold) FontWeight.Bold else FontWeight.Normal
                    val fontStyle = if (item.isItalic) FontStyle.Italic else FontStyle.Normal

                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(item.text),
                        style = TextStyle(
                            color = Color(item.fontColor),
                            fontSize = item.fontSize.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = fontWeight,
                            fontStyle = fontStyle,
                            textAlign = textAlign
                        ),
                        constraints = androidx.compose.ui.unit.Constraints(
                            maxWidth = (item.width - 24f).toInt().coerceAtLeast(10)
                        )
                    )

                    val textX = item.x + (item.width - textLayoutResult.size.width) / 2f
                    val textY = item.y + (item.height - textLayoutResult.size.height) / 2f

                    if (item.hasExtrusion) {
                        for (d in 1..item.extrusionDepth.toInt()) {
                            drawText(
                                textLayoutResult = textLayoutResult,
                                topLeft = Offset(textX + d, textY + d),
                                color = Color.Black
                            )
                        }
                    }

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(textX, textY)
                    )

                    if (isSelected) {
                        drawRoundRect(
                            color = IndigoAccent,
                            topLeft = Offset(item.x - 4f, item.y - 4f),
                            size = Size(item.width + 8f, item.height + 8f),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(width = 3f)
                        )

                        val handleCenter = Offset(item.x + item.width + 4f, item.y + item.height + 4f)
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = handleCenter
                        )
                        drawCircle(
                            color = IndigoAccent,
                            radius = 8f,
                            center = handleCenter
                        )
                    }
                }
            }
        }

        // Reset Zoom & Pan Floating Button
        if (scale != 1f || panOffset != Offset.Zero) {
            SmallFloatingActionButton(
                onClick = {
                    scale = 1f
                    panOffset = Offset.Zero
                },
                containerColor = IndigoAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.FitScreen, contentDescription = "ریست زوم", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${(scale * 100).toInt()}%", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun createCloudPath(offset: Offset, size: Size): Path {
    val cx = offset.x + size.width / 2f
    val cy = offset.y + size.height / 2f
    val rx = size.width / 2f
    val ry = size.height / 2f
    return Path().apply {
        addOval(Rect(cx - rx * 0.8f, cy - ry * 0.8f, cx + rx * 0.8f, cy + ry * 0.8f))
        addOval(Rect(cx - rx, cy - ry * 0.4f, cx - rx * 0.2f, cy + ry * 0.4f))
        addOval(Rect(cx + rx * 0.2f, cy - ry * 0.4f, cx + rx, cy + ry * 0.4f))
        addOval(Rect(cx - rx * 0.4f, cy - ry, cx + rx * 0.4f, cy - ry * 0.2f))
        addOval(Rect(cx - rx * 0.4f, cy + ry * 0.2f, cx + rx * 0.4f, cy + ry))
    }
}

private fun createExplosionPath(offset: Offset, size: Size): Path {
    val cx = offset.x + size.width / 2f
    val cy = offset.y + size.height / 2f
    val points = 12
    val outerR = size.width.coerceAtMost(size.height) / 2f
    val innerR = outerR * 0.65f
    return Path().apply {
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * Math.PI / points) - (Math.PI / 2)
            val px = cx + (r * cos(angle)).toFloat()
            val py = cy + (r * sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
}

private fun DrawScope.drawBubbleShape(
    shape: BubbleShape,
    offset: Offset,
    size: Size,
    color: Color
) {
    when (shape) {
        BubbleShape.ELLIPSE -> drawOval(color = color, topLeft = offset, size = size, style = Fill)
        BubbleShape.RECTANGLE -> drawRect(color = color, topLeft = offset, size = size, style = Fill)
        BubbleShape.ROUNDED_RECT -> drawRoundRect(color = color, topLeft = offset, size = size, cornerRadius = CornerRadius(24f, 24f), style = Fill)
        BubbleShape.CLOUD -> drawPath(path = createCloudPath(offset, size), color = color, style = Fill)
        BubbleShape.EXPLOSION -> drawPath(path = createExplosionPath(offset, size), color = color, style = Fill)
    }
}

private fun DrawScope.drawBubbleShapeOutline(
    shape: BubbleShape,
    offset: Offset,
    size: Size,
    color: Color,
    strokeWidth: Float
) {
    val strokeStyle = Stroke(width = strokeWidth)
    when (shape) {
        BubbleShape.ELLIPSE -> drawOval(color = color, topLeft = offset, size = size, style = strokeStyle)
        BubbleShape.RECTANGLE -> drawRect(color = color, topLeft = offset, size = size, style = strokeStyle)
        BubbleShape.ROUNDED_RECT -> drawRoundRect(color = color, topLeft = offset, size = size, cornerRadius = CornerRadius(24f, 24f), style = strokeStyle)
        BubbleShape.CLOUD -> drawPath(path = createCloudPath(offset, size), color = color, style = strokeStyle)
        BubbleShape.EXPLOSION -> drawPath(path = createExplosionPath(offset, size), color = color, style = strokeStyle)
    }
}
