package org.teachfx.antlr4.ep14.symtab;

import org.teachfx.antlr4.ep14.compiler.MathExprBaseVisitor;
import org.teachfx.antlr4.ep14.compiler.MathExprParser.CompileUnitContext;

public class SymbolCollector extends MathExprBaseVisitor {

    @Override
    public Object visitCompileUnit(CompileUnitContext ctx) {
        
        return null;
    }
    
}
