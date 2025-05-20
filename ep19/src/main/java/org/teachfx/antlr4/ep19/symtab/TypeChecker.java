package org.teachfx.antlr4.ep19.symtab;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

/**
 * 类型检查器，负责类型兼容性检查
 */
public class TypeChecker {

    /**
     * 检查赋值操作的类型兼容性
     * @param lhsType 左侧类型
     * @param rhsType 右侧类型
     * @param ctx 上下文
     * @return 如果类型兼容返回true，否则返回false
     */
    public static boolean checkAssignmentCompatibility(Type lhsType, Type rhsType, ParserRuleContext ctx) {
        String lhsTypeName = lhsType != null ? lhsType.toString() : "null";
        String rhsTypeName = rhsType != null ? rhsType.toString() : "null";
        CompilerLogger.warning(ctx, String.format("lhs %s *= rhs %s", lhsTypeName, rhsTypeName));
        
        if (lhsType == null || rhsType == null) {
            CompilerLogger.error(ctx, "类型未定义");
            return false;
        }

        if (areTypesCompatible(lhsType, rhsType)) {
            return true;
        }

        CompilerLogger.error(ctx, String.format("类型不兼容: 不能将 %s 类型赋值给 %s 类型", rhsTypeName, lhsTypeName));
        return false;
    }
    
    private static boolean areTypesCompatible(Type lhsType, Type rhsType) {
        // 如果类型相同，直接兼容
        if (lhsType == rhsType) {
            return true;
        }

        // null可以赋值给任何非基本类型
        if (rhsType == TypeTable.NULL && !lhsType.isPrimitive()) {
            return true;
        }

        // 数值类型的隐式转换（int -> float是安全的）
        if (lhsType == TypeTable.FLOAT && rhsType == TypeTable.INT) {
            return true;
        }

        // 结构体类型兼容性检查
        if (!lhsType.isPrimitive() && !rhsType.isPrimitive()) {
            // 实现继承关系检查
            return isSubtype(rhsType, lhsType);
        }
        
        return false;
    }
    
    private static boolean isSubtype(Type type, Type superType) {
        // 这里需要完整的类型继承关系检查实现
        // 当前只是一个示例实现
        return type == superType;
    }

    /**
     * 检查二元操作的类型兼容性
     * @param leftType 左操作数类型
     * @param rightType 右操作数类型
     * @param operator 操作符
     * @param ctx 上下文
     * @return 操作结果类型，如果不兼容返回null
     */
    public static Type checkBinaryOperationCompatibility(Type leftType, Type rightType, String operator, ParserRuleContext ctx) {
        if (leftType == null || rightType == null) {
            CompilerLogger.error(ctx, "操作数类型未定义");
            return null;
        }

        // 算术运算符: +, -, *, /
        if (operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")) {
            // 数值类型之间的运算
            if ((leftType == TypeTable.INT || leftType == TypeTable.FLOAT) &&
                (rightType == TypeTable.INT || rightType == TypeTable.FLOAT)) {

                // 如果任一操作数是float，结果为float
                if (leftType == TypeTable.FLOAT || rightType == TypeTable.FLOAT) {
                    return TypeTable.FLOAT;
                } else {
                    return TypeTable.INT;
                }
            }

            // 字符串连接
            if (operator.equals("+") && (leftType == TypeTable.STRING || rightType == TypeTable.STRING)) {
                return TypeTable.STRING;
            }

            CompilerLogger.error(ctx, "类型 " + leftType + " 和 " + rightType + " 不支持 '" + operator + "' 操作");
            return null;
        }

        // 比较运算符: ==, !=, <, >, <=, >=
        if (operator.equals("==") || operator.equals("!=")) {
            // 基本类型之间可以比较相等性
            if (leftType.isPrimitive() && rightType.isPrimitive()) {
                return TypeTable.BOOLEAN;
            }

            // 非基本类型只能与同类型或null比较
            if (!leftType.isPrimitive() && (rightType == leftType || rightType == TypeTable.NULL)) {
                return TypeTable.BOOLEAN;
            }

            if (!rightType.isPrimitive() && (leftType == rightType || leftType == TypeTable.NULL)) {
                return TypeTable.BOOLEAN;
            }

            CompilerLogger.error(ctx, "类型 " + leftType + " 和 " + rightType + " 不能比较相等性");
            return null;
        }

        if (operator.equals("<") || operator.equals(">") || operator.equals("<=") || operator.equals(">=")) {
            // 只有数值类型可以进行大小比较
            if ((leftType == TypeTable.INT || leftType == TypeTable.FLOAT) &&
                (rightType == TypeTable.INT || rightType == TypeTable.FLOAT)) {
                return TypeTable.BOOLEAN;
            }

            CompilerLogger.error(ctx, "类型 " + leftType + " 和 " + rightType + " 不支持比较操作 '" + operator + "'");
            return null;
        }

        CompilerLogger.error(ctx, "不支持的操作符: " + operator);
        return null;
    }

