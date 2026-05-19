grammar Cymbol;

@header {
package org.teachfx.antlr4.ep02.parser;
}

// ============================================
// EP02 — Tokens: Text→Words
//
// Lexical analysis: the compiler reads text and
// breaks it into tokens (words). Each token has
// a type and a text value.
//
// KEY INSIGHT: Keywords like 'if', 'return' are
// defined BEFORE the ID rule. ANTLR4 matches
// the first rule that wins. If ID came first,
// it would swallow keywords as identifiers!
//
// Grammar features:
//   - Explicit token definitions (MUL, DIV, etc.)
//   - Fragments (reusable sub-rules)
//   - Keywords (reserved words, placed before ID)
//   - Skipped tokens (whitespace, comments)
//
// Run: mvn exec:java -Dexec.args="src/main/resources/t.cymbol"
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

// --- Keywords (MUST come before ID!) ---
IF     : 'if' ;
ELSE   : 'else' ;
WHILE  : 'while' ;
RETURN : 'return' ;
INT_KW : 'int' ;
FLOAT_KW : 'float' ;
VOID   : 'void' ;
STRUCT : 'struct' ;

// --- Operators ---
MUL    : '*' ;
DIV    : '/' ;
ADD    : '+' ;
SUB    : '-' ;

// --- Identifiers: start with letter/underscore ---
// Uses fragments LETTER and DIGIT for clarity
ID     : LETTER (LETTER | DIGIT)* ;

// --- Integers ---
INT    : DIGIT+ ;

// --- Fragments (helper sub-rules, not standalone tokens) ---
fragment LETTER  : [a-zA-Z_] ;
fragment DIGIT   : [0-9] ;

// --- Skipped: whitespace ---
WS     : [ \t\r\n]+ -> skip ;

// --- Skipped: line comments // ... ---
LINE_COMMENT : '//' ~[\r\n]* -> skip ;

// --- Skipped: block comments /* ... */ ---
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
