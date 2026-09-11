package com.nim.game.domain.model

/**
 * Sealed hierarchy of all permissible domain errors.
 */
sealed interface DomainError {
    val message: String
}

sealed interface InvalidMoveError : DomainError {
    data class NotPlayersTurn(
        val expected: Player,
        val actual: Player,
    ) : InvalidMoveError {
        override val message: String = "It is player $expected's turn, but $actual attempted to move."
    }

    data class InvalidHeapIndex(
        val index: Int,
        val totalHeaps: Int,
    ) : InvalidMoveError {
        override val message: String = "Heap index $index is out of bounds (total heaps: $totalHeaps)."
    }

    data class HeapAlreadyEmpty(
        val index: Int,
    ) : InvalidMoveError {
        override val message: String = "Heap at index $index is already empty."
    }

    data class TakeOutOfAllowedRange(
        val requested: Int,
        val minAllowed: Int,
        val maxAllowed: Int,
    ) : InvalidMoveError {
        override val message: String = "Requested $requested matches, but allowed range is [$minAllowed, $maxAllowed]."
    }

    data object GameAlreadyFinished : InvalidMoveError {
        override val message: String = "Cannot make a move on a finished game."
    }
}

sealed interface ConfigurationError : DomainError {
    data class MultiHeapAiNotSupported(
        val strategy: Difficulty,
        val heapCount: Int,
    ) : ConfigurationError {
        override val message: String = "Strategy $strategy is not implemented for $heapCount heaps."
    }
}
