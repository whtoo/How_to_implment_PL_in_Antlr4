package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
// import org.teachfx.antlr4.ep19.parser.CymbolParser.ExprParenContext; // Remove this
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.type.ArrayType; // Added import
import org.teachfx.antlr4.ep19.symtab.TypeChecker;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep19.symtab.symbol.TypedefSymbol;

import java.util.ArrayList;
import java.util.List;

/**
 * 类型检查访问器
 * 负责检查所有表达式、语句和操作的类型兼容性
 */
public class TypeCheckVisitor extends CymbolASTVisitor<Type> {

    private final ScopeUtil scopeUtil;
    private final ParseTreeProperty<Type> types;

    public TypeCheckVisitor(ScopeUtil scopeUtil, ParseTreeProperty<Type> types) {
        this.scopeUtil = scopeUtil;
        this.types = types;
    }

    @Override
    public Type visitVarDecl(VarDeclContext ctx) {
        // 获取变量声明的类型
        Type declType = types.get(ctx.type());

        // 如果类型为null，尝试从符号表中获取
        if (declType == null) {
            // 尝试从TypeTable获取基本类型
            String typeName = ctx.type().getText();
            declType = TypeTable.getTypeByName(typeName);

            if (declType == null) {
                // 尝试从符号表中获取变量符号
                Symbol varSymbol = scopeUtil.get(ctx).resolve(ctx.ID().getText());
                if (varSymbol != null && varSymbol.type != null) {
                    declType = varSymbol.type;
                }
            }

            // 如果仍然为null，记录错误
            if (declType == null) {
                CompilerLogger.error(ctx, "无法确定变量 " + ctx.ID().getText() + " 的类型");
                return TypeTable.VOID;
            }
        }

        // 检查初始值表达式的类型兼容性
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
                Type initType = visit(initExpr);
                if (initType != null) { // 初始化表达式类型可能为null表示出错
                    TypeChecker.checkAssignmentCompatibility(declType, initType, ctx);
                }
            }
        }

        return declType;
    }

    @Override
    public Type visitStatAssign(StatAssignContext ctx) {
        if (ctx.expr() == null || ctx.expr().size() < 2) {
            CompilerLogger.error(ctx, "赋值语句不完整");
            return TypeTable.VOID;
        }

        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));

        if (leftType != null && rightType != null) {
            TypeChecker.checkAssignmentCompatibility(leftType, rightType, ctx);
        }

        return leftType;
    }

    @Override
    public Type visitExprBinary(ExprBinaryContext ctx) {
        if (ctx.expr() == null || ctx.expr().size() < 2 || ctx.o == null) {
            CompilerLogger.error(ctx, "二元表达式不完整");
            return TypeTable.VOID;
        }

        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));
        String operator = ctx.o.getText();

        if (leftType == null || rightType == null) {
            return TypeTable.VOID; // 已经报告过错误
        }

        Type resultType = TypeChecker.checkBinaryOperationCompatibility(leftType, rightType, operator, ctx);
        if (resultType == null) {
            return TypeTable.VOID; // 错误情况，返回void类型
        }

        return resultType;
    }

    @Override
    public Type visitExprBinaryMulDivPercent(ExprBinaryMulDivPercentContext ctx) {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));
        String operator = ctx.o.getText();
        Type resultType = null;

        if (leftType == null || rightType == null) {
            return TypeTable.VOID; // Error already reported
        }

        if (operator.equals("*") || operator.equals("/")) {
            // Existing logic for * and / using TypeChecker can be called, or replicated
            // For now, let's assume TypeChecker.checkBinaryOperationCompatibility handles * / correctly
            resultType = TypeChecker.checkBinaryOperationCompatibility(leftType, rightType, operator, ctx);
        } else if (operator.equals("%")) {
            Type actualLeftType = TypeChecker.resolveToActualType(leftType); // Use existing helper
            Type actualRightType = TypeChecker.resolveToActualType(rightType);
            if (actualLeftType != TypeTable.INT || actualRightType != TypeTable.INT) {
                CompilerLogger.error(ctx, "模运算的操作数必须是整数类型，实际为: " + leftType + ", " + rightType);
                resultType = TypeTable.VOID;
            } else {
                resultType = TypeTable.INT;
            }
        } else {
            // Should not happen if grammar and visitor names match
            CompilerLogger.error(ctx, "未知的运算符: " + operator + " 在 ExprBinaryMulDivPercentContext 中");
            resultType = TypeTable.VOID;
        }

        if (resultType == null || resultType == TypeTable.VOID) {
            types.put(ctx, TypeTable.VOID);
            return TypeTable.VOID;
        }

        types.put(ctx, resultType);
        return resultType;
    }

    @Override
    public Type visitExprLogicalAnd(ExprLogicalAndContext ctx) {
        Type leftType = visit(ctx.expr(0));
        Type rightType = visit(ctx.expr(1));

        boolean error = false;
        if (leftType != TypeTable.BOOLEAN) {
            CompilerLogger.error(ctx.expr(0), "逻辑与操作符'&&'的左操作数必须是布尔类型");
            error = true;
        }
        if (rightType != TypeTable.BOOLEAN) {
            CompilerLogger.error(ctx.expr(1), "逻辑与操作符'&&'的右操作数必须是布尔类型");
            error = true;
        }

        if (error) {
            types.put(ctx, TypeTable.VOID); // Or an error type
            return TypeTable.VOID;
        }

        types.put(ctx, TypeTable.BOOLEAN);
        return TypeTable.BOOLEAN;
    }

    @Override
    public Type visitExprUnary(ExprUnaryContext ctx) {
        Type operandType = visit(ctx.expr());
        String operator = ctx.getChild(0).getText();

        if (operandType == null) {
            return TypeTable.VOID; // 已经报告过错误
        }

        Type resultType = TypeChecker.checkUnaryOperationCompatibility(operandType, operator, ctx);
        if (resultType == null) {
            return TypeTable.VOID; // 错误情况，返回void类型
        }

        return resultType;
    }

    @Override
    public Type visitExprFuncCall(ExprFuncCallContext ctx) {
        // 检查函数名是否存在
        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "函数调用缺少函数名");
            return TypeTable.VOID;
        }

        // 获取函数名 - 根据语法，函数名来自ID，不是expr(0)
        String funcName = ctx.ID().getText();
        CompilerLogger.debug(String.format("访问函数 -> : %s", ctx.getText()));

        // 特殊处理内置函数print
        if (funcName.equals("print")) {
            // 收集参数类型，但不进行严格检查，print可以接受任何类型
            List<ExprContext> exprContexts = ctx.expr();
            if (exprContexts != null && !exprContexts.isEmpty()) {
                for (ExprContext exprCtx : exprContexts) {
                    if (exprCtx != null) {
                        visit(exprCtx); // 只是为了类型检查，不使用返回值
                    }
                }
            }
            return TypeTable.VOID; // print返回void
        }

        // 对于普通函数，尝试从符号表中解析
        Scope scope = scopeUtil.get(ctx);
        if (scope == null) {
            CompilerLogger.error(ctx, "无法获取函数调用的作用域");
            return TypeTable.VOID;
        }

        Symbol symbol = scope.resolve(funcName);

        if (symbol == null) {
            CompilerLogger.error(ctx, "表达式不是一个函数: " + funcName);
            return TypeTable.VOID;
        }

        if (!(symbol instanceof MethodSymbol)) {
            CompilerLogger.error(ctx, "表达式不是一个函数: " + funcName);
            return TypeTable.VOID;
        }

        MethodSymbol methodSymbol = (MethodSymbol) symbol;

        // 收集参数类型 - 所有的expr都是参数
        List<Type> argTypes = new ArrayList<>();
        List<ExprContext> exprContexts = ctx.expr();
        if (exprContexts != null && !exprContexts.isEmpty()) {
            for (ExprContext exprCtx : exprContexts) {
                if (exprCtx != null) {
                    Type argType = visit(exprCtx);
                    if (argType != null) { // 可能为null表示参数表达式有错误
                        argTypes.add(argType);
                    } else {
                        // 如果参数类型为null，添加VOID类型作为占位符
                        argTypes.add(TypeTable.VOID);
                    }
                }
            }
        }

        // 获取函数形参类型
        List<Symbol> parameters = new ArrayList<>(methodSymbol.getMembers().values());
        Type[] paramTypes = parameters.stream()
                .map(param -> param.type != null ? param.type : TypeTable.VOID) // 防止NPE
                .toArray(Type[]::new);

        // 检查参数类型兼容性
        TypeChecker.checkFunctionCallCompatibility(
                paramTypes,
                argTypes.toArray(new Type[0]),
                ctx
        );

        // 返回函数返回值类型
        return methodSymbol.type != null ? methodSymbol.type : TypeTable.VOID; // 防止NPE
    }

    @Override
    public Type visitExprStructMethodCall(ExprStructMethodCallContext ctx) {
        // 获取结构体表达式和方法名
        List<ExprContext> exprContexts = ctx.expr();
        if (exprContexts == null || exprContexts.isEmpty()) {
            CompilerLogger.error(ctx, "无效的结构体方法调用");
            return TypeTable.VOID;
        }

        ExprContext structExpr = exprContexts.get(0); // 结构体表达式
        if (structExpr == null) {
            CompilerLogger.error(ctx, "无效的结构体表达式");
            return TypeTable.VOID;
        }

        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "无效的方法名");
            return TypeTable.VOID;
        }
        String methodName = ctx.ID().getText(); // 方法名

        // 获取结构体类型
        Type structType = visit(structExpr);
        if (structType == null) {
            CompilerLogger.error(ctx, "无法确定结构体表达式的类型");
            return TypeTable.VOID;
        }

        // 处理typedef可能指向的结构体
        if (structType instanceof TypedefSymbol) {
            Type targetType = ((TypedefSymbol) structType).getTargetType();
            if (targetType instanceof StructSymbol) {
                structType = targetType;
            } else {
                CompilerLogger.error(ctx, "类型 " + structType + " 不是结构体类型");
                return TypeTable.VOID;
            }
        }

        if (!(structType instanceof StructSymbol)) {
            CompilerLogger.error(ctx, "不是结构体类型");
            return TypeTable.VOID;
        }

        StructSymbol structSymbol = (StructSymbol) structType;

        // 查找方法
        Symbol methodSymbol = structSymbol.resolveMember(methodName);
        if (methodSymbol == null) {
            CompilerLogger.error(ctx, "没有名为 " + methodName + " 的方法");
            return TypeTable.VOID;
        }

        if (!(methodSymbol instanceof MethodSymbol)) {
            CompilerLogger.error(ctx, methodName + " 不是一个方法");
            return TypeTable.VOID;
        }

        MethodSymbol method = (MethodSymbol) methodSymbol;

        // 收集参数类型 - 从expr(1)开始，因为expr(0)是结构体表达式
        List<Type> argTypes = new ArrayList<>();
        for (int i = 1; i < exprContexts.size(); i++) {
            ExprContext exprCtx = exprContexts.get(i);
            if (exprCtx != null) {
                Type argType = visit(exprCtx);
                if (argType != null) {
                    argTypes.add(argType);
                }
            }
        }

        // 获取方法形参类型
        List<Symbol> parameters = new ArrayList<>(method.getMembers().values());
        Type[] paramTypes = parameters.stream()
                .map(param -> param.type)
                .toArray(Type[]::new);

        // 检查参数类型兼容性
        TypeChecker.checkFunctionCallCompatibility(
                paramTypes,
                argTypes.toArray(new Type[0]),
                ctx
        );

        // 返回方法返回值类型
        return method.type;
    }

    @Override
    public Type visitExprGroup(ExprGroupContext ctx) { // Changed from ExprParenContext
        Type type = visit(ctx.expr());
        if (type != null) {
            types.put(ctx, type);
        }
        return type;
    }

    @Override
    public Type visitExprArrayAccess(ExprArrayAccessContext ctx) {
        // 访问子节点获取类型信息
        super.visitExprArrayAccess(ctx);

        // 获取数组表达式和索引表达式
        if (ctx.getChildCount() < 4) { // expr '[' expr ']'
            CompilerLogger.error(ctx, "无效的数组访问表达式");
            return TypeTable.VOID;
        }

        ExprContext arrayExpr = (ExprContext) ctx.getChild(0);
        ExprContext indexExpr = (ExprContext) ctx.getChild(2);

        Type arrayType = visit(arrayExpr);
        Type indexType = visit(indexExpr);

        // 检查数组表达式类型
        if (arrayType == null) {
            CompilerLogger.error(ctx, "无法确定数组表达式的类型");
            return TypeTable.VOID;
        }

        // NEW CHECK: Verify if the base expression is actually an array type
        if (!(arrayType instanceof ArrayType)) {
            CompilerLogger.error(ctx, "不是数组类型");
            return TypeTable.VOID;
        }

        // 检查索引类型必须是整数
        if (indexType != null && indexType != TypeTable.INT) {
            CompilerLogger.error(ctx, "数组索引必须是整数类型，实际为: " + indexType);
            return TypeTable.VOID;
        }

        // 返回数组元素类型
        Type elementType = ((ArrayType) arrayType).getElementType();
        types.put(ctx, elementType);
        return elementType;
    }

    @Override
    public Type visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        // 不调用super.visit，避免字段名被当作独立标识符处理

        // 根据语法 expr o='.' ID，获取结构体表达式和字段名
        if (ctx.expr() == null) {
            CompilerLogger.error(ctx, "结构体字段访问缺少结构体表达式");
            return TypeTable.VOID;
        }

        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "结构体字段访问缺少字段名");
            return TypeTable.VOID;
        }

        // 1. 检查左侧是否为结构体类型
        Type structType = visit(ctx.expr());
        if (structType == null) {
            CompilerLogger.error(ctx, "无法确定结构体表达式的类型");
            return TypeTable.VOID;
        }

        // 处理typedef可能指向的结构体
        if (structType instanceof TypedefSymbol) {
            Type targetType = ((TypedefSymbol) structType).getTargetType();
            if (targetType instanceof StructSymbol) {
                structType = targetType;
            } else {
                CompilerLogger.error(ctx, "不是结构体类型");
                return TypeTable.VOID;
            }
        }

        if (!(structType instanceof StructSymbol)) {
            CompilerLogger.error(ctx, "不是结构体类型");
            return TypeTable.VOID;
        }

        // 2. 检查右侧字段是否存在于结构体中
        StructSymbol structSymbol = (StructSymbol) structType;
        String memberName = ctx.ID().getText();

        Symbol memberSymbol = structSymbol.resolveMember(memberName);

        if (memberSymbol == null) {
            CompilerLogger.error(ctx, "没有名为 " + memberName + " 的成员");
            return TypeTable.VOID;
        }

        // 3. 将成员的类型赋给整个表达式
        Type memberType = memberSymbol.type;
        types.put(ctx, memberType);

        // 4. 如果是方法成员，确保类型正确
        if (memberSymbol instanceof MethodSymbol) {
            // MethodSymbol已经实现了Type接口，所以可以直接使用
            types.put(ctx, (Type)memberSymbol);
        }

        // 5. 支持嵌套结构体：记录成员是否为结构体类型，以支持多级访问
        if (memberType instanceof StructSymbol || 
            (memberType instanceof TypedefSymbol && 
             ((TypedefSymbol) memberType).getTargetType() instanceof StructSymbol)) {
            CompilerLogger.debug("成员 " + memberName + " 是结构体类型，支持嵌套访问");
        }

        return memberType;
    }

    @Override
    public Type visitStateCondition(StateConditionContext ctx) {
        if (ctx.cond == null) {
            CompilerLogger.error(ctx, "if语句缺少条件表达式");
            return null;
        }

        Type condType = visit(ctx.cond);

        if (condType != null && condType != TypeTable.BOOLEAN) {
            CompilerLogger.error(ctx.cond, "if条件表达式必须是布尔类型，实际为: " + condType);
        }

        // 访问then和else分支
        if (ctx.then != null) {
            visit(ctx.then);
        }

        if (ctx.elseDo != null) {
            visit(ctx.elseDo);
        }

        return null;
    }

    @Override
    public Type visitStateWhile(StateWhileContext ctx) {
        if (ctx.cond == null) {
            CompilerLogger.error(ctx, "while语句缺少条件表达式");
            return null;
        }

        Type condType = visit(ctx.cond);

        if (condType != null && condType != TypeTable.BOOLEAN) {
            CompilerLogger.error(ctx.cond, "while条件表达式必须是布尔类型，实际为: " + condType);
        }

        // 访问循环体
        if (ctx.then != null) {
            visit(ctx.then);
        }

        return null;
    }

    @Override
    public Type visitStatReturn(StatReturnContext ctx) {
        // 获取当前函数
        MethodSymbol currentFunction = findEnclosingFunction(ctx);
        if (currentFunction == null) {
            CompilerLogger.error(ctx, "return语句必须在函数内部");
            return null;
        }

        Type returnType = currentFunction.type;

        // 检查return表达式与函数返回类型的兼容性
        if (ctx.expr() != null) {
            Type exprType = visit(ctx.expr());

            if (exprType != null) { // 表达式类型可能为null表示出错
                if (returnType == TypeTable.VOID) {
                    CompilerLogger.error(ctx, "void函数不应返回值");
                } else if (!TypeChecker.checkAssignmentCompatibility(returnType, exprType, ctx)) {
                    CompilerLogger.error(ctx, "返回值类型 " + exprType + " 与函数返回类型 " + returnType + " 不兼容");
                }
            }
        } else if (returnType != TypeTable.VOID) {
            CompilerLogger.error(ctx, "函数应返回 " + returnType + " 类型的值");
        }

        return null;
    }

    @Override
    public Type visitPrimaryID(PrimaryIDContext ctx) {
        Type type = types.get(ctx);
        if (type == null) {
            CompilerLogger.error(ctx, "无法确定标识符类型: " + ctx.getText());
            return TypeTable.VOID;
        }
        return type;
    }

    @Override
    public Type visitPrimaryINT(PrimaryINTContext ctx) {
        return TypeTable.INT;
    }

    @Override
    public Type visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        return TypeTable.FLOAT;
    }

    @Override
    public Type visitPrimarySTRING(PrimarySTRINGContext ctx) {
        return TypeTable.STRING;
    }

    @Override
    public Type visitPrimaryCHAR(PrimaryCHARContext ctx) {
        return TypeTable.CHAR;
    }

    @Override
    public Type visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        // 确保布尔字面量被正确识别
        String boolText = ctx.getText();
        if ("true".equals(boolText) || "false".equals(boolText)) {
            CompilerLogger.debug("识别布尔字面量: " + boolText);
            return TypeTable.BOOLEAN;
        }
        return TypeTable.BOOLEAN;
    }

    private MethodSymbol findEnclosingFunction(StatReturnContext ctx) {
        // 从当前作用域向上查找，直到找到函数作用域
        Scope scope = scopeUtil.get(ctx);
        if (scope == null) {
            CompilerLogger.error(ctx, "无法获取return语句的作用域");
            return null;
        }

        while (scope != null) {
            if (scope instanceof MethodSymbol) {
                return (MethodSymbol) scope;
            }
            scope = scope.getEnclosingScope();
        }

        CompilerLogger.error(ctx, "return语句不在函数内部");
        return null;
    }
}
