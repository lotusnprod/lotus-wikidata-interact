import net.nprod.lotus.wdimport.wd.MainInstanceItems
import net.nprod.lotus.wdimport.wd.TestInstanceItems
import net.nprod.lotus.wdimport.wd.WDPublisher
import net.nprod.lotus.wdimport.wd.sparql.WDSparql

/**
 * This is a really crude and horrible script to create properties on wikidata's test instance
 */
fun main() {

    // Put here the properties you want to create.

    val propertiesToCopy = listOf<String>(
    )

    val publisher = WDPublisher(TestInstanceItems)

    publisher.connect()

    val wdSparql = WDSparql(MainInstanceItems)
    propertiesToCopy.forEach { property ->
        val query = """
        SELECT DISTINCT ?pLabel {
          VALUES ?p {wd:$property}.
          ?p rdfs:label ?label.
          SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }  
        }
    """.trimIndent()
        wdSparql.query(
            query
        ) {
            it.firstOrNull()?.let {bindingSet ->
                val o = bindingSet.getValue("pLabel").stringValue()
                if (o == null) {
                    println("Problem with $property")
                } else {
                    val prop = publisher.newProperty(
                        o,
                        "Some new property for ONPDB"
                    )

                    println("$property => ${prop?.toString()} ${prop?.iri}")
                }
            }
        }
    }
}