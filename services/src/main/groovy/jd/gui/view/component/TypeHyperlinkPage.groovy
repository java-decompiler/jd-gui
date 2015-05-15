/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view.component

import groovy.transform.CompileStatic
import org.fife.ui.rsyntaxtextarea.DocumentRange

import java.util.regex.Pattern

abstract class TypeHyperlinkPage extends HyperlinkPage {

    // --- UriOpenable --- //
    boolean openUri(URI uri) {
        java.util.List<DocumentRange> ranges = []
        def query = uri.query

        textArea.highlighter.clearMarkAllHighlights()

        if (query) {
            Map<String, String> parameters = parseQuery(query)

            if (parameters.containsKey('lineNumber')) {
                def lineNumber = parameters.get('lineNumber')
                if (lineNumber.isNumber()) {
                    goToLineNumber(lineNumber.toInteger())
                    return true
                }
            } else if (parameters.containsKey('position')) {
                def position = parameters.get('position')
                if (position.isNumber()) {
                    int pos = position.toInteger()
                    if (textArea.document.length > pos) {
                        ranges.add(new DocumentRange(pos, pos))
                    }
                }
            } else {
                def highlightFlags = parameters.get('highlightFlags')
                def highlightPattern = parameters.get('highlightPattern')

                if (highlightFlags && highlightPattern) {
                    def regexp = createRegExp(highlightPattern)

                    if (highlightFlags.indexOf('s') != -1) {
                        // Highlight strings
                        def pattern = Pattern.compile(regexp)
                        def matcher = pattern.matcher(textArea.text)

                        while (matcher.find()) {
                            ranges.add(new DocumentRange(matcher.start(), matcher.end()))
                        }
                    }

                    if ((highlightFlags.indexOf('t') != -1) && (highlightFlags.indexOf('r') != -1)) {
                        // Highlight type references
                        def pattern = Pattern.compile(regexp + '.*')

                        for (def entry : hyperlinks.entrySet()) {
                            def hyperlink = entry.value
                            def name = getMostInnerTypeName(hyperlink.internalTypeName)

                            if (pattern.matcher(name).matches()) {
                                ranges.add(new DocumentRange(hyperlink.startPosition, hyperlink.endPosition))
                            }
                        }
                    }
                }
            }
        }

        if (ranges) {
            textArea.markAllHighlightColor = selectHighlightColor
            textArea.markAll(ranges)
            setCaretPositionAndCenter(ranges.sort().get(0))
        }
    }

    @CompileStatic
    String getMostInnerTypeName(String typeName) {
        int lastPackageSeparatorIndex = typeName.lastIndexOf('/') + 1
        int lastTypeNameSeparatorIndex = typeName.lastIndexOf('$') + 1
        int lastIndex = Math.max(lastPackageSeparatorIndex, lastTypeNameSeparatorIndex)
        return typeName.substring(lastIndex)
    }

    static class TypeHyperlinkData extends HyperlinkPage.HyperlinkData {
        boolean enabled
        String internalTypeName

        TypeHyperlinkData(int startPosition, int endPosition, String internalTypeName) {
            super(startPosition, endPosition)
            this.enabled = false
            this.internalTypeName = internalTypeName
        }
    }
}
