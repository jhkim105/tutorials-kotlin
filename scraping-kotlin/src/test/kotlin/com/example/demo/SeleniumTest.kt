package com.example.demo

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

internal class SeleniumTest {

    @Test
    fun test() {
        crawl()
    }

    fun crawl() {
        val webDriverId = "webdriver.chrome.driver"
        val webDriverPath = "/Users/jihwankim/dev/tool/chromedriver"
        System.setProperty(webDriverId, webDriverPath)

        val options = ChromeOptions()
        options.addArguments("--start-maximized")
        options.addArguments("--disable-popup-blocking")
        options.addArguments("--disable-default-apps")

        val driver = ChromeDriver(options)

        try {
            driver.get("https://finance.naver.com")
            val doc =
                driver.findElement(By.xpath("/html/body/div[3]/div[3]/div[2]/div[1]/div[2]/div[1]/div[2]/div[1]/div[1]/a/span"))
            print(doc.text)
        } finally {
            driver.close()
        }

    }
}