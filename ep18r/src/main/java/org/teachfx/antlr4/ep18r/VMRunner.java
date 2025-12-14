package org.teachfx.antlr4.ep18r;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18r.stackvm.RegisterByteCodeAssembler;
import org.teachfx.antlr4.ep18r.stackvm.RegisterBytecodeDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VMRunner {

    public static void main(String[] args) throws Exception {
        String fileName = "t.vmr";
        if (args.length > 0) {
            fileName = args[0];
        }
        System.out.println("fileName = " + fileName);
        InputStream is = null;
        try {
            // Try to load from resources first
            is = VMRunner.class.getClassLoader().getResourceAsStream(fileName);
            if (is == null) {
                // Fallback to file system path if not found in resources
                File file = new File(fileName);
                if (file.exists()) {
                    is = new FileInputStream(file);
                } else {
                    System.err.println("Error: File not found in resources or filesystem: " + fileName);
                    return;
                }
            }
            CharStream charStream = CharStreams.fromStream(is);
            VMAssemblerLexer lexer = new VMAssemblerLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
            ParseTree parseTree = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();
            RegisterByteCodeAssembler listener = new RegisterByteCodeAssembler(RegisterBytecodeDefinition.instructions);
            walker.walk(listener, parseTree);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            throw e;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
        }
    }
}
