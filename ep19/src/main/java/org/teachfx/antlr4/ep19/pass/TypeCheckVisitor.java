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
        // 检查函数名是否存在
        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "函数调用缺少函数名");
            return TypeTable.VOID;
        }

        // 获取函数名
        String funcName = ctx.ID().getText();
        CompilerLogger.debug(String.format("访问函数 -> : %s", ctx.getText()));

        // 特殊处理内置函数print - 这是最优先的处理
        if ("print".equals(funcName)) {
            // 收集参数类型，但不进行严格检查，print可以接受任何类型
            for (int i = 0; i < ctx.expr().size(); i++) {
                visit(ctx.expr(i)); // 只是为了类型检查，不使用返回值
            }
            return TypeTable.VOID; // print返回void
        }

        // 对于非内置函数，尝试从符号表中解析
        Type funcType;
        // 如果是简单标识符，尝试从符号表中解析
        Scope scope = scopeUtil.get(ctx);
        Symbol symbol = scope.resolve(funcName);

        if (symbol instanceof MethodSymbol) {
            funcType = (MethodSymbol) symbol;
        } else {
            CompilerLogger.error(ctx, "未定义的函数: " + funcName);
            return TypeTable.VOID;
        }

        CompilerLogger.debug(String.format("访问函数 --> : %s", ctx.getText()));
        CompilerLogger.debug(String.format("访问函数: ---> %s,%s", funcName, funcType.getName()));

        // 如果不是方法类型，报错
        if (!(funcType instanceof MethodSymbol)) {
            CompilerLogger.error(ctx, "表达式不是一个函数: " + funcName);
            return TypeTable.VOID;
        }

        MethodSymbol methodSymbol = (MethodSymbol) funcType;

        // 收集参数类型
        List<Type> argTypes = new ArrayList<>();
        for (int i = 0; i < ctx.expr().size(); i++) {
            Type argType = visit(ctx.expr(i));
            if (argType != null) { // 可能为null表示参数表达式有错误
                argTypes.add(argType);
            }
        }

        // 获取函数形参类型
        List<Symbol> parameters = new ArrayList<>(methodSymbol.getMembers().values());
        Type[] paramTypes = parameters.stream()
                .map(param -> param.type)
                .toArray(Type[]::new);

        // 检查参数类型兼容性
        TypeChecker.checkFunctionCallCompatibility(
                paramTypes,
                argTypes.toArray(new Type[0]),
                ctx
        );

        // 返回函数返回值类型
        return methodSymbol.type;
    }

    @Override
    public Type visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        // 确保我们先访问子节点，获取类型信息
        super.visitExprStructFieldAccess(ctx);

        // 1. 检查左侧是否为结构体类型
        ExprContext exprCtx = null;
        if (ctx.getChildCount() > 0 && ctx.getChild(0) instanceof ExprContext) {
            exprCtx = (ExprContext) ctx.getChild(0);
        } else {
            CompilerLogger.error(ctx, "无效的结构体字段访问表达式");
            return TypeTable.VOID;
        }

        Type structType = types.get(exprCtx);
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
            CompilerLogger.error(ctx, exprCtx.getText() + " 不是一个结构体实例");
            return TypeTable.VOID;
        }

        // 2. 检查右侧字段是否存在于结构体中
        StructSymbol structSymbol = (StructSymbol) structType;
        String fieldName = "";
        if (ctx.getChildCount() >= 3) { // struct.field 格式的结构
            fieldName = ctx.getChild(2).getText();
        } else {
            CompilerLogger.error(ctx, "无效的结构体字段访问表达式");
            return TypeTable.VOID;
        }

        Symbol fieldSymbol = structSymbol.resolveMember(fieldName);

        if (fieldSymbol == null) {
            CompilerLogger.error(ctx, "结构体 " + structSymbol.getName() + " 没有名为 " + fieldName + " 的字段");
            return TypeTable.VOID;
        }

        // 3. 将字段的类型赋给整个表达式
        Type fieldType = fieldSymbol.type;
        types.put(ctx, fieldType);
        return fieldType;
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