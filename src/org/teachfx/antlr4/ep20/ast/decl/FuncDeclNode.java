package org.teachfx.antlr4.ep20.ast.decl;

import org.antlr.v4.runtime.ParserRuleContext;

import org.teachfx.antlr4.ep20.symtab.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.Type;
import org.teachfx.antlr4.ep20.symtab.TypeTable;
import org.teachfx.antlr4.ep20.ast.expr.IDExprNode;
import org.teachfx.antlr4.ep20.symtab.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FuncDeclNode extends DeclNode {
    /// pairs of formal param with its type

    public FuncDeclNode(Type retType, String funcName,List<IDExprNode> params,ParserRuleContext ctx) {
        MethodSymbol methodSymbol = new MethodSymbol(funcName,retType,null,ctx);

        params.forEach((param) -> methodSymbol.define(param.getRefSymbol()));

        setRefSymbol(methodSymbol);

        this.declName = funcName;
        this.ctx = ctx;
    }

}
