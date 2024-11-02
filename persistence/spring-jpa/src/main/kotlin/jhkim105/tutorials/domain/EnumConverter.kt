package jhkim105.tutorials.domain

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
abstract class EnumConverter<T : Enum<T>>(
  private val clazz: Class<T>,
) : AttributeConverter<T, String> {
  override fun convertToDatabaseColumn(enum: T?) = enum?.name
  override fun convertToEntityAttribute(code: String?): T? = code?.let { clazz.enumConstants.first { it.name == code } }
}