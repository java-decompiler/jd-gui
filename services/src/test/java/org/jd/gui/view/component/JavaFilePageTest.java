package org.jd.gui.view.component;

import junit.framework.TestCase;

import java.util.HashMap;

public class JavaFilePageTest extends TestCase {

    public HashMap<String, TypePage.DeclarationData> initDeclarations() {
        TypePage.DeclarationData data = new TypePage.DeclarationData(0, 1, "Test", "test", "I");
        HashMap<String, TypePage.DeclarationData> declarations = new HashMap<>();

        // Init type declarations
        declarations.put("Test", data);
        declarations.put("test/Test", data);
        declarations.put("*/Test", data);

        // Init field declarations
        declarations.put("Test-attributeInt-I", data);
        declarations.put("Test-attributeBoolean-Z", data);
        declarations.put("Test-attributeArrayBoolean-[[Z", data);
        declarations.put("Test-attributeString-Ljava/lang/String;", data);

        declarations.put("test/Test-attributeInt-I", data);
        declarations.put("test/Test-attributeBoolean-Z", data);
        declarations.put("test/Test-attributeArrayBoolean-[[Z", data);
        declarations.put("test/Test-attributeString-Ljava/lang/String;", data);

        declarations.put("*/Test-attributeBoolean-?", data);
        declarations.put("*/Test-attributeBoolean-Z", data);
        declarations.put("test/Test-attributeBoolean-?", data);

        // Init method declarations
        declarations.put("*/Test-getInt-()I", data);
        declarations.put("*/Test-getString-()Ljava/lang/String;", data);
        declarations.put("*/Test-add-(JJ)J", data);
        declarations.put("*/Test-createBuffer-(I)[C", data);

        declarations.put("test/Test-getInt-(*)?", data);
        declarations.put("test/Test-getString-(*)?", data);
        declarations.put("test/Test-add-(*)?", data);
        declarations.put("test/Test-createBuffer-(*)?", data);

        declarations.put("*/Test-getInt-(*)?", data);
        declarations.put("*/Test-getString-(*)?", data);
        declarations.put("*/Test-add-(*)?", data);
        declarations.put("*/Test-createBuffer-(*)?", data);

        return declarations;
    }

    public void testMatchFragmentAndAddDocumentRange() {}

    public void testMatchQueryAndAddDocumentRange() {}

    public void testMatchScope() {}
}
