/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.oldprocessor

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.required

class Parameters {
    private val parser = ArgParser("lotus_importer")

    fun parse(args: Array<String>) {
        parser.parse(args)
    }

    val input by parser.option(ArgType.String, shortName = "i", description = "Input file").required()

    val limit by parser.option(
        ArgType.Int,
        shortName = "l",
        description = "Limit the import to this number of entries (-1 for all, default 1)"
    ).default(1)
    val skip by parser.option(
        ArgType.Int,
        shortName = "s",
        description = "Skip this number of entries"
    ).default(0)
    val real by parser.option(
        ArgType.Boolean,
        shortName = "r",
        description = "Turn on real mode: this will write to WikiData!"
    ).default(false)
    val validation by parser.option(
        ArgType.Boolean,
        shortName = "v",
        description = "Turn on validation mode: this will do everything in memory to check the dataset"
    ).default(false)
    val persistent by parser.option(
        ArgType.Boolean,
        shortName = "p",
        description = "Turn on persistent mode (only for tests)"
    ).default(false)
    val realSparql by parser.option(
        ArgType.Boolean,
        shortName = "S",
        description = "Use the real WikiData instance for SPARQL queries (only for tests)"
    ).default(false)
    val output by parser.option(
        ArgType.String,
        shortName = "o",
        description = "Repository output file name (only for test persistent mode)"
    ).default("")
    val repositoryInputFilename by parser.option(
        ArgType.String,
        shortName = "u",
        description = "Repository input file name (data will be loaded from here)"
    ).default("")
}
