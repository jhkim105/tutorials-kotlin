package com.example.demo

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.junit.jupiter.api.Test


class JsoupTest {


    @Test
    fun test() {
        val doc: Document = Jsoup.connect("https://finance.naver.com/").get()
        val elements = doc.getElementsByAttributeValue("class", "num_quot dn")
        val targetElements = elements[0].getElementsByAttributeValue("class", "num")
        println(targetElements[0].text())
    }
}