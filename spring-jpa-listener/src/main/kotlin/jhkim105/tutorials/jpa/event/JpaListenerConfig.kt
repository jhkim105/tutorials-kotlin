package jhkim105.tutorials.jpa.event

import jakarta.persistence.EntityManagerFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn

@Configuration
@DependsOn("entityManagerFactory")
class JpaListenerConfig(
    private val entityManagerFactory: EntityManagerFactory,
    private val stockEventListener: StockEventListener,
    private val stockCommitEventListener: StockCommitEventListener
) : InitializingBean {

    override fun afterPropertiesSet() {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor::class.java)
        val registry = sessionFactory.serviceRegistry.getService(EventListenerRegistry::class.java)!!
        registry.appendListeners(EventType.POST_INSERT, stockEventListener)
        registry.appendListeners(EventType.PRE_UPDATE, stockEventListener)
        registry.appendListeners(EventType.POST_UPDATE, stockEventListener)
        registry.appendListeners(EventType.POST_COMMIT_UPDATE, stockCommitEventListener)
    }
}