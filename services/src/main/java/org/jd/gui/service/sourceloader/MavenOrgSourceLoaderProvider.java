/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourceloader;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.service.preferencespanel.MavenOrgSourceLoaderPreferencesProvider;
import org.jd.gui.spi.PreferencesPanel;
import org.jd.gui.spi.SourceLoader;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MavenOrgSourceLoaderProvider implements SourceLoader {
    protected static final String MAVENORG_SEARCH_URL_PREFIX = "https://search.maven.org/solrsearch/select?q=1:%22";
    protected static final String MAVENORG_SEARCH_URL_SUFFIX = "%22&rows=20&wt=xml";

    protected static final String MAVENORG_LOAD_URL_PREFIX = "https://search.maven.org/classic/remotecontent?filepath=";
    protected static final String MAVENORG_LOAD_URL_SUFFIX = "-sources.jar";

    protected HashSet<Container.Entry> failed = new HashSet<>();
    protected HashMap<Container.Entry, File> cache = new HashMap<>();

    @Override
    public String getSource(API api, Container.Entry entry) {
        if (isActivated(api)) {
            String filters = api.getPreferences().get(MavenOrgSourceLoaderPreferencesProvider.FILTERS);

            if ((filters == null) || filters.isEmpty()) {
                filters = MavenOrgSourceLoaderPreferencesProvider.DEFAULT_FILTERS_VALUE;
            }

            if (accepted(filters, entry.getPath())) {
                return search(entry, cache.get(entry.getContainer().getRoot().getParent()));
            }
        }

        return null;
    }

    @Override
    public String loadSource(API api, Container.Entry entry) {
        boolean activated = !"false".equals(api.getPreferences().get(MavenOrgSourceLoaderPreferencesProvider.ACTIVATED));

        if (isActivated(api)) {
            String filters = api.getPreferences().get(MavenOrgSourceLoaderPreferencesProvider.FILTERS);

            if ((filters == null) || filters.isEmpty()) {
                filters = MavenOrgSourceLoaderPreferencesProvider.DEFAULT_FILTERS_VALUE;
            }

            if (accepted(filters, entry.getPath())) {
                return search(entry, load(entry.getContainer().getRoot().getParent()));
            }
        }

        return null;
    }

    @Override
    public File loadSourceFile(API api, Container.Entry entry) {
        return isActivated(api) ? load(entry) : null;
    }

    private static boolean isActivated(API api) {
        return !"false".equals(api.getPreferences().get(MavenOrgSourceLoaderPreferencesProvider.ACTIVATED));
    }

    protected String search(Container.Entry entry, File file) {
        if (file != null) {
            String fileName = file.toString().toLowerCase();

            if (fileName.endsWith(".zip") || fileName.endsWith(".jar")) {
                byte[] buffer = new byte[1024 * 2];

                try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                    ZipEntry ze = zis.getNextEntry();
                    String name = entry.getPath();

                    name = name.substring(0, name.length()-6) + ".java"; // 6 = ".class".length()

                    while (ze != null) {
                        if (ze.getName().equals(name)) {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            int read = zis.read(buffer);

                            while (read > 0) {
                                out.write(buffer, 0, read);
                                read = zis.read(buffer);
                            }

                            return new String(out.toByteArray(), "UTF-8");
                        }

                        ze = zis.getNextEntry();
                    }

                    zis.closeEntry();
                } catch (IOException e) {
                    assert ExceptionUtil.printStackTrace(e);
                }
            }
        }

        return null;
    }

    protected File load(Container.Entry entry) {
        if (cache.containsKey(entry)) {
            return cache.get(entry);
        }

        if (!entry.isDirectory() && !failed.contains(entry)) {
            try {
                // SHA-1
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
                byte[] buffer = new byte[1024 * 2];

                try (DigestInputStream is = new DigestInputStream(entry.getInputStream(), messageDigest)) {
                    while (is.read(buffer) > -1);
                }

                byte[] array = messageDigest.digest();
                StringBuilder sb = new StringBuilder();

                for (byte b : array) {
                    sb.append(hexa((b & 255) >> 4));
                    sb.append(hexa(b & 15));
                }

                String sha1 = sb.toString();

                // Search artifact on maven.org
                URL searchUrl = new URL(MAVENORG_SEARCH_URL_PREFIX + sha1 + MAVENORG_SEARCH_URL_SUFFIX);
                boolean sourceAvailable = false;
                String id = null;

                try (InputStream is = searchUrl.openStream()) {
                    XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
                    String name = "";

                    while (reader.hasNext()) {
                        switch (reader.next()) {
                            case XMLStreamConstants.START_ELEMENT:
                                if ("str".equals(reader.getLocalName())) {
                                    if ("id".equals(reader.getAttributeValue(null, "name"))) {
                                        name = "id";
                                    } else {
                                        name = "str";
                                    }
                                } else {
                                    name = "";
                                }
                                break;
                            case XMLStreamConstants.CHARACTERS:
                                switch (name) {
                                    case "id":
                                        id = reader.getText().trim();
                                        break;
                                    case "str":
                                        sourceAvailable |= "-sources.jar".equals(reader.getText().trim());
                                        break;
                                }
                                break;
                        }
                    }

                    reader.close();
                }

                if (sourceAvailable == false) {
                    failed.add(entry);
                } else {
                    // Load source
                    int index1 = id.indexOf(':');
                    int index2 = id.lastIndexOf(':');
                    String groupId = id.substring(0, index1);
                    String artifactId = id.substring(index1+1, index2);
                    String version = id.substring(index2+1);
                    String filePath = groupId.replace('.', '/') + '/' + artifactId + '/' + version + '/' + artifactId + '-' + version;
                    URL loadUrl = new URL(MAVENORG_LOAD_URL_PREFIX + filePath + MAVENORG_LOAD_URL_SUFFIX);
                    File tmpFile = File.createTempFile("jd-gui.tmp.", '.' + artifactId + '-' + version + "-sources.jar");

                    tmpFile.delete();
                    tmpFile.deleteOnExit();

                    try (InputStream is = new BufferedInputStream(loadUrl.openStream()); OutputStream os = new BufferedOutputStream(new FileOutputStream(tmpFile))) {
                        int readed = is.read(buffer);
                        while (readed > 0) {
                            os.write(buffer, 0, readed);
                            readed = is.read(buffer);
                        }
                    }

                    cache.put(entry, tmpFile);
                    return tmpFile;
                }
            } catch (Exception e) {
                assert ExceptionUtil.printStackTrace(e);
                failed.add(entry);
            }
        }

        return null;
    }

    private static char hexa(int i) { return (char)( (i <= 9) ? ('0' + i) : (('a' - 10) + i) ); }

    protected boolean accepted(String filters, String path) {
        // 'filters' example : '+org +com.google +com.ibm +com.jcraft +com.springsource +com.sun -com +java +javax +sun +sunw'
        StringTokenizer tokenizer = new StringTokenizer(filters);

        while (tokenizer.hasMoreTokens()) {
            String filter = tokenizer.nextToken();

            if (filter.length() > 1) {
                String prefix = filter.substring(1).replace('.', '/');

                if (prefix.charAt(prefix.length() - 1) != '/') {
                    prefix += '/';
                }

                if (path.startsWith(prefix)) {
                    return (filter.charAt(0) == '+');
                }
            }
        }

        return false;
    }
}
