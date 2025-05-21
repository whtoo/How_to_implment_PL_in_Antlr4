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
            if (typeSymbol instanceof StructSymbol) {
                // 直接的结构体类型
                StructSymbol structSymbol = (StructSymbol) typeSymbol;
                varValue = new StructInstance(varName, this.currentSpace, structSymbol);
                logger.debug("创建{}类型的结构体实例{}", structSymbol.getName(), varName);
            } else if (typeSymbol instanceof TypedefSymbol) {
                // 通过typedef定义的类型
                TypedefSymbol typedefSymbol = (TypedefSymbol) typeSymbol;
                if (typedefSymbol.getTargetType() instanceof StructSymbol) {
                    StructSymbol structSymbol = (StructSymbol) typedefSymbol.getTargetType();
                    varValue = new StructInstance(varName, this.currentSpace, structSymbol);
                    logger.debug("创建{}->{}类型的结构体实例{}", typedefSymbol.getName(), structSymbol.getName(), varName);
                }
            }
        }

        // 如果有初始值表达式，用表达式的值初始化
        if (ctx.expr() != null) {
            varValue = visit(ctx.expr());
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

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        // 检查是否有函数表达式和参数
        if (ctx.expr() == null || ctx.expr().isEmpty()) {
            CompilerLogger.error(ctx, "函数调用缺少函数表达式");
            return null;
        }

        // 获取第一个表达式（函数名或结构体方法访问）
        ExprContext firstExpr = ctx.expr(0);
        if (firstExpr == null) {
            CompilerLogger.error(ctx, "函数调用缺少函数表达式");
            return null;
        }

        // 处理字符串字面量，避免将它们视为函数调用
        String firstExprText = firstExpr.getText();
        if (firstExprText.startsWith("\"") && firstExprText.endsWith("\"")) {
            // 字符串字面量不能作为函数调用，直接返回字符串值
            Object value = visit(firstExpr);
            logger.debug("字符串字面量: {}", value);
            return value;
        }

        // 特殊处理print函数（内置函数）
        if (firstExpr.getText().equals("print")) {
            // 处理print函数：直接输出参数
            for (int i = 1; i < ctx.expr().size(); i++) {
                ExprContext exprCtx = ctx.expr(i);
                if (exprCtx != null) {
                    Object result = visit(exprCtx);
                    if (result != null) {
                        System.out.println(result);
                        logger.info("输出: {}", result);
                    } else {
                        logger.info("输出: null");
                    }
                }
            }
            return null;
        }

        // 处理结构体方法调用
        if (firstExpr instanceof ExprStructFieldAccessContext) {
            ExprStructFieldAccessContext fieldAccessCtx = (ExprStructFieldAccessContext) firstExpr;

            Object structObj = visit(fieldAccessCtx.expr(0));
            if (!(structObj instanceof StructInstance)) {
                CompilerLogger.error(fieldAccessCtx.expr(0), "表达式 " + fieldAccessCtx.expr(0).getText() + " 不是一个结构体实例。");
                return null;
            }
            StructInstance instance = (StructInstance) structObj;

            String methodName;
            // Robust method name extraction
            if (fieldAccessCtx.ID() != null) { // Grammar: expr '.' ID
                methodName = fieldAccessCtx.ID().getText();
            } else if (fieldAccessCtx.expr().size() > 1 && fieldAccessCtx.expr(1) instanceof PrimaryIDContext) { // Grammar: expr '.' expr (where expr(1) is PrimaryID)
                methodName = fieldAccessCtx.expr(1).getText();
            } else if (fieldAccessCtx.expr().size() > 1) { // Fallback, might be risky if expr(1) is complex
                methodName = fieldAccessCtx.expr(1).getText();
                // Consider adding a warning if expr(1) is not a simple ID node.
                if (!(fieldAccessCtx.expr(1) instanceof PrimaryIDContext)) {
                    CompilerLogger.warning(fieldAccessCtx.expr(1), "方法名 '" + methodName + "' 从复杂表达式 '" + fieldAccessCtx.expr(1).getText() + "' 中提取。");
                }
            }
            else {
                CompilerLogger.error(fieldAccessCtx, "无法从结构体字段访问中提取方法名: " + fieldAccessCtx.getText());
                return null;
            }

            MethodSymbol methodSymbol = instance.getMethod(methodName);

            if (methodSymbol == null) {
                CompilerLogger.error(fieldAccessCtx, "结构体 '" + instance.getStructSymbol().getName() +
                                                   "' 没有名为 '" + methodName + "' 的方法。");
                return null;
            }

            // Create FunctionSpace for the method call, crucially linking it to the StructInstance
            FunctionSpace methodSpace = new FunctionSpace(methodSymbol.getName(), methodSymbol, instance);

            // Argument Processing
            // Arguments for the method call start from ctx.expr(1)
            int expectedParamCount = methodSymbol.getMembers().size();
            // Actual arguments are ctx.expr(1), ctx.expr(2), ..., so total expr count is expectedParamCount + 1
            int actualArgCount = ctx.expr().size() - 1;

            if (actualArgCount != expectedParamCount) {
                CompilerLogger.error(ctx, "方法 '" + methodName + "' 需要 " + expectedParamCount +
                                       " 个参数，但提供了 " + actualArgCount + " 个。");
                return null;
            }

            String[] paramNames = methodSymbol.getMembers().keySet().toArray(new String[0]);
            for (int i = 0; i < expectedParamCount; i++) {
                // Arguments in ExprFuncCallContext are ctx.expr(1), ctx.expr(2), ...
                Object paramValue = visit(ctx.expr(i + 1));
                methodSpace.define(paramNames[i], paramValue);
            }

            // Execute method
            stashSpace(methodSpace);
            Object resultValue = null;
            try {
                if (methodSymbol.blockStmt != null) {
                    visit(methodSymbol.blockStmt);
                } else {
                    CompilerLogger.error(ctx, "方法 '" + methodName + "' 没有定义方法体。");
                    // No explicit return, so resultValue remains null (for void methods)
                }
            } catch (ReturnValue returnValueException) {
                resultValue = returnValueException.value;
            } catch (Exception e) {
                CompilerLogger.error(ctx, "执行方法 '" + methodName + "' 时发生运行时错误: " + e.getMessage(), e);
                // resultValue remains null or could be a special error marker if needed
            } finally {
                restoreSpace();
            }
            return resultValue;
        }

        // Handling for global functions or other callable expressions
        Object exprValue = visit(firstExpr); // Evaluate the callable expression itself

        if (exprValue instanceof MethodSymbol) {
            // This means firstExpr (e.g., a PrimaryID) resolved directly to a MethodSymbol (a global function)
            MethodSymbol globalFunction = (MethodSymbol) exprValue;
            // Arguments for callFunction are expected to be from ctx.expr(1) onwards
            return callFunction(globalFunction, ctx);
        } else if (exprValue instanceof String) {
            // This means firstExpr evaluated to a string, which we interpret as a function name
            String funcNameFromString = (String) exprValue;
            logger.debug("尝试通过名称 '{}' 调用函数。", funcNameFromString);
            Scope scope = scopes.get(ctx);
            if (scope == null) {
                CompilerLogger.error(ctx, "找不到作用域，无法解析函数 '" + funcNameFromString + "'.");
                return null;
            }
            Symbol symFunc = scope.resolve(funcNameFromString);
            if (!(symFunc instanceof MethodSymbol)) {
                CompilerLogger.error(ctx, "'" + funcNameFromString + "' 不是一个有效的函数或未定义。");
                return null;
            }
            return callFunction((MethodSymbol) symFunc, ctx);
        } else {
            // If exprValue is not a MethodSymbol or a String, it's not callable in the expected way.
            // It could be a simple value if the syntax was misused, e.g. "myVar(1,2)" where myVar is an int.
            CompilerLogger.error(ctx, "表达式 '" + firstExpr.getText() + "' (类型: " + (exprValue != null ? exprValue.getClass().getSimpleName() : "null") + ") 不是可调用的函数或方法。");
            return exprValue; // Or return null to signify error
        }
    }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        // visit children
        return visit(ctx.expr()); // Assuming expr is the content of the group
    }

    @Override
    public Object visitStructDecl(StructDeclContext ctx) {
        // Struct declarations are handled by symbol table phase, interpreter doesn't need to do much.
        return null;
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        Object value = visit(ctx.expr()); // Get the operand's value

        if (ctx.op.getText().equals("!")) {
            if (value instanceof Boolean) {
                return !(Boolean) value;
            } else if (value instanceof Integer) { // Handle C-style int as bool
                 return ((Integer)value == 0) ? TypeTable.TRUE : TypeTable.FALSE; // Assuming TypeTable.TRUE/FALSE are 1/0 or boolean
            }
            CompilerLogger.error(ctx, "逻辑非 (!) 操作符仅适用于布尔或整数类型, 实际为: " + (value != null ? value.getClass().getSimpleName() : "null"));
            return null;
        } else if (ctx.op.getText().equals("-")) {
            if (value instanceof Integer) {
                return -(Integer) value;
            } else if (value instanceof Float) {
                return -(Float) value;
            }
            CompilerLogger.error(ctx, "负号 (-) 操作符仅适用于数字类型, 实际为: " + (value != null ? value.getClass().getSimpleName() : "null"));
            return null;
        }
        CompilerLogger.error(ctx, "未知的一元操作符: " + ctx.op.getText());
        return null;
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        if (ctx.expr().size() < 1) { // Should have at least the struct instance expression. Name is ID or expr(1)
            CompilerLogger.error(ctx, "结构体字段访问语法不正确:缺少结构体实例表达式。");
            return null;
        }

        Object structInstanceObj = visit(ctx.expr(0)); // Evaluate the expression for the struct instance

        if (!(structInstanceObj instanceof StructInstance)) {
            CompilerLogger.error(ctx.expr(0), "表达式 '" + ctx.expr(0).getText() + "' (类型: " + (structInstanceObj != null ? structInstanceObj.getClass().getSimpleName() : "null") + ") 不是一个结构体实例。");
            return null;
        }
        StructInstance instance = (StructInstance) structInstanceObj;

        String fieldName;
        if (ctx.ID() != null) { // Grammar: expr '.' ID
            fieldName = ctx.ID().getText();
        } else if (ctx.expr().size() > 1 && ctx.expr(1) instanceof PrimaryIDContext) { // Grammar: expr '.' expr where expr(1) is ID
            fieldName = ctx.expr(1).getText();
        } else if (ctx.expr().size() > 1) { // Fallback for expr '.' expr where expr(1) is more complex but its text is the name
             fieldName = ctx.expr(1).getText();
             if(!(ctx.expr(1) instanceof PrimaryIDContext)) {
                CompilerLogger.warning(ctx.expr(1), "字段名 '" + fieldName + "' 从复杂表达式 '" + ctx.expr(1).getText() + "' 中提取。");
             }
        } else {
            CompilerLogger.error(ctx, "无法从结构体字段访问中提取字段名: " + ctx.getText());
            return null;
        }

        // If this access is part of a function call (e.g. instance.method()),
        // the visitExprFuncCall will handle method resolution.
        // Here, we resolve fields or methods if accessed as values (e.g. f = instance.method; or x = instance.field)
        Symbol member = instance.getStructSymbol().resolveMember(fieldName);

        if (member == null) {
            CompilerLogger.error(ctx, "结构体 '" + instance.getStructSymbol().getName() + "' 没有名为 '" + fieldName + "' 的成员。");
            return null;
        }

        // Handle based on member type (Method or Field)
        if (member instanceof MethodSymbol) {
            // If a method is accessed as a value (e.g., func = myStruct.method), return the MethodSymbol.
            // visitExprFuncCall handles the case where it's immediately called (myStruct.method()).
            CompilerLogger.debug(ctx, "成员 '" + fieldName + "' 是一个方法。返回其 MethodSymbol。");
            return member;
        } else if (member instanceof org.teachfx.antlr4.ep19.symtab.symbol.VariableSymbol) {
            // If it's a field (assuming fields are VariableSymbols), retrieve its value from the StructInstance.
            CompilerLogger.debug(ctx, "成员 '" + fieldName + "' 是一个字段。返回值。");
            // The instance.getField(fieldName) should handle if the field exists at runtime,
            // though resolveMember should guarantee its definition.
            return instance.getField(fieldName);
        } else {
            // Member was resolved, but it's not a MethodSymbol or a VariableSymbol (field).
            // This indicates an unexpected symbol type for a struct member that can be accessed.
            CompilerLogger.error(ctx, "成员 '" + fieldName + "' (类型: " + member.getClass().getSimpleName() + 
                                   ") 在结构体 '" + instance.getStructSymbol().getName() + "' 中不是可识别的方法或字段类型。");
            return null;
        }
    }

    // > Expression evaluation
    @Override
    public Object visitStatAssign(StatAssignContext ctx) {
        if (ctx.expr() == null || ctx.expr().size() < 2) {
            CompilerLogger.error(ctx, "赋值语句不完整：需要左右两个表达式。");
            return null;
        }

        ExprContext lhsCtx = ctx.expr(0);
        Object rhsValue = visit(ctx.expr(1)); // Evaluate the right-hand side first

        if (lhsCtx instanceof ExprStructFieldAccessContext fieldAccessCtx) {
            // Assignment to a struct field, e.g., myStruct.field = value;
            Object structObj = visit(fieldAccessCtx.expr(0)); // Evaluate the struct instance expression

            if (!(structObj instanceof StructInstance)) {
                CompilerLogger.error(fieldAccessCtx.expr(0), "赋值目标：表达式 '" + fieldAccessCtx.expr(0).getText() + "' 不是一个结构体实例。");
                return null;
            }
            StructInstance instance = (StructInstance) structObj;

            String fieldName;
             if (fieldAccessCtx.ID() != null) {
                fieldName = fieldAccessCtx.ID().getText();
            } else if (fieldAccessCtx.expr().size() > 1 && fieldAccessCtx.expr(1) instanceof PrimaryIDContext) {
                fieldName = fieldAccessCtx.expr(1).getText();
            } else if (fieldAccessCtx.expr().size() > 1) {
                fieldName = fieldAccessCtx.expr(1).getText();
                 if(!(fieldAccessCtx.expr(1) instanceof PrimaryIDContext)) {
                    CompilerLogger.warning(fieldAccessCtx.expr(1), "赋值目标字段名 '" + fieldName + "' 从复杂表达式 '" + fieldAccessCtx.expr(1).getText() + "' 中提取。");
                 }
            } else {
                CompilerLogger.error(fieldAccessCtx, "赋值目标：无法从结构体字段访问中提取字段名: " + fieldAccessCtx.getText());
                return null;
            }
            
            // Check if fieldName is actually a field and not a method for assignment
            Symbol member = instance.getStructSymbol().resolveMember(fieldName);
            if (member == null || member instanceof MethodSymbol) {
                 CompilerLogger.error(fieldAccessCtx, "赋值目标：成员 '" + fieldName + "' 在结构体 '" + instance.getStructSymbol().getName() + "' 中不是一个可赋值的字段。");
                 return null;
            }

            logger.debug("结构体字段赋值: {}.{} = {}", instance.getName(), fieldName, rhsValue);
            instance.update(fieldName, rhsValue);

        } else if (lhsCtx instanceof PrimaryIDContext idCtx) {
            // Assignment to a variable, e.g., x = value;
            String varName = idCtx.getText();
            // Ensure variable is defined in current or enclosing scopes (update handles this)
            logger.debug("变量赋值: {} = {}", varName, rhsValue);
            currentSpace.update(varName, rhsValue);
        } else {
            CompilerLogger.error(lhsCtx, "赋值语句的左侧必须是变量或结构体字段，实际为：" + lhsCtx.getText());
            return null;
        }
        return null; // Assignment statement doesn't produce a value itself
    }

    @Override
    public Object visitStatReturn(StatReturnContext ctx) {
        Object returnValue = null;
        if (ctx.expr() != null) { // return expr;
            returnValue = visit(ctx.expr());
        }
        // For 'return;' or if expr evaluates to null, returnValue is null.
        sharedRetValue.value = returnValue;
        logger.debug("执行返回语句，返回值为: {}", returnValue);
        throw sharedRetValue; // Throw exception to unwind stack to function/method call site
    }

    @Override
    public Object visitStatBlock(StatBlockContext ctx) {
        // Create a new local scope for the block, unless it's a function's top-level block
        // FunctionSpace itself acts as the memory space for its top-level block's variables.
        // If currentSpace is already a FunctionSpace and this block is its direct body,
        // we might not need a nested MemorySpace unless it's for if/while blocks within.
        // However, for simplicity and consistency, creating a nested scope for any block is often done.
        // Let's assume a new MemorySpace for general blocks. Function execution handles its own space.
        
        boolean isFunctionOrMethodTopBlock = currentSpace instanceof FunctionSpace &&
                                             (ctx.getParent() instanceof FunctionDeclContext ||
                                              (ctx.getParent() instanceof MethodDeclContext && currentSpace.getEnclosingScope() instanceof StructInstance) ||
                                              (currentSpace.getSymbol() instanceof MethodSymbol && ((MethodSymbol)currentSpace.getSymbol()).blockStmt == ctx)
                                             );


        if (!isFunctionOrMethodTopBlock) {
            MemorySpace blockSpace = new MemorySpace(currentSpace.getName() + ".block", currentSpace); // Or a more unique name
            stashSpace(blockSpace);
            logger.debug("进入新的块作用域: {}", blockSpace.getName());
        } else {
            logger.debug("在现有函数/方法作用域 {} 中执行块。", currentSpace.getName());
        }

        try {
            super.visitStatBlock(ctx); // Visit statements within the block
        } finally {
            // Only restore space if we stashed a new one for this block
            if (!isFunctionOrMethodTopBlock) {
                logger.debug("退出块作用域，恢复到: {}", (memoryStack.isEmpty() ? "global" : memoryStack.peek().getName()));
                restoreSpace();
            }
        }
        return null; // Blocks themselves don't have a "value" in this interpreter
    }

    @Override
    public Object visitStateCondition(StateConditionContext ctx) {
        if (ctx.cond == null) {
            CompilerLogger.error(ctx, "if语句缺少条件表达式。");
            return null;
        }

        Object condValue = visit(ctx.cond);

        // Interpret integer 0 as false, non-zero as true, common in C-like languages
        boolean conditionResult;
        if (condValue instanceof Boolean) {
            conditionResult = (Boolean) condValue;
        } else if (condValue instanceof Integer) {
            conditionResult = ((Integer) condValue != 0);
        } else {
            CompilerLogger.error(ctx.cond, "if条件表达式必须是布尔或整数类型，实际为: " + (condValue != null ? condValue.getClass().getSimpleName() : "null"));
            return null;
        }
        
        logger.debug("If条件 {} 求值为 {}", ctx.cond.getText(), conditionResult);

        if (conditionResult) {
            if (ctx.then != null) {
                logger.debug("执行 If then 分支: {}", ctx.then.getText());
                visit(ctx.then);
            }
        } else {
            if (ctx.elseDo != null) {
                logger.debug("执行 If else 分支: {}", ctx.elseDo.getText());
                visit(ctx.elseDo);
            }
        }
        return null; // Statements don't have values
    }


    @Override
    public Object visitStateWhile(StateWhileContext ctx) {
        if (ctx.cond == null) {
            CompilerLogger.error(ctx, "while语句缺少条件表达式。");
            return null;
        }
        
        logger.debug("进入 While 循环, 条件: {}", ctx.cond.getText());
        
        Object condValue = visit(ctx.cond);
        boolean conditionResult;

        if (condValue instanceof Boolean) {
            conditionResult = (Boolean) condValue;
        } else if (condValue instanceof Integer) {
            conditionResult = ((Integer) condValue != 0);
        } else {
            CompilerLogger.error(ctx.cond, "while条件表达式必须是布尔或整数类型，实际为: " + (condValue != null ? condValue.getClass().getSimpleName() : "null"));
            return null;
        }

        while (conditionResult) {
            if (ctx.then != null) {
                logger.debug("执行 While 循环体: {}", ctx.then.getText());
                visit(ctx.then); // Execute the loop body
            }

            // Re-evaluate the condition for the next iteration
            condValue = visit(ctx.cond);
            if (condValue instanceof Boolean) {
                conditionResult = (Boolean) condValue;
            } else if (condValue instanceof Integer) {
                conditionResult = ((Integer) condValue != 0);
            } else {
                 CompilerLogger.error(ctx.cond, "while条件表达式在循环中必须是布尔或整数类型，实际为: " + (condValue != null ? condValue.getClass().getSimpleName() : "null"));
                 break; // Exit loop on error
            }
            logger.debug("While 条件 {} 求值为 {}", ctx.cond.getText(), conditionResult);
        }
        logger.debug("退出 While 循环。");
        return null; // Statements don't have values
    }

    @Override
    public Object visitExprNew(ExprNewContext ctx) {
        // Assuming 'new' is used for struct instantiation, e.g., new MyStruct
        // The expr() inside ExprNewContext should resolve to a type, specifically a StructSymbol.
        if (ctx.type() == null) {
            CompilerLogger.error(ctx, "'new' 表达式需要一个类型。");
            return null;
        }
        String typeName = ctx.type().getText();
        Scope currentScope = scopes.get(ctx); // Get scope at the 'new' expression site
        Symbol typeSymbol = currentScope.resolve(typeName);

        if (typeSymbol == null) {
            CompilerLogger.error(ctx.type(), "未知的类型 '" + typeName + "' 在 'new' 表达式中。");
            return null;
        }
        
        StructSymbol actualStructSymbol = null;
        if (typeSymbol instanceof StructSymbol) {
            actualStructSymbol = (StructSymbol) typeSymbol;
        } else if (typeSymbol instanceof TypedefSymbol) {
            TypedefSymbol typedef = (TypedefSymbol) typeSymbol;
            if (typedef.getTargetType() instanceof StructSymbol) {
                actualStructSymbol = (StructSymbol) typedef.getTargetType();
            }
        }

        if (actualStructSymbol == null) {
            CompilerLogger.error(ctx.type(), "类型 '" + typeName + "' 不是一个结构体类型，无法使用 'new' 创建实例。");
            return null;
        }
        
        // The name for the StructInstance could be anonymous or derived if needed for debugging.
        // Enclosing space for a new instance is the currentSpace where 'new' is called.
        String instanceName = actualStructSymbol.getName() + "@" + System.identityHashCode(ctx); // Example instance name
        logger.debug("使用 'new' 创建结构体 '{}' 的实例: {}", actualStructSymbol.getName(), instanceName);
        return new StructInstance(instanceName, currentSpace, actualStructSymbol);
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        return Boolean.parseBoolean(ctx.getText());
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        try {
            return Float.valueOf(ctx.getText());
        } catch (NumberFormatException e) {
            CompilerLogger.error(ctx, "无效的浮点数字面量: " + ctx.getText());
            return 0.0f; // Default or error value
        }
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        String text = ctx.getText();
        if (text.length() == 3 && text.startsWith("'") && text.endsWith("'")) {
            return text.charAt(1);
        }
        // Handle escape sequences if supported by grammar, e.g. '\n'
        if (text.length() == 4 && text.startsWith("'\\") && text.endsWith("'")){
             char c = text.charAt(2);
             switch(c) {
                 case 'n': return '\n';
                 case 't': return '\t';
                 case 'r': return '\r';
                 case '\'': return '\'';
                 case '\\': return '\\';
                 // Add more escapes if needed
                 default:
                    CompilerLogger.error(ctx, "未知的转义字符: " + text);
                    return text.charAt(2); // Or some error char
             }
        }
        CompilerLogger.error(ctx, "无效的字符字面量: " + text + ". 应为单引号包围的单个字符或有效转义序列。");
        return text.length() > 0 ? text.charAt(0) : '\0'; // Default or error char
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        String varName = ctx.getText();
        Scope currentProcessingScope = scopes.get(ctx); // The scope where this ID is defined/used.

        if (currentProcessingScope == null) {
            CompilerLogger.error(ctx, "找不到标识符 '" + varName + "' 的作用域信息。");
            return null;
        }

        Symbol symbol = currentProcessingScope.resolve(varName);

        if (symbol == null) {
            // Try to resolve in the current runtime memory space's hierarchy if not found by static scope
            // This might be controversial; typically, resolution should rely on static scopes found by ScopeUtil.
            // However, some dynamic languages might allow this. For Cymbol, static resolution is key.
            Object valueFromMemory = currentSpace.get(varName);
            if (valueFromMemory != null) {
                 CompilerLogger.warning(ctx, "标识符 '" + varName + "' 通过动态内存查找找到，但静态作用域未找到。");
                 return valueFromMemory;
            }
            CompilerLogger.error(ctx, "未定义的标识符: '" + varName + "'.");
            return null;
        }
        
        logger.debug("访问标识符: {}, 解析为符号: {}", varName, symbol.getClass().getSimpleName());

        // If the symbol represents a type or a scope itself (like a function name or struct name), return the symbol.
        // This is useful when an ID is used where a type or function is expected (e.g., new MyStruct, or f = myFunc).
        if (symbol instanceof MethodSymbol || symbol instanceof StructSymbol || symbol instanceof TypedefSymbol) {
            return symbol;
        }

        // For variables, retrieve their value from the current memory space.
        // The currentSpace stack correctly handles lexical scoping for variable values.
        Object value = currentSpace.get(varName);
        if (value == MemorySpace.UNDEFINED) { // Or however undefined is marked
            CompilerLogger.error(ctx, "变量 '" + varName + "' 已声明但可能未初始化或在当前路径上未定义值。");
            return null; // Or a special UndefinedMarker object
        }
        return value;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        String funcName = ctx.ID().getText();
        logger.debug("处理函数声明: {}", funcName);

        // During interpretation, function declarations primarily ensure the MethodSymbol is linked to its block.
        // The actual execution happens when the function is called.
        // For a 'main' function, if this interpreter is to auto-run it:
        if (funcName.equalsIgnoreCase("main")) {
            logger.info("找到 main 函数，准备执行...");
            MethodSymbol mainMethod = (MethodSymbol) scopes.get(ctx).resolve("main"); // Assuming main is in global scope from ScopeUtil
            if (mainMethod != null && mainMethod.blockStmt == ctx.blockDef()) { // blockDef should be the one from this ctx
                // Set up a FunctionSpace for main
                FunctionSpace mainSpace = new FunctionSpace("main", mainMethod, MemorySpace.globalSpace); // main has no outer instance, uses global
                stashSpace(mainSpace);
                Object mainResult = null;
                try {
                    visit(mainMethod.blockStmt); // Execute main's body
                } catch (ReturnValue rv) {
                    mainResult = rv.value;
                    logger.info("main 函数执行完毕，返回: {}", mainResult);
                } catch (Exception e) {
                    CompilerLogger.error(ctx, "main 函数执行时发生错误: " + e.getMessage(), e);
                } finally {
                    restoreSpace();
                }
                return mainResult; // Or some exit code
            } else {
                 CompilerLogger.error(ctx, "无法正确解析或找到 main 函数的定义体。");
            }
        }
        return null; // Function declaration itself doesn't return a value during this pass
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        try {
            return Integer.valueOf(ctx.getText());
        } catch (NumberFormatException e) {
            CompilerLogger.error(ctx, "无效的整数字面量: " + ctx.getText());
            return 0; // Default or error value
        }
    }

    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        String text = ctx.getText();
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            // TODO: Handle escape sequences within the string, e.g., \n, \t, \\, \"
            // For now, just unquote. A more robust solution would iterate and build the string.
            return text.substring(1, text.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\"); // Basic escapes
        }
        CompilerLogger.error(ctx, "无效的字符串字面量格式: " + text);
        return text; // Return as is if malformed, or empty string
    }

    /**
     * Invokes a global (non-struct) function.
     * @param function The MethodSymbol for the function.
     * @param ctx The call site context.
     * @return The return value of the function.
     */
    private Object callFunction(MethodSymbol function, ExprFuncCallContext ctx) {
        if (function == null) {
            CompilerLogger.error(ctx, "尝试调用空的函数符号。");
            return null;
        }
        logger.debug("调用全局函数: {}", function.getName());

        // Built-in functions can be handled here if not done earlier (like 'print')
        if (function.builtin) {
            // This is a fallback; 'print' is handled directly in visitExprFuncCall.
            // Add other built-in global functions here if necessary.
            CompilerLogger.warning(ctx, "内置函数 '" + function.getName() + "' 的调用逻辑应在此处专门处理(如果尚未处理)。");
            return null;
        }

        // Create FunctionSpace for the global function call. Its enclosing space is the current calling space.
        // Or, if all global functions are defined in globalSpace, it could be MemorySpace.globalSpace.
        // Using currentSpace allows for lexical scoping if global functions can be nested (not typical in Cymbol).
        FunctionSpace functionSpace = new FunctionSpace(function.getName(), function, currentSpace);

        // Argument processing: Arguments for a global function call initiated via
        // an ID (e.g., myFunc(a,b)) or an expression that resolves to MethodSymbol
        // are ctx.expr(1), ctx.expr(2)... because ctx.expr(0) was the function identifier/expression.
        int expectedParamCount = function.getMembers().size();
        int actualArgCount = ctx.expr().size() - 1; // Since ctx.expr(0) is the function itself

        if (actualArgCount != expectedParamCount) {
            CompilerLogger.error(ctx, "函数 '" + function.getName() + "' 需要 " + expectedParamCount +
                                   " 个参数，但提供了 " + actualArgCount + " 个。");
            return null;
        }

        String[] paramNames = function.getMembers().keySet().toArray(new String[0]);
        for (int i = 0; i < expectedParamCount; i++) {
            Object paramValue = visit(ctx.expr(i + 1)); // Arguments are expr(1), expr(2)...
            functionSpace.define(paramNames[i], paramValue);
            logger.debug("  参数 {}: {} = {}", i, paramNames[i], paramValue);
        }

        stashSpace(functionSpace);
        Object resultValue = null;
        try {
            if (function.blockStmt != null) {
                visit(function.blockStmt);
            } else {
                CompilerLogger.error(ctx, "函数 '" + function.getName() + "' 没有定义函数体。");
            }
        } catch (ReturnValue returnValueException) {
            resultValue = returnValueException.value;
        } catch (Exception e) {
            CompilerLogger.error(ctx, "执行函数 '" + function.getName() + "' 时发生运行时错误: " + e.getMessage(), e);
        } finally {
            restoreSpace();
        }
        logger.debug("函数 '{}' 调用完成，返回: {}", function.getName(), resultValue);
        return resultValue;
    }
}
