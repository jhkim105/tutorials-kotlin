import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import jhkim105.json.*
import java.time.OffsetDateTime

fun main() {
    val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

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

    try {
        val message: EnvelopeMessage = mapper.readValue(json)
        println("SUCCESS: Deserialization worked!")
        println("Message: $message")
        println("Payload: ${message.payload}")
        println("Payload class: ${message.payload::class}")
        
        val expected = StockTargetPricePayload(
            symbol = "TSLA",
            targetPrice = "400.00",
            institution = "JP Morgan",
            issuedAt = OffsetDateTime.parse("2025-10-30T13:19:29+09:00")
        )
        println("Expected: $expected")
        println("Are they equal? ${message.payload == expected}")
        
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        e.printStackTrace()
    }
}