/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.helpers

import net.nprod.lotus.wdimport.wd.publishing.Milliseconds
import kotlin.reflect.KClass

/**
 * This is used to retry a block of code a number of times if it files with the given exceptions.
 * Once it reaches the maximum of retries, it will throw the last exception received.
 */
@Suppress("TooGenericExceptionThrown", "TooGenericExceptionCaught") // On purpose we catch and throw it back
inline fun <U> tryCount(
    listExceptions: List<KClass<out Exception>>,
    maxRetries: Int = 3,
    delayMilliSeconds: Milliseconds = 0,
    f: () -> U
): U {
    var retries = 0

    while ((retries < maxRetries)) {
        try {
            return f()
        } catch (e: Exception) {
            retries += 1
            if (listExceptions.any { e::class == it }) {

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
