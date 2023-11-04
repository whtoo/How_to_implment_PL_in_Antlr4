package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

public interface IOperatorEmitter {

    String emitBinaryOp(OperatorType.BinaryOpType binaryOpType);
    String emitUnaryOp(OperatorType.UnaryOpType unaryOpType);
}
