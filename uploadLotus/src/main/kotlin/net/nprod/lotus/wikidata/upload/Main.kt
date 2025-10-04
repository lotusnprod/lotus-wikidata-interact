/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload

import net.nprod.lotus.wikidata.upload.jobs.LotusProcessRaw
import net.nprod.lotus.wikidata.upload.jobs.LotusRaw
import net.nprod.lotus.wikidata.upload.jobs.UnivocityBasedReader
import net.nprod.lotus.wikidata.upload.jobs.WikiDataWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.time.ExperimentalTime

/**
 * Main entry point for the uploadLotus application.
 *
 * Reads a TSV file, processes the data, and writes the results to Wikidata.
 *
 * @param args Command-line arguments. The first argument should be the path to the TSV file.
 */
@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger("MainThread")
    logger.info("Hello!")
    val tsvReader =
        UnivocityBasedReader<LotusRaw> {
            LotusRaw.fromRecord(it)
        }

    val fileName = args[0]
    val filePath = fileName
    val processor = LotusProcessRaw()
    val wdWriter = WikiDataWriter()

    // Configure the reader to not skip any records
    tsvReader.skip = 0
    tsvReader.open(filePath)
    val list = tsvReader.read()
    logger.info("Processing ${list.size} entries.")
    val processed = processor.process(list)

    // Write the processed data to Wikidata
    wdWriter.write(listOf(processed))

    logger.info("Done!")

    tsvReader.close()
}
