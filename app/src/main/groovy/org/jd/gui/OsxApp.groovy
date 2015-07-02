package org.jd.gui

import com.apple.eawt.AppEvent
import com.apple.eawt.Application
import com.apple.eawt.OpenFilesHandler
import com.apple.eawt.QuitHandler
import com.apple.eawt.QuitResponse

class OsxApp extends App {

    static void main(String[] args) {
        // Create an instance of the mac OSX Application class
        def application = new Application()

        App.main(args)

        // Add an handle invoked when the application is asked to open a list of files
        application.setOpenFileHandler(new OpenFilesHandler() {
            void openFiles(AppEvent.OpenFilesEvent e) {
                controller.openFiles(e.files)
            }
        })

        // Add an handle invoked when the application is asked to quit
        application.setQuitHandler(new QuitHandler() {
            void handleQuitRequestWith(AppEvent.QuitEvent e, QuitResponse response) {
                System.exit(0)
            }
        })
    }
}
