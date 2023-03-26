package org.teachfx.antlr4.ep13;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep13.ast.ExpressionNode;

import java.io.*;

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
        ASTVisitor<Double> astVisitor = new EvalExprVisitor();
        
        while(expr != null) {
            CharStream input = CharStreams.fromString(expr + "\n");
            MathLexer lexer = new MathLexer(input);
            lexer.setLine(line);
            lexer.setCharPositionInLine(0);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            parser.setInputStream(tokens);
            ParseTree tree = parser.compileUnit();
            ExpressionNode exprAST = tree.accept(new BuildAstVisitor());
            System.out.println("Result : " + astVisitor.visit(exprAST));
            expr = bufferedReader.readLine();
            line++;
        }
       
    }
    
}
