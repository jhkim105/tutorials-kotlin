package jhkim105.tutorials

import com.querydsl.codegen.ClassPathUtils
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class JpaConfig: InitializingBean {

    @Bean
    fun jpaQueryFactory(entityManager: EntityManager): JPAQueryFactory = JPAQueryFactory(entityManager)

    override fun afterPropertiesSet() {
        ClassPathUtils.scanPackage(Thread.currentThread().contextClassLoader, "jhkim105.tutorials.domain")
    }
}