package net.nprod.lotus.helpers

import java.lang.RuntimeException

inline fun <reified T : Throwable, U> tryCount(maxRetries: Int = 3, delaySeconds: Long = 0, f: () -> U): U {
    var retries = 0
    while (retries < maxRetries) {
        try {
            return f()
        } catch (e: Exception) {
            if (e is T) {
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