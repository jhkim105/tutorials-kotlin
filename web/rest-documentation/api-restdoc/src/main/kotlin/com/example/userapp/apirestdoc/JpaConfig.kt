package com.example.userapp.apirestdoc

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan("com.example.userapp.core.infra.persistence.jpa")
@EnableJpaRepositories("com.example.userapp.core.infra.persistence.jpa")
class JpaConfig {
}