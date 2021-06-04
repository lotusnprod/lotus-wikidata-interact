/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload

import org.springframework.boot.runApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<LotusImporter>(*args)
}
