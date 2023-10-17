grammar VecMath;

statlist : stat+;

stat  : assign ';'
    | varSlot ';';

assign : ID op='=' varSlot;
varSlot  : varSlot op=('*' | '/') varSlot
    | varSlot op=('+' | '-') varSlot
    | NUMBER
    | ID
    | '(' varSlot ')';

NUMBER : [0-9] | [1-9][0-9]+;
NEWLINE : '\n'+ -> skip ;
WS: [ \t\r]+ -> skip;
ID: [a-zA-Z][0-9a-zA-Z]*;
