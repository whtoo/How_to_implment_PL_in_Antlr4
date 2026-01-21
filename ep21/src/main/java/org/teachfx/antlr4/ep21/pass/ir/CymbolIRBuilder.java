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
import org.teachfx.antlr4.ep21.ir.expr.ArrayAccess;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayInit;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayLoad;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayStore;
import org.teachfx.antlr4.ep21.ir.lir.LIRNewArray;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import org.teachfx.antlr4.ep21.pass.cfg.CFGBuilder;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep21.symtab.scope.Scope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.util.Optional;
import java.util.Stack;


public class CymbolIRBuilder implements ASTVisitor<Void, VarSlot> {
    private final Logger logger = LogManager.getLogger(CymbolIRBuilder.class);
    public Prog prog = new Prog();
    private LinearIRBlock currentBlock = null;
    private LinearIRBlock exitBlock = null;
    private Stack<LinearIRBlock> breakStack;
    private Stack<LinearIRBlock> continueStack;
    private Stack<VarSlot> evalExprStack = null; // 显式初始化为null
    private Expr lValueIndexExpr = null; // 保存LValue情况下的数组索引表达式
    private VarSlot currentArraySlot = null; // 当前正在初始化的数组槽位
    private ASTNode curNode = null;
    private MethodSymbol currentMethodSymbol = null;

    // Public getter for prog field access
    public Prog getProg() {
        return prog;
    }

    @Override
    public Void visit(CompileUnit compileUnit) {
        logger.debug("visit CompileUnit: {}", compileUnit);
        prog = new Prog();
        compileUnit.getFuncDeclarations().forEach(x -> x.accept(this));
        compileUnit.getVarDeclarations().forEach(x -> x.accept(this));
        
        logger.debug("After AST processing - FuncDecl nodes: {}, VarDecl nodes: {}", 
            compileUnit.getFuncDeclarations(),
            compileUnit.getVarDeclarations());
        
        return null;
    }

