/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource


data class Transaction(
    val database: String? = null
)

class RecordFieldSetMapper : FieldSetMapper<Transaction?> {
    override fun mapFieldSet(fieldSet: FieldSet): Transaction {
        return Transaction(database = fieldSet.readString("database"))
    }
}

@Configuration
class ImportJob(val jobs: JobBuilderFactory, val steps: StepBuilderFactory) {
    private var maxCount: Long? = null

    @Value("file:data/buggy.tsv")
    lateinit var inputFile: Resource

    @Bean
    fun itemReader(): FlatFileItemReader<Transaction> = MyReader()

    @Bean
    fun itemProcessor(): ItemProcessor<Transaction, Transaction> = CustomItemProcessor()

    @Bean
    fun itemWriter(): ItemWriter<Transaction> = MyWriter()

    @Bean
    protected fun step1(itemReader: ItemReader<Transaction>): Step {
        return steps["step1"].chunk<Transaction, Transaction>(10)
            .reader(itemReader).processor(itemProcessor()).writer(itemWriter()).build()
    }

    @Bean(name = ["newJob"])
    fun newJob(step1: Step): Job {
        return jobs["importJob"].start(step1).build()
    }
}

class MyReader : FlatFileItemReader<Transaction>() {
    init {
        val tokenizer = DelimitedLineTokenizer("\t")
        val tokens = arrayOf("database")
        tokenizer.setNames(*tokens)
        tokenizer.setStrict(false)
        setResource(FileSystemResource("data/buggy.tsv"))
        val lineMapper: DefaultLineMapper<Transaction> = DefaultLineMapper<Transaction>()
        lineMapper.setLineTokenizer(tokenizer)
        lineMapper.setFieldSetMapper(RecordFieldSetMapper())
        setLineMapper(lineMapper)
        setLinesToSkip(1) // Header
    }

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        stepExecution.jobParameters.getLong("max_records")?.let { setMaxItemCount(it.toInt()) }
        stepExecution.jobParameters.getLong("skip")?.let { setLinesToSkip(it.toInt()+1) }
    }

}

class MyWriter : ItemWriter<Transaction> {
    override fun write(items: MutableList<out Transaction>) {
        println("Trying to write $items")
    }
}

class CustomItemProcessor : ItemProcessor<Transaction, Transaction> {
    private var parameters: JobParameters? = null

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        parameters = stepExecution.jobParameters
    }


    override fun process(item: Transaction): Transaction {
        println("unique is $parameters")
        return item
    }
}
