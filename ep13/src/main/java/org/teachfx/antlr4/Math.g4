grammar Math;

@header {
package org.teachfx.antlr4.ep13;
}

tokens {
    
}
compileUnit
    :   varSlot EOF
    |   assign EOF
    ;

funcall : name=ID '(' params=parameters ')';

parameters : parameter (',' parameter)* | ;

parameter : varSlot;

varSlot
    :   '(' varSlot ')'                         # parensExpr
    |   op=('+'|'-') varSlot                    # unaryExpr
    |   left=varSlot op=('*'|'/') right=varSlot    # infixExpr
    |   left=varSlot op=('+'|'-') right=varSlot    # infixExpr
    |   var=ID                               # varExpr
    |   value=NUM                            # numberExpr
    ;
assign :  name=ID '=' value=varSlot EOF                     # assignExpr
    ;

OP_ADD: '+';
OP_SUB: '-';
OP_MUL: '*';
OP_DIV: '/';

NUM :   [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
ID  :   [a-zA-Z]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);