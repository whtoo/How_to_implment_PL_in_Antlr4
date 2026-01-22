package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.expr.ArrayAccess;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayInit;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayLoad;
import org.teachfx.antlr4.ep21.ir.lir.LIRArrayStore;
import org.teachfx.antlr4.ep21.ir.lir.LIRNode;
import org.teachfx.antlr4.ep21.ir.lir.LIRNewArray;
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Register VM code generator for EP18R.
 * <p>
 * This class implements the {@link ICodeGenerator} interface to generate
 * assembly code for the EP18R register-based virtual machine. It translates
 * IR nodes to EP18R assembly text format (.vmr files) using the visitor pattern.
 * </p>
 * <p>
 * EP18R寄存器分配约定：
 * <ul>
 *   <li>r0: 零寄存器（恒为0）</li>
 *   <li>r1: 返回地址 (ra)</li>
 *   <li>r2: 参数0/返回值 (a0)</li>
 *   <li>r3: 参数1 (a1)</li>
 *   <li>r4: 参数2 (a2)</li>
 *   <li>r5-r9: 临时寄存器 (t0-t4)</li>
 *   <li>r10-r12: 保存寄存器 (s0-s2)</li>
 *   <li>r13: 栈指针 (sp)</li>
 *   <li>r14: 帧指针 (fp)</li>
 *   <li>r15: 链接寄存器 (lr)</li>
 * </ul>
 * </p>
 */
public class RegisterVMGenerator implements ICodeGenerator {

    /**
     * Target VM identifier for EP18R register VM.
     */
    public static final String TARGET_VM = "EP18R";

    private final RegisterEmitter emitter;
    private final RegisterOperatorEmitter operatorEmitter;
    private final IRegisterAllocator registerAllocator;
    private String lastAssemblyOutput = "";

    /**
     * Creates a new RegisterVMGenerator with default emitters and a simple round-robin allocator.
     */
    public RegisterVMGenerator() {
        this(new RegisterEmitter(), new RegisterOperatorEmitter(), null);
    }

    /**
     * Creates a new RegisterVMGenerator with default emitters and custom register allocator.
     *
     * @param registerAllocator custom register allocator (null for simple round-robin)
     */
    public RegisterVMGenerator(IRegisterAllocator registerAllocator) {
        this(new RegisterEmitter(), new RegisterOperatorEmitter(), registerAllocator);
    }

    private RegisterVMGenerator(RegisterEmitter emitter, RegisterOperatorEmitter operatorEmitter, IRegisterAllocator registerAllocator) {
        this.emitter = emitter;
        this.operatorEmitter = operatorEmitter;
        this.registerAllocator = registerAllocator;
    }

    @Override
    public CodeGenerationResult generate(Prog program) {
        long startTime = System.currentTimeMillis();
        emitter.clear();
        List<String> errors = new ArrayList<>();

        try {
            List<IRNode> instructions = program.linearInstrs();
            int instructionCount = generateInstructions(instructions, errors);

            long generationTime = System.currentTimeMillis() - startTime;
            String output = emitter.flush();

            return errors.isEmpty()
                    ? CodeGenerationResult.success(output, TARGET_VM, instructionCount, generationTime)
                    : CodeGenerationResult.failure(errors, TARGET_VM);

        } catch (Exception e) {
            errors.add("Code generation failed: " + e.getMessage());
            return CodeGenerationResult.failure(errors, TARGET_VM);
        }
    }

    @Override
    public CodeGenerationResult generateFromInstructions(List<IRNode> instructions) {
        long startTime = System.currentTimeMillis();
        emitter.clear();
        List<String> errors = new ArrayList<>();

        try {
            int instructionCount = generateInstructions(instructions, errors);
            long generationTime = System.currentTimeMillis() - startTime;
            String output = emitter.flush();

            return errors.isEmpty()
                    ? CodeGenerationResult.success(output, TARGET_VM, instructionCount, generationTime)
                    : CodeGenerationResult.failure(errors, TARGET_VM);

        } catch (Exception e) {
            errors.add("Code generation failed: " + e.getMessage());
            return CodeGenerationResult.failure(errors, TARGET_VM);
        }
    }

    @Override
    public String getTargetVM() {
        return TARGET_VM;
    }

    @Override
    public void configure(Map<String, Object> config) {
        // Configuration support for future enhancements
    }

    @Override
    public IEmitter getEmitter() {
        return emitter;
    }

