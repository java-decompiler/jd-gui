/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.service.converter.classfiletojavasyntax;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.model.processor.Processor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.processor.ConvertClassFileProcessor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.processor.UpdateJavaSyntaxTreeProcessor;
import org.jd.core.v1.service.converter.classfiletojavasyntax.util.TypeMaker;

import java.util.Map;

/**
 * Convert ClassFile model to Java syntax model.<br><br>
 *
 * Input:  {@link org.jd.core.v1.model.classfile.ClassFile}<br>
 * Output: {@link org.jd.core.v1.model.javasyntax.CompilationUnit}<br>
 *
 * @see ConvertClassFileProcessor
 */
public class ClassFileToJavaSyntaxProcessor implements Processor {
    protected static final ConvertClassFileProcessor CONVERT_CLASS_FILE_PROCESSOR = new ConvertClassFileProcessor();
    protected static final UpdateJavaSyntaxTreeProcessor UPDATE_JAVA_SYNTAX_TREE_PROCESSOR = new UpdateJavaSyntaxTreeProcessor();

    public void process(Message message) throws Exception {
        Loader loader = message.getHeader("loader");
        Map<String, Object> configuration = message.getHeader("configuration");

        if (configuration == null) {
            message.setHeader("typeMaker", new TypeMaker(loader));
        } else {
            TypeMaker typeMaker = null;

            try {
                typeMaker = (TypeMaker)configuration.get("typeMaker");

                if (typeMaker == null) {
                    // Store the heavy weight object 'typeMaker' in 'configuration' to reuse it
                    configuration.put("typeMaker", typeMaker=new TypeMaker(loader));
                }
            } catch (Exception e) {
                if (typeMaker == null) {
                    typeMaker = new TypeMaker(loader);
                }
            }

            message.setHeader("typeMaker", typeMaker);
        }

        CONVERT_CLASS_FILE_PROCESSOR.process(message);
        UPDATE_JAVA_SYNTAX_TREE_PROCESSOR.process(message);
    }
}
