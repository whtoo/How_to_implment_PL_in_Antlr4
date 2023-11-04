package org.teachfx.antlr4.ep20.pass.codegen;

import org.teachfx.antlr4.ep20.symtab.type.OperatorType;

public class CymbolVMIOperatorEmitter implements IOperatorEmitter {
    @Override
    public String emitBinaryOp(OperatorType.BinaryOpType binaryOpType) {
        switch (binaryOpType) {
            case ADD -> {
                return "iadd";
            }
            case SUB -> {
                return "isub";
            }
            case MUL -> {
                return "imult";
            }
            case DIV -> {
                return "idiv";
            }
            case MOD -> {
                return "imod";
            }
            case NE -> {
                return "ine";
            }
            case LT -> {
                return "ilt";
            }
            case LE -> {
                return "ile";
            }
            case EQ -> {
                return "ieq";
            }
            case GT -> {
                return "igt";
            }
            case GE -> {
                return "ige";
            }
            case AND -> {
                return "iand";
            }
            case OR -> {
                return "ior";
            }
        }
        return null;
    }

    @Override
    public String emitUnaryOp(OperatorType.UnaryOpType unaryOpType) {
        switch (unaryOpType){
            case NEG -> {
                return "ineg";
            }
            case NOT -> {
                return "inot";
            }
        }
        return null;
    }
}
