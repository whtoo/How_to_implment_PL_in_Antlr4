// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

    package org.teachfx.antlr4.ep14.compiler;

    import org.teachfx.antlr4.ep14.symtab.*;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MathExprParser}.
 */
public interface MathExprListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MathExprParser#compileUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompileUnit(MathExprParser.CompileUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathExprParser#compileUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompileUnit(MathExprParser.CompileUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link MathExprParser#varDelaration}.
	 * @param ctx the parse tree
	 */
	void enterVarDelaration(MathExprParser.VarDelarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathExprParser#varDelaration}.
	 * @param ctx the parse tree
	 */
	void exitVarDelaration(MathExprParser.VarDelarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MathExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(MathExprParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(MathExprParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MathExprParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(MathExprParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MathExprParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(MathExprParser.TypeContext ctx);
}