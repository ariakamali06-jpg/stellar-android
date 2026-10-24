package com.stellar.editor.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.stellar.editor.data.AlignmentMode
import com.stellar.editor.data.BubbleShape
import com.stellar.editor.data.Page
import com.stellar.editor.data.TextItem
import com.stellar.editor.ui.theme.BgDark
import com.stellar.editor.ui.theme.IndigoAccent

@OptIn(ExperimentalTextApi::class)
@Composable
fun MangaCanvas(
    page: Page,
    selectedItem: TextItem?,
    onSelectItem: (TextItem?) -> Unit,
    onItemMoved: (TextItem, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    var bgImageBitmap by remember(page.imageUri) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(page.imageUri) {
        if (!page.imageUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(page.imageUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bm = BitmapFactory.decodeStream(stream)
                    bgImageBitmap = bm?.asImageBitmap()
                }
            } catch (_: Exception) {
                bgImageBitmap = null
            }
        } else {
            bgImageBitmap = null
        }
    }

    var draggedItem by remember { mutableStateOf<TextItem?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgDark)
            .pointerInput(page.textItems) {
                detectTapGestures { tapOffset ->
                    val hit = page.textItems.lastOrNull { item ->
                        tapOffset.x >= item.x && tapOffset.x <= item.x + item.width &&
                                tapOffset.y >= item.y && tapOffset.y <= item.y + item.height
                    }
                    onSelectItem(hit)
                }
            }
            .pointerInput(page.textItems) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val hit = page.textItems.lastOrNull { item ->
                            startOffset.x >= item.x && startOffset.x <= item.x + item.width &&
                                    startOffset.y >= item.y && startOffset.y <= item.y + item.height
                        }
                        if (hit != null) {
                            draggedItem = hit
                            onSelectItem(hit)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggedItem?.let { item ->
                            onItemMoved(item, item.x + dragAmount.x, item.y + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        draggedItem = null
                    },
                    onDragCancel = {
                        draggedItem = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val bg = bgImageBitmap
            if (bg != null) {
                drawImage(
                    image = bg,
                    dstSize = androidx.compose.ui.unit.IntSize(canvasWidth.toInt(), canvasHeight.toInt())
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
                    drawCircle(
                        color = IndigoAccent,
                        radius = 8f,
                        center = Offset(item.x + item.width + 4f, item.y + item.height + 4f)
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBubbleShape(
    shape: BubbleShape,
    offset: Offset,
    size: Size,
    color: Color
) {
    when (shape) {
        BubbleShape.ELLIPSE -> {
            drawOval(color = color, topLeft = offset, size = size)
        }
        BubbleShape.RECTANGLE -> {
            drawRect(color = color, topLeft = offset, size = size)
        }
        BubbleShape.ROUNDED_RECT -> {
            drawRoundRect(color = color, topLeft = offset, size = size, cornerRadius = CornerRadius(24f, 24f))
        }
        BubbleShape.CLOUD -> {
            val path = Path().apply {
                val cx = offset.x + size.width / 2f
                val cy = offset.y + size.height / 2f
                val rx = size.width / 2f
                val ry = size.height / 2f
                addOval(androidx.compose.ui.geometry.Rect(cx - rx * 0.8f, cy - ry * 0.8f, cx + rx * 0.8f, cy + ry * 0.8f))
                addOval(androidx.compose.ui.geometry.Rect(cx - rx, cy - ry * 0.4f, cx - rx * 0.2f, cy + ry * 0.4f))
                addOval(androidx.compose.ui.geometry.Rect(cx + rx * 0.2f, cy - ry * 0.4f, cx + rx, cy + ry * 0.4f))
                addOval(androidx.compose.ui.geometry.Rect(cx - rx * 0.4f, cy - ry, cx + rx * 0.4f, cy - ry * 0.2f))
                addOval(androidx.compose.ui.geometry.Rect(cx - rx * 0.4f, cy + ry * 0.2f, cx + rx * 0.4f, cy + ry))
            }
            drawPath(path = path, color = color)
        }
        BubbleShape.EXPLOSION -> {
            val path = Path().apply {
                val cx = offset.x + size.width / 2f
                val cy = offset.y + size.height / 2f
                val points = 12
                val outerR = size.width.coerceAtMost(size.height) / 2f
                val innerR = outerR * 0.65f
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = (i * Math.PI / points) - (Math.PI / 2)
                    val px = cx + (r * Math.cos(angle)).toFloat()
                    val py = cy + (r * Math.sin(angle)).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBubbleShapeOutline(
    shape: BubbleShape,
    offset: Offset,
    size: Size,
    color: Color,
    strokeWidth: Float
) {
    val strokeStyle = Stroke(width = strokeWidth)
    when (shape) {
        BubbleShape.ELLIPSE -> {
            drawOval(color = color, topLeft = offset, size = size, style = strokeStyle)
        }
        BubbleShape.RECTANGLE -> {
            drawRect(color = color, topLeft = offset, size = size, style = strokeStyle)
        }
        BubbleShape.ROUNDED_RECT -> {
            drawRoundRect(color = color, topLeft = offset, size = size, cornerRadius = CornerRadius(24f, 24f), style = strokeStyle)
        }
        BubbleShape.CLOUD, BubbleShape.EXPLOSION -> {
            drawBubbleShape(shape, offset, size, color)
        }
    }
}
