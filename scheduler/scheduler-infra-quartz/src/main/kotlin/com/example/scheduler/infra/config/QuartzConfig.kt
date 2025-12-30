package com.example.scheduler.infra.config

import org.quartz.spi.JobFactory
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.quartz.SpringBeanJobFactory

@Configuration
class QuartzConfig {
    @Bean
    fun schedulerFactoryBeanCustomizer(jobFactory: JobFactory): SchedulerFactoryBeanCustomizer {
        return SchedulerFactoryBeanCustomizer { it.setJobFactory(jobFactory) }
    }

    @Bean
    fun jobFactory(beanFactory: AutowireCapableBeanFactory): JobFactory {
        return AutowiringSpringBeanJobFactory(beanFactory)
    }
}

private class AutowiringSpringBeanJobFactory(
    private val beanFactory: AutowireCapableBeanFactory
) : SpringBeanJobFactory() {
    override fun createJobInstance(bundle: org.quartz.spi.TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        beanFactory.autowireBean(job)
        return job
    }
}
