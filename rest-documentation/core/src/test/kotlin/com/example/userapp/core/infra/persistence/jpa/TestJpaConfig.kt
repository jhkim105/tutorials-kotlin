package com.example.userapp.core.infra.persistence.jpa

import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan("com.example.userapp.core.infra.persistence.jpa")
@EnableJpaRepositories("com.example.userapp.core.infra.persistence.jpa")
@ComponentScan("com.example.userapp.core.infra.persistence.jpa")
class TestJpaConfig