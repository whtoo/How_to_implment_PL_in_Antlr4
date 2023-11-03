package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

public interface IOperatorEmitter {

    public String emitBinaryOp(OperatorType.BinaryOpType binaryOpType);
    public String emitUnaryOp(OperatorType.UnaryOpType unaryOpType);
}
