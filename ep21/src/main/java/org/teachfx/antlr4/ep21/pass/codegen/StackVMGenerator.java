package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep18.stackvm.codegen.BytecodeDefinitionInterface;
import org.teachfx.antlr4.ep18.stackvm.codegen.BytecodeDefinitionInterface.DefaultBytecodeDefinition;
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
import org.teachfx.antlr4.ep21.symtab.type.OperatorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Stack VM code generator for EP18.
 * <p>
 * This class implements the {@link ICodeGenerator} interface to generate
 * bytecode for the EP18 stack-based virtual machine. It translates IR nodes
 * to EP18 assembly instructions using the visitor pattern.
 * </p>
 */
public class StackVMGenerator implements ICodeGenerator {

    /**
     * Target VM identifier for EP18 stack VM.
     */
    public static final String TARGET_VM = "EP18";

    private final BytecodeDefinitionInterface bytecodeDef;
    private final IEmitter emitter;
    private final IOperatorEmitter operatorEmitter;
    private String lastAssemblyOutput = "";

    /**
     * Creates a new StackVMGenerator with default emitters.
     */
    public StackVMGenerator() {
        this(new StackVMEmitter(), new StackVMOperatorEmitter());
    }

    /**
     * Creates a new StackVMGenerator with a custom emitter.
     *
     * @param emitter the custom instruction emitter
     */
    public StackVMGenerator(IEmitter emitter) {
        this(emitter, new StackVMOperatorEmitter());
    }

