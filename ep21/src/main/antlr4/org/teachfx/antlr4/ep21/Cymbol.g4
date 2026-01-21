grammar Cymbol;

file :   (functionDecl | varDecl)+ #compilationUnit ;

varDecl
    :   (type '[' expr ']' ID | type ID ('[' expr ']')?) ('=' (expr | arrayInitializer))? ';'
    ;

arrayInitializer
    :   '{' expr (',' expr)* '}'
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
    :   (type '[' expr ']' ID | type ID ('[' expr ']')?)  // 支持两种数组参数格式：int[10] arr 和 int arr[10]
    ;

block:  '{' stmts=statement* '}' ;    // possibly empty statement block

statement:   varDecl             #statVarDecl
    |   'return' expr? ';' #statReturn
    |   'if' '(' cond=expr ')' then=statement ('else' elseDo=statement)? #stateCondition
    |   'while' '(' cond=expr ')' then=statement #stateWhile
    |   'break' ';' #visitBreak
    |   'continue' ';' #visitContinue
    |   expr '=' expr ';' #statAssign // assignment
    |   expr ';'       #exprStat // func call
    |   block               #statBlock
    ;

expr:   callFunc=expr '(' ( expr (',' expr)* )? ')' #exprFuncCall   // func call like f(), f(x), f(1,2)
    |   expr '[' expr ']' #exprArrayAccess    // array access: arr[index]
    |   o='-' expr         #exprUnary       // unary minus
    |   o='!' expr         #exprUnary       // boolean not
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

ID  :   LETTER (LETTER | [0-9] | '_')* ;
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

COMMENT
    : '/*' .*? '*/' '\n' -> skip
    ;