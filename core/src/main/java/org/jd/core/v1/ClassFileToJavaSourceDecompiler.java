/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import org.jd.core.v1.api.Decompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.service.converter.classfiletojavasyntax.ClassFileToJavaSyntaxProcessor;
import org.jd.core.v1.service.deserializer.classfile.DeserializeClassFileProcessor;
import org.jd.core.v1.service.fragmenter.javasyntaxtojavafragment.JavaSyntaxToJavaFragmentProcessor;
import org.jd.core.v1.service.layouter.LayoutFragmentProcessor;
import org.jd.core.v1.service.tokenizer.javafragmenttotoken.JavaFragmentToTokenProcessor;
import org.jd.core.v1.service.writer.WriteTokenProcessor;

import java.util.Map;

public class ClassFileToJavaSourceDecompiler implements Decompiler {
    protected DeserializeClassFileProcessor deserializer = new DeserializeClassFileProcessor();
    protected ClassFileToJavaSyntaxProcessor converter = new ClassFileToJavaSyntaxProcessor();
    protected JavaSyntaxToJavaFragmentProcessor fragmenter = new JavaSyntaxToJavaFragmentProcessor();
    protected LayoutFragmentProcessor layouter = new LayoutFragmentProcessor();
    protected JavaFragmentToTokenProcessor tokenizer = new JavaFragmentToTokenProcessor();
    protected WriteTokenProcessor writer = new WriteTokenProcessor();

    public void decompile(Loader loader, Printer printer, String internalName) throws Exception {
        Message message = new Message();

        message.setHeader("mainInternalTypeName", internalName);
        message.setHeader("loader", loader);
        message.setHeader("printer", printer);

        decompile(message);
    }

    public void decompile(Loader loader, Printer printer, String internalName, Map<String, Object> configuration) throws Exception {
        Message message = new Message();

        message.setHeader("mainInternalTypeName", internalName);
        message.setHeader("configuration", configuration);
        message.setHeader("loader", loader);
        message.setHeader("printer", printer);

        decompile(message);
    }

    protected void decompile(Message message) throws Exception {
        this.deserializer.process(message);
        this.converter.process(message);
        this.fragmenter.process(message);
        this.layouter.process(message);
        this.tokenizer.process(message);
        this.writer.process(message);
    }
}
