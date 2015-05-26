/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.type;

import jd.gui.spi.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public abstract class AbstractTypeFactoryProvider implements TypeFactory {

    protected List<String> externalSelectors;
    protected Pattern externalPathPattern;

    /**
     * Initialize "selectors" and "pathPattern" with optional external properties file
     */
    AbstractTypeFactoryProvider() {
        Properties properties = new Properties();
        Class clazz = this.getClass();

        try (InputStream is = clazz.getClassLoader().getResourceAsStream(clazz.getName().replace('.', '/') + ".properties")) {
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException ignore) {
        }

        init(properties);
    }

    protected void init(Properties properties) {
        String selectors = properties.getProperty("selectors");
        externalSelectors = (selectors == null) ? null : Arrays.asList(selectors.split(","));

        String pathRegExp = properties.getProperty("pathRegExp");
        externalPathPattern = (pathRegExp == null) ? null : Pattern.compile(pathRegExp);
    }

    protected List<String> getExternalSelectors() { return externalSelectors; }
    protected Pattern getExternalPathPattern() { return externalPathPattern; }

    public String[] getSelectors() {
        return (externalSelectors==null) ? null : externalSelectors.toArray(new String[externalSelectors.size()]);
    }
    public Pattern getPathPattern() { return externalPathPattern; }
}
