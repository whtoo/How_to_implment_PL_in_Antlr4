package org.teachfx.antlr4.ep20.pass.symtab;

import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.CallFuncNode;
import org.teachfx.antlr4.ep20.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep20.ast.stmt.BlockStmtNode;
import org.teachfx.antlr4.ep20.pass.ast.ASTBaseVisitor;
import org.teachfx.antlr4.ep20.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.symbol.Symbol;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Stack;

public class LocalDefine extends ASTBaseVisitor {
    Stack<Scope> scopeStack = new Stack<>();
    private Scope currentScope = new GlobalScope();
    private Scope topScope = currentScope;

    public LocalDefine() {
        super();
    }

    @Override
    public Void visit(CompileUnit rootNode) {
        stashScope(topScope);
        super.visit(rootNode);
        popScope();
        return null;
    }

    // visit IDExprNode
    @Override
    public Void visit(IDExprNode idExprNode) {
        Symbol symbol = currentScope.resolve(idExprNode.getImage());
        if (Objects.nonNull(symbol)) {
            idExprNode.setRefSymbol(symbol);
        } else {
            System.out.println("Undefined symbol: " + idExprNode.getImage());
        }
        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        super.visit(varDeclNode);
        currentScope.define(varDeclNode.getRefSymbol());

        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        var methodScope = new MethodSymbol(funcDeclNode.getDeclName(),
                funcDeclNode.getRetTypeNode().getBaseType(),
                currentScope, funcDeclNode);
        methodScope.setArgs(funcDeclNode.getParamSlots().getVarDeclNodeList().size());
        funcDeclNode.setRefSymbol(methodScope);
        currentScope.define(methodScope);
        stashScope(methodScope);
        super.visit(funcDeclNode);
        currentScope = popScope();
        return null;
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        if (blockStmtNode.getParentScopeType() == BlockStmtNode.ScopeType.FuncScope) {
            super.visit(blockStmtNode);
            return null;
        }
        var blockScope = new org.teachfx.antlr4.ep20.symtab.scope.LocalScope(currentScope);
        stashScope(blockScope);
        super.visit(blockStmtNode);
        currentScope = popScope();
        return null;
    }

    @Override
    public Void visit(CallFuncNode callExprNode) {
        var funcSymbol = (MethodSymbol) currentScope.resolve(callExprNode.getFuncName());
        callExprNode.setCallFuncSymbol(funcSymbol);
        return super.visit(callExprNode);
    }

    protected void stashScope(org.teachfx.antlr4.ep20.symtab.scope.Scope scope) {
        scopeStack.push(scope);
        currentScope = scope;
    }

    protected Scope popScope() {
        if (!scopeStack.empty()) scopeStack.pop();
        if (!scopeStack.empty()) scopeStack.peek();
        return topScope;
    }

    protected Scope getTopScope() {
        return topScope;
    }

    public void setTopScope(Scope topScope) {
        this.topScope = topScope;
    }
}
