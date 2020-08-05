package net.nprod.onpdb.wdimport.wd.models

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql

// TODO: Identifiers

data class WDTaxon(
    override var name: String,
    val parentTaxon: ItemIdValue?,
    val taxonName: String?,
    val taxonRank: RemoteItem
) : Publishable() {
    override var type = InstanceItems::taxon

    override fun dataStatements() =
        listOfNotNull(
            parentTaxon?.let { ReferenceableValueStatement(InstanceItems::parentTaxon, it) },
            taxonName?.let { ReferenceableValueStatement(InstanceItems::taxonName, it) },
            ReferenceableRemoteItemStatement(InstanceItems::taxonRank, taxonRank)
        )

    override fun tryToFind(wdSparql: WDSparql, instanceItems: InstanceItems): Publishable {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wdt: <http://www.wikidata.org/prop/direct/>
            SELECT DISTINCT ?id {
              ?id wdt:${wdSparql.resolve(InstanceItems::instanceOf).id} wd:${wdSparql.resolve(InstanceItems::taxon).id};
                  wdt:${wdSparql.resolve(InstanceItems::taxonRank).id} wd:${wdSparql.resolve(taxonRank).id};
                  wdt:${wdSparql.resolve(InstanceItems::taxonName).id} ${Rdf.literalOf(name).queryString}.
            }
            """.trimIndent()
        println(query)
        val results = wdSparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }
        if(instanceItems.sparqlEndpoint == null) {
            println("We found $results on the official instance but we can't use them here")
        }
        return this
    }
}