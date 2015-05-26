/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.indexer

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.model.Container
import jd.gui.api.model.Indexes

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

class XmlFileIndexerProvider extends AbstractIndexerProvider {
    XMLInputFactory factory

    XmlFileIndexerProvider() {
        factory = XMLInputFactory.newInstance()
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
    }

    /**
     * @return local + optional external selectors
     */
    String[] getSelectors() { ['*:file:*.xml', '*:file:*.xsl', '*:file:*.xslt', '*:file:*.xsd'] + externalSelectors }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        def index = indexes.getIndex('strings')
        def set = new HashSet<String>()
        def reader

        try {
            reader = factory.createXMLStreamReader(entry.inputStream)

            set.add(reader.version)
            set.add(reader.encoding)
            set.add(reader.characterEncodingScheme)

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        set.add(reader.localName)
                        for (int i = reader.attributeCount - 1; i >= 0; i--) {
                            set.add(reader.getAttributeLocalName(i))
                            set.add(reader.getAttributeValue(i))
                        }
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            set.add(reader.getNamespacePrefix(i))
                            set.add(reader.getNamespaceURI(i))
                        }
                        break
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        set.add(reader.getPITarget())
                        set.add(reader.getPIData())
                        break
                    case XMLStreamConstants.START_DOCUMENT:
                        set.add(reader.version)
                        set.add(reader.encoding)
                        set.add(reader.characterEncodingScheme)
                        break
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        set.add(reader.localName)
                        set.add(reader.text)
                        break
                    case XMLStreamConstants.ATTRIBUTE:
                        set.add(reader.prefix)
                        set.add(reader.namespaceURI)
                        set.add(reader.localName)
                        set.add(reader.text)
                        break
                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.DTD:
                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                        set.add(reader.text.trim())
                        break
                    case XMLStreamConstants.NAMESPACE:
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            set.add(reader.getNamespacePrefix(i))
                            set.add(reader.getNamespaceURI(i))
                        }
                        break
                }
            }
        } catch (Exception ignore) {
        } finally {
            reader?.close()
        }

        for (def string : set) {
            if (string) {
                index.get(string).add(entry)
            }
        }
    }
}
