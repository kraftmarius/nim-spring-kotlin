package com.nim.game.application

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameId
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe in-memory repository for storing active and finished game sessions.
 */
@Repository
class GameRepository {
    private val storage = ConcurrentHashMap<GameId, Game>()

    fun save(game: Game): Game {
        storage[game.id] = game

        return game
    }

    fun findById(id: GameId): Game? = storage[id]

    /** Number of stored games. */
    val size: Int
        get() = storage.size
}
