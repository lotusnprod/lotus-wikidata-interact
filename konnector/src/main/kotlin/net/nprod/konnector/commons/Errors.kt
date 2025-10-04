/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.commons

sealed class KnownError : RuntimeException()

sealed class KnownCriticalError : RuntimeException()

object APIError : KnownError()

object NonExistent : KnownError()

data class BadRequestError(
    val content: String,
) : KnownError()

class DecodingError(
    override val message: String,
) : KnownError()

object TooManyRequests : KnownError()

object TimeoutException : KnownError()

data class UnManagedReturnCode(
    val status: Int,
) : KnownCriticalError()
