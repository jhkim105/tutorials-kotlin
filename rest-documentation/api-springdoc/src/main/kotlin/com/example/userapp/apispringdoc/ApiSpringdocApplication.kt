package com.example.userapp.apispringdoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.userapp"])
@EntityScan("com.example.userapp.core.infra.persistence.jpa")
@EnableJpaRepositories("com.example.userapp.core.infra.persistence.jpa")
class ApiSpringdocApplication

fun main(args: Array<String>) {
    runApplication<ApiSpringdocApplication>(*args)
}
