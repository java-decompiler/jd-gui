/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api.model;

import java.util.Collection;
import java.util.Map;

/**
 * Whatever the language/file format (Java|Groovy|Scala/Class|DEX, Java|Javascript/Source, C#/CIL, ...), type names,
 * stored in the indexes, use the JVM internal format (package separator = '/', inner class separator = '$').<br>
 * <br>
 * List of default indexes:
 * <ul>
 *     <li>
 *         Map "strings"<br>
 *         key: a string<br>
 *         value: a list of entries containing the string
 *     </li>
 *     <li>
 *         Map "typeDeclarations"<br>
 *         key: a type name using internal JVM internal format<br>
 *         value: a list of entries containing the type declaration
 *     </li>
 *     <li>
 *         Map "constructorDeclarations"<br>
 *         key: a type name using internal JVM internal format<br>
 *         value: a list of entries containing the constructor declaration
 *     </li>
 *     <li>
 *         Map "constructorReferences"<br>
 *         key: a type name using internal JVM internal format<br>
 *         value: a list of entries containing the constructor reference
 *     </li>
 *     <li>
 *         Map "methodDeclarations"<br>
 *         key: a method name<br>
 *         value: a list of entries containing the method declaration
 *     </li>
 *     <li>
 *         Map "methodReferences"<br>
 *         key: a method name<br>
 *         value: a list of entries containing the method reference
 *     </li>
 *     <li>
 *         Map "fieldDeclarations"<br>
 *         key: a field name<br>
 *         value: a list of entries containing the field declaration
 *     </li>
 *     <li>
 *         Map "fieldReferences"<br>
 *         key: a field name<br>
 *         value: a list of entries containing the field reference
 *     </li>
 *     <li>
 *         Map "subTypeNames"<br>
 *         key: a super type name using internal JVM internal format<br>
 *         value: a list of sub type names using internal JVM internal format
 *     </li>
 * </ul>
 */
public interface Indexes {
    Map<String, Collection> getIndex(String name);
}
