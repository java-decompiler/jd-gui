/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package org.jd.gui.service.treenode

import org.jd.gui.api.API
import org.jd.gui.api.feature.ContainerEntryGettable
import org.jd.gui.api.feature.PageCreator
import org.jd.gui.api.feature.TreeNodeExpandable
import org.jd.gui.api.feature.UriGettable
import org.jd.gui.api.model.Container
import org.jd.gui.api.model.Type
import org.jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

abstract class AbstractTypeFileTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {

    static class BaseTreeNode extends DefaultMutableTreeNode implements ContainerEntryGettable, UriGettable, PageCreator {
        Container.Entry entry
        PageAndTipFactory factory;
        URI uri

        BaseTreeNode(Container.Entry entry, String fragment, Object userObject, PageAndTipFactory factory) {
            super(userObject)
            this.entry = entry
            this.factory = factory

            if (fragment) {
                def uri = entry.uri
                this.uri = new URI(uri.scheme, uri.host, uri.path, fragment)
            } else {
                this.uri = entry.uri
            }
        }

        // --- ContainerEntryGettable --- //
        Container.Entry getEntry() { entry }

        // --- UriGettable --- //
        URI getUri() { uri }

        // --- PageCreator --- //
        public <T extends JComponent & UriGettable> T createPage(API api) {
            // Lazy 'tip' initialization
            userObject.tip = factory.makeTip(api, entry)
            return factory.makePage(api, entry)
        }
    }

    static class FileTreeNode extends BaseTreeNode implements TreeNodeExpandable {
        boolean initialized

        FileTreeNode(Container.Entry entry, Object userObject, PageAndTipFactory pageAndTipFactory) {
            this(entry, null, userObject, pageAndTipFactory)
        }

        FileTreeNode(Container.Entry entry, String fragment, Object userObject, PageAndTipFactory factory) {
            super(entry, fragment, userObject, factory)
            initialized = false
            // Add dummy node
            add(new DefaultMutableTreeNode())
        }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()
                // Create type node
                def types = api.getTypeFactory(entry)?.make(api, entry)

                for (def type : types) {
                    add(new TypeTreeNode(entry, type, new TreeNodeBean(label: type.displayTypeName, icon: type.icon), factory))
                }
                
                initialized = true
            }
        }
    }

    static class TypeTreeNode extends BaseTreeNode implements TreeNodeExpandable {
        boolean initialized
        Type type

        TypeTreeNode(Container.Entry entry, Type type, Object userObject, PageAndTipFactory factory) {
            super(entry, type.name, userObject, factory)
            this.initialized = false
            this.type = type
            // Add dummy node
            add(new DefaultMutableTreeNode())
        }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()

                def typeName = type.name

                // Create inner types
                type.innerTypes.sort { t1, t2 ->
                    t1.name.compareTo(t2.name)
                }.each {
                    add(new TypeTreeNode(entry, it, new TreeNodeBean(label: it.displayInnerTypeName, icon: it.icon), factory))
                }

                // Create fields
                type.fields.collect {
                    def fragment = typeName + '-' + it.name + '-' + it.descriptor
                    return new FieldOrMethodBean(fragment:fragment, label:it.displayName, icon:it.icon)
                }.sort { f1, f2 ->
                    f1.label.compareTo(f2.label)
                }.each {
                    add(new FieldOrMethodTreeNode(entry, it.fragment, new TreeNodeBean(label: it.label, icon: it.icon), factory))
                }

                // Create methods
                type.methods.grep {
                    !it.name.equals('<clinit>')
                }.collect {
                    def fragment = typeName + '-' + it.name + '-' + it.descriptor
                    return new FieldOrMethodBean(fragment:fragment, label:it.displayName, icon:it.icon)
                }.sort { m1, m2 ->
                    m1.label.compareTo(m2.label)
                }.each {
                    add(new FieldOrMethodTreeNode(entry, it.fragment, new TreeNodeBean(label: it.label, icon: it.icon), factory))
                }

                initialized = true
            }
        }
    }

    static class FieldOrMethodTreeNode extends BaseTreeNode {
        FieldOrMethodTreeNode(Container.Entry entry, String fragment, Object userObject, PageAndTipFactory factory) {
            super(entry, fragment, userObject, factory)
        }
    }

    static class FieldOrMethodBean {
        String fragment, label
        Icon icon
    }

    interface PageAndTipFactory {
        public <T extends JComponent & UriGettable> T makePage(API api, Container.Entry entry);
        public String makeTip(API api, Container.Entry entry);
    }
}
