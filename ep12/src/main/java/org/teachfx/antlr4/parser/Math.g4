grammar Math;
@header {
package org.teachfx.antlr4.ep12.parser;
}
compileUnit
    :   expr EOF                            
    |   assign EOF
    ;

expr
    :   '(' expr ')'                         # parensExpr
    |   op=('+'|'-') expr                    # unaryExpr
    |   left=expr op=('*'|'/') right=expr    # infixExpr
    |   left=expr op=('+'|'-') right=expr    # infixExpr
    |   var=ID                               # varExpr
    |   value=NUM                            # numberExpr
    ;
assign :  name=ID '=' value=expr EOF                     # assignExpr
    ;

OP_ADD: '+';
OP_SUB: '-';
OP_MUL: '*';
OP_DIV: '/';

NUM :   [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
ID  :   [a-zA-Z]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);