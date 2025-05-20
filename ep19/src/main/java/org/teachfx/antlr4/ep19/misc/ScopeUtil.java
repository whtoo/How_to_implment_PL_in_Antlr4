package org.teachfx.antlr4.ep19.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.symtab.Type;
import org.teachfx.antlr4.ep19.symtab.scope.Scope;
import org.teachfx.antlr4.ep19.symtab.symbol.Symbol;

public class ScopeUtil {

    private static final Logger logger = LoggerFactory.getLogger(ScopeUtil.class);
    private final ParseTreeProperty<Scope> scopes;

    public ScopeUtil(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    public Type lookup(ParserRuleContext ctx) {
        // 特殊处理基本类型节点
        if (ctx.getText().equals("void") || ctx.getText().equals("int") ||
            ctx.getText().equals("float") || ctx.getText().equals("bool") ||
            ctx.getText().equals("String") || ctx.getText().equals("char") ||
            ctx.getText().equals("Object")) {
            // 直接从TypeTable获取基本类型
            Type type = org.teachfx.antlr4.ep19.symtab.TypeTable.getTypeByName(ctx.getText());
            if (type != null) {
                logger.debug("直接解析基本类型成功: {}", ctx.getText());
                return type;
            }
        }

        String name = Util.name(ctx);
        logger.debug("查找类型: {}", name);
        Scope scope = get(ctx);
        logger.debug("在作用域中: {}", scope);

        if (scope == null) {
            String msg = "找不到作用域: " + name;
            CompilerLogger.error(ctx, msg);
            return null;
        }

        // 首先尝试从当前作用域解析
        Symbol symbol = scope.resolve(name);
        if (symbol instanceof Type) {
            logger.debug("解析类型成功: {}", name);
            return (Type) symbol;
        } else if (symbol != null && symbol.type instanceof Type) {
            logger.debug("解析符号类型成功: {}", name);
            return symbol.type;
        } else {
            // 如果当前作用域找不到，尝试从全局作用域查找
            Scope globalScope = findGlobalScope(scope);
            if (globalScope != null && globalScope != scope) {
                Symbol globalSymbol = globalScope.resolve(name);
                if (globalSymbol instanceof Type) {
                    logger.debug("从全局作用域解析类型成功: {}", name);
                    return (Type) globalSymbol;
                } else if (globalSymbol != null && globalSymbol.type instanceof Type) {
                    logger.debug("从全局作用域解析符号类型成功: {}", name);
                    return globalSymbol.type;
                }
            }

            // 如果仍然找不到，报错
            String msg = "未知类型: " + name;
            CompilerLogger.error(ctx, msg);
            return null;
        }
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

    public Symbol resolve(ParserRuleContext ctx) {
        String name = Util.name(ctx);
        logger.debug("查找符号: {}", name);

        Scope scope = get(ctx);
        logger.debug("在函数作用域中: {}", scope);

        if (scope == null) {
            String msg = "找不到作用域: " + name;
            CompilerLogger.error(ctx, msg);
            return null;
        }

        Symbol symbol = scope.resolve(name);
        if (symbol == null) {
            String msg = "未知符号: " + name;
            CompilerLogger.error(ctx, msg);
        } else {
            logger.debug("解析符号成功: {}", name);
        }
        return symbol;
    }

    public Scope get(ParserRuleContext ctx) {
        return scopes.get(ctx);
    }
}
