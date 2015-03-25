/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.view

import groovy.swing.SwingBuilder
import jd.gui.api.feature.LineNumberNavigable
import jd.gui.model.configuration.Configuration

import javax.swing.JComponent
import javax.swing.KeyStroke
import javax.swing.UIManager
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.Color
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

class GoToView {
    SwingBuilder swing
    Listener listener

    GoToView(SwingBuilder swing, Configuration configuration) {
        this.swing = swing
        this.listener = new Listener(Color.decode(configuration.preferences.get('JdGuiPreferences.errorBackgroundColor')))
        // Load GUI description
        swing.edt {
            build(GoToDescription)
            goToDialog.with {
                rootPane.with {
                    defaultButton = goToOkButton
                    getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "OpenTypeView.cancel")
                    actionMap.put("OpenTypeView.cancel", goToCancelAction)
                }
                pack()
                locationRelativeTo = parent
                goToEnterLineNumberTextField.addKeyListener(new KeyAdapter() {
                    void keyTyped(KeyEvent e) {
                        if (! Character.isDigit(e.keyChar)) {
                            e.consume()
                        }
                    }
                })
            }
            // Add binding
            goToEnterLineNumberTextField.document.addDocumentListener(listener)
        }
    }

    void show(LineNumberNavigable navigator, Closure okClosure) {
        // Init
        listener.navigator = navigator
        swing.doLater {
            goToEnterLineNumberLabel.text = "Enter line number (1..$navigator.maximumLineNumber):"
            goToOkAction.closure = {
                okClosure(Integer.valueOf(goToEnterLineNumberTextField.text))
                goToDialog.visible = false
            }
            goToEnterLineNumberTextField.text = ''
            goToDialog.visible = true
        }
    }

    class Listener implements DocumentListener {
        LineNumberNavigable navigator
        Color backgroundColor
        Color errorBackgroundColor

        Listener(Color errorBackgroundColor) {
            this.backgroundColor = UIManager.getColor('TextField.background')
            this.errorBackgroundColor = errorBackgroundColor
        }

        void insertUpdate(DocumentEvent e) { onTextChange() }
        void removeUpdate(DocumentEvent e) { onTextChange() }
        void changedUpdate(DocumentEvent e) { onTextChange() }

        void onTextChange() {
            swing.doLater {
                String text = goToEnterLineNumberTextField.text

                if (text.length() == 0) {
                    goToOkAction.enabled = false
                    clearErrorMessage()
                } else if (text.isInteger()) {
                    int lineNumber = Integer.valueOf(text)

                    if (lineNumber > navigator.maximumLineNumber) {
                        goToOkAction.enabled = false
                        showErrorMessage('Line number out of range')
                    } else if (navigator.checkLineNumber(lineNumber)) {
                        goToOkAction.enabled = true
                        clearErrorMessage()
                    } else {
                        goToOkAction.enabled = false
                        showErrorMessage('Line number not found')
                    }
                } else {
                    goToOkAction.enabled = false
                    showErrorMessage('Not a number')
                }
            }
        }

        void showErrorMessage(String message) {
            swing.with {
                goToEnterLineNumberErrorLabel.text = message
                goToEnterLineNumberTextField.background = errorBackgroundColor
            }
        }

        void clearErrorMessage() {
            swing.with {
                goToEnterLineNumberErrorLabel.text = ' '
                goToEnterLineNumberTextField.background = backgroundColor
            }
        }
    }
}
