// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep20.parser;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CymbolParser}.
 */
public interface CymbolListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by the {@code compilationUnit}
	 * labeled alternative in {@link CymbolParser#file}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(CymbolParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compilationUnit}
	 * labeled alternative in {@link CymbolParser#file}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(CymbolParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(CymbolParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(CymbolParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#primaryType}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryType(CymbolParser.PrimaryTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#primaryType}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryType(CymbolParser.PrimaryTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(CymbolParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(CymbolParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(CymbolParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(CymbolParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(CymbolParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(CymbolParser.FormalParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link CymbolParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(CymbolParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link CymbolParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(CymbolParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code statVarDecl}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStatVarDecl(CymbolParser.StatVarDeclContext ctx);
	/**
	 * Exit a parse tree produced by the {@code statVarDecl}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStatVarDecl(CymbolParser.StatVarDeclContext ctx);
	/**
	 * Enter a parse tree produced by the {@code statReturn}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStatReturn(CymbolParser.StatReturnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code statReturn}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStatReturn(CymbolParser.StatReturnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stateCondition}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStateCondition(CymbolParser.StateConditionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stateCondition}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStateCondition(CymbolParser.StateConditionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stateWhile}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStateWhile(CymbolParser.StateWhileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stateWhile}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStateWhile(CymbolParser.StateWhileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code visitBreak}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterVisitBreak(CymbolParser.VisitBreakContext ctx);
	/**
	 * Exit a parse tree produced by the {@code visitBreak}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitVisitBreak(CymbolParser.VisitBreakContext ctx);
	/**
	 * Enter a parse tree produced by the {@code visitContinue}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterVisitContinue(CymbolParser.VisitContinueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code visitContinue}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitVisitContinue(CymbolParser.VisitContinueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code statAssign}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStatAssign(CymbolParser.StatAssignContext ctx);
	/**
	 * Exit a parse tree produced by the {@code statAssign}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStatAssign(CymbolParser.StatAssignContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprStat}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterExprStat(CymbolParser.ExprStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprStat}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitExprStat(CymbolParser.ExprStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code statBlock}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void enterStatBlock(CymbolParser.StatBlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code statBlock}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 */
	void exitStatBlock(CymbolParser.StatBlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprBinary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprBinary(CymbolParser.ExprBinaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprBinary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprBinary(CymbolParser.ExprBinaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprGroup}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprGroup(CymbolParser.ExprGroupContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprGroup}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprGroup(CymbolParser.ExprGroupContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprUnary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprUnary(CymbolParser.ExprUnaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprUnary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprUnary(CymbolParser.ExprUnaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprPrimary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprPrimary(CymbolParser.ExprPrimaryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprPrimary}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprPrimary(CymbolParser.ExprPrimaryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprFuncCall}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprFuncCall(CymbolParser.ExprFuncCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprFuncCall}
	 * labeled alternative in {@link CymbolParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprFuncCall(CymbolParser.ExprFuncCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryID}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryID(CymbolParser.PrimaryIDContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryID}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryID(CymbolParser.PrimaryIDContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryINT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryINT(CymbolParser.PrimaryINTContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryINT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryINT(CymbolParser.PrimaryINTContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryFLOAT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryFLOAT(CymbolParser.PrimaryFLOATContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryFLOAT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryFLOAT(CymbolParser.PrimaryFLOATContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryCHAR}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryCHAR(CymbolParser.PrimaryCHARContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryCHAR}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryCHAR(CymbolParser.PrimaryCHARContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primarySTRING}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimarySTRING(CymbolParser.PrimarySTRINGContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primarySTRING}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimarySTRING(CymbolParser.PrimarySTRINGContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryBOOL}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryBOOL(CymbolParser.PrimaryBOOLContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryBOOL}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryBOOL(CymbolParser.PrimaryBOOLContext ctx);
}