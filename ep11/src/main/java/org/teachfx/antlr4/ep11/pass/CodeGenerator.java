package org.teachfx.antlr4.ep11.pass;

import org.teachfx.antlr4.ep11.ir.*;
import org.teachfx.antlr4.ep11.stackvm.*;

import java.util.*;

/**
 * CodeGenerator — translates IR (three-address code) into VM bytecode.
 *
 * Slot allocation convention:
 *   Slots [0 .. nargs-1]  = function parameters
 *   Slots [nargs .. N]    = local variables and temporaries
 *
 * The CALL instruction in the VM places arguments into the callee's
 * local-variable slots 0..nargs-1 before jumping.
 */
public class CodeGenerator {

    private final ByteCodeAssembler asm;
    private final List<IRFunction> irFunctions;

    public CodeGenerator(ByteCodeAssembler asm, List<IRFunction> irFunctions) {
        this.asm = asm;
        this.irFunctions = irFunctions;
    }

    /** Generate bytecode for all IR functions. Returns the assembler. */
    public ByteCodeAssembler generate() {
        // First pass: register all functions in constant pool with placeholder addresses
        for (IRFunction func : irFunctions) {
            asm.getFunctionIndex(func.name);
        }

        // Second pass: generate code for each function
        for (IRFunction func : irFunctions) {
            generateFunction(func);
        }

        return asm;
    }

    /**
     * Infer parameter names by looking at the symbol table or by heuristic:
     * the first N unique identifiers that appear in expressions but are never
     * assigned to (by AssignIR, BinaryOpIR.result, etc.) are parameters.
     *
     * We take a simpler approach: scan the instructions in order. Any identifier
     * used as a source before it appears as a target is a parameter.
     */
    private List<String> inferParamNames(IRFunction func) {
        Set<String> defined = new LinkedHashSet<>();
        List<String> params = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (IRInstruction instr : func.instructions) {
            // Track targets
            if (instr instanceof AssignIR a) {
                defined.add(a.lhs);
            } else if (instr instanceof BinaryOpIR b) {
                defined.add(b.result);
            } else if (instr instanceof UnaryOpIR u) {
                defined.add(u.result);
            } else if (instr instanceof CallIR c && c.result != null) {
                defined.add(c.result);
            }

            // Track sources (first use before definition = parameter)
            for (String src : getSources(instr)) {
                if (!isLiteral(src) && !defined.contains(src) && !seen.contains(src)) {
                    params.add(src);
                    seen.add(src);
                }
            }
        }
        return params;
    }

    /** Get all source operands from an instruction. */
    private List<String> getSources(IRInstruction instr) {
        List<String> sources = new ArrayList<>();
        if (instr instanceof AssignIR a) {
            sources.add(a.rhs);
        } else if (instr instanceof BinaryOpIR b) {
            sources.add(b.lhs);
            sources.add(b.rhs);
        } else if (instr instanceof UnaryOpIR u) {
            sources.add(u.operand);
        } else if (instr instanceof CallIR c) {
            sources.addAll(c.args);
        } else if (instr instanceof ReturnIR r) {
            if (r.value != null) sources.add(r.value);
        } else if (instr instanceof BranchIR b) {
            sources.add(b.cond);
        }
        return sources;
    }

    private void generateFunction(IRFunction func) {
        // Infer parameter names
        List<String> paramNames = inferParamNames(func);
        int nArgs = paramNames.size();

        // Build slot map: params first, then locals/temps
        Map<String, Integer> slotMap = new LinkedHashMap<>();
        int slotCounter = 0;

        // Parameters get slots [0..nArgs-1]
        for (String param : paramNames) {
            slotMap.put(param, slotCounter++);
        }

        // Collect remaining variable/temp names
        Set<String> allNames = collectNames(func);
        for (String name : allNames) {
            if (!slotMap.containsKey(name)) {
                slotMap.put(name, slotCounter++);
            }
        }

        int nLocals = slotCounter;

        // Define the function at the current code position
        asm.defineFunction(func.name, nArgs, nLocals, asm.currentIp());

        // Generate code for each IR instruction
        for (IRInstruction instr : func.instructions) {
            generateInstruction(instr, slotMap);
        }
    }

    private Set<String> collectNames(IRFunction func) {
        Set<String> names = new LinkedHashSet<>();
        for (IRInstruction instr : func.instructions) {
            if (instr instanceof AssignIR a) {
                names.add(a.lhs);
                addIfVar(a.rhs, names);
            } else if (instr instanceof BinaryOpIR b) {
                names.add(b.result);
                addIfVar(b.lhs, names);
                addIfVar(b.rhs, names);
            } else if (instr instanceof UnaryOpIR u) {
                names.add(u.result);
                addIfVar(u.operand, names);
            } else if (instr instanceof CallIR c) {
                if (c.result != null) names.add(c.result);
                for (String arg : c.args) addIfVar(arg, names);
            } else if (instr instanceof ReturnIR r) {
                if (r.value != null) addIfVar(r.value, names);
            } else if (instr instanceof BranchIR b) {
                addIfVar(b.cond, names);
            }
        }
        return names;
    }

    /** Add a name only if it looks like a variable (not a literal). */
    private void addIfVar(String name, Set<String> names) {
        if (name == null) return;
        if (isLiteral(name)) return;
        names.add(name);
    }

    private boolean isLiteral(String s) {
        if (s == null) return true;
        try { Integer.parseInt(s); return true; } catch (NumberFormatException e) {}
        try { Float.parseFloat(s); return true; } catch (NumberFormatException e) {}
        if (s.equals("true") || s.equals("false")) return true;
        if (s.startsWith("'") && s.endsWith("'") && s.length() == 3) return true;
        if (s.startsWith("\"") && s.endsWith("\"")) return true;
        return false;
    }

