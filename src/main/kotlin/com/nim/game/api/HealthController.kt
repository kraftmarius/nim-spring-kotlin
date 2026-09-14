package com.nim.game.api

import com.nim.game.application.GameRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class HealthResponse(
    val timestamp: Instant = Instant.now(),
    val activeGames: Int,
    val totalGames: Int,
)

@RestController
@RequestMapping("/api/v1/health")
class HealthController(
    private val repository: GameRepository,
) {
    @GetMapping
    fun getHealth(): ResponseEntity<HealthResponse> =
        ResponseEntity.ok(
            HealthResponse(
                activeGames = repository.activeGames(),
                totalGames = repository.totalGames(),
            ),
        )
}
