package org.teachfx.antlr4.ep21.pass.ast;

import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclListNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep21.ast.expr.*;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;
import org.teachfx.antlr4.ep21.CymbolBaseVisitor;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.CymbolVisitor;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.BinaryOpType;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType;

import java.util.List;
import java.util.Objects;


public class CymbolASTBuilder extends CymbolBaseVisitor<ASTNode> implements CymbolVisitor<ASTNode> {

    public CymbolASTBuilder() {
        super();

    }

    public static CymbolASTBuilder build(CymbolParser.CompilationUnitContext ctx) {
        var astBuilder = new CymbolASTBuilder();

        astBuilder.visit(ctx);
        return astBuilder;
    }


    @Override
    public ASTNode visitCompilationUnit(CymbolParser.CompilationUnitContext ctx) {
        var compilationUnit = new CompileUnit();
        for(var childNode : ctx.children) {
            var node = visit(childNode);
            // Java 21 模式匹配增强
            switch (node) {
                case VarDeclNode varDeclNode -> compilationUnit.addVarDecl(varDeclNode);
                case FuncDeclNode funcDeclNode -> compilationUnit.addFuncDecl(funcDeclNode);
                case null, default -> { /* 忽略其他类型节点 */ }
            }
        }
        compilationUnit.setCtx(ctx);
        return compilationUnit;
    }

    @Override
    public ASTNode visitVarDecl(CymbolParser.VarDeclContext ctx) {
        var typeNode = (TypeNode)visit(ctx.primaryType());
        var symbol = new VariableSymbol(ctx.ID().getText(),typeNode.getBaseType());
        var assignNode  = (ExprNode) visit(ctx.expr());
        var idExprNode = new IDExprNode(ctx.ID().getText(), ctx);
        idExprNode.setRefSymbol(symbol);

        return new VarDeclNode(symbol,assignNode,idExprNode,ctx);
    }

    @Override
    public ASTNode visitPrimaryType(CymbolParser.PrimaryTypeContext ctx) {
        // Java 21 改进的switch表达式
        return switch(ctx.getText()) {
            case "Bool" -> TypeNode.BoolNode;
            case "Void" -> TypeNode.VoidNode;
            case "String" -> TypeNode.StrNode;
            case "Object" -> TypeNode.ObjNode;
            default -> TypeNode.IntNode;
        };
    }

    @Override
    public ASTNode visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        var paramSlots = Objects.nonNull(ctx.params) ? (VarDeclListNode)visit(ctx.params) : null;
        paramSlots = paramSlots == null ? new VarDeclListNode(List.of(),ctx) : paramSlots;
        var retType = (TypeNode)visit(ctx.retType);
        var funcName = ctx.funcName.getText();

        var bodyStmt = (BlockStmtNode) (ctx.blockDef != null ? visit(ctx.blockDef) : null);

        // Ensure bodyStmt is never null - if it is, create an empty block
        if (bodyStmt == null) {
            bodyStmt = new BlockStmtNode(List.of(), ctx);
        }

