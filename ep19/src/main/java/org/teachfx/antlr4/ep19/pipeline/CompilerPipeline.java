package org.teachfx.antlr4.ep19.pipeline;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolLexer;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.pass.Interpreter;
import org.teachfx.antlr4.ep19.pass.LocalDefine;
import org.teachfx.antlr4.ep19.pass.LocalResolver;
import org.teachfx.antlr4.ep19.pass.TypeCheckVisitor;

/**
 * CompilerPipeline interface defines the standard compilation process for the Cymbol compiler.
 * Each method represents a phase in the compilation process.
 */
public interface CompilerPipeline {

    /**
     * Performs lexical analysis on the input source code.
     * 
     * @param charStream The input character stream
     * @return A stream of tokens
     */
    CommonTokenStream lexicalAnalysis(CharStream charStream);

    /**
     * Performs syntax analysis on the token stream.
     * 
     * @param tokenStream The stream of tokens
     * @return The parse tree
     */
    ParseTree syntaxAnalysis(CommonTokenStream tokenStream);

    /**
     * Performs symbol definition on the parse tree.
     * This phase collects symbols and scope information.
     * 
     * @param parseTree The parse tree
     * @return The LocalDefine visitor with symbol information
     */
    LocalDefine symbolDefinition(ParseTree parseTree);

    /**
     * Performs symbol resolution on the parse tree.
     * This phase resolves references and assigns types.
     * 
     * @param parseTree The parse tree
     * @param scopeUtil The scope utility
     * @return The LocalResolver visitor with type information
     */
    LocalResolver symbolResolution(ParseTree parseTree, ScopeUtil scopeUtil);

    /**
     * Performs type checking on the parse tree.
     * This phase verifies type compatibility of all expressions.
     * 
     * @param parseTree The parse tree
     * @param scopeUtil The scope utility
     * @param localResolver The LocalResolver visitor with type information
     * @return The TypeCheckVisitor
     */
    TypeCheckVisitor typeChecking(ParseTree parseTree, ScopeUtil scopeUtil, LocalResolver localResolver);

    /**
     * Interprets the parse tree.
     * This phase executes the code.
     * 
     * @param parseTree The parse tree
     * @param scopeUtil The scope utility
     * @return The result of the interpretation
     */
    Object interpretation(ParseTree parseTree, ScopeUtil scopeUtil);

    /**
     * Runs the entire compilation pipeline from source code to execution.
     * 
     * @param charStream The input character stream
     * @return The result of the compilation
     */
    Object compile(CharStream charStream);

    /**
     * Runs the compilation pipeline without interpretation.
     * This is useful for static analysis tools.
     * 
     * @param charStream The input character stream
     * @return The parse tree with all annotations
     */
    ParseTree compileWithoutInterpretation(CharStream charStream);

    /**
     * Compiles the source code and returns a CompilationResult object.
     * This allows for saving the compiled code for later execution.
     * 
     * @param charStream The input character stream
     * @return The compilation result
     */
    CompilationResult compileToResult(CharStream charStream);

    /**
     * Executes previously compiled code.
     * 
     * @param result The compilation result
     * @return The result of the execution
     */
    Object execute(CompilationResult result);
}
