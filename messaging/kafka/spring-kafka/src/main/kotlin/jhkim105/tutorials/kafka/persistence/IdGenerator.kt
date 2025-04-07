package jhkim105.tutorials.kafka.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import java.util.*

private val FACTORY = Tsid.FactorySupplier.INSTANCE.get()

object IdGenerator {
  fun tsid(): String {
    return FACTORY.generate().toString()
  }

  fun uuid(): String {
    return UUID.randomUUID().toString()
  }
}