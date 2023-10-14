package org.teachfx.antlr4.ep20.pass.ir;

import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.CompileUnit;
import org.teachfx.antlr4.ep20.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep20.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep20.ast.expr.*;
import org.teachfx.antlr4.ep20.ast.stmt.*;
import org.teachfx.antlr4.ep20.ast.type.TypeNode;
import org.teachfx.antlr4.ep20.ir.Prog;
import org.teachfx.antlr4.ep20.ir.def.Func;
import org.teachfx.antlr4.ep20.ir.expr.*;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.values.BoolVal;
import org.teachfx.antlr4.ep20.ir.expr.values.IntVal;
import org.teachfx.antlr4.ep20.ir.expr.values.StringVal;
import org.teachfx.antlr4.ep20.ir.expr.values.Var;
import org.teachfx.antlr4.ep20.ir.stmt.*;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;


public class CymbolIRBuilder implements ASTVisitor<Void, Expr> {
    private final Stack<MethodSymbol> methodSymbolStack = new Stack<>();
    public Prog root = null;
    private Func currentFunc = null;
    private List<Stmt> stmts;
    private Stack<Label> breakStack;
    private Stack<Label> continueStack;

    @Override
    public Void visit(CompileUnit rootNode) {
        root = new Prog(null);

        var funcLst = rootNode.getFuncDeclarations().stream().map(this::visit);
        funcLst.map(Func.class::cast).forEach(root::addFunc);

        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        if (varDeclNode.hasInitializer()) {
            addStmt(new Assign(new Var(varDeclNode.getRefSymbol()), visit(varDeclNode.getAssignExprNode())));
        }
        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        methodSymbolStack.push((MethodSymbol) funcDeclNode.getRefSymbol());
        /**/
        stmts = new ArrayList<>();
        /**/
        breakStack = new Stack<>();
        continueStack = new Stack<>();

        currentFunc = new Func(funcDeclNode.getDeclName(), (MethodSymbol) funcDeclNode.getRefSymbol(), stmts);

        setRetHook(methodSymbolStack.peek());

        transformBlockStmt(funcDeclNode.getBody());

        currentFunc.setBody(stmts);

        root.addFunc(currentFunc);

        methodSymbolStack.pop();

        currentFunc = null;

        return null;
    }

    private void setRetHook(MethodSymbol methodSymbol) {
        if (methodSymbol.getName().equalsIgnoreCase("main")) {
            var mainRet = new ReturnVal(null, methodSymbol);
            mainRet.setMainEntry(true);
            currentFunc.retHook = (mainRet);
        } else {
            var comRet = new ReturnVal(null, methodSymbol);
            comRet.setMainEntry(false);
            currentFunc.retHook = (comRet);
        }
    }

    @Override
    public Void visit(VarDeclStmtNode varDeclStmtNode) {
        if (varDeclStmtNode.getVarDeclNode().getAssignExprNode() != null) {
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
        return new BinExpr(binaryExprNode.getOpType(), lhs, rhs);
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
        return new UnaryExpr(unaryExprNode.getOpType(), expr);
    }

    @Override
    public Expr visit(CallFuncNode callExprNode) {
        var args = callExprNode.getArgsNode().stream().map(this::visit).map(Expr.class::cast).toList();
        return new CallFunc(new Var(callExprNode.getCallFuncSymbol()), args);
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        var thenLabel = new Label(null, ifStmtNode.getScope());
        var elseLabel = new Label(null, ifStmtNode.getScope());
        var endLabel = new Label(null, ifStmtNode.getScope());

        var condExpr = (Expr) visit(ifStmtNode.getConditionalNode());
        var hasElseBranch = ifStmtNode.getElseBlock().isPresent();
        var thenSuccLabel = hasElseBranch ? elseLabel : endLabel;

        cjump(condExpr, thenLabel, thenSuccLabel);
        label(thenLabel);

        ifStmtNode.getThenBlock().accept(this);

        if (hasElseBranch) jump(endLabel);

        if (hasElseBranch) {
            label(elseLabel);
            ifStmtNode.getElseBlock().ifPresent(this::transformBlockStmt);
        }

        /// Label local
        label(endLabel);

        return null;
    }

    protected void transformBlockStmt(StmtNode blockStmt) {
        blockStmt.accept(this);
    }

    @Override
    public Void visit(ExprStmtNode exprStmtNode) {
        addStmt(new ExprStmt(visit(exprStmtNode.getExprNode())));
        return null;
    }


    @Override
    public Void visit(ReturnStmtNode returnStmtNode) {
        var retVal = (Expr) visit(returnStmtNode.getRetNode());
        addStmt(new ExprStmt(retVal));
        jump(currentFuncExitEntry());
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        var beginLabel = new Label(null, whileStmtNode.getScope());
        var thenLabel = new Label(null, whileStmtNode.getScope());
        var endLabel = new Label(null, whileStmtNode.getScope());

        pushBreakTarget(endLabel);
        pushContinueTarget(beginLabel);

        var condExpr = (Expr) visit(whileStmtNode.getConditionNode());
        label(beginLabel);
        cjump(condExpr, thenLabel, endLabel);
        label(thenLabel);
        transformBlockStmt(whileStmtNode.getBlockNode());
        jump(beginLabel);

        label(endLabel);

        popBreakTarget();
        popContinueTarget();

        return null;
    }

    @Override
    public Void visit(AssignStmtNode assignStmtNode) {
        addStmt(new Assign((Var) visit(assignStmtNode.getLhs()), visit(assignStmtNode.getRhs())));
        return null;
    }

    @Override
    public Void visit(BreakStmtNode breakStmtNode) {
        addStmt(new JMP(currentBreakTarget()));
        return null;
    }

    @Override
    public Void visit(ContinueStmtNode continueStmtNode) {
        addStmt(new JMP(currentContinueTarget()));
        return null;
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        Optional.ofNullable(blockStmtNode.getStmtNodes()).ifPresent(stmtNodes -> stmtNodes.forEach(this::transformBlockStmt));

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

    protected void cjump(Expr cond, Label thenLabel, Label elseLabel) {
        addStmt(new CJMP(cond, thenLabel, elseLabel));
    }

    protected Label currentBreakTarget() {
        return breakStack.peek();
    }

    private Label currentContinueTarget() {
        return continueStack.peek();
    }

    protected void pushBreakTarget(Label label) {
        breakStack.push(label);
    }

    protected void pushContinueTarget(Label label) {
        continueStack.push(label);
    }

    protected void popBreakTarget() {
        breakStack.pop();
    }

    protected void popContinueTarget() {
        continueStack.pop();
    }

    protected Label currentFuncExitEntry() {
        return currentFunc.retHook.retFuncLabel;
    }
}
