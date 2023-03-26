package org.teachfx.antlr4.ep15;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep15.parser.CymbolLexer;
import org.teachfx.antlr4.ep15.parser.CymbolParser;
import org.teachfx.antlr4.ep15.visitor.LocalDefine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // TODO: Add test cases for local resolver.
        String fileName = null;
        if(args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if(fileName != null) is = new FileInputStream(fileName);
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        LocalDefine localResolver = new LocalDefine();
        parseTree.accept(localResolver);
        System.out.println("scope attached with " + localResolver.scopes);
    }
}
