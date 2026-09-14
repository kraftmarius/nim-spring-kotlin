package com.nim.game.domain.strategy

import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.GameRules
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.NimDefaults
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.Random

class OptimalStrategyTest {
    private val strategy = OptimalStrategy()

    @ParameterizedTest(name = "Misere: Heap {0} -> take {1} to force P-position")
    @CsvSource(
        "2, 1", // Leaves 1
        "3, 2", // Leaves 1
        "4, 3", // Leaves 1
        "6, 1", // Leaves 5
        "7, 2", // Leaves 5
        "8, 3", // Leaves 5
    )
    fun `misere mode forces opponent onto p-positions`(
        heapSize: Int,
        expectedTake: Int,
    ) {
        val game = createSingleHeapGame(heapSize, GameMode.MISERE)

        val move = strategy.determineMove(game)

        assertEquals(Player.COMPUTER, move.player)
        assertEquals(0, move.heapIndex)
        assertEquals(expectedTake, move.matches)
    }

    @Test
    fun `misere mode with single match is forced to take 1`() {
        val game = createSingleHeapGame(1, GameMode.MISERE)

        val move = strategy.determineMove(game)

        assertEquals(1, move.matches)
    }

    @ParameterizedTest(name = "Misere: On losing P-position heap {0} -> produces valid random move")
    @CsvSource("5", "9", "13")
    fun `misere mode executes random fallback when already in losing position`(heapSize: Int) {
        val seededStrategy = OptimalStrategy(Random(42))
        val game = createSingleHeapGame(heapSize, GameMode.MISERE)

        val move = seededStrategy.determineMove(game)

        assertTrue(move.matches in GameRules.MIN_TAKE..game.rules.maxTake)
    }

    @ParameterizedTest(name = "Normal: Heap {0} -> take {1} to force P-position")
    @CsvSource(
        "1, 1", // Leaves 0 (wins immediately)
        "2, 2", // Leaves 0 (wins immediately)
        "3, 3", // Leaves 0 (wins immediately)
        "5, 1", // Leaves 4
        "6, 2", // Leaves 4
        "7, 3", // Leaves 4
    )
    fun `normal mode forces opponent onto p-positions`(
        heapSize: Int,
        expectedTake: Int,
    ) {
        val game = createSingleHeapGame(heapSize, GameMode.NORMAL)

        val move = strategy.determineMove(game)

        assertEquals(expectedTake, move.matches)
    }

    @ParameterizedTest(name = "Normal: On losing P-position heap {0} -> produces valid random move")
    @CsvSource("4", "8", "12")
    fun `normal mode executes random fallback when already in losing position`(heapSize: Int) {
        val seededStrategy = OptimalStrategy(Random(42))
        val game = createSingleHeapGame(heapSize, GameMode.NORMAL)

        val move = seededStrategy.determineMove(game)

        assertTrue(move.matches in GameRules.MIN_TAKE..game.rules.maxTake)
    }

    @Test
    fun `should throw IllegalStateException on multi-heap game`() {
        val multiHeapGame =
            Game(
                heapState = HeapState(listOf(3, 4)),
                currentTurn = Player.COMPUTER,
            )

        val exception =
            assertThrows<IllegalStateException> {
                strategy.determineMove(multiHeapGame)
            }

        assertTrue(exception.message!!.contains("single-heap"))
    }

    @Test
    fun `solves optimal move for custom maxTake of 4`() {
        // Modulus = 5. For Misere, losing position is n % 5 == 1 (e.g. 6).
        // At heap = 8: (8 - 1) % 5 = 2 -> AI takes 2 to leave 6.
        val game = createSingleHeapGame(matches = 8, mode = GameMode.MISERE, maxTake = 4)

        val move = strategy.determineMove(game)

        assertEquals(2, move.matches)
    }

    private fun createSingleHeapGame(
        matches: Int,
        mode: GameMode,
        maxTake: Int = NimDefaults.DEFAULT_MAX_TAKE,
    ): Game =
        Game(
            heapState = HeapState.single(matches),
            rules = GameRules(maxTake = maxTake, mode = mode),
            currentTurn = Player.COMPUTER,
        )
}
