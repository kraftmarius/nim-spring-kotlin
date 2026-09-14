package com.nim.game.api

import com.nim.game.application.GameRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HealthController::class)
class HealthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var gameRepository: GameRepository

    @Test
    fun `GET health returns 200 with repository game metrics`() {
        `when`(gameRepository.activeGames()).thenReturn(2)
        `when`(gameRepository.totalGames()).thenReturn(5)

        mockMvc
            .perform(get("/api/v1/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.timestamp").isNotEmpty)
            .andExpect(jsonPath("$.activeGames").value(2))
            .andExpect(jsonPath("$.totalGames").value(5))
    }
}
