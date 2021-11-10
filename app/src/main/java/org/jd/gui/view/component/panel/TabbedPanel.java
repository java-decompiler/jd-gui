/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.component.panel;

import org.jd.gui.api.API;
import org.jd.gui.api.feature.ContainerEntryGettable;
import org.jd.gui.api.feature.PreferencesChangeListener;
import org.jd.gui.api.feature.UriGettable;
import org.jd.gui.service.platform.PlatformService;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

public class TabbedPanel<T extends JComponent & UriGettable> extends JPanel implements PreferencesChangeListener {
	protected static final ImageIcon CLOSE_ICON = new ImageIcon(TabbedPanel.class.getClassLoader().getResource("org/jd/gui/images/close.gif"));
    protected static final ImageIcon  CLOSE_ACTIVE_ICON = new ImageIcon(TabbedPanel.class.getClassLoader().getResource("org/jd/gui/images/close_active.gif"));

    protected static final String TAB_LAYOUT = "UITabsPreferencesProvider.singleLineTabs";

    protected API api;
    protected CardLayout cardLayout;
    protected JTabbedPane tabbedPane;
    protected Map<String, String> preferences;

    public TabbedPanel(API api) {
        this.api = api;
		create();
	}

    protected void create() {
		setLayout(cardLayout = new CardLayout());
		add("panel", new JPanel());
		add("tabs", tabbedPane = createTabPanel());
	}

    protected JTabbedPane createTabPanel() {
        JTabbedPane tabPanel = new JTabbedPane() {
            @Override
            public String getToolTipText(MouseEvent e) {
                int index = indexAtLocation(e.getX(), e.getY());
                if (index != -1) {
                    return ((JComponent)getTabComponentAt(index)).getToolTipText();
                }
                return super.getToolTipText(e);
            }
        };
        ToolTipManager.sharedInstance().registerComponent(tabPanel);
        tabPanel.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { showPopupTabMenu(e); }
            @Override public void mouseReleased(MouseEvent e) { showPopupTabMenu(e); }
            protected void showPopupTabMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = tabPanel.indexAtLocation(e.getX(), e.getY());
                    if (index != -1) {
                        new PopupTabMenu(tabPanel.getComponentAt(index)).show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
        return tabPanel;
	}

    protected static Color darker(Color c) {
		return new Color(
			Math.max((int)(c.getRed()  *0.85), 0),
			Math.max((int)(c.getGreen()*0.85), 0),
			Math.max((int)(c.getBlue() *0.85), 0),
			c.getAlpha());
	}

    public void addPage(String title, Icon icon, String tip, T page) {
        // Add a new tab
        JLabel tabCloseButton = new JLabel(CLOSE_ICON);
        tabCloseButton.setToolTipText("Close this panel");
        tabCloseButton.addMouseListener(new MouseListener() {
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) { ((JLabel)e.getSource()).setIcon(CLOSE_ACTIVE_ICON); }
            @Override public void mouseExited(MouseEvent e) { ((JLabel)e.getSource()).setIcon(CLOSE_ICON); }
            @Override public void mouseClicked(MouseEvent e) { removeComponent(page); }
        });

		JPanel tab = new JPanel(new BorderLayout());
        tab.setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
		tab.setOpaque(false);
        tab.setToolTipText(tip);
        tab.add(new JLabel(title, icon, JLabel.LEADING), BorderLayout.CENTER);
		tab.add(tabCloseButton, BorderLayout.EAST);
        ToolTipManager.sharedInstance().unregisterComponent(tab);

		int index = tabbedPane.getTabCount();
		tabbedPane.addTab(title, page);
        tabbedPane.setTabComponentAt(index, tab);
        setSelectedIndex(index);

        cardLayout.show(this, "tabs");
	}

