package org.teachfx.antlr4.ep19.pipeline;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.pass.LocalDefine;
import org.teachfx.antlr4.ep19.pass.LocalResolver;
import org.teachfx.antlr4.ep19.pass.TypeCheckVisitor;

import java.io.Serializable;

/**
 * Represents the result of a compilation process.
 * This class encapsulates all the information needed to execute the compiled code later.
 */
public class CompilationResult implements Serializable {
    
    private final ParseTree parseTree;
    private final ScopeUtil scopeUtil;
    private final LocalDefine localDefine;
    private final LocalResolver localResolver;
    private final TypeCheckVisitor typeChecker;
    private final boolean successful;
    private final String errorMessage;
    
    /**
     * Creates a successful compilation result.
     * 
     * @param parseTree The parse tree
     * @param scopeUtil The scope utility
     * @param localDefine The local define visitor
     * @param localResolver The local resolver visitor
     * @param typeChecker The type checker visitor
     */
    public CompilationResult(ParseTree parseTree, ScopeUtil scopeUtil, LocalDefine localDefine, 
                            LocalResolver localResolver, TypeCheckVisitor typeChecker) {
        this.parseTree = parseTree;
        this.scopeUtil = scopeUtil;
        this.localDefine = localDefine;
        this.localResolver = localResolver;
        this.typeChecker = typeChecker;
        this.successful = true;
        this.errorMessage = null;
    }
    
    /**
     * Creates a failed compilation result.
     * 
     * @param errorMessage The error message
     */
    public CompilationResult(String errorMessage) {
        this.parseTree = null;
        this.scopeUtil = null;
        this.localDefine = null;
        this.localResolver = null;
        this.typeChecker = null;
        this.successful = false;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Returns the parse tree.
     * 
     * @return The parse tree
     */
    public ParseTree getParseTree() {
        return parseTree;
    }
    
    /**
     * Returns the scope utility.
     * 
     * @return The scope utility
     */
    public ScopeUtil getScopeUtil() {
        return scopeUtil;
    }
    
    /**
     * Returns the local define visitor.
     * 
     * @return The local define visitor
     */
    public LocalDefine getLocalDefine() {
        return localDefine;
    }
    
    /**
     * Returns the local resolver visitor.
     * 
     * @return The local resolver visitor
     */
    public LocalResolver getLocalResolver() {
        return localResolver;
    }
    
    /**
     * Returns the type checker visitor.
     * 
     * @return The type checker visitor
     */
    public TypeCheckVisitor getTypeChecker() {
        return typeChecker;
    }
    
    /**
     * Returns whether the compilation was successful.
     * 
     * @return True if the compilation was successful, false otherwise
     */
    public boolean isSuccessful() {
        return successful;
    }
    
    /**
     * Returns the error message if the compilation failed.
     * 
     * @return The error message, or null if the compilation was successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}