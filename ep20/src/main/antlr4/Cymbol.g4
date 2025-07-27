grammar Cymbol;

@header {
package org.teachfx.antlr4.ep20.parser;
}

file :   (functionDecl | varDecl | typedefDecl | structDecl)+ #compilationUnit ;

varDecl
    :   type ID ('[' expr ']')? ('=' (expr | arrayInitializer))? ';'
    ;

typedefDecl
    :   'typedef' type ID ';'
    ;

structDecl
    :   'struct' ID '{' structMember* '}' ';'
    ;

structMember
    :   type ID ('[' expr ']')? ';'
    ;

type: primaryType | ID;
primaryType: 'float' | 'int' | 'void' | 'bool' | 'string' | 'object';

functionDecl
    :   retType=primaryType funcName=ID '(' params=formalParameters? ')' blockDef=block // "void f(int x) {...}"
    ;
formalParameters
    :   formalParameter (',' formalParameter)*
    ;
formalParameter
    :   type ID ('[' expr ']')?  // 支持带大小的数组参数，如 int arr[3] 或不带大小的数组参数，如 int arr[]
    ;

block:  '{' stmts=statetment* '}' ;    // possibly empty statement block

statetment:   varDecl             #statVarDecl
    |   'return' expr? ';' #statReturn
    |   'if' '(' cond=expr ')' then=statetment ('else' elseDo=statetment)? #stateCondition
    |   'while' '(' cond=expr ')' then=statetment #stateWhile
    |   'break' ';' #visitBreak
    |   'continue' ';' #visitContinue
    |   expr '=' expr ';' #statAssign // assignment
    |   expr ';'       #exprStat // func call
    |   block               #statBlock
    ;

expr:   callFunc=expr '(' ( expr (',' expr)* )? ')' #exprFuncCall   // func call like f(), f(x), f(1,2)
    |   expr '[' expr ']' #exprArrayAccess    // array access: arr[index]
    |   expr '.' ID #exprFieldAccess          // struct field access: obj.field
    |   '(' primaryType ')' expr #exprCast         // type cast: (int)floatVal
    |   o='-' expr         #exprUnary       // unary minus
    |   o='!' expr         #exprUnary       // boolean not
    |   expr o=('*'|'/'|'%') expr    #exprBinary
    |   expr o=('+'|'-') expr #exprBinary
    |   expr o=('=='|'!='|'>'|'>='|'<'|'<=') expr #exprBinary
    |   expr o='&&' expr #exprLogicalAnd
    |   primary #exprPrimary
    |   '(' expr ')'         #exprGroup
    ;

arrayInitializer
    :   '{' expr (',' expr)* '}'
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