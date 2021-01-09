/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.configurations

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.SimpleJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource
import org.springframework.core.task.SimpleAsyncTaskExecutor

import org.springframework.core.task.TaskExecutor

@Configuration
@EnableBatchProcessing
class JobConfiguration {
    @Value("classpath:org/springframework/batch/core/schema-drop-sqlite.sql")
    lateinit var dropRepositoryTables: Resource

    @Value("classpath:org/springframework/batch/core/schema-sqlite.sql")
    lateinit var dataRepositorySchema: Resource

    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        dataSource.setDriverClassName("org.sqlite.JDBC")
        dataSource.url = "jdbc:sqlite:data/job-repository.sqlite"
        return dataSource
    }

    @Bean
    fun dataSourceInitializer(dataSource: DataSource): DataSourceInitializer {
        val databasePopulator = ResourceDatabasePopulator()
        databasePopulator.addScript(dropRepositoryTables)
        databasePopulator.addScript(dataRepositorySchema)
        databasePopulator.setIgnoreFailedDrops(true)
        val initializer = DataSourceInitializer()
        initializer.setDataSource(dataSource)
        initializer.setDatabasePopulator(databasePopulator)
        return initializer
    }

    private val jobRepository: JobRepository
        get() {
            val factory = JobRepositoryFactoryBean()
            factory.setDataSource(dataSource())
            factory.transactionManager = transactionManager
            factory.afterPropertiesSet()
            return factory.getObject() as JobRepository
        }
    private val transactionManager: PlatformTransactionManager = ResourcelessTransactionManager()

    @Bean
    @Qualifier("asyncExecutor")
    fun taskExecutor(): TaskExecutor {
        val taskExecutor = SimpleAsyncTaskExecutor()
        taskExecutor.isDaemon = true
        taskExecutor.threadPriority = Thread.MIN_PRIORITY
        return taskExecutor
    }

    @Bean(name = ["asyncJobLauncher"])
    fun jobLauncher(@Qualifier("asyncExecutor") taskExecutor: TaskExecutor): JobLauncher {
        val jobLauncher = SimpleJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(taskExecutor)
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }
}