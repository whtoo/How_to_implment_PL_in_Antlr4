// Generated from LabeledExpr.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LabeledExprParser}.
 */
public interface LabeledExprListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LabeledExprParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(LabeledExprParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link LabeledExprParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(LabeledExprParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by {@link LabeledExprParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(LabeledExprParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link LabeledExprParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(LabeledExprParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by {@link LabeledExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(LabeledExprParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link LabeledExprParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(LabeledExprParser.ExprContext ctx);
}