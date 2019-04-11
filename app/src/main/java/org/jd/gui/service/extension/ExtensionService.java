/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.extension;

import org.jd.gui.util.exception.ExceptionUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ExtensionService {
    protected static final ExtensionService EXTENSION_SERVICE = new ExtensionService();
    protected static final UrlComparator URL_COMPARATOR = new UrlComparator();

    protected ClassLoader extensionClassLoader;

    public static ExtensionService getInstance() {
        return EXTENSION_SERVICE;
    }

    protected ExtensionService() {
        try {
            URI jarUri = ExtensionService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            File baseDirectory = new File(jarUri).getParentFile();
            File extDirectory = new File(baseDirectory, "ext");

            if (extDirectory.exists() && extDirectory.isDirectory()) {
                ArrayList<URL> urls = new ArrayList<>();

                searchJarAndMetaInf(urls, extDirectory);

                if (!urls.isEmpty()) {
                    URL[] array = urls.toArray(new URL[urls.size()]);
                    Arrays.sort(array, URL_COMPARATOR);
                    extensionClassLoader = new URLClassLoader(array, ExtensionService.class.getClassLoader());
                }
            }
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        extensionClassLoader = ExtensionService.class.getClassLoader();
    }

    protected void searchJarAndMetaInf(List<URL> urls, File directory) throws Exception {
        File metaInf = new File(directory, "META-INF");

        if (metaInf.exists() && metaInf.isDirectory()) {
            urls.add(directory.toURI().toURL());
        } else {
            for (File child : directory.listFiles()) {
                if (child.isDirectory()) {
                    searchJarAndMetaInf(urls, child);
                } else if (child.getName().toLowerCase().endsWith(".jar")) {
                    urls.add(new URL("jar", "", child.toURI().toURL().toString() + "!/"));
                }
            }
        }
    }

    public <T> Collection<T> load(Class<T> service) {
        ArrayList<T> list = new ArrayList<>();
        Iterator<T> iterator = ServiceLoader.load(service, extensionClassLoader).iterator();

        while (iterator.hasNext()) {
            list.add(iterator.next());
        }

        return list;
    }

    protected static class UrlComparator implements Comparator<URL> {
        @Override
        public int compare(URL url1, URL url2) {
            return url1.getPath().compareTo(url2.getPath());
        }
    }
}
