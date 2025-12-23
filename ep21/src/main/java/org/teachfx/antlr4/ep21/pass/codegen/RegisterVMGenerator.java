package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.IRVisitor;
import org.teachfx.antlr4.ep21.ir.Prog;
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
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

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
    private String lastAssemblyOutput = "";

    /**
     * Creates a new RegisterVMGenerator with default emitters.
     */
    public RegisterVMGenerator() {
        this(new RegisterEmitter(), new RegisterOperatorEmitter());
    }

    /**
     * Creates a new RegisterVMGenerator with a custom emitter.
     *
     * @param emitter the custom instruction emitter
     */
    public RegisterVMGenerator(RegisterEmitter emitter) {
        this(emitter, new RegisterOperatorEmitter());
    }

    /**
     * Creates a new RegisterVMGenerator with custom emitters.
     *
     * @param emitter the custom instruction emitter
     * @param operatorEmitter the custom operator emitter
     */
    public RegisterVMGenerator(RegisterEmitter emitter, RegisterOperatorEmitter operatorEmitter) {
        this.emitter = emitter;
        this.operatorEmitter = operatorEmitter;
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

    /**
     * Generates assembly from a list of IR instructions.
     *
     * @param instructions the list of IR nodes
     * @param errors the list to collect errors
     * @return the number of instructions generated
     */
    private int generateInstructions(List<IRNode> instructions, List<String> errors) {
        RegisterGeneratorVisitor visitor = new RegisterGeneratorVisitor(emitter, operatorEmitter, errors);

        for (IRNode node : instructions) {
            if (node instanceof Stmt stmt) {
                stmt.accept(visitor);
            } else if (node instanceof Expr expr) {
                expr.accept(visitor);
            } else {
                errors.add("Unknown IR node type: " + node.getClass().getSimpleName());
            }
        }

        return visitor.getInstructionCount();
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
        private final List<String> errors;
        private int instructionCount = 0;
        private int tempReg = 5;  // Start from t0 (r5)

        public RegisterGeneratorVisitor(
                RegisterEmitter emitter,
                RegisterOperatorEmitter operatorEmitter,
                List<String> errors) {
            this.emitter = emitter;
            this.operatorEmitter = operatorEmitter;
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
            int reg = tempReg;
            tempReg = (tempReg + 1) % 10;  // Cycle through r5-r9
            return reg;
        }

        private void freeTemp(int reg) {
            // In this simple allocator, we just cycle back
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
            // Generate RHS expression and store result to LHS
            Operand rhs = assign.getRhs();
            int resultReg = loadToRegister(rhs);

            // Store to LHS (frame slot or operand slot)
            VarSlot lhs = assign.getLhs();
            if (lhs instanceof FrameSlot frameSlot) {
                // Store to stack frame
                int offset = frameSlot.getSlotIdx() * 4;
                emitInstruction("sw r" + resultReg + ", fp, " + offset);
            } else if (lhs instanceof OperandSlot) {
                // OperandSlot is already on stack, no store needed for register VM
                // The result is already in resultReg which represents the value
                // For simplicity, we just keep the value in the register
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
            instructions.add(label + ":");
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
