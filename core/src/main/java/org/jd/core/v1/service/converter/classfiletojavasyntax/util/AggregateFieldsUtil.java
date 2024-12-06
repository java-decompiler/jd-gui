/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax.util;

import org.jd.core.v1.model.javasyntax.declaration.BaseFieldDeclarator;
import org.jd.core.v1.model.javasyntax.declaration.FieldDeclarators;
import org.jd.core.v1.service.converter.classfiletojavasyntax.model.javasyntax.declaration.ClassFileFieldDeclaration;

import java.util.List;

public class AggregateFieldsUtil {

    public static void aggregate(List<ClassFileFieldDeclaration> fields) {
        if (fields != null) {
            int size = fields.size();

            if (size > 1) {
                int firstIndex=0, lastIndex=0;
                ClassFileFieldDeclaration firstField = fields.get(0);

                for (int index=1; index<size; index++) {
                    ClassFileFieldDeclaration field = fields.get(index);

                    if ((firstField.getFirstLineNumber() == 0) || (firstField.getFlags() != field.getFlags()) || !firstField.getType().equals(field.getType())) {
                        firstField = field;
                        firstIndex = lastIndex = index;
                    } else {
                        int lineNumber = field.getFirstLineNumber();

                        if (lineNumber > 0) {
                            if (lineNumber == firstField.getFirstLineNumber()) {
                                // Compatible field -> Keep index
                                lastIndex = index;
                            } else {
                                // Aggregate declarators from 'firstIndex' to 'lastIndex'
                                aggregate(fields, firstField, firstIndex, lastIndex);

                                int length = lastIndex-firstIndex;
                                index -= length;
                                size -= length;

                                firstField = field;
                                firstIndex = lastIndex = index;
                            }
                        }
                    }
                }

                // Aggregate declarators from 'firstIndex' to 'lastIndex'
                aggregate(fields, firstField, firstIndex, lastIndex);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static void aggregate(List<ClassFileFieldDeclaration> fields, ClassFileFieldDeclaration firstField, int firstIndex, int lastIndex) {
        if (firstIndex < lastIndex) {
            List<ClassFileFieldDeclaration> sublist = fields.subList(firstIndex + 1, lastIndex + 1);

            int length = lastIndex - firstIndex;
            FieldDeclarators declarators = new FieldDeclarators(length);
            BaseFieldDeclarator bfd = firstField.getFieldDeclarators();

            if (bfd.isList()) {
                declarators.addAll(bfd.getList());
            } else {
                declarators.add(bfd.getFirst());
            }

            for (ClassFileFieldDeclaration f : sublist) {
                bfd = f.getFieldDeclarators();

                if (bfd.isList()) {
                    declarators.addAll(bfd.getList());
                } else {
                    declarators.add(bfd.getFirst());
                }
            }

            firstField.setFieldDeclarators(declarators);
            sublist.clear();
        }
    }
}
