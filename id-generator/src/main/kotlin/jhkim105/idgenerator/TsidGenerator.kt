package jhkim105.idgenerator

import com.github.f4b6a3.tsid.TsidCreator

object TsidGenerator {
  inline fun <reified T> generator(): T {
    val tsid = TsidCreator.getTsid()
    return when (T::class) {
      String::class -> tsid.toString() as T
      Long::class -> tsid.toLong() as T
      else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}. Only String and Long are supported.")
    }
  }
}