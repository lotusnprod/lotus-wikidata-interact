/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2021
 */

package net.nprod.lotus.wikidata.download.modes.mirror

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import net.nprod.lotus.wikidata.download.DEFAULT_REPOSITORY
import java.io.File

class MirrorCommand : CliktCommand(name = "mirror") {
    // Overridden help attribute
    override fun help(context: Context) = "Mirror Wikidata entries related to LOTUS locally"

    // Parameters
    private val store by option("-s", "--store", help = "Where the data is going to be stored")
        .default(DEFAULT_REPOSITORY)

    // Execution logic
    override fun run() {
        val storeFile = File(store)
        mirror(storeFile)
    }
}
