/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.commons

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

suspend fun <T, R> List<T>.mapAsync(transform: suspend (T) -> R): List<R> =
    coroutineScope {
        map { async { transform(it) } }.awaitAll()
    }
