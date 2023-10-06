grammar JSON;

// Start rule
json : (object | array)  ;
object : '{}'  | '{' pair ( ',' pair)* '}';
pair : STRING ':' value { System.out.println("catch pair"); };
value : STRING 
    |   NUMBER
    |   object
    |   array
    |   'true'
    |   'false'
    |   'null';
array : '[' value (',' value)* ']'
    |   '[' ']';

STRING : '"' (ESC | ~["\\])* '"';

fragment ESC : '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE : 'u' HEX HEX HEX HEX;
fragment HEX : [0-9a-fA-F];

NUMBER : '-'? INT '.' INT EXP?
    |   '-'? INT EXP
    |   '-'? INT;

fragment INT : '0' | [1-9][0-9]*;
fragment EXP : [Ee] [+\-]? INT;

WS : [ \t\r\n]+ -> skip;