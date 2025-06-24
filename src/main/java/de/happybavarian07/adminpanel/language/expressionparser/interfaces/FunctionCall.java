package de.happybavarian07.adminpanel.language.expressionparser.interfaces;

import de.happybavarian07.adminpanel.language.expressionparser.Interpreter;

import java.util.List;

@FunctionalInterface
public interface FunctionCall {
    Object call(Interpreter interpreter, List<Object> arguments, String callType);
}
