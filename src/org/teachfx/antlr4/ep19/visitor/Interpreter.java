package org.teachfx.antlr4.ep19.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.FunctionSpace;
import org.teachfx.antlr4.ep19.misc.MemorySpace;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.symtab.*;

import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class Interpreter extends CymbolBaseVisitor<Object> {
    private ScopeUtil scopes;
    private Stack<MemorySpace> memoryStack;
    private MemorySpace currentSpace;
    private static final ReturnValue sharedRetValue = new ReturnValue(null);

    public Interpreter(ScopeUtil scopes) {
        this.scopes = scopes;
        this.memoryStack = new Stack<>();
        this.currentSpace = MemorySpace.globalSpace;
    }

    public void interpret(ParseTree context) {
        context.accept(this);
    }

    public void stashSpace(MemorySpace space) {
        this.memoryStack.add(this.currentSpace);
        this.currentSpace = space;
    }

    public void restoreSpace() {
        this.currentSpace = this.memoryStack.pop();
    }

    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        if (ctx.getChildCount() > 2) {
            this.currentSpace.define(ctx.getChild(1).getText(), visit(ctx.getChild(3)));
        }
        return 0;
    }

    // < Expression evaluation
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        // System.out.println("exec in line " + ctx.start.getLine() + " : with " + ctx.getText());
        Object left = visit(ctx.getChild(0));
        Object right = visit(ctx.getChild(2));
        String op = ctx.o.getText();

        Object ret = 0;

        Integer lhs = (Integer) left;
        Integer rhs = (Integer) right;

        switch (op) {
        case "+":
            if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
                ret = (Float) left + (Float) right;
            } else {
                ret = (Integer) left + (Integer) right;
            }
            break;
        case "-":
            if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
                ret = (Float) left - (Float) right;
            } else {
                ret = (Integer) left - (Integer) right;
            }
            break;
        case "*":
            if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
                ret = (Float) left * (Float) right;
            } else {
                ret = (Integer) left * (Integer) right;
            }
            break;
        case "/":
            if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
                ret = (Float) left / (Float) right;
            } else {
                ret = (Integer) left / (Integer) right;
            }
            break;
        case "<":
            ret = (lhs < rhs) ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        case ">":
            ret = lhs > rhs ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        case "<=":
            ret = lhs <= rhs ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        case ">=":
            ret = lhs >= rhs ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        case "!=":
            ret = lhs != rhs ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        case "==":
            ret = lhs == rhs ? TypeTable.TRUE : TypeTable.FALSE;
            break;
        }
        return ret;
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        return super.visitExprPrimary(ctx);
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        // Resolve method symbol from scope unity by calling visitPrimaryID
        System.out.println("visit func " + ctx.getText());
        MethodSymbol method = (MethodSymbol) visit(ctx.getChild(0));

        Object value = 0;
        if (method.builtin) {
            if (method.getName() == "print") {
                System.out.println(" eval " + ctx.getText());
                List<ParseTree> args = ctx.children.subList(1, ctx.children.size() - 1);
                String fmtArgs = args.stream()
                .map(p -> visit(p))
                .filter(p -> p != null)
                .map(p -> p.toString())
                .collect(Collectors.joining(","));
                System.out.println(" print res :" + fmtArgs);
            }
        } else {
            FunctionSpace methodSpace = new FunctionSpace(method.getName(), method, this.currentSpace);
            // currentSpace is not changed
            // Fill params
            Set<String> paramNames = method.getMembers().keySet();
            int i = 0;
            for (String name : paramNames) {
                
                Object paramValue = visit(ctx.getChild(2 * (i + 1)));
                System.out.println(" name " + name + "," + " val " + paramValue.toString());
                methodSpace.define(name, paramValue);
                i++;
            }

            stashSpace(methodSpace);
            // exec blockDef
            try {
                visit(method.blockStmt);
            } catch (ReturnValue e) {
                value = e.value;
            }

            restoreSpace();
        }
        return value;
    }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        // visit children
        return visit(ctx.getChild(1));
    }

    @Override
    public Object visitStructDecl(CymbolParser.StructDeclContext ctx) {
        return 0;
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        Integer i = (Integer) visit(ctx.getChild(1));

        if (ctx.getChild(0).getText() == "!") {
            return (i == TypeTable.TRUE ? TypeTable.FALSE : TypeTable.TRUE);
        } else {
            return -i;
        }
    }

    // > Expression evaluation
    @Override
    public Object visitStatAssign(StatAssignContext ctx) {
        this.currentSpace.update(ctx.getChild(0).getText(), visit(ctx.getChild(2)));
        return 0;
    }

    @Override
    public Object visitStatReturn(StatReturnContext ctx) {
        sharedRetValue.value = visit(ctx.getChild(1));
        throw sharedRetValue;
    }

    @Override
    public Object visitStatBlock(StatBlockContext ctx) {
        MemorySpace local = new MemorySpace("local",this.currentSpace);
        Object ret = 0;
        stashSpace(local);
        
        super.visitStatBlock(ctx);
       
        restoreSpace();
        return ret;
    }

    @Override
    public Object visitStateCondition(StateConditionContext ctx) {
        Object ret = 0;
        // System.out.println("exec in line " + ctx.start.getLine() + ":" + ctx.getText());
        if(visit(ctx.cond) == TypeTable.TRUE) {
            visit(ctx.then);
        } else {
            if(ctx.elseDo != null) {
                visit(ctx.elseDo);
            }
        }
        return ret;
    }

    @Override
    public Object visitStateWhile(StateWhileContext ctx) {
        return super.visitStateWhile(ctx);
    }

    @Override
    public Object visitExprNew(ExprNewContext ctx) {
        
        for (ParseTree node : ctx.children) {
            System.out.println(String.format("new call ",node.getText()));
        }

        return super.visitExprNew(ctx);
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        return ctx.getText() == "true" ? TypeTable.TRUE : TypeTable.FALSE;
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        return Float.valueOf(ctx.getText());
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        // Return raw text
        return ctx.getText();
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        Scope scope = this.scopes.get(ctx);
        String tokenName = ctx.start.getText();

        Symbol symbol = scope.resolve(tokenName);
        if (ScopedSymbol.class.isAssignableFrom(symbol.getClass())) {
            // 作用域符号统统直接返回，它们都是一个自封闭的作用域和求值环境。
            // 针对它们的求值只能发生在其内部某个方法或者表达式的调用上。
            return symbol;
        }
        return this.currentSpace.get(symbol.getName());
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        return 0;
    }

    @Override
    public Object visitPrimaryINT(PrimaryINTContext ctx) {
        return Integer.valueOf(ctx.getText());
    }

    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        // Return raw text
        return ctx.getText();
    }
}
