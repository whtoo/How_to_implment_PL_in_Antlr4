package org.teachfx.antlr4.ep16.misc;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import org.teachfx.antlr4.ep16.symtab.*;

public class ScopeUtil {
    
    private ParseTreeProperty<Scope> scopes;

    public ScopeUtil(ParseTreeProperty<Scope> scopes) {
        this.scopes = scopes;
    }

    public Type lookup(ParserRuleContext ctx) {
        String name = Util.name(ctx);
        System.out.println("lookup type is : "+name);
        Scope scope = get(ctx);
        System.out.println("scope is : "+scope);
        Type type= scope.lookup(name);
        if(type == null) { 
            String msg = "unknown type: " + name;
//            compiler.reportError(ctx, msg);
        }
        return type;
    }
    
    public Symbol resolve(ParserRuleContext ctx) {
        String name = Util.name(ctx);
        System.out.println("lookup func is : "+name);

        Scope scope = get(ctx);
        System.out.println("func scope is : "+scope);

        Symbol symbol = scope.resolve(name);
        if(symbol == null) { 
            String msg = "unknown symbol: " + name;
//            compiler.reportError(ctx, msg);
        }
        return symbol;
    }

    public Scope get(ParserRuleContext ctx) {
        return scopes.get(ctx);
    }
}
