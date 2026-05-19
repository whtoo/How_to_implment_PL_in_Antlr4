grammar Cymbol;

@header {
package org.teachfx.antlr4.ep05.parser;
}

// ============================================
// EP05 — Symbols: Names Have Meaning
//
// A language with functions, variables, blocks.
// The compiler walks the AST, collects all symbols,
// and prints a formatted symbol table.
// ============================================

file : (functionDecl | varDecl | statement)+ ;

varDecl
    : type ID ('=' expr)? ';'
    ;

type: 'float' | 'int' | 'void' ;

functionDecl
    : retType=type funcName=ID '(' params=formalParameters? ')' blockDef=block
    ;

formalParameters
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : type ID
    ;

block: '{' statement* '}' ;

statement: block                                   # statBlock
    | varDecl                                      # statVarDecl
    | 'return' expr? ';'                           # statReturn
    | 'if' '(' cond=expr ')' then=statement
          ('else' elseDo=statement)?               # stateCondition
    | 'while' '(' cond=expr ')' then=statement     # stateWhile
    | expr '=' expr ';'                            # statAssign
    | expr ';'                                     # stat
    ;

expr: ID '(' ( expr (',' expr)* )? ')'             # exprFuncCall
    | '-' expr                                     # exprUnary
    | '!' expr                                     # exprUnary
    | expr o=('*'|'/') expr                        # exprBinary
    | expr o=('+'|'-') expr                        # exprBinary
    | expr o=('=='|'!='|'>'|'>='|'<'|'<=') expr   # exprBinary
    | primary                                      # exprPrimary
    | '(' expr ')'                                 # exprGroup
    ;

primary: ID       # primaryID
    | INT          # primaryINT
    | FLOAT        # primaryFLOAT
    ;

ID  : [_a-zA-Z][_a-zA-Z0-9]* ;
INT : [0-9]+ ;
FLOAT : INT? '.' INT ;
WS  : [ \t\n\r]+ -> skip ;

SLCOMMENT : '//' .*? ('\n'|'\r'|'\r\n'|EOF) -> skip ;
COMMENT   : '/*' .*? '*/' -> skip ;
