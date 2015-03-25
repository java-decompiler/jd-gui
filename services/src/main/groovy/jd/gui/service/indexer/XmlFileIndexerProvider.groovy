/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes
import jd.gui.spi.Indexer

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import java.util.regex.Pattern

class XmlFileIndexerProvider implements Indexer {
    XMLInputFactory factory

    XmlFileIndexerProvider() {
        factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    }

    String[] getTypes() { ['*:file:*.xml', '*:file:*.xsl', '*:file:*.xslt', '*:file:*.xsd'] }

    Pattern getPathPattern() { null }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        def index = indexes.getIndex('strings')
        def reader

        try {
            reader = factory.createXMLStreamReader(entry.inputStream)

            index.get(reader.version).add(entry)
            index.get(reader.encoding).add(entry)
            index.get(reader.characterEncodingScheme).add(entry)

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        index.get(reader.localName).add(entry)
                        for (int i = reader.attributeCount - 1; i >= 0; i--) {
                            index.get(reader.getAttributeLocalName(i)).add(entry)
                            index.get(reader.getAttributeValue(i)).add(entry)
                        }
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            index.get(reader.getNamespacePrefix(i)).add(entry)
                            index.get(reader.getNamespaceURI(i)).add(entry)
                        }
                        break
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        index.get(reader.getPITarget()).add(entry)
                        index.get(reader.getPIData()).add(entry)
                        break
                    case XMLStreamConstants.START_DOCUMENT:
                        index.get(reader.version).add(entry)
                        index.get(reader.encoding).add(entry)
                        index.get(reader.characterEncodingScheme).add(entry)
                        break
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        index.get(reader.localName).add(entry)
                        index.get(reader.text).add(entry)
                        break
                    case XMLStreamConstants.ATTRIBUTE:
                        index.get(reader.prefix).add(entry)
                        index.get(reader.namespaceURI).add(entry)
                        index.get(reader.localName).add(entry)
                        index.get(reader.text).add(entry)
                        break
                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.DTD:
                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                        index.get(reader.text.trim()).add(entry)
                        break
                    case XMLStreamConstants.NAMESPACE:
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            index.get(reader.getNamespacePrefix(i)).add(entry)
                            index.get(reader.getNamespaceURI(i)).add(entry)
                        }
                        break
                }
            }
        } catch (Exception ignore) {
        } finally {
            reader?.close()
        }
    }
}
