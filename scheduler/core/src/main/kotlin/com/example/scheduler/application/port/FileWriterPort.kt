package com.example.scheduler.application.port

interface FileWriterPort {
    fun writeFile(directory: String, fileName: String, content: String)
}
