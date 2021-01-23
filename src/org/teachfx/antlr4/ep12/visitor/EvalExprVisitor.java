package org.teachfx.antlr4.ep12.visitor;

import java.util.HashMap;
import java.util.Map;

import org.teachfx.antlr4.ep12.ast.*;
import org.teachfx.antlr4.ep12.parser.*;

import org.teachfx.antlr4.ep12.parser.MathParser.AssignExprContext;
import org.teachfx.antlr4.ep12.parser.MathParser.CompileUnitContext;
import org.teachfx.antlr4.ep12.parser.MathParser.InfixExprContext;
import org.teachfx.antlr4.ep12.parser.MathParser.NumberExprContext;
import org.teachfx.antlr4.ep12.parser.MathParser.ParensExprContext;
import org.teachfx.antlr4.ep12.parser.MathParser.UnaryExprContext;
import org.teachfx.antlr4.ep12.parser.MathParser.VarExprContext;

public class EvalExprVisitor implements ASTVisitor<Double> {
    protected Map<String,Double> memory;

    public EvalExprVisitor(){
        this.memory = new HashMap<>();
    }

    @Override
    public Double visit(AdditionNode node) {
        return visit(node.left) + visit(node.right);
    }

    @Override
    public Double visit(SubtractionNode node) {
        return visit(node.left) - visit(node.right);
    }

    @Override
    public Double visit(MultiplicationNode node) {
       return visit(node.left) * visit(node.right);
    }

    @Override
    public Double visit(DivisionNode node) {
        return visit(node.left) / visit(node.right);
    }

    @Override
    public Double visit(NegateNode node) {
        return visit(node.innerNode) * (-1);
    }

    @Override
    public Double visit(NumberNode node) {
        return node.value;
    }

    @Override
    public Double visit(ExpressionNode node) {
        if(node.getClass().equals(AdditionNode.class)) {
            return visit((AdditionNode)node);
        } else if(node.getClass().equals(SubtractionNode.class)) {
            return visit((SubtractionNode)node);
        } else if(node.getClass().equals(MultiplicationNode.class)) {
            return visit((MultiplicationNode)node);
        } else if(node.getClass().equals(DivisionNode.class)) {
            return visit((DivisionNode)node);
        } else if(node.getClass().equals(NegateNode.class)) {
            return visit((NegateNode)node);
        } else if(node.getClass().equals(NumberNode.class)) {
            return visit((NumberNode)node);
        } else if(node.getClass().equals(AssignNode.class)) {
            return visit((AssignNode)node);
        } else if(node.getClass().equals(VarNode.class)) {
            return visit((VarNode)node);
        }
        return null;
    }

    @Override
    public Double visit(AssignNode node) {
        memory.put(node.varName,visit(node.value));
        return memory.get(node.varName);
    }

    @Override
    public Double visit(VarNode node) {     
        return memory.get(node.name);
    }

}
