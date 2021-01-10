/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import net.nprod.lotus.importer.oldprocessor.tryGzipThenNormal
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.BufferedReader

@Configuration
class LotusImportJob(val jobs: JobBuilderFactory, val steps: StepBuilderFactory) {
    @Value("file:data/buggy.tsv")
    lateinit var inputFile: Resource

    @Bean
    fun itemReader(): UnivocityBasedReader<LotusRawTSV> = UnivocityBasedReader {
        LotusRawTSV.fromRecord(it)
    }

    @Bean
    fun itemProcessor(): ItemProcessor<LotusRawTSV, LotusRawTSV> = CustomItemProcessor()

    @Bean
    fun itemWriter(): ItemWriter<LotusRawTSV> = MyWriter()

    @Bean
    protected fun step1(itemReader: ItemReader<LotusRawTSV>): Step {
        return steps["step1"].chunk<LotusRawTSV, LotusRawTSV>(10)
            .reader(itemReader).processor(itemProcessor()).writer(itemWriter()).build()
    }

    @Bean(name = ["newJob"])
    fun newJob(step1: Step): Job {
        return jobs["importJob"].start(step1).build()
    }
}

class UnivocityBasedReader<T>(private val f: (com.univocity.parsers.common.record.Record) -> T) :
    ResourceAwareItemReaderItemStream<T>, ItemStreamReader<T> {
    private var tsvParser: TsvParser? = null
    private var skip: Int? = null
    private var maxItemCount: Long? = null
    private var bufferedReader: BufferedReader? = null

    override fun open(executionContext: ExecutionContext) {
        val settingsParser = TsvParserSettings()
        settingsParser.format.setLineSeparator("\n")
        settingsParser.isAutoClosingEnabled = false
        maxItemCount?.let { settingsParser.numberOfRecordsToRead = it }
        settingsParser.isHeaderExtractionEnabled = true
        bufferedReader = tryGzipThenNormal("data/buggy.tsv")
        tsvParser = bufferedReader?.let { reader ->
            TsvParser(settingsParser).also { parser ->
                parser.beginParsing(reader)
                skip?.let { repeat(it) { parser.parseNextRecord() } }
            }
        }
    }

    override fun update(executionContext: ExecutionContext) {
        //TODO("Not yet implemented update $executionContext")
    }

    override fun close() {
        println("Closing!!")
        tsvParser?.stopParsing()
        bufferedReader?.close()
    }

    override fun read(): T? {
        if (tsvParser?.context?.isStopped == true) return null
        val record: com.univocity.parsers.common.record.Record = tsvParser?.parseNextRecord() ?: return null
        return f(record)
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        stepExecution.jobParameters.getLong("max_records")?.let { maxItemCount = it }
        stepExecution.jobParameters.getLong("skip")?.let { skip = it.toInt() }
    }

    override fun setResource(resource: Resource) {
        TODO("Not yet implemented resource $resource")
    }
}

class MyWriter : ItemWriter<LotusRawTSV> {
    override fun write(items: MutableList<out LotusRawTSV>) {
        println("Trying to write $items")
    }
}

class CustomItemProcessor : ItemProcessor<LotusRawTSV, LotusRawTSV> {
    private var parameters: JobParameters? = null

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        parameters = stepExecution.jobParameters
    }


    override fun process(item: LotusRawTSV): LotusRawTSV {
        println("${item.database} ${item.compound.inchiKey} ${item.organism.organismCleaned} ${item.reference.doi}")
        return item
    }
}
