package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.runtime.FunctionSpace;
import org.teachfx.antlr4.ep19.runtime.MemorySpace;
import org.teachfx.antlr4.ep19.runtime.StructInstance;
import org.teachfx.antlr4.ep19.symtab.ReturnValue;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.scope.ScopedSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep19.symtab.symbol.TypedefSymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Stack;

/**
 * 解释器 - 以visit模式实现
 */
public class Interpreter extends CymbolBaseVisitor<Object> {

    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    private static final ReturnValue sharedRetValue = new ReturnValue(null);
    private final ScopeUtil scopes;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace currentSpace;

    public Interpreter(ScopeUtil scopes) {
        this.scopes = scopes;
        this.memoryStack = new Stack<>();
        this.currentSpace = MemorySpace.globalSpace;
    }

    public void interpret(ParseTree context) {
        context.accept(this);
    }

    public void stashSpace(MemorySpace space) {
        this.memoryStack.add(this.currentSpace);
        this.currentSpace = space;
    }

    public void restoreSpace() {
        this.currentSpace = this.memoryStack.pop();
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        Scope scope = scopes.get(ctx);

        // 获取变量名和类型名
        String varName = ctx.ID().getText();
        String typeName = ctx.type().getText();
        Object varValue = null;

        logger.debug("定义变量: {}，类型: {}", varName, typeName);

        // 尝试通过类型名获取类型符号
        Symbol typeSymbol = scope.resolve(typeName);

        // 创建变量，为结构体和类型别名特殊处理
        if (typeSymbol != null) {
            if (typeSymbol instanceof StructSymbol structSymbol) {
                // 直接的结构体类型
                varValue = new StructInstance(varName, this.currentSpace, structSymbol);
                logger.debug("创建{}类型的结构体实例{}", structSymbol.getName(), varName);
            } else if (typeSymbol instanceof TypedefSymbol typedefSymbol) {
                // 通过typedef定义的类型
                if (typedefSymbol.getTargetType() instanceof StructSymbol structSymbol) {
                    varValue = new StructInstance(varName, this.currentSpace, structSymbol);
                    logger.debug("创建{}->{}类型的结构体实例{}", typedefSymbol.getName(), structSymbol.getName(), varName);
                }
            }
        }

        // 如果有初始值表达式，用表达式的值初始化
        if (ctx.expr() != null && !ctx.expr().isEmpty()) {
            // 获取初始化表达式（最后一个表达式，如果存在的话）
            ExprContext initExpr = null;
            if (ctx.expr().size() == 1) {
                // 只有初始化表达式：type ID = expr
                initExpr = ctx.expr(0);
            } else if (ctx.expr().size() == 2) {
                // 有数组大小和初始化表达式：type ID[expr] = expr
                initExpr = ctx.expr(1);
            }

            if (initExpr != null) {
                varValue = visit(initExpr);
            }
        }

        // 定义变量
        this.currentSpace.define(varName, varValue);
        return 0;
    }

    // < Expression evaluation
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        if (ctx.getChildCount() < 3) {
            logger.error("错误: 二元表达式不完整");
            return null;
        }

        Object left = visit(ctx.getChild(0));
        Object right = visit(ctx.getChild(2));

        if (left == null || right == null) {
            logger.error("错误: 二元表达式的操作数为null");
            return null;
        }

        Object ret = 0;
        Number lhs = 0, rhs = 0;
        if (left instanceof Number) {
            lhs = (Number) left;
        }
        if (right instanceof Number) {
            rhs = (Number) right;
        }