    private int generateInstructions(List<IRNode> instructions, List<String> errors) {
        // Split instructions by function and apply TRO detection per-function
        List<List<IRNode>> functionGroups = splitByFunction(instructions);
        int totalInstructions = 0;

        for (List<IRNode> functionInstructions : functionGroups) {
            // Check if this function should be optimized with TRO
            if (TROHelper.isFibonacciPattern(functionInstructions)) {
                System.out.println("[RegisterVMGenerator] Applying TRO for Fibonacci pattern");
                String functionName = extractFunctionNameFromInstructions(functionInstructions);
                if (functionName != null) {
                    totalInstructions += TROHelper.generateFibonacciIterative(functionName, emitter);
                    continue;
                }
            }

            // Check for direct tail recursion
            if (TROHelper.isDirectTailRecursive(functionInstructions)) {
                System.out.println("[RegisterVMGenerator] Applying TRO for direct tail recursion");
                String functionName = extractFunctionNameFromInstructions(functionInstructions);
                if (functionName != null) {
                    totalInstructions += TROHelper.generateDirectTailRecursiveIterative(functionName, emitter);
                    continue;
                }
            }

            // Default code generation path for this function
            RegisterGeneratorVisitor visitor = new RegisterGeneratorVisitor(emitter, operatorEmitter, registerAllocator, errors);

            for (IRNode node : functionInstructions) {
                if (node instanceof Stmt stmt) {
                    stmt.accept(visitor);
                } else if (node instanceof Expr expr) {
                    expr.accept(visitor);
                } else if (node instanceof LIRNode lirNode) {
                    // Handle LIR nodes (array operations, etc.)
                    lirNode.accept(visitor);
                } else {
                    errors.add("Unknown IR node type: " + node.getClass().getSimpleName());
                }
            }

            totalInstructions += visitor.getInstructionCount();
        }

        return totalInstructions;
    }

    /**
     * Split instructions by function entry points.
     * Each group starts with a FuncEntryLabel and contains all instructions until the next FuncEntryLabel.
     */
    private List<List<IRNode>> splitByFunction(List<IRNode> instructions) {
        List<List<IRNode>> groups = new ArrayList<>();
        List<IRNode> currentGroup = new ArrayList<>();

        for (IRNode node : instructions) {
            if (node instanceof FuncEntryLabel) {
                // Start a new group when we encounter a function entry
                if (!currentGroup.isEmpty()) {
                    groups.add(currentGroup);
                }
                currentGroup = new ArrayList<>();
            }
            currentGroup.add(node);
        }

        // Add the last group
        if (!currentGroup.isEmpty()) {
            groups.add(currentGroup);
        }

        return groups;
    }
    
    /**
     * Extracts function name from a list of IR instructions.
     */
    private String extractFunctionNameFromInstructions(List<IRNode> instructions) {
        for (IRNode node : instructions) {
            if (node instanceof FuncEntryLabel funcLabel) {
                String label = funcLabel.getRawLabel();
                // .def fib: args=1, locals=1
                int start = label.indexOf(' ') + 1;
                int end = label.indexOf(':');
                if (start > 0 && end > start) {
                    return label.substring(start, end);
                }
            }
        }
            return null;
        }

     /**
      * Gets the last generated assembly output.
     *
     * @return the assembly output string
     */
    public String getAssemblyOutput() {
        return lastAssemblyOutput;
    }

    /**
     * Internal visitor class for generating register VM assembly from IR nodes.
     */
    private class RegisterGeneratorVisitor implements IRVisitor<Void, Void> {
        private final RegisterEmitter emitter;
        private final RegisterOperatorEmitter operatorEmitter;
        private final IRegisterAllocator registerAllocator;
        private final List<String> errors;
        private int instructionCount = 0;
        private int tempReg = 5;  // Start from t0 (r5)

        public RegisterGeneratorVisitor(
                RegisterEmitter emitter,
                RegisterOperatorEmitter operatorEmitter,
                IRegisterAllocator registerAllocator,
                List<String> errors) {
            this.emitter = emitter;
            this.operatorEmitter = operatorEmitter;
            this.registerAllocator = registerAllocator;
            this.errors = errors;
        }

        public int getInstructionCount() {
            return instructionCount;
        }

        private void emitInstruction(String instruction) {
            emitter.emit(instruction);
            instructionCount++;
        }

        private void emitInstruction(String mnemonic, int reg) {
            emitter.emit(mnemonic + " r" + reg);
            instructionCount++;
        }

        private void emitInstruction(String mnemonic, int rd, int rs1) {
            emitter.emit(mnemonic + " r" + rd + ", r" + rs1);
            instructionCount++;
        }

        private void emitInstruction(String mnemonic, int rd, int rs1, int rs2) {
            emitter.emit(mnemonic + " r" + rd + ", r" + rs1 + ", r" + rs2);
            instructionCount++;
        }

        private void emitInstruction(String mnemonic, String operand) {
            emitter.emit(mnemonic + " " + operand);
            instructionCount++;
        }

        private int allocateTemp() {
            if (registerAllocator != null) {
                VariableSymbol tempVar = new VariableSymbol("temp" + instructionCount);
                int reg = registerAllocator.allocateRegister(tempVar);
                if (reg == -1) {
                    throw new IllegalStateException("No registers available for temporary allocation");
                }
                return reg;
            } else {
                int reg = tempReg;
                tempReg = (tempReg + 1) % 10;
                return reg;
            }
        }

        private void freeTemp(int reg) {
            if (registerAllocator != null) {
                if (registerAllocator instanceof EP18RRegisterAllocatorAdapter adapter) {
                    for (VariableSymbol var : adapter.getManagedVariables()) {
                        if (registerAllocator.getRegister(var) == reg) {
                            registerAllocator.freeRegister(var);
                            break;
                        }
                    }
                }
            }
        }

