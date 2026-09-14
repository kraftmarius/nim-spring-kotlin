package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.GameRules
import com.nim.game.domain.model.Move

/**
 * Mathematical solver for the subtraction game variant of Nim.
 * Computes optimal moves using modulo arithmetic against P-positions.
 */
class OptimalStrategy : AiStrategy {
    override fun determineMove(game: Game): Move {
        check(!game.isOver()) { "Cannot determine move for a finished game." }
        check(!game.heapState.isEmpty()) { "Cannot determine move when heaps are empty." }

        check(game.heapState.heaps.size == 1) {
            "OptimalStrategy requires single-heap; multi-heap must be rejected upstream"
        }

        val heapSize = game.heapState.heaps[0]
        val modulus = game.rules.maxTake + 1
        val minAllowed = GameRules.MIN_TAKE
        val maxTake = minOf(game.rules.maxTake, heapSize)

        val targetRemainder =
            when (game.rules.mode) {
                GameMode.MISERE -> 1
                GameMode.NORMAL -> 0
            }

        val remainder = (heapSize - targetRemainder).mod(modulus)

        val matchesToTake =
            if (remainder in minAllowed..maxTake) {
                remainder
            } else {
                // Already in a losing P-position; execute a defensive fallback move.
                minAllowed
            }

        return Move(
            player = game.currentTurn,
            heapIndex = 0,
            matches = matchesToTake,
        )
    }
}
