package net.nprod.lotus.helpers

import kotlin.reflect.KClass

inline fun <U> tryCount(
    listExceptions: List<KClass<out Exception>>,
    maxRetries: Int = 3,
    delaySeconds: Long = 0,
    f: () -> U
): U {
    var retries = 0
    while (retries < maxRetries) {
        try {
            return f()
        } catch (e: Exception) {
            if (listExceptions.any { it -> e::class == it }) {
                retries += 1
                if (retries == maxRetries) throw e
                if (delaySeconds > 0) Thread.sleep(delaySeconds * 1000L)
                continue
            }
            throw e
        }
    }
    throw RuntimeException("This is not accessible")
}