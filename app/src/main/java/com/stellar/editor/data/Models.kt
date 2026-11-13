package com.stellar.editor.data

import java.util.UUID

enum class BubbleShape(val displayName: String) {
    ELLIPSE("بیضی"),
    RECTANGLE("مستطیل"),
    CLOUD("ابر"),
    EXPLOSION("انفجار"),
    ROUNDED_RECT("گوشه‌گرد")
}

enum class AlignmentMode {
    LEFT, CENTER, RIGHT
}

enum class WarpEffect(val displayName: String) {
    NONE("عادی"),
    WAVE("موج"),
    ZIGZAG("زیگزاگ"),
    TREMBLING("لرزان")
}

data class TextItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "متن جدید",
    val x: Float = 100f,
    val y: Float = 100f,
    val width: Float = 220f,
    val height: Float = 120f,
    val fontSize: Float = 22f,
    val fontColor: Long = 0xFF000000,
    val bubbleColor: Long = 0xFFFFFFFF,
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 3f,
    val hasBubble: Boolean = true,
    val bubbleShape: BubbleShape = BubbleShape.ELLIPSE,
    val hasShadow: Boolean = false,
    val shadowColor: Long = 0x66000000,
    val shadowOffsetX: Float = 6f,
    val shadowOffsetY: Float = 6f,
    val hasExtrusion: Boolean = false,
    val extrusionDepth: Float = 5f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val alignment: AlignmentMode = AlignmentMode.CENTER,
    val warpEffect: WarpEffect = WarpEffect.NONE,
    val rotation: Float = 0f
)

data class Page(
    val id: String = UUID.randomUUID().toString(),
    val pageNumber: Int = 1,
    val imageUri: String? = null,
    val imagePath: String? = null,
    val textItems: List<TextItem> = emptyList()
)

data class Project(
    val name: String = "پروژه مانگا ۱",
    val pages: List<Page> = listOf(Page(pageNumber = 1))
)
