package org.teachfx.antlr4.ep5;

import org.teachfx.antlr4.ep5.JavaParser.ClassDeclarationContext;
import org.teachfx.antlr4.ep5.JavaParser.MethodDeclarationContext;

import org.antlr.v4.runtime.TokenStream;

public class ExtractInterfaceListener extends JavaBaseListener {
    JavaParser parser;
    public ExtractInterfaceListener(JavaParser parser) {
        this.parser = parser;
    }

    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        System.out.println("interface I"+ctx.Identifier()+"{");
    }

    @Override
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        System.out.println("}");
    }

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        TokenStream tokens = parser.getTokenStream();
        String type = "void";
        if(ctx.type() != null){
            type = tokens.getText(ctx.type());
        }
        String args = tokens.getText(ctx.formalParameters());
        System.out.println("\t"+type+ctx.Identifier()+args+";");
    }
}
