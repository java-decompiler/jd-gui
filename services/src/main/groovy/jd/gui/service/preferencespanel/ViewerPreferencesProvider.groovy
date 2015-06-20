/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.preferencespanel

import jd.gui.spi.PreferencesPanel
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.Theme

import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.*

class ViewerPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener {
    static final int MIN_VALUE = 2
    static final int MAX_VALUE = 40
    static final String FONT_SIZE_KEY = 'ViewerPreferences.fontSize'

    PreferencesPanel.PreferencesPanelChangeListener listener = null
    JTextField fontSizeTextField
    Color errorBackgroundColor = Color.RED
    Color defaultBackgroundColor

    ViewerPreferencesProvider() {
        super(new BorderLayout())

        add(new JLabel('Font size (' + MIN_VALUE + '..' + MAX_VALUE + '): '), BorderLayout.WEST)

        fontSizeTextField = new JTextField()
        fontSizeTextField.document.addDocumentListener(this)
        add(fontSizeTextField, BorderLayout.CENTER)

        defaultBackgroundColor = fontSizeTextField.background
    }

    // --- PreferencesPanel --- //
    String getPreferencesGroupTitle() { 'Viewer' }
    String getPreferencesPanelTitle() { 'Appearance' }

    public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor
    }

    public boolean isActivated() { true }

    void loadPreferences(Map<String, String> preferences) {
        def fontSize = preferences.get(FONT_SIZE_KEY)

        if (! fontSize) {
            // Search default value for the current platform
            def textArea = new RSyntaxTextArea()

            def theme = Theme.load(getClass().classLoader.getResourceAsStream('rsyntaxtextarea/themes/eclipse.xml'))
            theme.apply(textArea)

            fontSize = textArea.font.size
        }

        fontSizeTextField.text = fontSize
        fontSizeTextField.setCaretPosition(fontSizeTextField.text.size())
    }

    void savePreferences(Map<String, String> preferences) {
        preferences.put(FONT_SIZE_KEY, fontSizeTextField.text)
    }

    boolean arePreferencesValid() {
        try {
            int i = Integer.valueOf(fontSizeTextField.text)
            return (i >= MIN_VALUE) && (i <= MAX_VALUE)
        } catch (NumberFormatException ignore) {
            return false
        }
    }

    void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {
        this.listener = listener
    }

    // --- DocumentListener --- //
    void insertUpdate(DocumentEvent e) { onTextChange() }
    void removeUpdate(DocumentEvent e) { onTextChange() }
    void changedUpdate(DocumentEvent e) { onTextChange() }

    void onTextChange() {
        fontSizeTextField.background = arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor
        listener?.preferencesPanelChanged(this)
    }
}
