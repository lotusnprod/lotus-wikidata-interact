/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

@file:Suppress("unused")

package net.nprod.lotus.importer

import com.univocity.parsers.common.record.Record
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import java.io.Reader

/**
 * Get a list of records from the given Reader
 * Returns the full list if lines is null
 */

fun parseTSVFile(
    file: Reader,
    lines: Int? = null,
    skip: Int = 0,
): List<Record>? {
    val settingsParser = TsvParserSettings()
    settingsParser.format.setLineSeparator("\n")
    settingsParser.isHeaderExtractionEnabled = true
    val tsvParser = TsvParser(settingsParser)

    if (lines == null) return tsvParser.parseAllRecords(file)

    tsvParser.beginParsing(file)
    var count = 0
    val list = mutableListOf<Record>()

    repeat(skip) { tsvParser.parseNextRecord() }
    while (true) {
        val record = tsvParser.parseNextRecord()
        // Reached the amount of lines needed or the parser stopped or we reached the end of the file
        if ((count >= lines) || tsvParser.context.isStopped || record == null) break
        list.add(record)
        count++
    }

    tsvParser.stopParsing()
    return list
}
