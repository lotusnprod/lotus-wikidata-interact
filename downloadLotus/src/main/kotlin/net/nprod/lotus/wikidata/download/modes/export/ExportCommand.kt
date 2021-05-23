/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.download.modes.export

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import net.nprod.lotus.wikidata.download.DEFAULT_REPOSITORY
import java.io.File

class ExportCommand : CliktCommand(help = "Export LOTUS to… something") {
    private val store by option("-s", "--store", help = "Where the data is going to be stored")
        .default(DEFAULT_REPOSITORY)
    private val outputDirectory by option("-o", "--output", help = "Output directory").required()
    private val direct by option(
        "-d",
        "--direct",
        help = "Connect directly to WikiData, do not use the local instance"
    ).flag("-l", "--local", default = false, defaultForHelp = "Use the local instance")

    override fun run() {
        val storeFile = File(store)
        val outputDirectory = outputDirectory.let {
            File(it).also {
                it.mkdirs()
            }
        }
        runBlocking {
            export(storeFile, outputDirectory, direct).join()
        }
    }
}
