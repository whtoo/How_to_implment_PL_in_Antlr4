package org.teachfx.antlr4.ep16;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep16.misc.*;
import org.teachfx.antlr4.ep16.parser.CymbolLexer;
import org.teachfx.antlr4.ep16.parser.CymbolParser;

import org.teachfx.antlr4.ep16.visitor.*;

public class Compiler {
   
    public static void main(String[] args) throws IOException {
        String fileName = null;
        if(args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if(fileName != null) is = new FileInputStream(fileName);
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        LocalDefine localDefine = new LocalDefine();
        parseTree.accept(localDefine);
        System.out.println("scope attached with " + localDefine.getScopes());
        LocalResolver localResolver = new LocalResolver(new ScopeUtil(localDefine.getScopes()));
        parseTree.accept(localResolver);
        System.out.println("types attached with " + localDefine.getScopes());
    }
}
