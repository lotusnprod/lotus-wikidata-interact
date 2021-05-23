/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.download

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import net.nprod.lotus.wikidata.download.modes.export.ExportCommand
import net.nprod.lotus.wikidata.download.modes.mirror.MirrorCommand
import net.nprod.lotus.wikidata.download.modes.query.QueryCommand

const val DEFAULT_REPOSITORY = "data/local_rdf"

class Exporter : CliktCommand() {
    override fun run() {
        echo("Lotus Exporter")
        this.shortHelp()
    }
}

fun main(args: Array<String>) = Exporter()
    .subcommands(MirrorCommand(), QueryCommand(), ExportCommand())
    .main(args)
