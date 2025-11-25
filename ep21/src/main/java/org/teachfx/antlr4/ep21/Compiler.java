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
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.ControlFlowAnalysis;
import org.teachfx.antlr4.ep21.pass.codegen.CymbolAssembler;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;
import org.teachfx.antlr4.ep21.utils.StreamUtils;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Compiler {

    private final  static Logger logger = LogManager.getLogger(Compiler.class);
    
    /**
     * 验证ANTLR版本信息
     */
    private static void checkANTLRVersions() {
        logger.info("=== ANTLR版本检查 ===");
        
        try {
            // 检查运行时版本
            String runtimeVersion = org.antlr.v4.runtime.Token.class.getPackage().getImplementationVersion();
            logger.info("ANTLR运行时版本: {}", runtimeVersion);
            String toolVersion = org.antlr.v4.tool.ANTLRToolListener.class.getPackage().getImplementationVersion();
            String majorVersion = toolVersion.split("\\.")[0];

            if (runtimeVersion != null && !runtimeVersion.startsWith(majorVersion)) {
                logger.warn("⚠️  版本不匹配警告: 工具版本({})与运行时版本({})不匹配",toolVersion, runtimeVersion);
                logger.warn("这可能导致生成代码与运行时不兼容的问题");
            } else {
                logger.info("✅ 版本匹配正常");
            }
            
        } catch (Exception e) {
            logger.error("检查ANTLR版本时出错", e);
        }
        
        logger.info("=== 版本检查完成 ===");
    }
    
    /**
     * 健壮地解析输出目录路径，优先从类加载器获取资源路径，然后回退到其他策略
     */
    private static Path resolveOutputDirectory() {
        // 策略1：从当前类的资源目录获取路径
        try {
            var classLoader = Compiler.class.getClassLoader();
            var resource = classLoader.getResource("");
            if (resource != null) {
                var path = Paths.get(resource.toURI());
                if (Files.exists(path) && Files.isDirectory(path)) {
                    logger.debug("使用类加载器资源路径: {}", path);
                    return path;
                }
            }
        } catch (URISyntaxException | SecurityException e) {
            logger.debug("从类加载器获取路径失败: {}", e.getMessage());
        }

        // 策略2：从项目根目录推断路径
        try {
            var currentDir = Paths.get(System.getProperty("user.dir"));
            var projectRoot = currentDir;
            
            // 查找项目根目录（包含pom.xml的目录）
            while (projectRoot != null && !Files.exists(projectRoot.resolve("pom.xml"))) {
                projectRoot = projectRoot.getParent();
            }
            
            if (projectRoot != null) {
                var targetClasses = projectRoot.resolve("target/classes");
                if (Files.exists(targetClasses) && Files.isDirectory(targetClasses)) {
                    logger.debug("使用target/classes目录: {}", targetClasses);
                    return targetClasses;
                }
                
                var resourcesDir = projectRoot.resolve("src/main/resources");
                if (Files.exists(resourcesDir) && Files.isDirectory(resourcesDir)) {
                    logger.debug("使用src/main/resources目录: {}", resourcesDir);
                    return resourcesDir;
                }
                
                logger.debug("使用项目根目录: {}", projectRoot);
                return projectRoot;
            }
        } catch (Exception e) {
            logger.debug("从项目根目录推断路径失败: {}", e.getMessage());
        }

        // 策略3：回退到当前工作目录
        try {
            var fallbackPath = Paths.get(System.getProperty("user.dir"));
            logger.debug("使用回退路径（当前工作目录）: {}", fallbackPath);
            return fallbackPath;
        } catch (Exception e) {
            logger.error("所有路径解析策略都失败", e);
            throw new RuntimeException("无法解析输出目录路径", e);
        }
    }

    /**
     * 确保输出目录存在，如果不存在则创建
     */
    private static Path ensureOutputDirectory() {
        var outputDir = resolveOutputDirectory();
        if (!Files.exists(outputDir)) {
            try {
                Files.createDirectories(outputDir);
                logger.debug("已创建输出目录: {}", outputDir);
            } catch (IOException e) {
                logger.error("创建输出目录失败: {}", outputDir, e);
                throw new RuntimeException("无法创建输出目录", e);
            }
        }
        return outputDir;
    }
    
    protected static void printIRTree(List<IRNode> irNodeList) {
        var prettyFormatText = irNodeList.stream().map(irNode -> {
            if (irNode instanceof Label) {
                return irNode.toString();
            }
            return "    "+irNode.toString();
        }).reduce((a, b) -> a + "\n" + b);
        prettyFormatText.ifPresent(logger::debug);
    }

    /**
     * 编译器主函数，负责整个编译流程的执行
     * 
     * @param args 命令行参数，第一个参数为待编译的源文件路径
     * @throws IOException 当文件读取或写入发生错误时抛出
     */
    public static void main(String[] args) throws IOException {
        // 首先检查ANTLR版本
        checkANTLRVersions();
        
        String fileName = args.length > 0 ? args[0] : null;
        InputStream is = System.in;
        if (fileName != null) {
            is = new FileInputStream(fileName);
        } else {

            is = Compiler.class.getClassLoader().getResourceAsStream("t.cymbol");
            if (is == null) {
                fileName = "src/main/resources/t.cymbol";
                is = new FileInputStream(fileName);
            }
        }
        // 词法分析阶段
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        
        // 语法分析阶段
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        // 构建抽象语法树(AST)
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);

        // 符号表构建和局部定义处理
        astRoot.accept(new LocalDefine());

        // 生成中间表示(IR)
        var irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);

        // 优化基本块
        irBuilder.prog.optimizeBasicBlock();

        // 控制流图生成和优化处理
        Stream.of(
                        StreamUtils.indexStream(irBuilder.prog.blockList.stream()
                                        .map(irBuilder::getCFG))
                                .peek(cfgPair -> {
                                    var cfg = cfgPair.getRight();
                                    var idx = cfgPair.getLeft();
                                    
                                    // 生成Mermaid和DOT两种格式的控制流图
                                    String mermaidContent = cfg.toString();
                                    String dotContent = cfg.toDOT();
                                    saveCFGInBothFormats(mermaidContent, dotContent, "%d_origin".formatted(idx));
                                    
                                    // 应用控制流优化
                                    cfg.addOptimizer(new ControlFlowAnalysis<>());
                                    cfg.applyOptimizers();
                                    
                                    // 保存优化后的控制流图
                                    String optimizedMermaid = cfg.toString();
                                    String optimizedDot = cfg.toDOT();
                                    saveCFGInBothFormats(optimizedMermaid, optimizedDot, "%d_optimized".formatted(idx));
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
        try {
            // 使用健壮的路径解析方法
            var outputDir = ensureOutputDirectory();
            var filePath = outputDir.resolve("t.cymbol");
            
            logger.debug("保存汇编信息到: {}", filePath);
            
            // 使用NIO进行文件操作，更高效和健壮
            Files.writeString(filePath, buffer);
            
            logger.debug("成功保存汇编信息到: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("保存汇编信息失败", e);
            throw new RuntimeException("保存汇编信息到文件失败", e);
        }
    }

    protected static void saveToEp20Res(String buffer, String suffix) {
        try {
            // 使用健壮的路径解析方法
            var outputDir = ensureOutputDirectory();
            var filePath = outputDir.resolve("graph_%s.md".formatted(suffix));
            
            logger.debug("保存控制流图到: {}", filePath);
            
            // 格式化内容为Mermaid图表
            var template = """
                    ```mermaid
                    %s
                    ```
                    """.formatted(buffer);
            
            // 使用NIO进行文件操作，更高效和健壮
            Files.writeString(filePath, template);
            
            logger.debug("成功保存控制流图到: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("保存控制流图失败", e);
            throw new RuntimeException("保存控制流图到文件失败", e);
        }
    }
    
    /**
     * 保存DOT格式的控制流图
     */
    protected static void saveDOTToFile(String dotContent, String suffix) {
        try {
            var outputDir = ensureOutputDirectory();
            var filePath = outputDir.resolve("cfg_%s.dot".formatted(suffix));
            
            logger.debug("保存DOT格式控制流图到: {}", filePath);
            
            // 使用NIO进行文件操作
            Files.writeString(filePath, dotContent);
            
            logger.debug("成功保存DOT格式控制流图到: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("保存DOT格式控制流图失败", e);
            throw new RuntimeException("保存DOT格式控制流图失败", e);
        }
    }
    
    /**
     * 同时生成Mermaid和DOT两种格式的控制流图
     */
    protected static void saveCFGInBothFormats(String mermaidContent, String dotContent, String suffix) {
        saveToEp20Res(mermaidContent, suffix);
        saveDOTToFile(dotContent, suffix);
    }
}
