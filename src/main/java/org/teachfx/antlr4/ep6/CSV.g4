grammar CSV;

file : header row+;
header : row;
row : field ( ',' field )* '\n';
field : TEXT | STRING;
TEXT : ~[ \r\n\t,]+;
STRING : '"' ~[\\"] '"';