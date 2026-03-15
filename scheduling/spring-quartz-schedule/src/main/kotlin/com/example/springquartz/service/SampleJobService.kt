package com.example.springquartz.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SampleJobService {

    private val logger = LoggerFactory.getLogger(SampleJobService::class.java)

    fun executeSampleJob() {
        logger.info("The sample job has begun...")
        try {
            Thread.sleep(5000)
        } catch (e: InterruptedException) {
            logger.error("Error while executing sample job", e)
        } finally {
            logger.info("Sample job has finished...")
        }
    }
}
