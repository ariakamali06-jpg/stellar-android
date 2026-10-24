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
    var text: String = "متن جدید",
    var x: Float = 100f,
    var y: Float = 100f,
    var width: Float = 220f,
    var height: Float = 120f,
    var fontSize: Float = 22f,
    var fontColor: Long = 0xFF000000,       // ARGB Black
    var bubbleColor: Long = 0xFFFFFFFF,     // ARGB White
    var strokeColor: Long = 0xFF000000,     // ARGB Black
    var strokeWidth: Float = 3f,
    var hasBubble: Boolean = true,
    var bubbleShape: BubbleShape = BubbleShape.ELLIPSE,
    var hasShadow: Boolean = false,
    var shadowColor: Long = 0x66000000,
    var shadowOffsetX: Float = 6f,
    var shadowOffsetY: Float = 6f,
    var hasExtrusion: Boolean = false,
    var extrusionDepth: Float = 5f,
    var isBold: Boolean = true,
    var isItalic: Boolean = false,
    var alignment: AlignmentMode = AlignmentMode.CENTER,
    var warpEffect: WarpEffect = WarpEffect.NONE,
    var rotation: Float = 0f
)

data class Page(
    val id: String = UUID.randomUUID().toString(),
    var pageNumber: Int = 1,
    var imageUri: String? = null,
    val textItems: MutableList<TextItem> = mutableListOf()
)

data class Project(
    var name: String = "پروژه مانگا ۱",
    val pages: MutableList<Page> = mutableListOf(Page(pageNumber = 1))
)
