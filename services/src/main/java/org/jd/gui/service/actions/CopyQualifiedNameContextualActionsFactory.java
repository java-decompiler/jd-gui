/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.service.actions;

import org.jd.gui.api.API;
import org.jd.gui.api.model.Container;
import org.jd.gui.api.model.Type;
import org.jd.gui.spi.ContextualActionsFactory;
import org.jd.gui.spi.TypeFactory;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class CopyQualifiedNameContextualActionsFactory implements ContextualActionsFactory {

    public Collection<Action> make(API api, Container.Entry entry, String fragment) {
        return Collections.<Action>singletonList(new CopyQualifiedNameAction(api, entry, fragment));
    }

    public static class CopyQualifiedNameAction extends AbstractAction {
        protected static final ImageIcon ICON = new ImageIcon(CopyQualifiedNameAction.class.getClassLoader().getResource("org/jd/gui/images/cpyqual_menu.png"));

        protected API api;
        protected Container.Entry entry;
        protected String fragment;

        public CopyQualifiedNameAction(API api, Container.Entry entry, String fragment) {
            this.api = api;
            this.entry = entry;
            this.fragment = fragment;

            putValue(GROUP_NAME, "Edit > CutCopyPaste");
            putValue(NAME, "Copy Qualified Name");
            putValue(SMALL_ICON, ICON);
        }

        public void actionPerformed(ActionEvent e) {
            if (fragment != null) {
                TypeFactory typeFactory = api.getTypeFactory(entry);
                Type type = (typeFactory == null) ? null : typeFactory.make(api, entry, fragment);

                if (type != null) {
                    StringBuilder sb = new StringBuilder(type.getDisplayPackageName());
                    int dashIndex = fragment.indexOf('-');

                    if (sb.length() > 0) {
                        sb.append('.');
                    }

                    sb.append(type.getDisplayTypeName());

                    if (dashIndex != -1) {
                        int lastDashIndex = fragment.lastIndexOf('-');

                        if (dashIndex == lastDashIndex) {
                            // See jd.gui.api.feature.UriOpenable
                            throw new InvalidFormatException("fragment: " + fragment);
                        } else {
                            String name = fragment.substring(dashIndex + 1, lastDashIndex);
                            String descriptor = fragment.substring(lastDashIndex + 1);

                            if (descriptor.startsWith("(")) {
                                for (Type.Method method : type.getMethods()) {
                                    if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                                        sb.append('.').append(method.getDisplayName());
                                        break;
                                    }
                                }
                            } else {
                                for (Type.Field field : type.getFields()) {
                                    if (field.getName().equals(name) && field.getDescriptor().equals(descriptor)) {
                                        sb.append('.').append(field.getDisplayName());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
                    return;
                }
            }

            // Copy path of entry
            String path = new File(entry.getUri()).getAbsolutePath();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        }
    }
}
