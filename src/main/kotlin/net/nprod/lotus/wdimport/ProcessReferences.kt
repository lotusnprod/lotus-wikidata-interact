package net.nprod.lotus.wdimport

import net.nprod.lotus.input.DataTotal
import net.nprod.lotus.wdimport.wd.InstanceItems
import net.nprod.lotus.wdimport.wd.WDFinder
import net.nprod.lotus.wdimport.wd.interfaces.Publisher
import net.nprod.lotus.wdimport.wd.models.WDArticle

fun processReferences(
    dataTotal: DataTotal,
    wdFinder: WDFinder,
    instanceItems: InstanceItems,
    publisher: Publisher
) = dataTotal.referenceCache.store.map {
    val article = WDArticle(
        name = it.value.title ?: it.value.doi,
        title = it.value.title,
        doi = it.value.doi.toUpperCase(), // DOIs are always uppercase
    ).tryToFind(wdFinder, instanceItems)
    // TODO: Add PMID and PMCID
    publisher.publish(article, "upserting article")
    it.value to article
}.toMap()