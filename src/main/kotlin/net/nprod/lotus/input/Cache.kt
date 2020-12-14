package net.nprod.lotus.input

import java.util.concurrent.atomic.AtomicLong

interface Cache<T, U> {
    val store: MutableMap<T, U>
    fun getOrNew(key: T, value: U): U
}

class IndexableCache<T, U : Indexable> : Cache<T, U> {
    override val store: MutableMap<T, U> = mutableMapOf()
    private val treated: MutableMap<T, Boolean> = mutableMapOf()
    private var counter = AtomicLong(0)
    override fun getOrNew(key: T, value: U): U {
        return store[key] ?: run {
            val count = counter.incrementAndGet()
            value.id = count
            store[key] = value
            value
        }
    }

    fun getOrNew(key: T, generator: () -> U): U {
        return getOrNew(key, generator())
    }

    /**
     * Mark that key as treated
     */
    fun markTreated(key: T) {
        treated[key] = true
    }

    /**
     * Is this key treated
     */
    fun isTreated(key: T) = treated.getOrDefault(key, false)
}

interface Indexable {
    var id: Long?
}
