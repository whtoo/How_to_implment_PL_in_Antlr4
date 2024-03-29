package org.teachfx.antlr4.ep14;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.teachfx.antlr4.ep14.compiler.MathExprParser;
import org.teachfx.antlr4.ep14.compiler.MathExprLexer;
import org.teachfx.antlr4.ep14.symtab.SymbolTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class Compiler {
    public static void main(String[] args) throws IOException {
        String fileName = "src/main/resources/t.math";
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);
        SymbolTable syTb = new SymbolTable();
        CharStream inputStream = CharStreams.fromStream(is);
        MathExprLexer lexer = new MathExprLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MathExprParser parser = new MathExprParser(tokens);
        parser.compileUnit(syTb);
    }
}
