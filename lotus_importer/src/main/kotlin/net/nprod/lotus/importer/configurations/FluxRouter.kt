/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.configurations

import net.nprod.lotus.importer.fluxhandlers.GreetingHandler
import net.nprod.lotus.importer.fluxhandlers.LogHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class FluxRouter {
    @Bean
    fun route(greetingHandler: GreetingHandler, logHandler: LogHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
            greetingHandler::hello
        ).andRoute(
            RequestPredicates.GET("/log").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
            logHandler::log
        )
    }
}