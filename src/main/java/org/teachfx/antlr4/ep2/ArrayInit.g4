grammar ArrayInit;

array : '{' value (',' value)* '}';

value : array
        | INT;

INT : [0-9]+;
WS : [ \t\r\n]+ -> skip;