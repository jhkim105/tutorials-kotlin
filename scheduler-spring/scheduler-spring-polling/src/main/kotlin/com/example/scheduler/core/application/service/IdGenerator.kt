package com.example.scheduler.core.application.service

import java.util.UUID

object IdGenerator {
    fun newId(): String = UUID.randomUUID().toString()
}
