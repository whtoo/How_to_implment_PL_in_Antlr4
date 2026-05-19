grammar Cymbol;

@header {
package org.teachfx.antlr4.ep03.parser;
}

// ============================================
// EP03 — Grammar: Words→Trees
//
// Context-free grammar defines how tokens
// combine into valid sentences (statements).
// The parser builds a parse tree — a concrete
// representation showing exactly how the input
// matches the grammar rules.
//
// KEY INSIGHTS:
//   1. Operator precedence: MulDiv rules appear
//      BEFORE AddSub — lower in the tree = higher
//      precedence (evaluated first).
//   2. Recursion: expr calls itself, enabling
//      arbitrarily nested expressions.
//   3. Labeled alternatives (#Label) give names
//      to alternative branches, enabling targeted
//      visitor methods.
//   4. Left-recursion is OK in ANTLR4!
//
// Run: mvn exec:java -pl ep03 -Dexec.args="src/main/resources/t.cymbol"
// ============================================

prog: stat+ ;

stat: expr ';'          # printExpr
    | ID '=' expr ';'   # assign
    ;

// --- Expression grammar with precedence ---
// Order matters! MulDiv is listed first, so it
// binds tighter (lower in the parse tree).
// This gives us standard arithmetic precedence:
//   * and / bind tighter than + and -
expr: expr op=(MUL|DIV) expr   # MulDiv       // precedence 2 (highest)
    | expr op=(ADD|SUB) expr   # AddSub       // precedence 1
    | INT                       # int          // atomic
    | ID                        # id           // atomic
    | '(' expr ')'              # parens       // override precedence
    ;

// --- Tokens ---
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
ID  : [a-zA-Z_][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;

// Comments
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
