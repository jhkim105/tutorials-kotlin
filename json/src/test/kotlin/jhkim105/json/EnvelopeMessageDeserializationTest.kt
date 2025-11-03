package jhkim105.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.OffsetDateTime

class EnvelopeMessageDeserializationTest : StringSpec({

    val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)

    "should deserialize STOCK_TARGET_PRICE payload correctly" {
        // given
        val json = """
            {
              "event_type": "STOCK_TARGET_PRICE",
              "version": "v1",
              "event_time": "2025-10-30T13:19:29+09:00",
              "payload": {
                "symbol": "TSLA",
                "target_price": "400.00",
                "institution": "JP Morgan",
                "issued_at": "2025-10-30T13:19:29+09:00"
              }
            }
        """.trimIndent()

        // when
        val message: EnvelopeMessage = mapper.readValue(json)

        // then
        message.version shouldBe "v1"
        message.payload shouldBe StockTargetPricePayload(
            symbol = "TSLA",
            targetPrice = "400.00",
            institution = "JP Morgan",
            issuedAt = OffsetDateTime.parse("2025-10-30T13:19:29+09:00")
        )
    }
})