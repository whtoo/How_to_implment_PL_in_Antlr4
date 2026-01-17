package org.teachfx.antlr4.ep18r.vizvmr.unified.core;

import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.VisualizationListener;

import java.io.InputStream;
import java.util.Set;

/**
 * IVM接口适配器
 *
 * <p>将RegisterVMInterpreter适配为IVM接口</p>
 */
public class IVMAdapter implements IVM {

    private final RegisterVMInterpreter interpreter;

    public IVMAdapter(RegisterVMInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    @Override
    public void exec() throws Exception {
        interpreter.exec();
    }

    @Override
    public void step() {
        throw new UnsupportedOperationException("step() not implemented in RegisterVMInterpreter");
    }

    @Override
    public void loadCode(byte[] bytecode) {
        interpreter.loadCode(bytecode);
    }

    @Override
    public void setPaused(boolean paused) {
        interpreter.setPaused(paused);
    }

    @Override
    public boolean isPaused() {
        return interpreter.isPaused();
    }

    @Override
    public void setStepMode(boolean stepMode) {
        interpreter.setStepMode(stepMode);
    }

    @Override
    public boolean isStepMode() {
        return interpreter.isStepMode();
    }

    @Override
    public void setAutoStepMode(boolean autoStepMode) {
        interpreter.setAutoStepMode(autoStepMode);
    }

    @Override
    public void setAutoStepDelay(int delayMs) {
        interpreter.setAutoStepDelay(delayMs);
    }

    @Override
    public int getAutoStepDelay() {
        return interpreter.getAutoStepDelay();
    }

    @Override
    public void addBreakpoint(int pc) {
        interpreter.addBreakpoint(pc);
    }

    @Override
    public void removeBreakpoint(int pc) {
        interpreter.removeBreakpoint(pc);
    }

    @Override
    public Set<Integer> getBreakpoints() {
        return interpreter.getBreakpoints();
    }

    @Override
    public boolean hasBreakpoints() {
        return interpreter.getBreakpoints() != null && !interpreter.getBreakpoints().isEmpty();
    }

    @Override
    public int getRegister(int regNum) {
        return interpreter.getRegister(regNum);
    }

    @Override
    public int readHeap(int address) {
        return interpreter.readHeap(address);
    }

    @Override
    public Object readGlobal(int address) {
        return interpreter.readGlobal(address);
    }

    @Override
    public int getProgramCounter() {
        return interpreter.getProgramCounter();
    }

    @Override
    public String[] getDisassembly() {
        byte[] code = interpreter.getCode();
        if (code == null || code.length == 0) {
            return new String[0];
        }

        org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler disassembler =
            new org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler(
                code,
                code.length,
                interpreter.getConstantPool()
            );

        return disassembler.disassembleToString().split("\n");
    }

    @Override
    public int[] getCallStack() {
        org.teachfx.antlr4.ep18r.stackvm.StackFrame[] stack = interpreter.getCallStack();
        if (stack == null || stack.length == 0) {
            return new int[0];
        }

        int[] callStackPCs = new int[stack.length];
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] != null) {
                callStackPCs[i] = stack[i].returnAddress;
            } else {
                callStackPCs[i] = 0;
            }
        }
        return callStackPCs;
    }

    @Override
    public void addVisualizationListener(VisualizationListener listener) {
        interpreter.addVisualizationListener(listener);
    }

    public RegisterVMInterpreter getInterpreter() {
        return interpreter;
    }

    public static boolean load(IVM vm, InputStream input) throws Exception {
        if (vm instanceof IVMAdapter adapter) {
            return RegisterVMInterpreter.load(adapter.getInterpreter(), input);
        }
        throw new IllegalArgumentException("VM is not an IVMAdapter");
    }
}
