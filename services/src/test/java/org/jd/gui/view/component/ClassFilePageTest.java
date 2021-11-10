package org.jd.gui.view.component;

import junit.framework.TestCase;
import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class ClassFilePageTest extends TestCase {

    public HashMap<String, TypePage.DeclarationData> initDeclarations() {
        TypePage.DeclarationData data = new TypePage.DeclarationData(0, 1, "Test", "test", "I");
        HashMap<String, TypePage.DeclarationData> declarations = new HashMap<>();

        // Init type declarations
        declarations.put("Test", data);
        declarations.put("test/Test", data);

        // Init field declarations
        declarations.put("Test-attributeInt-I", data);
        declarations.put("Test-attributeBoolean-Z", data);
        declarations.put("Test-attributeArrayBoolean-[[Z", data);
        declarations.put("Test-attributeString-Ljava/lang/String;", data);

        declarations.put("test/Test-attributeInt-I", data);
        declarations.put("test/Test-attributeBoolean-Z", data);
        declarations.put("test/Test-attributeArrayBoolean-[[Z", data);
        declarations.put("test/Test-attributeString-Ljava/lang/String;", data);

        // Init method declarations
        declarations.put("Test-getInt-()I", data);
        declarations.put("Test-getString-()Ljava/lang/String;", data);
        declarations.put("Test-add-(JJ)J", data);
        declarations.put("Test-createBuffer-(I)[C", data);

        declarations.put("test/Test-getInt-()I", data);
        declarations.put("test/Test-getString-()Ljava/lang/String;", data);
        declarations.put("test/Test-add-(JJ)J", data);
        declarations.put("test/Test-createBuffer-(I)[C", data);

        return declarations;
    }

    public TreeMap<Integer, HyperlinkPage.HyperlinkData> initHyperlinks() {
        TreeMap<Integer, HyperlinkPage.HyperlinkData> hyperlinks = new TreeMap<>();

        hyperlinks.put(0, new TypePage.HyperlinkReferenceData(0, 1, new TypePage.ReferenceData("java/lang/Integer", "MAX_VALUE", "I", "Test")));
        hyperlinks.put(1, new TypePage.HyperlinkReferenceData(0, 1, new TypePage.ReferenceData("java/lang/Integer", "toString", "()Ljava/lang/String;", "Test")));

        return hyperlinks;
    }

    public ArrayList<TypePage.StringData> initStrings() {
        ArrayList<TypePage.StringData> strings = new ArrayList<>();

        strings.add(new TypePage.StringData(0, 3, "abc", "Test"));

        return strings;
    }

    public void testMatchFragmentAndAddDocumentRange() {
        HashMap<String, TypePage.DeclarationData> declarations = initDeclarations();
        ArrayList<DocumentRange> ranges = new ArrayList<>();

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("Test-attributeBoolean-Z", declarations, ranges);
        Assert.assertTrue(ranges.size() == 1);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-attributeBoolean-Z", declarations, ranges);
        Assert.assertTrue(ranges.size() == 1);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("*/Test-attributeBoolean-Z", declarations, ranges);
        Assert.assertTrue(ranges.size() == 2);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("Test-createBuffer-(I)[C", declarations, ranges);
        Assert.assertTrue(ranges.size() == 1);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-createBuffer-(I)[C", declarations, ranges);
        Assert.assertTrue(ranges.size() == 1);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("*/Test-getString-(*)?", declarations, ranges);
        Assert.assertTrue(ranges.size() == 2);

        ranges.clear();
        ClassFilePage.matchFragmentAndAddDocumentRange("test/Test-add-(?J)?", declarations, ranges);
        Assert.assertTrue(ranges.size() == 1);
    }

    public void testMatchQueryAndAddDocumentRange() {
        HashMap<String, String> parameters = new HashMap<>();
        HashMap<String, TypePage.DeclarationData> declarations = initDeclarations();
        TreeMap<Integer, HyperlinkPage.HyperlinkData> hyperlinks = initHyperlinks();
        ArrayList<TypePage.StringData> strings = initStrings();
        ArrayList<DocumentRange> ranges = new ArrayList<>();

        parameters.put("highlightPattern", "ab");
        parameters.put("highlightFlags", "s");

        parameters.put("highlightScope", null);
        ranges.clear();
        ClassFilePage.matchQueryAndAddDocumentRange(parameters, declarations, hyperlinks, strings, ranges);
        Assert.assertTrue(ranges.size() == 1);

        parameters.put("highlightScope", "");
        ranges.clear();
        ClassFilePage.matchQueryAndAddDocumentRange(parameters, declarations, hyperlinks, strings, ranges);
        Assert.assertTrue(ranges.size() == 1);

        parameters.put("highlightScope", "Test");
        ranges.clear();
        ClassFilePage.matchQueryAndAddDocumentRange(parameters, declarations, hyperlinks, strings, ranges);
        Assert.assertTrue(ranges.size() == 1);
    }

    public void testMatchScope() {
        Assert.assertTrue(ClassFilePage.matchScope(null, "java/lang/String"));
        Assert.assertTrue(ClassFilePage.matchScope("", "java/lang/String"));

        Assert.assertTrue(ClassFilePage.matchScope("java/lang/String", "java/lang/String"));
        Assert.assertTrue(ClassFilePage.matchScope("*/lang/String", "java/lang/String"));
        Assert.assertTrue(ClassFilePage.matchScope("*/String", "java/lang/String"));

        Assert.assertTrue(ClassFilePage.matchScope(null, "Test"));
        Assert.assertTrue(ClassFilePage.matchScope("", "Test"));

        Assert.assertTrue(ClassFilePage.matchScope("Test", "Test"));
        Assert.assertTrue(ClassFilePage.matchScope("*/Test", "Test"));
    }
}
