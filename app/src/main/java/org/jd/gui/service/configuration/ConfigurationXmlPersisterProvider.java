/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.configuration;

import org.jd.gui.Constants;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.service.platform.PlatformService;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import javax.xml.stream.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.jar.Manifest;

public class ConfigurationXmlPersisterProvider implements ConfigurationPersister {
    protected static final String ERROR_BACKGROUND_COLOR = "JdGuiPreferences.errorBackgroundColor";
    protected static final String JD_CORE_VERSION = "JdGuiPreferences.jdCoreVersion";

    protected static final File FILE = getConfigFile();

    protected static File getConfigFile() {
        String configFilePath = System.getProperty(Constants.CONFIG_FILENAME);

        if (configFilePath != null) {
            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                return configFile;
            }
        }

        if (PlatformService.getInstance().isLinux()) {
            // See: http://standards.freedesktop.org/basedir-spec/basedir-spec-latest.html
            String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfigHome != null) {
                File xdgConfigHomeFile = new File(xdgConfigHome);
                if (xdgConfigHomeFile.exists()) {
                    return new File(xdgConfigHomeFile, Constants.CONFIG_FILENAME);
                }
            }

            File userConfigFile = new File(System.getProperty("user.home"), ".config");
            if (userConfigFile.exists()) {
                return new File(userConfigFile, Constants.CONFIG_FILENAME);
            }
        } else if (PlatformService.getInstance().isWindows()) {
            // See: http://blogs.msdn.com/b/patricka/archive/2010/03/18/where-should-i-store-my-data-and-configuration-files-if-i-target-multiple-os-versions.aspx
            String roamingConfigHome = System.getenv("APPDATA");
            if (roamingConfigHome != null) {
                File roamingConfigHomeFile = new File(roamingConfigHome);
                if (roamingConfigHomeFile.exists()) {
                    return new File(roamingConfigHomeFile, Constants.CONFIG_FILENAME);
                }
            }
        }

        return new File(Constants.CONFIG_FILENAME);
    }

    @Override
    public Configuration load() {
        // Default values
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        int w = (screenSize.width>Constants.DEFAULT_WIDTH) ? Constants.DEFAULT_WIDTH : screenSize.width;
        int h = (screenSize.height>Constants.DEFAULT_HEIGHT) ? Constants.DEFAULT_HEIGHT : screenSize.height;
        int x = (screenSize.width-w)/2;
        int y = (screenSize.height-h)/2;

        Configuration config = new Configuration();
        config.setMainWindowLocation(new Point(x, y));
        config.setMainWindowSize(new Dimension(w, h));
        config.setMainWindowMaximize(false);

        String defaultLaf = System.getProperty("swing.defaultlaf");

        config.setLookAndFeel((defaultLaf != null) ? defaultLaf : UIManager.getSystemLookAndFeelClassName());

        File recentSaveDirectory = new File(System.getProperty("user.dir"));

        config.setRecentLoadDirectory(recentSaveDirectory);
        config.setRecentSaveDirectory(recentSaveDirectory);

        if (FILE.exists()) {
            try (FileInputStream fis = new FileInputStream(FILE)) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(fis);

                // Load values
                String name = "";
                Stack<String> names = new Stack<>();
                List<File> recentFiles = new ArrayList<>();
                boolean maximize = false;
                Map<String, String> preferences = config.getPreferences();

                while (reader.hasNext()) {
                    switch (reader.next()) {
                        case XMLStreamConstants.START_ELEMENT:
                            names.push(name);
                            name += '/' + reader.getLocalName();
                            switch (name) {
                                case "/configuration/gui/mainWindow/location":
                                    x = Integer.parseInt(reader.getAttributeValue(null, "x"));
                                    y = Integer.parseInt(reader.getAttributeValue(null, "y"));
                                    break;
                                case "/configuration/gui/mainWindow/size":
                                    w = Integer.parseInt(reader.getAttributeValue(null, "w"));
                                    h = Integer.parseInt(reader.getAttributeValue(null, "h"));
                                    break;
                            }
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            name = names.pop();
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            switch (name) {
                                case "/configuration/recentFilePaths/filePath":
                                    File file = new File(reader.getText().trim());
                                    if (file.exists()) {
                                        recentFiles.add(file);
                                    }
                                    break;
                                case "/configuration/recentDirectories/loadPath":
                                    file = new File(reader.getText().trim());
                                    if (file.exists()) {
                                        config.setRecentLoadDirectory(file);
                                    }
                                    break;
                                case "/configuration/recentDirectories/savePath":
                                    file = new File(reader.getText().trim());
                                    if (file.exists()) {
                                        config.setRecentSaveDirectory(file);
                                    }
                                    break;
                                case "/configuration/gui/lookAndFeel":
                                    config.setLookAndFeel(reader.getText().trim());
                                    break;
                                case "/configuration/gui/mainWindow/maximize":
                                    maximize = Boolean.parseBoolean(reader.getText().trim());
                                    break;
                                default:
                                    if (name.startsWith("/configuration/preferences/")) {
                                        String key = name.substring("/configuration/preferences/".length());
                                        preferences.put(key, reader.getText().trim());
                                    }
                                    break;
                            }
                            break;
                    }
                }

                if (recentFiles.size() > Constants.MAX_RECENT_FILES) {
                    // Truncate
                    recentFiles = recentFiles.subList(0, Constants.MAX_RECENT_FILES);
                }
                config.setRecentFiles(recentFiles);

                if ((x >= 0) && (y >= 0) && (x + w < screenSize.width) && (y + h < screenSize.height)) {
                    // Update preferences
                    config.setMainWindowLocation(new Point(x, y));
                    config.setMainWindowSize(new Dimension(w, h));
                    config.setMainWindowMaximize(maximize);
                }

                reader.close();
            } catch (Exception e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        }

        if (! config.getPreferences().containsKey(ERROR_BACKGROUND_COLOR)) {
            config.getPreferences().put(ERROR_BACKGROUND_COLOR, "0xFF6666");
        }

        config.getPreferences().put(JD_CORE_VERSION, getJdCoreVersion());

        return config;
    }

    protected String getJdCoreVersion() {
        try {
            Enumeration<URL> enumeration = ConfigurationXmlPersisterProvider.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

            while (enumeration.hasMoreElements()) {
                try (InputStream is = enumeration.nextElement().openStream()) {
                    String attribute = new Manifest(is).getMainAttributes().getValue("JD-Core-Version");
                    if (attribute != null) {
                        return attribute;
                    }
                }
            }
        } catch (IOException e) {
            assert ExceptionUtil.printStackTrace(e);
        }

        return "SNAPSHOT";
    }

    @Override
    public void save(Configuration configuration) {
        Point l = configuration.getMainWindowLocation();
        Dimension s = configuration.getMainWindowSize();

        try (FileOutputStream fos = new FileOutputStream(FILE)) {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fos);

            // Save values
            writer.writeStartDocument();
            writer.writeCharacters("\n");
            writer.writeStartElement("configuration");
            writer.writeCharacters("\n\t");

            writer.writeStartElement("gui");
            writer.writeCharacters("\n\t\t");
                writer.writeStartElement("mainWindow");
                writer.writeCharacters("\n\t\t\t");
                    writer.writeStartElement("location");
                        writer.writeAttribute("x", String.valueOf(l.x));
                        writer.writeAttribute("y", String.valueOf(l.y));
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");
                    writer.writeStartElement("size");
                        writer.writeAttribute("w", String.valueOf(s.width));
                        writer.writeAttribute("h", String.valueOf(s.height));
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");
                    writer.writeStartElement("maximize");
                        writer.writeCharacters(String.valueOf(configuration.isMainWindowMaximize()));
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t");
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement("lookAndFeel");
                    writer.writeCharacters(configuration.getLookAndFeel());
                writer.writeEndElement();
                writer.writeCharacters("\n\t");
            writer.writeEndElement();
            writer.writeCharacters("\n\t");

            writer.writeStartElement("recentFilePaths");

            for (File recentFile : configuration.getRecentFiles()) {
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement("filePath");
                    writer.writeCharacters(recentFile.getAbsolutePath());
                writer.writeEndElement();
            }

            writer.writeCharacters("\n\t");
            writer.writeEndElement();
            writer.writeCharacters("\n\t");

            writer.writeStartElement("recentDirectories");
            writer.writeCharacters("\n\t\t");
                writer.writeStartElement("loadPath");
                    writer.writeCharacters(configuration.getRecentLoadDirectory().getAbsolutePath());
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement("savePath");
                    writer.writeCharacters(configuration.getRecentSaveDirectory().getAbsolutePath());
                writer.writeEndElement();
                writer.writeCharacters("\n\t");
            writer.writeEndElement();
            writer.writeCharacters("\n\t");

            writer.writeStartElement("preferences");

            for (Map.Entry<String, String> preference : configuration.getPreferences().entrySet()) {
                writer.writeCharacters("\n\t\t");
                writer.writeStartElement(preference.getKey());
                    writer.writeCharacters(preference.getValue());
                writer.writeEndElement();
            }

            writer.writeCharacters("\n\t");
            writer.writeEndElement();
            writer.writeCharacters("\n");

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch (Exception e) {
            assert ExceptionUtil.printStackTrace(e);
        }
    }
}
