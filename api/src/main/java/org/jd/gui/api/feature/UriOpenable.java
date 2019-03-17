/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.api.feature;

import java.net.URI;

/**
 * uri                : scheme '://' path ('?' query)? ('#' fragment)?<br>
 * scheme             : 'generic' | 'jar' | 'war' | 'ear' | 'dex' | ...<br>
 * path               : singlePath('!' singlePath)*<br>
 * singlePath         : [path/to/dir/] | [path/to/file]<br>
 * query              : queryLineNumber | queryPosition | querySearch<br>
 * queryLineNumber    : 'lineNumber=' [numeric]<br>
 * queryPosition      : 'position=' [numeric]<br>
 * querySearch        : 'highlightPattern=' queryPattern '&highlightFlags=' queryFlags ('&highlightScope=' typeName)?<br>
 * queryPattern       : [start of string] | [start of type name] | [start of field name] | [start of method name]<br>
 * queryFlags         : 'd'? // Match declarations<br>
 *                      'r'? // Match references<br>
 *                      't'? // Match types<br>
 *                      'c'? // Match constructors<br>
 *                      'm'? // Match methods<br>
 *                      'f'? // Match fields<br>
 *                      's'? // Match strings<br>
 * fragment            : fragmentType | fragmentField | fragmentMethod<br>
 * fragmentType        : typeName<br>
 * fragmentField       : typeName '-' [field name] '-' descriptor<br>
 * fragmentMethod      : typeName '-' [method name] '-' methodDescriptor<br>
 * methodDescriptor    : '(*)?' | // Match all method descriptors<br>
 *                       '(' descriptor* ')' descriptor<br>
 * descriptor          : '?' | // Match a primitive or a type name<br>
 *                       '['* primitiveOrTypeName<br>
 * primitiveOrTypeName : 'B' | 'C' | 'D' | 'F' | 'I' | 'J' | 'L' typeName ';' | 'S' | 'Z'<br>
 * typeName            : [internal qualified name] | '*\/' [name]<br>
 * <br>
 * Examples:<br>
 * <ul>
 *  <li>file://dir1/dir2/</li>
 *  <li>file://dir1/dir2/file</li>
 *  <li>jar://dir1/dir2/</li>
 *  <li>jar://dir1/dir2/file</li>
 *
 *  <li>jar://dir1/dir2/javafile</li>
 *  <li>jar://dir1/dir2/javafile#type</li>
 *  <li>jar://dir1/dir2/javafile#type-fieldName-descriptor</li>
 *  <li>jar://dir1/dir2/javafile#type-methodName-descriptor</li>
 *  <li>jar://dir1/dir2/javafile#innertype</li>
 *  <li>jar://dir1/dir2/javafile#innertype-fieldName-?</li>
 *  <li>jar://dir1/dir2/javafile#innertype-methodName-(*)?</li>
 *  <li>jar://dir1/dir2/javafile#innertype-methodName-(?JZLjava/lang/Sting;C)I</li>
 *  <li>jar://dir1/dir2/javafile#innertype-fieldName-descriptor</li>
 *  <li>jar://dir1/dir2/javafile#innertype-methodName-descriptor</li>
 *
 *  <li>file://dir1/dir2/file?lineNumber=numeric</li>
 *  <li>file://dir1/dir2/file?position=numeric</li>
 *  <li>file://dir1/dir2/file?highlightPattern=hello&highlightFlags=drtcmfs&highlightScope=java/lang/String</li>
 *  <li>file://dir1/dir2/file?highlightPattern=hello&highlightFlags=drtcmfs&highlightScope=*\/String</li>
 * </ul>
 */
public interface UriOpenable {
    boolean openUri(URI uri);
}
