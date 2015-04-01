/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui

import groovy.swing.SwingBuilder
import jd.gui.service.configuration.ConfigurationPersisterService

import javax.swing.JOptionPane

import jd.gui.controller.MainController

class App {

	static void main(String[] args) {
		if (args.contains("-h")) {
			JOptionPane.showMessageDialog(null, """Usage: jd-gui [option] [input-file] ...

Option:
 -h\tshow this help message and exit""", Constants.APP_NAME)
		} else {
            // Load preferences
            def persister = ConfigurationPersisterService.instance.get()
            def configuration = persister.load()
            addShutdownHook { persister.save(configuration) }

            // Create SwingBuilder, set look and feel
            def swing = new SwingBuilder()
            swing.lookAndFeel(configuration.lookAndFeel)

            // Create main controller and show main frame
            new MainController(swing, configuration).with {
				show()
				openFiles(args.collect { new File(it) })
			}
		}
	}
}
