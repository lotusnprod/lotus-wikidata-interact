package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

// TODO: Identifiers

data class WDArticle(
    override var name: String,
    val title: String?,
    val doi: String?
): Publishable() {
    override var type = InstanceItems::scholarlyArticle

    override fun dataStatements() =
        listOfNotNull(
            title?.let { ReferencableValueStatement.monolingualValue(InstanceItems::title, it) },
            doi?.let { ReferencableValueStatement(InstanceItems::doi, it) })


    override fun tryToFind(iSparql: ISparql, instanceItems: InstanceItems): WDArticle {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${iSparql.resolve(InstanceItems::instanceOf).id} wd:${iSparql.resolve(InstanceItems::scholarlyArticle).id};
                  wdt:${iSparql.resolve(InstanceItems::doi).id} ${Rdf.literalOf(doi).queryString}.
            }
            """.trimIndent()
        println(query)
        val results = iSparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
            }
        }

        if (results.isNotEmpty()) {
            this.published(ItemIdValueImpl.fromId(results.first(), InstanceItems::wdURI.get(instanceItems)) as ItemIdValue)
        }

        return this
    }
}