    /**
     * 检查一元操作的类型兼容性
     * @param operandType 操作数类型
     * @param operator 操作符
     * @param ctx 上下文
     * @return 操作结果类型，如果不兼容返回null
     */
    public static Type checkUnaryOperationCompatibility(Type operandType, String operator, ParserRuleContext ctx) {
        if (operandType == null) {
            CompilerLogger.error(ctx, "操作数类型未定义");
            return null;
        }

        // 负号运算符: -
        if (operator.equals("-")) {
            if (operandType == TypeTable.INT || operandType == TypeTable.FLOAT) {
                return operandType; // 返回相同类型
            }

            CompilerLogger.error(ctx, "类型 " + operandType + " 不支持负号操作");
            return null;
        }

        // 逻辑非运算符: !
        if (operator.equals("!")) {
            if (operandType == TypeTable.BOOLEAN) {
                return TypeTable.BOOLEAN;
            }

            CompilerLogger.error(ctx, "类型 " + operandType + " 不支持逻辑非操作");
            return null;
        }

        CompilerLogger.error(ctx, "不支持的一元操作符: " + operator);
        return null;
    }

    /**
     * 检查函数调用的参数类型兼容性
     * @param paramTypes 形参类型列表
     * @param argTypes 实参类型列表
     * @param ctx 上下文
     * @return 如果所有参数兼容返回true，否则返回false
     */
    public static boolean checkFunctionCallCompatibility(Type[] paramTypes, Type[] argTypes, ParserRuleContext ctx) {
        if (paramTypes.length != argTypes.length) {
            CompilerLogger.error(ctx, "参数数量不匹配: 期望 " + paramTypes.length + " 个参数，实际 " + argTypes.length + " 个参数");
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (!checkAssignmentCompatibility(paramTypes[i], argTypes[i], ctx)) {
                CompilerLogger.error(ctx, "第 " + (i+1) + " 个参数类型不匹配: 期望 " + paramTypes[i] + "，实际 " + argTypes[i]);
                return false;
            }
        }

        return true;
    }

    /**
     * 检查结构体字段访问的类型
     * @param structType 结构体类型
     * @param fieldName 字段名
     * @param ctx 上下文
     * @return 字段类型，如果字段不存在返回null
     */
    public static Type checkStructFieldAccess(Type structType, String fieldName, ParserRuleContext ctx) {
        if (structType == null) {
            CompilerLogger.error(ctx, "结构体类型未定义");
            return null;
        }

        if (!(structType instanceof StructSymbol)) {
            CompilerLogger.error(ctx, "类型 " + structType + " 不是结构体类型");
            return null;
        }

        StructSymbol structSymbol = (StructSymbol) structType;
        if (structSymbol.resolveMember(fieldName) == null) {
            CompilerLogger.error(ctx, "结构体 " + structSymbol.getName() + " 没有名为 " + fieldName + " 的字段");
            return null;
        }

        return structSymbol.resolveMember(fieldName).type;
    }

    /**
     * 检查结构体方法调用的类型
     * @param structType 结构体类型
     * @param methodName 方法名
     * @param argTypes 实参类型列表
     * @param ctx 上下文
     * @return 方法返回类型，如果方法不存在或参数不匹配返回null
     */
    public static Type checkStructMethodCall(Type structType, String methodName, Type[] argTypes, ParserRuleContext ctx) {
        if (structType == null) {
            CompilerLogger.error(ctx, "结构体类型未定义");
            return null;
        }

        if (!(structType instanceof StructSymbol)) {
            CompilerLogger.error(ctx, "类型 " + structType + " 不是结构体类型");
            return null;
        }

        StructSymbol structSymbol = (StructSymbol) structType;
        Symbol methodSymbol = structSymbol.resolveMember(methodName);

        if (methodSymbol == null) {
            CompilerLogger.error(ctx, "结构体 " + structSymbol.getName() + " 没有名为 " + methodName + " 的方法");
            return null;
        }

        if (!(methodSymbol instanceof org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol)) {
            CompilerLogger.error(ctx, structSymbol.getName() + "." + methodName + " 不是一个方法");
            return null;
        }

        org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol method =
            (org.teachfx.antlr4.ep19.symtab.symbol.MethodSymbol) methodSymbol;

        // 获取方法形参类型
        org.teachfx.antlr4.ep19.symtab.symbol.Symbol[] parameters =
            method.getMembers().values().toArray(new org.teachfx.antlr4.ep19.symtab.symbol.Symbol[0]);
        Type[] paramTypes = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramTypes[i] = parameters[i].type;
        }

        // 检查参数类型兼容性
        if (!checkFunctionCallCompatibility(paramTypes, argTypes, ctx)) {
            return null;
        }

        return method.type; // 返回方法返回值类型
    }
}