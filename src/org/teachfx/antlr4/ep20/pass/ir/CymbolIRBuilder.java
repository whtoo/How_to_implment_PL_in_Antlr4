package org.teachfx.antlr4.ep20.pass.ir;

import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.expr.Var;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.ir.stmt.ExprStmt;
import org.teachfx.antlr4.ep20.ir.stmt.ReturnVal;
import org.teachfx.antlr4.ep20.ir.stmt.Stmt;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;


public class CymbolIRBuilder implements ASTVisitor<IRNode> {
    public Prog root = null;
    @Override
    public IRNode visit(CompileUnit rootNode) {
        root = new Prog(null);
        
        var funcLst = rootNode.getFuncDeclarations().stream().map(this::visit);
        funcLst.map(Func.class::cast).forEach(root::addFunc);
        return root;
    }

    @Override
    public IRNode visit(VarDeclNode varDeclNode) {
        return new Var(varDeclNode.getRefSymbol());
    }

    @Override
    public IRNode visit(FuncDeclNode funcDeclNode) {
        var bodyStmts = funcDeclNode.getBody().getStmtNodes().stream()
                .map(this::visit).toList();
        var bodyStmtsList = bodyStmts.stream().map(Stmt.class::cast).toList();
        return new Func(funcDeclNode.getDeclName(), (MethodSymbol) funcDeclNode.getRefSymbol(),bodyStmtsList);
    }

    @Override
    public IRNode visit(VarDeclStmtNode varDeclStmtNode) {
        if(varDeclStmtNode.getVarDeclNode().getAssignExprNode() != null) {
            return new Assign(new Var(varDeclStmtNode.getVarDeclNode().getRefSymbol()),(Expr) visit(varDeclStmtNode.getVarDeclNode().getAssignExprNode()));
        }
        return null;
    }

    @Override
    public IRNode visit(TypeNode typeNode) {
        return null;
    }

    @Override
    public IRNode visit(BinaryExprNode binaryExprNode) {
        var lhs = (Expr) visit(binaryExprNode.getLhs());
        var rhs = (Expr) visit(binaryExprNode.getRhs());
        return new BinExpr(binaryExprNode.getOpType(),lhs,rhs);
    }

    @Override
    public IRNode visit(IDExprNode idExprNode) {
        return new Var(idExprNode.getRefSymbol());
    }

    @Override
    public IRNode visit(BoolExprNode boolExprNode) {
        return new BoolVal(boolExprNode.getRawValue());
    }

    @Override
    public IRNode visit(IntExprNode intExprNode) {
        // generate IntVal from intExprNode
        return new IntVal(intExprNode.getRawValue());
    }

    @Override
    public IRNode visit(FloatExprNode floatExprNode) {
        return new IntVal(floatExprNode.getRawValue().intValue());
    }

    @Override
    public IRNode visit(NullExprNode nullExprNode) {
        return null;
    }

    @Override
    public IRNode visit(StringExprNode stringExprNode) {
        return new StringVal(stringExprNode.getRawValue());
    }

    @Override
    public IRNode visit(UnaryExprNode unaryExprNode) {
        var expr = (Expr) visit(unaryExprNode.getValExpr());
        return new UnaryExpr(unaryExprNode.getOpType(),expr);
    }

    @Override
    public IRNode visit(CallFuncNode callExprNode) {
        var args = callExprNode.getArgsNode().stream().map(this::visit).map(Expr.class::cast).toList();
        return new CallFunc(new Var(callExprNode.getCallFuncSymbol()),args);
    }

    @Override
    public IRNode visit(IfStmtNode ifStmtNode) {
        return null;
    }

    @Override
    public IRNode visit(ExprStmtNode exprStmtNode) {
        return new ExprStmt((Expr) visit(exprStmtNode.getExprNode()));
    }

    @Override
    public IRNode visit(BlockStmtNode blockStmtNode) {
        // blockStmtNode.getStmtNodes().stream().map(this::visit).map(Stmt.class::cast).toList();
        return null;
    }

    @Override
    public IRNode visit(ReturnStmtNode returnStmtNode) {
        return new ReturnVal((Expr)visit(returnStmtNode.getRetNode()));
    }

    @Override
    public IRNode visit(WhileStmtNode whileStmtNode) {
        return null;
    }

    @Override
    public IRNode visit(AssignStmtNode assignStmtNode) {
        return new Assign((Var) visit(assignStmtNode.getLhs()),(Expr) visit(assignStmtNode.getRhs()));
    }

}
