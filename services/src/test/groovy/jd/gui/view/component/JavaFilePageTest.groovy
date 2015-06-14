/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

class JavaFilePageTest extends GroovyTestCase {

    HashMap<String, TypePage.DeclarationData> initDeclarations() {
        def data = new TypePage.DeclarationData(0, 1, "Test", "test", "I")
        HashMap<String, TypePage.DeclarationData> declarations = [:]

        // Init type declarations
        declarations.put("Test", data)
        declarations.put("test/Test", data)
        declarations.put("*/Test", data)

        // Init field declarations
        declarations.put("Test-attributeInt-I", data)
        declarations.put("Test-attributeBoolean-Z", data)
        declarations.put("Test-attributeArrayBoolean-[[Z", data)
        declarations.put("Test-attributeString-Ljava/lang/String;", data)

        declarations.put("test/Test-attributeInt-I", data)
        declarations.put("test/Test-attributeBoolean-Z", data)
        declarations.put("test/Test-attributeArrayBoolean-[[Z", data)
        declarations.put("test/Test-attributeString-Ljava/lang/String;", data)

        declarations.put("*/Test-attributeBoolean-?", data)
        declarations.put("*/Test-attributeBoolean-Z", data)
        declarations.put("test/Test-attributeBoolean-?", data)

        // Init method declarations
        declarations.put("*/Test-getInt-()I", data)
        declarations.put("*/Test-getString-()Ljava/lang/String;", data)
        declarations.put("*/Test-add-(JJ)J", data)
        declarations.put("*/Test-createBuffer-(I)[C", data)

        declarations.put("test/Test-getInt-(*)?", data)
        declarations.put("test/Test-getString-(*)?", data)
        declarations.put("test/Test-add-(*)?", data)
        declarations.put("test/Test-createBuffer-(*)?", data)

        declarations.put("*/Test-getInt-(*)?", data)
        declarations.put("*/Test-getString-(*)?", data)
        declarations.put("*/Test-add-(*)?", data)
        declarations.put("*/Test-createBuffer-(*)?", data)

        return declarations
    }

    void testMatchFragmentAndAddDocumentRange() {
    }

    void testMatchQueryAndAddDocumentRange() {
    }

    void testMatchScope() {
    }
}
