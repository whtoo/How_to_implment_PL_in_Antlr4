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
import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.BinaryOpType;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType.UnaryOpType;
import org.teachfx.antlr4.ep21.symtab.type.Type;
import org.teachfx.antlr4.ep21.symtab.type.TypeTable;
import org.teachfx.antlr4.ep21.symtab.type.ArrayType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.symtab.type.TypeTable;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;


public class CymbolASTBuilder extends CymbolBaseVisitor<ASTNode> implements CymbolVisitor<ASTNode> {

    private static final Logger logger = LogManager.getLogger(CymbolASTBuilder.class);
    private final Map<ParserRuleContext, ASTNode> astCache = new HashMap<>();

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
        // 支持两种数组声明格式：
        // 1. C风格: int[5] arr
        // 2. 原格式: int arr[5]

        Type varType;
        String varName;
        var typeNode = (TypeNode)visit(ctx.type());
        
        // Debug logging to see children
        logger.debug("visitVarDecl: text={}, childCount={}", ctx.getText(), ctx.getChildCount());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i);
            logger.debug("  child[{}]: {} - {}", i, child.getClass().getSimpleName(), child.getText());
        }

        // 检查是否为C风格数组声明：type '[' expr ']' ID
        // 新语法: (type '[' expr ']' ID | type ID ('[' expr ']')?) ...
        // 如果第一个expr()存在且是维度表达式（在type之后，ID之前）
        if (ctx.getChildCount() >= 5 &&
            ctx.getChild(1).getText().equals("[") &&
            ctx.getChild(2) instanceof CymbolParser.ExprContext &&
            ctx.getChild(3).getText().equals("]") &&
            ctx.getChild(4) instanceof org.antlr.v4.runtime.tree.TerminalNode &&
            ((org.antlr.v4.runtime.tree.TerminalNode) ctx.getChild(4)).getSymbol().getType() == CymbolParser.ID) {
            logger.debug("Detected C-style array declaration: type '[' expr ']' ID");

            // C风格: int[5] arr
            varName = ctx.ID().getText();

            // 创建数组类型
            varType = TypeTable.createArrayType(typeNode.getBaseType());
            typeNode.setBaseType(varType);
            typeNode.setDim(1);

            var symbol = new VariableSymbol(varName, varType);
            ExprNode assignNode = null;

            // Extract array size expression from ctx.getChild(2) (expr after '[')
            ExprNode sizeExpr = (ExprNode) visit(ctx.getChild(2));

            // 处理初始化值 - 临时跳过以避免EmptyStack错误
            // 后续需要正确的数组初始化处理
            if (ctx.arrayInitializer() != null) {
                // TODO: 实现数组初始化处理
                logger.debug("Array initializer detected - skipping for now");
                assignNode = null;
            } else if (ctx.getChildCount() > 5 && ctx.getChild(5).getText().equals("=")) {
                // 处理简单表达式赋值
                int assignIndex = -1;
                for (int i = 0; i < ctx.getChildCount(); i++) {
                    if (ctx.getChild(i).getText().equals("=")) {
                        assignIndex = i + 1;
                        break;
                    }
                }
                if (assignIndex > 0 && assignIndex < ctx.getChildCount()) {
                    assignNode = (ExprNode) visit(ctx.getChild(assignIndex));
                }
            }

            var idExprNode = new IDExprNode(varName, ctx);
            idExprNode.setRefSymbol(symbol);

            var varDeclNode = new VarDeclNode(symbol, assignNode, idExprNode, ctx);
            varDeclNode.setArraySizeExpr(sizeExpr);
            return varDeclNode;
        } else {
            logger.debug("Detected old-style array declaration or non-array declaration");
            // 原格式: int arr[5] 或 int arr
            // 检查是否为数组声明（语法：type ID['expr']?）
            varType = typeNode.getBaseType();
            ExprNode sizeExpr = null;
            
            // Detect array size expression by looking for '[' after ID
            boolean hasArrayBrackets = false;
            int openBracketIndex = -1;
            int closeBracketIndex = -1;
            for (int i = 0; i < ctx.getChildCount(); i++) {
                var child = ctx.getChild(i);
                if (child instanceof org.antlr.v4.runtime.tree.TerminalNode) {
                    var term = (org.antlr.v4.runtime.tree.TerminalNode) child;
                    if (term.getText().equals("[")) {
                        openBracketIndex = i;
                    } else if (term.getText().equals("]")) {
                        closeBracketIndex = i;
                    }
                }
            }
            if (openBracketIndex != -1 && closeBracketIndex != -1 && openBracketIndex < closeBracketIndex) {
                // There is an array size expression between brackets
                hasArrayBrackets = true;
                // Find expr child between openBracketIndex and closeBracketIndex
                for (int i = openBracketIndex + 1; i < closeBracketIndex; i++) {
                    var child = ctx.getChild(i);
                    if (child instanceof CymbolParser.ExprContext) {
                        sizeExpr = (ExprNode) visit(child);
                        break;
                    }
                }
                // If no expr found between brackets, sizeExpr remains null (empty array size)
                // Create array type
                varType = TypeTable.createArrayType(varType);
                typeNode.setBaseType(varType);
                typeNode.setDim(1);
            }
            
            var symbol = new VariableSymbol(ctx.ID().getText(), varType);
            ExprNode assignNode = null;

            // 处理初始化值：优先处理数组初始化，然后处理单个表达式
            if (ctx.arrayInitializer() != null) {
                // 数组初始化：{1, 2, 3}
                assignNode = (ExprNode) visit(ctx.arrayInitializer());
            } else {
                // 查找赋值符号
                int assignIndex = -1;
                for (int i = 0; i < ctx.getChildCount(); i++) {
                    if (ctx.getChild(i).getText().equals("=")) {
                        assignIndex = i + 1;
                        break;
                    }
                }
                if (assignIndex > 0 && assignIndex < ctx.getChildCount()) {
                    assignNode = (ExprNode) visit(ctx.getChild(assignIndex));
                }
            }

            var idExprNode = new IDExprNode(ctx.ID().getText(), ctx);
            idExprNode.setRefSymbol(symbol);

            var varDeclNode = new VarDeclNode(symbol, assignNode, idExprNode, ctx);
            varDeclNode.setArraySizeExpr(sizeExpr);
            return varDeclNode;
        }
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
         TypeNode paramTypeNode = (TypeNode)visit(ctx.type());
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
    public ASTNode visitExprArrayAccess(CymbolParser.ExprArrayAccessContext ctx) {
        // Check cache
        if (astCache.containsKey(ctx)) {
            System.out.println("[ASTBuilder] Returning cached ArrayAccessExprNode for ctx: " + System.identityHashCode(ctx));
            return astCache.get(ctx);
        }
        
        ExprNode arrayExpr = (ExprNode) visit(ctx.expr(0));
        ExprNode indexExpr = (ExprNode) visit(ctx.expr(1));
        System.out.println("[ASTBuilder] Creating ArrayAccessExprNode for arrayExpr: " + arrayExpr
            + ", indexExpr: " + indexExpr
            + ", ctx object: " + System.identityHashCode(ctx)
            + ", ctx text: " + ctx.getText()
            + ", parent: " + (ctx.parent != null ? ctx.parent.getClass().getSimpleName() : "null"));
        if (arrayExpr instanceof IDExprNode idExprNode) {
            System.out.println("[ASTBuilder] IDExprNode refSymbol: " + idExprNode.getRefSymbol()
                + " (object: " + System.identityHashCode(idExprNode) + ")");
        }
        ArrayAccessExprNode node = new ArrayAccessExprNode(arrayExpr, indexExpr, ctx);
        System.out.println("[ASTBuilder] Created ArrayAccessExprNode object: " + System.identityHashCode(node));
        astCache.put(ctx, node);
        return node;
    }

    @Override
    public ASTNode visitArrayInitializer(CymbolParser.ArrayInitializerContext ctx) {
        // 处理数组初始化语法：{expr, expr, ...}
        // 收集所有初始化元素表达式
        List<ExprNode> elements = ctx.expr().stream()
                .map(exprCtx -> (ExprNode) visit(exprCtx))
                .toList();

        // 创建数组初始化表达式节点
        return new ArrayInitializerExprNode(elements, ctx);
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
