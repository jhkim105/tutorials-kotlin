package com.example.demo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class CrawlerTest @Autowired constructor(val crawler: Crawler) {

    @Test
    fun test() {
        crawler.crawl()
    }
}