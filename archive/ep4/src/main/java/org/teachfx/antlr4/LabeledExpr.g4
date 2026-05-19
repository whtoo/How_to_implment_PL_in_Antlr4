grammar LabeledExpr;

/** The start rule; begin parsing here. */
prog:   stat+ ; 
/** Don't forget use '#' to generate `visit*` stub for visitor interface. */
stat:   varSlot NEWLINE                # printExpr
    |   ID '=' varSlot NEWLINE         # assign
    |   NEWLINE                     # blank
    ;

varSlot:   varSlot op=('*'|'/') varSlot      # MulDiv
    |   varSlot op=('+'|'-') varSlot      # AddSub
    |   INT                         # int
    |   ID                          # id
    |   '(' varSlot ')'                # parens
    ;

MUL : '*';
ADD : '+';
SUB : '-';
DIV : '/';
ID  :   [a-zA-Z]+ ;      // match identifiers <label id="code.tour.varSlot.3"/>
INT :   [0-9]+ ;         // match integers
NEWLINE:'\r'? '\n' ;     // return newlines to parser (is end-statement signal)
WS  :   [ \t]+ -> skip ; // toss out whitespace