    private void generateInstruction(IRInstruction instr, Map<String, Integer> slotMap) {
        if (instr instanceof AssignIR a) {
            emitLoad(a.rhs, slotMap);
            emitStore(a.lhs, slotMap);
        } else if (instr instanceof BinaryOpIR b) {
            emitLoad(b.lhs, slotMap);
            emitLoad(b.rhs, slotMap);
            emitBinop(b.op);
            emitStore(b.result, slotMap);
        } else if (instr instanceof UnaryOpIR u) {
            emitLoad(u.operand, slotMap);
            emitUnaryOp(u.op);
            emitStore(u.result, slotMap);
        } else if (instr instanceof CallIR c) {
            // Push arguments in order
            for (String arg : c.args) {
                emitLoad(arg, slotMap);
            }
            // Emit CALL
            int funcIdx = asm.getFunctionIndex(c.funcName);
            asm.emitWithOperand(BytecodeDefinition.INSTR_CALL, funcIdx);
            // CALL leaves return value on stack; store it
            if (c.result != null) {
                emitStore(c.result, slotMap);
            } else {
                // Void call — pop unused return value
                asm.emitOpcode(BytecodeDefinition.INSTR_POP);
            }
        } else if (instr instanceof ReturnIR r) {
            if (r.value != null) {
                emitLoad(r.value, slotMap);
                // Value remains on stack for caller
            }
            asm.emitOpcode(BytecodeDefinition.INSTR_RET);
        } else if (instr instanceof LabelIR l) {
            asm.defineLabel(l.label);
        } else if (instr instanceof JumpIR j) {
            int labelAddr = asm.getLabelAddress(j.target);
            asm.emitWithOperand(BytecodeDefinition.INSTR_BR, labelAddr);
        } else if (instr instanceof BranchIR b) {
            emitLoad(b.cond, slotMap);
            // BRF: branch to falseLabel if condition == 0
            int falseAddr = asm.getLabelAddress(b.falseLabel);
            asm.emitWithOperand(BytecodeDefinition.INSTR_BRF, falseAddr);
            // BR: unconditional to trueLabel
            int trueAddr = asm.getLabelAddress(b.trueLabel);
            asm.emitWithOperand(BytecodeDefinition.INSTR_BR, trueAddr);
        }
    }

    /** Emit a LOAD or ICONST to push a value onto the stack. */
    private void emitLoad(String name, Map<String, Integer> slotMap) {
        if (isLiteral(name)) {
            try {
                int val = Integer.parseInt(name);
                asm.emitWithOperand(BytecodeDefinition.INSTR_ICONST, val);
                return;
            } catch (NumberFormatException e) {}
            try {
                float val = Float.parseFloat(name);
                int poolIdx = asm.getConstantPoolIndex(val);
                asm.emitWithOperand(BytecodeDefinition.INSTR_FCONST, poolIdx);
                return;
            } catch (NumberFormatException e) {}
            if (name.equals("true")) {
                asm.emitWithOperand(BytecodeDefinition.INSTR_ICONST, 1);
                return;
            }
            if (name.equals("false")) {
                asm.emitWithOperand(BytecodeDefinition.INSTR_ICONST, 0);
                return;
            }
            asm.emitWithOperand(BytecodeDefinition.INSTR_ICONST, 0);
            return;
        }
        Integer slot = slotMap.get(name);
        if (slot == null) {
            System.err.println("Warning: unknown variable '" + name + "'");
            slot = 0;
        }
        asm.emitWithOperand(BytecodeDefinition.INSTR_LOAD, slot);
    }

    /** Emit a STORE to pop the stack into a variable slot. */
    private void emitStore(String name, Map<String, Integer> slotMap) {
        if (isLiteral(name)) {
            asm.emitOpcode(BytecodeDefinition.INSTR_POP);
            return;
        }
        Integer slot = slotMap.get(name);
        if (slot == null) {
            System.err.println("Warning: unknown variable '" + name + "' for store");
            slot = 0;
        }
        asm.emitWithOperand(BytecodeDefinition.INSTR_STORE, slot);
    }

    private void emitBinop(String op) {
        switch (op) {
            case "+"  -> asm.emitOpcode(BytecodeDefinition.INSTR_IADD);
            case "-"  -> asm.emitOpcode(BytecodeDefinition.INSTR_ISUB);
            case "*"  -> asm.emitOpcode(BytecodeDefinition.INSTR_IMUL);
            case "/"  -> asm.emitOpcode(BytecodeDefinition.INSTR_IDIV);
            case "%"  -> asm.emitOpcode(BytecodeDefinition.INSTR_IDIV); // approximate
            case "==" -> asm.emitOpcode(BytecodeDefinition.INSTR_IEQ);
            case "!=" -> asm.emitOpcode(BytecodeDefinition.INSTR_INE);
            case "<"  -> asm.emitOpcode(BytecodeDefinition.INSTR_ILT);
            case "<=" -> asm.emitOpcode(BytecodeDefinition.INSTR_ILE);
            case ">"  -> asm.emitOpcode(BytecodeDefinition.INSTR_IGT);
            case ">=" -> asm.emitOpcode(BytecodeDefinition.INSTR_IGE);
            case "&&" -> asm.emitOpcode(BytecodeDefinition.INSTR_IAND);
            default   -> asm.emitOpcode(BytecodeDefinition.INSTR_IADD);
        }
    }

    private void emitUnaryOp(String op) {
        switch (op) {
            case "-" -> asm.emitOpcode(BytecodeDefinition.INSTR_INEG);
            case "!" -> asm.emitOpcode(BytecodeDefinition.INSTR_INOT);
            default  -> asm.emitOpcode(BytecodeDefinition.INSTR_INOT);
        }
    }
}
