package com.nim.game.application

import com.nim.game.domain.model.GameId
import com.nim.game.domain.model.InvalidMoveError

class GameNotFoundException(
    val id: GameId,
) : RuntimeException("Game with id '${id.value}' was not found.")

class InvalidMoveException(
    val error: InvalidMoveError,
) : RuntimeException(error.message)
