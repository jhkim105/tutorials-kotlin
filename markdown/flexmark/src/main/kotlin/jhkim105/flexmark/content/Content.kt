package jhkim105.flexmark.content

// 최상위 문서
data class ContentDocument(
    val body: List<Block>
)

// ===== 블록 =====
sealed interface Block { val type: String }

data class TitleBlock(
    val level: Int,
    val children: List<Inline>
) : Block { override val type = "TITLE" }

data class ParagraphBlock(
    val children: List<Inline>
) : Block { override val type = "PARAGRAPH" }

data class CaptionBlock(
    val children: List<Inline>
) : Block { override val type = "CAPTION" }

data class MarginBlock(
    val height: String // e.g. "40"
) : Block { override val type = "MARGIN" }

data class ImageBlock(
    val images: List<ImageItem>
) : Block { override val type = "IMAGE" }

data class ImageItem(
    val image_url: String,
    val copyright: Copyright? = null
)

data class Copyright(
    val description: String? = null, // 이미지 title
    val holder: String? = null       // alt 텍스트
)

// ===== 인라인 =====
sealed interface Inline { val kind: String }

data class TextRun(
    val text: String
) : Inline { override val kind = "TEXT" }

data class StrongInline(
    val children: List<Inline>
) : Inline { override val kind = "STRONG" }

data class EmphasisInline(
    val children: List<Inline>
) : Inline { override val kind = "EMPHASIS" }

data class CodeInline(
    val code: String
) : Inline { override val kind = "CODE" }

data class LinkInline(
    val destination: String,
    val title: String? = null,
    val children: List<Inline> = emptyList()
) : Inline { override val kind = "LINK" }

data class LineBreakInline(
    val hard: Boolean // true: <br> (Hard), false: soft wrap
) : Inline { override val kind = "BR" }
