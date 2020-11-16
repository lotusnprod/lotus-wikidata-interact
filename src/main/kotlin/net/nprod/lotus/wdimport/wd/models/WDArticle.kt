package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

// TODO: Identifiers

data class WDArticle(
    override var name: String,
    val title: String?,
    val doi: String?
) : Publishable() {
    override var type = InstanceItems::scholarlyArticle

    override fun dataStatements() =
        listOfNotNull(
            title?.let { ReferencableValueStatement.monolingualValue(InstanceItems::title, it) },
            doi?.let { ReferencableValueStatement(InstanceItems::doi, it) })

    /**
     * Try to find an article with that DOI, we always take the smallest ID
     */
    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDArticle {
        require(doi != null) { "The DOI cannot be null" }
        val dois = wdFinder.wdkt.searchDOI(doi).query.search.map { it.title.trimStart('Q').toInt() to it.title }.toMap()
            .toSortedMap().values

        if (dois.isNotEmpty()) {
            this.published(ItemIdValueImpl.fromId(dois.first(), InstanceItems::wdURI.get(instanceItems)) as ItemIdValue)
        }

        return this
    }
}