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

        // 获取变量类型
        Type type = scopes.lookup(ctx.type());
        String varName = Util.name(ctx);

        // 创建变量符号
        VariableSymbol var = new VariableSymbol(varName, type);

        if (type == null) {
            CompilerLogger.error(ctx, "Unknown type when declaring variable: " + var);
        } else {
            logger.debug("变量 {} 的类型为 {}", varName, type);

            // 将变量ID节点与其类型关联起来
            if (ctx.ID() != null) {
                stashType(ctx.ID(), type);
                logger.debug("将变量ID节点 {} 与类型 {} 关联", ctx.ID().getText(), type);
            }
        }

        // 将变量添加到当前作用域
        Scope scope = scopes.get(ctx);
        scope.define(var);

        // 如果有初始化表达式，确保它被处理
        if (ctx.expr() != null) {
            visit(ctx.expr());
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
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        // 这里有一个func name ctx和symbol没有建立匹配的问题
        copyType(ctx.expr(FUNC_EXPR), ctx);

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
                CompilerLogger.error((ParserRuleContext)parent.getChild(STRUCT), "无法确定结构体类型");
                return null;
            }

            ParserRuleContext member = (ParserRuleContext) parent.getChild(MEMBER_PARENT).getChild(MEMBER);
            String name = member.start.getText();

            // 处理结构体类型可能是TypedefSymbol的情况
            StructSymbol struct = null;
            if (structType instanceof TypedefSymbol) {
                Type targetType = ((TypedefSymbol) structType).getTargetType();
                if (targetType instanceof StructSymbol) {
                    struct = (StructSymbol) targetType;
                } else {
                    CompilerLogger.error(member, "类型 " + ((TypedefSymbol) structType).getName() + " 不是结构体类型");
                    return null;
                }
            } else if (structType instanceof StructSymbol) {
                struct = (StructSymbol) structType;
            } else {
                CompilerLogger.error(member, "类型 " + structType + " 不是结构体类型");
                return null;
            }

            Symbol memberSymbol = struct.resolveMember(name);
            if (memberSymbol != null) {
                Type memberType = memberSymbol.type;
                logger.debug("结构体 {} 访问字段 {} 的类型为 {}", struct.getName(), name, memberType);
                stashType(member, memberType);
            } else {
                CompilerLogger.error(member, "结构体 " + struct.getName() + " 没有名为 " + name + " 的成员");
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
        return super.visitPrimaryBOOL(ctx);
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        setType(ctx);
        return null;
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
                    logger.error("无法解析类型: {}", typeName);
                }
            } else {
                logger.error("找不到类型节点的作用域: {}", typeName);
            }
        }

        return null;
    }

    @Override
    public Object visitTypedefDecl(TypedefDeclContext ctx) {
        // 获取typedef声明的名称和目标类型
        String typeName = ctx.ID().getText();
        String targetTypeName = ctx.type().getText();

        // 首先尝试从作用域中获取目标类型
        Scope scope = scopes.get(ctx);
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
        }

        if (targetType == null) {
            CompilerLogger.error(ctx, "未知的类型: " + targetTypeName);
            return null;
        }

        // 更新TypedefSymbol中的目标类型
        if (scope == null) {
            CompilerLogger.error(ctx, "找不到作用域");
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
            } else if (tokenName.equals("true") || tokenName.equals("false") || tokenName.equals("bool")) {
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