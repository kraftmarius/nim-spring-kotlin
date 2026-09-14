package com.nim.game.api

import com.nim.game.api.CreateGameRequest
import com.nim.game.api.MakeMoveRequest
import com.nim.game.application.GameNotFoundException
import com.nim.game.application.GameService
import com.nim.game.application.InvalidMoveException
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameId
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.InvalidMoveError
import com.nim.game.domain.model.Move
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@WebMvcTest(GameController::class)
class GameControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var gameService: GameService

    @Test
    fun `POST games returns 201 with location and state`() {
        val gameId = GameId.random()
        val game = Game(id = gameId, heapState = HeapState.single(13), currentTurn = Player.HUMAN)
        `when`(gameService.createGame(CreateGameRequest())).thenReturn(game)

        mockMvc
            .perform(
                post("/api/v1/games")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isCreated)
            .andExpect(header().string("Location", "/api/v1/games/${gameId.value}"))
            .andExpect(jsonPath("$.id").value(gameId.value.toString()))
            .andExpect(jsonPath("$.heaps[0]").value(13))
            .andExpect(jsonPath("$.currentTurn").value("HUMAN"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
    }

    @Test
    fun `POST games returns 400 when game rules are contradictory`() {
        val request = CreateGameRequest(maxTake = 1)
        `when`(gameService.createGame(request)).thenThrow(
            IllegalArgumentException("maxTake (1) must be greater than 1"),
        )

        mockMvc
            .perform(
                post("/api/v1/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value("maxTake (1) must be greater than 1"))
    }

    @Test
    fun `GET games by id returns 200 on existing game`() {
        val gameId = GameId.random()
        val game = Game(id = gameId, heapState = HeapState.single(7), currentTurn = Player.HUMAN)
        `when`(gameService.getGame(gameId)).thenReturn(game)

        mockMvc
            .perform(get("/api/v1/games/${gameId.value}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(gameId.value.toString()))
            .andExpect(jsonPath("$.heaps[0]").value(7))
    }

    @Test
    fun `GET games by id returns 404 when game not found`() {
        val gameId = GameId.random()
        `when`(gameService.getGame(gameId)).thenThrow(GameNotFoundException(gameId))

        mockMvc
            .perform(get("/api/v1/games/${gameId.value}"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Game with id '${gameId.value}' was not found."))
    }

    @Test
    fun `POST moves returns 200 with updated state`() {
        val gameId = GameId.random()
        val updatedGame =
            Game(
                id = gameId,
                heapState = HeapState.single(9),
                currentTurn = Player.HUMAN,
                history =
                    listOf(
                        Move(Player.HUMAN, 0, 2),
                        Move(Player.COMPUTER, 0, 2),
                    ),
            )
        `when`(gameService.makeMove(gameId, 0, 2)).thenReturn(updatedGame)

        val request = MakeMoveRequest(matches = 2, heapIndex = 0)

        mockMvc
            .perform(
                post("/api/v1/games/${gameId.value}/moves")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.heaps[0]").value(9))
            .andExpect(jsonPath("$.lastComputerMove.matches").value(2))
            .andExpect(jsonPath("$.history.length()").value(2))
    }

    @Test
    fun `POST moves returns 400 when move is illegal`() {
        val gameId = GameId.random()
        val error = InvalidMoveError.TakeOutOfAllowedRange(requested = 5, minAllowed = 1, maxAllowed = 3)
        `when`(gameService.makeMove(gameId, 0, 5)).thenThrow(InvalidMoveException(error))

        val request = MakeMoveRequest(matches = 5, heapIndex = 0)

        mockMvc
            .perform(
                post("/api/v1/games/${gameId.value}/moves")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.detail").value(error.message))
    }
}
