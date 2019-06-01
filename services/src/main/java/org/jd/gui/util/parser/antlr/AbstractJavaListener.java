/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.util.parser.antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.jd.gui.api.model.Container;
import org.jd.gui.util.exception.ExceptionUtil;

import java.util.HashMap;
import java.util.List;

public abstract class AbstractJavaListener extends JavaBaseListener {
    protected Container.Entry entry;
    protected String packageName = "";
    protected HashMap<String, String> nameToInternalTypeName = new HashMap<>();
    protected StringBuilder sb = new StringBuilder();
    protected HashMap<String, String> typeNameCache = new HashMap<>();

    public AbstractJavaListener(Container.Entry entry) {
        this.entry = entry;
    }

    public void enterPackageDeclaration(JavaParser.PackageDeclarationContext ctx) {
        packageName = concatIdentifiers(ctx.qualifiedName().Identifier());
    }

    public void enterImportDeclaration(JavaParser.ImportDeclarationContext ctx) {
        List<TerminalNode> identifiers = ctx.qualifiedName().Identifier();
        int size = identifiers.size();

        if (size > 1) {
            nameToInternalTypeName.put(identifiers.get(size - 1).getText(), concatIdentifiers(identifiers));
        }
    }

    protected String concatIdentifiers(List<TerminalNode> identifiers) {
        switch (identifiers.size()) {
            case 0:
                return "";
            case 1:
                return identifiers.get(0).getText();
            default:
                sb.setLength(0);

                for (TerminalNode identifier : identifiers) {
                    sb.append(identifier.getText()).append('/');
                }

                // Remove last separator
                sb.setLength(sb.length() - 1);

                return sb.toString();
        }
    }

    protected String resolveInternalTypeName(List<TerminalNode> identifiers) {
        switch (identifiers.size()) {
            case 0:
                return null;

            case 1:
                // Search in cache
                String name = identifiers.get(0).getText();
                String qualifiedName = typeNameCache.get(name);

                if (qualifiedName != null) {
                    return qualifiedName;
                }

                // Search in imports
                String imp = nameToInternalTypeName.get(name);

                if (imp != null) {
                    // Import found
                    return imp;
                }

                // Search type in same package
                String prefix = name + '.';

                if (entry.getPath().indexOf('/') != -1) {
                    // Not in root package
                    Container.Entry parent = entry.getParent();
                    int packageLength = parent.getPath().length() + 1;

                    for (Container.Entry child : parent.getChildren()) {
                        if (!child.isDirectory() && child.getPath().substring(packageLength).startsWith(prefix)) {
                            qualifiedName = packageName + '/' + name;
                            typeNameCache.put(name, qualifiedName);
                            return qualifiedName;
                        }
                    }
                }

                // Search type in root package
                for (Container.Entry child : entry.getContainer().getRoot().getChildren()) {
                    if (!child.isDirectory() && child.getPath().startsWith(prefix)) {
                        typeNameCache.put(name, name);
                        return name;
                    }
                }

                // Search type in 'java.lang'
                try {
                    if (Class.forName("java.lang." + name) != null) {
                        qualifiedName = "java/lang/" + name;
                        typeNameCache.put(name, qualifiedName);
                        return qualifiedName;
                    }
                } catch (ClassNotFoundException ignore) {
                    // Ignore class loading error
                }

                // Type not found
                qualifiedName = "*/" + name;
                typeNameCache.put(name, qualifiedName);
                return qualifiedName;

            default:
                // Qualified type name -> Nothing to do
                return concatIdentifiers(identifiers);
        }
    }

    protected String createDescriptor(JavaParser.TypeContext typeContext, int dimension) {
        if (typeContext == null) {
            return "V";
        } else {
            dimension += countDimension(typeContext.children);
            JavaParser.PrimitiveTypeContext primitive = typeContext.primitiveType();
            String name;

            if (primitive == null) {
                JavaParser.ClassOrInterfaceTypeContext type = typeContext.classOrInterfaceType();
                List<JavaParser.TypeArgumentsContext> typeArgumentsContexts = type.typeArguments();

                if (typeArgumentsContexts.size() == 1) {
                    JavaParser.TypeArgumentsContext typeArgumentsContext = typeArgumentsContexts.get(0);
                    List<JavaParser.TypeArgumentContext> typeArguments = typeArgumentsContext.typeArgument();
                } else if (typeArgumentsContexts.size() > 1) {
                    throw new RuntimeException("UNEXPECTED");
                }

                name = "L" + resolveInternalTypeName(type.Identifier()) + ";";
            } else {
                // Search primitive
                switch (primitive.getText()) {
                    case "boolean": name = "Z"; break;
                    case "byte":    name = "B"; break;
                    case "char":    name = "C"; break;
                    case "double":  name = "D"; break;
                    case "float":   name = "F"; break;
                    case "int":     name = "I"; break;
                    case "long":    name = "J"; break;
                    case "short":   name = "S"; break;
                    case "void":    name = "V"; break;
                    default:
                        throw new RuntimeException("UNEXPECTED PRIMITIVE");
                }
            }

            switch (dimension) {
                case 0:  return name;
                case 1:  return "[" + name;
                case 2:  return "[[" + name;
                default: return new String(new char[dimension]).replace('\0', '[') + name;
            }
        }
    }

    protected int countDimension(List<ParseTree> children) {
        int dimension = 0;

        for (ParseTree child : children) {
            if (child instanceof TerminalNodeImpl) {
                if (((TerminalNodeImpl)child).getSymbol().getType() == JavaParser.LBRACK)
                    dimension++;
            }
        }

        return dimension;
    }
}
