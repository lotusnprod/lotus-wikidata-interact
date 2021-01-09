/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.controllers

import kotlinx.serialization.Serializable
import net.nprod.lotus.importer.jobs.ImportJob
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody


@Serializable
data class JobData(
    val name: String,
    val createTime: String,
    val endTime: String,
    val isRunning: Boolean,
    val status: String
) {
    companion object {
        fun fromJobExecution(execution: JobExecution): JobData = JobData(
            execution.jobId.toString(),
            execution.createTime.toString(),
            execution.endTime?.toString() ?: "",
            execution.isRunning,
            execution.status.toString()
        )
    }
}


@Controller
class JobController(
    val importJob: ImportJob, val applicationContext: ApplicationContext,
    @Qualifier("asyncJobLauncher") val jobLauncher: JobLauncher
) {
    private var lastJob: JobExecution? = null

    @GetMapping("/jobs/import/request", produces = ["application/json"])
    @ResponseBody
    fun newRunjob(): JobData? {
        if (lastJob == null || lastJob?.status in arrayOf(
                BatchStatus.COMPLETED,
                BatchStatus.FAILED,
                BatchStatus.STOPPED
            )
        ) {
            try {
                lastJob = jobLauncher.run(
                    importJob.newJob(),
                    JobParametersBuilder().addLong("unique", System.nanoTime()).toJobParameters()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }
        return lastJob?.let { JobData.fromJobExecution(it) }
    }
}