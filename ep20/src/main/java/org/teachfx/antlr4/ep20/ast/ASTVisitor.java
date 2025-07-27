package org.teachfx.antlr4.ep20.ast;

import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.StructDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.StructMemberNode;
import org.teachfx.antlr4.ep20.ast.decl.TypedefDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;

public interface ASTVisitor<S,E> {
    /// Root Node
    S visit(CompileUnit rootNode);

    /// Decl
    S visit(VarDeclNode varDeclNode);

    S visit(FuncDeclNode funcDeclNode);

    S visit(VarDeclStmtNode varDeclStmtNode);
    
    S visit(StructDeclNode structDeclNode);
    
    S visit(StructMemberNode structMemberNode);
    
    S visit(TypedefDeclNode typedefDeclNode);

    /// Type
    E visit(TypeNode typeNode);

    // Expr
    default E visit(ExprNode node) {
        if (node instanceof BinaryExprNode) {
            return visit((BinaryExprNode) node);
        } else if (node instanceof IDExprNode) {
            return visit((IDExprNode) node);
        } else if (node instanceof BoolExprNode) {
            return visit((BoolExprNode) node);
        } else if (node instanceof CallFuncNode) {
            return visit((CallFuncNode) node);
        } else if (node instanceof IntExprNode) {
            return visit((IntExprNode) node);
        } else if (node instanceof FloatExprNode) {
            return visit((FloatExprNode) node);
        } else if (node instanceof NullExprNode) {
            return visit((NullExprNode) node);
        } else if (node instanceof StringExprNode) {
            return visit((StringExprNode) node);
        } else if (node instanceof UnaryExprNode) {
            return visit((UnaryExprNode) node);
        } else if (node instanceof ArrayLiteralNode) {
            return visit((ArrayLiteralNode) node);
        } else if (node instanceof CastExprNode) {
            return visit((CastExprNode) node);
        } else if (node instanceof FieldAccessNode) {
            return visit((FieldAccessNode) node);
        }
        return null;
    }
    E visit(BinaryExprNode binaryExprNode);

    E visit(IDExprNode idExprNode);

    /// literal value
    E visit(BoolExprNode boolExprNode);

    E visit(CallFuncNode callExprNode);

    E visit(IntExprNode intExprNode);

    E visit(FloatExprNode floatExprNode);

    E visit(NullExprNode nullExprNode);

    E visit(StringExprNode stringExprNode);

    E visit(UnaryExprNode unaryExprNode);
    
    E visit(ArrayLiteralNode arrayLiteralNode);
    
    E visit(CastExprNode castExprNode);
    
    E visit(FieldAccessNode fieldAccessNode);

    /// Stmt
    S visit(IfStmtNode ifStmtNode);

    S visit(ExprStmtNode exprStmtNode);

    S visit(BlockStmtNode blockStmtNode);

    S visit(ReturnStmtNode returnStmtNode);

    S visit(WhileStmtNode whileStmtNode);

    S visit(AssignStmtNode assignStmtNode);

    S visit(BreakStmtNode breakStmtNode);

    S visit(ContinueStmtNode continueStmtNode);
    default S visit(StmtNode node) {
        if (node instanceof IfStmtNode) {
            return visit((IfStmtNode) node);
        } else if (node instanceof WhileStmtNode) {
            return visit((WhileStmtNode) node);
        } else if (node instanceof BlockStmtNode) {
            return visit((BlockStmtNode) node);
        } else if (node instanceof ExprStmtNode) {
            return visit((ExprStmtNode) node);
        } else if (node instanceof ReturnStmtNode) {
            return visit((ReturnStmtNode) node);
        } else if (node instanceof AssignStmtNode) {
            return visit((AssignStmtNode) node);
        } else if (node instanceof BreakStmtNode) {
            return visit((BreakStmtNode) node);
        } else if (node instanceof ContinueStmtNode) {
            return visit((ContinueStmtNode) node);
        } else {
            return visit((VarDeclStmtNode) node);
        }
    }
}
