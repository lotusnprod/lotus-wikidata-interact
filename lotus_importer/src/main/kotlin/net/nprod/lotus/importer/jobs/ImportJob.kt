/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.jobs

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.UnexpectedInputException
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.net.MalformedURLException


data class Transaction(
    val database: String? = null
)

class RecordFieldSetMapper : FieldSetMapper<Transaction?> {
    override fun mapFieldSet(fieldSet: FieldSet): Transaction {
        return Transaction(database = fieldSet.readString("database"))
    }
}

class CustomItemProcessor : ItemProcessor<Transaction, Transaction> {
    override fun process(item: Transaction): Transaction = item
}

@Configuration
class ImportJob(val jobs: JobBuilderFactory, val steps: StepBuilderFactory) {
    @Value("file:data/buggy.tsv")
    lateinit var inputFile: Resource

    @Value("file:data/outtest.tsv")
    lateinit var outputFile: Resource

    @Bean
    fun itemReader(): ItemReader<Transaction> {
        val reader: FlatFileItemReader<Transaction> = FlatFileItemReader<Transaction>()
        val tokenizer = DelimitedLineTokenizer("\t")
        val tokens = arrayOf("database")
        tokenizer.setNames(*tokens)
        tokenizer.setStrict(false)
        reader.setResource(inputFile)
        val lineMapper: DefaultLineMapper<Transaction> = DefaultLineMapper<Transaction>()
        lineMapper.setLineTokenizer(tokenizer)
        lineMapper.setFieldSetMapper(RecordFieldSetMapper())
        reader.setLineMapper(lineMapper)
        return reader
    }

    @Bean
    fun itemProcessor(): ItemProcessor<Transaction, Transaction> {
        return CustomItemProcessor()
    }

    @Bean
    @Throws(MalformedURLException::class)
    fun itemWriter(): ItemWriter<Transaction> {
        val itemWriter = MyWriter()

        return itemWriter
    }

    protected fun step1(): Step {
        return steps["step1"].chunk<Transaction, Transaction>(10)
            .reader(itemReader()).processor(itemProcessor()).writer(itemWriter()).build()
    }

    @Bean(name = ["newJob"])
    fun newJob(): Job {
        return jobs["firstBatchJob"].start(step1()).build()
    }
}

class MyWriter : ItemWriter<Transaction> {
    override fun write(items: MutableList<out Transaction>) {
        println("Trying to write $items")
    }
}
