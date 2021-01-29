grammar Cymbol;
@header {
package org.teachfx.antlr4.ep15.parser;
}
file:   (functionDecl | varDecl)+ ;

varDecl
    :   type ID ('=' expr)? ';'
    ;
type:   'float' | 'int' | 'void' ; // pre-defined types

functionDecl
    :   type ID '(' formalParameters? ')' block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   type ID
    ;

block:  '{' stat* '}' ;    // possibly empty statement block

stat:   block               #statBlock 
    |   varDecl             #statVarDecl
    |   'if' expr 'then' stat ('else' stat)? #statIfElese
    |   'return' expr? ';' # statReturn
    |   expr '=' expr ';' #statAssign // assignment 
    |   expr ';'         #statExpr // func call
    ;

expr:   ID '(' exprLst? ')' #exprCall   // func call like f(), f(x), f(1,2)
    |   '-' expr         #exprUnary       // unary minus
    |   '!' expr         #exprUnary       // boolean not
    |   expr ('*'|'/') expr    #exprBinary
    |   expr ('+'|'-') expr #exprBinary
    |   expr '==' expr #exprBinary         // equality comparison (lowest priority op)
    |   ID                   #exprID   // variable reference
    |   INT                  #exprINT
    |   FLOAT #exprFLOAT
    |   '(' expr ')'         #exprGroup
    ;
exprLst : expr (',' expr)* #exprList ;   // arg list

ID  :   LETTER (LETTER | [0-9])* ;
fragment
LETTER : [a-zA-Z] ;

INT :   [0-9]+ ;
FLOAT : INT? '.' INT ;
WS  :   [ \t\n\r]+ -> skip ;

SLCOMMENT
    :   '//' .*? '\n' -> skip
    ;
