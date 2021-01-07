/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.input

/**
 * Interface for objects that have an id
 */
interface Indexable {
    var id: Long?
}
