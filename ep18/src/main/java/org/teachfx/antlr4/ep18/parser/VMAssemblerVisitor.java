// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link VMAssemblerParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface VMAssemblerVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(VMAssemblerParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#globals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGlobals(VMAssemblerParser.GlobalsContext ctx);
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(VMAssemblerParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#instr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInstr(VMAssemblerParser.InstrContext ctx);
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#operand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperand(VMAssemblerParser.OperandContext ctx);
	/**
	 * Visit a parse tree produced by {@link VMAssemblerParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(VMAssemblerParser.LabelContext ctx);
}