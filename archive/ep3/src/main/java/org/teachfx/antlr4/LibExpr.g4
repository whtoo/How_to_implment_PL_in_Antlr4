grammar LibExpr;
import CommonLexRules;

/** The start rule; begin parsing here. */
prog:   stat+ ; 

stat:   varSlot NEWLINE
    |   ID '=' varSlot NEWLINE
    |   NEWLINE                   
    ;

varSlot:   varSlot ('*'|'/') varSlot
    |   varSlot ('+'|'-') varSlot
    |   INT                    
    |   ID                    
    |   '(' varSlot ')'
    ;