    protected void setSelectedIndex(int index) {
        if (index != -1) {
            if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                // Ensure that the new page is visible (bug with SCROLL_TAB_LAYOUT)
                ChangeEvent event = new ChangeEvent(tabbedPane);
                for (ChangeListener listener : tabbedPane.getChangeListeners()) {
                    if (listener.getClass().getPackage().getName().startsWith("javax.")) {
                        listener.stateChanged(event);
                    }
                }
            }

            tabbedPane.setSelectedIndex(index);
        }
    }

    @SuppressWarnings("unchecked")
    protected T showPage(URI uri) {
        String u1 = uri.getPath();
        int i = tabbedPane.getTabCount();

        while (i-- > 0) {
            T page = (T)tabbedPane.getComponentAt(i);
            String u2 = page.getUri().getPath();
            if (u1.startsWith(u2)) {
                tabbedPane.setSelectedIndex(i);
                return page;
            }
        }

        return null;
    }

    protected class PopupTabMenu extends JPopupMenu {
        public PopupTabMenu(Component component) {
            // Add default popup menu entries
            JMenuItem menuItem = new JMenuItem("Close", null);
            menuItem.addActionListener(e -> removeComponent(component));
            add(menuItem);

            menuItem = new JMenuItem("Close Others", null);
            menuItem.addActionListener(e -> removeOtherComponents(component));
            add(menuItem);

            menuItem = new JMenuItem("Close All", null);
            menuItem.addActionListener(e -> removeAllComponents());
            add(menuItem);

            // Add "Select Tab" popup menu entry
            if ((tabbedPane.getTabCount() > 1) && (PlatformService.getInstance().isMac() || "true".equals(preferences.get(TAB_LAYOUT)))) {
                addSeparator();
                JMenu menu = new JMenu("Select Tab");
                int count = tabbedPane.getTabCount();

                for (int i = 0; i < count; i++) {
                    JPanel tab = (JPanel) tabbedPane.getTabComponentAt(i);
                    JLabel label = (JLabel) tab.getComponent(0);
                    JMenuItem subMenuItem = new JMenuItem(label.getText(), label.getIcon());
                    subMenuItem.addActionListener(new SubMenuItemActionListener(i));
                    if (component == tabbedPane.getComponentAt(i)) {
                        subMenuItem.setFont(subMenuItem.getFont().deriveFont(Font.BOLD));
                    }
                    menu.add(subMenuItem);
                }

                add(menu);
            }

            // Add SPI popup menu entries
            if (component instanceof ContainerEntryGettable) {
                Collection<Action> actions = api.getContextualActions(((ContainerEntryGettable)component).getEntry(), null);

                if (actions != null) {
                    addSeparator();

                    for (Action action : actions) {
                        if (action != null) {
                            add(action);
                        } else {
                            addSeparator();
                        }
                    }
                }
            }
        }
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    protected class SubMenuItemActionListener implements ActionListener {
        protected int index;

        public SubMenuItemActionListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(index);
        }
    }


    // --- Popup menu actions --- //
    public void removeComponent(Component component) {
        tabbedPane.remove(component);
        if (tabbedPane.getTabCount() == 0) {
            cardLayout.show(this, "panel");
        }
    }

    protected void removeOtherComponents(Component component) {
        int i = tabbedPane.getTabCount();
        while (i-- > 0) {
            Component c = tabbedPane.getComponentAt(i);
            if (c != component) {
                tabbedPane.remove(i);
            }
        }
        if (tabbedPane.getTabCount() == 0) {
            cardLayout.show(this, "panel");
        }
    }

    protected void removeAllComponents() {
        tabbedPane.removeAll();
        if (tabbedPane.getTabCount() == 0) {
            cardLayout.show(this, "panel");
        }
    }

    // --- PreferencesChangeListener --- //
    @Override
    public void preferencesChanged(Map<String, String> preferences) {
        // Store preferences
        this.preferences = preferences;
        // Update layout
        if ("true".equals(preferences.get(TAB_LAYOUT))) {
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } else {
            tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        }
        setSelectedIndex(tabbedPane.getSelectedIndex());
    }
}
