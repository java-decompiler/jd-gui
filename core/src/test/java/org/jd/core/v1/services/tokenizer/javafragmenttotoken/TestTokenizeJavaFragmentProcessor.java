/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.services.tokenizer.javafragmenttotoken;

import org.jd.core.v1.model.javafragment.JavaFragment;
import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.model.processor.Processor;
import org.jd.core.v1.services.tokenizer.javafragmenttotoken.visitor.TokenizeJavaFragmentTestVisitor;

import java.util.List;

public class TestTokenizeJavaFragmentProcessor implements Processor {

    @Override
    public void process(Message message) throws Exception {
        List<JavaFragment> fragments = message.getBody();
        TokenizeJavaFragmentTestVisitor visitor = new TokenizeJavaFragmentTestVisitor(fragments.size() * 3);

        // Create tokens
        for (JavaFragment fragment : fragments) {
            fragment.accept(visitor);
        }

        message.setBody(visitor.getTokens());
    }
}
