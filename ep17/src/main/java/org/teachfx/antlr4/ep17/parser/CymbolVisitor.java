// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep17.parser;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CymbolParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CymbolVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by the {@code compilationUnit}
	 * labeled alternative in {@link CymbolParser#file}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(CymbolParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#varDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDecl(CymbolParser.VarDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(CymbolParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#primaryType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryType(CymbolParser.PrimaryTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#functionDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDecl(CymbolParser.FunctionDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#formalParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameters(CymbolParser.FormalParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#formalParameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFormalParameter(CymbolParser.FormalParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link CymbolParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(CymbolParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statBlock}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatBlock(CymbolParser.StatBlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statVarDecl}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatVarDecl(CymbolParser.StatVarDeclContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statReturn}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatReturn(CymbolParser.StatReturnContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stateCondition}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStateCondition(CymbolParser.StateConditionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stateWhile}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStateWhile(CymbolParser.StateWhileContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statAssign}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatAssign(CymbolParser.StatAssignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stat}
	 * labeled alternative in {@link CymbolParser#statetment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(CymbolParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprBinary}
	 * labeled alternative in {@link CymbolParser#varSlot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBinary(CymbolParser.ExprBinaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprGroup}
	 * labeled alternative in {@link CymbolParser#varSlot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprGroup(CymbolParser.ExprGroupContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprUnary}
	 * labeled alternative in {@link CymbolParser#varSlot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprUnary(CymbolParser.ExprUnaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprPrimary}
	 * labeled alternative in {@link CymbolParser#varSlot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprPrimary(CymbolParser.ExprPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprFuncCall}
	 * labeled alternative in {@link CymbolParser#varSlot}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprFuncCall(CymbolParser.ExprFuncCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryID}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryID(CymbolParser.PrimaryIDContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryINT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryINT(CymbolParser.PrimaryINTContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryFLOAT}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryFLOAT(CymbolParser.PrimaryFLOATContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryCHAR}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryCHAR(CymbolParser.PrimaryCHARContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primarySTRING}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimarySTRING(CymbolParser.PrimarySTRINGContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryBOOL}
	 * labeled alternative in {@link CymbolParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryBOOL(CymbolParser.PrimaryBOOLContext ctx);
}