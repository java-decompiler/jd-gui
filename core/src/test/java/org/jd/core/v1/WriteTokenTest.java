/*
 * Copyright (c) 2008, 2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1;

import org.jd.core.v1.model.message.Message;
import org.jd.core.v1.model.token.*;
import org.jd.core.v1.printer.PlainTextMetaPrinter;
import org.jd.core.v1.service.writer.WriteTokenProcessor;
import org.jd.core.v1.util.DefaultList;
import org.junit.Assert;
import org.junit.Test;

public class WriteTokenTest {
    public static final KeywordToken PACKAGE = new KeywordToken("package");
    public static final KeywordToken IF = new KeywordToken("if");
    public static final KeywordToken IMPORT = new KeywordToken("import");
    public static final KeywordToken STATIC = new KeywordToken("static");
    public static final KeywordToken PUBLIC = new KeywordToken("public");
    public static final KeywordToken CLASS = new KeywordToken("class");
    public static final KeywordToken VOID = new KeywordToken("void");
    public static final KeywordToken INT = new KeywordToken("int");
    public static final KeywordToken BOOLEAN = new KeywordToken("boolean");
    public static final KeywordToken RETURN = new KeywordToken("return");
    public static final KeywordToken FALSE = new KeywordToken("false");
    public static final KeywordToken NULL = new KeywordToken("null");
    public static final KeywordToken NEW = new KeywordToken("new");

    @Test
    public void writeClassDeclaration() throws Exception {
        DefaultList<Token> tokens = new DefaultList<>();

        // package org.jd.core.v1.service.writer;\n\n
        tokens.add(PACKAGE);
        tokens.add(new TextToken(" org.jd.core.v1.service.test;"));
        tokens.add(NewLineToken.NEWLINE_2);

        // import javasyntax.util.ArrayList;\n
        tokens.add(IMPORT);
        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, "java/util/ArrayList", "java.util.ArrayList", null, null));
        tokens.add(TextToken.SEMICOLON);
        tokens.add(NewLineToken.NEWLINE_1);

        // import static org.junit.Assert.*;\n\n
        tokens.add(IMPORT);
        tokens.add(TextToken.SPACE);
        tokens.add(STATIC);
        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, "org/junit/Assert/*", "org.junit.Assert.*", null, null));
        tokens.add(TextToken.SEMICOLON);
        tokens.add(NewLineToken.NEWLINE_2);

        // public class WriteTokenTest {\n
        tokens.add(PUBLIC);
        tokens.add(TextToken.SPACE);
        tokens.add(CLASS);
        tokens.add(TextToken.SPACE);
        tokens.add(new DeclarationToken(DeclarationToken.TYPE, "org/jd/core/v1/service/test/WriteTokenTest", "WriteTokenTest", null));
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);

        // public static void main(String[] args) {\n
        tokens.add(PUBLIC);
        tokens.add(TextToken.SPACE);
        tokens.add(STATIC);
        tokens.add(TextToken.SPACE);
        tokens.add(VOID);
        tokens.add(TextToken.SPACE);
        tokens.add(new DeclarationToken(DeclarationToken.METHOD, "org/jd/core/v1/service/test/WriteTokenTest", "main", "([Ljava/lang/String;)V"));
        tokens.add(TextToken.LEFTROUNDBRACKET);
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, "java/lang/String", "String", null, "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(new TextToken("[] args) "));
        tokens.add(StartBlockToken.START_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);

        // if (args == null)\n
        tokens.add(new LineNumberToken(8));
        tokens.add(IF);
        tokens.add(new TextToken(" (args == "));
        tokens.add(NULL);
        tokens.add(new TextToken(")"));
        tokens.add(StartBlockToken.START_DECLARATION_OR_STATEMENT_BLOCK);
            // return;\n
            tokens.add(NewLineToken.NEWLINE_1);
            tokens.add(RETURN);
            tokens.add(TextToken.SEMICOLON);
            tokens.add(NewLineToken.NEWLINE_1);
        tokens.add(EndBlockToken.END_DECLARATION_OR_STATEMENT_BLOCK);

        // int i = call(\n
        //  "aaaa",\n
        //  b,\n
        //  new Enumeration() {\n
        //      public boolean hasMoreElements() {\n
        //          return false;\n
        //      }\n
        //      public Object nextElement() {\n
        //          return null;\n
        //      }\n
        //  },
        //  c);\n
        tokens.add(new LineNumberToken(10));
        tokens.add(INT);
        tokens.add(new TextToken(" i = "));
        tokens.add(new ReferenceToken(ReferenceToken.METHOD, "org/jd/core/v1/service/test/WriteTokenTest", "call", "(IILjava/util/Enumeration;I)V", "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);
        tokens.add(new LineNumberToken(11));
        tokens.add(new StringConstantToken("aaaa", "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(TextToken.COMMA);
        tokens.add(NewLineToken.NEWLINE_1);
        tokens.add(new LineNumberToken(12));
        tokens.add(new TextToken("b,"));
        tokens.add(NewLineToken.NEWLINE_1);
        tokens.add(new LineNumberToken(13));
        tokens.add(NEW);
        tokens.add(TextToken.SPACE);
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, "java/util/Enumeration", "Enumeration", null, "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(new TextToken("() "));
        tokens.add(StartBlockToken.START_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);
            // public boolean hasMoreElements()...
            tokens.add(PUBLIC);
            tokens.add(TextToken.SPACE);
            tokens.add(BOOLEAN);
            tokens.add(TextToken.SPACE);
            tokens.add(new DeclarationToken(DeclarationToken.METHOD, "org/jd/core/v1/service/test/WriteTokenTest$1", "hasMoreElements", "()Z"));
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            tokens.add(TextToken.SPACE);
            tokens.add(StartBlockToken.START_BLOCK);
            tokens.add(NewLineToken.NEWLINE_1);
                // return false;
                tokens.add(new LineNumberToken(15));
                tokens.add(RETURN);
                tokens.add(TextToken.SPACE);
                tokens.add(FALSE);
                tokens.add(TextToken.SEMICOLON);
                tokens.add(NewLineToken.NEWLINE_1);
            tokens.add(EndBlockToken.END_BLOCK);
            tokens.add(NewLineToken.NEWLINE_1);
            // public Object nextElement()...
            tokens.add(PUBLIC);
            tokens.add(TextToken.SPACE);
            tokens.add(new ReferenceToken(ReferenceToken.TYPE, "java/lang/Object", "Object", null, "org/jd/core/v1/service/test/WriteTokenTest"));
            tokens.add(TextToken.SPACE);
            tokens.add(new DeclarationToken(DeclarationToken.METHOD, "org/jd/core/v1/service/test/WriteTokenTest$1", "nextElement", "()Ljava/lang/Object;"));
            tokens.add(StartBlockToken.START_PARAMETERS_BLOCK);
            tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
            tokens.add(TextToken.SPACE);
            tokens.add(StartBlockToken.START_BLOCK);
            tokens.add(NewLineToken.NEWLINE_1);
                // return null;
                tokens.add(new LineNumberToken(18));
                tokens.add(RETURN);
                tokens.add(TextToken.SPACE);
                tokens.add(NULL);
                tokens.add(TextToken.SEMICOLON);
                tokens.add(NewLineToken.NEWLINE_1);
            tokens.add(EndBlockToken.END_BLOCK);
            tokens.add(NewLineToken.NEWLINE_1);
        tokens.add(EndBlockToken.END_BLOCK);
        tokens.add(TextToken.COMMA);
        tokens.add(NewLineToken.NEWLINE_1);

        tokens.add(new LineNumberToken(21));
        tokens.add(new TextToken("c"));
        tokens.add(EndBlockToken.END_PARAMETERS_BLOCK);
        tokens.add(TextToken.SEMICOLON);
        tokens.add(NewLineToken.NEWLINE_1);

        // System.out.println(i);
        tokens.add(new LineNumberToken(22));
        tokens.add(new ReferenceToken(ReferenceToken.TYPE, "java/lang/System", "System", null, "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(TextToken.DOT);
        tokens.add(new ReferenceToken(ReferenceToken.FIELD, "java/lang/System", "out", "java/io/PrintStream", "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(TextToken.DOT);
        tokens.add(new ReferenceToken(ReferenceToken.METHOD, "java/io/PrintStream", "println", "(I)V", "org/jd/core/v1/service/test/WriteTokenTest"));
        tokens.add(TextToken.LEFTROUNDBRACKET);
        tokens.add(new TextToken("i"));
        tokens.add(TextToken.RIGHTROUNDBRACKET);
        tokens.add(TextToken.SEMICOLON);
        tokens.add(NewLineToken.NEWLINE_1);

        // }\n
        tokens.add(EndBlockToken.END_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);

        // }
        tokens.add(EndBlockToken.END_BLOCK);

        PlainTextMetaPrinter printer = new PlainTextMetaPrinter();
        WriteTokenProcessor writer = new WriteTokenProcessor();

        Message message = new Message(tokens);
        message.setHeader("printer", printer);
        message.setHeader("maxLineNumber", 22);
        message.setHeader("majorVersion", 0);
        message.setHeader("minorVersion", 0);

        writer.process(message);

        String source = printer.toString();

        System.out.println("- - - - - - - - ");
        System.out.print(source);
        System.out.println("- - - - - - - - ");

        String expected =
            "/*  1:  0 */ package org.jd.core.v1.service.test;\n" +
            "/*  2:  0 */ \n" +
            "/*  3:  0 */ import java.util.ArrayList<META-REFERENCE type='TYPE' internalName='java/util/ArrayList' descriptor='null' ownerInternalName='null'/>;\n" +
            "/*  4:  0 */ import static org.junit.Assert.*<META-REFERENCE type='TYPE' internalName='org/junit/Assert/*' descriptor='null' ownerInternalName='null'/>;\n" +
            "/*  5:  0 */ \n" +
            "/*  6:  0 */ public class WriteTokenTest<META-DECLARATION type='TYPE' internalName='org/jd/core/v1/service/test/WriteTokenTest' descriptor='null'/> {\n" +
            "/*  7:  0 */   public static void main<META-DECLARATION type='METHOD' internalName='org/jd/core/v1/service/test/WriteTokenTest' descriptor='([Ljava/lang/String;)V'/>(String<META-REFERENCE type='TYPE' internalName='java/lang/String' descriptor='null' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>[] args) {\n" +
            "/*  8:  8 */     if (args == null)\n" +
            "/*  9:  0 */       return;\n" +
            "/* 10: 10 */     int i = call<META-REFERENCE type='METHOD' internalName='org/jd/core/v1/service/test/WriteTokenTest' descriptor='(IILjava/util/Enumeration;I)V' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>(\n" +
            "/* 11: 11 */       \"aaaa\"<META-STRING ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>,\n" +
            "/* 12: 12 */       b,\n" +
            "/* 13: 13 */       new Enumeration<META-REFERENCE type='TYPE' internalName='java/util/Enumeration' descriptor='null' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>() {\n" +
            "/* 14:  0 */         public boolean hasMoreElements<META-DECLARATION type='METHOD' internalName='org/jd/core/v1/service/test/WriteTokenTest$1' descriptor='()Z'/>() {\n" +
            "/* 15: 15 */           return false;\n" +
            "/* 16:  0 */         }\n" +
            "/* 17:  0 */         public Object<META-REFERENCE type='TYPE' internalName='java/lang/Object' descriptor='null' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/> nextElement<META-DECLARATION type='METHOD' internalName='org/jd/core/v1/service/test/WriteTokenTest$1' descriptor='()Ljava/lang/Object;'/>() {\n" +
            "/* 18: 18 */           return null;\n" +
            "/* 19:  0 */         }\n" +
            "/* 20:  0 */       },\n" +
            "/* 21: 21 */       c);\n" +
            "/* 22: 22 */     System<META-REFERENCE type='TYPE' internalName='java/lang/System' descriptor='null' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>.out<META-REFERENCE type='FIELD' internalName='java/lang/System' descriptor='java/io/PrintStream' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>.println<META-REFERENCE type='METHOD' internalName='java/io/PrintStream' descriptor='(I)V' ownerInternalName='org/jd/core/v1/service/test/WriteTokenTest'/>(i);\n" +
            "/* 23:  0 */   }\n" +
            "/* 24:  0 */ }\n";

        Assert.assertEquals(expected, source);
    }


    @Test
    public void testComments() throws Exception {
        DefaultList<Token> tokens = new DefaultList<>();

        // package org.jd.core.v1.service.writer;\n\n
        tokens.add(PACKAGE);
        tokens.add(new TextToken(" org.jd.core.v1.service.test;"));
        tokens.add(NewLineToken.NEWLINE_2);

        // /* Block comment */\n
        tokens.add(StartMarkerToken.COMMENT);
        tokens.add(new TextToken("/* Block comment */"));
        tokens.add(EndMarkerToken.COMMENT);
        tokens.add(NewLineToken.NEWLINE_1);

        // /* Javadoc comment */\n
        tokens.add(StartMarkerToken.JAVADOC);
        tokens.add(new TextToken("/** Javadoc comment */"));
        tokens.add(EndMarkerToken.JAVADOC);
        tokens.add(NewLineToken.NEWLINE_1);

        // public class WriteCommentTest {\n
        tokens.add(PUBLIC);
        tokens.add(TextToken.SPACE);
        tokens.add(CLASS);
        tokens.add(TextToken.SPACE);
        tokens.add(new DeclarationToken(DeclarationToken.TYPE, "org/jd/core/v1/service/test/WriteTokenTest", "WriteTokenTest", null));
        tokens.add(TextToken.SPACE);
        tokens.add(StartBlockToken.START_BLOCK);
        tokens.add(NewLineToken.NEWLINE_1);

        // }
        tokens.add(EndBlockToken.END_BLOCK);

        PlainTextMetaPrinter printer = new PlainTextMetaPrinter();
        WriteTokenProcessor writer = new WriteTokenProcessor();

        Message message = new Message(tokens);
        message.setHeader("printer", printer);
        message.setHeader("maxLineNumber", 0);
        message.setHeader("majorVersion", 0);
        message.setHeader("minorVersion", 0);

        writer.process(message);

        String source = printer.toString();

        System.out.println("- - - - - - - - ");
        System.out.print(source);
        System.out.println("- - - - - - - - ");

        Assert.assertTrue(source.indexOf("<MARKER type='COMMENT'>/* Block comment */</MARKER type='COMMENT'>") != -1);
        Assert.assertTrue(source.indexOf("<MARKER type='JAVADOC'>/** Javadoc comment */</MARKER type='JAVADOC'>") != -1);
    }
}
