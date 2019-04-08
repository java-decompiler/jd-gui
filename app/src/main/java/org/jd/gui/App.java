/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui;

import org.jd.gui.controller.MainController;
import org.jd.gui.model.configuration.Configuration;
import org.jd.gui.service.configuration.ConfigurationPersister;
import org.jd.gui.service.configuration.ConfigurationPersisterService;
import org.jd.gui.util.exception.ExceptionUtil;
import org.jd.gui.util.net.InterProcessCommunicationUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
    protected static final String SINGLE_INSTANCE = "UIMainWindowPreferencesProvider.singleInstance";

    protected static MainController controller;

    public static void main(String[] args) {
		if (checkHelpFlag(args)) {
			JOptionPane.showMessageDialog(null, "Usage: jd-gui [option] [input-file] ...\n\nOption:\n -h Show this help message and exit", Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
		} else {
            // Load preferences
            ConfigurationPersister persister = ConfigurationPersisterService.getInstance().get();
            Configuration configuration = persister.load();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> persister.save(configuration)));

            if ("true".equals(configuration.getPreferences().get(SINGLE_INSTANCE))) {
                InterProcessCommunicationUtil ipc = new InterProcessCommunicationUtil();
                try {
                    ipc.listen(receivedArgs -> controller.openFiles(newList(receivedArgs)));
                } catch (Exception notTheFirstInstanceException) {
                    // Send args to main windows and exit
                    ipc.send(args);
                    System.exit(0);
                }
            }

            // Create SwingBuilder, set look and feel
            try {
                UIManager.setLookAndFeel(configuration.getLookAndFeel());
            } catch (Exception e) {
                configuration.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                try {
                    UIManager.setLookAndFeel(configuration.getLookAndFeel());
                } catch (Exception ee) {
                    assert ExceptionUtil.printStackTrace(ee);
                }
           }

            // Create main controller and show main frame
            controller = new MainController(configuration);
            controller.show(newList(args));
		}
	}

    protected static boolean checkHelpFlag(String[] args) {
        if (args != null) {
            for (String arg : args) {
                if ("-h".equals(arg)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static List<File> newList(String[] paths) {
        if (paths == null) {
            return Collections.emptyList();
        } else {
            ArrayList<File> files = new ArrayList<>(paths.length);
            for (String path : paths) {
                files.add(new File(path));
            }
            return files;
        }
    }
}
