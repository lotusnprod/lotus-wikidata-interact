/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wdimport.wd.models.entries

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.models.statements.ReferencedValueStatement
import net.nprod.lotus.wdimport.wd.publishing.Publishable
import net.nprod.lotus.wdimport.wd.publishing.RemoteItem
import net.nprod.lotus.wdimport.wd.sparql.InChIKey
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

/**
 * Wikidata publishable for a compound (chemical entity or group of isomers)
 *
 * @param inChIKey InChiKey
 * @param inChI InChI
 * @param isomericSMILES isomeric SMILES of the molecule
 * @param canonicalSMILES canonical smiles
 * @param pcId PubChem ID
 * @param chemicalFormula Chemical formula
 * @param mass Mass
 * @param iupac IUPAC name
 * @param undefinedStereocenters number of undefined stereocenters
 * @param f function that will act on that compound to add properties for example
 */
data class WDCompound(
    override var label: String = "",
    val inChIKey: String?,
    val inChI: String?,
    val isomericSMILES: String?,
    val canonicalSMILES: String?,
    val pcId: String? = null,
    val chemicalFormula: String?,
    val mass: String?,
    val iupac: String?,
    val undefinedStereocenters: Int,
    val f: WDCompound.() -> Unit = {}
) : Publishable() {
    override var type: RemoteItem =
        if (undefinedStereocenters == 0) InstanceItems::chemicalCompound else InstanceItems::groupOfStereoIsomers
    private val logger: Logger = LogManager.getLogger(WDCompound::class.qualifiedName)

    init {
        apply(f)
    }

    override fun dataStatements(): List<ReferencedValueStatement> =
        listOfNotNull(
            inChIKey?.let { ReferencedValueStatement(InstanceItems::inChIKey, it) },
            inChI?.let { ReferencedValueStatement(InstanceItems::inChI, it) },
            isomericSMILES?.let { ReferencedValueStatement(InstanceItems::isomericSMILES, it) },
            canonicalSMILES?.let { ReferencedValueStatement(InstanceItems::canonicalSMILES, it) },
            chemicalFormula?.let {
                ReferencedValueStatement(
                    InstanceItems::chemicalFormula,
                    it
                )
            },
            mass?.let {
                ReferencedValueStatement(
                    InstanceItems::mass,
                    it
                )
            },
            // For this we need to check the labels first…
            // iupac?.let { ReferencableValueStatement(InstanceItems::iupac, it )},
            pcId?.let { ReferencedValueStatement(InstanceItems::pcId, it) }
        )

    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDCompound =
        tryToFind(wdFinder, instanceItems, mapOf())

    /**
     * Try to find this entry, but also uses a cache to not hit the sparql all the time
     */
    fun tryToFind(
        wdFinder: WDFinder,
        instanceItems: InstanceItems,
        cache: Map<InChIKey, String> = mapOf()
    ): WDCompound {
        // In the case of the test instance, we do not have the ability to do SPARQL queries
        val results = if (cache.containsKey(inChIKey)) {
            listOf(cache[inChIKey])
        } else {
            val query =
                """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${wdFinder.sparql.resolve(InstanceItems::inChIKey).id} ${Rdf.literalOf(inChIKey).queryString}.
            }
                """.trimIndent()

            wdFinder.sparql.selectQuery(query) { result ->
                result.map { bindingSet ->
                    bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
                }
            }
        }

        if (results.isNotEmpty()) {
            this.publishedAs(
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

    /**
     * Add a `found in taxon` property with the given references
     */
    fun foundInTaxon(wdTaxon: WDTaxon, f: ReferencedValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add for an already published taxon." }
        // We make it overwritable, meaning we can add duplicates if needed
        val refStatement =
            ReferencedValueStatement(InstanceItems::foundInTaxon, wdTaxon.id, overwritable = true).apply(f)
        preStatements.add(refStatement)
    }
}
