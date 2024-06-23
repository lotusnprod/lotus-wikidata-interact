/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.upload.jobs

import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import net.nprod.lotus.helpers.GZIPReader
import java.io.BufferedReader
import java.io.File

fun tryGzipThenNormal(fileName: String): BufferedReader =
    try {
        GZIPReader(fileName).bufferedReader
    } catch (e: java.util.zip.ZipException) {
        e.message
        File(fileName).bufferedReader()
    }

class UnivocityBasedReader<T>(
    private val f: (com.univocity.parsers.common.record.Record) -> T,
) {
    private var csvParser: CsvParser? = null

    /**
     * Number of records to skip
     */
    var skip: Int? = null

    /**
     * Maximal number of records to read
     */
    var maximalNumber: Long? = null
    private var bufferedReader: BufferedReader? = null

    fun open(fileName: String) {
        val settingsParser = CsvParserSettings()
        settingsParser.format.setLineSeparator("\n")
        settingsParser.format.setDelimiter("\t")
        settingsParser.isAutoClosingEnabled = false
        maximalNumber?.let { settingsParser.numberOfRecordsToRead = it + (skip ?: 0) }
        settingsParser.isHeaderExtractionEnabled = true

        bufferedReader = tryGzipThenNormal(fileName)
        csvParser =
            bufferedReader?.let { reader ->
                CsvParser(settingsParser).also { parser ->
                    parser.beginParsing(reader)
                    skip?.let { repeat(it) { parser.parseNextRecord() } }
                }
            }
    }

    fun close() {
        csvParser?.stopParsing()
        bufferedReader?.close()
    }

    fun read(): List<T> {
        val list = mutableListOf<T>()
        while (csvParser?.context?.isStopped == false) { // we have a false for null matching
            val record: com.univocity.parsers.common.record.Record = csvParser?.parseNextRecord() ?: break
            list.add(f(record))
        }
        return list
    }
}
