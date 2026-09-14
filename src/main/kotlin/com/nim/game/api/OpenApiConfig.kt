package com.nim.game.api

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun nimOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Nim Game API")
                    .description("REST API for the subtraction game Nim featuring stochastic and mathematically optimal computer agents.")
                    .version("1.0.0"),
            )
}
