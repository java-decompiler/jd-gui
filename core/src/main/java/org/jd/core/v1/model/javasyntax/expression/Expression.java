/*
 * Copyright (c) 2008-2019 Emmanuel Dupuy.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.core.v1.model.javasyntax.expression;

import org.jd.core.v1.api.printer.Printer;
import org.jd.core.v1.model.javasyntax.type.Type;

/* Priority  Operator   Operation                              Order of Evaluation
 * 0         new        Object creation
 * 1         [ ]        Array index                            Left to Right
 *           ()         Method call
 *           .          Member access
 *           ++         Postfix increment
 *           --         Postfix decrement
 * 2         ++         Prefix increment                       Right to Left
 *           --         Prefix decrement
 *           + -        Unary plus, minus
 *           ~          Bitwise NOT
 *           !          Boolean (logical) NOT
 * 3         (type)     Type cast                              Right to Left
 * 4         +          String concatenation                   Left to Right
 * 5         * / %      Multiplication, division, remainder    Left to Right
 * 6         + -        Addition, subtraction                  Left to Right
 * 7         <<         Signed bit shift left to right         Left to Right
 *           >>         Signed bit shift right to left
 *           >>>        Unsigned bit shift right to left
 * 8         < <=       Less than, less than or equal to       Left to Right
 *           > >=       Greater than, greater than or equal to
 *           instanceof Reference test
 * 9         ==         Equal to                               Left to Right
 *           !=         Not equal to
 * 10        &          Bitwise AND                            Left to Right
 *           &          Boolean (logical) AND
 * 11        ^          Bitwise XOR                            Left to Right
 *           ^          Boolean (logical) XOR
 * 12        |          Bitwise OR                             Left to Right
 *           |          Boolean (logical) OR
 * 13        &&         Boolean (logical) AND                  Left to Right
 * 14        ||         Boolean (logical) OR                   Left to Right
 * 15        ? :        Conditional                            Right to Left
 * 16        =          Assignment                             Right to Left
 *           *= /= +=   Combinated assignment
 *           -= %=      (operation and assignment)
 *           <<= >>=
 *           >>>=
 *           &= ^= |=
 * 17        ->         Lambda                                 Right to Left
 *
 * References:
 * - http://introcs.cs.princeton.edu/java/11precedence
 * - The Java® Language Specification Java SE 8 Edition, 15.2 Forms of Expressions
 */
public interface Expression extends BaseExpression {
    int UNKNOWN_LINE_NUMBER = Printer.UNKNOWN_LINE_NUMBER;

    int getLineNumber();

    Type getType();

    int getPriority();
}