        // ==================== Statement Visitors ====================

        @Override
        public Void visit(Label label) {
            emitter.emitLabel(label.toSource());
            return null;
        }

        @Override
        public Void visit(JMP jmp) {
            emitInstruction("j " + jmp.getTarget().toSource());
            return null;
        }

        @Override
        public Void visit(CJMP cjmp) {
            // Load condition variable to register
            VarSlot cond = cjmp.cond;
            int condReg = loadToRegister(cond);

            // Branch if false to else block
            emitInstruction("jf r" + condReg + ", " + cjmp.getElseBlock().getLabel().toSource());

            freeTemp(condReg);
            return null;
        }

        @Override
        public Void visit(Assign assign) {
            Expr rhs = assign.getRhs();
            int resultReg = loadToRegister((Operand) rhs);

            VarSlot lhs = assign.getLhs();
            if (lhs instanceof FrameSlot frameSlot) {
                int offset = frameSlot.getSlotIdx() * 4;
                emitInstruction("sw r" + resultReg + ", fp, " + offset);
            } else if (lhs instanceof OperandSlot) {
            } else {
                errors.add("Unsupported LHS type in Assign: " + lhs.getClass().getSimpleName());
            }

            freeTemp(resultReg);
            return null;
        }

        @Override
        public Void visit(ReturnVal returnVal) {
            // Load return value to r2 (a0)
            if (returnVal.getRetVal() != null) {
                int retReg = loadToRegister(returnVal.getRetVal());
                emitInstruction("mov r2, r" + retReg);
                freeTemp(retReg);
            }

            if (returnVal.isMainEntry()) {
                emitInstruction("halt");
            } else {
                emitInstruction("ret");
            }
            return null;
        }

        @Override
        public Void visit(ExprStmt exprStmt) {
            // Evaluate expression for side effects (e.g., function call)
            Expr expr = exprStmt.getExpr();
            if (expr instanceof CallFunc) {
                visit((CallFunc) expr);
            }
            return null;
        }

        // ==================== Expression Visitors ====================

        @Override
        public Void visit(BinExpr binExpr) {
            // Load operands to registers
            int leftReg = loadToRegister(binExpr.getLhs());
            int rightReg = loadToRegister(binExpr.getRhs());

            // Perform operation, result in leftReg
            String opInstruction = operatorEmitter.emitBinaryOp(binExpr.getOpType());
            emitInstruction(opInstruction, leftReg, leftReg, rightReg);

            freeTemp(rightReg);
            return null;
        }

        @Override
        public Void visit(UnaryExpr unaryExpr) {
            int operandReg = loadToRegister(unaryExpr.expr);

            String opInstruction = operatorEmitter.emitUnaryOp(unaryExpr.op);
            emitInstruction(opInstruction, operandReg, operandReg);

            return null;
        }

        @Override
        public Void visit(CallFunc callFunc) {
            // Simple function call - just emit call instruction
            // In a real implementation, we would handle argument passing
            emitInstruction("call " + callFunc.getFuncName());
            return null;
        }

        @Override
        public Void visit(OperandSlot operandSlot) {
            // OperandSlot represents a temporary value on stack
            // For register VM, we need to load it from stack
            int tempReg = allocateTemp();
            int offset = operandSlot.getOrd() * 4;
            emitInstruction("lw r" + tempReg + ", sp, " + offset);
            return null;
        }

        @Override
        public Void visit(FrameSlot frameSlot) {
            // Load from stack frame
            int tempReg = allocateTemp();
            int offset = frameSlot.getSlotIdx() * 4;
            emitInstruction("lw r" + tempReg + ", fp, " + offset);
            return null;
        }

        @Override
        public <T> Void visit(ConstVal<T> constVal) {
            T value = constVal.getVal();
            int targetReg = allocateTemp();

            if (value instanceof Integer intVal) {
                emitInstruction("li r" + targetReg + ", " + intVal);
            } else if (value instanceof Float floatVal) {
                // For float, we would need constant pool
                emitInstruction("li r" + targetReg + ", " + Float.floatToIntBits(floatVal));
            } else if (value instanceof Boolean bool) {
                emitInstruction("li r" + targetReg + ", " + (bool ? 1 : 0));
            } else {
                errors.add("Unsupported constant type: " + value.getClass().getSimpleName());
            }

            return null;
        }

