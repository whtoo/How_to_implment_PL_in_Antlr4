package org.teachfx.antlr4.ep20;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep20.pass.cfg.ControlFlowAnalysis;
import org.teachfx.antlr4.ep20.pass.codegen.CymbolAssembler;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.*;
import java.util.LinkedList;
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
        prettyFormatText.ifPresent(logger::debug);
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

        irBuilder.prog.optimizeBasicBlock();
        var cfgOptimizer = new ControlFlowAnalysis<IRNode>();
        var cnt = 0;
        var codeBuffer = new LinkedList<IRNode>();

        for(var funBlock : irBuilder.prog.blockList) {
            var cfg = irBuilder.getCFG(funBlock);
            saveToEp20Res(cfg.toString(),"origin"+cnt);
            cfg.addOptimizer(cfgOptimizer);
            cfg.applyOptimizers();
            saveToEp20Res(cfg.toString(),"optimized"+cnt);
            cnt++;
            logger.info("CFG:\n" + cfg.toString());
            codeBuffer.addAll(cfg.getIRNodes());
        }

        var assembler = new CymbolAssembler();
        assembler.visit(codeBuffer);
        saveToEp18Res(assembler.getAsmInfo());
        logger.debug("\n%s".formatted(assembler.getAsmInfo()));
    }

    protected static void saveToEp18Res(String buffer) {
        String modulePath = "../ep18/src/main/resources"; // 替换 "my-module" 为你的模块名称
        File moduleDirectory = new File(modulePath);
        logger.info("file path %s".formatted(moduleDirectory.getAbsolutePath()));
        if (moduleDirectory.exists()) {
            logger.info("模块路径：" + moduleDirectory.getAbsolutePath());
            var filePath = modulePath+"/t.vm";
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                outputStream.write(buffer.getBytes());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            logger.error("模块路径不存在！");
        }
    }

    protected static void saveToEp20Res(String buffer,String suffix) {
        String modulePath = "./src/main/resources"; // 替换 "my-module" 为你的模块名称
        File moduleDirectory = new File(modulePath);
        logger.info("file path %s".formatted(moduleDirectory.getAbsolutePath()));
        if (moduleDirectory.exists()) {
            logger.info("模块路径：" + moduleDirectory.getAbsolutePath());
            var filePath = modulePath+"/graph_%s.md".formatted(suffix);
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                var mdTmeplate = """
                        ```mermaid
                        %s
                        ```
                        """.formatted(buffer);
                outputStream.write(mdTmeplate.getBytes());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            logger.error("模块路径不存在！");
        }
    }
}
