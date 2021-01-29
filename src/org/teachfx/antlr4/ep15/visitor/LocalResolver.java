package org.teachfx.antlr4.ep15.visitor;

import org.teachfx.antlr4.ep15.symtab.*;
import org.teachfx.antlr4.ep15.parser.*;
import org.teachfx.antlr4.ep15.parser.CymbolParser.*;

public class LocalResolver extends CymbolBaseVisitor {
   private BaseScope gloableScope = new GlobalScope();
   private Scope currentScope = null;
   
   @Override
   public Object visitFile(CymbolParser.FileContext ctx) {
       currentScope = gloableScope;
       return super.visitFile(ctx);
   }

   @Override
   public Object visitVarDecl(VarDeclContext ctx) {
       return super.visitVarDecl(ctx);
   }
    
}
