package org.teachfx.antlr4.ep21.pass.ir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ast.ASTNode;
import org.teachfx.antlr4.ep21.ast.ASTVisitor;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.decl.FuncDeclNode;
import org.teachfx.antlr4.ep21.ast.decl.VarDeclNode;
import org.teachfx.antlr4.ep21.ast.expr.*;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.ast.type.TypeNode;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.CFGBuilder;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.util.List;
import java.util.Optional;
import java.util.Stack;


public class CymbolIRBuilder implements ASTVisitor<Void, VarSlot> {
    private static final Logger logger = LogManager.getLogger(CymbolIRBuilder.class);
    public Prog prog = null;

    private LinearIRBlock currentBlock = null;
    private LinearIRBlock exitBlock = null;
    private Stack<LinearIRBlock> breakStack;
    private Stack<LinearIRBlock> continueStack;
    private Stack<VarSlot> evalExprStack;

    private ASTNode curNode = null;
    @Override
    public Void visit(CompileUnit compileUnit) {
        prog = new Prog();
        logger.debug("visit root");
        compileUnit.getFuncDeclarations().forEach(x -> x.accept(this));

        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        logger.debug("visit %s".formatted(varDeclNode.toString()));

        if(varDeclNode.hasInitializer()){
            var lhsNode = (IDExprNode)varDeclNode.getIdExprNode();
            var lhs = FrameSlot.get((VariableSymbol) lhsNode.getRefSymbol());
            varDeclNode.getAssignExprNode().accept(this);
            var rhs = peekEvalOperand();
            addInstr(Assign.with(lhs,rhs));
            popEvalOperand();
        }
        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        logger.debug("visit %s".formatted(funcDeclNode.toString()));

        curNode = funcDeclNode;
        var methodSymbol = (MethodSymbol) funcDeclNode.getRefSymbol();
        var entryLabel = new FuncEntryLabel(methodSymbol.getName(),methodSymbol.getArgs(),methodSymbol.getLocals(),methodSymbol);
        // Expand
        forkNewBlock(methodSymbol);
        var startBlock = currentBlock;

        evalExprStack = new Stack<>();
        getCurrentBlock().addStmt(entryLabel);

        var exitBlock = new LinearIRBlock();
        exitBlock.setScope(methodSymbol);
        var exitEntry = new ReturnVal(null,methodSymbol);

        setExitHook(exitEntry,methodSymbol);

        exitBlock.addStmt(exitEntry);
        this.exitBlock = exitBlock;

        funcDeclNode.getBody().accept(this);

        prog.addBlock(startBlock);

        setCurrentBlock(exitBlock);

        clearBlock();
        return null;
    }

    @Override
    public Void visit(VarDeclStmtNode varDeclStmtNode) {
        logger.debug("visit %s".formatted(varDeclStmtNode.toString()));
        if (varDeclStmtNode.getVarDeclNode().hasInitializer()) {
            varDeclStmtNode.getVarDeclNode().accept(this);
        }

        return null;
    }

    @Override
    public VarSlot visit(TypeNode typeNode) {
        return null;
    }

    @Override
    public VarSlot visit(BinaryExprNode binaryExprNode) {
        curNode = binaryExprNode;

        binaryExprNode.getLhs().accept(this);
        var lhs = peekEvalOperand();

        binaryExprNode.getRhs().accept(this);
        var rhs = peekEvalOperand();

        var res = addInstr(BinExpr.with(binaryExprNode.getOpType(),lhs,rhs));

        res.ifPresent(this::pushEvalOperand);

        return null;
    }

    @Override
    public VarSlot visit(UnaryExprNode unaryExprNode) {
        curNode = unaryExprNode;
        unaryExprNode.getValExpr().accept(this);
        var expr = peekEvalOperand();
        var res = addInstr(UnaryExpr.with(unaryExprNode.getOpType(),expr));
        res.ifPresent(this::pushEvalOperand);

        return null;
    }

