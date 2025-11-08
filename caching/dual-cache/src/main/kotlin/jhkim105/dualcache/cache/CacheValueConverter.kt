package jhkim105.dualcache.cache

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Component
class CacheValueConverter(
	private val objectMapper: ObjectMapper
) {

	fun serialize(value: Any?): String =
		objectMapper.writeValueAsString(value)

	fun deserialize(payload: String, returnType: Type): Any? {
		if (payload == "null") {
			return null
		}
		val javaType = objectMapper.typeFactory.constructType(returnType)
		return objectMapper.readValue(payload, javaType)
	}
}
