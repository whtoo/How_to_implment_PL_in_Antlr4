package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.scope.BaseScope;
import org.teachfx.antlr4.ep19.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep19.symtab.scope.LocalScope;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.TypedefSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 */
/*
 * @author Arthur.Blitz
 * @description 变量消解-标记每个ast节点的作用域归属问题.
 * @purpose 解决变量的定位问题--属于哪个作用域
 */
public class LocalDefine extends CymbolASTVisitor<Object> {
    private static final Logger logger = LoggerFactory.getLogger(LocalDefine.class);
    private final ParseTreeProperty<Scope> scopes;
    private Scope currentScope = null;
    private Deque<Scope> scopeStack;

    public LocalDefine() {
        BaseScope globalScope = new GlobalScope();
        currentScope = globalScope;

        // 在全局作用域中注册所有基本类型
        globalScope.define(new Symbol("int", TypeTable.INT));
        globalScope.define(new Symbol("float", TypeTable.FLOAT));
        globalScope.define(new Symbol("bool", TypeTable.BOOLEAN));
        globalScope.define(new Symbol("char", TypeTable.CHAR));
        globalScope.define(new Symbol("void", TypeTable.VOID));
        globalScope.define(new Symbol("String", TypeTable.STRING));
        globalScope.define(new Symbol("Object", TypeTable.OBJECT));

        // 注册内置函数
        MethodSymbol printFuncSymbol = new MethodSymbol("print", globalScope, null);
        printFuncSymbol.builtin = true;
        // print函数可以接受任何类型的参数，所以不需要定义具体的参数
        // 这样在TypeCheckVisitor中会特殊处理print函数
        globalScope.define(printFuncSymbol);

        /// Define main entry
        MethodSymbol mainFuncSymbol = new MethodSymbol("main", globalScope, null);
        mainFuncSymbol.builtin = true;
        globalScope.define(mainFuncSymbol);

        scopes = new ParseTreeProperty<Scope>();
        scopeStack = new ArrayDeque<>();
        scopeStack.push(globalScope);
    }

    public ParseTreeProperty<Scope> getScopes() {
        return scopes;
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        stashScope(ctx);
        return super.visitVarDecl(ctx);
    }

    @Override
    public Object visitStatVarDecl(StatVarDeclContext ctx) {
        return super.visitStatVarDecl(ctx);
    }

    @Override
    public Object visitStructDecl(StructDeclContext ctx) {
        String structName = ctx.ID().getText();
        logger.debug("定义结构体: {}", structName);

        StructSymbol struct = new StructSymbol(structName, currentScope, ctx);
        currentScope.define(struct);

        scopes.put(ctx, struct);

        Scope savedScope = currentScope;
        currentScope = struct;

        super.visitStructDecl(ctx);

        currentScope = savedScope;

        return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        MethodSymbol methodScope = new MethodSymbol(Util.name(ctx), currentScope, ctx);
        methodScope.blockStmt = ctx.blockDef;
        methodScope.callee = (ParserRuleContext) ctx.parent;
        currentScope.define(methodScope);
        stashScope(ctx);
        pushScope(methodScope);
        super.visitFunctionDecl(ctx);
        popScope();
        return null;
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        super.visitFormalParameter(ctx);
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitBlock(BlockContext ctx) {

        Scope local = new LocalScope(currentScope);
        stashScope(ctx);
        pushScope(local);
        super.visitBlock(ctx);
        popScope();

        return null;
    }

    @Override
    public Object visitStructMemeber(StructMemeberContext ctx) {
        logger.debug("处理结构体成员: {}", Util.name(ctx));
        stashScope(ctx);

        if (currentScope instanceof StructSymbol) {
            StructSymbol structScope = (StructSymbol) currentScope;

            // 处理结构体字段
            if (ctx.ID() != null && ctx.block() == null) {
                VariableSymbol member = new VariableSymbol(Util.name(ctx));
                structScope.addField(member);
            }
            // 处理结构体方法
            else if (ctx.ID() != null && ctx.block() != null) {
                String methodName = ctx.ID().getText();
                logger.debug("定义结构体方法: {}", methodName);

                MethodSymbol methodSymbol = new MethodSymbol(methodName, currentScope, ctx);
                methodSymbol.blockStmt = ctx.block();
                methodSymbol.callee = ctx;

                structScope.addMethod(methodSymbol);

                // 为方法创建新的作用域
                stashScope(ctx);
                pushScope(methodSymbol);

                // 访问方法参数和方法体
                if (ctx.formalParameters() != null) {
                    visit(ctx.formalParameters());
                }

                if (ctx.block() != null) {
                    visit(ctx.block());
                }

                // 恢复作用域
                popScope();

                // 已经手动访问了子节点，所以返回null
                return null;
            }
        } else {
            logger.error("错误：当前作用域不是结构体作用域");
        }

        return super.visitStructMemeber(ctx);
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        stashScope(ctx);
        return super.visitExprBinary(ctx);
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        stashScope(ctx);
        return super.visitExprUnary(ctx);
    }

    @Override
    public Object visitType(TypeContext ctx) {
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {

        stashScope(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        stashScope(ctx);
        return null;
    }

    /**
     * bind ctx to current scope
     *
     * @param ctx
     */
    public void stashScope(ParserRuleContext ctx) {
        scopes.put(ctx, currentScope);
    }

    /**
     * change scope
     *
     * @param scope
     */
    public void pushScope(Scope scope) {
        currentScope = scope;
    }

    public void popScope() {
        currentScope = currentScope.getEnclosingScope();
    }

    @Override
    public Object visitStat(StatContext ctx) {
        stashScope(ctx);
        return super.visitStat(ctx);
    }

    @Override
    public Object visitStatReturn(StatReturnContext ctx) {
        stashScope(ctx);
        return super.visitStatReturn(ctx);
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        stashScope(ctx);
        return super.visitExprStructFieldAccess(ctx);
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        stashScope(ctx);
        return super.visitExprPrimary(ctx);
    }

    @Override
    public Object visitTypedefDecl(TypedefDeclContext ctx) {
        String typeName = ctx.ID().getText();
        String targetTypeName = ctx.type().getText();
        logger.debug("定义类型别名: {} -> {}", typeName, targetTypeName);

        // 尝试先从当前作用域解析目标类型
        Symbol targetTypeSymbol = currentScope.resolve(targetTypeName);
        Type targetType = null;

        if (targetTypeSymbol != null) {
            // 如果在作用域中找到了类型符号
            if (targetTypeSymbol instanceof Type) {
                targetType = (Type) targetTypeSymbol;
            } else if (targetTypeSymbol.type != null) {
                targetType = targetTypeSymbol.type;
            }
        } else {
            // 尝试从TypeTable中获取基本类型
            targetType = TypeTable.getTypeByName(targetTypeName);
        }

        // 即使此时无法解析目标类型，也创建TypedefSymbol
        // LocalResolver会在后续阶段完成类型解析
        TypedefSymbol typedef = new TypedefSymbol(typeName, targetType);
        currentScope.define(typedef);

        stashScope(ctx);
        return null;
    }

}
