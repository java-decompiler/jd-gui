/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.declaration.MemberDeclaration;
import org.jd.core.v1.model.javasyntax.declaration.MemberDeclarations;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileMemberDeclaration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MergeMembersUtil {
    protected static final MemberDeclarationComparator MEMBER_DECLARATION_COMPARATOR = new MemberDeclarationComparator();

    @SuppressWarnings("unchecked")
    public static MemberDeclarations merge(
            List<? extends ClassFileMemberDeclaration> fields,
            List<? extends ClassFileMemberDeclaration> methods,
            List<? extends ClassFileMemberDeclaration> innerTypes) {
        int size;

        if (fields != null)
            size = fields.size();
        else
            size = 0;

        if (methods != null)
            size += methods.size();

        if (innerTypes != null)
            size += innerTypes.size();

        MemberDeclarations result = new MemberDeclarations(size);

        merge(result, fields);
        merge(result, methods);
        merge(result, innerTypes);

        return result;
    }

    protected static void merge(List<MemberDeclaration> result, List<? extends ClassFileMemberDeclaration> members) {
        if ((members != null) && !members.isEmpty()) {
            sort(members);

            if (result.isEmpty()) {
                result.addAll(members);
            } else {
                int resultIndex=0, resultLength=result.size();
                int listStartIndex=0, listEndIndex=0, listLength=members.size(), listLineNumber=0;

                while (listEndIndex < listLength) {
                    // Search first line number > 0
                    while (listEndIndex < listLength) {
                        listLineNumber = members.get(listEndIndex++).getFirstLineNumber();
                        if (listLineNumber > 0)
                            break;
                    }

                    if (listLineNumber == 0) {
                        // Add end of list to result
                        result.addAll(members.subList(listStartIndex, listEndIndex));
                    } else {
                        // Search insert index in result
                        while (resultIndex < resultLength) {
                            ClassFileMemberDeclaration member = (ClassFileMemberDeclaration)result.get(resultIndex);
                            int resultLineNumber = member.getFirstLineNumber();
                            if (resultLineNumber > listLineNumber)
                                break;
                            resultIndex++;
                        }

                        // Add end of list to result
                        result.addAll(resultIndex, members.subList(listStartIndex, listEndIndex));

                        int subListLength = listEndIndex - listStartIndex;
                        resultIndex += subListLength;
                        resultLength += subListLength;
                        listStartIndex = listEndIndex;
                    }
                }
            }
        }
    }

    protected static void sort(List<? extends ClassFileMemberDeclaration> members) {
        int order = 0;
        int lastLineNumber = 0;

        // Detect order type
        for (ClassFileMemberDeclaration member : members) {
            int lineNumber = member.getFirstLineNumber();

            if ((lineNumber > 0) && (lineNumber != lastLineNumber)) {
                if (lastLineNumber > 0) {
                    if (order == 0) { // Unknown order
                        order = (lineNumber > lastLineNumber) ? 1 : 2;
                    } else if (order == 1) { // Ascendant order
                        if (lineNumber < lastLineNumber) {
                            order = 3; // Random order
                            break;
                        }
                    } else if (order == 2) { // Descendant order
                        if (lineNumber > lastLineNumber) {
                            order = 3; // Random order
                            break;
                        }
                    }
                }

                lastLineNumber = lineNumber;
            }
        }

        // Sort
        switch (order) {
            case 2: // Descendant order
                Collections.reverse(members);
                break;
            case 3: // Random order : ascendant sort and set unknown line number members at the end
                members.sort(MEMBER_DECLARATION_COMPARATOR);
                break;
        }
    }

    protected static class MemberDeclarationComparator implements Comparator<ClassFileMemberDeclaration> {
        public int compare(ClassFileMemberDeclaration md1, ClassFileMemberDeclaration md2) {
            int lineNumber1 = md1.getFirstLineNumber();
            int lineNumber2 = md2.getFirstLineNumber();

            if (lineNumber1 == 0) {
                lineNumber1 = Integer.MAX_VALUE;
            }

            if (lineNumber2 == 0) {
                lineNumber2 = Integer.MAX_VALUE;
            }

            return lineNumber1 - lineNumber2;
        }
    }
}
