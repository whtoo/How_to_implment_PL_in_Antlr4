// Generated from VMAssembler.g4 by ANTLR 4.13.2

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link VMAssemblerParser}.
 */
public interface VMAssemblerListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(VMAssemblerParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(VMAssemblerParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#globals}.
	 * @param ctx the parse tree
	 */
	void enterGlobals(VMAssemblerParser.GlobalsContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#globals}.
	 * @param ctx the parse tree
	 */
	void exitGlobals(VMAssemblerParser.GlobalsContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#globalVariable}.
	 * @param ctx the parse tree
	 */
	void enterGlobalVariable(VMAssemblerParser.GlobalVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#globalVariable}.
	 * @param ctx the parse tree
	 */
	void exitGlobalVariable(VMAssemblerParser.GlobalVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclaration(VMAssemblerParser.FunctionDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#functionDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclaration(VMAssemblerParser.FunctionDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#instr}.
	 * @param ctx the parse tree
	 */
	void enterInstr(VMAssemblerParser.InstrContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#instr}.
	 * @param ctx the parse tree
	 */
	void exitInstr(VMAssemblerParser.InstrContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#temp}.
	 * @param ctx the parse tree
	 */
	void enterTemp(VMAssemblerParser.TempContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#temp}.
	 * @param ctx the parse tree
	 */
	void exitTemp(VMAssemblerParser.TempContext ctx);
	/**
	 * Enter a parse tree produced by {@link VMAssemblerParser#label}.
	 * @param ctx the parse tree
	 */
	void enterLabel(VMAssemblerParser.LabelContext ctx);
	/**
	 * Exit a parse tree produced by {@link VMAssemblerParser#label}.
	 * @param ctx the parse tree
	 */
	void exitLabel(VMAssemblerParser.LabelContext ctx);
}