package com.example.scheduler.application.action

import com.example.scheduler.application.port.ActionHandler
import com.example.scheduler.application.port.ActionResolver
import com.example.scheduler.domain.action.ActionKey
import org.springframework.stereotype.Component

@Component
class ActionRegistry(handlers: List<ActionHandler>) : ActionResolver {
    private val handlerMap: Map<ActionKey, ActionHandler> = handlers.associateBy { it.key }

    override fun resolve(actionKey: String): ActionHandler {
        val key = runCatching { ActionKey.valueOf(actionKey) }
            .getOrElse { throw IllegalArgumentException("Unsupported actionKey: $actionKey") }

        return handlerMap[key] ?: throw IllegalArgumentException("Unregistered actionKey: $actionKey")
    }
}
