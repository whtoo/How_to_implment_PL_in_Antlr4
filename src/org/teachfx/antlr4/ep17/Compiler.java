package org.teachfx.antlr4.ep17;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep17.misc.*;
import org.teachfx.antlr4.ep17.parser.CymbolLexer;
import org.teachfx.antlr4.ep17.parser.CymbolParser;

import org.teachfx.antlr4.ep17.visitor.*;

public class Compiler {
   
    public static void main(String[] args) throws IOException {
        try {
            String fileName = new File("classes/org/teachfx/antlr4/ep17/").getAbsolutePath() + "/t.cymbol";
            if(args.length > 0) fileName = args[0];
            InputStream is = System.in;
            if(fileName != null) is = new FileInputStream(fileName);
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();
            CallGraphVisitor collector = new CallGraphVisitor();
            parseTree.accept(collector);
            System.out.println(collector.callGraph.toString());
            System.out.println(collector.callGraph.toDOT());
            System.out.println(collector.callGraph.toST().render());
        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
}
