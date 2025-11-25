package org.teachfx.antlr4.ep21.ir.mir;

import java.util.HashSet;
import java.util.Set;

/**
 * MIR赋值语句
 */
public class MIRAssignStmt extends MIRStmt {
    private final String target;
    private final MIRExpr source;
    
    public MIRAssignStmt(String target, MIRExpr source) {
        this.target = target;
        this.source = source;
    }
    
    @Override
    public Set<String> getUsedVariables() {
        return source.getUsedVariables();
    }
    
    @Override
    public Set<String> getDefinedVariables() {
        Set<String> defined = new HashSet<>();
        defined.add(target);
        return defined;
    }
    
    @Override
    public void accept(MIRVisitor<?> visitor) {
        visitor.visit(this);
    }
    
    public String getTarget() {
        return target;
    }
    
    public MIRExpr getSource() {
        return source;
    }
}