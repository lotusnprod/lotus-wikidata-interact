/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.gnfinder

import com.google.protobuf.util.JsonFormat
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import protob.GNFinderGrpcKt
import protob.Gnfinder
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * Default timeout for requests
 */
const val GNFINDER_DEFAULT_TIMEOUT: Long = 5

@Serializable
data class GNFinderResult(
    val dataSourceId: Int = -1,
    val dataSourceTitle: String = "",
    val taxonId: String = "",
    val matchedCanonicalFull: String = "",
    val matchedName: String = "",
    val matchedCardinality: Int? = null,
    val matchedCanonicalSimple: String? = null,
    val classificationPath: String? = null,
    val classificationRank: String? = null,
    val classificationIds: String? = null,
    val matchType: String? = null,
)

@Serializable
data class GNFinderVerification(
    val bestResult: GNFinderResult? = null,
    val preferredResults: List<GNFinderResult>? = null,
)

@Serializable
data class GNFinderNames(
    val name: String,
    val cardinality: Int? = null,
    val verbatim: String? = null,
    val odds: Double? = null,
    val offsetStart: Int? = null,
    val offsetEnd: Int? = null,
    val annotationNomenType: String? = null,
    val annotation: String? = null,
    val verification: GNFinderVerification? = null,
)

@Serializable
data class GNFinderResponse(
    val names: List<GNFinderNames>? = listOf(),
)

fun voidRequest(): Gnfinder.Void = Gnfinder.Void.newBuilder().build()

/**
 * Connect to a local GNFinder instance accessible by gRPC
 */
class GNFinderClient(
    target: String,
    dispatcher: ExecutorCoroutineDispatcher,
) : Closeable {
    private val channel =
        ManagedChannelBuilder
            .forTarget(target)
            .usePlaintext()
            .executor(dispatcher.asExecutor())
            .build()
    private val stub: GNFinderGrpcKt.GNFinderCoroutineStub =
        GNFinderGrpcKt.GNFinderCoroutineStub(channel)

    fun ping(): String =
        runBlocking {
            stub.ping(voidRequest()).value
        }

    fun ver(): String =
        runBlocking {
            stub.ver(voidRequest()).version
        }

    fun findNames(
        query: String,
        language: String = "english",
        sources: Iterable<Int> = listOf(),
        verification: Boolean = false,
    ): String {
        val message =
            runBlocking {
                val params =
                    Gnfinder.Params
                        .newBuilder()
                        .apply {
                            this.language = language
                            this.text = query
                            this.verification = verification
                            this.addAllSources(sources)
                        }.build()
                stub.findNames(params)
            }
        val printer = JsonFormat.printer()
        return printer.print(message)
    }

    fun findNamesToStructured(
        query: String,
        language: String = "english",
        sources: Iterable<Int> = listOf(),
        verification: Boolean = false,
    ): GNFinderResponse {
        val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        return json.decodeFromString(
            GNFinderResponse.serializer(),
            findNames(query, language, sources, verification),
        )
    }

    override fun close() {
        channel.shutdown().awaitTermination(GNFINDER_DEFAULT_TIMEOUT, TimeUnit.SECONDS)
    }
}
