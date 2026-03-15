package jhkim105.idgenerator

import java.util.*

object UuidGenerator {

  fun generate(): String {
    return UUID.randomUUID().toString()
  }
}