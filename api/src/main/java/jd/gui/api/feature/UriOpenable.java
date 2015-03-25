/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.api.feature;

import java.net.URI;

/**
 * uri          = [scheme:][//authority][path][?query][#fragment]<br>
 * scheme       = generic | jar | war | ear | dex | ...<br>
 * authority    = ''<br>
 * path         = path/to/dir/ | path/to/file<br>
 * query        = '' | highlight=text<br>
 * fragment     = '' | type | innertype | lineNumber=... | highlightPattern=...&highlightFlags=[drtcmfs]&highlightScope=...<br>
 * <br>
 * Examples:
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
 *  <li>jar://dir1/dir2/javafile#innertype-methodName-(?)?</li>
 *  <li>jar://dir1/dir2/javafile#innertype-fieldName-descriptor</li>
 *  <li>jar://dir1/dir2/javafile#innertype-methodName-descriptor</li>
 *
 *  <li>file://dir1/dir2/file?lineNumber=number</li>
 *  <li>file://dir1/dir2/file?position=numeric</li>
 *  <li>file://dir1/dir2/file?highlightPattern=hello&highlightFlags=drtcmfs&highlightScope=internalTypeName</li>
 * </ul>
 */
public interface UriOpenable {
    public boolean openUri(URI uri);
}
