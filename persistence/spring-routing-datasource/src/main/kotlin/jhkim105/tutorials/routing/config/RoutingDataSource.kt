package jhkim105.tutorials.routing.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

enum class DataSourceType {
    MASTER,
}

class SlaveLoadBalancer(private val slaveKeys: List<String>) {
    private val counter = AtomicInteger(0)

    fun nextSlaveKey(): String {
        if (slaveKeys.isEmpty()) {
            throw IllegalStateException("No slave data sources configured.")
        }
        val index = (counter.getAndIncrement() and Int.MAX_VALUE) % slaveKeys.size
        return slaveKeys[index]
    }
}

class RoutingDataSource(
    private val slaveLoadBalancer: SlaveLoadBalancer,
) : AbstractRoutingDataSource() {

    override fun determineCurrentLookupKey(): Any {
        val isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly()
        return if (isReadOnly) {
            val slaveKey = slaveLoadBalancer.nextSlaveKey()
            logger.info { ">>> [DataSource Routing] Current Transaction is READ-ONLY. Routing to SLAVE DataSource -> [$slaveKey]" }
            slaveKey
        } else {
            logger.info { ">>> [DataSource Routing] Current Transaction is WRITE (readOnly=false). Routing to MASTER DataSource -> [MASTER]" }
            DataSourceType.MASTER
        }
    }
}
