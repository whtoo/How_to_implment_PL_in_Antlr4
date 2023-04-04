package org.teachfx.antlr4.ep8;

public class RawValueNode extends ASTNode {

    public RawValueNode(String rawText) {
        super(rawText);
    }

    @Override
    public String toString() {
        return this.getRawText();
    }
}
