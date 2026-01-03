package com.example.scheduler.application.port

interface HttpClientPort {
    fun get(url: String): Int
}
