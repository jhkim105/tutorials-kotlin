package com.example.userapp.apirestdoc.controller

import com.example.userapp.core.application.port.`in`.CreateUserUseCase
import com.example.userapp.core.application.port.`in`.DeleteUserUseCase
import com.example.userapp.core.application.port.`in`.GetUserUseCase
import com.example.userapp.core.domain.User
import com.example.userapp.core.domain.UserId
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors.*
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(UserController::class)
@AutoConfigureRestDocs
class UserControllerRestDocsTest(
    @Autowired val mockMvc: MockMvc
) {

    @MockkBean
    lateinit var createUser: CreateUserUseCase

    @MockkBean
    lateinit var getUser: GetUserUseCase

    @MockkBean
    lateinit var deleteUser: DeleteUserUseCase

    @Test
    fun `create user - restdocs`() {
        val user = User(UserId(1), "Alice", "alice@example.com", Instant.now())
        every { createUser.create(any()) } returns user

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Alice","email":"alice@example.com"}"""
        }
        .andExpect {
            status { isCreated() }
        }
        .andDo {
            handle(document("users-create",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                requestFields(
                    fieldWithPath("name").description("User name"),
                    fieldWithPath("email").description("User email")
                ),
                responseFields(
                    fieldWithPath("id").description("Generated id"),
                    fieldWithPath("name").description("User name"),
                    fieldWithPath("email").description("User email")
                )
            ))
        }
    }

    @Test
    fun `list users - restdocs`() {
        every { getUser.list() } returns listOf(
            User(UserId(1), "Alice", "alice@example.com", Instant.now())
        )
        mockMvc.get("/api/users")
            .andExpect { status { isOk() } }
            .andDo {
                handle(document("users-list",
                    preprocessResponse(prettyPrint())
                ))
            }
    }
}