    @Override
    public Void visit(VarDeclNode varDeclNode) {
        logger.debug("visit(VarDeclNode): {}", varDeclNode);
        System.out.println("[DEBUG] visit(VarDeclNode) object: " + System.identityHashCode(varDeclNode)
            + ", assignExprNode: " + (varDeclNode.getAssignExprNode() != null ? 
                varDeclNode.getAssignExprNode() + " (object: " + System.identityHashCode(varDeclNode.getAssignExprNode()) + ")" : "null")
            + ", idExprNode: " + varDeclNode.getIdExprNode() + " (object: " + System.identityHashCode(varDeclNode.getIdExprNode()) + ")");
        logger.debug("hasArraySize: {}, arraySizeExpr: {}", 
            varDeclNode.hasArraySize(), varDeclNode.getArraySizeExpr());
        
        FrameSlot arraySlot = null; // 数组变量的FrameSlot
        
        // 处理数组声明
        if (varDeclNode.hasArraySize()) {
            logger.debug("Processing array declaration: {}", varDeclNode);
            
            // 1. 创建FrameSlot
            arraySlot = FrameSlot.get((VariableSymbol) varDeclNode.getIdExprNode().getRefSymbol());
            logger.debug("Created FrameSlot for array variable: {}", arraySlot);

            // 2. 评估大小表达式（推送到表达式栈）
            varDeclNode.getArraySizeExpr().accept(this);
            
            // 3. 从栈弹出大小表达式
            Expr sizeExpr = peekEvalOperand();
            logger.debug("Array size expression type: {}, value: {}", 
                sizeExpr.getClass().getSimpleName(), sizeExpr);
            popEvalOperand();

            // 4. 创建LIRNewArray指令
            String elementTypeName = "int"; // TODO: get actual element type from symbol
            LIRNewArray newArrayInstr = new LIRNewArray(sizeExpr, arraySlot, elementTypeName);
            logger.debug("Created LIRNewArray instruction: {}", newArrayInstr);

            // 5. 添加到当前基本块
            logger.debug("Current block before addInstr: {}", getCurrentBlock());
            addInstr(newArrayInstr);
            logger.debug("After addInstr for LIRNewArray");
            
            logger.debug("Created LIRNewArray instruction for array declaration: {}", newArrayInstr);
        }
        
        // 处理初始化（如果有）
        if (varDeclNode.hasInitializer()) {
            if (varDeclNode.getAssignExprNode() instanceof ArrayInitializerExprNode) {
                // 数组初始化器：需要生成LIRArrayInit指令
                logger.debug("Processing array initializer for array variable: {}", arraySlot);
                
                if (arraySlot == null) {
                    // 非数组变量的数组初始化器？理论上不应该发生
                    throw new UnsupportedOperationException("Array initializer without array declaration not supported: " + varDeclNode);
                }
                
                // 设置当前数组槽位上下文，供ArrayInitializerExprNode使用
                currentArraySlot = arraySlot;
                try {
                    // 访问数组初始化器表达式，它会创建LIRArrayInit指令
                    varDeclNode.getAssignExprNode().accept(this);
                } finally {
                    currentArraySlot = null; // 清理上下文
                }
            } else {
                // 普通初始化器
                if (!(varDeclNode.getIdExprNode() instanceof IDExprNode)) {
                    throw new UnsupportedOperationException("暂不支持数组或复杂类型的变量声明初始化: " + varDeclNode);
                }
                var lhsNode = (IDExprNode)varDeclNode.getIdExprNode();
                var lhs = FrameSlot.get((VariableSymbol) lhsNode.getRefSymbol());

                // 评估表达式以生成RHS
                varDeclNode.getAssignExprNode().accept(this);
                var rhs = peekEvalOperand();

                addInstr(Assign.with(lhs, rhs));
                popEvalOperand();
            }
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
        varDeclStmtNode.getVarDeclNode().accept(this);

        return null;
    }

    @Override
    public VarSlot visit(TypeNode typeNode) {
        return null;
    }

    @Override
    public VarSlot visit(BinaryExprNode binaryExprNode) {
        curNode = binaryExprNode;
        logger.debug("visit BinaryExprNode: op={}, lhs={}, rhs={}",
            binaryExprNode.getOpType(),
            binaryExprNode.getLhs(),
            binaryExprNode.getRhs());

        binaryExprNode.getLhs().accept(this);
        var lhs = peekEvalOperand();
        logger.debug("BinaryExpr LHS evaluated to: {}", lhs);

        binaryExprNode.getRhs().accept(this);
        var rhs = peekEvalOperand();
        logger.debug("BinaryExpr RHS evaluated to: {}", rhs);

        var res = addInstr(BinExpr.with(binaryExprNode.getOpType(),lhs,rhs));
        logger.debug("BinExpr created: {}, result: {}", res, res.isPresent() ? "PRESENT" : "MISSING");

        res.ifPresent(this::pushEvalOperand);
        logger.debug("After BinExpr, evalExprStack size: {}", evalExprStack != null ? evalExprStack.size() : "null");

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
        System.out.println("[DEBUG] Entering visit(ArrayAccessExprNode) for " + arrayAccessExprNode
            + " (object: " + System.identityHashCode(arrayAccessExprNode) + ")");

        // 根据isLValue()决定生成什么IR指令
        if (arrayAccessExprNode.getArray() instanceof IDExprNode idExprNode) {
            System.out.println("[DEBUG] Processing array access for variable: " + idExprNode.getImage()
                + " (IDExprNode object: " + System.identityHashCode(idExprNode) + ")"
                + ", refSymbol: " + idExprNode.getRefSymbol()
                + ", ArrayAccessExprNode object: " + System.identityHashCode(arrayAccessExprNode));

            // 直接通过符号获取数组变量的FrameSlot，不依赖评估栈
            VariableSymbol arraySymbol = (VariableSymbol) idExprNode.getRefSymbol();
            if (arraySymbol == null) {
                System.out.println("[ERROR] 数组变量符号未解析: " + idExprNode.getImage()
                    + " (IDExprNode object: " + System.identityHashCode(idExprNode) + ")"
                    + ", refSymbol: " + idExprNode.getRefSymbol()
                    + ", ArrayAccessExprNode object: " + System.identityHashCode(arrayAccessExprNode));
                throw new IllegalStateException("数组变量符号未解析: " + idExprNode.getImage());
            }
            FrameSlot baseSlot = FrameSlot.get(arraySymbol);
            System.out.println("[DEBUG] Got FrameSlot for array variable " + idExprNode.getImage() + ": " + baseSlot);

            // 处理数组表达式（清理可能存在的栈推送）
            try {
                arrayAccessExprNode.getArray().accept(this);
                // 如果数组表达式推送了FrameSlot到栈，弹出它
                peekEvalOperand();
                popEvalOperand();
                logger.debug("清理了数组变量 {} 的栈推送", idExprNode.getImage());
            } catch (IllegalStateException e) {
                // 栈为空，正常情况
                logger.debug("数组变量 {} 未推送FrameSlot到评估栈", idExprNode.getImage());
            }

            // 处理索引表达式
            arrayAccessExprNode.getIndex().accept(this);
            Expr indexExpr = peekEvalOperand();

            if (!arrayAccessExprNode.isLValue()) {
                // RValue: 生成LIRArrayLoad指令（读取数组元素）
                VarSlot resultSlot = OperandSlot.genTemp();
                LIRArrayLoad arrayLoadInstr = new LIRArrayLoad(baseSlot, indexExpr, resultSlot);
                addInstr(arrayLoadInstr);
                popEvalOperand();  // 弹出索引表达式
                pushEvalOperand(resultSlot);
                return resultSlot;
            } else {
                // LValue: 推送baseSlot到栈，供AssignStmtNode使用
                // AssignStmtNode会从栈弹出baseSlot并从成员变量获取indexExpr，然后生成LIRArrayStore指令
                pushEvalOperand(baseSlot);
                setLValueIndexExpr(indexExpr);
                popEvalOperand();  // 弹出索引表达式
                return null;
            }
        } else {
            throw new UnsupportedOperationException("暂不支持复杂数组表达式: " + arrayAccessExprNode.getArray());
        }
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
    public VarSlot visit(ArrayInitializerExprNode arrayInitializerExprNode) {
        curNode = arrayInitializerExprNode;

        // 数组初始化需要：
        // 1. 评估所有初始化元素
        // 2. 创建LIRArrayInit节点来表示数组初始化
        // 3. 后续代码生成器会将其转换为具体的数组初始化指令
        
        // 评估所有初始化元素
        List<Expr> elements = new java.util.ArrayList<>();
        for (ExprNode element : arrayInitializerExprNode.getElements()) {
            element.accept(this);
            Expr evaluated = popEvalOperand();
            elements.add(evaluated);
        }

        // 获取数组槽位：优先使用当前数组声明上下文中的槽位
        VarSlot arraySlot;
        if (currentArraySlot != null) {
            // 在数组变量声明上下文中，使用已经分配的FrameSlot
            arraySlot = currentArraySlot;
            logger.debug("Using current array slot from declaration context: {}", arraySlot);
        } else {
            // 独立数组初始化表达式（理论上不应该发生，但保留兼容性）
            logger.warn("Array initializer without declaration context, using temporary slot");
            arraySlot = OperandSlot.genTemp();
        }
        
        // 创建数组初始化LIR指令
        LIRArrayInit arrayInit = new LIRArrayInit(
                arraySlot,
                arrayInitializerExprNode.getSize(),
                elements,
                arrayInitializerExprNode.getExprType() != null 
                    ? arrayInitializerExprNode.getExprType().getName() 
                    : "unknown"
        );
        
        addInstr(arrayInit);
        
        logger.info("Generated array initialization IR: {}", arrayInit);
        
        // 返回数组槽位作为结果
        pushEvalOperand(arraySlot);
        
        return arraySlot;
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
        condBlock.addStmt(new Label(condBlock.getScope()));
        var doBlock = new LinearIRBlock(currentBlock.getScope());
        doBlock.addStmt(new Label(doBlock.getScope()));
        var endBlock = new LinearIRBlock(currentBlock.getScope());
        endBlock.addStmt(new Label(endBlock.getScope()));

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
        thenBlock.addStmt(new Label(thenBlock.getScope()));
        var endBlock = new LinearIRBlock(currentBlock.getScope());
        endBlock.addStmt(new Label(endBlock.getScope()));

        prog.addBlock(thenBlock);
        prog.addBlock(endBlock);

        if (ifStmtNode.getElseBlock().isEmpty()) {
            jumpIf(cond,thenBlock,endBlock);
            setCurrentBlock(thenBlock);
            ifStmtNode.getThenBlock().accept(this);
        }else {
            var elseBlock = new LinearIRBlock(currentBlock.getScope());
            elseBlock.addStmt(new Label(elseBlock.getScope()));
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
        logger.debug("visit AssignStmtNode: LHS={}, RHS={}", 
            assignStmtNode.getLhs(), assignStmtNode.getRhs());

        // 处理数组访问作为左值（arr[index] = value）
        if (assignStmtNode.getLhs() instanceof ArrayAccessExprNode arrayAccessExpr) {
            logger.debug("Processing array access as LHS: arr[index] = value");

            // 首先处理左值数组访问（标记为LValue）
            arrayAccessExpr.accept(this);
            
            // 检查是否有保存的LValue索引表达式（来自ArrayAccessExprNode.visit）
            if (lValueIndexExpr != null) {
                // 此时栈顶应该是baseSlot
                
                // 处理右值（RHS）
                assignStmtNode.getRhs().accept(this);
                Expr rhs = popEvalOperand();  // 弹出rhs
                VarSlot baseSlot = popEvalOperand();  // 弹出baseSlot

                // 生成LIRArrayStore指令
                LIRArrayStore arrayStoreInstr = new LIRArrayStore(baseSlot, lValueIndexExpr, rhs);
                addInstr(arrayStoreInstr);
                logger.debug("Created LIRArrayStore instruction");

                // 清除LValue索引表达式
                lValueIndexExpr = null;

                return null;
            } else {
                // 回退到旧的ArrayAssign方式（如果LValue索引表达式不存在）
                // 处理数组访问左值（LHS）
                if (arrayAccessExpr.getArray() instanceof IDExprNode idExprNode) {
                    VariableSymbol arraySymbol = (VariableSymbol) idExprNode.getRefSymbol();
                    if (arraySymbol == null) {
                        logger.error("数组变量符号未解析: {}", idExprNode.getImage());
                        throw new IllegalStateException("数组变量符号未解析: " + idExprNode.getImage());
                    }
                    FrameSlot baseSlot = FrameSlot.get(arraySymbol);
                    logger.debug("Created FrameSlot for array variable: {}", idExprNode.getImage());

                    // 处理索引表达式
                    arrayAccessExpr.getIndex().accept(this);
                    var indexSlot = peekEvalOperand();
                    logger.debug("Array index evaluated to: {}", indexSlot);

                    // 处理右值（RHS）
                    assignStmtNode.getRhs().accept(this);
                    var rhs = peekEvalOperand();

                    // 创建数组访问表达式
                    ArrayAccess arrayAccess = ArrayAccess.with(null, indexSlot, baseSlot);
                    logger.debug("Created ArrayAccess for LHS: {}", arrayAccess);

                    // 创建数组赋值语句
                    addInstr(ArrayAssign.with(arrayAccess, rhs));
                    logger.debug("Created ArrayAssign instruction");

                    // 清理操作数栈（弹出索引和右值）
                    popEvalOperand(); // 弹出索引
                    popEvalOperand(); // 弹出右值

                    return null;
                } else {
                    throw new UnsupportedOperationException("暂不支持复杂数组表达式作为左值: " + arrayAccessExpr.getArray());
                }
            }
        }

        // 处理普通变量赋值
        logger.debug("Processing simple variable assignment");
        assignStmtNode.getRhs().accept(this);
        var rhs = peekEvalOperand();
        logger.debug("RHS expression evaluated to: {}", rhs);

        var lhsNode = (IDExprNode) assignStmtNode.getLhs();
        var lhs = FrameSlot.get((VariableSymbol) lhsNode.getRefSymbol());
        logger.debug("Created FrameSlot for LHS: {}", lhsNode.getImage());

        addInstr(Assign.with(lhs, rhs));
        popEvalOperand();
        logger.debug("Created Assign instruction: {} = {}", lhs, rhs);

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

        logger.debug("addInstr called with IRNode type: {}", stmt.getClass().getSimpleName());
        
        // 检查当前块是否存在
        if (getCurrentBlock() == null) {
            logger.error("addInstr called with null currentBlock");
            throw new IllegalStateException("Current block is not initialized. Call forkNewBlock() first.");
        }

        logger.debug("Current block is not null, adding statement");
        getCurrentBlock().addStmt(stmt);
        logger.debug("Statement added to current block");
        
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
        if (curNode != null) {
            logger.debug(curNode.toString());
        }

        // 空检查
        if (evalExprStack == null) {
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
        if (evalExprStack.isEmpty()) {
            throw new IllegalStateException(
                "Expression evaluation stack is empty when trying to peek. " +
                "This indicates that a previous expression evaluation did not push a value to stack. " +
                "Current node: " + (curNode != null ? curNode.toString() : "null")
            );
        }
        return evalExprStack.peek();
    }

    protected void setLValueIndexExpr(Expr expr) {
        this.lValueIndexExpr = expr;
    }

    public CFG<IRNode> getCFG(LinearIRBlock startBlocks) {
        var cfgBuilder = new CFGBuilder(startBlocks);
        return cfgBuilder.getCFG();
    }
}
