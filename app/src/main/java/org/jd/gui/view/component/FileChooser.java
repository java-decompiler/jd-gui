package org.jd.gui.view.component;

import org.jd.gui.util.io.FileUtils;
import org.jd.gui.util.sys.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by jianhua.fengjh on 27/11/2015.
 */
public class FileChooser extends JFileChooser {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int showOpenDialog(Component parent) {
        if (!SystemUtils.isMacOS()) {
            return super.showOpenDialog(parent);
        } else {
            setDialogType(JFileChooser.OPEN_DIALOG);
            return showNativeFileDialog(this);
        }
    }

    public int showSaveDialog(Component parent) {

        if (!SystemUtils.isMacOS()) {
            return super.showSaveDialog(parent);
        } else {
            setDialogType(JFileChooser.SAVE_DIALOG);
            return showNativeFileDialog(this);
        }
    }

    private static int showNativeFileDialog(final JFileChooser chooser) {
        if (chooser != null) {

            FileDialog fileDialog = new FileDialog((Frame) chooser.getParent());
            fileDialog.setDirectory(chooser.getCurrentDirectory().getPath());
            File file = chooser.getSelectedFile();

            if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
                fileDialog.setFile(file != null ? file.getName() : ""); //save only need name
            } else {
                fileDialog.setFile(file != null ? file.getPath() : "");
            }

            fileDialog.setFilenameFilter(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    String path = dir.getPath();
                    String pathSeparator = File.pathSeparator;
                    return chooser.getFileFilter().accept(new File(0 + path.length() + pathSeparator.length() + name.length() + path + pathSeparator + name));
                }

            });

            if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
                fileDialog.setMode(FileDialog.SAVE);
            } else {
                fileDialog.setMode(FileDialog.LOAD);
            }

            if (chooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
                System.setProperty("apple.awt.fileDialogForDirectories", "true");
            } else {
                System.setProperty("apple.awt.fileDialogForDirectories", "false");
            }

            fileDialog.setVisible(true);

            //reset fileDialogForDirectories property
            System.setProperty("apple.awt.fileDialogForDirectories", "false");
            if (fileDialog.getFile() == null) {
                return JFileChooser.CANCEL_OPTION;
            }

            String dir = fileDialog.getDirectory();
            String trailingSlash = FileUtils.ensureTrailingSlash(dir);
            String strFile = fileDialog.getFile();
            chooser.setSelectedFile(new File(strFile.length() != 0 ? trailingSlash.concat(strFile) : trailingSlash));

            return JFileChooser.APPROVE_OPTION;
        }

        return JFileChooser.ERROR_OPTION;
    }

}
