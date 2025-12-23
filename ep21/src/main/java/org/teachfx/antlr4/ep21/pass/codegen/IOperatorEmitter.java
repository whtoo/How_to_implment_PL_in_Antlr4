package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

/**
 * Operator emitter interface for VM-specific operator code generation.
 * <p>
 * This interface abstracts the emission of binary and unary operators
 * for different VM targets (StackVM, RegisterVM). Each VM implementation
 * can provide its own operator encoding strategy.
 * </p>
 */
public interface IOperatorEmitter {

    /**
     * Emits a binary operator for the target VM.
     *
     * @param binaryOpType the binary operator type (e.g., ADD, SUB, MUL, DIV)
     * @return the VM-specific instruction or encoding for the operator
     */
    String emitBinaryOp(OperatorType.BinaryOpType binaryOpType);

    /**
     * Emits a unary operator for the target VM.
     *
     * @param unaryOpType the unary operator type (e.g., NEG, NOT)
     * @return the VM-specific instruction or encoding for the operator
     */
    String emitUnaryOp(OperatorType.UnaryOpType unaryOpType);
}
