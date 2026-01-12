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
import org.teachfx.antlr4.ep21.pass.cfg.LivenessAnalysis;
import org.teachfx.antlr4.ep21.pass.cfg.TailRecursionOptimizer;
import org.teachfx.antlr4.ep21.pass.codegen.CodeGenerationResult;
import org.teachfx.antlr4.ep21.pass.codegen.GeneratorFactory;
import org.teachfx.antlr4.ep21.pass.codegen.ICodeGenerator;
import org.teachfx.antlr4.ep21.pass.codegen.StackVMGenerator;
import org.teachfx.antlr4.ep21.pass.codegen.VMTargetType;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;
import org.teachfx.antlr4.ep21.utils.StreamUtils;
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
     * 使用Java 21字符串模板优化错误信息
     */
    private static Path resolveOutputDirectory() {
        // Java 21: 使用switch表达式简化条件逻辑
        return switch (resolveOutputDirectoryStrategy()) {
            case 0 -> tryResolveFromClassLoader();
            case 1 -> tryResolveFromProjectRoot();
            case 2 -> resolveFallbackPath();
            default -> throw new RuntimeException("无法解析输出目录路径");
        };
    }

    private static int resolveOutputDirectoryStrategy() {
        // 策略1：从当前类的资源目录获取路径
        try {
            var classLoader = Compiler.class.getClassLoader();
            var resource = classLoader.getResource("");
            if (resource != null) {
                var path = Paths.get(resource.toURI());
                if (Files.exists(path) && Files.isDirectory(path)) {
                    logger.debug("使用类加载器资源路径: {}", path);
                    return 0;
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
                    return 1;
                }
                
                var resourcesDir = projectRoot.resolve("src/main/resources");
                if (Files.exists(resourcesDir) && Files.isDirectory(resourcesDir)) {
                    logger.debug("使用src/main/resources目录: {}", resourcesDir);
                    return 1;
                }
                
                logger.debug("使用项目根目录: {}", projectRoot);
                return 1;
            }
        } catch (Exception e) {
            logger.debug("从项目根目录推断路径失败: {}", e.getMessage());
        }

        // 策略3：回退到当前工作目录
        return 2;
    }

    private static Path tryResolveFromClassLoader() {
        // 这个方法通过策略0调用，直接返回路径
        try {
            var classLoader = Compiler.class.getClassLoader();
            var resource = classLoader.getResource("");
            if (resource != null) {
                return Paths.get(resource.toURI());
            }
        } catch (URISyntaxException | SecurityException e) {
            logger.debug("从类加载器获取路径失败: {}", e.getMessage());
        }
        throw new RuntimeException("无法从类加载器解析路径");
    }

    private static Path tryResolveFromProjectRoot() {
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
                    return targetClasses;
                }
                
                var resourcesDir = projectRoot.resolve("src/main/resources");
                if (Files.exists(resourcesDir) && Files.isDirectory(resourcesDir)) {
                    return resourcesDir;
                }
                
                return projectRoot;
            }
        } catch (Exception e) {
            logger.debug("从项目根目录推断路径失败: {}", e.getMessage());
        }
        throw new RuntimeException("无法从项目根目录解析路径");
    }

    private static Path resolveFallbackPath() {
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
                                    // 应用尾递归优化
                                    System.out.println("添加TailRecursionOptimizer到CFG...");
                                    cfg.addOptimizer(new TailRecursionOptimizer());
                                    System.out.println("应用优化器...");
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
                .forEach(irNodeList -> {
                    VMTargetType targetType = VMTargetType.STACK_VM;
                    logger.info("开始为 {} 生成字节码", targetType.getIdentifier());
                    logger.debug("IR节点数量: {}", irNodeList.size());

                    GeneratorFactory factory = new GeneratorFactory();
                    ICodeGenerator generator = factory.createGenerator(targetType);
                    CodeGenerationResult result = generator.generateFromInstructions(irNodeList);

                    if (result.isSuccess()) {
                        logger.info("字节码生成成功");
                        logger.info("目标虚拟机: {}", result.getTargetVM());
                        logger.info("生成指令数: {}", result.getInstructionCount());
                        logger.info("生成耗时: {}ms", result.getGenerationTimeMs());

                        // 保存生成的字节码
                        saveVMCode(result.getOutput());
                    } else {
                        logger.error("字节码生成失败");
                        for (String error : result.getErrors()) {
                            logger.error("  - {}", error);
                        }
                    }
                });
    }

    /**
     * 将虚拟机汇编代码保存到文件
     * 文件名格式为 output_<timestamp>_<index>.vm，避免文件覆盖问题
     *
     * @param buffer 要保存的汇编代码内容，不能为null或空
     * @throws IllegalArgumentException 如果buffer为null或空
     * @throws RuntimeException 如果文件保存失败
     */
    protected static void saveVMCode(String buffer) {
        // 输入验证
        if (buffer == null || buffer.trim().isEmpty()) {
            logger.warn("尝试保存空的汇编代码内容");
            throw new IllegalArgumentException("汇编代码内容不能为空");
        }
        
        try {
            // 使用健壮的路径解析方法
            var outputDir = ensureOutputDirectory();
            
            // 生成唯一的文件名，避免覆盖问题
            var timestamp = System.currentTimeMillis();
            var fileName = "output_%d.vm".formatted(timestamp);
            var filePath = outputDir.resolve(fileName);
            
            logger.info("开始保存虚拟机汇编代码到: {}", filePath);
            
            // 使用NIO进行文件操作，更高效和健壮
            Files.writeString(filePath, buffer);
            
            logger.info("成功保存虚拟机汇编代码到: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("保存虚拟机汇编代码失败 - I/O错误: {}", e.getMessage(), e);
            throw new RuntimeException("保存虚拟机汇编代码到文件失败: " + e.getMessage(), e);
        } catch (SecurityException e) {
            logger.error("保存虚拟机汇编代码失败 - 权限错误: {}", e.getMessage(), e);
            throw new RuntimeException("没有权限保存虚拟机汇编代码到文件: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("保存虚拟机汇编代码失败 - 未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("保存虚拟机汇编代码时发生未知错误: " + e.getMessage(), e);
        }
    }

    /**
     * 将Mermaid格式的控制流图保存到Markdown文件
     *
     * @param mermaidContent Mermaid图表内容，不能为null或空
     * @param fileSuffix 文件后缀，用于生成唯一文件名，不能为null或空
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果文件保存失败
     */
    protected static void saveMermaidControlFlowGraph(String mermaidContent, String fileSuffix) {
        // 1. 参数验证 - 防御性编程
        if (mermaidContent == null || mermaidContent.trim().isEmpty()) {
            logger.warn("尝试保存空的Mermaid内容");
            throw new IllegalArgumentException("Mermaid图表内容不能为空");
        }
        
        if (fileSuffix == null || fileSuffix.trim().isEmpty()) {
            logger.warn("尝试使用空的文件后缀");
            throw new IllegalArgumentException("文件后缀不能为空");
        }
        
        // 2. 文件后缀安全检查 - 防止路径遍历攻击
        String safeSuffix = fileSuffix.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (!safeSuffix.equals(fileSuffix)) {
            logger.warn("文件后缀包含非法字符，已清理: {} -> {}", fileSuffix, safeSuffix);
        }
        
        try {
            // 3. 使用健壮的路径解析方法
            Path outputDir = ensureOutputDirectory();
            String fileName = "cfg_mermaid_%s.md".formatted(safeSuffix);
            Path filePath = outputDir.resolve(fileName);
            
            // 4. 路径安全性检查 - 确保文件在输出目录内
            if (!filePath.normalize().startsWith(outputDir.normalize())) {
                logger.error("非法的文件路径: {}", filePath);
                throw new SecurityException("生成的文件路径超出了允许的输出目录");
            }
            
            logger.debug("开始保存Mermaid控制流图到: {}", filePath);
            
            // 5. 使用StringBuilder进行高效的字符串构建
            StringBuilder templateBuilder = new StringBuilder(128)
                .append("```mermaid\n")
                .append(mermaidContent)
                .append("\n```");
            
            String formattedContent = templateBuilder.toString();
            
            // 6. 使用NIO进行文件操作，更高效和健壮
            // 添加CREATE和TRUNCATE_EXISTING选项确保原子性
            Files.writeString(
                filePath,
                formattedContent,
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.WRITE
            );
            
            logger.info("成功保存Mermaid控制流图到: {}", filePath.toAbsolutePath());
            
        } catch (IOException e) {
            logger.error("保存Mermaid控制流图失败 - I/O错误: {}", e.getMessage(), e);
            throw new RuntimeException("保存Mermaid控制流图到文件失败: " + e.getMessage(), e);
        } catch (SecurityException e) {
            logger.error("保存Mermaid控制流图失败 - 安全错误: {}", e.getMessage(), e);
            throw new RuntimeException("没有权限保存Mermaid控制流图: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("保存Mermaid控制流图失败 - 未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("保存Mermaid控制流图时发生未知错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 保存DOT格式的控制流图 - 使用Java 21字符串模板
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
        saveMermaidControlFlowGraph(mermaidContent, suffix);
        saveDOTToFile(dotContent, suffix);
    }
    
    /**
     * 记录活性分析结果
     */
    protected static void logLivenessAnalysisResults(LivenessAnalysis livenessAnalysis, CFG<IRNode> cfg, int functionIdx) {
        logger.info("函数 {} 活性分析结果:", functionIdx);
        for (var block : cfg.nodes) {
            logger.info("  基本块 {}: liveIn={}, liveOut={}",
                block.getId(),
                livenessAnalysis.getLiveIn(block),
                livenessAnalysis.getLiveOut(block));
        }
    }
}
