package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.parser.*;
import org.teachfx.antlr4.ep20.pass.codegen.CymbolAssembler;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Compiler {

    public static void main(String[] args) throws IOException {
        String fileName = null;
        File file = new File("src/main/resources/t.cymbol");
        fileName = file.getAbsolutePath();
        if (args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if (fileName != null) is = new FileInputStream(fileName);
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        CymbolASTBuilder astBuilder = new CymbolASTBuilder("t.cymbol");
        ASTNode astRoot = parseTree.accept(astBuilder);
        astRoot.accept(new LocalDefine());
        var irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        var assembler = new CymbolAssembler();
        irBuilder.root.accept(assembler);
        System.out.println(assembler.flushCode());
        var url = Compiler.class.getClassLoader().getResource("t.vm");
        assert url != null;
        var savedFile = new File("src/main/resources/t.vm");
        assembler.saveToFile(savedFile);
    }
}
