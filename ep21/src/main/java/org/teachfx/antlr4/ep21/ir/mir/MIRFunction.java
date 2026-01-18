package org.teachfx.antlr4.ep21.ir.mir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MIR函数表示，封装函数级别的信息
 */
public class MIRFunction extends MIRNode {
    private final String name;
    private final List<MIRStmt> statements;
    private final Set<String> parameters;
    private final Set<String> localVariables;
    
    // Cache for variable information to avoid repeated computation
    private transient Set<String> cachedUsedVariables;
    private transient Set<String> cachedDefinedVariables;
    private transient boolean variableInfoValid;
    
    public MIRFunction(String name) {
        this.name = name;
        this.statements = new ArrayList<>();
        this.parameters = new HashSet<>();
        this.localVariables = new HashSet<>();
        this.variableInfoValid = false;
    }
    
    @Override
    public int getComplexityLevel() {
        return 0; // 函数级别，最高级抽象
    }
    
    @Override
    public Set<String> getUsedVariables() {
        if (!variableInfoValid || cachedUsedVariables == null) {
            cachedUsedVariables = computeUsedVariables();
            cachedDefinedVariables = computeDefinedVariables();
            variableInfoValid = true;
        }
        return cachedUsedVariables;
    }

    private Set<String> computeUsedVariables() {
        Set<String> used = new HashSet<>();
        for (MIRStmt stmt : statements) {
            used.addAll(stmt.getUsedVariables());
        }
        used.addAll(parameters);
        return used;
    }
    
    @Override
    public Set<String> getDefinedVariables() {
        if (!variableInfoValid || cachedDefinedVariables == null) {
            cachedUsedVariables = computeUsedVariables();
            cachedDefinedVariables = computeDefinedVariables();
            variableInfoValid = true;
        }
        return cachedDefinedVariables;
    }

    private Set<String> computeDefinedVariables() {
        Set<String> defined = new HashSet<>();
        defined.addAll(localVariables);
        defined.addAll(parameters);
        for (MIRStmt stmt : statements) {
            defined.addAll(stmt.getDefinedVariables());
        }
        return defined;
    }
    
    @Override
    public void accept(MIRVisitor<?> visitor) {
        visitor.visit(this);
    }
    
    public void addStatement(MIRStmt stmt) {
        statements.add(stmt);
        variableInfoValid = false;
    }
    
    public String getName() {
        return name;
    }
    
    public List<MIRStmt> getStatements() {
        return statements;
    }
    
    public void addParameter(String param) {
        parameters.add(param);
        variableInfoValid = false;
    }
    
    public void addLocalVariable(String var) {
        localVariables.add(var);
        variableInfoValid = false;
    }
}