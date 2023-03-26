package org.teachfx.antlr4.ep17;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep17.parser.CymbolLexer;
import org.teachfx.antlr4.ep17.parser.CymbolParser;
import org.teachfx.antlr4.ep17.visitor.CallGraphVisitor;

import java.io.*;

public class Compiler {
   
    public static void main(String[] args) throws IOException {
        try {
            File inputFile = new File("t.cymbol");
            InputStream is = new FileInputStream(inputFile);
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();
            CallGraphVisitor collector = new CallGraphVisitor();
            parseTree.accept(collector);
            
            File saveFile = new File("call.dot");
            System.out.println(saveFile.getAbsolutePath());
            saveFile.createNewFile();
            
            OutputStream outputStream = new FileOutputStream(saveFile);
            outputStream.write(collector.callGraph.toDOT().getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
       
    }
}
