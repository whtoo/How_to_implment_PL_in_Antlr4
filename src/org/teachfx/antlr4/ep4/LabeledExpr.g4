grammar LabeledExpr;
import CommonLexRules;

/** The start rule; begin parsing here. */
prog:   stat+ ; 

stat:   expr NEWLINE                
    |   ID '=' expr NEWLINE        
    |   NEWLINE                   
    ;

expr:   expr ('*'|'/') expr   
    |   expr ('+'|'-') expr   
    |   INT                    
    |   ID                    
    |   '(' expr ')'         
    ;

MUL : '*';
ADD : '+';
SUB : '-';
DIV : '/';