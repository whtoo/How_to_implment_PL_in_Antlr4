package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
//import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep20.pass.cfg.ControlFlowAnalysis;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Compiler {

    private final  static Logger logger = LogManager.getLogger(Compiler.class);
    protected static void printIRTree(List<IRNode> irNodeList) {
        var prettyFormatText = irNodeList.stream().map(irNode -> {
            if (irNode instanceof Label) {
                return irNode.toString();
            }
            return "    "+irNode.toString();
        }).reduce((a, b) -> a + "\n" + b);
        logger.info("IR Tree:" + "\n"+prettyFormatText.get());
    }

    public static void main(String[] args) throws IOException {
        String fileName = args.length > 0 ? args[0] : (new File("src/main/resources/t.cymbol")).getAbsolutePath();

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
        printIRTree(irBuilder.prog.linearInstrs());

        var dataFlowAnalysis = new ControlFlowAnalysis();
        irBuilder.prog.accept(dataFlowAnalysis);

    }
}
