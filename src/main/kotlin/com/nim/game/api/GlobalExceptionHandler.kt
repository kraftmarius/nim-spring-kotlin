package com.nim.game.api

import com.nim.game.application.GameNotFoundException
import com.nim.game.application.InvalidMoveException
import com.nim.game.domain.strategy.UnsupportedStrategyException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(GameNotFoundException::class)
    fun handleNotFound(ex: GameNotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Game not found")

    @ExceptionHandler(InvalidMoveException::class)
    fun handleInvalidMove(ex: InvalidMoveException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.error.message)

    @ExceptionHandler(UnsupportedStrategyException::class)
    fun handleUnsupportedStrategy(ex: UnsupportedStrategyException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_IMPLEMENTED, ex.error.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ProblemDetail {
        val detail = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Invalid request parameters.")
}
