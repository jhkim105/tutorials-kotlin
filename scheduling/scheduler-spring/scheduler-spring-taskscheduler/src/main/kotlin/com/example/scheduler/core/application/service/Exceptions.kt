package com.example.scheduler.core.application.service

class NotFoundException(message: String) : RuntimeException(message)
class ConflictException(message: String) : RuntimeException(message)
class ValidationException(message: String) : RuntimeException(message)
