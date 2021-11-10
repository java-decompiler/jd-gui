/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */
package org.jd.gui.view.component;

import org.fife.ui.rsyntaxtextarea.DocumentRange;
import org.fife.ui.rtextarea.Marker;
import org.jd.gui.api.API;
import org.jd.gui.api.feature.FocusedTypeGettable;
import org.jd.gui.api.feature.IndexesChangeListener;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.api.feature.UriOpenable;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Indexes;
import org.jd.gui.api.model.Type;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.index.IndexesUtil;
import org.jd.gui.util.matcher.DescriptorMatcher;

import java.awt.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TypePage extends CustomLineNumbersPage implements UriGettable, UriOpenable, IndexesChangeListener, FocusedTypeGettable {
    protected API api;
    protected Container.Entry entry;
    protected Collection<Future<Indexes>> collectionOfFutureIndexes = Collections.emptyList();

    protected HashMap<String, DeclarationData> declarations = new HashMap<>();
    protected TreeMap<Integer, DeclarationData> typeDeclarations = new TreeMap<>();
    protected ArrayList<ReferenceData> references = new ArrayList<>();
    protected ArrayList<StringData> strings = new ArrayList<>();

    public TypePage(API api, Container.Entry entry) {
        // Init attributes
        this.api = api;
        this.entry = entry;
    }

    @Override
    protected boolean isHyperlinkEnabled(HyperlinkData hyperlinkData) {
        return ((HyperlinkReferenceData)hyperlinkData).reference.enabled;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void openHyperlink(int x, int y, HyperlinkData hyperlinkData) {
        HyperlinkReferenceData hyperlinkReferenceData = (HyperlinkReferenceData)hyperlinkData;

        if (hyperlinkReferenceData.reference.enabled) {
            try {
                // Save current position in history
                Point location = textArea.getLocationOnScreen();
                int offset = textArea.viewToModel(new Point(x - location.x, y - location.y));
                URI uri = entry.getUri();
                api.addURI(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), "position=" + offset, null));

                // Open link
                ReferenceData reference = hyperlinkReferenceData.reference;
                String typeName = reference.typeName;
                List<Container.Entry> entries = IndexesUtil.findInternalTypeName(collectionOfFutureIndexes, typeName);
                String fragment = typeName;

                if (reference.name != null) {
                    fragment += '-' + reference.name;
                }
                if (reference.descriptor != null) {
                    fragment += '-' + reference.descriptor;
                }

                if (entries.contains(entry)) {
                    api.openURI(new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), fragment));
                } else {
                    String rootUri = entry.getContainer().getRoot().getUri().toString();
                    ArrayList<Container.Entry> sameContainerEntries = new ArrayList<>();

                    for (Container.Entry entry : entries) {
                        if (entry.getUri().toString().startsWith(rootUri)) {
                            sameContainerEntries.add(entry);
                        }
                    }

                    if (sameContainerEntries.size() > 0) {
                        api.openURI(x, y, sameContainerEntries, null, fragment);
                    } else if (entries.size() > 0) {
                        api.openURI(x, y, entries, null, fragment);
                    }
                }
            } catch (URISyntaxException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }
    }

    // --- UriGettable --- //
    @Override public URI getUri() { return entry.getUri(); }

    // --- UriOpenable --- //
    /**
     * @param uri for URI format, @see jd.gui.api.feature.UriOpenable
     */
    @Override
    public boolean openUri(URI uri) {
        ArrayList<DocumentRange> ranges = new ArrayList<>();
        String fragment = uri.getFragment();
        String query = uri.getQuery();

        Marker.clearMarkAllHighlights(textArea);

        if (fragment != null) {
            matchFragmentAndAddDocumentRange(fragment, declarations, ranges);
        }

        if (query != null) {
            Map<String, String> parameters = parseQuery(query);

            if (parameters.containsKey("lineNumber")) {
                String lineNumber = parameters.get("lineNumber");

                try {
                    goToLineNumber(Integer.parseInt(lineNumber));
                    return true;
                } catch (NumberFormatException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else if (parameters.containsKey("position")) {
                String position = parameters.get("position");

                try {
                    int pos = Integer.parseInt(position);
                    if (textArea.getDocument().getLength() > pos) {
                        ranges.add(new DocumentRange(pos, pos));
                    }
                } catch (NumberFormatException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else {
                matchQueryAndAddDocumentRange(parameters, declarations, hyperlinks, strings, ranges);
            }
        }

        if ((ranges != null) && !ranges.isEmpty()) {
            textArea.setMarkAllHighlightColor(SELECT_HIGHLIGHT_COLOR);
            Marker.markAll(textArea, ranges);
            ranges.sort(null);
            setCaretPositionAndCenter(ranges.get(0));
        }

        return true;
    }

    public static void matchFragmentAndAddDocumentRange(String fragment, HashMap<String, DeclarationData> declarations, List<DocumentRange> ranges) {
        if ((fragment.indexOf('?') != -1) || (fragment.indexOf('*') != -1)) {
            // Unknown type and/or descriptor ==> Select all and scroll to the first one
            int lastDash = fragment.lastIndexOf('-');

            if (lastDash == -1) {
                // Search types
                String slashAndTypeName = fragment.substring(1);
                String typeName = fragment.substring(2);

                for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                    if (entry.getKey().endsWith(slashAndTypeName) || entry.getKey().equals(typeName)) {
                        ranges.add(new DocumentRange(entry.getValue().startPosition, entry.getValue().endPosition));
                    }
                }
            } else {
                String prefix = fragment.substring(0, lastDash+1);
                String suffix = fragment.substring(lastDash+1);
                BiFunction<String, String, Boolean> matchDescriptors;

                if (suffix.charAt(0) == '(') {
                    matchDescriptors = DescriptorMatcher::matchMethodDescriptors;
                } else {
                    matchDescriptors = DescriptorMatcher::matchFieldDescriptors;
                }

                if (fragment.charAt(0) == '*') {
                    // Unknown type
                    String slashAndTypeNameAndName = prefix.substring(1);
                    String typeNameAndName = prefix.substring(2);

                    for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                        String key = entry.getKey();
                        if ((key.indexOf(slashAndTypeNameAndName) != -1) || (key.startsWith(typeNameAndName))) {
                            int index = key.lastIndexOf('-') + 1;
                            if (matchDescriptors.apply(suffix, key.substring(index))) {
                                ranges.add(new DocumentRange(entry.getValue().startPosition, entry.getValue().endPosition));
                            }
                        }
                    }
                } else {
                    // Known type
                    for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                        String key = entry.getKey();
                        if (key.startsWith(prefix)) {
                            int index = key.lastIndexOf('-') + 1;
                            if (matchDescriptors.apply(suffix, key.substring(index))) {
                                ranges.add(new DocumentRange(entry.getValue().startPosition, entry.getValue().endPosition));
                            }
                        }
                    }
                }
            }
        } else {
            // Known type and descriptor ==> Search and high light item
            DeclarationData data = declarations.get(fragment);
            if (data != null) {
                ranges.add(new DocumentRange(data.startPosition, data.endPosition));
            } else if (fragment.endsWith("-<clinit>-()V")) {
                // 'static' bloc not found ==> Select type declaration
                String typeName = fragment.substring(0, fragment.indexOf('-'));
                data = declarations.get(typeName);
                ranges.add(new DocumentRange(data.startPosition, data.endPosition));
            }
        }
    }

    public static void matchQueryAndAddDocumentRange(
            Map<String, String> parameters,
            HashMap<String, DeclarationData> declarations, TreeMap<Integer, HyperlinkData> hyperlinks, ArrayList<StringData> strings,
            List<DocumentRange> ranges) {

        String highlightFlags = parameters.get("highlightFlags");
        String highlightPattern = parameters.get("highlightPattern");

        if ((highlightFlags != null) && (highlightPattern != null)) {
            String highlightScope = parameters.get("highlightScope");
            String regexp = createRegExp(highlightPattern);
            Pattern pattern = Pattern.compile(regexp + ".*");

            if (highlightFlags.indexOf('s') != -1) {
                // Highlight strings
                Pattern patternForString = Pattern.compile(regexp);

                for (StringData data : strings) {
                    if (matchScope(highlightScope, data.owner)) {
                        Matcher matcher = patternForString.matcher(data.text);
                        int offset = data.startPosition;

                        while(matcher.find()) {
                            ranges.add(new DocumentRange(offset + matcher.start(), offset + matcher.end()));
                        }
                    }
                }
            }

            boolean t = (highlightFlags.indexOf('t') != -1); // Highlight types
            boolean f = (highlightFlags.indexOf('f') != -1); // Highlight fields
            boolean m = (highlightFlags.indexOf('m') != -1); // Highlight methods
            boolean c = (highlightFlags.indexOf('c') != -1); // Highlight constructors

            if (highlightFlags.indexOf('d') != -1) {
                // Highlight declarations
                for (Map.Entry<String, DeclarationData> entry : declarations.entrySet()) {
                    DeclarationData declaration = entry.getValue();

                    if (matchScope(highlightScope, declaration.typeName)) {
                        if ((t && declaration.isAType()) || (c && declaration.isAConstructor())) {
                            matchAndAddDocumentRange(pattern, getMostInnerTypeName(declaration.typeName), declaration.startPosition, declaration.endPosition, ranges);
                        }
                        if ((f && declaration.isAField()) || (m && declaration.isAMethod())) {
                            matchAndAddDocumentRange(pattern, declaration.name, declaration.startPosition, declaration.endPosition, ranges);
                        }
                    }
                }
            }

            if (highlightFlags.indexOf('r') != -1) {
                // Highlight references
                for (Map.Entry<Integer, HyperlinkData> entry : hyperlinks.entrySet()) {
                    HyperlinkData hyperlink = entry.getValue();
                    ReferenceData reference = ((HyperlinkReferenceData)hyperlink).reference;

                    if (matchScope(highlightScope, reference.owner)) {
                        if ((t && reference.isAType()) || (c && reference.isAConstructor())) {
                            matchAndAddDocumentRange(pattern, getMostInnerTypeName(reference.typeName), hyperlink.startPosition, hyperlink.endPosition, ranges);
                        }
                        if ((f && reference.isAField()) || (m && reference.isAMethod())) {
                            matchAndAddDocumentRange(pattern, reference.name, hyperlink.startPosition, hyperlink.endPosition, ranges);
                        }
                    }
                }
            }
        }
    }

    public static boolean matchScope(String scope, String type) {
        if ((scope == null) || scope.isEmpty())
            return true;
        if (scope.charAt(0) == '*')
            return type.endsWith(scope.substring(1)) || type.equals(scope.substring(2));
        return type.equals(scope);
    }

    public static void matchAndAddDocumentRange(Pattern pattern, String text, int start, int end, List<DocumentRange> ranges) {
        if (pattern.matcher(text).matches()) {
            ranges.add(new DocumentRange(start, end));
        }
    }

    public static String getMostInnerTypeName(String typeName) {
        int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1;
        int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1;
        int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex);
        return typeName.substring(lastIndex);
    }

    // --- FocusedTypeGettable --- //
    @Override public String getFocusedTypeName() {
        Map.Entry<Integer, DeclarationData> entry = typeDeclarations.floorEntry(textArea.getCaretPosition());

        if (entry != null) {
            DeclarationData data = entry.getValue();
            if (data != null) {
                return data.typeName;
            }
        }

        return null;
    }

    @Override public Container.Entry getEntry() { return entry; }

    // --- IndexesChangeListener --- //
    @Override
    public void indexesChanged(Collection<Future<Indexes>> collectionOfFutureIndexes) {
        // Update the list of containers
        this.collectionOfFutureIndexes = collectionOfFutureIndexes;
        // Refresh links
        boolean refresh = false;

        for (ReferenceData reference : references) {
            String typeName = reference.typeName;
            boolean enabled;

            if (reference.name == null) {
                enabled = false;

                try {
                    for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                        if (futureIndexes.isDone()) {
                            Map<String, Collection> index = futureIndexes.get().getIndex("typeDeclarations");
                            if ((index != null) && (index.get(typeName) != null)) {
                                enabled = true;
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            } else {
                try {
                    // Recursive search
                    typeName = searchTypeHavingMember(typeName, reference.name, reference.descriptor, entry);
                    if (typeName != null) {
                        // Replace type with the real type having the referenced member
                        reference.typeName = typeName;
                        enabled = true;
                    } else {
                        enabled = false;
                    }
                } catch (Error e) {
                    // Catch StackOverflowError or OutOfMemoryError
                    assert ExceptionUtil.printStackTrace(e);
                    enabled = false;
                }
            }

            if (reference.enabled != enabled) {
                reference.enabled = enabled;
                refresh = true;
            }
        }

        if (refresh) {
            textArea.repaint();
        }
    }

    @SuppressWarnings("unchecked")
    protected String searchTypeHavingMember(String typeName, String name, String descriptor, Container.Entry entry) {
        ArrayList<Container.Entry> entries = new ArrayList<>();

        try {
            for (Future<Indexes> futureIndexes : collectionOfFutureIndexes) {
                if (futureIndexes.isDone()) {
                    Map<String, Collection> index = futureIndexes.get().getIndex("typeDeclarations");
                    if (index != null) {
                        Collection<Container.Entry> collection = index.get(typeName);
                        if (collection != null) {
                            entries.addAll(collection);
                        }
                    }
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        String rootUri = entry.getContainer().getRoot().getUri().toString();
        ArrayList<Container.Entry> sameContainerEntries = new ArrayList<>();

        for (Container.Entry e : entries) {
            if (e.getUri().toString().startsWith(rootUri)) {
                sameContainerEntries.add(e);
            }
        }

        if (sameContainerEntries.size() > 0) {
            return searchTypeHavingMember(typeName, name, descriptor, sameContainerEntries);
        } else {
            return searchTypeHavingMember(typeName, name, descriptor, entries);
        }
    }

    protected String searchTypeHavingMember(String typeName, String name, String descriptor, List<Container.Entry> entries) {
        for (Container.Entry entry : entries) {
            Type type = api.getTypeFactory(entry).make(api, entry, typeName);

            if (type != null) {
                if (descriptor.indexOf('(') == -1) {
                    // Search a field
                    for (Type.Field field : type.getFields()) {
                        if (field.getName().equals(name) && DescriptorMatcher.matchFieldDescriptors(field.getDescriptor(), descriptor)) {
                            // Field found
                            return typeName;
                        }
                    }
                } else {
                    // Search a method
                    for (Type.Method method : type.getMethods()) {
                        if (method.getName().equals(name) && DescriptorMatcher.matchMethodDescriptors(method.getDescriptor(), descriptor)) {
                            // Method found
                            return typeName;
                        }
                    }
                }

                // Not found -> Search in super type
                String typeOwnerName = searchTypeHavingMember(type.getSuperName(), name, descriptor, entry);
                if (typeOwnerName != null) {
                    return typeOwnerName;
                }
            }
        }

        return null;
    }

    public static class StringData {
        int startPosition;
        int endPosition;
        String text;
        String owner;

        public StringData(int startPosition, int length, String text, String owner) {
            this.startPosition = startPosition;
            this.endPosition = startPosition + length;
            this.text = text;
            this.owner = owner;
        }
    }

    public static class DeclarationData {
        int startPosition;
        int endPosition;
        String typeName;
        /**
         * Field or method name or null for type
         */
        String name;
        String descriptor;

        public DeclarationData(int startPosition, int length, String typeName, String name, String descriptor) {
            this.startPosition = startPosition;
            this.endPosition = startPosition + length;
            this.typeName = typeName;
            this.name = name;
            this.descriptor = descriptor;
        }

        public boolean isAType() { return name == null; }
        public boolean isAField() { return (descriptor != null) && descriptor.charAt(0) != '('; }
        public boolean isAMethod() { return (descriptor != null) && descriptor.charAt(0) == '('; }
        public boolean isAConstructor() { return "<init>".equals(name); }
    }

    public static class HyperlinkReferenceData extends HyperlinkData {
        public ReferenceData reference;

        public HyperlinkReferenceData(int startPosition, int length, ReferenceData reference) {
            super(startPosition, startPosition+length);
            this.reference = reference;
        }
    }

    protected static class ReferenceData {
        public String typeName;
        /**
         * Field or method name or null for type
         */
        public String name;
        /**
         * Field or method descriptor or null for type
         */
        public String descriptor;
        /**
         * Internal type name containing reference or null for "import" statement.
         * Used to high light items matching with URI like "file://dir1/dir2/file?highlightPattern=hello&highlightFlags=drtcmfs&highlightScope=type".
         */
        public String owner;
        /**
         * "Enabled" flag for link of reference
         */
        public boolean enabled = false;

        public ReferenceData(String typeName, String name, String descriptor, String owner) {
            this.typeName = typeName;
            this.name = name;
            this.descriptor = descriptor;
            this.owner = owner;
        }

        boolean isAType() { return name == null; }
        boolean isAField() { return (descriptor != null) && descriptor.charAt(0) != '('; }
        boolean isAMethod() { return (descriptor != null) && descriptor.charAt(0) == '('; }
        boolean isAConstructor() { return "<init>".equals(name); }
    }
}
