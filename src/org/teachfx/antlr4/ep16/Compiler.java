package org.teachfx.antlr4.ep16;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep16.misc.ScopeUtil;
import org.teachfx.antlr4.ep16.parser.CymbolLexer;
import org.teachfx.antlr4.ep16.parser.CymbolParser;
import org.teachfx.antlr4.ep16.visitor.Interpreter;
import org.teachfx.antlr4.ep16.visitor.LocalDefine;
import org.teachfx.antlr4.ep16.visitor.LocalResolver;

import java.io.*;

public class Compiler {
   
    public static void main(String[] args) throws IOException,FileNotFoundException {
        String fileName = null;
        fileName = new File("classes").getAbsolutePath() + "/t.cymbol";
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
        //System.out.println("scope attached with " + localDefine.getScopes());
        ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());
        LocalResolver localResolver = new LocalResolver(scopeUtil);
        parseTree.accept(localResolver);
        //System.out.println("types attached with " + localDefine.getScopes());
        Interpreter interpreter = new Interpreter(scopeUtil);
        interpreter.interpret(parseTree);

    }
}
