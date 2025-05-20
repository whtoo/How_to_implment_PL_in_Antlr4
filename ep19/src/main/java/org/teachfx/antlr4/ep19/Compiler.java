package org.teachfx.antlr4.ep19;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.pass.Interpreter;
import org.teachfx.antlr4.ep19.pass.LocalDefine;
import org.teachfx.antlr4.ep19.pass.LocalResolver;
import org.teachfx.antlr4.ep19.pass.TypeCheckVisitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// 静态引入我们修改的解析器和词法分析器
import org.teachfx.antlr4.ep19.parser.CymbolLexer;
import org.teachfx.antlr4.ep19.parser.CymbolParser;

public class Compiler {

    private static final Logger logger = LoggerFactory.getLogger(Compiler.class);

    /**
     * @throws FileNotFoundException Input -> CharStream -> Tokens -> ParserTree
     *                               --LocalDefine--> AnnotatedParserTree
     *                               --LocalResolver--> AnnotatedParserTree
     *                               --TypeCheck--> TypeVerifiedParserTree
     *                               -> Interpreter
     */
    public static void main(String[] args) throws IOException {
        logger.info("Cymbol编译器启动");
        
        String fileName = null;
        fileName = "t.cymbol";
        if (args.length > 0) fileName = args[0];
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

        logger.info("编译流程开始");
        
        // 词法分析阶段
        logger.debug("开始词法分析");
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        
        // 语法分析阶段
        logger.debug("开始语法分析");
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        logger.info("语法分析完成");
        
        // 符号定义阶段 - 收集符号和作用域信息
        logger.info("开始符号定义阶段");
        LocalDefine localDefine = new LocalDefine();
        parseTree.accept(localDefine);
        logger.info("符号定义阶段完成");
        
        // 创建作用域工具
        logger.debug("初始化作用域工具");
        ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());
        
        // 符号解析阶段 - 解析引用并分配类型
        logger.info("开始符号解析阶段");
        LocalResolver localResolver = new LocalResolver(scopeUtil);
        parseTree.accept(localResolver);
        logger.info("符号解析阶段完成");
        
        // 类型检查阶段 - 验证所有表达式的类型兼容性
        logger.info("开始类型检查阶段");
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(scopeUtil, localResolver.types);
        parseTree.accept(typeChecker);
        logger.info("类型检查阶段完成");
        
        // 解释执行阶段
        logger.info("开始解释执行阶段");
        Interpreter interpreter = new Interpreter(scopeUtil);
        interpreter.interpret(parseTree);
        logger.info("解释执行阶段完成");
        
        logger.info("编译流程结束");
    }

    /**
     * 解析源代码并生成AST
     *
     * @param code 源代码字符流
     * @return 解析树根节点
     */
    public static ParseTree parse(CharStream code) {
        // 创建词法分析器和语法分析器
        CymbolLexer lexer = new CymbolLexer(code);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokens);
        
        // 设置错误处理策略，并解析代码生成AST
        parser.removeErrorListeners();
        parser.setErrorHandler(new CustomErrorStrategy());
        ParseTree tree = parser.file();
        
        // 记录语法错误数量
        int syntaxErrors = parser.getNumberOfSyntaxErrors();
        if (syntaxErrors > 0) {
            logger.error("语法分析遇到 {} 个错误", syntaxErrors);
        }
        
        return tree;
    }

    /**
     * 编译并解释源代码
     *
     * @param sourcePath 源代码文件路径
     * @throws IOException IO异常
     */
    private static void compile(String sourcePath) throws IOException {
        // 读取源代码文件
        try (InputStream is = new FileInputStream(sourcePath)) {
            CharStream input = CharStreams.fromStream(is);
            ParseTree tree = parse(input);
            
            // 检查是否有语法错误
            if (tree == null || tree.getChildCount() == 0) {
                logger.error("语法分析失败，无法生成有效的语法树");
                return;
            }
            
            // 定义变量和作用域
            LocalDefine localDefine = new LocalDefine();
            localDefine.visit(tree);
            
            // 解析变量类型
            ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());
            LocalResolver resolver = new LocalResolver(scopeUtil);
            resolver.visit(tree);
            
            // 类型检查
            TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(scopeUtil, resolver.types);
            typeCheckVisitor.visit(tree);
            
            // 解释执行代码
            logger.debug("开始解释执行代码");
            Interpreter interpreter = new Interpreter(scopeUtil);
            try {
                interpreter.interpret(tree);
                logger.debug("代码执行完成");
            } catch (Exception e) {
                logger.error("解释执行时发生错误: {}", e.getMessage());
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            logger.error("找不到源文件: {}", sourcePath);
        }
    }

    /**
     * 自定义错误处理策略，记录详细日志
     */
    private static class CustomErrorStrategy extends org.antlr.v4.runtime.DefaultErrorStrategy {
        @Override
        public void reportError(org.antlr.v4.runtime.Parser recognizer, org.antlr.v4.runtime.RecognitionException e) {
            logger.error("语法错误 at line {}:{}: {}", 
                e.getOffendingToken().getLine(), 
                e.getOffendingToken().getCharPositionInLine(),
                e.getMessage());
            super.reportError(recognizer, e);
        }
    }
}
