/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.sourcesaver;

import groovy.transform.CompileStatic;
import jd.core.CoreConstants;
import jd.core.Decompiler;
import jd.core.process.DecompilerImpl;
import jd.gui.api.API;
import jd.gui.api.model.Container;
import jd.gui.util.decompiler.ContainerLoader;
import jd.gui.util.decompiler.GuiPreferences;
import jd.gui.spi.SourceSaver;
import jd.gui.util.decompiler.PlainTextPrinter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

public class ClassFileSourceSaverProvider implements SourceSaver {
    protected static final String ESCAPE_UNICODE_CHARACTERS = "ClassFileSaverPreferences.escapeUnicodeCharacters";
    protected static final String OMIT_THIS_PREFIX = "ClassFileSaverPreferences.omitThisPrefix";
    protected static final String WRITE_DEFAULT_CONSTRUCTOR = "ClassFileSaverPreferences.writeDefaultConstructor";
    protected static final String REALIGN_LINE_NUMBERS = "ClassFileSaverPreferences.realignLineNumbers";
    protected static final String WRITE_LINE_NUMBERS = "ClassFileSaverPreferences.writeLineNumbers";
    protected static final String WRITE_METADATA = "ClassFileSaverPreferences.writeMetadata";

    protected static final String[] TYPES = new String[] { "*:file:*.class" };
    protected static final Decompiler DECOMPILER = new DecompilerImpl();

    protected GuiPreferences preferences = new GuiPreferences();
    protected ContainerLoader loader = new ContainerLoader();
    protected PlainTextPrinter printer = new PlainTextPrinter();
    protected ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public String[] getTypes() { return TYPES; }

    public Pattern getPathPattern() { return null; }

    public String getSourcePath(Container.Entry entry) {
        String path = entry.getPath();
        return path.substring(0, path.length()-6) + ".java";
    }

    public int getFileCount(API api, Container.Entry entry) {
        if (entry.getPath().indexOf('$') == -1) {
            return 1;
        } else {
            return 0;
        }
    }

    public void save(API api, Controller controller, Listener listener, Path path, Container.Entry entry) {
        try {
            // Call listener
            if (path.toString().indexOf('$') == -1) {
                listener.pathSaved(path);
            }
            // Init preferences
            Map<String, String> p = api.getPreferences();
            preferences.setUnicodeEscape(getPreferenceValue(p, ESCAPE_UNICODE_CHARACTERS, false));
            preferences.setShowPrefixThis(! getPreferenceValue(p, OMIT_THIS_PREFIX, false));
            preferences.setShowDefaultConstructor(getPreferenceValue(p, WRITE_DEFAULT_CONSTRUCTOR, false));
            preferences.setRealignmentLineNumber(getPreferenceValue(p, REALIGN_LINE_NUMBERS, true));
            preferences.setShowLineNumbers(getPreferenceValue(p, WRITE_LINE_NUMBERS, true));

            // Init loader
            loader.setEntry(entry);

            // Init printer
            baos.reset();
            PrintStream ps = new PrintStream(baos);
            printer.setPrintStream(ps);
            printer.setPreferences(preferences);

            // Decompile class file
            DECOMPILER.decompile(preferences, loader, printer, entry.getPath());

            // Metadata
            if (getPreferenceValue(p, WRITE_METADATA, true)) {
                // Add location
                ps.print("\n\n/* Location:              ");
                ps.print(new File(entry.getUri()).getPath());
                // Add Java compiler version
                int majorVersion = printer.getMajorVersion();

                if (majorVersion >= 45) {
                    ps.print("\n * Java compiler version: ");

                    if (majorVersion >= 49) {
                        ps.print(majorVersion - (49 - 5));
                    } else {
                        ps.print(majorVersion - (45 - 1));
                    }

                    ps.print(" (");
                    ps.print(majorVersion);
                    ps.print('.');
                    ps.print(printer.getMinorVersion());
                    ps.print(')');
                }
                // Add JD-Core version
                ps.print("\n * JD-Core Version:       ");
                ps.print(CoreConstants.JD_CORE_VERSION);
                ps.print("\n */");
            }

            try (OutputStream os = Files.newOutputStream(path)) {
                baos.writeTo(os);
            } catch (IOException ignore) {
            }
        } catch (Exception ignore) {
            try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
                writer.write("// INTERNAL ERROR //");
            } catch (IOException ignoreAgain) {
            }
        }
    }

    @CompileStatic
    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String v = preferences.get(key);

        if (v == null) {
            return defaultValue;
        } else {
            return Boolean.valueOf(v);
        }
    }
}
