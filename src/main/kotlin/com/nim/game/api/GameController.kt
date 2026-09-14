package com.nim.game.api

import com.nim.game.application.GameService
import com.nim.game.domain.model.GameId
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.UUID

@RestController
@RequestMapping("/api/v1/games")
class GameController(
    private val gameService: GameService,
) {
    @PostMapping
    fun createGame(
        @RequestBody(required = false) request: CreateGameRequest?,
    ): ResponseEntity<GameResponse> {
        val game = gameService.createGame(request ?: CreateGameRequest())
        val location = URI.create("/api/v1/games/${game.id.value}")

        return ResponseEntity.created(location).body(GameResponse.from(game))
    }

    @GetMapping
    fun getAllGames(): ResponseEntity<List<GameResponse>> {
        val games = gameService.getAllGames()

        return ResponseEntity.ok(games.map { GameResponse.from(it) })
    }

    @GetMapping("/{id}")
    fun getGame(
        @PathVariable id: UUID,
    ): ResponseEntity<GameResponse> {
        val game = gameService.getGame(GameId(id))

        return ResponseEntity.ok(GameResponse.from(game))
    }

    @PostMapping("/{id}/moves")
    fun makeMove(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MakeMoveRequest,
    ): ResponseEntity<GameResponse> {
        val game = gameService.makeMove(GameId(id), request.heapIndex, request.matches)

        return ResponseEntity.ok(GameResponse.from(game))
    }
}
