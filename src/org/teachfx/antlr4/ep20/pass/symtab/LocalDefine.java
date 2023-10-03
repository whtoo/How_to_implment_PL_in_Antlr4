package org.teachfx.antlr4.ep20.pass.symtab;

import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep20.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep20.pass.ast.ASTBaseVisitor;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

import java.util.Objects;
import java.util.Stack;

public class LocalDefine extends ASTBaseVisitor {
    Stack<org.teachfx.antlr4.ep20.symtab.scope.Scope> scopeStack = new Stack<>();
    private org.teachfx.antlr4.ep20.symtab.scope.Scope currentScope = new org.teachfx.antlr4.ep20.symtab.scope.GlobalScope();

    public LocalDefine() {
        super();
    }

    @Override
    public void visit(CompileUnit rootNode) {
        super.visit(rootNode);
    }

    // visit IDExprNode
    @Override
    public void visit(IDExprNode idExprNode) {
        Symbol symbol = currentScope.resolve(idExprNode.getImage());
        if (Objects.nonNull(symbol)) {
            idExprNode.setRefSymbol(symbol);
        } else {
            System.out.println("Undefined symbol: " + idExprNode.getImage());
        }
    }

    @Override
    public void visit(VarDeclNode varDeclNode) {
        super.visit(varDeclNode);
        currentScope.define(varDeclNode.getRefSymbol());
    }

    @Override
    public void visit(FuncDeclNode funcDeclNode) {
        var methodScope = new org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol(funcDeclNode.getDeclName(),
                funcDeclNode.getRetTypeNode().getBaseType(),
                currentScope,funcDeclNode);
        stashScope(methodScope);
        super.visit(funcDeclNode);
        currentScope = popScope();
    }

    @Override
    public void visit(BlockStmtNode blockStmtNode) {
        if (blockStmtNode.getParentScopeType() == BlockStmtNode.ParentScopeType.FuncScope) {
            super.visit(blockStmtNode);
            return;
        }
        var blockScope = new org.teachfx.antlr4.ep20.symtab.scope.LocalScope(currentScope);
        stashScope(blockScope);
        super.visit(blockStmtNode);
        currentScope = popScope();
    }


    protected void stashScope(org.teachfx.antlr4.ep20.symtab.scope.Scope scope) {
        scopeStack.push(scope);
        currentScope = scope;
    }

    protected Scope popScope() {
        return scopeStack.pop();
    }
}
