package org.teachfx.antlr4.ep19.visitor;

import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep19.misc.ScopeUtil;
import org.teachfx.antlr4.ep19.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep19.parser.CymbolParser;
import org.teachfx.antlr4.ep19.parser.CymbolParser.*;
import org.teachfx.antlr4.ep19.runtime.FunctionSpace;
import org.teachfx.antlr4.ep19.runtime.MemorySpace;
import org.teachfx.antlr4.ep19.runtime.StructInstance;
import org.teachfx.antlr4.ep19.symtab.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 解释器 - 以visit模式实现
 */
public class Interpreter extends CymbolBaseVisitor<Object> {
<<<<<<< HEAD
    private final ScopeUtil scopes;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace currentSpace;
=======
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
    private static final ReturnValue sharedRetValue = new ReturnValue(null);
    private final ScopeUtil scopes;
    private final Stack<MemorySpace> memoryStack;
    private MemorySpace currentSpace;

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
        if (ctx.getChildCount() >= 2) {
<<<<<<< HEAD
            System.out.printf("var as - %s = %s%n",ctx.getChild(1).getText(),ctx.getChild(3).getText());
=======
            System.out.printf("var as - %s = %s%n", ctx.getChild(1).getText(), ctx.getChild(3).getText());
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2

            this.currentSpace.define(ctx.getChild(1).getText(), visit(ctx.getChild(3)));
        }
        return 0;
    }

    // < Expression evaluation
    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {

        Object left = visit(ctx.getChild(0));
        Object right = visit(ctx.getChild(2));
        String op = ctx.o.getText();

        Object ret = 0;

        Integer lhs = (Integer) left;
        Integer rhs = (Integer) right;

        switch (op) {
<<<<<<< HEAD
            case "+" -> {
                if (left.getClass().toString().equals("Float") || right.getClass().toString().equals("Float")) {
=======
            case "+":
                if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
                    ret = (Float) left + (Float) right;
                } else {
                    ret = (Integer) left + (Integer) right;
                }
<<<<<<< HEAD
            }
            case "-" -> {
                if (left.getClass().toString().equals("Float") || right.getClass().toString().equals("Float")) {
=======
                break;
            case "-":
                if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
                    ret = (Float) left - (Float) right;
                } else {
                    ret = (Integer) left - (Integer) right;
                }
<<<<<<< HEAD
            }
            case "*" -> {
                if (left.getClass().toString().equals("Float") || right.getClass().toString().equals("Float")) {
=======
                break;
            case "*":
                if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
                    ret = (Float) left * (Float) right;
                } else {
                    ret = (Integer) left * (Integer) right;
                }
<<<<<<< HEAD
            }
            case "/" -> {
                if (left.getClass().toString().equals("Float") || right.getClass().toString().equals("Float")) {
=======
                break;
            case "/":
                if (left.getClass().toString() == "Float" || right.getClass().toString() == "Float") {
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
                    ret = (Float) left / (Float) right;
                } else {
                    ret = (Integer) left / (Integer) right;
                }
<<<<<<< HEAD
            }
            case "<" -> ret = (lhs < rhs) ? TypeTable.TRUE : TypeTable.FALSE;
            case ">" -> ret = lhs > rhs ? TypeTable.TRUE : TypeTable.FALSE;
            case "<=" -> ret = lhs <= rhs ? TypeTable.TRUE : TypeTable.FALSE;
            case ">=" -> ret = lhs >= rhs ? TypeTable.TRUE : TypeTable.FALSE;
            case "!=" -> ret = lhs != rhs ? TypeTable.TRUE : TypeTable.FALSE;
            case "==" -> ret = lhs == rhs ? TypeTable.TRUE : TypeTable.FALSE;
=======
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
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
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
            if ("print".equalsIgnoreCase(method.getName())) {
                System.out.println(" eval " + ctx.getText());
                List<ParseTree> args = ctx.children.subList(1, ctx.children.size() - 1);
                String fmtArgs = args.stream()
<<<<<<< HEAD
                .map(this::visit)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(","));
=======
                        .map(p -> visit(p))
                        .filter(p -> p != null)
                        .map(p -> p.toString())
                        .collect(Collectors.joining(","));
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
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
        if (Objects.equals(ctx.getChild(0).getText(), "!")) {
            return (Objects.equals(i, TypeTable.TRUE) ? TypeTable.FALSE : TypeTable.TRUE);
        } else {
            return -i;
        }
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {

        StructInstance instance = (StructInstance) this.currentSpace.get(ctx.children.get(0).getText());

        String fieldName = ctx.expr(1).getText();

        return instance.get(fieldName);
    }

    // > Expression evaluation
    @Override
    public Object visitStatAssign(StatAssignContext ctx) {

        ExprContext lhs = ctx.expr(0);
        ExprContext rhs = ctx.expr(1);

<<<<<<< HEAD
        if (lhs instanceof ExprStructFieldAccessContext swaps) {
            StructInstance instance = (StructInstance)this.currentSpace.get(lhs.children.get(0).getText());
            Object assignValue = visit(ctx.expr(1));
            System.out.printf("assign %s with %s%n",lhs.getText(),assignValue);
            instance.update(swaps.expr(1).getText(),assignValue);
=======
        if (lhs instanceof ExprStructFieldAccessContext wlhs) {
            StructInstance instance = (StructInstance) this.currentSpace.get(lhs.children.get(0).getText());
            Object assignValue = visit(ctx.expr(1));
            System.out.printf("assign %s with %s%n", lhs.getText(), assignValue);
            instance.update(wlhs.expr(1).getText(), assignValue);

>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
        } else {
            this.currentSpace.update(lhs.getText(), visit(rhs));
        }

        return 0;
    }

    @Override
    public Object visitStatReturn(StatReturnContext ctx) {
        sharedRetValue.value = visit(ctx.getChild(1));
        throw sharedRetValue;
    }

    @Override
    public Object visitStatBlock(StatBlockContext ctx) {
        MemorySpace local = new MemorySpace("local", this.currentSpace);
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
        if (visit(ctx.cond) == TypeTable.TRUE) {
            visit(ctx.then);
        } else {
            if (ctx.elseDo != null) {
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
        if (ctx.expr().stream().findFirst().isPresent()) {
            ExprContext structRef = ctx.expr().stream().findFirst().get();
<<<<<<< HEAD
            StructSymbol symbol = (StructSymbol)visit(structRef);

            return new StructInstance(structRef.getText(),currentSpace,symbol);
=======
            StructSymbol symbol = (StructSymbol) visit(structRef);
            StructInstance instance = new StructInstance(structRef.getText(), currentSpace, symbol);

            return instance;
>>>>>>> d93896d1675d56790ebfb23ce74717ad166ddba2
        }

        return null;
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        return Objects.equals(ctx.getText(), "true") ? TypeTable.TRUE : TypeTable.FALSE;
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
        System.out.println("func entry - " + ctx.ID().getText());
        if (ctx.ID().getText().equalsIgnoreCase("main")) {
            // exec blockDef
            try {
                visit(ctx.blockDef);
            } catch (ReturnValue ex) {
                return ex.value;
            }
            return 0;
        }

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
