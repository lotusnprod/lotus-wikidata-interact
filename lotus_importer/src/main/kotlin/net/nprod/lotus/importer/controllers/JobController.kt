/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.importer.controllers

import kotlinx.serialization.Serializable
import net.nprod.lotus.importer.jobs.LotusImportJob
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.time.ExperimentalTime


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
            execution.endTime?.toString() ?: "", // It can really be null, don't get fooled
            execution.isRunning,
            execution.status.toString()
        )
    }
}


@Controller
@ExperimentalTime
class JobController constructor(
    val lotusImportJob: LotusImportJob,
    @Qualifier("asyncJobLauncher") val jobLauncher: JobLauncher,
    @Qualifier("newJob") val job: Job
) {
    private var lastJob: JobExecution? = null

    @GetMapping("/jobs/import/request", produces = ["application/json"])
    @ResponseBody
    fun newRunjob(
        @RequestParam(name = "max_records") maxRecords: Long? = null,
        @RequestParam(name = "skip") skip: Long? = null
    ): JobData? {
        if (lastJob == null || lastJob?.status in arrayOf(
                BatchStatus.COMPLETED,
                BatchStatus.FAILED,
                BatchStatus.STOPPED
            )
        ) {
            val parameters = JobParametersBuilder().addLong("unique", System.nanoTime())
            maxRecords?.let { parameters.addLong("max_records", it) }
            skip?.let { if (it >= 0) parameters.addLong("skip", it) }
            try {
                lastJob = jobLauncher.run(job, parameters.toJobParameters())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return lastJob?.let { JobData.fromJobExecution(it) }
    }
}
