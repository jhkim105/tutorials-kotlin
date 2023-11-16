package jhkim105.tutorials.enum_mapping

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class EnumMappingControllerTests(@Autowired val mockMvc: MockMvc) {

    @Test
    fun test() {
        mockMvc.perform(MockMvcRequestBuilders.get("/enum-mapping")
            .param("idp", "google")
            .accept(MediaType.APPLICATION_JSON))
            .andDo { println() }
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("GOOGLE"))
    }


}