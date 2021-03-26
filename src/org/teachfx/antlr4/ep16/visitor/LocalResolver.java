package org.teachfx.antlr4.ep16.visitor;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.teachfx.antlr4.ep16.parser.CymbolParser;
import org.teachfx.antlr4.ep16.parser.CymbolParser.*;
import org.teachfx.antlr4.ep16.symtab.*;
import org.teachfx.antlr4.ep16.misc.*;

/**
 * @description 给变量分配类型
 * @purpose 给变量确定具体类型
 */
public class LocalResolver extends CymbolASTVisitor<Object> {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    // ID[index-expr]
    private static final int ARRAY_EXPR = 0;
    private static final int FUNC_EXPR = 0;
    // struct ID block-declaration
    private static final int STRUCT = 0;
    private static final int MEMBER_PARENT = 2;
    // ID.(ID|FUNC-CALL)
    private static final int MEMBER = 0;
    
    private ScopeUtil scopes;
    public ParseTreeProperty<Type> types;

    public LocalResolver(ScopeUtil scopes) {
        this.scopes = scopes;
        this.types = new ParseTreeProperty<Type>();
    }


    @Override
    public Object visitVarDecl(VarDeclContext ctx) {
        super.visitVarDecl(ctx);
        System.out.println(ctx.getClass().toString());
        Type type = scopes.lookup(ctx.type());
        System.out.println(ctx.getText());

        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);

        if(type == null) { CompilerLogger.error(ctx , "Unknown type when declaring variable: " + var); }
        Scope scope = scopes.get(ctx);
        scope.define(var);
        return null;
    }

    @Override
    public Object visitFormalParameter(FormalParameterContext ctx) {
        Type type = scopes.lookup(ctx.type());
        VariableSymbol var = new VariableSymbol(Util.name(ctx), type);
        Scope scope = scopes.get(ctx);
        scope.define(var);
        return null;
    }
    
    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx){
        super.visitFunctionDecl(ctx);
        Symbol method = this.scopes.resolve(ctx);
        String returnType = ctx.type().getStart().getText();
        method.type = method.scope.lookup(returnType);
        
        return null;
    }
    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        // 这里有一个func name ctx和symbol没有建立匹配的问题
        copyType(ctx.expr(FUNC_EXPR),ctx);
    
        return null;
    }

    // @Override
    // public void exitExpr_Array(Expr_ArrayContext ctx) {
    //     copyType(ctx.expr(ARRAY_EXPR), ctx);
    // }

    @Override
    public Object visitExprGroup(ExprGroupContext ctx) {
        super.visitExprGroup(ctx);
        copyType(ctx.expr(),ctx);
        return null;
    }
    
    // @Override
    // public void visitTerminal(TerminalNode node) {
    //     if(node.getSymbol().getText().equals(".")) {
    //         ParserRuleContext parent = (ParserRuleContext) node.getParent();
    //         StructSymbol struct = (StructSymbol) types.get(parent.getChild(STRUCT));
    //         ParserRuleContext member = (ParserRuleContext) parent.getChild(MEMBER_PARENT).getChild(MEMBER);
    //         String name = member.start.getText();
    //         Type memberType = struct.resolveMember(name).type;
    //         stashType(member, memberType);
    //     }
    // }

    @Override
    public Object visitExprBinary(ExprBinaryContext ctx) {
        super.visitExprBinary(ctx);
        System.out.println(tab + "binary operation : " + ctx.getText());
        System.out.println(tab + "operator " + ctx.o.getText());
        System.out.println(tab + "left operand is " + ctx.expr(LEFT).getText() + " right operand is "+ctx.expr(RIGHT).getText());
        copyType(ctx.expr(LEFT),ctx);
        
        return null;
    }

    @Override
    public Object visitExprUnary(ExprUnaryContext ctx) {
        super.visitExprUnary(ctx);
        copyType(ctx.expr(),ctx);
        return null;
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        super.visitExprPrimary(ctx);
        copyType(ctx.primary(),ctx);
        return null;
    }

    @Override
    public Object visitPrimaryBOOL(PrimaryBOOLContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryCHAR(PrimaryCHARContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimaryID(PrimaryIDContext ctx) {
        setType(ctx);
        return null;
    }

    public Object visitPrimaryINT(PrimaryINTContext ctx) { 
        setType(ctx);
        return null; 
    }

    @Override
    public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx) {
        setType(ctx);
        return null;
    }

    @Override
    public Object visitPrimarySTRING(PrimarySTRINGContext ctx) {
        setType(ctx);
        return null;
    }

    private void setType(ParserRuleContext ctx) {
        // already defined type as in the case of struct members
        if(types.get(ctx) != null) { return; }
        
        int tokenValue = ctx.start.getType();
        String tokenName = ctx.start.getText();
        if(tokenValue == CymbolParser.ID) {
            Scope scope = scopes.get(ctx);
            Symbol s = scope.resolve(tokenName);
            
            if(s == null) { CompilerLogger.error(ctx,"Unknown type for id: " + tokenName); }
            else { stashType(ctx, s.type); }
            
        } else if (tokenValue == CymbolParser.INT || 
                   tokenName.equals("int")) {
            stashType(ctx, TypeTable.INT);   
        } else if (tokenValue == CymbolParser.FLOAT ||
                   tokenName.equals("float")) {
            stashType(ctx, TypeTable.FLOAT);            
        } else if (tokenValue == CymbolParser.CHAR ||
                   tokenName.equals("char")) {
            stashType(ctx, TypeTable.CHAR);
        } else if (tokenName.equals("true") ||
                   tokenName.equals("false")||
                   tokenName.equals("bool")) {
            stashType(ctx, TypeTable.BOOLEAN);            
        } else if (tokenName.equals("void")) {
            stashType(ctx, TypeTable.VOID);
        } else if (tokenName.equals("null")) {
            stashType(ctx, TypeTable.NULL);
        }
    }

    private void stashType(ParserRuleContext ctx, Type type) {
        types.put(ctx, type);
    }
    
    private void copyType(ParserRuleContext from, ParserRuleContext to) {
        Type type = types.get(from);
        types.put(to, type);
    }
    
}