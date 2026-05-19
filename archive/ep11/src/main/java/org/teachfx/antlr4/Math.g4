grammar Math;

compileUnit
    :   varSlot EOF
    ;

varSlot
    :   '(' varSlot ')'                         # parensExpr
    |   op=('+'|'-') varSlot                    # unaryExpr
    |   left=varSlot op=('*'|'/') right=varSlot    # infixExpr
    |   left=varSlot op=('+'|'-') right=varSlot    # infixExpr
    |   value=NUM                            # numberExpr
    ;

OP_ADD: '+';
OP_SUB: '-';
OP_MUL: '*';
OP_DIV: '/';

NUM :   [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
ID  :   [a-zA-Z]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);