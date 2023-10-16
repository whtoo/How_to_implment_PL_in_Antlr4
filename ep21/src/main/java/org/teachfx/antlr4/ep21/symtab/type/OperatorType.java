package org.teachfx.antlr4.ep21.symtab.type;

public class OperatorType {
    public enum BinaryOpType  {
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        MOD("%"),
        NE("!="),
        LT("<"), LE("<="),EQ("=="), GT(">"), GE(">="),
        AND("&&"), OR("||");

        public String getOpRawVal() {
            return opRawVal;
        }

        private final String opRawVal;

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

        private final String opRawVal;

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
