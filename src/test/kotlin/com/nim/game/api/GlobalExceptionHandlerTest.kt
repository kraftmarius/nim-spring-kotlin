package com.nim.game.api

import com.nim.game.application.GameService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(GameController::class)
class GlobalExceptionHandlerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gameService: GameService

    @Test
    fun `PUT on games endpoint returns 405 with problem detail`() {
        mockMvc
            .perform(put("/api/v1/games").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isMethodNotAllowed)
            .andExpect(jsonPath("$.detail").isNotEmpty)
    }

    @Test
    fun `POST games with malformed JSON returns 400 with problem detail`() {
        mockMvc
            .perform(
                post("/api/v1/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json"),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value("Malformed or unreadable JSON request payload."))
    }
}
