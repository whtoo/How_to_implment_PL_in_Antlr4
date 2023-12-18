package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

public interface IOperatorEmitter {

    String emitBinaryOp(OperatorType.BinaryOpType binaryOpType);
    String emitUnaryOp(OperatorType.UnaryOpType unaryOpType);
}
