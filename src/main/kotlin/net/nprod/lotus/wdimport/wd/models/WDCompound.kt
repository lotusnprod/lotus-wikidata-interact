package net.nprod.lotus.wdimport.wd.models

import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.sparql.ISparql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf
import org.wikidata.wdtk.datamodel.implementation.ItemIdValueImpl
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue

data class WDCompound(
    override var name: String="",
    val inChIKey: String?,
    val inChI: String?,
    val isomericSMILES: String?,
    val pcId: String? = null,
    val chemicalFormula: String?,
    val iupac: String?,
    val undefinedStereocenters: Int,
    val f: WDCompound.() -> Unit = {}
) : Publishable() {
    override var type = if (undefinedStereocenters==0) InstanceItems::chemicalCompound else InstanceItems::groupOfIsomers
    private val logger: Logger = LogManager.getLogger(this::class.qualifiedName)

    init {
        apply(f)
    }

    override fun dataStatements() =
        listOfNotNull(
            inChIKey?.let { ReferencableValueStatement(InstanceItems::inChIKey, it) },
            inChI?.let { ReferencableValueStatement(InstanceItems::inChI, it) },
            isomericSMILES?.let { ReferencableValueStatement(InstanceItems::isomericSMILES, it) },
            chemicalFormula?.let { ReferencableValueStatement(InstanceItems::chemicalFormula, it, overwriteable = true) },
            //iupac?.let { ReferencableValueStatement(InstanceItems::iupac, it )},  // For this we need to check the labels first…
            pcId?.let { ReferencableValueStatement(InstanceItems::pcId, it) }
        )

    override fun tryToFind(wdFinder: WDFinder, instanceItems: InstanceItems): WDCompound {
        // In the case of the test instance, we do not have the ability to do SPARQL queries

        val query = """
            PREFIX wd: <${InstanceItems::wdURI.get(instanceItems)}>
            PREFIX wdt: <${InstanceItems::wdtURI.get(instanceItems)}>
            SELECT DISTINCT ?id {
              ?id wdt:${wdFinder.sparql.resolve(InstanceItems::inChIKey).id} ${Rdf.literalOf(inChIKey).queryString}.
            }
            """.trimIndent()

        val results = wdFinder.sparql.query(query) { result ->
            result.map { bindingSet ->
                bindingSet.getValue("id").stringValue().replace(instanceItems.wdURI, "")
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

    fun naturalProductOfTaxon(wdTaxon: WDTaxon, f: ReferencableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        val refStatement = ReferencableValueStatement(InstanceItems::naturalProductOfTaxon, wdTaxon.id).apply(f)
        preStatements.add(refStatement)
    }

    fun foundInTaxon(wdTaxon: WDTaxon, f: ReferencableValueStatement.() -> Unit) {
        require(wdTaxon.published) { "Can only add an already published taxon." }
        val refStatement = ReferencableValueStatement(InstanceItems::foundInTaxon, wdTaxon.id, overwriteable = true).apply(f)
        preStatements.add(refStatement)
    }
}