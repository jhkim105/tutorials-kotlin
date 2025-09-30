package jhkim105.flexmark.research_content

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jhkim105.flexmark.research_content.BlockTypes.CALL_OUT
import jhkim105.flexmark.research_content.BlockTypes.DROPDOWN
import jhkim105.flexmark.research_content.BlockTypes.IMAGE
import jhkim105.flexmark.research_content.BlockTypes.INTRO
import jhkim105.flexmark.research_content.BlockTypes.MARGIN
import jhkim105.flexmark.research_content.BlockTypes.PARAGRAPH
import jhkim105.flexmark.research_content.BlockTypes.TITLE
import jhkim105.flexmark.research_content.BlockTypes.UNORDERED_LIST
import jhkim105.flexmark.research_content.InlineTypes.CONTENTS_LINK
import jhkim105.flexmark.research_content.InlineTypes.EXTERNAL_LINK
import jhkim105.flexmark.research_content.InlineTypes.NEWS_LINK
import jhkim105.flexmark.research_content.InlineTypes.SCHEME_LINK
import jhkim105.flexmark.research_content.InlineTypes.STOCK_LINK
import jhkim105.flexmark.research_content.InlineTypes.TEXT

typealias ResearchContentBody = List<Block>

/**
 * Block
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(IntroBlock::class, name = INTRO),
    JsonSubTypes.Type(ParagraphBlock::class, name = PARAGRAPH),
    JsonSubTypes.Type(TitleBlock::class, name = TITLE),
    JsonSubTypes.Type(CallOutBlock::class, name = CALL_OUT),
    JsonSubTypes.Type(DropdownBlock::class, name = DROPDOWN),
    JsonSubTypes.Type(UnorderedListBlock::class, name = UNORDERED_LIST),
    JsonSubTypes.Type(MarginBlock::class, name = MARGIN),
    JsonSubTypes.Type(ImagesBlock::class, name = IMAGE)
)
sealed interface Block {
    val id: String
    val type: String
}

object BlockTypes {
    const val INTRO = "INTRO"
    const val PARAGRAPH = "PARAGRAPH"
    const val TITLE = "TITLE"
    const val CALL_OUT = "CALL_OUT"
    const val DROPDOWN = "DROPDOWN"
    const val UNORDERED_LIST = "UNORDERED_LIST"
    const val MARGIN = "MARGIN"
    const val IMAGE = "IMAGES"
}

/**
 * Test styling for inline text
 */
data class TextStyle(
    val weight: String? = null,
    val color: String? = null,
    val highlight: String? = null
)

/**
 * Inline
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = TextInline::class
)
@JsonSubTypes(
    JsonSubTypes.Type(TextInline::class, name = TEXT),
    JsonSubTypes.Type(StockLink::class, name = STOCK_LINK),
    JsonSubTypes.Type(NewsLink::class, name = NEWS_LINK),
    JsonSubTypes.Type(ContentsLink::class, name = CONTENTS_LINK),
    JsonSubTypes.Type(SchemeLink::class, name = SCHEME_LINK),
    JsonSubTypes.Type(ExternalLink::class, name = EXTERNAL_LINK)
)
sealed interface Inline {
    val type: String
}

object InlineTypes {
    const val TEXT = "TEXT"
    const val STOCK_LINK = "STOCK_LINK"
    const val NEWS_LINK = "NEWS_LINK"
    const val CONTENTS_LINK = "CONTENTS_LINK"
    const val SCHEME_LINK = "SCHEME_LINK"
    const val EXTERNAL_LINK = "EXTERNAL_LINK"
}

data class TextInline(
    @JsonProperty("type")
    override val type: String = "TEXT",
    val text: String = "",
    val weight: String? = null,
    val color: String? = null,
    val highlight: String? = null
) : Inline

sealed interface LinkInline : Inline {
    val id: String
    val children: List<Inline> // 링크 안에 표시될 텍스트 등
}

data class StockLink(
    override val id: String,
    val stockId: String,
    val isinCode: String,
    val exchangeId: String,
    override val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "STOCK_LINK"
) : LinkInline

data class NewsLink(
    override val id: String,
    val newsId: String,
    override val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "NEWS_LINK"
) : LinkInline

data class ContentsLink(
    override val id: String,
    val url: String,
    override val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "CONTENTS_LINK"
) : LinkInline

data class SchemeLink(
    override val id: String,
    val openScheme: String,
    override val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "SCHEME_LINK"
) : LinkInline

data class ExternalLink(
    override val id: String,
    val url: String,
    val navigationType: String? = null, // "default" 등
    override val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "EXTERNAL_LINK"
) : LinkInline

data class IntroBlock(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "INTRO"
) : Block

data class ParagraphBlock(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "PARAGRAPH"
) : Block

data class TitleBlock(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "TITLE"
) : Block

data class CallOutBlock(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "CALL_OUT"
) : Block

data class DropdownBlock(
    override val id: String,
    val children: List<DropdownChild> = emptyList(),
    @JsonProperty("type")
    override val type: String = "DROPDOWN"
) : Block

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(DropdownSummary::class, name = "SUMMARY"),
    JsonSubTypes.Type(DropdownDescriptions::class, name = "DESCRIPTIONS")
)
sealed interface DropdownChild {
    val id: String
    val type: String
}

data class DropdownSummary(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "SUMMARY"
) : DropdownChild

data class DropdownDescriptions(
    override val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    override val type: String = "DESCRIPTIONS"
) : DropdownChild

data class UnorderedListBlock(
    override val id: String,
    val children: List<UnorderedListItem> = emptyList(),
    val caption: String? = null,
    @JsonProperty("type")
    override val type: String = "UNORDERED_LIST"
) : Block

data class UnorderedListItem(
    val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    val type: String = "LIST_ITEM"
)

data class MarginBlock(
    override val id: String,
    val height: String,
    @JsonProperty("type")
    override val type: String = "MARGIN"
) : Block

data class ImagesBlock(
    override val id: String,
    val images: List<ImageItem> = emptyList(),
    @JsonProperty("type")
    override val type: String = "IMAGES"
) : Block

data class ImageItem(
    val id: String,
    @JsonProperty("imageUrl")
    val imageUrl: String,
    val copyright: Copyright? = null
)

data class Copyright(
    val description: String? = null,
    val holder: String? = null
)







