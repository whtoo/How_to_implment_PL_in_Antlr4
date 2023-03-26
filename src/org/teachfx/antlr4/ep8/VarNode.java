package org.teachfx.antlr4.ep8;

public class VarNode extends RawValueNode {

    public VarNode(String rawText) {
        super(rawText);
    }

    public String getVarName() {
        return this.getRawText();
    }

    public String toString() {
        return " ( VAR " + this.getRawText() + " ) ";
    }
}