        String op = ctx.getChild(1).getText();
        switch (op) {
            case "+" -> {
                if (left instanceof String || right instanceof String) {
                    ret = left.toString() + right.toString();
                } else if (left instanceof Float || right instanceof Float) {
                    ret = lhs.floatValue() + rhs.floatValue();
                } else {
                    ret = lhs.intValue() + rhs.intValue();
                }
            }
            case "-" -> {
                if (left instanceof Float || right instanceof Float) {
                    ret = lhs.floatValue() - rhs.floatValue();
                } else {
                    ret = lhs.intValue() - rhs.intValue();
                }
            }
            case "*" -> {
                if (left instanceof Float || right instanceof Float) {
                    ret = lhs.floatValue() * rhs.floatValue();
                } else {
                    ret = lhs.intValue() * rhs.intValue();
                }
            }
            case "/" -> {
                if (left instanceof Float || right instanceof Float) {
                    ret = lhs.floatValue() / rhs.floatValue();
                } else {
                    ret = (Integer) left / (Integer) right;
                }
            }
            case "<" -> ret = (lhs.doubleValue() < rhs.doubleValue()) ? TypeTable.TRUE : TypeTable.FALSE;
            case ">" -> ret = lhs.doubleValue() > rhs.doubleValue() ? TypeTable.TRUE : TypeTable.FALSE;
            case "<=" -> ret = lhs.doubleValue() <= rhs.doubleValue() ? TypeTable.TRUE : TypeTable.FALSE;
            case ">=" -> ret = lhs.doubleValue() >= rhs.doubleValue() ? TypeTable.TRUE : TypeTable.FALSE;
            case "!=" -> ret = !left.equals(right) ? TypeTable.TRUE : TypeTable.FALSE;
            case "==" -> ret = left.equals(right) ? TypeTable.TRUE : TypeTable.FALSE;
            default -> {
                logger.error("错误: 未知的二元操作符: {}", op);
                ret = null;
            }
        }
        return ret;
    }

    // 处理参数
    private void processMethodParameters(ExprFuncCallContext ctx, int paramCount, String[] paramNames, FunctionSpace methodSpace) {
        for (int i = 0; i < paramCount; i++) {
            int paramIndex = i + 1; // 参数在ctx.expr()中的实际索引
            if (paramIndex >= ctx.expr().size()) {
                CompilerLogger.error(ctx, "方法调用参数索引越界: " + paramIndex);
                return; // 这里应该抛出异常或返回错误，取决于具体错误处理策略
            }
            Object paramValue = visit(ctx.expr(paramIndex));
            methodSpace.define(paramNames[i], paramValue);
        }
    }

    @Override
    public Object visitExprStructMethodCall(ExprStructMethodCallContext ctx) {
        // 获取结构体表达式和方法名
        ExprContext structExpr = ctx.expr(0);
        String methodName = ctx.ID().getText();

        // 获取结构体实例
        Object structObj = visit(structExpr);
        if (structObj == null) {
            CompilerLogger.error(ctx, "无法访问空结构体");
            return null;
        }

        if (!(structObj instanceof StructInstance)) {
            CompilerLogger.error(ctx, "表达式不是结构体实例");
            return null;
        }

        StructInstance instance = (StructInstance) structObj;
        MethodSymbol methodSymbol = instance.getMethod(methodName);

        if (methodSymbol == null) {
            CompilerLogger.error(ctx, "结构体 " + instance.getStructSymbol().getName() +
                               " 没有名为 " + methodName + " 的方法");
            return null;
        }

        // 创建方法调用空间
        FunctionSpace methodSpace = new FunctionSpace(methodSymbol.getName(), methodSymbol, instance);

        // 收集参数名和值
        int paramCount = methodSymbol.getMembers().size();
        String[] paramNames = methodSymbol.getMembers().keySet().toArray(new String[0]);

        // 确保参数数量匹配 - 从expr(1)开始，因为expr(0)是结构体表达式
        int argCount = ctx.expr().size() - 1;
        if (argCount != paramCount) {
            CompilerLogger.error(ctx, "方法 " + methodName + " 需要 " + paramCount +
                               " 个参数，但提供了 " + argCount + " 个");
            return null;
        }

        // 处理参数 - 从expr(1)开始
        for (int i = 0; i < paramCount; i++) {
            if (i + 1 >= ctx.expr().size()) {
                CompilerLogger.error(ctx, "参数索引越界: " + (i + 1));
                return null;
            }

            Object paramValue = visit(ctx.expr(i + 1));
            methodSpace.define(paramNames[i], paramValue);
        }

        // 保存当前空间并切换到方法空间
        stashSpace(methodSpace);

        // 执行方法体
        Object result = null;
        try {
            if (methodSymbol.blockStmt != null) {
                visit(methodSymbol.blockStmt);
            } else {
                CompilerLogger.error(ctx, "方法 " + methodName + " 没有方法体");
            }
        } catch (ReturnValue returnValue) {
            result = returnValue.value;
        } catch (Exception e) {
            CompilerLogger.error(ctx, "执行方法 " + methodName + " 时发生错误: " + e.getMessage());
        }

        // 恢复原空间
        restoreSpace();

        return result;
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        // 检查函数名是否存在
        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "函数调用缺少函数名");
            return null;
        }

        // 获取函数名 - 根据语法，函数名来自ID，不是expr(0)
        String funcName = ctx.ID().getText();

        // 特殊处理print函数（内置函数）
        if (funcName.equals("print")) {
            // 处理print函数：直接输出参数
            if (ctx.expr() != null && !ctx.expr().isEmpty()) {
                for (int i = 0; i < ctx.expr().size(); i++) {
                    ExprContext exprCtx = ctx.expr(i);
                    if (exprCtx != null) {
                        Object result = visit(exprCtx);
                        if (result != null) {
                            System.out.print(result);
                            logger.info("输出: {}", result);
                        } else {
                            System.out.print("null");
                            logger.info("输出: null");
                        }
                    }
                }
                // 在所有参数输出完毕后换行
                System.out.println();
            } else {
                // 如果没有参数，只输出换行
                System.out.println();
            }
            return null;
        }

        logger.debug("访问函数: {}", funcName);

        // 查找函数符号
        Scope scope = scopes.get(ctx);
        if (scope == null) {
            CompilerLogger.error(ctx, "找不到作用域，无法解析函数 " + funcName);
            return null;
        }

        Symbol symFunc = scope.resolve(funcName);
        if (symFunc == null || !(symFunc instanceof MethodSymbol)) {
            CompilerLogger.error(ctx, funcName + " 不是一个有效的函数");
            return null;
        }

        // 处理普通函数调用
        Object result = callFunction((MethodSymbol) symFunc, ctx);
        logger.info("函数调用结果: {}", result);
        return result;
    }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        // visit children
        return visit(ctx.getChild(1));
    }

    @Override
    public Object visitStructDecl(StructDeclContext ctx) {
        return 0;
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        Integer i = (Integer) visit(ctx.getChild(1));
        if (Objects.equals(ctx.getChild(0).getText(), "!")) {
            return (Objects.equals(i, TypeTable.TRUE) ? TypeTable.FALSE : TypeTable.TRUE);
        } else {
            return -i;
        }
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        // 根据语法 expr o='.' ID，获取结构体表达式和字段名
        if (ctx.expr() == null) {
            CompilerLogger.error(ctx, "结构体字段访问缺少结构体表达式");
            return null;
        }

        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "结构体字段访问缺少字段名");
            return null;
        }

        // 获取结构体对象和字段名
        Object struct = visit(ctx.expr());
        String fieldName = ctx.ID().getText();

        if (struct == null) {
            CompilerLogger.error(ctx, "无法访问空结构体");
            return null;
        }

        if (!(struct instanceof StructInstance)) {
            CompilerLogger.error(ctx, "表达式不是结构体实例，无法访问字段 " + fieldName);
            return null;
        }

        // 从结构体实例中获取字段值
        StructInstance instance = (StructInstance) struct;

        // 普通字段访问
        if (!instance.hasField(fieldName)) {
            CompilerLogger.error(ctx, "结构体实例没有名为 " + fieldName + " 的字段");
            return null;
        }

        return instance.getField(fieldName);
    }

    // > Expression evaluation
    @Override
    public Object visitStatAssign(StatAssignContext ctx) {
        if (ctx.expr() == null || ctx.expr().size() < 2) {
            logger.error("错误: 赋值语句不完整");
            return 0;
        }

        ExprContext lhs = ctx.expr(0);
        ExprContext rhs = ctx.expr(1);

        if (lhs instanceof ExprStructFieldAccessContext swaps) {
            if (swaps.expr() == null || swaps.ID() == null) {
                logger.error("错误: 结构体字段访问不完整");
                return 0;
            }

            // 获取结构体实例
            Object structObj = visit(swaps.expr());
            String fieldName = swaps.ID().getText();

            if (!(structObj instanceof StructInstance)) {
                logger.error("错误: 表达式不是结构体实例，无法赋值字段 {}", fieldName);
                return 0;
            }

            StructInstance instance = (StructInstance) structObj;
            Object assignValue = visit(rhs);
            logger.debug("赋值 {} 为 {}", lhs.getText(), assignValue);
            instance.update(fieldName, assignValue);
        } else {
            this.currentSpace.update(lhs.getText(), visit(rhs));
        }

        return 0;
    }

    @Override
    public Object visitStatReturn(StatReturnContext ctx) {
        if (ctx.getChildCount() > 1 && ctx.getChild(1) != null) {
            sharedRetValue.value = visit(ctx.getChild(1));
        } else {
            sharedRetValue.value = null;
        }
        throw sharedRetValue;
    }

    @Override
    public Object visitStatBlock(StatBlockContext ctx) {
        MemorySpace local = new MemorySpace("local", this.currentSpace);
        Object ret = 0;
        stashSpace(local);

        super.visitStatBlock(ctx);

        restoreSpace();
        return ret;
    }

    @Override
    public Object visitStateCondition(StateConditionContext ctx) {
        Object ret = 0;
        // System.out.println("exec in line " + ctx.start.getLine() + ":" + ctx.getText());
        if (ctx.cond == null) {
            logger.error("错误: if语句缺少条件表达式");
            return ret;
        }

        Object condValue = visit(ctx.cond);
        if (condValue == TypeTable.TRUE) {
            if (ctx.then != null) {
                visit(ctx.then);
            }
        } else {
            if (ctx.elseDo != null) {
                visit(ctx.elseDo);
            }
        }
        return ret;
    }


    @Override
    public Object visitStateWhile(StateWhileContext ctx) {
        if (ctx.cond == null) {
            logger.error("错误: while语句缺少条件表达式");
            return null;
        }

        while (visit(ctx.cond) == TypeTable.TRUE) {
            if (ctx.then != null) {
                visit(ctx.then);
            }
        }

        return null;
    }

    @Override
    public Object visitExprNew(ExprNewContext ctx) {
        if (ctx.expr().stream().findFirst().isPresent()) {
            ExprContext structRef = ctx.expr().stream().findFirst().get();
            org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol symbol = (org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol) visit(structRef);

            return new StructInstance(structRef.getText(), currentSpace, symbol);
        }

        return null;
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        String text = ctx.getText();
        if ("true".equals(text)) {
            return TypeTable.TRUE;
        } else if ("false".equals(text)) {
            return TypeTable.FALSE;
        }
        return TypeTable.FALSE; // 默认情况返回FALSE
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        return Float.valueOf(ctx.getText());
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        // 提取字符字面量中的实际字符
        String text = ctx.getText();
        if (text.length() >= 3 && text.startsWith("'") && text.endsWith("'")) {
            return text.charAt(1);
        }
        return text; // 安全起见，如果格式不对就返回原始文本
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        Scope scope = this.scopes.get(ctx);
        if (scope == null) {
            logger.error("错误: 找不到 {} 的作用域", ctx.getText());
            return null;
        }

        String tokenName = ctx.start.getText();
        Symbol symbol = scope.resolve(tokenName);

        if (symbol == null) {
            logger.error("错误: 找不到符号 {}", tokenName);
            return null;
        }

        if (ScopedSymbol.class.isAssignableFrom(symbol.getClass())) {
            // 作用域符号统统直接返回，它们都是一个自封闭的作用域和求值环境。
            // 针对它们的求值只能发生在其内部某个方法或者表达式的调用上。
            return symbol;
        }
        return this.currentSpace.get(symbol.getName());
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        logger.debug("func entry - " + ctx.ID().getText());
        if (ctx.ID().getText().equalsIgnoreCase("main")) {
            // exec blockDef
            try {
                visit(ctx.blockDef);
            } catch (ReturnValue ex) {
                return ex.value;
            }
            return 0;
        }

        return 0;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        return Integer.valueOf(ctx.getText());
    }

    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        // 去除字符串两侧的引号
        String text = ctx.getText();
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text; // 安全起见，如果格式不对就返回原始文本
    }

    /**
     * 调用结构体方法
     * @param method 方法符号
     * @param ctx 函数调用上下文
     * @return 方法返回值
     */
    private Object callStructMethod(MethodSymbol method, ExprFuncCallContext ctx) {
        // 安全检查
        if (method == null || ctx == null) {
            CompilerLogger.error(ctx, "方法或调用上下文为空");
            return null;
        }

        // 实现结构体方法调用
        FunctionSpace methodSpace = new FunctionSpace(method.getName(), method, this.currentSpace);

        // 收集参数值
        int paramCount = method.getMembers().size();
        String[] paramNames = method.getMembers().keySet().toArray(new String[0]);

        // 确保参数数量匹配
        if (ctx.expr().size() != paramCount) {
            CompilerLogger.error(ctx, "方法 " + method.getName() + " 需要 " + paramCount +
                               " 个参数，但提供了 " + ctx.expr().size() + " 个");
            return null;
        }

        // 处理参数
        for (int i = 0; i < paramCount; i++) {
            if (i >= ctx.expr().size()) {
                CompilerLogger.error(ctx, "参数索引越界: " + i);
                return null;
            }

            Object paramValue = visit(ctx.expr(i));
            methodSpace.define(paramNames[i], paramValue);
        }

        // 保存当前空间并切换到方法空间
        stashSpace(methodSpace);

        // 执行方法体
        Object result = null;
        try {
            if (method.blockStmt != null) {
                visit(method.blockStmt);
            } else {
                CompilerLogger.error(ctx, "方法 " + method.getName() + " 没有方法体");
            }
        } catch (ReturnValue returnValue) {
            result = returnValue.value;
        } catch (Exception e) {
            CompilerLogger.error(ctx, "执行方法 " + method.getName() + " 时发生错误: " + e.getMessage());
        }

        // 恢复原空间
        restoreSpace();

        return result;
    }

    /**
     * 调用普通函数
     * @param function 函数符号
     * @param ctx 函数调用上下文
     * @return 函数返回值
     */
    private Object callFunction(MethodSymbol function, ExprFuncCallContext ctx) {
        // 安全检查
        if (function == null || ctx == null) {
            CompilerLogger.error(ctx, "函数或调用上下文为空");
            return null;
        }

        // 处理内置函数
        if (function.builtin) {
            if ("print".equals(function.getName())) {
                for (int i = 1; i < ctx.expr().size(); i++) {
                    Object value = visit(ctx.expr(i));
                    logger.info("输出: {}", value);
                }
                return null;
            }
            // 可以添加其他内置函数的处理
        }

        // 创建函数调用空间
        FunctionSpace functionSpace = new FunctionSpace(function.getName(), function, this.currentSpace);

        // 收集参数名和值
        int paramCount = function.getMembers().size();
        String[] paramNames = function.getMembers().keySet().toArray(new String[0]);

        // 确保参数数量匹配
        if (ctx.expr().size() != paramCount) {
            CompilerLogger.error(ctx, "函数 " + function.getName() + " 需要 " + paramCount +
                               " 个参数，但提供了 " + ctx.expr().size() + " 个");
            return null;
        }

        // 处理参数
        for (int i = 0; i < paramCount; i++) {
            if (i >= ctx.expr().size()) {
                CompilerLogger.error(ctx, "参数索引越界: " + i);
                return null;
            }

            Object paramValue = visit(ctx.expr(i));
            functionSpace.define(paramNames[i], paramValue);
        }

        // 保存当前空间并切换到函数空间
        stashSpace(functionSpace);

        // 执行函数体
        Object result = null;
        try {
            if (function.blockStmt != null) {
                visit(function.blockStmt);
            } else {
                CompilerLogger.error(ctx, "函数 " + function.getName() + " 没有函数体");
            }
        } catch (ReturnValue returnValue) {
            result = returnValue.value;
        } catch (Exception e) {
            CompilerLogger.error(ctx, "执行函数 " + function.getName() + " 时发生错误: " + e.getMessage());
        }

        // 恢复原空间
        restoreSpace();

        return result;
    }
}
