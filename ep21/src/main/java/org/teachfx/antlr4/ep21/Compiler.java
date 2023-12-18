package org.teachfx.antlr4.ep21;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.parser.CymbolLexer;
import org.teachfx.antlr4.ep21.parser.CymbolParser;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.ControlFlowAnalysis;
import org.teachfx.antlr4.ep21.pass.codegen.CymbolAssembler;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;
import org.teachfx.antlr4.ep21.utils.StreamUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

        CymbolASTBuilder astBuilder = new CymbolASTBuilder();

        ASTNode astRoot = parseTree.accept(astBuilder);

        astRoot.accept(new LocalDefine());

        var irBuilder = new CymbolIRBuilder();

        astRoot.accept(irBuilder);

        irBuilder.prog.optimizeBasicBlock();

        Stream.of(
                        StreamUtils.indexStream(irBuilder.prog.blockList.stream()
                                        .map(irBuilder::getCFG))
                                .peek(cfgPair -> {
                                    var cfg = cfgPair.getRight();
                                    var idx = cfgPair.getLeft();
                                    saveToEp20Res(cfg.toString(), "%d_origin".formatted(idx));
                                    cfg.addOptimizer(new ControlFlowAnalysis<>());
                                    cfg.applyOptimizers();
                                    saveToEp20Res(cfg.toString(), "%d_optimized".formatted(idx));
                                })
                                .map(Pair::getRight)
                                .map(CFG::getIRNodes)
                                .reduce(new ArrayList<IRNode>(), (a, b) -> {
                                    a.addAll(b);
                                    return a;
                                })
                )
                .map(irNodeList -> {
                    var assembler = new CymbolAssembler();
                    assembler.visit(irNodeList);
                    return assembler;
                })
                .forEach(assembler -> {
                    saveToEp18Res(assembler.getAsmInfo());
                    logger.debug("\n%s".formatted(assembler.getAsmInfo()));
                });
    }

    protected static void saveToEp18Res(String buffer) {
        String modulePath = "../ep18/src/main/resources"; // 替换 "my-module" 为你的模块名称
        File moduleDirectory = new File(modulePath);
        logger.debug("file path %s".formatted(moduleDirectory.getAbsolutePath()));
        if (moduleDirectory.exists()) {
            logger.debug("模块路径：" + moduleDirectory.getAbsolutePath());
            var filePath = modulePath+"/t.vm";
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                    var res = file.createNewFile();
                    logger.debug("create file %s is %b".formatted(filePath,res));
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
        logger.debug("file path %s".formatted(moduleDirectory.getAbsolutePath()));
        if (moduleDirectory.exists()) {
            logger.debug("模块路径：" + moduleDirectory.getAbsolutePath());
            var filePath = modulePath+"/graph_%s.md".formatted(suffix);
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                   var res = file.createNewFile();
                   logger.debug("create file %s is %b".formatted(filePath,res));
                }
                String template = """
                        ```mermaid
                        %s
                        ```
                        """.formatted(buffer);
                outputStream.write(template.getBytes());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            logger.error("模块路径不存在！");
        }
    }
}
