/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component;

import com.sun.deploy.util.StringUtils;
import org.jd.gui.api.API;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.index.IndexesUtil;
import org.jd.gui.util.io.TextReader;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestFilePage extends HyperlinkPage implements UriGettable, IndexesChangeListener {
    protected API api;
    protected Container.Entry entry;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes = Collections.emptyList();
    protected boolean beautify = false;
    protected JCheckBox beautifyCheckbox;

    public ManifestFilePage(API api, Container.Entry entry) {
        beautifyCheckbox = new JCheckBox();
        beautifyCheckbox.setText("Beautify");
        beautifyCheckbox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                beautify = beautifyCheckbox.isSelected();
                buildText(entry);
            }
        });
        add(beautifyCheckbox, BorderLayout.NORTH);
        this.api = api;
        this.entry = entry;
        buildText(entry);
    }

    private void buildText(Container.Entry entry) {
        // Load content file
        String text = TextReader.getText(entry.getInputStream());
        if (beautify) {
            Manifest manifest = new Manifest();
            try {
                manifest.read(entry.getInputStream());
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                dumpManifestHeaders(manifest, printWriter);
                text = stringWriter.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Parse hyperlinks. Docs:
        // - http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
        // - http://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html
        int startLineIndex = text.indexOf("Main-Class:");
        if (startLineIndex != -1) {
            // Example: Main-Class: jd.gui.App
            int startIndex = skipSeparators(text, startLineIndex + "Main-Class:".length());
            int endIndex = searchEndIndexOfValue(text, startLineIndex, startIndex);
            String typeName = text.substring(startIndex, endIndex);
            String internalTypeName = typeName.replace('.', '/');
            addHyperlink(new ManifestHyperlinkData(startIndex, endIndex, internalTypeName + "-main-([Ljava/lang/String;)V"));
        }

        startLineIndex = text.indexOf("Premain-Class:");
        if (startLineIndex != -1) {
            // Example: Premain-Class: packge.JavaAgent
            int startIndex = skipSeparators(text, startLineIndex + "Premain-Class:".length());
            int endIndex = searchEndIndexOfValue(text, startLineIndex, startIndex);
            String typeName = text.substring(startIndex, endIndex);
            String internalTypeName = typeName.replace('.', '/');
            // Undefined parameters : 2 candidate methods
            // http://docs.oracle.com/javase/6/docs/api/java/lang/instrument/package-summary.html
            addHyperlink(new ManifestHyperlinkData(startIndex, endIndex, internalTypeName + "-premain-(*)?"));
        }
        // Display
        setText(text);
    }

    public int skipSeparators(String text, int index) {
        int length = text.length();

        while (index < length) {
            switch (text.charAt(index)) {
                case ' ': case '\t': case '\n': case '\r':
                    index++;
                    break;
                default:
                    return index;
            }
        }

        return index;
    }

    public int searchEndIndexOfValue(String text, int startLineIndex, int startIndex) {
        int length = text.length();
        int index = startIndex;

        while (index < length) {
            // MANIFEST.MF Specification: max line length = 72
            // http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
            switch (text.charAt(index)) {
                case '\r':
                    // CR followed by LF ?
                    if ((index-startLineIndex >= 70) && (index+1 < length) && (text.charAt(index+1) == ' ')) {
                        // Multiline value
                        startLineIndex = index+1;
                    } else if ((index-startLineIndex >= 70) && (index+2 < length) && (text.charAt(index+1) == '\n') && (text.charAt(index+2) == ' ')) {
                        // Multiline value
                        index++;
                        startLineIndex = index+1;
                    } else {
                        // (End of file) or (single line value) => return end index
                        return index;
                    }
                    break;
                case '\n':
                    if ((index-startLineIndex >= 70) && (index+1 < length) && (text.charAt(index+1) == ' ')) {
                        // Multiline value
                        startLineIndex = index+1;
                    } else {
                        // (End of file) or (single line value) => return end index
                        return index;
                    }
                    break;
            }
            index++;
        }

        return index;
    }

    protected boolean isHyperlinkEnabled(HyperlinkData hyperlinkData) { return ((ManifestHyperlinkData)hyperlinkData).enabled; }

    protected void openHyperlink(int x, int y, HyperlinkData hyperlinkData) {
        ManifestHyperlinkData data = (ManifestHyperlinkData)hyperlinkData;

        if (data.enabled) {
            try {
                // Save current position in history
                Point location = textArea.getLocationOnScreen();
                int offset = textArea.viewToModel(new Point(x-location.x, y-location.y));
                URI uri = entry.getUri();
                api.addURI(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "position=" + offset, null));
                // Open link
                String text = getText();
                String textLink = getValue(text, hyperlinkData.startPosition, hyperlinkData.endPosition);
                String internalTypeName = textLink.replace('.', '/');
                List<Container.Entry> entries = IndexesUtil.findInternalTypeName(collectionOfFutureIndexes, internalTypeName);
                String rootUri = entry.getContainer().getRoot().getUri().toString();
                ArrayList<Container.Entry> sameContainerEntries = new ArrayList<>();

                for (Container.Entry entry : entries) {
                    if (entry.getUri().toString().startsWith(rootUri)) {
                        sameContainerEntries.add(entry);
                    }
                }

                if (sameContainerEntries.size() > 0) {
                    api.openURI(x, y, sameContainerEntries, null, data.fragment);
                } else if (entries.size() > 0) {
                    api.openURI(x, y, entries, null, data.fragment);
                }
            } catch (URISyntaxException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    // --- UriGettable --- //
    public URI getUri() { return entry.getUri(); }

    // --- ContentSavable --- //
    public String getFileName() {
        String path = entry.getPath();
        int index = path.lastIndexOf('/');
        return path.substring(index+1);
    }

    // --- IndexesChangeListener --- //
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        // Update the list of containers
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        // Refresh links
        boolean refresh = false;
        String text = getText();

        for (Map.Entry<Integer, HyperlinkData> entry : hyperlinks.entrySet()) {
            ManifestHyperlinkData entryData = (ManifestHyperlinkData)entry.getValue();
            String textLink = getValue(text, entryData.startPosition, entryData.endPosition);
            String internalTypeName = textLink.replace('.', '/');
            boolean enabled = IndexesUtil.containsInternalTypeName(collectionOfFutureIndexes, internalTypeName);

            if (entryData.enabled != enabled) {
                entryData.enabled = enabled;
                refresh = true;
            }
        }

        if (refresh) {
            textArea.repaint();
        }
    }

    public static String getValue(String text, int startPosition, int endPosition) {
        return text
            // Extract text of link
            .substring(startPosition, endPosition)
            // Convert multiline value
            .replace("\r\n ", "")
            .replace("\r ", "")
            .replace("\n ", "");
    }

    public static class ManifestHyperlinkData extends HyperlinkData {
        public boolean enabled;
        public String fragment;

        ManifestHyperlinkData(int startPosition, int endPosition, String fragment) {
            super(startPosition, endPosition);
            this.enabled = false;
            this.fragment = fragment;
        }
    }

    public static PrintWriter dumpManifestHeaders(Manifest manifest, PrintWriter out) throws IOException {

        Attributes mainAttributes = manifest.getMainAttributes();
        Map<String, String> sortedMainAttributes = getSortedAttributes(mainAttributes);
        for (Map.Entry<String,String> attributeEntry : sortedMainAttributes.entrySet()) {
            out.print(attributeEntry.getKey());
            out.print(": ");
            out.println(dumpValue(attributeEntry.getValue()));
        }

        for (Map.Entry<String, Attributes> entryAttributes : manifest.getEntries().entrySet()) {
            out.println("");
            out.println("Entry: " + entryAttributes.getKey());
            Map<String, String> sortedEntryAttributes = getSortedAttributes(entryAttributes.getValue());
            for (Map.Entry<String,String> attributeEntry : sortedEntryAttributes.entrySet()) {
                out.print(attributeEntry.getKey());
                out.print(": ");
                out.println(dumpValue(attributeEntry.getValue()));
            }
        }
        return out;
    }

    private static Map<String, String> getSortedAttributes(Attributes mainAttributes) {
        Map<String,String> sortedMainAttributes = new TreeMap<String,String>();
        for (Map.Entry<Object,Object> attributeEntry : mainAttributes.entrySet()) {
            Attributes.Name attributeName = (Attributes.Name) attributeEntry.getKey();
            sortedMainAttributes.put(attributeName.toString(), (String) attributeEntry.getValue());
        }
        return sortedMainAttributes;
    }

    private static String dumpValue(String value) {
        StringTokenizer valueTokenizer = new StringTokenizer(value, "\",", true);
        boolean inString = false;
        List<String> valueParts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        while (valueTokenizer.hasMoreTokens()) {
            String currentToken = valueTokenizer.nextToken();
            if ("\"".equals(currentToken)) {
                inString = !inString;
                currentPart.append(currentToken);
            } else if (",".equals(currentToken) && !inString) {
                valueParts.add(currentPart.toString());
                currentPart = new StringBuilder();
            } else {
                currentPart.append(currentToken);
            }
        }
        if (currentPart.length() > 0) {
            valueParts.add(currentPart.toString());
        }
        return StringUtils.join(valueParts, ",\n  ");
    }

}

