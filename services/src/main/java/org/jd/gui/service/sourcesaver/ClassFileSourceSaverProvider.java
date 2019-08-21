/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.sourcesaver;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.decompiler.ContainerLoader;
import org.jd.gui.util.decompiler.LineNumberStringBuilderPrinter;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.io.NewlineOutputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ClassFileSourceSaverProvider extends AbstractSourceSaverProvider {
    protected static final String ESCAPE_UNICODE_CHARACTERS = "ClassFileDecompilerPreferences.escapeUnicodeCharacters";
    protected static final String REALIGN_LINE_NUMBERS      = "ClassFileDecompilerPreferences.realignLineNumbers";
    protected static final String WRITE_LINE_NUMBERS        = "ClassFileSaverPreferences.writeLineNumbers";
    protected static final String WRITE_METADATA            = "ClassFileSaverPreferences.writeMetadata";
    protected static final String JD_CORE_VERSION           = "JdGuiPreferences.jdCoreVersion";

    protected static final ClassFileToJavaSourceDecompiler DECOMPILER = new ClassFileToJavaSourceDecompiler();

    protected ContainerLoader loader = new ContainerLoader();
    protected LineNumberStringBuilderPrinter printer = new LineNumberStringBuilderPrinter();

    @Override public String[] getSelectors() { return appendSelectors("*:file:*.class"); }

    @Override
    public String getSourcePath(Container.Entry entry) {
        String path = entry.getPath();
        int index = path.lastIndexOf('.');
        String prefix = (index == -1) ? path : path.substring(0, index);
        return prefix + ".java";
    }

    @Override
    public int getFileCount(API api, Container.Entry entry) {
        if (entry.getPath().indexOf('$') == -1) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void save(API api, Controller controller, Listener listener, Path rootPath, Container.Entry entry) {
        String sourcePath = getSourcePath(entry);
        Path path = rootPath.resolve(sourcePath);

        saveContent(api, controller, listener, rootPath, path, entry);
    }

    @Override
    public void saveContent(API api, Controller controller, Listener listener, Path rootPath, Path path, Container.Entry entry) {
        try {
            // Call listener
            if (path.toString().indexOf('$') == -1) {
                listener.pathSaved(path);
            }
            // Init preferences
            Map<String, String> preferences = api.getPreferences();
            boolean realignmentLineNumbers = getPreferenceValue(preferences, REALIGN_LINE_NUMBERS, true);
            boolean unicodeEscape = getPreferenceValue(preferences, ESCAPE_UNICODE_CHARACTERS, false);
            boolean showLineNumbers = getPreferenceValue(preferences, WRITE_LINE_NUMBERS, true);

            Map<String, Object> configuration = new HashMap<>();
            configuration.put("realignLineNumbers", realignmentLineNumbers);

            // Init loader
            loader.setEntry(entry);

            // Init printer
            printer.setRealignmentLineNumber(realignmentLineNumbers);
            printer.setUnicodeEscape(unicodeEscape);
            printer.setShowLineNumbers(showLineNumbers);

            // Format internal name
            String entryPath = entry.getPath();
            assert entryPath.endsWith(".class");
            String entryInternalName = entryPath.substring(0, entryPath.length() - 6); // 6 = ".class".length()

            // Decompile class file
            DECOMPILER.decompile(loader, printer, entryInternalName, configuration);

            StringBuilder stringBuffer = printer.getStringBuffer();

            // Metadata
            if (getPreferenceValue(preferences, WRITE_METADATA, true)) {
                // Add location
                String location =
                    new File(entry.getUri()).getPath()
                    // Escape "\ u" sequence to prevent "Invalid unicode" errors
                    .replaceAll("(^|[^\\\\])\\\\u", "\\\\\\\\u");
                stringBuffer.append("\n\n/* Location:              ");
                stringBuffer.append(location);
                // Add Java compiler version
                int majorVersion = printer.getMajorVersion();

                if (majorVersion >= 45) {
                    stringBuffer.append("\n * Java compiler version: ");

                    if (majorVersion >= 49) {
                        stringBuffer.append(majorVersion - (49 - 5));
                    } else {
                        stringBuffer.append(majorVersion - (45 - 1));
                    }

                    stringBuffer.append(" (");
                    stringBuffer.append(majorVersion);
                    stringBuffer.append('.');
                    stringBuffer.append(printer.getMinorVersion());
                    stringBuffer.append(')');
                }
                // Add JD-Core version
                stringBuffer.append("\n * JD-Core Version:       ");
                stringBuffer.append(preferences.get(JD_CORE_VERSION));
                stringBuffer.append("\n */");
            }

            try (PrintStream ps = new PrintStream(new NewlineOutputStream(Files.newOutputStream(path)), true, "UTF-8")) {
                ps.print(stringBuffer.toString());
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }
        } catch (Throwable t) {
            assert ExceptionUtil.printStackTrace(t);

            try (BufferedWriter writer = Files.newBufferedWriter(path, Charset.defaultCharset())) {
                writer.write("// INTERNAL ERROR //");
            } catch (IOException ee) {
                assert ExceptionUtil.printStackTrace(ee);
            }
        }
    }

    protected static boolean getPreferenceValue(Map<String, String> preferences, String key, boolean defaultValue) {
        String v = preferences.get(key);
        return (v == null) ? defaultValue : Boolean.valueOf(v);
    }
}
