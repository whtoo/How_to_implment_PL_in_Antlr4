package org.teachfx.antlr4.ep15.symtab;

import org.teachfx.antlr4.ep15.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep15.parser.CymbolParser;
import org.teachfx.antlr4.ep15.parser.CymbolParser.VarDeclContext;

public class LocalResolver extends CymbolBaseVisitor {
   private BaseScope gloableScope = new GlobalScope();
   private Scope currentScope = null;

   @Override
   public Object visitFile(CymbolParser.FileContext ctx) {
       // TODO Auto-generated method stub
       currentScope = gloableScope;
       return super.visitFile(ctx);
   }

   @Override
   public Object visitVarDecl(VarDeclContext ctx) {
       // TODO Auto-generated method stub
       return super.visitVarDecl(ctx);
   }
    
}
