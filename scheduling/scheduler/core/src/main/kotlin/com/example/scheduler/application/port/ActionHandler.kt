package com.example.scheduler.application.port

import com.example.scheduler.domain.model.DeliverySchedule
import com.example.scheduler.domain.action.ActionKey

interface ActionHandler {
    val key: ActionKey

    fun execute(payload: String, schedule: DeliverySchedule)
}