    @Override
    public VarSlot visit(IDExprNode idExprNode) {
        curNode = idExprNode;

        if (idExprNode.getRefSymbol() instanceof VariableSymbol) {
            var varSlot = FrameSlot.get((VariableSymbol) idExprNode.getRefSymbol());

            if(!idExprNode.isLValue()) {
                // RVal
                pushEvalOperand(varSlot);
            }
        }
        return null;
    }

    @Override
    public VarSlot visit(BoolExprNode boolExprNode) {

        pushEvalOperand( ConstVal.valueOf(boolExprNode.getRawValue()));

        return null;
    }
    @Override
    public VarSlot visit(IntExprNode intExprNode) {
        pushEvalOperand(ConstVal.valueOf(intExprNode.getRawValue()));
        return null;
    }

    @Override
    public VarSlot visit(FloatExprNode floatExprNode) {
        pushEvalOperand(ConstVal.valueOf(floatExprNode.getRawValue()));
        return null;
    }

    @Override
    public VarSlot visit(NullExprNode nullExprNode) {
        pushEvalOperand(ConstVal.valueOf(nullExprNode.getRawValue()));
        return null;
    }

    @Override
    public VarSlot visit(StringExprNode stringExprNode) {
        pushEvalOperand(ConstVal.valueOf(stringExprNode.getRawValue()));
        return null;
    }

    @Override
    public VarSlot visit(CallFuncNode callExprNode) {
        curNode = callExprNode;
        var methodSymbol = callExprNode.getCallFuncSymbol();
        var funcName = callExprNode.getFuncName();
        var args = callExprNode.getArgsNode().size();
        callExprNode.getArgsNode().forEach(x -> x.accept(this));
        addInstr(new CallFunc(funcName,args,methodSymbol));
        return null;
    }

    @Override
    public Void visit(ExprStmtNode exprStmtNode) {
        curNode = exprStmtNode;
        exprStmtNode.getExprNode().accept(this);
        return null;
    }

    @Override
    public Void visit(BlockStmtNode blockStmtNode) {
        curNode = blockStmtNode;
        blockStmtNode.getStmtNodes().forEach(x -> x.accept(this));
        return null;
    }

    @Override
    public Void visit(ReturnStmtNode returnStmtNode) {
        curNode = returnStmtNode;
        if (returnStmtNode.getRetNode() != null) {
            returnStmtNode.getRetNode().accept(this);
            popEvalOperand();
        }

        jump(exitBlock);
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        curNode = whileStmtNode;
        var condBlock = new LinearIRBlock(currentBlock.getScope());
        var doBlock = new LinearIRBlock(currentBlock.getScope());
        var endBlock = new LinearIRBlock(currentBlock.getScope());

        jump(condBlock);

        pushBreakStack(endBlock);
        pushContinueStack(condBlock);

        setCurrentBlock(condBlock);
        whileStmtNode.getConditionNode().accept(this);

        var cond = peekEvalOperand();

        jumpIf(cond,doBlock,endBlock);

        setCurrentBlock(doBlock);
        whileStmtNode.getBlockNode().accept(this);
        jump(condBlock);

        popBreakStack();
        popContinueStack();

        setCurrentBlock(endBlock);
        return null;
    }

    @Override
    public Void visit(IfStmtNode ifStmtNode) {
        curNode = ifStmtNode;

        ifStmtNode.getCondExpr().accept(this);
        var cond = peekEvalOperand();
        var thenBlock = new LinearIRBlock(currentBlock.getScope());
        var endBlock = new LinearIRBlock(currentBlock.getScope());

        if (ifStmtNode.getElseBlock().isEmpty()) {
            jumpIf(cond,thenBlock,endBlock);
            setCurrentBlock(thenBlock);
            ifStmtNode.getThenBlock().accept(this);
        }else {
            var elseBlock = new LinearIRBlock(currentBlock.getScope());
            jumpIf(cond,thenBlock,elseBlock);
            setCurrentBlock(thenBlock);
            ifStmtNode.getThenBlock().accept(this);
            setCurrentBlock(elseBlock);
            ifStmtNode.getElseBlock().ifPresent(x -> x.accept(this));
        }

        setCurrentBlock(endBlock);
        return null;
    }

