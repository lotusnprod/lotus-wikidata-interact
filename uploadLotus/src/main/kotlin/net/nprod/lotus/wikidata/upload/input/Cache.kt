/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.input

import java.util.concurrent.atomic.AtomicLong

/**
 * A Generic cache interface with Keys of type T and Values of type U
 */
interface Cache<T, U> {
    /**
     * The store for the cache
     */
    val store: MutableMap<T, U>

    /**
     * Get or add a new entity
     */
    fun getOrNew(
        key: T,
        value: U,
    ): U
}

/**
 * A cache implementation that can count values
 *
 * It is not thread-safe yet!
 */
class IndexableCache<T, U : Indexable> : Cache<T, U> {
    override val store: MutableMap<T, U> = mutableMapOf()

    private var counter = AtomicLong(0)

    override fun getOrNew(
        key: T,
        value: U,
    ): U =
        store[key] ?: run {
            val count = counter.incrementAndGet()
            value.id = count
            store[key] = value
            value
        }

    /**
     * Generate a value using an extension function
     */
    fun getOrNew(
        key: T,
        generator: () -> U,
    ): U = getOrNew(key, generator())
}
