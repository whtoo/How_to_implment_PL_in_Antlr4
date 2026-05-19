package org.teachfx.antlr4.ep03;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * TreePrinter — pretty-prints parse trees in indented form.
 *
 * The raw toStringTree() output from ANTLR4 is a lisp-style
 * string like "(prog (stat (expr ...)))". While useful,
 * it's hard to read for deep trees.
 *
 * This utility prints trees with indentation, making the
 * hierarchical structure immediately visible:
 *
 *   prog
 *    |-- stat : printExpr
 *    |    |-- expr : AddSub
 *    |         |-- expr : int
 *    |         |    \-- INT '3'
 *    |         |-- ADD '+'
 *    |         \-- expr : int
 *    |              \-- INT '4'
 *
 * Usage:
 *   TreePrinter.print(tree, parser);
 */
public class TreePrinter {

    /** Print a parse tree with indentation. */
    public static void print(ParseTree tree, org.antlr.v4.runtime.Parser parser) {
        printNode(tree, parser, "", true, true);
    }

    private static void printNode(ParseTree node, org.antlr.v4.runtime.Parser parser,
                                   String indent, boolean isLast, boolean isRoot) {
        if (node == null) return;

        String prefix = isRoot ? "" : (isLast ? " \\-- " : " |-- ");
        String label = getNodeLabel(node, parser);

        System.out.println(indent + prefix + label);

        if (node instanceof RuleNode ruleNode) {
            int childCount = ruleNode.getChildCount();
            String childIndent = indent + (isRoot ? "" : (isLast ? "     " : " |   "));
            for (int i = 0; i < childCount; i++) {
                printNode(ruleNode.getChild(i), parser, childIndent,
                         i == childCount - 1, false);
            }
        }
    }

    /** Build a human-readable label for a parse tree node. */
    private static String getNodeLabel(ParseTree node, org.antlr.v4.runtime.Parser parser) {
        if (node instanceof TerminalNode term) {
            String tokenName = parser.getVocabulary().getSymbolicName(term.getSymbol().getType());
            if (tokenName == null) {
                tokenName = parser.getVocabulary().getLiteralName(term.getSymbol().getType());
                if (tokenName != null) tokenName = tokenName.replace("'", "");
            }
            return tokenName + " '" + term.getText() + "'";
        }

        if (node instanceof ParserRuleContext ctx) {
            String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
            // For labeled alternatives, try to show the label
            return ruleName;
        }

        return node.getClass().getSimpleName();
    }

    // ============================================
    // Lisp-style tree printer
    // ============================================

    /** Print the parse tree in Lisp style (like toStringTree but with newlines). */
    public static void printLisp(ParseTree tree, org.antlr.v4.runtime.Parser parser) {
        System.out.println(formatLisp(tree, parser, 0));
    }

    private static String formatLisp(ParseTree node, org.antlr.v4.runtime.Parser parser, int depth) {
        String indent = "  ".repeat(depth);
        if (node instanceof TerminalNode term) {
            return indent + term.getText();
        }

        StringBuilder sb = new StringBuilder();
        if (node instanceof ParserRuleContext ctx) {
            String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
            sb.append(indent).append("(").append(ruleName);
        } else {
            sb.append(indent).append("(").append(node.getClass().getSimpleName());
        }

        int childCount = node.getChildCount();
        if (childCount > 0) {
            sb.append("\n");
            for (int i = 0; i < childCount; i++) {
                sb.append(formatLisp(node.getChild(i), parser, depth + 1));
                if (i < childCount - 1) sb.append("\n");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
