package org.teachfx.antlr4.ep11;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;

public class Calc {
    public static void main(String[] args) throws IOException {
        String fileName = null;
        if(args.length > 0) fileName = args[0];
        InputStream is = System.in;
        if(fileName != null) is = new FileInputStream(fileName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String expr = bufferedReader.readLine();
        int line = 1;
        MathParser parser = new MathParser(null);
        while(expr != null) {
            ANTLRInputStream input = new ANTLRInputStream(expr+"\n");
            MathLexer lexer = new MathLexer(input);
            lexer.setLine(line);
            lexer.setCharPositionInLine(0);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser.setInputStream(tokens);
            ParseTree tree = parser.compileUnit();
            ExpressionNode exprAST = new BuildAstVisitor().visit(tree);
            ASTVisitor<Double> astVisitor = new EvalExprVisitor();
            System.out.println("Result : " + astVisitor.visit(exprAST));
            expr = bufferedReader.readLine();
            line++;
        }
       
    }
    
}
