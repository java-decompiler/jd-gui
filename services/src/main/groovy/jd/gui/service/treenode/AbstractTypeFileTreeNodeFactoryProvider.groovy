/*
 * Copyright (c) 2008-2015 Emmanuel Dupuy
 * This program is made available under the terms of the GPLv3 License.
 */

package jd.gui.service.treenode

import groovy.transform.CompileStatic
import jd.gui.api.API
import jd.gui.api.feature.PageCreator
import jd.gui.api.feature.TreeNodeExpandable
import jd.gui.api.feature.UriGettable
import jd.gui.api.model.Container
import jd.gui.api.model.Type
import jd.gui.view.data.TreeNodeBean

import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

abstract class AbstractTypeFileTreeNodeFactoryProvider extends AbstractTreeNodeFactoryProvider {

    static class BaseTreeNode extends DefaultMutableTreeNode implements UriGettable, PageCreator {
        Container.Entry entry
        PageFactory pageFactory;
        URI uri

        BaseTreeNode(Container.Entry entry, String fragment, Object userObject, PageFactory pageFactory) {
            super(userObject)
            this.entry = entry
            this.pageFactory = pageFactory

            if (fragment) {
                def uri = entry.uri
                this.uri = new URI(uri.scheme, uri.host, uri.path, fragment)
            } else {
                this.uri = entry.uri
            }
        }

        // --- UriGettable --- //
        URI getUri() { uri }

        // --- PageCreator --- //
        public <T extends JComponent & UriGettable> T createPage(API api) {
            // Lazy 'tip' initialization
            def file = new File(entry.container.root.uri)
            def tip = "<html>Location: $file.path"

            entry.inputStream.withStream { is ->
                is.skip(4) // Skip magic number
                int minorVersion = readUnsignedShort(is)
                int majorVersion = readUnsignedShort(is)

                if (majorVersion >= 49) {
                    tip += "<br>Java compiler version: ${majorVersion - (49-5)} ($majorVersion.$minorVersion)"
                } else if (majorVersion >= 45) {
                    tip += "<br>Java compiler version: 1.${majorVersion - (45-1)} ($majorVersion.$minorVersion)"
                }
            }

            tip += "</html>"

            userObject.tip = tip

            return pageFactory.make(api, entry)
        }

        /**
         * @see java.io.DataInputStream#readUnsignedShort()
         */
        @CompileStatic
        int readUnsignedShort(InputStream is) throws IOException {
            int ch1 = is.read()
            int ch2 = is.read()
            if ((ch1 | ch2) < 0)
                throw new EOFException()
            return (ch1 << 8) + (ch2 << 0)
        }
    }

    static class FileTreeNode extends BaseTreeNode implements TreeNodeExpandable {
        boolean initialized

        FileTreeNode(Container.Entry entry, Object userObject, PageFactory pageFactory) {
            this(entry, null, userObject, pageFactory)
        }

        FileTreeNode(Container.Entry entry, String fragment, Object userObject, PageFactory pageFactory) {
            super(entry, fragment, userObject, pageFactory)
            initialized = false
            // Add dummy node
            add(new DefaultMutableTreeNode())
        }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()
                // Create type node
                def type = api.getTypeFactory(entry)?.make(api, entry, null)
                if (type) {
                    add(new TypeTreeNode(entry, type, new TreeNodeBean(label: type.displayTypeName, icon: type.icon), pageFactory))
                }
                
