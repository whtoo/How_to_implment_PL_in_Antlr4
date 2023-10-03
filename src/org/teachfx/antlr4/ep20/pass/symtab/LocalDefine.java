package org.teachfx.antlr4.ep20.pass.symtab;

import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep20.pass.ast.ASTBaseVisitor;
import org.teachfx.antlr4.ep20.symtab.GlobalScope;
import org.teachfx.antlr4.ep20.symtab.LocalScope;
import org.teachfx.antlr4.ep20.symtab.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.Scope;

import java.util.Stack;

public class LocalDefine extends ASTBaseVisitor {
    Stack<Scope> scopeStack = new Stack<>();
    private Scope currentScope = new GlobalScope();

    @Override
    public void visit(VarDeclNode varDeclNode) {
        super.visit(varDeclNode);
        currentScope.define(varDeclNode.getRefSymbol());
    }

    @Override
    public void visit(FuncDeclNode funcDeclNode) {
        var methodScope = new MethodSymbol(funcDeclNode.getDeclName(),
                funcDeclNode.getRetTypeNode().getBaseType(),
                currentScope,funcDeclNode);
        stashScope(methodScope);
        super.visit(funcDeclNode);
        currentScope = popScope();
    }

    @Override
    public void visit(BlockStmtNode blockStmtNode) {
        var blockScope = new LocalScope(currentScope);
        stashScope(blockScope);
        super.visit(blockStmtNode);
        currentScope = popScope();
    }


    protected void stashScope(Scope scope) {
        scopeStack.push(scope);
        currentScope = scope;
    }

    protected Scope popScope() {
        return scopeStack.pop();
    }


}
