grammar Cymbol;
@header {
package org.teachfx.antlr4.ep15.parser;
}
file :   (functionDecl | varDecl)+ #compilationUnit ;

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

stat:   block               
    |   varDecl             
    |   'if' expr 'then' stat ('else' stat)? 
    |   'return' expr? ';' 
    |   expr '=' expr ';'  // assignment 
    |   expr ';'         // func call
    ;

expr:   ID '(' exprLst? ')' #exprFunCall   // func call like f(), f(x), f(1,2)
    |   '-' expr         #exprUnary       // unary minus
    |   '!' expr         #exprUnary       // boolean not
    |   expr ('*'|'/') expr    #exprBinary
    |   expr ('+'|'-') expr #exprBinary
    |   expr '==' expr #exprBinary         // equality comparison (lowest priority op)
    |   primary #exprPrimary
    |   '(' expr ')'         #exprGroup
    ;
primary:    ID                   #primaryID   // variable reference
    |       INT                  #primaryINT
    |       FLOAT                #primaryFLOAT
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
