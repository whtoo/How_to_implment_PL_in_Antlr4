grammar VMAssembler;
// END: members
@header {
package org.teachfx.antlr4.ep18.parser;
}
program
    :   globals?
        ( functionDeclaration | instr | label | NEWLINE )+
    ;
   
// how much data space
// START: data
globals : NEWLINE* '.globals' intVal=INT NEWLINE ;
// END: data

//  .def fact: args=1, locals=0
// START: func
functionDeclaration
    :   '.def' name=ID ':' 'args' '=' a=INT ',' 'locals' '=' lo=INT NEWLINE
    ;
// END: func

// START: instr
instr
    :   op=ID NEWLINE                         
    |   op=ID a=temp NEWLINE
    |   op=ID a=temp ',' b=temp NEWLINE
    |   op=ID a=temp ',' b=temp ',' c=temp NEWLINE
        
    ;
// END: instr

// START: temp
temp
    :   ID   // basic code label; E.g., "loop"
    |   REG  // register name; E.g., "r0"
    |   FUNC // function label; E.g., "f()"
// END: temp
    |   INT
    |   BOOL
    |   CHAR
    |   STRING
    |   FLOAT

    ;

label
    :   ID ':';

REG :   'r' INT ;

ID  :   LETTER (LETTER | '_' | '0'..'9')* ;

FUNC:   ID '()' {setText(getText().substring(0,getText().length()-2)); };

fragment
LETTER
    :   ('a'..'z' | 'A'..'Z')
    ;
    
INT :   '-'? '0'..'9'+ ;

CHAR:   '\'' . '\'' ;

BOOL: 'true' | 'false';

STRING: '"' STR_CHARS '"' { setText(getText().substring(1, getText().length() - 1)); };

fragment STR_CHARS : ~'"'* ;

FLOAT
    :   INT '.' INT*
    |   '.' INT+
    ;

WS  :   (' '|'\t')+ {skip();} ;

NEWLINE
    :   (';' .*?)? '\r'? '\n'  // optional comment followed by newline
    ;