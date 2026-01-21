package org.teachfx.antlr4.ep21.pass.symtab;

import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep21.ast.expr.ArrayAccessExprNode;
import org.teachfx.antlr4.ep21.ast.expr.CallFuncNode;
import org.teachfx.antlr4.ep21.ast.expr.ExprNode;
import org.teachfx.antlr4.ep21.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.pass.ast.ASTBaseVisitor;
import org.teachfx.antlr4.ep21.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep21.symtab.scope.LocalScope;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.symbol.Symbol;

import java.util.Objects;
import java.util.Stack;

public class LocalDefine extends ASTBaseVisitor {
    Stack<Scope> scopeStack = new Stack<>();

    Stack<Scope> loopStack = new Stack<>();

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
        System.out.println("[LocalDefine] Visiting IDExprNode: " + idExprNode.getImage()
            + " (object: " + System.identityHashCode(idExprNode) + ")"
            + ", currentScope: " + currentScope);
        Symbol symbol = currentScope.resolve(idExprNode.getImage());
        System.out.println("[LocalDefine] Resolving symbol: " + idExprNode.getImage()
            + ", found: " + (symbol != null ? symbol.getClass().getSimpleName() : "null"));
        if (Objects.nonNull(symbol)) {
            idExprNode.setRefSymbol(symbol);
            System.out.println("[LocalDefine] Set refSymbol for " + idExprNode.getImage()
                + ": " + symbol);
        } else {
            System.out.println("[LocalDefine] Undefined symbol: " + idExprNode.getImage());
            // Print scope hierarchy
            Scope scope = currentScope;
            int depth = 0;
            while (scope != null) {
                System.out.println("[LocalDefine]   Scope depth " + depth + ": " + scope);
                scope = scope.getEnclosingScope();
                depth++;
            }
        }
        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        System.out.println("[LocalDefine] Visiting VarDeclNode: " + varDeclNode.getDeclName()
            + " (object: " + System.identityHashCode(varDeclNode) + ")"
            + ", refSymbol: " + varDeclNode.getRefSymbol()
            + ", assignExprNode: " + (varDeclNode.getAssignExprNode() != null ?
                varDeclNode.getAssignExprNode() + " (object: " + System.identityHashCode(varDeclNode.getAssignExprNode()) + ")" : "null")
            + ", idExprNode: " + varDeclNode.getIdExprNode() + " (object: " + System.identityHashCode(varDeclNode.getIdExprNode()) + ")");
        super.visit(varDeclNode);
        currentScope.define(varDeclNode.getRefSymbol());
        System.out.println("[LocalDefine] Defined symbol: " + varDeclNode.getRefSymbol());

        return null;
    }

    @Override
    public Void visit(ArrayAccessExprNode arrayAccessExprNode) {
        System.out.println("[LocalDefine] Visiting ArrayAccessExprNode: " + arrayAccessExprNode
            + " (object: " + System.identityHashCode(arrayAccessExprNode) + ")");
        ExprNode arrayExpr = arrayAccessExprNode.getArray();
        System.out.println("[LocalDefine] Array access array expression: " + arrayExpr
            + " (object: " + System.identityHashCode(arrayExpr) + ")");
        if (arrayExpr instanceof IDExprNode idExprNode) {
            System.out.println("[LocalDefine] Array expression is IDExprNode: " + idExprNode.getImage()
                + " (object: " + System.identityHashCode(idExprNode) + ")"
                + ", refSymbol before: " + idExprNode.getRefSymbol());
        }
        return super.visit(arrayAccessExprNode);
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
        if (blockStmtNode.getParentScopeType() == ScopeType.FuncScope) {
            super.visit(blockStmtNode);
            return null;
        }

        blockStmtNode.setScope(currentScope);
        var blockScope = new LocalScope(currentScope);
        stashScope(blockScope);
        super.visit(blockStmtNode);
        currentScope = popScope();
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        var loopScope = new LocalScope(currentScope,ScopeType.LoopScope);

        whileStmtNode.setScope(currentScope);
        pushLoopScope(loopScope);
        stashScope(loopScope);
        super.visit(whileStmtNode);
        currentScope= popScope();
        popLoopScope();

        return null;
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        var ifScope = new LocalScope(currentScope);

        ifStmtNode.setScope(currentScope);
        stashScope(ifScope);
        super.visit(ifStmtNode);
        currentScope = popScope();
        return null;
    }

    @Override
    public Void visit(BreakStmtNode breakStmtNode) {
        breakStmtNode.setScope(currentScope);

        if (!loopStack.isEmpty()) {
            return super.visit(breakStmtNode);
        }

        System.out.println("Break stmt must be in while scope");
        return null;
    }

    @Override
    public Void visit(ContinueStmtNode continueStmtNode) {
        continueStmtNode.setScope(currentScope);
        if (!loopStack.isEmpty()) {
            return super.visit(continueStmtNode);
        }
        System.out.println("Continue stmt must be in while scope");
        return null;
    }

    @Override
    public Void visit(CallFuncNode callExprNode) {
        var funcSymbol = (MethodSymbol) currentScope.resolve(callExprNode.getFuncName());
        callExprNode.setCallFuncSymbol(funcSymbol);
        return super.visit(callExprNode);
    }

    protected void stashScope(org.teachfx.antlr4.ep21.symtab.scope.Scope scope) {
        scopeStack.push(scope);
        currentScope = scope;
    }

    protected Scope popScope() {
        if (!scopeStack.empty()) return scopeStack.pop();
        if (!scopeStack.empty()) return scopeStack.peek();
        return topScope;
    }

    protected Scope getTopScope() {
        return topScope;
    }

    public void setTopScope(Scope topScope) {
        this.topScope = topScope;
    }

    // generate push/pop loop scope methods for loopStack
    public void pushLoopScope(Scope scope) {
        loopStack.push(scope);
    }

    public Scope popLoopScope() {
        if (loopStack.isEmpty()) return null;
        return loopStack.pop();
    }
}
