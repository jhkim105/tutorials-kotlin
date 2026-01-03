package com.example.scheduler.application.port

interface ActionResolver {
    fun resolve(actionKey: String): ActionHandler
}
