/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.extension

import groovy.transform.CompileStatic

@Singleton
@CompileStatic
class ExtensionService {
    protected ClassLoader extensionClassLoader = initClassLoader()

    protected ClassLoader initClassLoader() {
        def extDirectory = new File("ext");

        if (extDirectory.exists() && extDirectory.isDirectory()) {
            List<URL> urls = []

            searchJarAndMetaInf(urls, extDirectory)

            if (! urls.isEmpty()) {
                URL[] array = urls.sort { u1, u2 -> u1.path.compareTo(u2.path) }
                return new URLClassLoader(array, ExtensionService.class.classLoader)
            }
        }

        return ExtensionService.class.classLoader
    }

    protected void searchJarAndMetaInf(List<URL> urls, File directory) {
        def metaInf = new File(directory, 'META-INF')

        if (metaInf.exists() && metaInf.isDirectory()) {
            urls.add(directory.toURI().toURL())
        } else {
            directory.eachFile { File child ->
                if (child.isDirectory()) {
                    searchJarAndMetaInf(urls, child)
                } else if (child.name.toLowerCase().endsWith('.jar')) {
                    urls.add(new URL('jar', '', child.toURI().toURL().toString() + '!/'))
                }
            }
        }
    }

    public <T> Collection<T> load(Class<T> service) {
        return ServiceLoader.load(service, extensionClassLoader).toList()
    }
}
