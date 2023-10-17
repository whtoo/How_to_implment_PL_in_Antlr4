grammar Cymbol;

@header {
package org.teachfx.antlr4.ep21.parser;
}

file :   (functionDecl | varDecl)+ #compilationUnit ;

varDecl
    :   primaryType ID ('=' varSlot)? ';'
    ;

primaryType: 'float' | 'int' | 'void' | 'bool' | 'string' | 'object';

functionDecl
    :   retType=primaryType funcName=ID '(' params=formalParameters? ')' blockDef=block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   primaryType ID
    ;

block:  '{' stmts=statetment* '}' ;    // possibly empty statement block

statetment:   varDecl             #statVarDecl
    |   'return' varSlot? ';' #statReturn
    |   'if' '(' cond=varSlot ')' then=statetment ('else' elseDo=statetment)? #stateCondition
    |   'while' '(' cond=varSlot ')' then=statetment #stateWhile
    |   'break' ';' #visitBreak
    |   'continue' ';' #visitContinue
    |   varSlot '=' varSlot ';' #statAssign // assignment
    |   varSlot ';'       #exprStat // func call
    |   block               #statBlock
    ;

varSlot:   callFunc=varSlot '(' ( varSlot (',' varSlot)* )? ')' #exprFuncCall   // func call like f(), f(x), f(1,2)
    |   o='-' varSlot         #exprUnary       // unary minus
    |   o='!' varSlot         #exprUnary       // boolean not
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

COMMNET
    : '/*' .*? '*/' '\n' -> skip
    ;