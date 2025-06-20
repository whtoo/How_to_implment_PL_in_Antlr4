package org.teachfx.antlr4.ep19;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolLexer;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.pass.LocalDefine;
import org.teachfx.antlr4.ep19.pass.LocalResolver;
import org.teachfx.antlr4.ep19.pass.TypeCheckVisitor;
import org.teachfx.antlr4.ep19.pass.Interpreter;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class CompilerTestUtil {

    public static class CompilationResult {
        public final boolean success;
        public final List<String> errors;
        public final String output;

        public CompilationResult(boolean success, List<String> errors, String output) {
            this.success = success;
            this.errors = errors;
            this.output = output;
        }
    }

    public static CompilationResult compile(String cymbolCode) {
        return compile(cymbolCode, false);
    }

    /**
     * Compiles and optionally interprets Cymbol code.
     * 
     * @param cymbolCode The Cymbol code to compile
     * @param interpret Whether to run the interpreter
     * @return The compilation result
     */
    public static CompilationResult compile(String cymbolCode, boolean interpret) {
        PrintStream originalErr = System.err;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();

        List<String> errorMessages = new ArrayList<>();
        boolean success = true;
        ParseTree parseTree = null;
        ScopeUtil scopeUtil = null;

        try {
            System.setErr(new PrintStream(errContent));
            System.setOut(new PrintStream(outContent)); // Capture print output as well for interpreter tests

            // Override CompilerLogger's error reporting to capture messages
            CompilerLogger.setErrorListener(errorMessages::add);

            CharStream charStream = CharStreams.fromString(cymbolCode);
            CymbolLexer lexer = new CymbolLexer(charStream);
            // Remove default error listeners to prevent console spam during tests
            lexer.removeErrorListeners(); 
            // Add a custom listener if needed to capture lexer errors specifically, or rely on parser

            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            // Remove default error listeners for parser too
            parser.removeErrorListeners();
            // Add a custom listener to capture parser syntax errors into errorMessages list
            parser.addErrorListener(new org.antlr.v4.runtime.BaseErrorListener() {
                @Override
                public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, 
                                        Object offendingSymbol, 
                                        int line, 
                                        int charPositionInLine, 
                                        String msg, 
                                        org.antlr.v4.runtime.RecognitionException e) {
                    errorMessages.add("Syntax Error: line " + line + ":" + charPositionInLine + " " + msg);
                }
            });

            parseTree = parser.file();

            if (!errorMessages.isEmpty()) { // Check for syntax errors before proceeding
                return new CompilationResult(false, errorMessages, outContent.toString());
            }

            LocalDefine localDefine = new LocalDefine();
            parseTree.accept(localDefine);

            scopeUtil = new ScopeUtil(localDefine.getScopes());

            LocalResolver localResolver = new LocalResolver(scopeUtil);
            parseTree.accept(localResolver);

            // If errors occurred in LocalDefine or LocalResolver via CompilerLogger, they are captured.
            if (!errorMessages.isEmpty()) {
                 return new CompilationResult(false, errorMessages, outContent.toString());
            }

            TypeCheckVisitor typeChecker = new TypeCheckVisitor(scopeUtil, localResolver.types);
            parseTree.accept(typeChecker);

            // Check errors from TypeCheckVisitor
            if (!errorMessages.isEmpty()) {
                success = false;
            }

            // Run the interpreter if requested and type checking is successful
            if (interpret && success) {
                Interpreter interpreter = new Interpreter(scopeUtil);
                interpreter.interpret(parseTree);
            }

        } catch (Exception e) {
            success = false;
            errorMessages.add("Exception during compilation: " + e.getMessage());
            // e.printStackTrace(new PrintStream(errContent)); // Log full exception to errContent if needed
        } finally {
            System.setErr(originalErr);
            System.setOut(originalOut);
            CompilerLogger.setErrorListener(null); // Reset listener
        }

        return new CompilationResult(success && errorMessages.isEmpty(), errorMessages, outContent.toString());
    }
} 
