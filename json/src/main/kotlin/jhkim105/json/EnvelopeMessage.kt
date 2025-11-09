package jhkim105.json

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.OffsetDateTime

// ---------- DTO 정의 ----------
data class EnvelopeMessage(
    val eventType: EventType,
    val version: String,
    val eventTime: OffsetDateTime,
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "event_type"
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = StockTargetPricePayload::class, name = "STOCK_TARGET_PRICE"),
        JsonSubTypes.Type(value = StockNewsPayload::class, name = "STOCK_NEWS"),
        JsonSubTypes.Type(value = StockDividendPayload::class, name = "STOCK_DIVIDEND")
    )
    val payload: EventPayload
)

enum class EventType {
    STOCK_TARGET_PRICE,
    STOCK_NEWS,
    STOCK_DIVIDEND,
    UNKNOWN
}

sealed interface EventPayload

data class StockTargetPricePayload(
    val symbol: String,
    val targetPrice: String,
    val institution: String,
    val issuedAt: OffsetDateTime
) : EventPayload

data class StockNewsPayload(
    val symbol: String,
    val title: String,
    val content: String
) : EventPayload

data class StockDividendPayload(
    val symbol: String,
    val dividend: Double,
    val recordDate: OffsetDateTime
) : EventPayload
// ---------- DTO 정의 끝 ----------