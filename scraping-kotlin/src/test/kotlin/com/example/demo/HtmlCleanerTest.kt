package com.example.demo

import org.htmlcleaner.HtmlCleaner
import org.junit.jupiter.api.Test
import java.net.URL

class HtmlCleanerTest {

    @Test
    fun test() {
        val cleaner = HtmlCleaner()
        val tagNode = cleaner.clean(URL("https://finance.naver.com/"))
        val obj = tagNode.evaluateXPath("/html/body/div[3]/div[3]/div[2]/div[1]/div[2]/div[1]/div[2]/div[1]/div[1]/a/span")
        println(obj.size) // 0. XPath not work.

        val tagList = tagNode.getElementListByAttValue("class", "num_quot dn", true, true)
        val node = tagList[0]
        val numNodeList = node.getElementListByAttValue("class", "num", true, true)
        println(numNodeList[0].text)
    }
}