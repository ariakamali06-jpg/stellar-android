package com.stellar.editor.data

import android.content.Context
import android.graphics.*
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object ProjectManager {

    fun exportPageToBitmap(context: Context, page: Page, targetWidth: Int = 1080, targetHeight: Int = 1920): Bitmap {
        var baseBitmap: Bitmap? = null
        if (!page.imageUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(page.imageUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    baseBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) { }
        }

        val w = baseBitmap?.width ?: targetWidth
        val h = baseBitmap?.height ?: targetHeight
        val outputBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        if (baseBitmap != null) {
            canvas.drawBitmap(baseBitmap!!, 0f, 0f, null)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (item in page.textItems) {
            canvas.save()
            canvas.translate(item.x, item.y)
            if (item.rotation != 0f) {
                canvas.rotate(item.rotation, item.width / 2f, item.height / 2f)
            }

            val rect = RectF(0f, 0f, item.width, item.height)

            if (item.hasBubble) {
                if (item.hasShadow) {
                    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = item.shadowColor.toInt()
                        style = Paint.Style.FILL
                    }
                    val shadowRect = RectF(
                        rect.left + item.shadowOffsetX,
                        rect.top + item.shadowOffsetY,
                        rect.right + item.shadowOffsetX,
                        rect.bottom + item.shadowOffsetY
                    )
                    drawBubbleShape(canvas, shadowRect, item.bubbleShape, shadowPaint)
                }

                paint.color = item.bubbleColor.toInt()
                paint.style = Paint.Style.FILL
                drawBubbleShape(canvas, rect, item.bubbleShape, paint)

                if (item.strokeWidth > 0f) {
                    strokePaint.color = item.strokeColor.toInt()
                    strokePaint.strokeWidth = item.strokeWidth
                    drawBubbleShape(canvas, rect, item.bubbleShape, strokePaint)
                }
            }

            textPaint.apply {
                color = item.fontColor.toInt()
                textSize = item.fontSize
                textAlign = when (item.alignment) {
                    AlignmentMode.LEFT -> Paint.Align.LEFT
                    AlignmentMode.CENTER -> Paint.Align.CENTER
                    AlignmentMode.RIGHT -> Paint.Align.RIGHT
                }
                var style = Typeface.NORMAL
                if (item.isBold && item.isItalic) style = Typeface.BOLD_ITALIC
                else if (item.isBold) style = Typeface.BOLD
                else if (item.isItalic) style = Typeface.ITALIC
                typeface = Typeface.create(Typeface.DEFAULT, style)
            }

            val lines = item.text.split("\n")
            val lineHeight = textPaint.fontSpacing
            val totalTextHeight = lines.size * lineHeight
            var startY = (item.height - totalTextHeight) / 2f + textPaint.textSize

            val textX = when (item.alignment) {
                AlignmentMode.LEFT -> 20f
                AlignmentMode.CENTER -> item.width / 2f
                AlignmentMode.RIGHT -> item.width - 20f
            }

            for (line in lines) {
                if (item.hasExtrusion) {
                    val extPaint = Paint(textPaint).apply {
                        color = Color.BLACK
                    }
                    for (d in 1..item.extrusionDepth.toInt()) {
                        canvas.drawText(line, textX + d, startY + d, extPaint)
                    }
                }
                canvas.drawText(line, textX, startY, textPaint)
                startY += lineHeight
            }

            canvas.restore()
        }

        return outputBitmap
    }

    private fun drawBubbleShape(canvas: Canvas, rect: RectF, shape: BubbleShape, paint: Paint) {
        when (shape) {
            BubbleShape.ELLIPSE -> {
                canvas.drawOval(rect, paint)
            }
            BubbleShape.RECTANGLE -> {
                canvas.drawRect(rect, paint)
            }
            BubbleShape.ROUNDED_RECT -> {
                canvas.drawRoundRect(rect, 24f, 24f, paint)
            }
            BubbleShape.CLOUD -> {
                val path = Path()
                val cx = rect.centerX()
                val cy = rect.centerY()
                val rx = rect.width() / 2f
                val ry = rect.height() / 2f
                path.addOval(RectF(cx - rx * 0.8f, cy - ry * 0.8f, cx + rx * 0.8f, cy + ry * 0.8f), Path.Direction.CW)
                path.addCircle(cx - rx * 0.5f, cy, ry * 0.5f, Path.Direction.CW)
                path.addCircle(cx + rx * 0.5f, cy, ry * 0.5f, Path.Direction.CW)
                path.addCircle(cx, cy - ry * 0.5f, rx * 0.45f, Path.Direction.CW)
                path.addCircle(cx, cy + ry * 0.5f, rx * 0.45f, Path.Direction.CW)
                canvas.drawPath(path, paint)
            }
            BubbleShape.EXPLOSION -> {
                val path = Path()
                val cx = rect.centerX()
                val cy = rect.centerY()
                val points = 12
                val outerR = rect.width().coerceAtMost(rect.height()) / 2f
                val innerR = outerR * 0.65f
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = (i * Math.PI / points) - (Math.PI / 2)
                    val px = cx + (r * Math.cos(angle)).toFloat()
                    val py = cy + (r * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
        }
    }

    fun exportAllPagesToFiles(context: Context, project: Project): List<File> {
        val exportDir = File(context.getExternalFilesDir(null), "Stellar_Exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val results = mutableListOf<File>()
        project.pages.forEachIndexed { index, page ->
            val num = String.format("%02d", index + 1)
            val file = File(exportDir, "$num.png")
            val bitmap = exportPageToBitmap(context, page)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            results.add(file)
        }
        return results
    }

    fun projectToJson(project: Project): String {
        val root = JSONObject()
        root.put("name", project.name)
        val pagesArray = JSONArray()
        for (page in project.pages) {
            val pObj = JSONObject()
            pObj.put("id", page.id)
            pObj.put("pageNumber", page.pageNumber)
            pObj.put("imageUri", page.imageUri ?: "")
            val itemsArray = JSONArray()
            for (item in page.textItems) {
                val iObj = JSONObject()
                iObj.put("id", item.id)
                iObj.put("text", item.text)
                iObj.put("x", item.x.toDouble())
                iObj.put("y", item.y.toDouble())
                iObj.put("width", item.width.toDouble())
                iObj.put("height", item.height.toDouble())
                iObj.put("fontSize", item.fontSize.toDouble())
                iObj.put("fontColor", item.fontColor)
                iObj.put("bubbleColor", item.bubbleColor)
                iObj.put("strokeColor", item.strokeColor)
                iObj.put("strokeWidth", item.strokeWidth.toDouble())
                iObj.put("hasBubble", item.hasBubble)
                iObj.put("bubbleShape", item.bubbleShape.name)
                iObj.put("hasShadow", item.hasShadow)
                iObj.put("hasExtrusion", item.hasExtrusion)
                iObj.put("isBold", item.isBold)
                iObj.put("isItalic", item.isItalic)
                iObj.put("alignment", item.alignment.name)
                iObj.put("rotation", item.rotation.toDouble())
                itemsArray.put(iObj)
            }
            pObj.put("textItems", itemsArray)
            pagesArray.put(pObj)
        }
        root.put("pages", pagesArray)
        return root.toString(2)
    }
}
