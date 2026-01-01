package com.example.scheduler.config

import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SpringBeanJobFactory

@Configuration
class QuartzConfig(private val beanFactory: AutowireCapableBeanFactory) {

    private fun jobFactory(): SpringBeanJobFactory {
        return object : SpringBeanJobFactory() {
            override fun createJobInstance(bundle: org.quartz.spi.TriggerFiredBundle): Any {
                val job = super.createJobInstance(bundle)
                beanFactory.autowireBean(job)
                return job
            }
        }
    }

    @Bean
    fun schedulerFactoryBeanCustomizer(): SchedulerFactoryBeanCustomizer {
        return SchedulerFactoryBeanCustomizer { factory ->
            factory.setJobFactory(jobFactory())
        }
    }
}
