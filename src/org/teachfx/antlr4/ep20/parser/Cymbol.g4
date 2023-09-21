grammar Cymbol;
@header {
package org.teachfx.antlr4.ep20.parser;
}
file :   (functionDecl | varDecl)+ #compilationUnit ;

varDecl
    :   primaryType ID ('=' expr)? ';'
    ;

primaryType: 'Float' | 'Int' | 'Void' | 'Bool' | 'String' | 'Object';

functionDecl
    :   retType=primaryType funcName=ID '(' params=formalParameters? ')' blockDef=block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   primaryType ID
    ;

block:  '{' statetment* '}' ;    // possibly empty statement block

statetment:   block               #statBlock
    |   varDecl             #statVarDecl
    |   'return' expr? ';' #statReturn
    |   'if' '(' cond=expr ')' then=statetment ('else' elseDo=statetment)? #stateCondition
    |   'while' '(' cond=expr ')' then=statetment #stateWhile
    |   expr '=' expr ';' #statAssign // assignment 
    |   expr ';'       #stat // func call
    ;

expr:   expr '(' ( expr (',' expr)* )? ')' #exprFuncCall   // func call like f(), f(x), f(1,2)
    |   '-' expr         #exprUnary       // unary minus
    |   '!' expr         #exprUnary       // boolean not
    |   expr o=('*'|'/') expr    #exprBinary
    |   expr o=('+'|'-') expr #exprBinary
    |   expr o=('=='|'!='|'>'|'>='|'<'|'<=') expr #exprBinary
    |   primary #exprPrimary
    |   '(' expr ')'         #exprGroup
    ;
primary:    ID                   #primaryID   // variable reference
    |       INT                  #primaryINT
    |       FLOAT                #primaryFLOAT
    |       CHAR                 #primaryCHAR
    |       STRING               #primarySTRING
    |       BOOLEAN              #primaryBOOL
    ;

ID  :   LETTER (LETTER | [0-9])* ;
BOOLEAN: 'true' | 'false';
NULL : 'null';

fragment
LETTER : [a-zA-Z] ;

INT :   [0-9]+ ;
FLOAT : INT? '.' INT ;
WS  :   [ \t\n\r]+ -> skip ;
CHAR :  '\'' . '\'' ;
STRING: '"' ~( '"' | '\r' | '\n' )* '"'; 

SLCOMMENT
    :   '//' .*? '\n' -> skip
    ;

COMMNET
    : '/*' .*? '\n' -> skip
    ;