package org.teachfx.antlr4.ep6;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CSVExplor {
    public static void main(String[] args) throws IOException {
        String fileName = null;
        if(args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if(fileName != null) is = new FileInputStream(fileName);
        ANTLRInputStream input = new ANTLRInputStream(is);
        CSVLexer lexer = new CSVLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CSVParser parser = new CSVParser(tokens);
        ParseTree tree = parser.file();
        CSVReader reader = new CSVReader();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(reader, tree);
        reader.printTables();
    }
}