        return new FuncDeclNode(retType,funcName,paramSlots,bodyStmt,ctx);
    }

    @Override
    public ASTNode visitFormalParameters(CymbolParser.FormalParametersContext ctx) {
        var paramSlots = ctx.children.stream().filter(n -> n instanceof CymbolParser.FormalParameterContext).map((paramNode)-> (VarDeclNode)visit(paramNode)).toList();

        return new VarDeclListNode(paramSlots,ctx);
    }

    @Override
    public ASTNode visitFormalParameter(CymbolParser.FormalParameterContext ctx) {
         TypeNode paramTypeNode = (TypeNode)visit(ctx.primaryType());
         String paramName = ctx.ID().getText();

         return new VarDeclNode(new VariableSymbol(paramName, paramTypeNode.getBaseType()),null,null,ctx);
    }

    @Override
    public ASTNode visitBlock(CymbolParser.BlockContext ctx) {
        var stmtNodeStream = ctx.children.stream().map((stmtCtx)-> (StmtNode)visit(stmtCtx));
        var stmtList = stmtNodeStream.filter(Objects::nonNull).toList();
        var stmtNode = new BlockStmtNode(stmtList,ctx);
        stmtNode.setParentScopeType(ScopeType.FuncScope);
        return stmtNode;
    }

    @Override
    public ASTNode visitStatBlock(CymbolParser.StatBlockContext ctx) {
        var stmtList = ctx.block().statement().stream().map((stmtCtx)-> (StmtNode)visit(stmtCtx))
                .filter(Objects::nonNull)
                .toList();
        var stmtNode = new BlockStmtNode(stmtList,ctx);
        stmtNode.setParentScopeType(ScopeType.BlockScope);
        return stmtNode;
    }

    @Override
    public ASTNode visitStatVarDecl(CymbolParser.StatVarDeclContext ctx) {
        var varDeclNode = (VarDeclNode)visit(ctx.varDecl());
        return new VarDeclStmtNode(varDeclNode,ctx);
    }

    @Override
    public ASTNode visitStatReturn(CymbolParser.StatReturnContext ctx) {
        ExprNode retVal = null;
        if (ctx.expr() != null) {
            retVal = (ExprNode)visit(ctx.expr());
        }
        return new ReturnStmtNode(retVal,ctx);
    }

    @Override
    public ASTNode visitStateCondition(CymbolParser.StateConditionContext ctx) {
        ExprNode condNode = (ExprNode) visit(ctx.cond);
        StmtNode thenBlock = (StmtNode) visit(ctx.then);
        StmtNode elseBlock = (StmtNode) (ctx.elseDo != null ? visit(ctx.elseDo) : null);

        // Ensure thenBlock is never null - if it is, create an empty block
        if (thenBlock == null) {
            thenBlock = new BlockStmtNode(List.of(), ctx.then);
        }
        // Ensure elseBlock is never null if else exists - if it is, create an empty block
        if (ctx.elseDo != null && elseBlock == null) {
            elseBlock = new BlockStmtNode(List.of(), ctx.elseDo);
        }

        return new IfStmtNode(condNode,thenBlock,elseBlock,ctx);
    }

    @Override
    public ASTNode visitStateWhile(CymbolParser.StateWhileContext ctx) {
        var condNode  = (ExprNode) visit(ctx.cond);
        var thenBlock = (BlockStmtNode) visit(ctx.then);
        return new WhileStmtNode(condNode,thenBlock,ctx);
    }

    @Override
    public ASTNode visitVisitBreak(CymbolParser.VisitBreakContext ctx) {
        return new BreakStmtNode(ctx);
    }

    @Override
    public ASTNode visitVisitContinue(CymbolParser.VisitContinueContext ctx) {
        return new ContinueStmtNode(ctx);
    }

    @Override
    public ASTNode visitStatAssign(CymbolParser.StatAssignContext ctx) {
        var lhs = (ExprNode) visit(ctx.expr(0));
        var rhs = (ExprNode) visit(ctx.expr(1));
        return new AssignStmtNode(lhs,rhs,ctx);
    }

    @Override
    public ASTNode visitExprStat(CymbolParser.ExprStatContext ctx) {
        var exprNode = (ExprNode) visit(ctx.expr());
        return  new ExprStmtNode(exprNode,ctx);
    }

    @Override
    public ASTNode visitExprBinary(CymbolParser.ExprBinaryContext ctx) {
        var lhs = (ExprNode)visit(ctx.expr(0));
        var rhs = (ExprNode)visit(ctx.expr(1));
        var operator = ctx.o.getText();

        return new BinaryExprNode(BinaryOpType.fromString(operator),lhs,rhs,ctx);
    }

    @Override
    public ASTNode visitExprGroup(CymbolParser.ExprGroupContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitExprUnary(CymbolParser.ExprUnaryContext ctx) {
        var exprNode = (ExprNode) visit(ctx.expr());
        return new UnaryExprNode(UnaryOpType.fromString(ctx.o.getText()),exprNode,ctx);
    }

    @Override
    public ASTNode visitExprFuncCall(CymbolParser.ExprFuncCallContext ctx) {
        List<ExprNode> argsNode = ctx.expr().stream().skip(1).map(arg -> (ExprNode) visit(arg)).toList();
        return new CallFuncNode(ctx.expr(0).getText(),argsNode,ctx);
    }

    @Override
    public ASTNode visitPrimaryID(CymbolParser.PrimaryIDContext ctx) {

        return new IDExprNode(ctx.getText(),ctx);
    }

    @Override
    public ASTNode visitPrimaryINT(CymbolParser.PrimaryINTContext ctx) {
        return new IntExprNode(Integer.valueOf(ctx.getText()),ctx);
    }

    @Override
    public ASTNode visitPrimaryFLOAT(CymbolParser.PrimaryFLOATContext ctx) {
        return new FloatExprNode(Double.valueOf(ctx.getText()),ctx);
    }

    @Override
    public ASTNode visitPrimarySTRING(CymbolParser.PrimarySTRINGContext ctx) {
        var size = ctx.getText().length();

        return new StringExprNode(ctx.getText().substring(1, size - 1),ctx);
    }

    @Override
    public ASTNode visitPrimaryBOOL(CymbolParser.PrimaryBOOLContext ctx) {
        return new BoolExprNode(Boolean.valueOf(ctx.getText()),ctx);
    }
}
