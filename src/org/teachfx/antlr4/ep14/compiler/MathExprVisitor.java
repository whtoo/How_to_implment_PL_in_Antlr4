// Generated from ./compiler/MathExpr.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep14.compiler;
import org.teachfx.antlr4.ep14.symtab.*;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MathExprParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MathExprVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MathExprParser#compileUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompileUnit(MathExprParser.CompileUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link MathExprParser#varDelaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDelaration(MathExprParser.VarDelarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link MathExprParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(MathExprParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link MathExprParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(MathExprParser.TypeContext ctx);
}