// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT
package src.main.java.org.teachfx.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LibExprParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LibExprVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LibExprParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(LibExprParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by {@link LibExprParser#stat}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStat(LibExprParser.StatContext ctx);
	/**
	 * Visit a parse tree produced by {@link LibExprParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(LibExprParser.ExprContext ctx);
}