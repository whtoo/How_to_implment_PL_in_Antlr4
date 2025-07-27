package org.teachfx.antlr4.ep20.pass.ast;

import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclListNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.parser.CymbolVisitor;
import org.teachfx.antlr4.ep20.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep20.symtab.type.ArrayType;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType.BinaryOpType;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType.UnaryOpType;

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
            if(node instanceof VarDeclNode varDeclNode) {
                compilationUnit.addVarDecl(varDeclNode);
            } else if (node instanceof FuncDeclNode funcDeclNode) {
                compilationUnit.addFuncDecl(funcDeclNode);
            }
        }
        compilationUnit.setCtx(ctx);
        return compilationUnit;
    }

    @Override
    public ASTNode visitVarDecl(CymbolParser.VarDeclContext ctx) {
        var typeNode = (TypeNode)visit(ctx.primaryType());
        var symbol = new VariableSymbol(ctx.ID().getText(),typeNode.getBaseType());
        var assignNode  = (ExprNode) visit(ctx.arrayInitializer());
        var idExprNode = new IDExprNode(ctx.ID().getText(),null);
        idExprNode.setRefSymbol(symbol);

        return new VarDeclNode(symbol,assignNode,idExprNode,ctx);
    }

    @Override
    public ASTNode visitPrimaryType(CymbolParser.PrimaryTypeContext ctx) {

        switch(ctx.getText()) {
            case "Bool" -> {
                return TypeNode.BoolNode;
            }
            case "Void" -> {
                return TypeNode.VoidNode;
            }
            case "String" -> {
                return TypeNode.StrNode;
            }
            case "Object" -> {
                return TypeNode.ObjNode;
            }
            default -> {
                return TypeNode.IntNode;
            }
        }
    }

    @Override
    public ASTNode visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        var paramSlots = Objects.nonNull(ctx.params) ? (VarDeclListNode)visit(ctx.params) : null;
        paramSlots = paramSlots == null ? new VarDeclListNode(List.of(),ctx) : paramSlots;
        var retType = (TypeNode)visit(ctx.retType);
        var funcName = ctx.funcName.getText();

        var bodyStmt = (BlockStmtNode) (ctx.blockDef != null ? visit(ctx.blockDef) : null);

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
        var stmtList = ctx.block().statetment().stream().map((stmtCtx)-> (StmtNode)visit(stmtCtx))
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
        var retVal = (ExprNode)visit(ctx.expr());
        return new ReturnStmtNode(retVal,ctx);
    }

    @Override
    public ASTNode visitStateCondition(CymbolParser.StateConditionContext ctx) {
        ExprNode condNode = (ExprNode) visit(ctx.cond);
        StmtNode thenBlock = (StmtNode) visit(ctx.then);
        StmtNode elseBlock = (StmtNode) (ctx.elseDo != null ? visit(ctx.elseDo) : null);

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
    public ASTNode visitExprArrayAccess(CymbolParser.ExprArrayAccessContext ctx) {
        var array = (ExprNode) visit(ctx.expr(0));
        var index = (ExprNode) visit(ctx.expr(1));
        return new ArrayAccessNode(array, index, ctx);
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