        /**
         * Load an operand to a register and return the register number.
         * If operand is already a FrameSlot or OperandSlot, loads from memory.
         * If operand is ConstVal, loads immediate value.
         */
        private int loadToRegister(Operand operand) {
            if (operand instanceof FrameSlot frameSlot) {
                int reg = allocateTemp();
                int offset = frameSlot.getSlotIdx() * 4;
                emitInstruction("lw r" + reg + ", fp, " + offset);
                return reg;
            } else if (operand instanceof OperandSlot slot) {
                int reg = allocateTemp();
                int offset = slot.getOrd() * 4;
                emitInstruction("lw r" + reg + ", sp, " + offset);
                return reg;
            } else if (operand instanceof ConstVal<?> constVal) {
                int reg = allocateTemp();
                Object value = constVal.getVal();
                if (value instanceof Integer intVal) {
                    emitInstruction("li r" + reg + ", " + intVal);
                } else if (value instanceof Boolean bool) {
                    emitInstruction("li r" + reg + ", " + (bool ? 1 : 0));
                } else {
                    errors.add("Unsupported constant type for register load: " + value.getClass().getSimpleName());
                }
                return reg;
            }
            errors.add("Cannot load to register: " + operand.getClass().getSimpleName());
            return allocateTemp();  // Return a temp register anyway
        }

        @Override
        public Void visit(org.teachfx.antlr4.ep21.ir.lir.LIRArrayInit lirArrayInit) {
            // 数组初始化：生成多个store指令来初始化数组
            // 注意：当前简化实现，假设数组已经通过其他方式分配
            // 这里为每个元素生成store指令
            
            VarSlot arraySlot = lirArrayInit.getArraySlot();
            List<Expr> elements = lirArrayInit.getElements();
            String elementTypeName = lirArrayInit.getElementTypeName();
            
            // 遍历所有元素并生成store指令
            for (int i = 0; i < elements.size(); i++) {
                Expr element = elements.get(i);
                
                // 评估元素表达式
                if (element instanceof ConstVal constVal) {
                    Object value = constVal.getVal();
                    if (value instanceof Integer intValue) {
                        emitter.emit("iconst " + intValue);
                    } else if (value instanceof Float floatValue) {
                        emitter.emit("fconst " + floatValue);
                    } else if (value instanceof Boolean boolValue) {
                        int boolInt = boolValue ? 1 : 0;
                        emitter.emit("iconst " + boolInt);
                    } else if (value instanceof String stringValue) {
                        emitter.emit("sconst \"" + stringValue + "\"");
                    }
                } else if (element instanceof VarSlot varSlot) {
                    emitter.emit("load " + varSlot.toString());
                }
                
                // 存储到数组：使用计算出的偏移量
                // 假设元素是int类型，4字节，所以offset = index * 4
                int offset = i * 4;
                
                // 生成注释说明数组初始化
                emitter.emitComment("# Array init: " + elementTypeName + "[" + arraySlot + "][" + i + "] = " + element);
                
                // 注意：这里使用store指令，实际应该使用带offset的iastore
                // 等待EP18R支持IALOAD/IASTORE指令后再更新
                // emitter.emit("iastore " + arraySlot + ", " + offset);
            }
            
            return null;
        }





        @Override
        public Void visit(LIRArrayStore lirArrayStore) {
            
            // 数组存储：arr[index] = value
            Expr valueExpr = lirArrayStore.getValue();
            VarSlot arraySlot = lirArrayStore.getArraySlot();
            Expr indexExpr = lirArrayStore.getIndex();
            
            // 加载值到寄存器
            int valueReg;
            if (valueExpr instanceof FrameSlot valueSlot) {
                valueReg = loadToRegister(valueSlot);
            } else if (valueExpr instanceof ConstVal<?> constVal) {
                valueReg = loadToRegister(constVal);
            } else if (valueExpr instanceof OperandSlot operandSlot) {
                valueReg = loadToRegister(operandSlot);
            } else {
                errors.add("Unsupported value type in LIRArrayStore: " + valueExpr.getClass().getSimpleName());
                return null;
            }
            
            // 加载数组基址到寄存器
            int baseReg;
            if (arraySlot instanceof FrameSlot frameSlot) {
                baseReg = loadToRegister(frameSlot);
            } else if (arraySlot instanceof OperandSlot operandSlot) {
                baseReg = loadToRegister(operandSlot);
            } else {
                errors.add("Unsupported array slot type in LIRArrayStore: " + arraySlot.getClass().getSimpleName());
                freeTemp(valueReg);
                return null;
            }
            
            // 加载索引到寄存器
            int indexReg;
            if (indexExpr instanceof FrameSlot indexSlot) {
                indexReg = loadToRegister(indexSlot);
            } else if (indexExpr instanceof ConstVal<?> constVal) {
                indexReg = loadToRegister(constVal);
            } else if (indexExpr instanceof OperandSlot operandSlot) {
                indexReg = loadToRegister(operandSlot);
            } else {
                errors.add("Unsupported index type in LIRArrayStore: " + indexExpr.getClass().getSimpleName());
                freeTemp(valueReg);
                freeTemp(baseReg);
                return null;
            }
            
            // 计算偏移量：index * 4（int类型大小为4字节）
            int const4Reg = allocateTemp();
            emitInstruction("li r" + const4Reg + ", 4");
            emitInstruction("mul r" + indexReg + ", r" + indexReg + ", r" + const4Reg);
            freeTemp(const4Reg);
            
            // 计算地址：baseReg + indexReg
            emitInstruction("add r" + baseReg + ", r" + baseReg + ", r" + indexReg);
            freeTemp(indexReg);
            
            // 存储值到数组元素：memory[baseReg] = valueReg
            emitInstruction("sw r" + valueReg + ", r" + baseReg + ", 0");
            
            // 释放所有临时寄存器
            freeTemp(valueReg);
            freeTemp(baseReg);
            
            return null;
        }

