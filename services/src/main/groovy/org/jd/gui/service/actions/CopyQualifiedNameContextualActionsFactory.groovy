/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.actions

import com.sun.media.sound.InvalidFormatException
import org.jd.gui.api.API
import org.jd.gui.api.model.Container
import org.jd.gui.spi.ContextualActionsFactory

import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.ImageIcon
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent

class CopyQualifiedNameContextualActionsFactory implements ContextualActionsFactory {

    Collection<Action> make(API api, Container.Entry entry, String fragment) {
        return Collections.singletonList(new CopyQualifiedNameAction(api, entry, fragment))
    }

    static class CopyQualifiedNameAction extends AbstractAction {
        static final ImageIcon ICON = new ImageIcon(CopyQualifiedNameAction.class.classLoader.getResource('org/jd/gui/images/cpyqual_menu.png'))

        protected API api
        protected Container.Entry entry
        protected String fragment

        CopyQualifiedNameAction(API api, Container.Entry entry, String fragment) {
            this.api = api
            this.entry = entry
            this.fragment = fragment

            putValue(GROUP_NAME, 'Edit > CutCopyPaste')
            putValue(NAME, 'Copy Qualified Name')
            putValue(SMALL_ICON, ICON)
        }

        void actionPerformed(ActionEvent e) {
            def type = api.getTypeFactory(entry)?.make(api, entry, fragment)

            if (type) {
                def sb = new StringBuffer(type.displayPackageName)
                int dashIndex = fragment.indexOf('-')

                if (sb.length() > 0) {
                    sb.append('.')
                }

                sb.append(type.displayTypeName)

                if (dashIndex != -1) {
                    int lastDashIndex = fragment.lastIndexOf('-')

                    if (dashIndex == lastDashIndex) {
                        // See jd.gui.api.feature.UriOpenable
                        throw new InvalidFormatException('fragment: ' + fragment)
                    } else {
                        def name = fragment.substring(dashIndex+1, lastDashIndex)
                        def descriptor = fragment.substring(lastDashIndex+1)

                        if (descriptor.startsWith('(')) {
                            for (def method : type.methods) {
                                if (method.name.equals(name) && method.descriptor.equals(descriptor)) {
                                    sb.append('.').append(method.displayName)
                                    break
                                }
                            }
                        } else {
                            for (def field : type.fields) {
                                if (field.name.equals(name) && field.descriptor.equals(descriptor)) {
                                    sb.append('.').append(field.displayName)
                                    break
                                }
                            }
                        }
                    }
                }

                Toolkit.defaultToolkit.systemClipboard.setContents(new StringSelection(sb.toString()), null)
            } else {
                // Copy path of entry
                def path = new File(entry.uri).absolutePath
                Toolkit.defaultToolkit.systemClipboard.setContents(new StringSelection(path), null)
            }
        }
    }
}
