/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.lotus.wikidata.download.sparql

object LOTUSQueries {
    val prefixes =
        """
        PREFIX wd: <http://www.wikidata.org/entity/>
        PREFIX wdt: <http://www.wikidata.org/prop/direct/>
        PREFIX wikibase: <http://wikiba.se/ontology#>
        PREFIX p: <http://www.wikidata.org/prop/>
        PREFIX prov: <http://www.w3.org/ns/prov#>
        PREFIX ps: <http://www.wikidata.org/prop/statement/>
        PREFIX pq: <http://www.wikidata.org/prop/qualifier/>
        PREFIX pr: <http://www.wikidata.org/prop/reference/>
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX bd: <http://www.bigdata.com/rdf#> 
        """.trimIndent()

    val queryCompoundsOfInterest =
        """
        $prefixes
        SELECT ?compound_id
        WHERE {
            ?compound_id     wdt:P235 ?inchikey;
                             p:P703 ?pp703.
        }
        """.trimIndent()

    val queryCompoundTaxonRefModularForCompoundIds =
        """
        $prefixes
        CONSTRUCT {
            ?compound_id    wdt:P703 ?taxon_id;
                            p:P703 ?pp703.
            ?pp703          ps:P703 ?taxon_id;
                            prov:wasDerivedFrom ?derived.
            ?derived        pr:P248 ?reference_id.
        }
        WHERE {
            ?compound_id     p:P703 ?pp703.
            ?pp703           ps:P703 ?taxon_id;
                             prov:wasDerivedFrom ?derived.
            ?derived         pr:P248 ?reference_id.
            VALUES ?compound_id { %%IDS%% } 
        }
        """.trimIndent()

    val queryCompoundTaxonRef =
        """
        $prefixes
        CONSTRUCT {
            ?compound_id    wdt:P31 ?type;
                            wdt:P703 ?taxon_id;
                            p:P703 ?pp703.
            ?pp703          ps:P703 ?taxon_id;
                            prov:wasDerivedFrom ?derived.
           
            ?derived        pr:P248 ?reference_id.
        }
        WHERE {
            ?compound_id     wdt:P235 ?inchikey;
                             p:P703 ?pp703;
                             wdt:P31 ?type.

            ?pp703           ps:P703 ?taxon_id;
                             prov:wasDerivedFrom ?derived.
            ?derived         pr:P248 ?reference_id.

            #VALUES ?type { wd:Q113145171 wd:Q59199015 } # type of chemical entity or group of stereoisomers
        }
        """.trimIndent()

    val queryIdsLocal =
        """
        $prefixes
        SELECT ?compound_id ?taxon_id ?reference_id
        WHERE {
            ?compound_id     p:P703 ?pp703.
            ?pp703           ps:P703 ?taxon_id;
                             prov:wasDerivedFrom/pr:P248 ?reference_id.
        }
        """.trimIndent()

    val queryTaxonParents =
        """
        $prefixes
        SELECT ?parenttaxon_id
        WHERE {
            VALUES ?id { %%IDS%% } 
            ?id (p:P171/ps:P171)+ ?parenttaxon_id.
        }
        """.trimIndent()

    val queryTaxoRanksInfo =
        """
        $prefixes
        CONSTRUCT {
            ?id ?p ?o
        }
        WHERE {
            ?id wdt:P31     wd:Q427626;
                ?p          ?o.
        }
        """.trimIndent()

    val mirrorQueryForTaxo =
        """
        $prefixes
        CONSTRUCT {
            ?id ?p ?o.
            ?id p:P171 ?p171.
            ?p171 ps:P171 ?parenttaxon_id.
        } WHERE { 
            VALUES ?id { %%IDS%% }
            ?id ?p ?o.
            OPTIONAL { ?id p:P171 ?p171. 
            ?p171 ps:P171 ?parenttaxon_id. }
        }
        """.trimIndent()

    val mirrorQueryForCompound =
        """
        $prefixes
        CONSTRUCT {
            ?id ?p ?o.
        } WHERE { 
            VALUES ?id { %%IDS%% }
            VALUES ?p { wdt:P31 wdt:P274 wdt:P233 wdt:P2017 wdt:P703
             wdt:P234 wdt:P235 wdt:P662 }
            ?id ?p ?o.
        }
        """.trimIndent()

    val mirrorQueryForReference =
        """
        $prefixes
        CONSTRUCT {
            ?id ?p ?o.
        } WHERE { 
            VALUES ?id { %%IDS%% }
            VALUES ?p { wdt:P31 wdt:P1476 wdt:P2093 wdt:P407 wdt:P577 wdt:P1433 
            wdt:P478 wdt:P304 wdt:P433 wdt:P356 wdt:P698 wdt:P5875 }
            ?id ?p ?o.
        }
        """.trimIndent()
}
