package org.teachfx.antlr4.ep20.ast;
import org.teachfx.antlr4.ep20.ast.decl.*;
import org.teachfx.antlr4.ep20.debugger.Dumper;

import java.util.ArrayList;
import java.util.List;

public class CompileUnit extends ASTNode {
    public void addFuncDecl(FuncDeclNode funcDecl) {
        this.funcDeclarations.add(funcDecl);
    }

    public void addVarDecl(VarDeclNode varDecl) {
        this.varDeclarations.add(varDecl);
    }

    private List<VarDeclNode> varDeclarations = new ArrayList<>();

    private List<FuncDeclNode> funcDeclarations = new ArrayList<>();

    public CompileUnit(List<VarDeclNode> varDeclarations,List<FuncDeclNode> funcDeclarations) {
        this.varDeclarations = varDeclarations;
        this.funcDeclarations = funcDeclarations;
    }

    public CompileUnit() {}

    public List<VarDeclNode> getVarDeclarations() {
        return varDeclarations;
    }

    public void setVarDeclarations(List<VarDeclNode> varDeclarations) {
        this.varDeclarations = varDeclarations;
    }

    public List<FuncDeclNode> getFuncDeclarations() {
        return funcDeclarations;
    }

    public void setFuncDeclarations(List<FuncDeclNode> funcDeclarations) {
        this.funcDeclarations = funcDeclarations;
    }

    @Override
    protected void _dump(Dumper d) {
        d.printClass(this,getLocation());
        d.printNodeList("varDecl",varDeclarations);
        d.printNodeList("funcDecl",funcDeclarations);
    }
}