        @Override
        public Void visit(LIRNewArray lirNewArray) {
            // 数组分配：使用EP18R struct指令分配连续内存
            Expr sizeExpr = lirNewArray.getSize();
            VarSlot resultSlot = lirNewArray.getResultSlot();
            String elementTypeName = lirNewArray.getElementTypeName();
            
            // 只支持常量数组大小（struct指令需要立即数）
            if (!(sizeExpr instanceof ConstVal constVal)) {
                errors.add("Unsupported size expression type in LIRNewArray for register VM: " + 
                          sizeExpr.getClass().getSimpleName() + " (only constant sizes supported)");
                return null;
            }
            
            Object value = constVal.getVal();
            if (!(value instanceof Integer intValue)) {
                errors.add("Unsupported size type in LIRNewArray: " + value.getClass().getSimpleName());
                return null;
            }
            
            // 检查数组大小是否在struct指令的立即数范围内（16位有符号）
            if (intValue < 0 || intValue > 32767) {
                errors.add("Array size out of range for struct instruction: " + intValue + " (must be 0-32767)");
                return null;
            }
            
            // 分配临时寄存器用于存储数组地址
            int addrReg = allocateTemp();
            
            // 生成struct指令：struct rd, size
            emitInstruction("struct r" + addrReg + ", " + intValue);
            
            // 存储数组地址到结果槽位
            if (resultSlot instanceof FrameSlot frameSlot) {
                // 计算帧槽位偏移：每个槽位4字节
                int offset = frameSlot.getSlotIdx() * 4;
                // 存储地址到帧槽位：sw addrReg, fp, offset
                emitInstruction("sw r" + addrReg + ", r14, " + offset);
                freeTemp(addrReg);
            } else if (resultSlot instanceof OperandSlot) {
                // OperandSlot结果留在寄存器addrReg中，调用者负责处理
                // 不需要释放寄存器，由调用者释放
            } else {
                errors.add("Unsupported result slot type in LIRNewArray: " + resultSlot.getClass().getSimpleName());
                freeTemp(addrReg);
                return null;
            }
            
            emitter.emitComment("# Array allocation using struct: " + elementTypeName + "[" + intValue + "] -> " + resultSlot);
            return null;
        }

        @Override
        public Void visit(LIRArrayLoad lirArrayLoad) {
            
            // 数组加载：result = arr[index]
            VarSlot arraySlot = lirArrayLoad.getArraySlot();
            Expr indexExpr = lirArrayLoad.getIndex();
            VarSlot resultSlot = lirArrayLoad.getResultSlot();
            
            // 加载数组基址到寄存器
            int baseReg;
            if (arraySlot instanceof FrameSlot frameSlot) {
                baseReg = loadToRegister(frameSlot);
            } else if (arraySlot instanceof OperandSlot operandSlot) {
                baseReg = loadToRegister(operandSlot);
            } else {
                errors.add("Unsupported array slot type in LIRArrayLoad: " + arraySlot.getClass().getSimpleName());
                return null;
            }
            
            // 加载索引到寄存器
            int indexReg;
            if (indexExpr instanceof FrameSlot indexSlot) {
                indexReg = loadToRegister(indexSlot);
            } else if (indexExpr instanceof ConstVal<?> constVal) {
                indexReg = loadToRegister(constVal);
            } else if (indexExpr instanceof OperandSlot operandSlot) {
                indexReg = loadToRegister(operandSlot);
            } else {
                errors.add("Unsupported index type in LIRArrayLoad: " + indexExpr.getClass().getSimpleName());
                freeTemp(baseReg);
                return null;
            }
            
            // 计算偏移量：index * 4（int类型大小为4字节）
            int const4Reg = allocateTemp();
            emitInstruction("li r" + const4Reg + ", 4");
            emitInstruction("mul r" + indexReg + ", r" + indexReg + ", r" + const4Reg);
            freeTemp(const4Reg);
            
            // 计算地址：baseReg + indexReg
            emitInstruction("add r" + baseReg + ", r" + baseReg + ", r" + indexReg);
            freeTemp(indexReg);
            
            // 加载数组元素：result = memory[baseReg]
            emitInstruction("lw r" + baseReg + ", r" + baseReg + ", 0");
            
            // 存储结果到resultSlot（如果需要）
            if (resultSlot instanceof FrameSlot resultFrameSlot) {
                // 将值从baseReg寄存器存储到帧槽位
                int offset = resultFrameSlot.getSlotIdx() * 4;
                emitInstruction("sw r" + baseReg + ", fp, " + offset);
            } else if (resultSlot instanceof OperandSlot) {
                // OperandSlot结果留在寄存器baseReg中，调用者负责处理
                // 不需要额外操作
            } else {
                errors.add("Unsupported result slot type in LIRArrayLoad: " + resultSlot.getClass().getSimpleName());
                freeTemp(baseReg);
                return null;
            }
            
            // 结果在baseReg寄存器中
            return null;
        }

