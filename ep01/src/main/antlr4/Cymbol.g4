grammar Cymbol;

@header {
package org.teachfx.antlr4.ep01.parser;
}

// ============================================
// EP01 — Hello, Compiler!
//
// This is a tiny language. It can:
//   - Print expressions:  3 + 4 * 2;
//   - Assign variables:   x = 10;
//   - Use variables:      x + 5;
//
// Run: mvn exec:java -Dexec.args="src/main/resources/t.cymbol"
// ============================================

prog: stat+ ;

stat: expr ';'          # printExpr
    | ID '=' expr ';'   # assign
    ;

expr: expr op=('*'|'/') expr   # MulDiv
    | expr op=('+'|'-') expr   # AddSub
    | INT                       # int
    | ID                        # id
    | '(' expr ')'              # parens
    ;

// Tokens
MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
ID  : [a-zA-Z_][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
WS  : [ \t\r\n]+ -> skip ;

// Single-line comment
COMMENT : '//' ~[\r\n]* -> skip ;
