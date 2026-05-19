grammar Cymbol;

@header {
package org.teachfx.antlr4.ep11.parser;
}

file : (functionDecl | varDecl | typedefDecl | structDecl)+ #compilationUnit ;

varDecl
    : type ID ('[' expr ']')? ('=' (expr | arrayInitializer))? ';'
    ;

typedefDecl
    : 'typedef' type ID ';'
    ;

structDecl
    : 'struct' ID '{' structMember* '}'
    ;

structMember
    : type ID ('[' expr ']')? ';'
    ;

type: primaryType | ID;
primaryType: 'float' | 'int' | 'void' | 'bool' | 'string' | 'object';

functionDecl
    : retType=primaryType funcName=ID '(' params=formalParameters? ')' blockDef=block
    ;

formalParameters
    : formalParameter (',' formalParameter)*
    ;

formalParameter
    : type ID ('[' expr ']')?
    ;

block: '{' stmts=statement* '}' ;

statement: varDecl             #statVarDecl
    | 'return' expr? ';'       #statReturn
    | 'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)? #stateCondition
    | 'while' '(' cond=expr ')' then=statement #stateWhile
    | 'break' ';'              #visitBreak
    | 'continue' ';'           #visitContinue
    | expr '=' expr ';'        #statAssign
    | expr ';'                 #exprStat
    | block                    #statBlock
    ;

expr: callFunc=expr '(' ( expr (',' expr)* )? ')' #exprFuncCall
    | expr '[' expr ']'        #exprArrayAccess
    | expr '.' ID              #exprFieldAccess
    | o='-' expr               #exprUnary
    | o='!' expr               #exprUnary
    | expr o=('*'|'/'|'%') expr   #exprBinary
    | expr o=('+'|'-') expr       #exprBinary
    | expr o=('=='|'!='|'>'|'>='|'<'|'<=') expr #exprBinary
    | expr o='&&' expr         #exprLogicalAnd
    | '(' primaryType ')' expr #exprCast
    | primary                  #exprPrimary
    | '(' expr ')'             #exprGroup
    ;

arrayInitializer
    : '{' expr (',' expr)* '}'
    ;

primary: ID      #primaryID
    | INT         #primaryINT
    | FLOAT       #primaryFLOAT
    | CHAR        #primaryCHAR
    | STRING      #primarySTRING
    | BOOLEAN     #primaryBOOL
    ;

ID  : LETTER (LETTER | [0-9])* ;
BOOLEAN: 'true' | 'false';
NULL : 'null';
NEW : 'new';

fragment
LETTER : [a-zA-Z] ;

INT : [0-9]+ ;
FLOAT : INT? '.' INT ;
WS  : [ \t\n\r]+ -> skip ;
CHAR : '\'' . '\'' ;
STRING: '"' ~( '"' | '\r' | '\n' )* '"';

SLCOMMENT : '//' .*? '\n' -> skip ;
COMMENT   : '/*' .*? '*/' '\n'? -> skip ;
