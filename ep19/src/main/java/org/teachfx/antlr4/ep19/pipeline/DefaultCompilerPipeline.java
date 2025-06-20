package org.teachfx.antlr4.ep19.pipeline;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolLexer;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.pass.Interpreter;
import org.teachfx.antlr4.ep19.pass.LocalDefine;
import org.teachfx.antlr4.ep19.pass.LocalResolver;
import org.teachfx.antlr4.ep19.pass.TypeCheckVisitor;

/**
 * Default implementation of the CompilerPipeline interface.
 * This class implements the standard compilation process for the Cymbol compiler.
 */
public class DefaultCompilerPipeline implements CompilerPipeline {

    private static final Logger logger = LoggerFactory.getLogger(DefaultCompilerPipeline.class);

    /**
     * Custom error handler for the parser
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

    @Override
    public CommonTokenStream lexicalAnalysis(CharStream charStream) {
        logger.debug("开始词法分析");
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        logger.debug("词法分析完成");
        return tokenStream;
    }

    @Override
    public ParseTree syntaxAnalysis(CommonTokenStream tokenStream) {
        logger.debug("开始语法分析");
        CymbolParser parser = new CymbolParser(tokenStream);

        // Set custom error handler
        parser.removeErrorListeners();
        parser.setErrorHandler(new CustomErrorStrategy());

        ParseTree parseTree = parser.file();

        // Check for syntax errors
        int syntaxErrors = parser.getNumberOfSyntaxErrors();
        if (syntaxErrors > 0) {
            logger.error("语法分析遇到 {} 个错误", syntaxErrors);
        } else {
            logger.info("语法分析完成");
        }

        return parseTree;
    }

    @Override
    public LocalDefine symbolDefinition(ParseTree parseTree) {
        logger.info("开始符号定义阶段");
        LocalDefine localDefine = new LocalDefine();
        parseTree.accept(localDefine);
        logger.info("符号定义阶段完成");
        return localDefine;
    }

    @Override
    public LocalResolver symbolResolution(ParseTree parseTree, ScopeUtil scopeUtil) {
        logger.info("开始符号解析阶段");
        LocalResolver localResolver = new LocalResolver(scopeUtil);
        parseTree.accept(localResolver);
        logger.info("符号解析阶段完成");
        return localResolver;
    }

    @Override
    public TypeCheckVisitor typeChecking(ParseTree parseTree, ScopeUtil scopeUtil, LocalResolver localResolver) {
        logger.info("开始类型检查阶段");
        TypeCheckVisitor typeChecker = new TypeCheckVisitor(scopeUtil, localResolver.types);
        parseTree.accept(typeChecker);
        logger.info("类型检查阶段完成");
        return typeChecker;
    }

    @Override
    public Object interpretation(ParseTree parseTree, ScopeUtil scopeUtil) {
        logger.info("开始解释执行阶段");
        Interpreter interpreter = new Interpreter(scopeUtil);
        Object result = null;
        try {
            interpreter.interpret(parseTree);
            logger.info("解释执行阶段完成");
        } catch (Exception e) {
            logger.error("解释执行时发生错误: {}", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Object compile(CharStream charStream) {
        logger.info("编译流程开始");

        // Compile to result
        CompilationResult result = compileToResult(charStream);

        // Check if compilation was successful
        if (!result.isSuccessful()) {
            logger.error("编译失败: {}", result.getErrorMessage());
            return null;
        }

        // Execute the compiled code
        Object executionResult = execute(result);

        logger.info("编译流程结束");
        return executionResult;
    }

    @Override
    public ParseTree compileWithoutInterpretation(CharStream charStream) {
        logger.info("编译流程开始（不包含解释执行）");

        // Compile to result
        CompilationResult result = compileToResult(charStream);

        // Check if compilation was successful
        if (!result.isSuccessful()) {
            logger.error("编译失败: {}", result.getErrorMessage());
            return null;
        }

        logger.info("编译流程结束（不包含解释执行）");
        return result.getParseTree();
    }

    @Override
    public CompilationResult compileToResult(CharStream charStream) {
        logger.info("编译到结果对象开始");

        try {
            // Lexical analysis
            CommonTokenStream tokenStream = lexicalAnalysis(charStream);

            // Syntax analysis
            ParseTree parseTree = syntaxAnalysis(tokenStream);

            // Check if syntax analysis was successful
            if (parseTree == null || parseTree.getChildCount() == 0) {
                logger.error("语法分析失败，无法生成有效的语法树");
                return new CompilationResult("语法分析失败，无法生成有效的语法树");
            }

            // Symbol definition
            LocalDefine localDefine = symbolDefinition(parseTree);

            // Create scope utility
            logger.debug("初始化作用域工具");
            ScopeUtil scopeUtil = new ScopeUtil(localDefine.getScopes());

            // Symbol resolution
            LocalResolver localResolver = symbolResolution(parseTree, scopeUtil);

            // Type checking
            TypeCheckVisitor typeChecker = typeChecking(parseTree, scopeUtil, localResolver);

            logger.info("编译到结果对象完成");

            // Create and return the compilation result
            return new CompilationResult(parseTree, scopeUtil, localDefine, localResolver, typeChecker);
        } catch (Exception e) {
            logger.error("编译过程中发生错误: {}", e.getMessage());
            e.printStackTrace();
            return new CompilationResult("编译过程中发生错误: " + e.getMessage());
        }
    }

    @Override
    public Object execute(CompilationResult result) {
        if (!result.isSuccessful()) {
            logger.error("无法执行失败的编译结果: {}", result.getErrorMessage());
            return null;
        }

        logger.info("执行已编译代码开始");

        // Execute the compiled code
        Object executionResult = interpretation(result.getParseTree(), result.getScopeUtil());

        logger.info("执行已编译代码完成");

        return executionResult;
    }
}
