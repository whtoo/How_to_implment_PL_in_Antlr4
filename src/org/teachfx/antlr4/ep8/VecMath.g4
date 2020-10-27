grammar VecMath;

statlist : stat+;

stat  : assign ';'
    | expr ';';

assign : ID op='=' expr;
expr  : expr op=('*' | '/') expr
    | expr op=('+' | '-') expr 
    | NUMBER
    | ID
    | '(' expr ')';

NUMBER : [0-9] | [1-9][0-9]+;
NEWLINE : '\n'+ -> skip ;
WS: [ \t\r]+ -> skip;
ID: [a-zA-Z][0-9a-zA-Z]*;
