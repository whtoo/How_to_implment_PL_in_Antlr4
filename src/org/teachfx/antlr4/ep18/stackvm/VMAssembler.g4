grammar VMAssembler;
// END: members
@header {
package org.teachfx.antlr4.ep18.stackvm;
}
program
    :   globals?
        ( functionDeclaration | instr | label | NEWLINE )+
    ;
   
// how much data space
// START: data
globals : NEWLINE* '.globals' INT NEWLINE ;
// END: data

//  .def fact: args=1, locals=0
// START: func
functionDeclaration
    :   '.def' name=ID ':' 'args' '=' a=INT ',' 'locals' '=' lo=INT NEWLINE
    ;
// END: func

// START: instr
instr
    :   ID NEWLINE                         
    |   ID operand NEWLINE                 
    |   ID a=operand ',' b=operand NEWLINE 
    |   ID a=operand ',' b=operand ',' c=operand NEWLINE
        
    ;
// END: instr

// START: operand
operand
    :   ID   // basic code label; E.g., "loop"
    |   REG  // register name; E.g., "r0"
    |   FUNC // function label; E.g., "f()"
    |   INT
// ...
// END: operand
    |   CHAR
    |   STRING
    |   FLOAT
    ;

label
    :   ID ':';

REG :   'r' INT ;

ID  :   LETTER (LETTER | '0'..'9')* ;

FUNC:   ID '()';

fragment
LETTER
    :   ('a'..'z' | 'A'..'Z')
    ;
    
INT :   '-'? '0'..'9'+ ;

CHAR:   '\'' . '\'' ;

STRING: '\\"' STR_CHARS '\\"' ;

fragment STR_CHARS : ~'"'* ;

FLOAT
    :   INT '.' INT*
    |   '.' INT+
    ;

WS  :   (' '|'\t')+ {skip();} ;

NEWLINE
    :   (';' .*?)? '\r'? '\n'  // optional comment followed by newline
    ;