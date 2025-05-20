grammar Cymbol;
@header {
package org.teachfx.antlr4.ep19.parser;
}

file :   (structDecl | typedefDecl | functionDecl | varDecl | statement)+ ;

structDecl : 'struct' ID '{' structMemeber+ '}'
;

typedefDecl : 'typedef' type ID ';' ;

structMemeber
     :   type ID ';'
     |   structDecl
     ;

varDecl
    :   type ID ('=' expr)? ';'
    ;
type:  primaryType | ID ; // pre-defined types
primaryType: 'float' | 'int' | 'void' | 'bool' | 'String' | 'Object';
functionDecl
    :   retType=type funcName=ID '(' params=formalParameters? ')' blockDef=block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   type ID
    ;

block:  '{' statement* '}' ;    // possibly empty statement block

statement:   block               #statBlock
    |   structDecl               #statStructDecl
    |   typedefDecl              #statTypedefDecl
    |   varDecl                  #statVarDecl
    |   'return' expr? ';'       #statReturn
    |   'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)? #stateCondition
    |   'while' '(' cond=expr ')' then=statement #stateWhile
    |   expr '=' expr ';'        #statAssign // assignment
    |   expr ';'                 #stat // func call
    ;

expr
  : ID '(' ( expr (',' expr)* )? ')'                     # exprFuncCall // 内置函数调用和普通函数调用
  | expr o='.' expr                                     # exprStructFieldAccess
  | '-' expr                                            # exprUnary
  | '!' expr                                            # exprUnary
  | expr o=('*' | '/') expr                             # exprBinary
  | expr o=('+' | '-') expr                             # exprBinary
  | expr o=('!=' | '==' | '<' | '>' | '<=' | '>=') expr # exprBinary
  | 'new' expr '(' (expr (',' expr)* )? ')'             # exprNew // new Point()
  | primary                                             # exprPrimary 
  | '(' expr ')'                                        # exprGroup
  ;

primary:    ID                   #primaryID   // variable reference
    |       INT                  #primaryINT
    |       FLOAT                #primaryFLOAT
    |       CHAR                 #primaryCHAR
    |       STRING               #primarySTRING
    |       BOOLEAN              #primaryBOOL
    ;

ID  :   [_a-zA-Z] [_a-zA-Z0-9]* ;
BOOLEAN: 'true' | 'false';
NULL : 'null';

INT :   [0-9]+ ;
FLOAT : INT? '.' INT ;
WS  :   [ \t\n\r]+ -> skip ;
CHAR :  '\'' . '\'' ;
STRING: '"' ~( '"' | '\r' | '\n' )* '"'; 

SLCOMMENT
    :   '//' .*? ('\n'|'\r'|'\r\n' | EOF) -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;