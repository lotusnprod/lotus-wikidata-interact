// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

@file:Suppress("unused")

package net.nprod.lotus.helpers

import com.univocity.parsers.common.record.Record
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.zip.GZIPInputStream

/**
 * Get a list of records from the given Reader
 * Returns the full list if lines is null
 */

fun parseTSVFile(file: Reader, lines: Int? = null, skip: Int = 0): List<Record>? {

    val settingsParser = TsvParserSettings()
    settingsParser.format.setLineSeparator("\n")
    settingsParser.isHeaderExtractionEnabled = true
    val tsvParser = TsvParser(settingsParser)

    if (lines == null) return tsvParser.parseAllRecords(file)

    tsvParser.beginParsing(file)
    var count = 0
    val list = mutableListOf<Record>()
    var record: Record?
    repeat(skip) { tsvParser.parseNextRecord() }
    while (true) {
        record = tsvParser.parseNextRecord()
        if (count >= lines) break                     // Reached the amount of lines needed
        if (tsvParser.context.isStopped) break      // The parser stopped
        if (record == null) break                   // Reached the end of the file

        list.add(record)
        count++
    }
    tsvParser.stopParsing()
    return list
}

class GZIPReader(name: String) {
    val bufferedReader: BufferedReader
    private val fileInputStream: FileInputStream = FileInputStream(name)
    private val inputStreamReader: InputStreamReader
    private val inputStream: GZIPInputStream = GZIPInputStream(fileInputStream)

    init {
        inputStreamReader = InputStreamReader(inputStream)
        bufferedReader = BufferedReader(inputStreamReader)
    }

    fun close() {
        // Not sure we need to close all of them
        bufferedReader.close()
        inputStreamReader.close()
        inputStream.close()
        fileInputStream.close()
    }
}
