// SPDX-License-Identifier: AGPL-3.0-or-later
/**
 * Copyright (c) 2020 Jonathan Bisson
 */

package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

data class WDCompound(
    override var name: String = "",
    val inChIKey: String?,
    val inChI: String?,
    val isomericSMILES: String?,
    val canonicalSMILES: String?,
    val pcId: String? = null,
    val chemicalFormula: String?,
    val iupac: String?,
    val undefinedStereocenters: Int,
    val f: WDCompound.() -> Unit = {}
) : Publishable() {
    override var type =
        if (undefinedStereocenters == 0) InstanceItems::chemicalCompound else InstanceItems::groupOfStereoIsomers
    private val logger: Logger = LogManager.getLogger(WDCompound::class.qualifiedName)

    init {
        apply(f)
    }

    override fun dataStatements() =
        listOfNotNull(
            inChIKey?.let { ReferencableValueStatement(InstanceItems::inChIKey, it) },
            inChI?.let { ReferencableValueStatement(InstanceItems::inChI, it) },
            isomericSMILES?.let { ReferencableValueStatement(InstanceItems::isomericSMILES, it) },
            canonicalSMILES?.let { ReferencableValueStatement(InstanceItems::canonicalSMILES, it) },
            chemicalFormula?.let {
                ReferencableValueStatement(
                    InstanceItems::chemicalFormula,
                    it
                )
            },
            //iupac?.let { ReferencableValueStatement(InstanceItems::iupac, it )},  // For this we need to check the labels first…
            pcId?.let { ReferencableValueStatement(InstanceItems::pcId, it) }
        )

    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems) =
        tryToFind(wdFinder, instanceItems, mapOf())

    fun tryToFind(
        wdFinder: WDFinder,
        instanceItems: InstanceItems,
        cache: Map<InChIKey, String> = mapOf()
    ): WDCompound {
        // In the case of the test instance, we do not have the ability to do SPARQL queries
        val results = if (cache.containsKey(inChIKey)) {
            listOf(cache[inChIKey])
        } else {
            val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${wdFinder.sparql.resolve(InstanceItems::inChIKey).id} ${Rdf.literalOf(inChIKey).queryString}.
            }
            """.trimIndent()

            wdFinder.sparql.query(query) { result ->
                result.map { bindingSet ->
                    bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
                }
            }
        }

        if (results.isNotEmpty()) {
            this.published(
                ItemIdValueImpl.fromId(
                    results.first(),
                    InstanceItems::wdURI.get(instanceItems)
                ) as ItemIdValue
            )
        } else {
            logger.info("This is a new compound!")
        }

        return this
    }

    fun foundInTaxon(wdTaxon: WDTaxon, f: ReferencableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add for an already published taxon." }
        val refStatement =
            ReferencableValueStatement(InstanceItems::foundInTaxon, wdTaxon.id, overwritable = true).apply(f) // We make it overwritable, meaning we can add duplicates if needed
        preStatements.add(refStatement)
    }
}