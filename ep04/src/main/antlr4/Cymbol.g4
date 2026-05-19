grammar Cymbol;

@header {
package org.teachfx.antlr4.ep04.parser;
}

// ============================================
// EP04 — AST: Meaningful Trees
//
// Same grammar as EP03, but now we build an AST
// (Abstract Syntax Tree) instead of using the raw
// parse tree.
//
// Parse tree has punctuation and rule names.
// AST has only what matters — the semantic
// structure of the program.
//
// Example:
//   Parse tree: (stat (expr (expr 3) + (expr 4)) ;)
//   AST:        Add(Int(3), Int(4))
//
// Run: mvn exec:java -pl ep04 -Dexec.args="src/main/resources/t.cymbol"
// ============================================

prog: stat+ ;

stat: expr ';'          # printExpr
    | ID '=' expr ';'   # assign
    ;

expr: expr op=(MUL|DIV) expr   # MulDiv
    | expr op=(ADD|SUB) expr   # AddSub
    | INT                       # int
    | ID                        # id
    | '(' expr ')'              # parens
    ;

MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
ID  : [a-zA-Z_][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;

LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
