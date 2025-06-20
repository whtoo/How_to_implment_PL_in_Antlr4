package org.teachfx.antlr4.ep19.pass;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.misc.Util;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.TypeTable;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.symbol.StructSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;
import org.teachfx.antlr4.ep19.symtab.symbol.VariableSymbol;
import org.teachfx.antlr4.ep19.symtab.symbol.TypedefSymbol;
import org.teachfx.antlr4.ep19.symtab.type.ArrayType; // Added import

/**
 * @description 给变量分配类型
 * @purpose 给变量确定具体类型
 */
public class LocalResolver extends CymbolASTVisitor<Object> {
    private static final Logger logger = LoggerFactory.getLogger(LocalResolver.class);
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    // ID[index-expr]
    private static final int ARRAY_EXPR = 0;
    private static final int FUNC_EXPR = 0;
    // struct ID block-declaration
    private static final int STRUCT = 0;
    private static final int MEMBER_PARENT = 2;
    // ID.(ID|FUNC-CALL)
    private static final int MEMBER = 0;
    private final ScopeUtil scopes;
    public ParseTreeProperty<Type> types;

    public LocalResolver(ScopeUtil scopes) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<Type>();
    }


    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        // 先访问子节点，确保类型节点已经被处理
        super.visitVarDecl(ctx);

        // 获取变量基本类型
        Type baseType = scopes.lookup(ctx.type());
        String varName = Util.name(ctx);
        Type finalType;

        // Check if it's an array declaration by inspecting the parse tree structure
        // Grammar: type ID ('[' expr ']')? ('=' expr)? ';'
        // Children: TypeContext, ID_TerminalNode, (optional LBRACK_TerminalNode, ExprContext, RBRACK_TerminalNode), ...
        boolean isArrayDeclaration = false;
        if (ctx.getChildCount() >= 5 && // Must have at least type, ID, [, expr, ] sequence
            ctx.getChild(2) instanceof TerminalNode &&
            ctx.getChild(2).getText().equals("[") &&
            ctx.getChild(3) instanceof ExprContext && // expr for size
            ctx.getChild(4) instanceof TerminalNode &&
            ctx.getChild(4).getText().equals("]")) {
            isArrayDeclaration = true;
        }

        if (isArrayDeclaration) {
            if (baseType != null) {
                finalType = new ArrayType(baseType);
                logger.debug("变量 {} 是数组类型，元素类型为 {}, 最终类型为 {}", varName, baseType.getName(), finalType.getName());
            } else {
                CompilerLogger.error(ctx, "Unknown base type for array variable: " + varName);
                finalType = TypeTable.VOID; // Error case, use VOID to prevent NPE, error already logged
            }
        } else {
            finalType = baseType;
            String finalTypeName = (finalType != null) ? finalType.getName() : "null";
            logger.debug("变量 {} 的类型为 {}", varName, finalTypeName);
        }

        // 创建变量符号
        VariableSymbol var = new VariableSymbol(varName, finalType);

        if (finalType == null) {
             CompilerLogger.error(ctx, "Unknown type for variable: " + varName + ". Base type was " + (baseType != null ? baseType.getName() : "null"));
        } else if (finalType == TypeTable.VOID && baseType == null && isArrayDeclaration) {
            // This case means baseType was null for an array, error already logged by "Unknown base type for array variable".
        } else {
            // 将变量ID节点与其类型关联起来
            if (ctx.ID() != null) {
                String typeNameToLog = (finalType.getName() != null) ? finalType.getName() : finalType.toString();
                stashType(ctx.ID(), finalType); // Associate the ID node with the final type
                logger.debug("将变量ID节点 {} 与类型 {} 关联", ctx.ID().getText(), typeNameToLog);
            }
        }

        // 将变量添加到当前作用域
        Scope scope = scopes.get(ctx); // currentScope should be correct here from DefPhase/stashScope
        scope.define(var);

        // 如果有表达式，确保它们被处理（可能包括数组大小和初始化表达式）
        if (ctx.expr() != null && !ctx.expr().isEmpty()) {
            for (ExprContext expr : ctx.expr()) {
                if (expr != null) {
                    visit(expr);
                }
            }
        }

        return null;
    }

    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);
        Scope scope = scopes.get(ctx);

        scope.define(var);
        return null;
    }

    @Override
    public Object visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        super.visitFunctionDecl(ctx);
        Symbol method = this.scopes.resolve(ctx);
        String returnType = ctx.type().getStart().getText();
        method.type = method.scope.lookup(returnType);

        return null;
    }

    @Override
    public Object visitExprStructMethodCall(ExprStructMethodCallContext ctx) {
        super.visitExprStructMethodCall(ctx);

        // 获取结构体表达式和方法名
        ExprContext structExpr = ctx.expr(0);
        String methodName = ctx.ID().getText();

        // 获取结构体类型
        Type structType = types.get(structExpr);
        if (structType == null) {
            // 先访问结构体表达式以获取其类型
            visit(structExpr);
            structType = types.get(structExpr);
        }

        if (structType != null) {
            // 处理typedef可能指向的结构体
            if (structType instanceof TypedefSymbol) {
                Type targetType = ((TypedefSymbol) structType).getTargetType();
                if (targetType instanceof StructSymbol) {
                    structType = targetType;
                }
            }

            if (structType instanceof StructSymbol) {
                StructSymbol structSymbol = (StructSymbol) structType;
                Symbol methodSymbol = structSymbol.resolveMember(methodName);

                if (methodSymbol != null && methodSymbol.type != null) {
                    stashType(ctx, methodSymbol.type);
                } else {
                    stashType(ctx, TypeTable.VOID);
                }
            } else {
                stashType(ctx, TypeTable.VOID);
            }
        } else {
            stashType(ctx, TypeTable.VOID);
        }

        return null;
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        // 先访问结构体表达式，确保其类型被正确解析
        if (ctx.expr() != null) {
            visit(ctx.expr());
        }

        // 获取结构体表达式和字段名
        String fieldName = ctx.ID() != null ? ctx.ID().getText() : "";

        // 获取结构体类型
        Type structType = types.get(ctx.expr());
        if (structType != null) {
            // 处理typedef可能指向的结构体
            if (structType instanceof TypedefSymbol) {
                Type targetType = ((TypedefSymbol) structType).getTargetType();
                if (targetType instanceof StructSymbol) {
                    structType = targetType;
                }
            }

            if (structType instanceof StructSymbol) {
                StructSymbol structSymbol = (StructSymbol) structType;
                Symbol fieldSymbol = structSymbol.resolveMember(fieldName);

                if (fieldSymbol != null && fieldSymbol.type != null) {
                    // 将字段的类型关联到整个字段访问表达式
                    stashType(ctx, fieldSymbol.type);
                    logger.debug("结构体字段访问 {} 的类型为 {}", ctx.getText(), fieldSymbol.type);
                } else {
                    stashType(ctx, TypeTable.VOID);
                    logger.debug("结构体字段 {} 未找到或类型为空", fieldName);
                }
            } else {
                stashType(ctx, TypeTable.VOID);
                logger.debug("表达式 {} 不是结构体类型", ctx.expr().getText());
            }
        } else {
            stashType(ctx, TypeTable.VOID);
            logger.debug("无法确定结构体表达式 {} 的类型", ctx.expr().getText());
        }

        return null;
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);

        // 检查函数名是否存在
        if (ctx.ID() == null) {
            CompilerLogger.error(ctx, "函数调用缺少函数名: " + ctx.getText());
            stashType(ctx, TypeTable.VOID);
            return null;
        }

        // 获取函数名 - 根据语法，函数名来自ID，不是expr(0)
        String funcName = ctx.ID().getText();

        // 特殊处理内置函数print
        if (funcName.equals("print")) {
            // 为print函数设置void返回类型
            stashType(ctx, TypeTable.VOID);
            logger.debug("处理print函数调用，设置返回类型为void");
            return null;
        }

        // 对于普通函数，从符号表中查找并设置类型
        Scope scope = scopes.get(ctx);
        if (scope != null) {
            Symbol symbol = scope.resolve(funcName);
            if (symbol != null && symbol.type != null) {
                stashType(ctx, symbol.type);
            } else {
                // 如果找不到函数，设置为void类型
                stashType(ctx, TypeTable.VOID);
            }
        } else {
            stashType(ctx, TypeTable.VOID);
        }

        return null;
    }

    // @Override
    // public void exitExpr_Array(Expr_ArrayContext ctx) {
    //     copyType(ctx.expr(ARRAY_EXPR), ctx);
    // }

    @Override
    public Object visitStructMemeber(StructMemeberContext ctx) {
        super.visitStructMemeber(ctx);

        if (ctx.type() != null) {
            Symbol s = scopes.resolve(ctx);
            if (s != null) {
                Type fieldType = scopes.lookup(ctx.type());
                if (fieldType != null) {
                    s.type = fieldType;

                    // 如果是结构体方法，处理方法参数
                    if (ctx.block() != null && ctx.formalParameters() != null) {
                        logger.debug("处理结构体方法参数: {}", Util.name(ctx));

                        // 确保方法参数被正确解析
                        if (ctx.formalParameters().formalParameter() != null) {
                            for (FormalParameterContext paramCtx : ctx.formalParameters().formalParameter()) {
                                // 获取参数类型
                                Type paramType = scopes.lookup(paramCtx.type());
                                if (paramType != null) {
                                    // 获取参数名
                                    String paramName = paramCtx.ID().getText();
                                    logger.debug("结构体方法参数: {} 类型: {}", paramName, paramType);

                                    // 在方法作用域中查找参数符号
                                    Scope methodScope = scopes.get(paramCtx);
                                    if (methodScope != null) {
                                        Symbol paramSymbol = methodScope.resolve(paramName);
                                        if (paramSymbol != null) {
                                            paramSymbol.type = paramType;
                                            // 将参数ID节点与类型关联
                                            stashType(paramCtx.ID(), paramType);
                                        } else {
                                            CompilerLogger.error(paramCtx, "无法解析方法参数: " + paramName);
                                        }
                                    } else {
                                        CompilerLogger.error(paramCtx, "无法找到方法参数的作用域: " + paramName);
                                    }
                                } else {
                                    CompilerLogger.error(paramCtx, "未知的参数类型: " + paramCtx.type().getText());
                                }
                            }
                        }
                    }
                } else {
                    CompilerLogger.error(ctx, "未知类型: " + ctx.type().getText());
                }
            } else {
                CompilerLogger.error(ctx, "无法解析结构体成员: " + Util.name(ctx));
            }
        }
        return null;
    }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        super.visitExprGroup(ctx);
        copyType(ctx.expr(), ctx);
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        if (node.getSymbol().getText().equals(".")) {
            ParserRuleContext parent = (ParserRuleContext) node.getParent();
            Type structType = types.get(parent.getChild(STRUCT));
            if (structType == null) {
                if (parent.getChild(STRUCT) instanceof ParserRuleContext) {
                    CompilerLogger.error((ParserRuleContext)parent.getChild(STRUCT), "无法确定结构体类型");
                } else {
                    logger.error("无法确定结构体类型: {}", parent.getChild(STRUCT).getText());
                }
                return null;
            }

            // 获取成员名称
            String name = "";
            if (parent.getChildCount() > MEMBER_PARENT && parent.getChild(MEMBER_PARENT) != null) {
                if (parent.getChild(MEMBER_PARENT).getChildCount() > MEMBER &&
                    parent.getChild(MEMBER_PARENT).getChild(MEMBER) != null) {

                    // 获取成员名称
                    if (parent.getChild(MEMBER_PARENT).getChild(MEMBER) instanceof ParserRuleContext) {
                        ParserRuleContext member = (ParserRuleContext) parent.getChild(MEMBER_PARENT).getChild(MEMBER);
                        name = member.start.getText();
                    } else if (parent.getChild(MEMBER_PARENT).getChild(MEMBER) instanceof TerminalNode) {
                        name = parent.getChild(MEMBER_PARENT).getChild(MEMBER).getText();
                    } else {
                        logger.error("无法获取结构体成员名称");
                        return null;
                    }
                } else {
                    logger.error("无法获取结构体成员");
                    return null;
                }
            } else {
                logger.error("无法获取结构体成员父节点");
                return null;
            }

            // 处理结构体类型可能是TypedefSymbol的情况
            StructSymbol struct = null;
            if (structType instanceof TypedefSymbol) {
                Type targetType = ((TypedefSymbol) structType).getTargetType();
                if (targetType instanceof StructSymbol) {
                    struct = (StructSymbol) targetType;
                } else {
                    logger.error("类型 {} 不是结构体类型", ((TypedefSymbol) structType).getName());
                    return null;
                }
            } else if (structType instanceof StructSymbol) {
                struct = (StructSymbol) structType;
            } else {
                logger.error("类型 {} 不是结构体类型", structType);
                return null;
            }

            Symbol memberSymbol = struct.resolveMember(name);
            if (memberSymbol != null) {
                Type memberType = memberSymbol.type;
                logger.debug("结构体 {} 访问成员 {} 的类型为 {}", struct.getName(), name, memberType);

                // 将成员类型与父节点关联
                if (parent.getChild(MEMBER_PARENT).getChild(MEMBER) instanceof ParserRuleContext) {
                    ParserRuleContext member = (ParserRuleContext) parent.getChild(MEMBER_PARENT).getChild(MEMBER);
                    stashType(member, memberType);
                }

                // 将成员类型与整个表达式关联
                stashType(parent, memberType);

                // 支持嵌套结构体：如果成员本身是结构体类型，确保它也被正确处理
                if (memberType instanceof StructSymbol || 
                    (memberType instanceof TypedefSymbol && 
                     ((TypedefSymbol) memberType).getTargetType() instanceof StructSymbol)) {
                    // 不需要在这里做额外处理，因为当访问嵌套结构体字段时，
                    // 会再次调用此方法，此时parent.getChild(STRUCT)将是上一级的字段访问表达式
                    logger.debug("成员 {} 是结构体类型，支持嵌套访问", name);
                }
            } else {
                logger.error("结构体 {} 没有名为 {} 的成员", struct.getName(), name);
            }
        }

        return null;
    }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        super.visitExprBinary(ctx);

        copyType(ctx.expr(LEFT), ctx);

        return null;
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        super.visitExprUnary(ctx);
        copyType(ctx.expr(), ctx);
        return null;
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        super.visitExprPrimary(ctx);
        copyType(ctx.primary(), ctx);
        return null;
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        // 布尔值直接使用BOOLEAN类型
        stashType(ctx, TypeTable.BOOLEAN);

        // 确保布尔字面量也被正确识别
        String boolText = ctx.getText();
        if ("true".equals(boolText) || "false".equals(boolText)) {
            // 将布尔字面量与BOOLEAN类型关联
            types.put(ctx, TypeTable.BOOLEAN);
            logger.debug("将布尔字面量 {} 与类型 BOOLEAN 关联", boolText);
        }

        return null;
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        // 检查是否是结构体字段访问中的字段名，如果是则跳过类型设置
        // 因为字段名应该由TypeCheckVisitor的visitExprStructFieldAccess处理
        if (isFieldNameInStructAccess(ctx)) {
            return null;
        }
        setType(ctx);
        return null;
    }

    /**
     * 检查当前ID是否是结构体字段访问表达式中的字段名
     */
    private boolean isFieldNameInStructAccess(PrimaryIDContext ctx) {
        // 检查父节点链：PrimaryID -> ExprPrimary -> ExprStructFieldAccess
        if (ctx.getParent() != null && 
            ctx.getParent().getParent() != null &&
            ctx.getParent().getParent() instanceof ExprStructFieldAccessContext) {

            ExprStructFieldAccessContext fieldAccessCtx = (ExprStructFieldAccessContext) ctx.getParent().getParent();
            // 检查当前ID是否是字段访问表达式的右侧（字段名）
            // 根据语法 expr o='.' ID，字段名通过ID()方法获取
            if (fieldAccessCtx.ID() != null &&
                fieldAccessCtx.ID().getText().equals(ctx.getText())) {
                return true;
            }
        }
        return false;
    }

    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitType(TypeContext ctx) {
        // 处理类型节点，确保它们被正确关联到类型对象
        String typeName = ctx.getText();
        logger.debug("处理类型节点: {}", typeName);

        // 首先尝试从TypeTable获取基本类型
        Type type = TypeTable.getTypeByName(typeName);

        if (type != null) {
            // 如果是基本类型，直接关联
            stashType(ctx, type);
            logger.debug("类型节点 {} 关联到基本类型 {}", typeName, type);
        } else {
            // 如果不是基本类型，尝试从作用域中解析
            Scope scope = scopes.get(ctx);
            if (scope != null) {
                Symbol symbol = scope.resolve(typeName);
                if (symbol instanceof Type) {
                    stashType(ctx, (Type) symbol);
                    logger.debug("类型节点 {} 关联到类型符号 {}", typeName, symbol);
                } else if (symbol != null && symbol.type != null) {
                    stashType(ctx, symbol.type);
                    logger.debug("类型节点 {} 关联到符号类型 {}", typeName, symbol.type);
                } else {
                    // 再次尝试从全局作用域中查找基本类型
                    Scope globalScope = findGlobalScope(scope);
                    if (globalScope != null) {
                        Symbol globalSymbol = globalScope.resolve(typeName);
                        if (globalSymbol instanceof Type) {
                            stashType(ctx, (Type) globalSymbol);
                            logger.debug("类型节点 {} 从全局作用域关联到类型符号 {}", typeName, globalSymbol);
                        } else if (globalSymbol != null && globalSymbol.type != null) {
                            stashType(ctx, globalSymbol.type);
                            logger.debug("类型节点 {} 从全局作用域关联到符号类型 {}", typeName, globalSymbol.type);
                        } else {
                            logger.error("无法解析类型: {}", typeName);
                        }
                    } else {
                        logger.error("无法解析类型: {}", typeName);
                    }
                }
            } else {
                logger.error("找不到类型节点的作用域: {}", typeName);
            }
        }

        return null;
    }

    /**
     * 查找全局作用域
     * @param scope 当前作用域
     * @return 全局作用域
     */
    private Scope findGlobalScope(Scope scope) {
        Scope current = scope;
        while (current != null && current.getEnclosingScope() != null) {
            current = current.getEnclosingScope();
        }
        return current;
    }

    @Override
    public Object visitTypedefDecl(TypedefDeclContext ctx) {
        // 获取typedef声明的名称和目标类型
        String typeName = ctx.ID().getText();
        String targetTypeName = ctx.type().getText();

        // 首先尝试从作用域中获取目标类型
        Scope scope = scopes.get(ctx);
        if (scope == null) {
            CompilerLogger.error(ctx, "找不到作用域");
            return null;
        }

        Symbol targetTypeSymbol = scope.resolve(targetTypeName);
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

            // 如果仍然找不到，尝试从全局作用域查找
            if (targetType == null) {
                Scope globalScope = findGlobalScope(scope);
                if (globalScope != null) {
                    Symbol globalSymbol = globalScope.resolve(targetTypeName);
                    if (globalSymbol instanceof Type) {
                        targetType = (Type) globalSymbol;
                    } else if (globalSymbol != null && globalSymbol.type != null) {
                        targetType = globalSymbol.type;
                    }
                }
            }
        }

        if (targetType == null) {
            CompilerLogger.error(ctx, "未知的类型: " + targetTypeName);
            return null;
        }

        Symbol typedefSymbol = scope.resolve(typeName);
        if (typedefSymbol instanceof TypedefSymbol) {
            ((TypedefSymbol) typedefSymbol).setTargetType(targetType);
            logger.debug("解析类型别名: {} -> {}", typeName, targetType);
        } else {
            CompilerLogger.error(ctx, "内部错误: 无法找到类型定义符号 " + typeName);
        }

        return null;
    }

    private void setType(ParserRuleContext ctx) {
        // already defined type as in the case of struct members
        if (types.get(ctx) != null) {
            return;
        }

        int tokenValue = ctx.start.getType();
        String tokenName = ctx.start.getText();

        // 特殊处理布尔字面量
        if (tokenName.equals("true") || tokenName.equals("false")) {
            stashType(ctx, TypeTable.BOOLEAN);
            return;
        }

        if (tokenValue == CymbolParser.ID) {
            Scope scope = scopes.get(ctx);
            Symbol s = scope.resolve(tokenName);

            if (s == null) {
                CompilerLogger.error(ctx, "Unknown type for id: " + tokenName);
            } else {
                stashType(ctx, s.type);
            }
        } else {
            // 尝试按名称查找基本类型
            Type type = TypeTable.getTypeByName(tokenName);
            if (type != null) {
                stashType(ctx, type);
                return;
            }

            // 如果不是已知类型名称，按照token类型判断
            if (tokenValue == CymbolParser.INT || tokenName.equals("int")) {
                stashType(ctx, TypeTable.INT);
            } else if (tokenValue == CymbolParser.FLOAT || tokenName.equals("float")) {
                stashType(ctx, TypeTable.FLOAT);
            } else if (tokenValue == CymbolParser.CHAR || tokenName.equals("char")) {
                stashType(ctx, TypeTable.CHAR);
            } else if (tokenValue == CymbolParser.STRING || tokenName.equals("String")) {
                stashType(ctx, TypeTable.STRING);
            } else if (tokenValue == CymbolParser.BOOLEAN || tokenName.equals("bool")) {
                stashType(ctx, TypeTable.BOOLEAN);
            } else if (tokenName.equals("void")) {
                stashType(ctx, TypeTable.VOID);
            } else if (tokenName.equals("null")) {
                stashType(ctx, TypeTable.NULL);
            }
        }
    }

    /// bind (ctx,type)
    private void stashType(ParserRuleContext ctx, Type type) {
        types.put(ctx, type);
    }

    /// bind (terminalNode,type) - 用于处理终结符节点，如变量ID
    private void stashType(org.antlr.v4.runtime.tree.TerminalNode node, Type type) {
        // 将终结符节点转换为ParserRuleContext的父节点
        if (node != null && node.getParent() instanceof ParserRuleContext) {
            ParserRuleContext parent = (ParserRuleContext) node.getParent();
            // 在types中存储节点文本到类型的映射
            logger.debug("将终结符节点 {} 与类型 {} 关联", node.getText(), type);
            types.put(parent, type);
        }
    }

    // pass `from` type to `to` type
    private void copyType(ParserRuleContext from, ParserRuleContext to) {
        Type type = types.get(from);
        types.put(to, type);
    }
}
