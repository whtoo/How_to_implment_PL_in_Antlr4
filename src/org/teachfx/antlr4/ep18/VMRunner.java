package org.teachfx.antlr4.ep18;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18.stackvm.ByteCodeAssembler;
import org.teachfx.antlr4.ep18.stackvm.BytecodeDefinition;
import org.teachfx.antlr4.ep18.stackvm.VMAssemblerLexer;
import org.teachfx.antlr4.ep18.stackvm.VMAssemblerParser;

public class VMRunner {
    
    public static void main(String[] args) throws Exception {
        String fileName = null;
        fileName = new File("classes").getAbsolutePath() + "/t.vm";
        if(args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if(fileName != null) is = new FileInputStream(fileName);
        CharStream charStream = CharStreams.fromStream(is);
        VMAssemblerLexer lexer = new VMAssemblerLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
        ParseTree parseTree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        ByteCodeAssembler listener = new ByteCodeAssembler(BytecodeDefinition.instructions);
        walker.walk(listener,parseTree);
    }
}
