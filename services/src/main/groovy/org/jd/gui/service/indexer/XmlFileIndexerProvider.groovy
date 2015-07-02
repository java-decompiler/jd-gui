/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.indexer

import groovy.transform.CompileStatic
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Indexes

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
    String[] getSelectors() { ['*:file:*.xml'] + externalSelectors }

    @CompileStatic
    void index(API api, Container.Entry entry, Indexes indexes) {
        def stringSet = new HashSet<String>()
        def typeReferenceSet = new HashSet<String>()
        def reader

        try {
            reader = factory.createXMLStreamReader(entry.inputStream)

            stringSet.add(reader.version)
            stringSet.add(reader.encoding)
            stringSet.add(reader.characterEncodingScheme)

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        boolean beanFlag = reader.localName.equals('bean')

                        stringSet.add(reader.localName)
                        for (int i = reader.attributeCount - 1; i >= 0; i--) {
                            def attributeName = reader.getAttributeLocalName(i)

                            stringSet.add(attributeName)

                            if (beanFlag && attributeName.equals('class')) {
                                // String bean reference
                                typeReferenceSet.add(reader.getAttributeValue(i).replace('.', '/'))
                            } else {
                                stringSet.add(reader.getAttributeValue(i))
                            }
                        }
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            stringSet.add(reader.getNamespacePrefix(i))
                            stringSet.add(reader.getNamespaceURI(i))
                        }
                        break
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        stringSet.add(reader.getPITarget())
                        stringSet.add(reader.getPIData())
                        break
                    case XMLStreamConstants.START_DOCUMENT:
                        stringSet.add(reader.version)
                        stringSet.add(reader.encoding)
                        stringSet.add(reader.characterEncodingScheme)
                        break
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        stringSet.add(reader.localName)
                        stringSet.add(reader.text)
                        break
                    case XMLStreamConstants.ATTRIBUTE:
                        stringSet.add(reader.prefix)
                        stringSet.add(reader.namespaceURI)
                        stringSet.add(reader.localName)
                        stringSet.add(reader.text)
                        break
                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.DTD:
                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                        stringSet.add(reader.text.trim())
                        break
                    case XMLStreamConstants.NAMESPACE:
                        for (int i = reader.namespaceCount - 1; i >= 0; i--) {
                            stringSet.add(reader.getNamespacePrefix(i))
                            stringSet.add(reader.getNamespaceURI(i))
                        }
                        break
                }
            }
        } catch (Exception ignore) {
        } finally {
            reader?.close()
        }

        def stringIndex = indexes.getIndex('strings')
        def typeReferenceIndex = indexes.getIndex('typeReferences')

        for (def string : stringSet) {
            if (string) {
                stringIndex.get(string).add(entry)
            }
        }

        for (def ref : typeReferenceSet) {
            if (ref) {
                typeReferenceIndex.get(ref).add(entry)
            }
        }
    }
}
