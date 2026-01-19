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

import java.util.Optional;
import java.util.Stack;


public class CymbolIRBuilder implements ASTVisitor<Void, VarSlot> {
    private final Logger logger = LogManager.getLogger(CymbolIRBuilder.class);
    public Prog prog = null;

    private LinearIRBlock currentBlock = null;
    private LinearIRBlock exitBlock = null;
    private Stack<LinearIRBlock> breakStack;
    private Stack<LinearIRBlock> continueStack;
    private Stack<VarSlot> evalExprStack = null; // 显式初始化为null

    private ASTNode curNode = null;
    private MethodSymbol currentMethodSymbol = null;
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

        // Java 21: 模式匹配改进
        if(varDeclNode.hasInitializer()){
            if (!(varDeclNode.getIdExprNode() instanceof IDExprNode)) {
                throw new UnsupportedOperationException("暂不支持数组或复杂类型的变量声明初始化: " + varDeclNode);
            }
            var lhsNode = (IDExprNode)varDeclNode.getIdExprNode();
            var lhs = FrameSlot.get((VariableSymbol) lhsNode.getRefSymbol());
            varDeclNode.getAssignExprNode().accept(this);
            var rhs = peekEvalOperand();
            addInstr(Assign.with(lhs, rhs));
            popEvalOperand();
        }
        return null;
    }

    @Override
    public Void visit(FuncDeclNode funcDeclNode) {
        logger.debug("visit %s".formatted(funcDeclNode.toString()));

        curNode = funcDeclNode;
        currentMethodSymbol = (MethodSymbol) funcDeclNode.getRefSymbol();
        var entryLabel = new FuncEntryLabel(currentMethodSymbol.getName(),currentMethodSymbol.getArgs(),currentMethodSymbol.getLocals(),currentMethodSymbol);
        // Expand - create block without automatic label for functions
        currentBlock = new LinearIRBlock();
        currentBlock.setScope(currentMethodSymbol);

        evalExprStack = new Stack<>();
        breakStack = new Stack<>();
        continueStack = new Stack<>();

        var startBlock = currentBlock;
        getCurrentBlock().addStmt(entryLabel);

        // Process function body
        if (funcDeclNode.getBody() != null) {
            funcDeclNode.getBody().accept(this);
        }

        // If no ReturnVal was added (empty body or no return statements), add one at the end
        // Check if the last statement is ReturnVal
        boolean hasReturnVal = getCurrentBlock().getStmts().stream()
            .anyMatch(stmt -> stmt instanceof ReturnVal);
        if (!hasReturnVal) {
            var exitEntry = new ReturnVal(null, currentMethodSymbol);
            setExitHook(exitEntry, currentMethodSymbol);
            getCurrentBlock().addStmt(exitEntry);
        }

        // Add only the startBlock (function entry block) to prog
        prog.addBlock(startBlock);

        currentMethodSymbol = null; // Reset after function processing
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

            if(!idExprNode.isLValue()){
                // RVal
                pushEvalOperand(varSlot);
            }
        }
        return null;
    }

    @Override
    public VarSlot visit(ArrayAccessExprNode arrayAccessExprNode) {
        curNode = arrayAccessExprNode;
        // TODO: 暂不支持数组访问作为右值（value = arr[index]）
        // 需要实现数组地址计算和加载
        throw new UnsupportedOperationException("暂不支持数组访问作为右值: " + arrayAccessExprNode);
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
        VarSlot retValue = null;
        if (returnStmtNode.getRetNode() != null) {
            returnStmtNode.getRetNode().accept(this);
            retValue = popEvalOperand();
        }

        // Add ReturnVal directly to current block instead of jumping to exitBlock
        // This ensures ReturnVal is in the block that's added to prog
        ReturnVal retVal = new ReturnVal(retValue, currentMethodSymbol);
        setExitHook(retVal, currentMethodSymbol);
        addInstr(retVal);
        return null;
    }

    @Override
    public Void visit(WhileStmtNode whileStmtNode) {
        curNode = whileStmtNode;
        var condBlock = new LinearIRBlock(currentBlock.getScope());
        var doBlock = new LinearIRBlock(currentBlock.getScope());
        var endBlock = new LinearIRBlock(currentBlock.getScope());

        // Add all blocks to prog
        prog.addBlock(condBlock);
        prog.addBlock(doBlock);
        prog.addBlock(endBlock);

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

        prog.addBlock(thenBlock);
        prog.addBlock(endBlock);

        if (ifStmtNode.getElseBlock().isEmpty()) {
            jumpIf(cond,thenBlock,endBlock);
            setCurrentBlock(thenBlock);
            ifStmtNode.getThenBlock().accept(this);
        }else {
            var elseBlock = new LinearIRBlock(currentBlock.getScope());
            prog.addBlock(elseBlock);
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

        // TODO: 暂时不支持数组访问作为左值（arr[index] = value）
        // 当前仅支持简单变量赋值
        if (assignStmtNode.getLhs() instanceof ArrayAccessExprNode) {
            throw new UnsupportedOperationException("暂不支持数组访问作为赋值左值: " + assignStmtNode.getLhs());
        }

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
        
        // 自动添加Label到新创建的基本块，处理null scope情况
        Label label;
        if (scope != null) {
            label = new Label(scope);
        } else {
            // 当scope为null时，使用toString()和null scope构造Label
            label = new Label("L0", null); // 使用默认标签名
        }
        currentBlock.addStmt(label);
        
        // 确保evalExprStack在每次fork新block时正确初始化
        evalExprStack = new Stack<>();
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
        // 防止在currentBlock为null时调用setLink
        if (currentBlock != null) {
            currentBlock.setLink(nextBlock);
        } else {
            logger.warn("setCurrentBlock called with null currentBlock, skipping link operation");
        }
        currentBlock = nextBlock;
    }


    public Optional<VarSlot> addInstr(IRNode stmt) {
        // 添加null检查
        if (stmt == null) {
            logger.error("addInstr called with null stmt");
            throw new NullPointerException("stmt cannot be null");
        }

        // 检查当前块是否存在
        if (getCurrentBlock() == null) {
            logger.error("addInstr called with null currentBlock");
            throw new IllegalStateException("Current block is not initialized. Call forkNewBlock() first.");
        }

        getCurrentBlock().addStmt(stmt);
        // Java 21: 改进的switch表达式模式匹配
        return switch (stmt) {
            case BinExpr binExpr -> {
                popEvalOperand();
                popEvalOperand();
                yield Optional.of(OperandSlot.pushStack());
            }
            case UnaryExpr unaryExpr -> {
                popEvalOperand();
                yield Optional.of(OperandSlot.pushStack());
            }
            case CJMP cjmp -> {
                // CJMP不需要操作evalExprStack，因为它不是表达式
                yield Optional.empty();
            }
            case CallFunc callFunc -> {
                int i = callFunc.getArgs();
                while (i > 0) {
                    popEvalOperand();
                    i--;
                }
                // 如果存在返回值，则要模拟压入返回值以保证栈平衡
                if (!callFunc.getFuncType().isVoid()) {
                    pushEvalOperand(OperandSlot.genTemp());
                }
                yield Optional.empty();
            }
            case null, default -> Optional.empty();
        };
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

        // 日志验证：检查evalExprStack状态 - Java 21: 使用字符串模板
        System.out.println("DEBUG CymbolIRBuilder: pushEvalOperand called, evalExprStack=" +
                          (evalExprStack == null ? "null" : "initialized"));

        if (curNode != null) {
            logger.debug(curNode.toString());
        }

        // 空检查
        if (evalExprStack == null) {
            System.out.println("DEBUG CymbolIRBuilder: evalExprStack is null, throwing exception");
            throw new NullPointerException("evalExprStack is not initialized");
        }

        // Java 21: 模式匹配改进
        if (operand instanceof OperandSlot operandSlot) {
            logger.debug("-> eval stack %s%n", evalExprStack.toString());
            evalExprStack.push(operandSlot);
            return operandSlot;
        } else {
            cnt++;
            var assignee = OperandSlot.pushStack();
            evalExprStack.push(assignee);
            addInstr(Assign.with(assignee, operand));
            logger.debug("-> eval stack %s%n", evalExprStack.toString());
            return assignee;
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