    /**
     * Creates a new StackVMGenerator with custom emitter and operator emitter.
     *
     * @param emitter the custom instruction emitter
     * @param operatorEmitter the custom operator emitter
     */
    public StackVMGenerator(IEmitter emitter, IOperatorEmitter operatorEmitter) {
        this.bytecodeDef = new DefaultBytecodeDefinition();
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

    /**
     * Generates bytecode from a list of IR instructions.
     * <p>
     * This method is provided for backward compatibility with existing tests.
     * </p>
     *
     * @param instructions the list of IR instructions
     */
    public void generateFrom(List<IRNode> instructions) {
        emitter.clear();
        List<String> errors = new ArrayList<>();
        generateInstructions(instructions, errors);
        lastAssemblyOutput = emitter.flush();
    }

    /**
     * Gets the last generated assembly output.
     *
     * @return the assembly output string
     */
    public String getAssemblyOutput() {
        return lastAssemblyOutput;
    }

    @Override
    public String getTargetVM() {
        return TARGET_VM;
    }

    @Override
    public void configure(Map<String, Object> config) {
        if (config.containsKey("emitter")) {
            Object emitterObj = config.get("emitter");
            if (emitterObj instanceof IEmitter) {
                // Can't reassign final field, but could use a wrapper
                // For now, this is a no-op
            }
        }
        if (config.containsKey("operatorEmitter")) {
            Object opEmitterObj = config.get("operatorEmitter");
            if (opEmitterObj instanceof IOperatorEmitter) {
                // Can't reassign final field, but could use a wrapper
                // For now, this is a no-op
            }
        }
    }

    @Override
    public IEmitter getEmitter() {
        return emitter;
    }

    /**
     * Generates instructions from a list of IR nodes.
     *
     * @param instructions the list of IR nodes
     * @param errors the list to collect errors
     * @return the number of instructions generated
     */
    private int generateInstructions(List<IRNode> instructions, List<String> errors) {
        IRGeneratorVisitor visitor = new IRGeneratorVisitor(bytecodeDef, emitter, operatorEmitter, errors);

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
     * Internal visitor class for generating stack VM instructions from IR nodes.
     */
    private static class IRGeneratorVisitor implements IRVisitor<Void, Void> {
        private final BytecodeDefinitionInterface bytecodeDef;
        private final IEmitter emitter;
        private final IOperatorEmitter operatorEmitter;
        private final List<String> errors;
        private int instructionCount = 0;

        public IRGeneratorVisitor(
                BytecodeDefinitionInterface bytecodeDef,
                IEmitter emitter,
                IOperatorEmitter operatorEmitter,
                List<String> errors) {
            this.bytecodeDef = bytecodeDef;
            this.emitter = emitter;
            this.operatorEmitter = operatorEmitter;
            this.errors = errors;
        }

        public int getInstructionCount() {
            return instructionCount;
        }

        private void emitInstruction(String mnemonic) {
            emitter.emit(mnemonic);
            instructionCount++;
        }

        private void emitInstructionWithOperand(String mnemonic, int operand) {
            emitter.emit(mnemonic + " " + operand);
            instructionCount++;
        }

        // ==================== Statement Visitors ====================

        @Override
        public Void visit(Label label) {
            emitter.emitLabel(label.toSource());
            return null;
        }

        @Override
        public Void visit(JMP jmp) {
            emitInstruction("br " + jmp.getTarget().toSource());
            return null;
        }

        @Override
        public Void visit(CJMP cjmp) {
            // Load condition variable onto stack
            VarSlot cond = cjmp.cond;
            if (cond instanceof FrameSlot frameSlot) {
                emitInstructionWithOperand("load", frameSlot.getSlotIdx());
            } else if (cond instanceof OperandSlot) {
                // OperandSlot is a temporary already on stack, no load needed
            } else {
                errors.add("Unsupported condition type in CJMP: " + cond.getClass().getSimpleName());
            }
            // Branch if false to else block
            emitInstruction("brf " + cjmp.getElseBlock().getLabel().toSource());
            return null;
        }

        @Override
        public Void visit(Assign assign) {
            Expr rhs = assign.getRhs();
            if (rhs instanceof VarSlot varSlot) {
                if (varSlot instanceof FrameSlot frameSlot) {
                    emitInstructionWithOperand("load", frameSlot.getSlotIdx());
                } else if (varSlot instanceof OperandSlot) {
                } else {
                    errors.add("Unsupported RHS type in Assign: " + rhs.getClass().getSimpleName());
                }
            } else if (rhs instanceof ConstVal<?> constVal) {
                emitConst(constVal);
            } else {
                errors.add("Unsupported RHS type in Assign: " + rhs.getClass().getSimpleName());
            }

            // Store to LHS
            VarSlot lhs = assign.getLhs();
            if (lhs instanceof FrameSlot frameSlot) {
                emitInstructionWithOperand("store", frameSlot.getSlotIdx());
            } else if (lhs instanceof OperandSlot) {
                // OperandSlot is a temporary on stack, no store needed
            } else {
                errors.add("Unsupported LHS type in Assign: " + lhs.getClass().getSimpleName());
            }

            return null;
        }

        @Override
        public Void visit(ReturnVal returnVal) {
            if (returnVal.getRetVal() != null) {
                if (returnVal.getRetVal() instanceof FrameSlot frameSlot) {
                    emitInstructionWithOperand("load", frameSlot.getSlotIdx());
                }
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
            // Evaluate expression for side effects
            if (exprStmt.getExpr() != null) {
                exprStmt.getExpr().accept(this);
            }
            return null;
        }

        // ==================== Expression Visitors ====================

        @Override
        public Void visit(BinExpr binExpr) {
            // Generate binary operator
            String opInstruction = operatorEmitter.emitBinaryOp(binExpr.getOpType());
            emitInstruction(opInstruction);
            return null;
        }

        @Override
        public Void visit(UnaryExpr unaryExpr) {
            // Generate unary operator
            String opInstruction = operatorEmitter.emitUnaryOp(unaryExpr.op);
            emitInstruction(opInstruction);
            return null;
        }

        @Override
        public Void visit(CallFunc callFunc) {
            // For function calls, we need to:
            // 1. Push arguments onto stack
            // 2. Emit call instruction
            emitInstructionWithOperand("call", 0); // Placeholder function index
            return null;
        }

        @Override
        public Void visit(OperandSlot operandSlot) {
            // OperandSlot represents a temporary value on stack
            // No action needed
            return null;
        }

        @Override
        public Void visit(FrameSlot frameSlot) {
            // Load from stack frame
            emitInstructionWithOperand("load", frameSlot.getSlotIdx());
            return null;
        }

        @Override
        public <T> Void visit(ConstVal<T> constVal) {
            emitConst(constVal);
            return null;
        }

        private <T> void emitConst(ConstVal<T> constVal) {
            T value = constVal.getVal();
            if (value instanceof Integer) {
                emitInstructionWithOperand("iconst", (Integer) value);
            } else if (value instanceof Float) {
                // For float constants, we need to use the constant pool
                emitInstructionWithOperand("fconst", 0); // Placeholder pool index
            } else if (value instanceof Boolean bool) {
                emitInstructionWithOperand("cconst", bool ? 1 : 0);
            } else if (value instanceof String) {
                emitInstructionWithOperand("sconst", 0); // Placeholder pool index
            } else {
                errors.add("Unsupported constant type: " + value.getClass().getSimpleName());
            }
        }

        @Override
        public Void visit(ArrayAccess arrayAccess) {
            // TODO: 实现数组访问作为右值
            errors.add("ArrayAccess not yet implemented for stack VM");
            return null;
        }

        @Override
        public Void visit(ArrayAssign arrayAssign) {
            // TODO: 实现数组赋值
            errors.add("ArrayAssign not yet implemented for stack VM");
            return null;
        }
    }

    /**
     * Default stack VM instruction emitter.
     */
    private static class StackVMEmitter implements IEmitter {
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
     * Default stack VM operator emitter.
     * <p>
     * Uses 32-bit fixed-length instruction format:
     * - opcode(8) + rd(5) + rs1(5) + rs2(5) + imm(9)
     * </p>
     */
    private static class StackVMOperatorEmitter implements IOperatorEmitter {

        @Override
        public String emitBinaryOp(OperatorType.BinaryOpType binaryOpType) {
            return switch (binaryOpType) {
                case ADD -> "iadd";
                case SUB -> "isub";
                case MUL -> "imul";
                case DIV -> "idiv";
                case MOD -> "imod";
                case LT -> "ilt";
                case LE -> "ile";
                case GT -> "igt";
                case GE -> "ige";
                case EQ -> "ieq";
                case NE -> "ine";
                case AND -> "iand";
                case OR -> "ior";
            };
        }

        @Override
        public String emitUnaryOp(OperatorType.UnaryOpType unaryOpType) {
            return switch (unaryOpType) {
                case NEG -> "ineg";
                case NOT -> "inot";
            };
        }
    }
}
