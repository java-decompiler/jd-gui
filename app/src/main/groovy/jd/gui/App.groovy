/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui

import groovy.swing.SwingBuilder
import jd.gui.service.configuration.ConfigurationPersisterService
import jd.gui.util.net.InterProcessCommunications

import javax.swing.JOptionPane

import jd.gui.controller.MainController

class App {
    static final String SINGLE_INSTANCE = 'UIMainWindowPreferencesProvider.singleInstance'

    static MainController controller

	static void main(String[] args) {
		if (args.contains("-h")) {
			JOptionPane.showMessageDialog(null, "Usage: jd-gui [option] [input-file] ...\n\nOption:\n -h Show this help message and exit", Constants.APP_NAME, JOptionPane.INFORMATION_MESSAGE)
		} else {
            // Load preferences
            def persister = ConfigurationPersisterService.instance.get()
            def configuration = persister.load()
            addShutdownHook { persister.save(configuration) }

            if ('true'.equals(configuration.preferences.get(SINGLE_INSTANCE))) {
                def ipc = new InterProcessCommunications()

                try {
                    ipc.listen { String[] receivedArgs ->
                        controller.openFiles(receivedArgs.collect { new File(it) })
                    }
                } catch (Exception notTheFirstInstanceException) {
                    // Send args to main windows and exit
                    ipc.send(args)
                    System.exit(0)
                }
            }

            // Create SwingBuilder, set look and feel
            def swing = new SwingBuilder()
            swing.lookAndFeel(configuration.lookAndFeel)

            // Create main controller and show main frame
            controller = new MainController(swing, configuration)
            controller.show()
            controller.openFiles(args.collect { new File(it) })
		}
	}
}
