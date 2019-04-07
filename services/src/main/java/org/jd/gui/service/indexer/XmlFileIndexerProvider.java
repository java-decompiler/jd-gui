/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.indexer;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class XmlFileIndexerProvider extends AbstractIndexerProvider {
    protected XMLInputFactory factory;

    public XmlFileIndexerProvider() {
        factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.xml"); }

    @Override
    @SuppressWarnings("unchecked")
    public void index(API api, Container.Entry entry, Indexes indexes) {
        HashSet<String> stringSet = new HashSet<>();
        HashSet<String> typeReferenceSet = new HashSet<>();
        XMLStreamReader reader = null;

        try {
            reader = factory.createXMLStreamReader(entry.getInputStream());

            stringSet.add(reader.getVersion());
            stringSet.add(reader.getEncoding());
            stringSet.add(reader.getCharacterEncodingScheme());

            while (reader.hasNext()) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        boolean beanFlag = reader.getLocalName().equals("bean");

                        stringSet.add(reader.getLocalName());
                        for (int i = reader.getAttributeCount() - 1; i >= 0; i--) {
                            String attributeName = reader.getAttributeLocalName(i);

                            stringSet.add(attributeName);

                            if (beanFlag && attributeName.equals("class")) {
                                // String bean reference
                                typeReferenceSet.add(reader.getAttributeValue(i).replace(".", "/"));
                            } else {
                                stringSet.add(reader.getAttributeValue(i));
                            }
                        }
                        for (int i = reader.getNamespaceCount() - 1; i >= 0; i--) {
                            stringSet.add(reader.getNamespacePrefix(i));
                            stringSet.add(reader.getNamespaceURI(i));
                        }
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        stringSet.add(reader.getPITarget());
                        stringSet.add(reader.getPIData());
                        break;
                    case XMLStreamConstants.START_DOCUMENT:
                        stringSet.add(reader.getVersion());
                        stringSet.add(reader.getEncoding());
                        stringSet.add(reader.getCharacterEncodingScheme());
                        break;
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        stringSet.add(reader.getLocalName());
                        stringSet.add(reader.getText());
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        stringSet.add(reader.getPrefix());
                        stringSet.add(reader.getNamespaceURI());
                        stringSet.add(reader.getLocalName());
                        stringSet.add(reader.getText());
                        break;
                    case XMLStreamConstants.COMMENT:
                    case XMLStreamConstants.DTD:
                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                        stringSet.add(reader.getText().trim());
                        break;
                    case XMLStreamConstants.NAMESPACE:
                        for (int i = reader.getNamespaceCount() - 1; i >= 0; i--) {
                            stringSet.add(reader.getNamespacePrefix(i));
                            stringSet.add(reader.getNamespaceURI(i));
                        }
                        break;
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            }
        }

        Map<String, Collection> stringIndex = indexes.getIndex("strings");
        Map<String, Collection> typeReferenceIndex = indexes.getIndex("typeReferences");

        for (String string : stringSet) {
            if ((string != null) && !string.isEmpty()) {
                stringIndex.get(string).add(entry);
            }
        }

        for (String ref : typeReferenceSet) {
            if ((ref != null) && !ref.isEmpty()) {
                typeReferenceIndex.get(ref).add(entry);
            }
        }
    }
}
