package org.teachfx.antlr4.ep8;

public class BinArithNode extends ASTNode {
    OPType op;
    ASTNode lhs;
    ASTNode rhs;


    public BinArithNode(String rawText) {
        super(rawText);
    }

    public BinArithNode(OPType op, ASTNode lhs, ASTNode rhs) {
        super(" ( expr " + op.getText() + "  " + lhs.toString() + "  " + rhs.toString() + " ) ");
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return this.getRawText();
    }
}
