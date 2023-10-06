// Generated from LibExpr.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep3;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LibExprParser}.
 */
public interface LibExprListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link LibExprParser#prog}.
     *
     * @param ctx the parse tree
     */
    void enterProg(LibExprParser.ProgContext ctx);

    /**
     * Exit a parse tree produced by {@link LibExprParser#prog}.
     *
     * @param ctx the parse tree
     */
    void exitProg(LibExprParser.ProgContext ctx);

    /**
     * Enter a parse tree produced by {@link LibExprParser#stat}.
     *
     * @param ctx the parse tree
     */
    void enterStat(LibExprParser.StatContext ctx);

    /**
     * Exit a parse tree produced by {@link LibExprParser#stat}.
     *
     * @param ctx the parse tree
     */
    void exitStat(LibExprParser.StatContext ctx);

    /**
     * Enter a parse tree produced by {@link LibExprParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterExpr(LibExprParser.ExprContext ctx);

    /**
     * Exit a parse tree produced by {@link LibExprParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitExpr(LibExprParser.ExprContext ctx);
}