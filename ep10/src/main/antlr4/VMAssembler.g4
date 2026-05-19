grammar VMAssembler;

@header {
package org.teachfx.antlr4.ep10.parser;
}

program
    :   ( globals )?
        ( functionDeclaration | instr | label | NEWLINE )+
    ;

// how much data space for global variables
globals : NEWLINE* '.globals' intVal=INT NEWLINE ;

// .def functionName: args=N, locals=M
functionDeclaration
    :   '.def' name=ID ':' 'args' '=' a=INT ',' 'locals' '=' lo=INT NEWLINE
    ;

// Instructions: can have 0-3 operands
instr
    :   op=ID NEWLINE                         // 0-operand: halt, ret, iadd, print, etc.
    |   op=ID a=temp NEWLINE                   // 1-operand: iconst 42, br label, load 0
    |   op=ID a=temp ',' b=temp NEWLINE        // 2-operand: (rare, but supported)
    |   op=ID a=temp ',' b=temp ',' c=temp NEWLINE  // 3-operand: (rare, but supported)
    ;

// Operands can be: labels (ID), registers (REG), function names (FUNC),
// integers, floats, strings, bools, chars
temp
    :   ID      // code label or variable name
    |   REG     // register name like r0
    |   FUNC    // function label like fact()
    |   INT
    |   BOOL
    |   CHAR
    |   STRING
    |   FLOAT
    ;

// A label marks a position in code
label
    :   ID ':' ;

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
