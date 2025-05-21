package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.Type;
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
        if (ctx.expr() != null) {
            Type initType = visit(ctx.expr());
            if (initType != null) { // 初始化表达式类型可能为null表示出错
                TypeChecker.checkAssignmentCompatibility(declType, initType, ctx);
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
        List<ExprContext> argExprsNodes;
        List<Type> actualArgTypes = new ArrayList<>();
        MethodSymbol methodToCall;
        String callNameForErrorMsg;

        // Path 1: Direct function call like funcName(arg1, arg2)
        if (ctx.ID() != null) {
            callNameForErrorMsg = ctx.ID().getText();
            CompilerLogger.debug(String.format("访问函数调用(ID): %s", callNameForErrorMsg));

            if (callNameForErrorMsg.equals("print")) {
                // For "print", visit arguments for any side effects or nested checks, but no strict type checking here.
                // Assuming ctx.expr() holds the arguments directly when ctx.ID() is present.
                argExprsNodes = ctx.expr() != null ? ctx.expr() : new ArrayList<>();
                for (ExprContext argExpr : argExprsNodes) {
                    visit(argExpr);
                }
                return TypeTable.VOID; // print returns void
            }

            Scope scope = scopeUtil.get(ctx);
            Symbol symbol = scope.resolve(callNameForErrorMsg);

            if (!(symbol instanceof MethodSymbol)) {
                CompilerLogger.error(ctx, "未定义的函数: " + callNameForErrorMsg);
                return TypeTable.VOID;
            }
            methodToCall = (MethodSymbol) symbol;
            // Assuming ctx.expr() holds arguments if ctx.ID() is present.
            // If grammar uses exprList: argExprsNodes = ctx.exprList() != null ? ctx.exprList().expr() : new ArrayList<>();
            argExprsNodes = ctx.expr() != null ? ctx.expr() : new ArrayList<>();

        } else { // Path 2: Call on an expression, e.g., struct.method(args) or funcPtr(args)
            if (ctx.expr().isEmpty()) {
                CompilerLogger.error(ctx, "函数调用缺少可调用表达式和参数");
                return TypeTable.VOID;
            }

            ExprContext callableExpr = ctx.expr(0);
            callNameForErrorMsg = callableExpr.getText(); // Initial name for errors, may be refined for structs
            CompilerLogger.debug(String.format("访问函数调用(expr): %s", callNameForErrorMsg));

            // Arguments are expr(1), expr(2), ...
            argExprsNodes = new ArrayList<>();
            for (int i = 1; i < ctx.expr().size(); i++) {
                argExprsNodes.add(ctx.expr(i));
            }

            if (callableExpr instanceof ExprStructFieldAccessContext) {
                ExprStructFieldAccessContext fieldAccessCtx = (ExprStructFieldAccessContext) callableExpr;
                Type structInstanceType = visit(fieldAccessCtx.expr(0)); // Type of the struct instance

                if (structInstanceType == null) {
                    CompilerLogger.error(fieldAccessCtx.expr(0), "无法确定结构体实例 " + fieldAccessCtx.expr(0).getText() + " 的类型");
                    return TypeTable.VOID;
                }

                // TypedefSymbol Handling
                if (structInstanceType instanceof TypedefSymbol) {
                    Type targetType = ((TypedefSymbol) structInstanceType).getTargetType();
                    if (targetType instanceof StructSymbol) {
                        structInstanceType = targetType;
                    } else {
                        CompilerLogger.error(fieldAccessCtx.expr(0), "类型别名 " + structInstanceType.getName() + " 解析后 (" + targetType.getName() + ") 不是结构体类型");
                        return TypeTable.VOID;
                    }
                }

                if (!(structInstanceType instanceof StructSymbol)) {
                    CompilerLogger.error(fieldAccessCtx.expr(0), "表达式 " + fieldAccessCtx.expr(0).getText() + " (类型: " + structInstanceType.getName() + ") 不是一个结构体实例");
                    return TypeTable.VOID;
                }

                StructSymbol structSymbol = (StructSymbol) structInstanceType;
                String methodName;

                // Method Name Extraction from struct.member
                if (fieldAccessCtx.ID() != null) { // Prefer expr.ID form
                    methodName = fieldAccessCtx.ID().getText();
                } else if (fieldAccessCtx.expr(1) != null && (fieldAccessCtx.expr(1) instanceof PrimaryIDContext || fieldAccessCtx.expr(1).getChildCount() == 1 && fieldAccessCtx.expr(1).getChild(0) instanceof PrimaryIDContext)) {
                    methodName = fieldAccessCtx.expr(1).getText();
                } else if (fieldAccessCtx.expr(1) != null) {
                    methodName = fieldAccessCtx.expr(1).getText();
                    CompilerLogger.warning(fieldAccessCtx.expr(1), "方法名 '" + methodName + "' 从复杂表达式中提取，可能不准确: " + fieldAccessCtx.expr(1).getText());
                }
                else {
                    CompilerLogger.error(fieldAccessCtx, "无法从结构体字段访问中提取方法名: " + fieldAccessCtx.getText());
                    return TypeTable.VOID;
                }
                callNameForErrorMsg = structSymbol.getName() + "." + methodName; // Refined name for error messages

                Symbol memberSymbol = structSymbol.resolveMember(methodName);
                if (memberSymbol == null) {
                    CompilerLogger.error(fieldAccessCtx, "结构体 " + structSymbol.getName() + " 没有名为 '" + methodName + "' 的成员");
                    return TypeTable.VOID;
                }
                if (!(memberSymbol instanceof MethodSymbol)) {
                    CompilerLogger.error(fieldAccessCtx, "'" + methodName + "' 在结构体 " + structSymbol.getName() + " 中不是一个方法");
                    return TypeTable.VOID;
                }
                methodToCall = (MethodSymbol) memberSymbol;

            } else {
                // Fallback for other callable expressions (e.g., function pointers - basic support)
                CompilerLogger.warning(callableExpr, "尝试通过文本名称解析可调用表达式: " + callableExpr.getText() + ". 这对于函数指针或复杂可调用对象可能无效.");
                Scope scope = scopeUtil.get(ctx);
                Symbol symbol = scope.resolve(callableExpr.getText());
                if (!(symbol instanceof MethodSymbol)) {
                    CompilerLogger.error(ctx, "表达式 " + callableExpr.getText() + " 未解析为可调用函数");
                    return TypeTable.VOID;
                }
                methodToCall = (MethodSymbol) symbol;
            }
        }

        // Common logic: Collect actual argument types
        for (ExprContext argExpr : argExprsNodes) {
            Type argType = visit(argExpr);
            if (argType == null) { // Error visiting arg, should have been logged by visit()
                return TypeTable.VOID; // Stop further checking for this call
            }
            actualArgTypes.add(argType);
        }

        // Get formal parameter types from the resolved method
        List<Symbol> formalParams = new ArrayList<>(methodToCall.getMembers().values());
        Type[] formalParamTypes = formalParams.stream()
                .map(param -> param.type)
                .toArray(Type[]::new);

        // Check compatibility
        // Assuming TypeChecker.checkFunctionCallCompatibility logs errors internally using the context
        TypeChecker.checkFunctionCallCompatibility(
                formalParamTypes,
                actualArgTypes.toArray(new Type[0]),
                ctx // Pass full context for error reporting line numbers
        );
        // If checkFunctionCallCompatibility returns a boolean status, it should be checked here.
        // For now, assuming it logs errors and doesn't throw/return status that needs immediate handling.

        // Return the method's return type
        return methodToCall.type;
    }

    @Override
    public Type visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        // The first child expression (e.g., expr(0)) should be the struct instance.
        // The ID child should be the member name.
        // Example ANTLR grammar: exprStructFieldAccess : expr '.' ID ;
        
        if (ctx.expr() == null || ctx.expr().isEmpty()) {
            CompilerLogger.error(ctx, "无效的结构体字段访问：缺少结构体实例表达式。");
            return TypeTable.VOID;
        }
        
        ExprContext structInstanceExprCtx = ctx.expr(0); // Assuming expr(0) is the struct instance
        Type structType = visit(structInstanceExprCtx); // Ensure the struct instance's type is resolved first

        if (structType == null || structType == TypeTable.VOID) {
            CompilerLogger.error(structInstanceExprCtx, "无法确定结构体实例表达式 '" + structInstanceExprCtx.getText() + "' 的类型。");
            types.put(ctx, TypeTable.VOID);
            return TypeTable.VOID;
        }

        // Handle TypedefSymbol: resolve it to the actual StructSymbol if applicable.
        if (structType instanceof TypedefSymbol) {
            TypedefSymbol typedef = (TypedefSymbol) structType;
            Type targetType = typedef.getTargetType(); // Assumes getTargetType() fully resolves.
            if (targetType instanceof StructSymbol) {
                structType = targetType;
            } else {
                CompilerLogger.error(ctx, "类型别名 '" + typedef.getName() + "' 解析后得到的类型 '" + (targetType != null ? targetType.getName() : "null") + "' 不是一个结构体类型。");
                types.put(ctx, TypeTable.VOID);
                return TypeTable.VOID;
            }
        }

        if (!(structType instanceof StructSymbol)) {
            CompilerLogger.error(structInstanceExprCtx, "表达式 '" + structInstanceExprCtx.getText() + "' (实际类型: " + structType.getName() + ") 不是结构体类型，无法访问其成员。");
            types.put(ctx, TypeTable.VOID);
            return TypeTable.VOID;
        }

        StructSymbol structSymbol = (StructSymbol) structType;
        String memberName;

        // Member Name Extraction: Prefer ID if available (grammar: expr '.' ID)
        // Fallback to expr(1) if grammar is expr '.' expr (where expr(1) is an identifier)
        if (ctx.ID() != null) {
            memberName = ctx.ID().getText();
        } else if (ctx.expr().size() > 1 && ctx.expr(1) instanceof PrimaryIDContext) {
            // This case might be less common if ID() is used for the member name.
            // It implies a grammar like `expr(0) DOT expr(1)` where expr(1) is the identifier.
            memberName = ctx.expr(1).getText();
        } else if (ctx.expr().size() > 1) {
             memberName = ctx.expr(1).getText();
             CompilerLogger.warning(ctx.expr(1), "成员名称 '" + memberName + "' 从复杂表达式 '" + ctx.expr(1).getText() + "' 中提取，用于类型检查。这可能表示非标准用法或潜在的语法歧义。");
        }
        else {
            CompilerLogger.error(ctx, "无效的结构体字段访问：无法提取成员名称。");
            types.put(ctx, TypeTable.VOID);
            return TypeTable.VOID;
        }

        Symbol memberSymbol = structSymbol.resolveMember(memberName);

        if (memberSymbol == null) {
            CompilerLogger.error(ctx, "结构体 '" + structSymbol.getName() + "' 中没有名为 '" + memberName + "' 的成员。");
            types.put(ctx, TypeTable.VOID);
            return TypeTable.VOID;
        }

        // Type Determination and Assignment for the ExprStructFieldAccessContext node
        if (memberSymbol instanceof MethodSymbol) {
            // For a method member, its "type" in an expression context is the method itself.
            types.put(ctx, (MethodSymbol) memberSymbol);
            return (MethodSymbol) memberSymbol;
        } else {
            // For a data field member, its type is the declared type of the field.
            if (memberSymbol.type == null) {
                 CompilerLogger.error(ctx, "成员字段 '" + memberName + "' 在结构体 '" + structSymbol.getName() + "' 中没有定义的类型。");
                 types.put(ctx, TypeTable.VOID);
                 return TypeTable.VOID;
            }
            types.put(ctx, memberSymbol.type);
            return memberSymbol.type;
        }
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
        return TypeTable.BOOLEAN;
    }

    private MethodSymbol findEnclosingFunction(StatReturnContext ctx) {
        // 从当前作用域向上查找，直到找到函数作用域
        Scope scope = scopeUtil.get(ctx);
        while (scope != null) {
            if (scope instanceof MethodSymbol) {
                return (MethodSymbol) scope;
            }
            scope = scope.getEnclosingScope();
        }
        return null;
    }
}