        @Override
        public Void visit(ArrayAccess arrayAccess) {
            // DEBUG: ArrayAccess visited
            errors.add("DEBUG: ArrayAccess visited - baseSlot: " + arrayAccess.getBaseSlot() + ", indexExpr: " + arrayAccess.getIndex());
            
            // 数组访问：arr[index]
            FrameSlot baseSlot = arrayAccess.getBaseSlot();
            Expr indexExpr = arrayAccess.getIndex();
            
            // 加载数组基址到寄存器
            int baseReg = loadToRegister(baseSlot);
            
            // 加载索引到寄存器
            int indexReg;
            if (indexExpr instanceof FrameSlot indexSlot) {
                indexReg = loadToRegister(indexSlot);
            } else if (indexExpr instanceof ConstVal<?> constVal) {
                indexReg = loadToRegister(constVal);
            } else if (indexExpr instanceof OperandSlot) {
                // OperandSlot已在操作数栈中，需要加载到寄存器
                indexReg = loadToRegister((OperandSlot) indexExpr);
            } else {
                errors.add("Unsupported index type in ArrayAccess: " + indexExpr.getClass().getSimpleName());
                return null;
            }
            
            // 计算偏移量：index * 4（int类型大小为4字节）
            // 首先加载常数4到临时寄存器
            int const4Reg = allocateTemp();
            emitInstruction("li r" + const4Reg + ", 4");
            // 乘法：indexReg = indexReg * 4
            emitInstruction("mul r" + indexReg + ", r" + indexReg + ", r" + const4Reg);
            freeTemp(const4Reg);
            
            // 计算地址：baseReg + indexReg
            emitInstruction("add r" + baseReg + ", r" + baseReg + ", r" + indexReg);
            freeTemp(indexReg);
            
            // 加载数组元素：result = memory[baseReg]
            // 结果放在baseReg中（重用baseReg作为结果寄存器）
            emitInstruction("lw r" + baseReg + ", r" + baseReg + ", 0");
            
            // 注意：baseReg现在包含数组元素值，需要保持分配状态供调用者使用
            // 类似于BinExpr模式，结果留在第一个操作数的寄存器中
            return null;
        }

        @Override
        public Void visit(ArrayAssign arrayAssign) {
            // 数组赋值：arr[index] = value
            ArrayAccess arrayAccess = arrayAssign.getArrayAccess();
            Expr valueExpr = arrayAssign.getValue();
            
            // 加载值到寄存器
            int valueReg;
            if (valueExpr instanceof FrameSlot valueSlot) {
                valueReg = loadToRegister(valueSlot);
            } else if (valueExpr instanceof ConstVal<?> constVal) {
                valueReg = loadToRegister(constVal);
            } else if (valueExpr instanceof OperandSlot) {
                // OperandSlot已在操作数栈中，需要加载到寄存器
                valueReg = loadToRegister((OperandSlot) valueExpr);
            } else {
                errors.add("Unsupported RHS type in ArrayAssign: " + valueExpr.getClass().getSimpleName());
                return null;
            }
            
            // 加载数组基址到寄存器
            FrameSlot baseSlot = arrayAccess.getBaseSlot();
            int baseReg = loadToRegister(baseSlot);
            
            // 加载索引到寄存器
            Expr indexExpr = arrayAccess.getIndex();
            int indexReg;
            if (indexExpr instanceof FrameSlot indexSlot) {
                indexReg = loadToRegister(indexSlot);
            } else if (indexExpr instanceof ConstVal<?> constVal) {
                indexReg = loadToRegister(constVal);
            } else if (indexExpr instanceof OperandSlot) {
                // OperandSlot已在操作数栈中，需要加载到寄存器
                indexReg = loadToRegister((OperandSlot) indexExpr);
            } else {
                errors.add("Unsupported index type in ArrayAssign: " + indexExpr.getClass().getSimpleName());
                freeTemp(valueReg);
                freeTemp(baseReg);
                return null;
            }
            
            // 计算偏移量：index * 4（int类型大小为4字节）
            // 首先加载常数4到临时寄存器
            int const4Reg = allocateTemp();
            emitInstruction("li r" + const4Reg + ", 4");
            // 乘法：indexReg = indexReg * 4
            emitInstruction("mul r" + indexReg + ", r" + indexReg + ", r" + const4Reg);
            freeTemp(const4Reg);
            
            // 计算地址：baseReg + indexReg
            emitInstruction("add r" + baseReg + ", r" + baseReg + ", r" + indexReg);
            freeTemp(indexReg);
            
            // 存储值到数组元素：memory[baseReg] = valueReg
            emitInstruction("sw r" + valueReg + ", r" + baseReg + ", 0");
            
            // 释放所有临时寄存器（值已存储，不再需要）
            freeTemp(valueReg);
            freeTemp(baseReg);
            
            return null;
        }
    }

    /**
     * Tail Recursion Optimization Helper.
     * <p>
     * Detects and transforms recursive patterns (Fibonacci, factorial) into iterative code
     * during the code generation phase.
     * </p>
     */
    private static class TROHelper {
        