                initialized = true
            }
        }
    }

    static class TypeTreeNode extends BaseTreeNode implements TreeNodeExpandable {
        boolean initialized
        Type type

        TypeTreeNode(Container.Entry entry, Type type, Object userObject, PageFactory pageFactory) {
            super(entry, type.name, userObject, pageFactory)
            this.initialized = false
            this.type = type
            // Add dummy node
            add(new DefaultMutableTreeNode())
        }

        // --- TreeNodeExpandable --- //
        void populateTreeNode(API api) {
            if (!initialized) {
                removeAllChildren()

                def sb = new StringBuffer()
                def typeAccess = type.flags
                def typeName = type.name
                def constructorName = type.displayInnerTypeName ?: type.displayTypeName
                def isInnerClass = (type.displayInnerTypeName != null)

                // Create inner types
                type.innerTypes.sort { t1, t2 ->
                    t1.name.compareTo(t2.name)
                }.each {
                    add(new TypeTreeNode(entry, it, new TreeNodeBean(label: it.displayInnerTypeName, icon: it.icon), pageFactory))
                }

                // Create fields
                type.fields.collect {
                    sb.setLength(0)
                    sb.append(it.name).append(' : ')
                    writeSignature(sb, it.descriptor, it.descriptor.length(), 0, false)
                    def label = sb.toString()
                    def fragment = typeName + '-' + it.name + '-' + it.descriptor
                    return new FieldOrMethodBean(fragment:fragment, label:label, icon:it.icon)
                }.sort { f1, f2 ->
                    f1.label.compareTo(f2.label)
                }.each {
                    add(new FieldOrMethodTreeNode(entry, it.fragment, new TreeNodeBean(label: it.label, icon: it.icon), pageFactory))
                }

                // Create methods
                type.methods.collect {
                    sb.setLength(0)
                    writeMethodSignature(sb, typeAccess, it.flags, isInnerClass, constructorName, it.name, it.descriptor)
                    def label = sb.toString()
                    def fragment = typeName + '-' + it.name + '-' + it.descriptor
                    return new FieldOrMethodBean(fragment:fragment, label:label, icon:it.icon)
                }.sort { m1, m2 ->
                    m1.label.compareTo(m2.label)
                }.each {
                    add(new FieldOrMethodTreeNode(entry, it.fragment, new TreeNodeBean(label: it.label, icon: it.icon), pageFactory))
                }

                initialized = true
            }
        }

        int writeSignature(StringBuffer sb, String descriptor, int  length, int index, boolean varargsFlag) {
            while (true) {
                // Print array : '[[?' ou '[L[?;'
                int dimensionLength = 0

                if (descriptor.charAt(index) == '[') {
                    dimensionLength++;

                    while (++index < length) {
                        if ((descriptor.charAt(index) == 'L') && (index+1 < length) && (descriptor.charAt(index+1) == '[')) {
                            index++
                            length--
                            dimensionLength++;
                        } else if (descriptor.charAt(index) == '[') {
                            dimensionLength++
                        } else {
                            break
                        }
                    }
                }

                switch(descriptor.charAt(index)) {
                    case 'B':
                        sb.append('byte')
                        index++
                        break
                    case 'C':
                        sb.append('char')
                        index++
                        break
                    case 'D':
                        sb.append('double')
                        index++
                        break
                    case 'F':
                        sb.append('float')
                        index++
                        break
                    case 'I':
                        sb.append('int')
                        index++
                        break
                    case 'J':
                        sb.append('long')
                        index++
                        break
                    case 'L': case '.':
                        int beginIndex = ++index
                        char c = '.'

                        // Search ; or de <
                        while (index < length) {
                            c = descriptor.charAt(index)
                            if ((c == ';') || (c == '<'))
                                break
                            index++
                        }

                        String internalClassName = descriptor.substring(beginIndex, index)
                        int lastPackageSeparatorIndex = internalClassName.lastIndexOf('/')

                        if (lastPackageSeparatorIndex >= 0) {
                            // Cut package name
                            internalClassName = internalClassName.substring(lastPackageSeparatorIndex + 1)
                        }

                        sb.append(internalClassName.replace('$', '.'))

                        if (c == '<') {
                            sb.append('<')
                            index = writeSignature(sb, descriptor, length, index+1, false)

                            while (descriptor.charAt(index) != '>') {
                                sb.append(', ')
                                index = writeSignature(sb, descriptor, length, index, false)
                            }
                            sb.append('>')

                            // pass '>'
                            index++
                        }

                        // pass ';'
                        if (descriptor.charAt(index) == ';')
                            index++
                        break
                    case 'S':
                        sb.append('short')
                        index++
                        break
                    case 'T':
                        int beginIndex = ++index
                        index = descriptor.substring(beginIndex, length).indexOf(';')
                        sb.append(descriptor.substring(beginIndex, index))
                        index++
                        break;
                    case 'V':
                        sb.append('void')
                        index++
                        break
                    case 'Z':
                        sb.append('boolean')
                        index++
                        break
                    case '-':
                        sb.append('? ').append('super').append(' ')
                        index = writeSignature(sb, descriptor, length, index+1, false)
                        break;
                    case '+':
                        sb.append('? ').append('extends').append(' ')
                        index = writeSignature(sb, descriptor, length, index+1, false)
                        break;
                    case '*':
                        sb.append('?')
                        index++
                        break
                    case 'X': case 'Y':
                        sb.append('int')
                        index++
                        break
                    default:
                        throw new RuntimeException('SignatureWriter.WriteSignature: invalid signature "' + descriptor + '"')
                }

                if (varargsFlag)
                {
                    if (dimensionLength > 0)
                    {
                        while (--dimensionLength > 0)
                            sb.append('[]')
                        sb.append('...')
                    }
                }
                else
                {
                    while (dimensionLength-- > 0)
                        sb.append('[]')
                }


                if ((index >= length) || (descriptor.charAt(index) != '.'))
                    break

                sb.append('.')
            }

            return index
        }

        void writeMethodSignature(
                StringBuffer sb, int typeAccess, int methodAccess, boolean isInnerClass,
                String constructorName, String methodName, String descriptor) {
            if (methodName.equals('<clinit>')) {
                sb.append('{...}')
            } else {
                boolean isAConstructor = methodName.equals('<init>')

                if (isAConstructor) {
                    sb.append(constructorName)
                } else {
                    sb.append(methodName)
                }

                // Skip generics
                int length = descriptor.length()
                int index = 0

                while ((index < length) && (descriptor.charAt(index) != '('))
                    index++

                if (descriptor.charAt(index) != '(') {
                    throw RuntimeException('Signature format exception: "' + descriptor + '"');
                }

                sb.append('(')

                // pass '('
                index++

                if (descriptor.charAt(index) != ')') {
                    if (isAConstructor && isInnerClass && ((typeAccess & Type.FLAG_STATIC) == 0)) {
                        // Skip first parameter
                        int lengthBackup = sb.length()
                        index = writeSignature(sb, descriptor, length, index, false)
                        sb.setLength(lengthBackup)
                    }

                    if (descriptor.charAt(index) != ')') {
                        int varargsParameterIndex

                        if ((methodAccess & Type.FLAG_VARARGS) == 0) {
                            varargsParameterIndex = Integer.MAX_VALUE
                        } else {
                            // Count parameters
                            int indexBackup = index
                            int lengthBackup = sb.length()

                            varargsParameterIndex = -1

                            while (descriptor.charAt(index) != ')') {
                                index = writeSignature(sb, descriptor, length, index, false)
                                varargsParameterIndex++
                            }

                            index = indexBackup
                            sb.setLength(lengthBackup)
                        }

                        // Write parameters
                        index = writeSignature(sb, descriptor, length, index, false)

                        int parameterIndex = 1

                        while (descriptor.charAt(index) != ')') {
                            sb.append(', ')
                            index = writeSignature(sb, descriptor, length, index, (parameterIndex == varargsParameterIndex))
                            parameterIndex++
                        }
                    }
                }

                if (isAConstructor) {
                    sb.append(')')
                } else {
                    sb.append(') : ')
                    writeSignature(sb, descriptor, length, ++index, false)
                }
            }
        }
    }

    static class FieldOrMethodTreeNode extends BaseTreeNode {
        FieldOrMethodTreeNode(Container.Entry entry, String fragment, Object userObject, PageFactory pageFactory) {
            super(entry, fragment, userObject, pageFactory)
        }
    }

    static class FieldOrMethodBean {
        String fragment, label
        Icon icon
    }
    
    interface PageFactory {
        public <T extends JComponent & UriGettable> T make(API api, Container.Entry entry);
    }
}
