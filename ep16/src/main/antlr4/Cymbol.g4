grammar Cymbol;
@header {
package org.teachfx.antlr4.ep16.parser;
}
file :   (functionDecl | varDecl | statetment)+ #compilationUnit ;

varDecl
    :   type ID ('=' varSlot)? ';'
    ;
type:  primaryType | ID  ; // pre-defined types
primaryType: 'float' | 'int' | 'void';
functionDecl
    :   retType=type funcName=ID '(' params=formalParameters? ')' blockDef=block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   type ID
    ;

block:  '{' statetment* '}' ;    // possibly empty statement block

statetment:   block               #statBlock
    |   varDecl             #statVarDecl
    |   'return' varSlot? ';' #statReturn
    |   'if' '(' cond=varSlot ')' then=statetment ('else' elseDo=statetment)? #stateCondition
    |   'while' '(' cond=varSlot ')' then=statetment #stateWhile
    |   varSlot '=' varSlot ';' #statAssign // assignment
    |   varSlot ';'       #stat // func call
    ;

varSlot:   varSlot '(' ( varSlot (',' varSlot)* )? ')' #exprFuncCall   // func call like f(), f(x), f(1,2)
    |   '-' varSlot         #exprUnary       // unary minus
    |   '!' varSlot         #exprUnary       // boolean not
    |   varSlot o=('*'|'/') varSlot    #exprBinary
    |   varSlot o=('+'|'-') varSlot #exprBinary
    |   varSlot o=('=='|'!='|'>'|'>='|'<'|'<=') varSlot #exprBinary
    |   primary #exprPrimary
    |   '(' varSlot ')'         #exprGroup
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
