package com.example.demo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class IntegrationTests(@Autowired val restTemplate: TestRestTemplate, @Autowired val mockMvc: MockMvc) {

  @BeforeAll
  fun setup() {
    println(">> Setup")
  }

  @Test
  fun `Assert blog page title, content and status code`() {
    println(">> Assert blog page title, content and status code")
    val entity = restTemplate.getForEntity<String>("/hello")
    assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(entity.body).contains("hello kotlin")
  }

  @Test
  fun testByMockMvc() {
    mockMvc.perform(get("/hello").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
  }

  @AfterAll
  fun teardown() {
    println(">> Tear down")
  }

}