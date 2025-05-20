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
        String name = Util.name(ctx);
        logger.debug("查找类型: {}", name);
        Scope scope = get(ctx);
        logger.debug("在作用域中: {}", scope);
        
        if (scope == null) {
            String msg = "找不到作用域: " + name;
            CompilerLogger.error(ctx, msg);
            return null;
        }
        
        Symbol symbol = scope.resolve(name);
        if (symbol instanceof Type) {
            logger.debug("解析类型成功: {}", name);
            return (Type) symbol;
        } else if (symbol != null && symbol.type instanceof Type) {
            logger.debug("解析符号类型成功: {}", name);
            return symbol.type;
        } else {
            String msg = "未知类型: " + name;
            CompilerLogger.error(ctx, msg);
            return null;
        }
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
