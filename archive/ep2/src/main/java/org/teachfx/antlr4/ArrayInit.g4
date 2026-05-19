grammar ArrayInit;

@header {
package org.teachfx.antlr4.ep2.parser;
}

array : '{' value (',' value)* '}';

value : array
        | INT;

INT : [0-9]+;
WS : [ \t\r\n]+ -> skip;