package com.nim.game.api

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `GET openapi json returns 200 with valid specification and endpoints`() {
        mockMvc
            .perform(get("/openapi.json"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.openapi").isNotEmpty)
            .andExpect(jsonPath("$.info.title").value("Nim Game API"))
            .andExpect(jsonPath("$.paths['/api/v1/games']").exists())
            .andExpect(jsonPath("$.paths['/api/v1/health']").exists())
    }

    @Test
    fun `GET docs returns 200 with Scalar interactive reference`() {
        mockMvc
            .perform(get("/docs"))
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
    }
}
