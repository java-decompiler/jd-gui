/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.view.component

class ClassFilePageTest extends GroovyTestCase {

    HashMap<String, TypePage.DeclarationData> initDeclarations() {
        def data = new TypePage.DeclarationData(0, 1, "Test", "test", "I")
        HashMap<String, TypePage.DeclarationData> declarations = [:]

        // Init type declarations
        declarations.put("Test", data)
        declarations.put("test/Test", data)

        // Init field declarations
        declarations.put("Test-attributeInt-I", data)
        declarations.put("Test-attributeBoolean-Z", data)
        declarations.put("Test-attributeArrayBoolean-[[Z", data)
        declarations.put("Test-attributeString-Ljava/lang/String;", data)

        declarations.put("test/Test-attributeInt-I", data)
        declarations.put("test/Test-attributeBoolean-Z", data)
        declarations.put("test/Test-attributeArrayBoolean-[[Z", data)
        declarations.put("test/Test-attributeString-Ljava/lang/String;", data)

        // Init method declarations
        declarations.put("Test-getInt-()I", data)
        declarations.put("Test-getString-()Ljava/lang/String;", data)
        declarations.put("Test-add-(JJ)J", data)
        declarations.put("Test-createBuffer-(I)[C", data)

        declarations.put("test/Test-getInt-()I", data)
        declarations.put("test/Test-getString-()Ljava/lang/String;", data)
        declarations.put("test/Test-add-(JJ)J", data)
        declarations.put("test/Test-createBuffer-(I)[C", data)

        return declarations
    }

    TreeMap<Integer, HyperlinkPage.HyperlinkData> initHyperlinks() {
        def hyperlinks = new TreeMap<Integer, HyperlinkPage.HyperlinkData>()

        hyperlinks.put(0, new TypePage.HyperlinkReferenceData(0, 1, new TypePage.ReferenceData("java/lang/Integer", "MAX_VALUE", "I", "Test")))
        hyperlinks.put(0, new TypePage.HyperlinkReferenceData(0, 1, new TypePage.ReferenceData("java/lang/Integer", "toString", "()Ljava/lang/String;", "Test")))

        return hyperlinks
    }

    ArrayList<TypePage.StringData> initStrings() {
        def strings = new ArrayList<TypePage.StringData>()

        strings.add(new TypePage.StringData(0, 3, "abc", "Test"))

        return strings
    }

    void testMatchFragmentAndAddDocumentRange() {
        def declarations = initDeclarations()
        def ranges = []

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("Test-attributeBoolean-Z", declarations, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-attributeBoolean-Z", declarations, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("*/Test-attributeBoolean-Z", declarations, ranges)
        assertTrue ranges.size() == 2

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("Test-createBuffer-(I)[C", declarations, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-createBuffer-(I)[C", declarations, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("*/Test-getString-(*)?", declarations, ranges)
        assertTrue ranges.size() == 2

        ranges.clear()
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-add-(?J)?", declarations, ranges)
        assertTrue ranges.size() == 1
    }

    void testMatchQueryAndAddDocumentRange() {
        def declarations = initDeclarations()
        def hyperlinks = initHyperlinks()
        def strings = initStrings()
        def ranges = []

        ranges.clear()
        ClassFilePage.matchQueryAndAddDocumentRange([highlightPattern:"ab", highlightFlags:"s", highlightScope:null], declarations, hyperlinks, strings, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchQueryAndAddDocumentRange([highlightPattern:"ab", highlightFlags:"s", highlightScope:""], declarations, hyperlinks, strings, ranges)
        assertTrue ranges.size() == 1

        ranges.clear()
        ClassFilePage.matchQueryAndAddDocumentRange([highlightPattern:"ab", highlightFlags:"s", highlightScope:"Test"], declarations, hyperlinks, strings, ranges)
        assertTrue ranges.size() == 1
    }

    void testMatchScope() {
        assertTrue ClassFilePage.matchScope(null, "java/lang/String")
        assertTrue ClassFilePage.matchScope("", "java/lang/String")

        assertTrue ClassFilePage.matchScope("java/lang/String", "java/lang/String")
        assertTrue ClassFilePage.matchScope("*/lang/String", "java/lang/String")
        assertTrue ClassFilePage.matchScope("*/String", "java/lang/String")

        assertTrue ClassFilePage.matchScope(null, "Test")
        assertTrue ClassFilePage.matchScope("", "Test")

        assertTrue ClassFilePage.matchScope("Test", "Test")
        assertTrue ClassFilePage.matchScope("*/Test", "Test")
    }
}
