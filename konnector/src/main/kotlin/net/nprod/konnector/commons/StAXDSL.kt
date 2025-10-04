/*
 *
 * SPDX-License-Identifier: MIT License
 *
 * Copyright (c) 2020 Jonathan Bisson
 *
 */

package net.nprod.konnector.commons

import java.io.StringWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader
import javax.xml.stream.events.XMLEvent

/**
 * Exception thrown when an unexpected XML element has been found
 */
class UnexpectedXMLElementException(
    override val message: String,
) : Exception()

/**
 * Is the current entry of type CDATA or CHARACTERS
 */

val XMLStreamReader.isText: Boolean
    get() = (this.eventType == XMLStreamReader.CHARACTERS) or (this.eventType == XMLStreamReader.CDATA)

/**
 * If the current element is a start or end element, return its name, if not returns null
 * It is useful when using takeWhile that calculates the full expression
 */

val XMLStreamReader.localNameValidated: String?
    get() = if (this.isEndElement or this.isStartElement) this.localName else null

class ElementList<T>(
    l: List<T>,
    val stream: XMLStreamReader,
) : List<T> by l

/**
 * Allows elements to be nested such as in element("A").element("B")
 * Instead of having to write element("A") { element("B")… }
 */

fun ElementList<Any>.element(
    vararg tagName: String,
    transform: (String) -> Any,
): ElementList<in Any> = this.stream.element(*tagName, transform = transform)

/**
 * Get the text from the XML element [tagName]
 * It differs from a direct call to allText as allText allows to bind all the found texts in the given element until
 * it reaches the end of [tagName]
 */

fun XMLStreamReader.tagText(tagName: String): String = this.element(tagName) { allText(tagName) }.joinToString()

/**
 * Grab an element without knowing anything about it
 */
fun XMLStreamReader.contentAsXML(tagName: String): String {
    // Inspired by http://www.java2s.com/Code/Java/XML/XmlReaderToWriter.htm
    val sw = StringWriter()
    val xmlOutputFactory = XMLOutputFactory.newFactory()
    val writer = xmlOutputFactory.createXMLStreamWriter(sw)
    writer.writeStartElement("PubmedArticleSet")
    writer.writeStartElement("PubmedArticle")

    this
        .asSequence()
        .takeWhile { !(it.isEndElement and (tagName == it.localNameValidated)) }
        .map {
            when (it.eventType) {
                XMLEvent.START_ELEMENT -> {
                    val localName = this.localName
                    val namespaceURI = this.namespaceURI
                    if (namespaceURI.isNotEmpty()) {
                        val prefix = this.prefix
                        if (prefix != null) {
                            writer.writeStartElement(prefix, localName, namespaceURI)
                        } else {
                            writer.writeStartElement(namespaceURI, localName)
                        }
                    } else {
                        writer.writeStartElement(localName)
                    }

                    run {
                        for (i in 0 until this.namespaceCount) {
                            writer.writeNamespace(this.getNamespacePrefix(i), this.getNamespaceURI(i))
                        }
                    }
                    for (i in 0 until this.attributeCount) {
                        val attUri = this.getAttributeNamespace(i)
                        if (attUri != "") {
                            writer.writeAttribute(attUri, this.getAttributeLocalName(i), this.getAttributeValue(i))
                        } else {
                            writer.writeAttribute(this.getAttributeLocalName(i), this.getAttributeValue(i))
                        }
                    }
                }
                XMLEvent.END_ELEMENT -> writer.writeEndElement()
                XMLEvent.SPACE -> {
                }
                XMLEvent.CHARACTERS -> writer.writeCharacters(this.textCharacters, this.textStart, this.textLength)
                XMLEvent.PROCESSING_INSTRUCTION -> writer.writeProcessingInstruction(this.piTarget, this.piData)
                else -> throw UnexpectedXMLElementException("Unhandled element ${this.eventType}")
            }
        }.toList()
    writer.writeEndElement()
    writer.writeEndElement()
    writer.flush()
    return sw.toString()
}

/**
 * Allows for element("a").element("b") { }
 *
 * @param tagName name of the XML element
 * @param transform function to transform the transform into [T]
 */

fun XMLStreamReader.element(tagName: String): XMLStreamReader {
    this.asSequence().takeWhile { (!it.isStartElement) and (tagName != it.localNameValidated) }
    return this
}

/**
 * For each single element [tagName], run [transform] on it and add make the result into a list
 *
 * @param tagName name of the XML element
 * @param transform function to transform the transform into [T]
 */

fun <T> XMLStreamReader.element(
    vararg tagName: String,
    transform: (String) -> T,
): ElementList<T> {
    val currentTagName = this.localName
    return ElementList(
        this
            .asSequence()
            .takeWhile { (!it.isEndElement || (it.localNameValidated != currentTagName)) }
            .filter { it.isStartElement }
            .filter { it.localName in tagName }
            .map { transform(it.localName) }
            .toList(),
        this,
    )
}

/**
 * A wrapper class that turns a XMLStreamReader into an Iterator
 * The trick here is that XMLStreamReader is a cursor, so we just return itself in the iterator when moving the cursor
 */

class XMLStreamReaderIterator(
    val reader: XMLStreamReader,
) : Iterator<XMLStreamReader> {
    override fun hasNext(): Boolean = reader.hasNext()

    @Suppress("SwallowedException")
    override fun next(): XMLStreamReader {
        try {
            reader.next()
        } catch (e: XMLStreamException) {
            throw NoSuchElementException()
        }
        return reader
    }
}

/**
 * Build a map of the attributes
 */
val XMLStreamReader.attributes: Map<String, String>
    get() {
        return (0 until this.attributeCount)
            .map {
                this.getAttributeName(it).localPart to this.getAttributeValue(it)
            }.toMap()
    }

/**
 * Make an Iterable out of a XMLStreamReader object so we can use map, forEach etc
 */

fun XMLStreamReader.asSequence(): Sequence<XMLStreamReader> =
    object : Sequence<XMLStreamReader> {
        override fun iterator(): Iterator<XMLStreamReader> = XMLStreamReaderIterator(this@asSequence)
    }

/**
 * This function waits for the first start of element, and then gives to [transform]
 *
 * @param transform The function that will transform that transform
 */

fun <R> XMLStreamReader.document(transform: XMLStreamReader.() -> R): R = transform(this.asSequence().first { isStartElement })

/**
 * Extract the text from the tag named [tagName]. This function will extract any character or CDATA types inside that
 * element until the closing element for [tagName] is found
 *
 * It allows extracting things such as :
 * <abstract><abstractElement>foo</abstractElement><abstractElement>bar</abstractElement></abstract>
 * that will be gatherd as "foo bar"
 *
 * @param tagName name of the tag
 * @param join the string used to join the different elements, by default a space
 */

fun XMLStreamReader.allText(
    tagName: String,
    join: String = " ",
): String? =
    this
        .asSequence()
        .takeWhile { !isEndElement || (localNameValidated != tagName) }
        .filter { isText }
        .map { this.text }
        .joinToString(join)
