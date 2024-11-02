package jhkim105.tutorials.domain

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>?, String?> {

    private val delimiter = ","

    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        return attribute?.joinToString(delimiter)
    }

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        return dbData?.split(delimiter)
    }
}