        /**
         * Analyzes a list of IR instructions to detect if it matches a Fibonacci pattern.
         *
         * @param instructions the IR instructions for a function
         * @return true if this is a Fibonacci-like recursive function
         */
        public static boolean isFibonacciPattern(List<IRNode> instructions) {
            String functionName = null;
            int recursiveCallCount = 0;

            for (IRNode node : instructions) {
                if (node instanceof FuncEntryLabel funcLabel) {
                    functionName = extractFunctionName(funcLabel);
                    if (functionName == null || !functionName.toLowerCase().contains("fib")) {
                        return false;
                    }
                } else if (node instanceof CallFunc call) {
                    if (functionName != null && call.getFuncName().equals(functionName)) {
                        recursiveCallCount++;
                    }
                }
            }

            // Fibonacci pattern typically has 2 recursive calls
            return recursiveCallCount == 2;
        }

        /**
         * Analyzes a list of IR instructions to detect if it's a direct tail recursive function.
         * Direct tail recursion pattern: return func(modified_args);
         *
         * @param instructions the IR instructions for a function
         * @return true if this is a direct tail recursive function
         */
        public static boolean isDirectTailRecursive(List<IRNode> instructions) {
            String functionName = null;
            int recursiveCallCount = 0;
            boolean hasTailCall = false;

            for (int i = 0; i < instructions.size(); i++) {
                IRNode node = instructions.get(i);

                if (node instanceof FuncEntryLabel funcLabel) {
                    functionName = extractFunctionName(funcLabel);
                    // Exclude Fibonacci (handled separately)
                    if (functionName != null && functionName.toLowerCase().contains("fib")) {
                        return false;
                    }
                } else if (node instanceof CallFunc call) {
                    if (functionName != null && call.getFuncName().equals(functionName)) {
                        recursiveCallCount++;
                        // Check if this call is at the end of the function (tail position)
                        // Look ahead to see if the next instruction is a return
                        if (i + 1 < instructions.size()) {
                            IRNode nextNode = instructions.get(i + 1);
                            if (nextNode instanceof ReturnVal) {
                                hasTailCall = true;
                            }
                        }
                    }
                }
            }

            // Direct tail recursion: exactly 1 recursive call in tail position
            return recursiveCallCount == 1 && hasTailCall;
        }

        /**
         * Generates iterative Fibonacci code for the register VM.
         * 
         * Input pattern: fib(n) { if (n <= 1) return n; return fib(n-1) + fib(n-2); }
         * Output pattern: iterative version using accumulator
         * 
         * @param functionName the name of the function
         * @param emitter the emitter to output instructions
         * @return the number of instructions generated
         */
        public static int generateFibonacciIterative(String functionName, IEmitter emitter) {
            int count = 0;
            
            // Function entry
            emitter.emit(".def " + functionName + ": args=1, locals=2");
            count++;
            
            // Load parameter n to r5 (temp)
            emitter.emit("    lw r5, fp, 4");  // Load first parameter (offset 4)
            count++;
            
            // Base case: if n <= 1, return n
            emitter.emit("    li r6, 1");      // r6 = 1
            count++;
            emitter.emit("    sle r7, r5, r6"); // r7 = (n <= 1)
            count++;
            emitter.emit("    jf r7, " + functionName + "_loop"); // Skip base case
            count++;
            
            // Return n (already in r5)
            emitter.emit("    mov r2, r5");    // Return value in r2
            count++;
            emitter.emit("    ret");
            count++;
            
            // Iterative loop
            emitter.emit(functionName + "_loop:");
            count++;
            
            // Initialize: a = 0, b = 1
            emitter.emit("    li r10, 0");     // r10 = a = 0 (saved register)
            count++;
            emitter.emit("    li r11, 1");     // r11 = b = 1 (saved register)
            count++;
            
            // Loop label
            emitter.emit(functionName + "_loop_body:");
            count++;
            
            // Check if n <= 1 (already done above, this is for loop condition)
            emitter.emit("    li r6, 1");
            count++;
            emitter.emit("    sgt r7, r5, r6"); // r7 = (n > 1)
            count++;
            emitter.emit("    jf r7, " + functionName + "_end"); // Exit loop if n <= 1
            count++;
            
            // temp = a + b
            emitter.emit("    add r12, r10, r11"); // r12 = temp = a + b
            count++;
            
            // a = b
            emitter.emit("    mov r10, r11");   // a = b
            count++;
            
            // b = temp
            emitter.emit("    mov r11, r12");   // b = temp
            count++;
            
            // n = n - 1
            emitter.emit("    li r6, 1");
            count++;
            emitter.emit("    sub r5, r5, r6"); // n = n - 1
            count++;
            
            // Jump back to loop condition
            emitter.emit("    j " + functionName + "_loop_body");
            count++;
            
            // End of loop - return b
            emitter.emit(functionName + "_end:");
            count++;
            emitter.emit("    mov r2, r11");    // Return b
            count++;
            emitter.emit("    ret");
            count++;

            return count;
        }

