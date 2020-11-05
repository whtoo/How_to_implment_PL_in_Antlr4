grammar MathExpr;

@header {
package org.teachfx.antlr4.ep14.compiler;
import org.teachfx.antlr4.ep14.symtab.*;
}

@members {SymbolTable symtab;}

compileUnit
   [SymbolTable symtab] // pass symbol table to start rule
    @init {this.symtab = symtab;}       // set the parser's field
    :  varDelaration+;

varDelaration : vtype=type name=ID ('=' value=expr)?  ';'
    	{
         VariableSymbol vs = new VariableSymbol($name.text,$vtype.tsym);
    	 symtab.define(vs);
         System.out.println($name.text+" ref to " + symtab.resolve($name.text));
        }        
    ;
expr :  lhs=expr op='+' rhs=expr  
    |   INT
    |   FLOAT
    |   name=ID // reference variable in an expression
    	{System.out.println("a2 line "+$name.getLine()+ " " + $name.text +" : ref to "+
    	 symtab.resolve($name.text));}
    | '(' expr ')'
    ;
type returns [Type tsym]
@after { // $start is the first tree node matched by this rule
    System.out.println("a3 line "+$start.getLine()+": ref "+$tsym.getName());
}
:   'int' { 
    $tsym = (Type)symtab.resolve("int");
}
 |  'float'{
    $tsym = (Type)symtab.resolve("float");
 };

OP_ADD: '+';
OP_SUB: '-';
OP_MUL: '*';
OP_DIV: '/';
INT   :  '0'..'9'+;
FLOAT :  INT '.' [0-9]+;
ID  :   [a-zA-Z]+;
WS  :   [ \t\r\n] -> channel(HIDDEN);