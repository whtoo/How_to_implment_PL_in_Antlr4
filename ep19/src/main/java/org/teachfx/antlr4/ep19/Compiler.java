package org.teachfx.antlr4.ep19;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.pipeline.CompilationResult;
import org.teachfx.antlr4.ep19.pipeline.CompilerPipeline;
import org.teachfx.antlr4.ep19.pipeline.ConfigurableCompilerPipeline;
import org.teachfx.antlr4.ep19.pipeline.DefaultCompilerPipeline;

import java.io.*;

/**
 * Cymbol编译器主类
 * 使用CompilerPipeline接口实现编译流程
 */
public class Compiler {

    private static final Logger logger = LoggerFactory.getLogger(Compiler.class);
    private static final CompilerPipeline pipeline = new DefaultCompilerPipeline();

    /**
     * 编译器入口方法
     * 
     * @param args 命令行参数，第一个参数为源文件路径
     * @throws IOException 如果文件读取失败
     */
    public static void main(String[] args) throws IOException {
        logger.info("Cymbol编译器启动");

        if (args.length > 0) {
            String command = args[0];

            switch (command) {
                case "--compile":
                    if (args.length > 2) {
                        compileAndSave(args[1], args[2]);
                    } else {
                        logger.error("缺少参数: --compile <源文件> <输出文件>");
                    }
                    break;
                case "--execute":
                    if (args.length > 1) {
                        executeCompiledFile(args[1]);
                    } else {
                        logger.error("缺少参数: --execute <编译文件>");
                    }
                    break;
                case "--static-analysis":
                    if (args.length > 1) {
                        staticAnalysis(args[1]);
                    } else {
                        logger.error("缺少参数: --static-analysis <源文件>");
                    }
                    break;
                default:
                    // 默认编译并执行
                    compileFile(args[0]);
                    break;
            }
        } else {
            // 默认编译t.cymbol
            String fileName = "t.cymbol";
            logger.debug("输入文件: {}", fileName);

            InputStream is = null;
            // 优先用类路径读取
            if (fileName != null) {
                is = Compiler.class.getClassLoader().getResourceAsStream(fileName);
                if (is == null) {
                    // 尝试用文件系统路径
                    try {
                        logger.debug("尝试从文件系统加载: {}", fileName);
                        is = new FileInputStream(fileName);
                    } catch (FileNotFoundException e) {
                        logger.error("找不到资源文件: {}", fileName, e);
                        throw e;
                    }
                } else {
                    logger.debug("从类路径加载资源: {}", fileName);
                }
            } else {
                logger.info("从标准输入读取代码");
                is = System.in;
            }

            // 创建字符流
            CharStream charStream = CharStreams.fromStream(is);

            // 使用编译管道执行完整的编译流程
            pipeline.compile(charStream);
        }
    }

    /**
     * 编译源代码文件
     * 
     * @param sourcePath 源代码文件路径
     * @throws IOException 如果文件读取失败
     */
    public static void compileFile(String sourcePath) throws IOException {
        logger.info("编译文件: {}", sourcePath);

        try (InputStream is = new FileInputStream(sourcePath)) {
            CharStream charStream = CharStreams.fromStream(is);
            pipeline.compile(charStream);
        } catch (FileNotFoundException e) {
            logger.error("找不到源文件: {}", sourcePath);
            throw e;
        }
    }

    /**
     * 编译源代码字符串
     * 
     * @param sourceCode 源代码字符串
     */
    public static void compileString(String sourceCode) {
        logger.info("编译源代码字符串");

        CharStream charStream = CharStreams.fromString(sourceCode);
        pipeline.compile(charStream);
    }

    /**
     * 编译源代码但不执行
     * 用于静态分析工具
     * 
     * @param sourcePath 源代码文件路径
     * @return 解析树
     * @throws IOException 如果文件读取失败
     */
    public static ParseTree compileWithoutExecution(String sourcePath) throws IOException {
        logger.info("编译文件（不执行）: {}", sourcePath);

        try (InputStream is = new FileInputStream(sourcePath)) {
            CharStream charStream = CharStreams.fromStream(is);
            return pipeline.compileWithoutInterpretation(charStream);
        } catch (FileNotFoundException e) {
            logger.error("找不到源文件: {}", sourcePath);
            throw e;
        }
    }

    /**
     * 编译源代码并保存编译结果到文件
     * 
     * @param sourcePath 源代码文件路径
     * @param outputPath 输出文件路径
     * @throws IOException 如果文件读取或写入失败
     */
    public static void compileAndSave(String sourcePath, String outputPath) throws IOException {
        logger.info("编译文件并保存: {} -> {}", sourcePath, outputPath);

        try (InputStream is = new FileInputStream(sourcePath)) {
            CharStream charStream = CharStreams.fromStream(is);
            CompilationResult result = pipeline.compileToResult(charStream);

            if (result.isSuccessful()) {
                saveCompilationResult(result, outputPath);
                logger.info("编译结果已保存到: {}", outputPath);
            } else {
                logger.error("编译失败: {}", result.getErrorMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error("找不到源文件: {}", sourcePath);
            throw e;
        }
    }

    /**
     * 执行已编译的代码
     * 
     * @param compiledPath 已编译文件路径
     * @throws IOException 如果文件读取失败
     */
    public static void executeCompiledFile(String compiledPath) throws IOException {
        logger.info("执行已编译文件: {}", compiledPath);

        try {
            CompilationResult result = loadCompilationResult(compiledPath);
            if (result.isSuccessful()) {
                pipeline.execute(result);
                logger.info("执行完成");
            } else {
                logger.error("无法执行: {}", result.getErrorMessage());
            }
        } catch (Exception e) {
            logger.error("执行已编译文件时发生错误: {}", e.getMessage());
            throw new IOException("执行已编译文件时发生错误", e);
        }
    }

    /**
     * 对源代码进行静态分析
     * 
     * @param sourcePath 源代码文件路径
     * @throws IOException 如果文件读取失败
     */
    public static void staticAnalysis(String sourcePath) throws IOException {
        logger.info("对文件进行静态分析: {}", sourcePath);

        // 创建一个不执行解释器的编译管道
        ConfigurableCompilerPipeline staticPipeline = new ConfigurableCompilerPipeline();
        staticPipeline.setPerformInterpretation(false);

        try (InputStream is = new FileInputStream(sourcePath)) {
            CharStream charStream = CharStreams.fromStream(is);
            CompilationResult result = staticPipeline.compileToResult(charStream);

            if (result.isSuccessful()) {
                logger.info("静态分析成功，代码没有语法或类型错误");
            } else {
                logger.error("静态分析失败: {}", result.getErrorMessage());
            }
        } catch (FileNotFoundException e) {
            logger.error("找不到源文件: {}", sourcePath);
            throw e;
        }
    }

    /**
     * 保存编译结果到文件
     * 
     * @param result 编译结果
     * @param outputPath 输出文件路径
     * @throws IOException 如果文件写入失败
     */
    private static void saveCompilationResult(CompilationResult result, String outputPath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputPath))) {
            oos.writeObject(result);
        }
    }

    /**
     * 从文件加载编译结果
     * 
     * @param compiledPath 已编译文件路径
     * @return 编译结果
     * @throws IOException 如果文件读取失败
     * @throws ClassNotFoundException 如果类型不匹配
     */
    private static CompilationResult loadCompilationResult(String compiledPath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(compiledPath))) {
            return (CompilationResult) ois.readObject();
        }
    }
}
