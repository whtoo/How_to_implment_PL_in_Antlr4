lexer grammar CommonLexRules;
ID  :   [a-zA-Z]+ ;      // match identifiers <label id="code.tour.varSlot.3"/>
INT :   [0-9]+ ;         // match integers
NEWLINE:'\r'? '\n' ;     // return newlines to parser (is end-statement signal)
WS  :   [ \t]+ -> skip ; // toss out whitespace