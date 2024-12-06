/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.compiler;

import javax.tools.*;
import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class CompilerUtil {
    protected static final File DESTINATION_DIRECTORY = new File("build/test-recompiled");
    protected static final String DESTINATION_DIRECTORY_PATH = DESTINATION_DIRECTORY.getAbsolutePath();

    public static boolean compile(String preferredJavaVersion, JavaFileObject... javaFileObjects) throws Exception {
        boolean compilationSuccess = false;
        String javaVersion = getJavaVersion(preferredJavaVersion);

        DESTINATION_DIRECTORY.mkdirs();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StringWriter writer = new StringWriter();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        List<String> options = Arrays.asList("-source", javaVersion, "-target", javaVersion, "-d", DESTINATION_DIRECTORY_PATH, "-cp", System.getProperty("java.class.path"));
        List<JavaFileObject> compilationUnits = Arrays.asList(javaFileObjects);

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null)) {
            compilationSuccess = compiler.getTask(writer, fileManager, diagnostics, options, null, compilationUnits).call();

            if (!diagnostics.getDiagnostics().isEmpty()) {
                StringBuilder sb = new StringBuilder();

                for (Diagnostic d : diagnostics.getDiagnostics()) {
                    switch (d.getKind()) {
                        case NOTE:
                        case WARNING:
                            break;
                        default:
                            if (d.getLineNumber() > 0) {
                                sb.append(String.format("%-7s - line %-4d- %s%n", d.getKind(), d.getLineNumber(), d.getMessage(null)));
                            } else {
                                sb.append(String.format("%-7s -          - %s%n", d.getKind(), d.getMessage(null)));
                            }
                            break;
                    }
                }

                if (sb.length() > 0) {
                    System.err.println(compilationUnits.get(0).getName());
                    System.err.print(sb.toString());
                }
            }
        }

        return compilationSuccess;
    }

    private static String getJavaVersion(String preferredJavaVersion) {
        int numericSystemJavaVersion = parseJavaVersion(System.getProperty("java.version"));

        if (numericSystemJavaVersion <= 8) {
            return preferredJavaVersion;
        } else {
            int numericPreferredJavaVersion = parseJavaVersion(preferredJavaVersion);

            if (numericPreferredJavaVersion < 6) {
                return "1.6";
            } else {
                return preferredJavaVersion;
            }
        }
    }

    private static int parseJavaVersion(String javaVersion) {
        if(javaVersion.startsWith("1.")) {
            javaVersion = javaVersion.substring(2, 3);
        } else {
            int index = javaVersion.indexOf(".");

            if(index != -1) {
                javaVersion = javaVersion.substring(0, index);
            }
        }

        return Integer.parseInt(javaVersion);
    }
}
