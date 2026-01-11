grammar VMAssembler;
// END: members
@header {
package org.teachfx.antlr4.ep18r.parser;
}
program
    :   ( globalVariable | globals )?
        ( functionDeclaration | instr | label | NEWLINE )+
    ;

// how much data space
// START: data
globals : NEWLINE* '.globals' intVal=INT NEWLINE ;
// END: data

// global variable declaration: .global int g_var
globalVariable : '.global' type=ID name=ID NEWLINE;

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
    |   '.word' INT NEWLINE
    ;
// END: instr

// START: temp
temp
    :   ID   // basic code label; E.g., "loop"
    |   REG  // register name; E.g., "r0"
    |   FUNC // function label; E.g., "f()"
    |   ZERO // ABI register: zero
    |   RA   // ABI register: ra
    |   A0   // ABI register: a0
    |   A1   // ABI register: a1
    |   A2   // ABI register: a2
    |   A3   // ABI register: a3
    |   A4   // ABI register: a4
    |   A5   // ABI register: a5
    |   S0   // ABI register: s0
    |   S1   // ABI register: s1
    |   S2   // ABI register: s2
    |   S3   // ABI register: s3
    |   S4   // ABI register: s4
    |   SP   // ABI register: sp
    |   FP   // ABI register: fp
    |   LR   // ABI register: lr
    |   T0   // ABI register: t0
    |   T1   // ABI register: t1
// END: temp
    |   INT
    |   BOOL
    |   CHAR
    |   STRING
    |   FLOAT

    ;

label
    :   ID ':';

// ABI寄存器别名（必须在ID之前定义，以获得更高优先级）
ZERO:   'zero' ;
RA  :   'ra' ;
A0  :   'a0' ;
A1  :   'a1' ;
A2  :   'a2' ;
A3  :   'a3' ;
A4  :   'a4' ;
A5  :   'a5' ;
S0  :   's0' ;
S1  :   's1' ;
S2  :   's2' ;
S3  :   's3' ;
S4  :   's4' ;
SP  :   'sp' ;
FP  :   'fp' ;
LR  :   'lr' ;

// 临时寄存器别名（用于栈参数）
T0  :   't0' ;
T1  :   't1' ;

// 数字寄存器：r0-r15（必须在ID之前定义）
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