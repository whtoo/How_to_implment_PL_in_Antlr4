package org.teachfx.antlr4.ep20.pass.ir;

import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.expr.Var;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.scope.Scope;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;

import java.util.*;


public class CymbolIRBuilder implements ASTVisitor<Void,Expr> {
    public Prog root = null;

    private List<Stmt> stmts;
    private LinkedList<Label> breakStack;
    private LinkedList<Label> continueStack;

    private Stack<MethodSymbol> methodSymbolStack = new Stack<>();

    @Override
    public Void visit(CompileUnit rootNode) {
        root = new Prog(null);
        
        var funcLst = rootNode.getFuncDeclarations().stream().map(this::visit);
        funcLst.map(Func.class::cast).forEach(root::addFunc);

        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        if(varDeclNode.hasInitializer()) {
           addStmt(new Assign(new Var(varDeclNode.getRefSymbol()),(Expr) visit(varDeclNode.getAssignExprNode())));
        }
        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        methodSymbolStack.push((MethodSymbol) funcDeclNode.getRefSymbol());
        stmts = new ArrayList<>();
        breakStack = new LinkedList<>();
        continueStack = new LinkedList<>();
        transformBlockStmt(funcDeclNode.getBody());
        if(methodSymbolStack.peek().getName().equalsIgnoreCase("main")) {
            var mainRet = new ReturnVal(null);
            mainRet.setMainEntry(true);
            addStmt(mainRet);
        }
        root.addFunc(new Func(funcDeclNode.getDeclName(), (MethodSymbol) funcDeclNode.getRefSymbol(),stmts));
        methodSymbolStack.pop();
        return null;
    }

    @Override
    public Void visit(VarDeclStmtNode varDeclStmtNode) {
        if(varDeclStmtNode.getVarDeclNode().getAssignExprNode() != null) {
            varDeclStmtNode.getVarDeclNode().accept(this);
        }
        return null;
    }

    @Override
    public Expr visit(TypeNode typeNode) {
        return null;
    }

    @Override
    public Expr visit(BinaryExprNode binaryExprNode) {
        var lhs = (Expr) visit(binaryExprNode.getLhs());
        var rhs = (Expr) visit(binaryExprNode.getRhs());
        return new BinExpr(binaryExprNode.getOpType(),lhs,rhs);
    }

    @Override
    public Expr visit(IDExprNode idExprNode) {
        return new Var(idExprNode.getRefSymbol());
    }

    @Override
    public Expr visit(BoolExprNode boolExprNode) {
        return new BoolVal(boolExprNode.getRawValue());
    }

    @Override
    public Expr visit(IntExprNode intExprNode) {
        // generate IntVal from intExprNode
        return new IntVal(intExprNode.getRawValue());
    }

    @Override
    public Expr visit(FloatExprNode floatExprNode) {
        return new IntVal(floatExprNode.getRawValue().intValue());
    }

    @Override
    public Expr visit(NullExprNode nullExprNode) {
        return null;
    }

    @Override
    public Expr visit(StringExprNode stringExprNode) {
        return new StringVal(stringExprNode.getRawValue());
    }

    @Override
    public Expr visit(UnaryExprNode unaryExprNode) {
        var expr = (Expr) visit(unaryExprNode.getValExpr());
        return new UnaryExpr(unaryExprNode.getOpType(),expr);
    }

    @Override
    public Expr visit(CallFuncNode callExprNode) {
        var args = callExprNode.getArgsNode().stream().map(this::visit).map(Expr.class::cast).toList();
        return new CallFunc(new Var(callExprNode.getCallFuncSymbol()),args);
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        var thenLabel = new Label(null, ifStmtNode.getScope());
        var elseLabel = new Label(null, ifStmtNode.getScope());
        var endLabel = new Label(null, ifStmtNode.getScope());

        var condExpr = (Expr) visit(ifStmtNode.getConditionalNode());
        var thenSuccLabel = ifStmtNode.getElseBlock().isPresent() ? elseLabel : endLabel;
        cjump(condExpr,thenLabel,thenSuccLabel);
        label(thenLabel);
        transformBlockStmt(ifStmtNode.getThenBlock());

        if (ifStmtNode.getElseBlock().isPresent()) {
            label(elseLabel);
            ifStmtNode.getElseBlock().ifPresent(this::transformBlockStmt);
            jump(endLabel);
        }
        label(endLabel);
        return null;
    }

    protected void transformBlockStmt(StmtNode blockStmt) {
        blockStmt.accept(this);
    }
    @Override
    public Void visit(ExprStmtNode exprStmtNode) {
        addStmt(new ExprStmt((Expr) visit(exprStmtNode.getExprNode())));
        return null;
    }


    @Override
    public Void visit(ReturnStmtNode returnStmtNode) {
        var retVal = new ReturnVal((Expr)visit(returnStmtNode.getRetNode()));
        retVal.setMainEntry(methodSymbolStack.peek().getName().equalsIgnoreCase("main"));
        addStmt(retVal);
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        var thenLabel = new Label(null, whileStmtNode.getScope());
        var endLabel = new Label(null,whileStmtNode.getScope());

        var condExpr = (Expr) visit(whileStmtNode.getConditionNode());
        cjump(condExpr,thenLabel,endLabel);
        label(thenLabel);
        transformBlockStmt(whileStmtNode.getBlockNode());
        label(endLabel);
        return null;
    }

    @Override
    public Void visit(AssignStmtNode assignStmtNode) {
        addStmt(new Assign((Var) visit(assignStmtNode.getLhs()),(Expr) visit(assignStmtNode.getRhs())));
        return null;
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        Optional.ofNullable(blockStmtNode.getStmtNodes())
                .ifPresent(stmtNodes -> stmtNodes.forEach(this::transformBlockStmt));

        return null;
    }

    protected void addStmt(Stmt stmt) {
        stmts.add(stmt);
    }

    protected void jump(Label thenLabel) {
        addStmt(new JMP(thenLabel));
    }

    protected void label(Label label) {
        addStmt(label);
    }

    protected void cjump(Expr cond,Label thenLabel,Label elseLabel) {
        addStmt(new CJMP(cond,thenLabel,elseLabel));
    }
}
