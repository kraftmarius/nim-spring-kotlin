package com.nim.game.domain.strategy

import com.nim.game.domain.model.ConfigurationError
import com.nim.game.domain.model.Difficulty
import com.nim.game.domain.model.Game
import com.nim.game.domain.model.GameMode
import com.nim.game.domain.model.GameRules
import com.nim.game.domain.model.HeapState
import com.nim.game.domain.model.Player
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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

    @ParameterizedTest(name = "Misere: On losing P-position heap {0} -> defensive fallback take 1")
    @CsvSource("1", "5", "9", "13")
    fun `misere mode falls back to minTake when already in losing position`(heapSize: Int) {
        val game = createSingleHeapGame(heapSize, GameMode.MISERE)

        val move = strategy.determineMove(game)

        assertEquals(1, move.matches)
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

    @ParameterizedTest(name = "Normal: On losing P-position heap {0} -> defensive fallback take 1")
    @CsvSource("4", "8", "12")
    fun `normal mode falls back to minTake when already in losing position`(heapSize: Int) {
        val game = createSingleHeapGame(heapSize, GameMode.NORMAL)

        val move = strategy.determineMove(game)

        assertEquals(1, move.matches)
    }

    @Test
    fun `should throw UnsupportedStrategyException on multi-heap game`() {
        val multiHeapGame =
            Game(
                heapState = HeapState(listOf(3, 4)),
                currentTurn = Player.COMPUTER,
            )

        val exception =
            assertThrows<UnsupportedStrategyException> {
                strategy.determineMove(multiHeapGame)
            }

        val error = assertInstanceOf(ConfigurationError.MultiHeapAiNotSupported::class.java, exception.error)
        assertEquals(Difficulty.NIGHTMARE, error.strategy)
        assertEquals(2, error.heapCount)
    }

    private fun createSingleHeapGame(
        matches: Int,
        mode: GameMode,
    ): Game =
        Game(
            heapState = HeapState.single(matches),
            rules = GameRules(minTake = 1, maxTake = 3, mode = mode),
            currentTurn = Player.COMPUTER,
        )
}
