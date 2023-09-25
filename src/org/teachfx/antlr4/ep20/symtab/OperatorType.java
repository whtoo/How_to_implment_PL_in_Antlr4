package org.teachfx.antlr4.ep20.symtab;

public class OperatorType {
    public enum BinaryOpType  {
        ADD("+"),
        MIN("-"),
        MUL("*"),
        DIV("/"),
        LESS("<"),LESSEQ("<="),EQ("=="),GREATER(">"),GREATEREQ(">=");

        public String getOpRawVal() {
            return opRawVal;
        }

        private String opRawVal;

        BinaryOpType(String s) {
            this.opRawVal = s;
        }

        public static BinaryOpType fromString(String op) {
            for (BinaryOpType opType : BinaryOpType.values()) {
                if (opType.opRawVal.equals(op)) {
                    return opType;
                }
            }
            throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }

    public enum UnaryOpType {
        /// `-` op
        NEG("-"),
        /// `!` op
        NOT("!");

        public String getOpRawVal() {
            return opRawVal;
        }

        private String opRawVal;

        UnaryOpType(String s) {
            this.opRawVal = s;
        }

        public static UnaryOpType fromString(String op) {
            for (UnaryOpType opType : UnaryOpType.values()) {
                if (opType.opRawVal.equals(op)) {
                    return opType;
                }
            }
            throw new IllegalArgumentException("Invalid operator: " + op);
        }
    }
}
