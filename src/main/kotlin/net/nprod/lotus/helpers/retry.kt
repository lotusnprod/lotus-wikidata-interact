package net.nprod.lotus.helpers

import java.lang.RuntimeException

inline fun <reified T : Throwable> tryCount(maxRetries: Int = 3, delay: Long = 0, f: () -> Unit) {
    var retries = 0
    while (retries < maxRetries) {
        try {
            f()
            break
        } catch (e: Exception) {
            if (e is T) {
                retries += 1
                if (retries == maxRetries) throw e
                if (delay > 0) Thread.sleep(delay * 1000L)
                continue
            }
            throw e
        }
    }
}