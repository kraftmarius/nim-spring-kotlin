package com.nim.game

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class NimApplication

fun main(args: Array<String>) {
    runApplication<NimApplication>(*args)
}