    @Override
    public Void visit(AssignStmtNode assignStmtNode) {
        curNode = assignStmtNode;
        assignStmtNode.getRhs().accept(this);
        var rhs = peekEvalOperand();
        var lhsNode = (IDExprNode) assignStmtNode.getLhs();
        var lhs = FrameSlot.get((VariableSymbol) lhsNode.getRefSymbol());
        addInstr(Assign.with(lhs,rhs));
        popEvalOperand();
        return null;
    }

    @Override
    public Void visit(BreakStmtNode breakStmtNode) {
        curNode = breakStmtNode;
        jump(breakStack.peek());

        return null;
    }

    @Override
    public Void visit(ContinueStmtNode continueStmtNode) {
        curNode = continueStmtNode;
        jump(continueStack.peek());

        return null;
    }

    public void forkNewBlock(Scope scope) {
        currentBlock = new LinearIRBlock();
        currentBlock.setScope(scope);
        breakStack = new Stack<>();
        continueStack = new Stack<>();
    }
    public void clearBlock() {
        currentBlock = null;
    }

    protected LinearIRBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(LinearIRBlock nextBlock) {
        currentBlock.setLink(nextBlock);
        currentBlock = nextBlock;
    }


    public Optional<VarSlot> addInstr(IRNode stmt) {

        getCurrentBlock().addStmt(stmt);
        if (stmt instanceof BinExpr) {
            popEvalOperand();
            popEvalOperand();
            return Optional.of(OperandSlot.pushStack());
        } else if (stmt instanceof UnaryExpr) {
            popEvalOperand();
            return Optional.of(OperandSlot.pushStack());
        } else if(stmt instanceof CJMP) {
            popEvalOperand();
        } else if (stmt instanceof CallFunc callFunc) {
            int i = callFunc.getArgs();

            while (i > 0){
                popEvalOperand();
                i--;
            }
            // 如果存在返回值，则要模拟压入返回值以保证栈平衡
            if(!callFunc.getFuncType().isVoid()) {
               pushEvalOperand(OperandSlot.genTemp());
            }
        }

        return Optional.empty();
    }

    protected void setExitHook(ReturnVal returnVal,MethodSymbol methodSymbol) {
        if (methodSymbol.getName().equalsIgnoreCase("main")) {
            returnVal.setMainEntry(true);
        }
    }

    public void jump(LinearIRBlock block) {
        addInstr(new JMP(block));
    }
    public void jumpIf(VarSlot cond, LinearIRBlock thenBlock, LinearIRBlock elseBlock) {
        addInstr(new CJMP(cond,thenBlock,elseBlock));
    }
    public void pushBreakStack(LinearIRBlock linearIRBlock) {
        breakStack.push(linearIRBlock);
    }

    public void popBreakStack() {
        breakStack.pop();
    }


    public void pushContinueStack(LinearIRBlock linearIRBlock) {
        continueStack.push(linearIRBlock);
    }


    public void popContinueStack() {
        continueStack.pop();
    }
    static int cnt = 0;
    protected VarSlot pushEvalOperand(Operand operand) {

        if (curNode != null) {
            logger.debug(curNode.toString());
        }

        if (!(operand instanceof OperandSlot)){
            cnt++;
            var assignee = OperandSlot.pushStack();
            evalExprStack.push(assignee);
            addInstr(Assign.with(assignee, operand));
            logger.debug("-> eval stack %s%n", evalExprStack.toString());

            return assignee;
        } else {
            logger.debug("-> eval stack %s%n", evalExprStack.toString());
            evalExprStack.push((VarSlot) operand);
            return (VarSlot) operand;
        }

    }

    protected VarSlot popEvalOperand() {
        var res = evalExprStack.pop();
        logger.debug("pop eval operand %s",res);
        logger.debug("pop stack");
        OperandSlot.popStack();
        if (OperandSlot.getOrdSeq() < 0) {
            throw new RuntimeException("un matched pop");
        }
        return res;
    }

    protected VarSlot peekEvalOperand() {
        return evalExprStack.peek();
    }

    public CFG<IRNode> getCFG(LinearIRBlock startBlocks) {
        var cfgBuilder = new CFGBuilder(startBlocks);
        return cfgBuilder.getCFG();
    }
}
