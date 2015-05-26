/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.type

import groovy.transform.CompileStatic
import jd.gui.spi.TypeFactory

import java.util.regex.Pattern

@CompileStatic
abstract class AbstractTypeFactoryProvider implements TypeFactory {

    List<String> externalSelectors
    Pattern      externalPathPattern

    /**
     * Initialize "selectors" and "pathPattern" with optional external properties file
     */
    AbstractTypeFactoryProvider() {
        def properties = new Properties()
        def clazz = this.getClass()
        def is = clazz.classLoader.getResourceAsStream(clazz.name.replace('.', '/') + '.properties')

        is?.withCloseable { properties.load(is) }
        init(properties)
    }

    protected void init(Properties properties) {
        if (properties) {
            externalSelectors = properties.getProperty('selectors')?.tokenize(',')

            String pathRegExp = properties.getProperty('pathRegExp')
            externalPathPattern = pathRegExp ? Pattern.compile(pathRegExp) : null
        } else {
            externalSelectors = null
            externalPathPattern = null
        }
    }

    String[] getSelectors() { externalSelectors?.toArray(new String[0]) }
    Pattern getPathPattern() { externalPathPattern }
}
