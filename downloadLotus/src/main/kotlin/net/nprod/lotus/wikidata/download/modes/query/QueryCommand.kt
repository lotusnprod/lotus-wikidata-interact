/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.download.modes.query

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import net.nprod.lotus.wikidata.download.DEFAULT_REPOSITORY
import java.io.File
import java.io.FileNotFoundException

class QueryCommand : CliktCommand(name = "query") {
    // Overridden help attribute
    override fun help(context: Context) = "Run a SPARQL SELECT query on the local instance or directly on WD"

    // Parameters
    private val store by option("-s", "--store", help = "Where the triplestore is")
        .default(DEFAULT_REPOSITORY)
    private val outputFilename by option("-o", "--output", help = "Output file")
    private val direct by option(
        "-d",
        "--direct",
        help = "Connect directly to WikiData, do not use the local instance",
    ).flag("-l", "--local", default = false, defaultForHelp = "Use the local instance")
    private val queryFilename by argument(help = "File with the SPARQL query")

    // Execution logic
    override fun run() {
        val storeFile = File(store)

        val queryFile = File(queryFilename)
        val outputFile = outputFilename?.let { File(it) }

        if (!storeFile.isDirectory && !direct) {
            throw FileNotFoundException("Impossible to open the repository, did you run mirror?")
        }

        if (!queryFile.exists()) {
            throw FileNotFoundException("Impossible to open the query!")
        }

        query(storeFile, queryFile, outputFile, direct = direct)
    }
}
