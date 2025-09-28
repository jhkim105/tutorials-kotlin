package jhkim105.json.content_model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// -------------------------------
// Top-level
// -------------------------------

typealias Document = List<Block>

/**
 * 공통: 모든 블록 노드는 id를 가집니다.
 * 다형성 구분은 JSON의 "type" 필드를 그대로 사용합니다.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(IntroBlock::class,        name = "INTRO"),
    JsonSubTypes.Type(ParagraphBlock::class,    name = "PARAGRAPH"),
    JsonSubTypes.Type(TitleBlock::class,        name = "TITLE"),
    JsonSubTypes.Type(CallOutBlock::class,      name = "CALL_OUT"),
    JsonSubTypes.Type(DropdownBlock::class,     name = "DROPDOWN"),
    JsonSubTypes.Type(UnorderedListBlock::class,name = "UNORDERED_LIST"),
    JsonSubTypes.Type(MarginBlock::class,       name = "MARGIN"),
    JsonSubTypes.Type(ImagesBlock::class,       name = "IMAGES")
)
sealed interface Block {
    val id: String
    val type: String
}

// -------------------------------
// Text styling for inline text
// -------------------------------

/**
 * 텍스트 런의 공통 스타일 필드
 * - weight: "bold" 등
 * - color: "red500" 등
 * - highlight: "highlight-yellow" 등
 */
data class TextStyle(
    val weight: String? = null,
    val color: String? = null,
    val highlight: String? = null
)

// -------------------------------
// Inline (문단 내부 요소)
// -------------------------------

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = TextRun::class // 기본은 텍스트 런(plain text)
)
@JsonSubTypes(
    JsonSubTypes.Type(TextRun::class,        name = "TEXT"),
    JsonSubTypes.Type(StockLink::class,      name = "STOCK_LINK"),
    JsonSubTypes.Type(NewsLink::class,       name = "NEWS_LINK"),
    JsonSubTypes.Type(ContentsLink::class,   name = "CONTENTS_LINK"),
    JsonSubTypes.Type(SchemeLink::class,     name = "SCHEME_LINK"),
    JsonSubTypes.Type(ExternalLink::class,   name = "EXTERNAL_LINK")
)
sealed interface Inline {
    val type: String
}

/** 기본 텍스트 노드: 입력 JSON에는 "type"이 없으므로 TEXT로 가정합니다. */
data class TextRun(
    @JsonProperty("type")
    override val type: String = "TEXT",
    val text: String = "",
    val weight: String? = null,
    val color: String? = null,
    val highlight: String? = null
) : Inline

/** 링크 공통 부모(필요시 재사용) */
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

// -------------------------------
// Blocks
// -------------------------------

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

/**
 * 제목 블록: 예시 JSON처럼 텍스트 런에 굵기 정보가 들어오더라도
 * 렌더링 단계에서 무시(또는 스타일 정책)할 수 있도록 그대로 둡니다.
 */
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

/**
 * 드롭다운 블록: SUMMARY / DESCRIPTIONS 두 하위 블록을 포함
 */
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
    JsonSubTypes.Type(DropdownSummary::class,     name = "SUMMARY"),
    JsonSubTypes.Type(DropdownDescriptions::class,name = "DESCRIPTIONS")
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

/**
 * 불릿 리스트 블록
 */
data class UnorderedListBlock(
    override val id: String,
    val children: List<ListItem> = emptyList(),
    val caption: String? = null,
    @JsonProperty("type")
    override val type: String = "UNORDERED_LIST"
) : Block

data class ListItem(
    val id: String,
    val children: List<Inline> = emptyList(),
    @JsonProperty("type")
    val type: String = "LIST_ITEM"
) {
    // LIST_ITEM은 Block 트리 내부 전용이라 Block으로 승격하지 않았습니다.
}

/**
 * 마진 블록 — 예시 JSON에서 height가 문자열("40") 형태라 String으로 매핑
 */
data class MarginBlock(
    override val id: String,
    val height: String,
    @JsonProperty("type")
    override val type: String = "MARGIN"
) : Block

/**
 * 이미지 블록
 */
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