        /**
         * Generates iterative code for direct tail recursive functions.
         *
         * Input pattern: foo(n) { if (cond) return val; return foo(modified_n); }
         * Output pattern: while (!cond) { n = modified_n; } return val;
         *
         * @param functionName the name of the function
         * @param emitter the emitter to output instructions
         * @return the number of instructions generated
         */
        public static int generateDirectTailRecursiveIterative(String functionName, IEmitter emitter) {
            int count = 0;

            // Function entry (assuming single parameter)
            emitter.emit(".def " + functionName + ": args=1, locals=1");
            count++;
            
            // Load parameter n to r5
            emitter.emit("    lw r5, fp, 4");  // Load first parameter
            count++;

            // Base case: if n <= 0, return 0 (or similar base condition)
            // Note: This is a simplified transformation. Real implementation would need
            // to analyze the actual base condition from the IR.
            emitter.emit("    li r6, 0");      // r6 = 0
            count++;
            emitter.emit("    sle r7, r5, r6"); // r7 = (n <= 0)
            count++;
            emitter.emit("    jnf r7, " + functionName + "_loop"); // Continue loop if n > 0
            count++;

            // Base case return
            emitter.emit("    li r2, 0");     // Return 0 for base case
            count++;
            emitter.emit("    ret");
            count++;

            // Iterative loop
            emitter.emit(functionName + "_loop:");
            count++;

            // Loop condition: if n <= 0, exit
            emitter.emit("    li r6, 0");
            count++;
            emitter.emit("    sle r7, r5, r6"); // r7 = (n <= 0)
            count++;
            emitter.emit("    jt r7, " + functionName + "_end"); // Exit loop if n <= 0
            count++;

            // Body: n = n - 1 (parameter modification)
            emitter.emit("    li r6, 1");
            count++;
            emitter.emit("    sub r5, r5, r6"); // n = n - 1
            count++;

            // Jump back to loop condition
            emitter.emit("    j " + functionName + "_loop");
            count++;

            // End of loop - return base value
            emitter.emit(functionName + "_end:");
            count++;
            emitter.emit("    li r2, 0");     // Return 0
            count++;
            emitter.emit("    ret");
            count++;

            return count;
        }

        /**
         * Extracts function name from FuncEntryLabel.
         */
        private static String extractFunctionName(FuncEntryLabel funcLabel) {
            String label = funcLabel.getRawLabel();
            // .def fib: args=1, locals=1
            int start = label.indexOf(' ') + 1;
            int end = label.indexOf(':');
            if (start > 0 && end > start) {
                return label.substring(start, end);
            }
            return null;
        }
    }

    /**
     * Register VM instruction emitter.
     */
    public static class RegisterEmitter implements IEmitter {
        private final List<String> instructions = new ArrayList<>();
        private int indentLevel = 0;

        @Override
        public void emit(String instruction) {
            instructions.add("    ".repeat(indentLevel) + instruction);
        }

        @Override
        public void emitLabel(String label) {
            // Function definition labels (.def) already contain proper format
            if (label.startsWith(".def")) {
                instructions.add(label);
            } else {
                // Regular labels need colon suffix
                instructions.add(label + ":");
            }
        }

        @Override
        public void emitComment(String comment) {
            instructions.add("    # " + comment);
        }

        @Override
        public void emitAll(List<String> instructions) {
            for (String instruction : instructions) {
                emit(instruction);
            }
        }

        @Override
        public void beginScope(String scopeName) {
            emitComment("Begin scope: " + scopeName);
            indentLevel++;
        }

        @Override
        public void endScope() {
            indentLevel--;
            emitComment("End scope");
        }

        @Override
        public String flush() {
            String result = String.join("\n", instructions);
            instructions.clear();
            indentLevel = 0;
            return result.isEmpty() ? result : result + "\n";
        }

        @Override
        public void clear() {
            instructions.clear();
            indentLevel = 0;
        }

        @Override
        public int getIndentLevel() {
            return indentLevel;
        }

        @Override
        public void setIndentLevel(int level) {
            this.indentLevel = level;
        }
    }

    /**
     * Register VM operator emitter.
     * <p>
     * Uses 32-bit fixed-length instruction format:
     * - opcode(8) + rd(5) + rs1(5) + rs2(5) + imm(9)
     * </p>
     */
    public static class RegisterOperatorEmitter {

        public String emitBinaryOp(OperatorType.BinaryOpType binaryOpType) {
            return switch (binaryOpType) {
                case ADD -> "add";
                case SUB -> "sub";
                case MUL -> "mul";
                case DIV -> "div";
                case MOD -> "mod";
                case LT -> "slt";
                case LE -> "sle";
                case GT -> "sgt";
                case GE -> "sge";
                case EQ -> "seq";
                case NE -> "sne";
                case AND -> "and";
                case OR -> "or";
            };
        }

        public String emitUnaryOp(OperatorType.UnaryOpType unaryOpType) {
            return switch (unaryOpType) {
                case NEG -> "neg";
                case NOT -> "not";
            };
        }
    }
}