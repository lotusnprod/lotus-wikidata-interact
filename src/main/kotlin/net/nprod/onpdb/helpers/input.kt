package net.nprod.onpdb.helpers

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
 */
fun parseTSVFile(file: Reader): List<Record>? {
    val settingsParser = TsvParserSettings()
    settingsParser.format.setLineSeparator("\n")
    settingsParser.isHeaderExtractionEnabled = true
    val tsvParser = TsvParser(settingsParser)

    return tsvParser.parseAllRecords(file)
}

fun GZIPRead(name: String): BufferedReader {
    return BufferedReader(InputStreamReader(GZIPInputStream(FileInputStream(name))))
}
