/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.helpers

import net.nprod.lotus.wdimport.wd.publishing.Milliseconds
import org.apache.logging.log4j.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

/**
 * This is used to retry a block of code a number of times if it files with the given exceptions.
 * Once it reaches the maximum of retries, it will throw the last exception received.
 */
@Suppress(
    "TooGenericExceptionThrown",
    "TooGenericExceptionCaught",
    "NestedBlockDepth",
    "LongParameterList"
) // On purpose we catch and throw it back
inline fun <U> tryCount(
    listExceptions: List<KClass<out Exception>> = listOf(),
    listNamedExceptions: List<String> = listOf(),
    maxRetries: Int = 3,
    delayMilliSeconds: Milliseconds = 0,
    logger: Logger? = null,
    f: () -> U
): U {
    var retries = 0

    while ((retries < maxRetries)) {
        try {
            return f()
        } catch (e: Exception) {
            retries += 1
            if (listExceptions.any { kclass ->
                (e::class == kclass) ||
                    (e::class.supertypes.contains(kclass.starProjectedType))
            } || listNamedExceptions.any { e::class.qualifiedName == it }
            ) {
                logger?.error("Retrying ($retries/$maxRetries): ${e.message}")
                if (retries != maxRetries) {
                    if (delayMilliSeconds > 0) Thread.sleep(delayMilliSeconds)
                    continue
                }
            }
            throw e
        }
    }

    throw RuntimeException("This is not accessible")
}
