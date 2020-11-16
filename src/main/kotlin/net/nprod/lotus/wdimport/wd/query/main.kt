package net.nprod.lotus.wdimport.wd.query

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.WDPublisher
import org.wikidata.wdtk.datamodel.helpers.Datamodel

//curl "https://www.wikidata.org/w/api.php?action=query&list=search&srsearch=haswbstatement:"P356=10.1007/S11745-003-1193-7"&format=json"
// haswbstatement:"P356=10.1007/S11745-003-1193-7"

@Serializable
data class SearchInfoResponse(
    val totalhits: Int
)

@Serializable
data class SearchResponse(
    val ns: Int,
    val title: String,
    val pageid: Long,
    val size: Int,
    val wordcount: Int,
    val snippet: String,
    val timestamp: String
)

@Serializable
data class QueryResponse(
    val searchinfo: SearchInfoResponse,
    val search: List<SearchResponse>
)

@Serializable
data class QueryActionResponse(
    val batchcomplete: String,
    val query: QueryResponse
)

class WDKT {
    val client: HttpClient

    init {
        client = HttpClient(CIO)
    }

    fun close() = client.close()

    fun searchDOI(doi: String): QueryActionResponse {
        var out: String = runBlocking {
            client.get("https://www.wikidata.org/w/api.php?action=query&list=search&srsearch=haswbstatement:\"P356=$doi\"&format=json")
        }
        return Json.decodeFromString(QueryActionResponse.serializer(), out)
    }
}

fun main() {
    // With the custom searcher
    val wdkt = WDKT()
    println(wdkt.searchDOI("10.1007/S11745-003-1193-7").query.search.map { it.title })
}