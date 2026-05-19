package org.teachfx.antlr4.ep08.ir;

import java.util.List;

/**
 * CallIR: call a function with arguments.
 * TAC: result = call funcName(arg1, arg2, ...)
 * If the function returns void, result is null.
 */
public class CallIR extends IRInstruction {
    public final String result;   // null if void
    public final String funcName;
    public final List<String> args;

    public CallIR(String result, String funcName, List<String> args) {
        this.result = result;
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (result != null) {
            sb.append(result).append(" = ");
        }
        sb.append("call ").append(funcName).append("(");
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(args.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
