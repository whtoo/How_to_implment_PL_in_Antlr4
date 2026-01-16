package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("unused")

public class VMRStepController {
    public enum StepMode {
        STEP_INTO,
        STEP_OVER,
        STEP_OUT,
        RUN_TO_LINE,
        CONTINUE
    }

    private final VMRVisualBridge visualBridge;
    private final VMRStateModel stateModel;
    private final VMRBreakpointManager breakpointManager;
    private final AtomicBoolean stepping;
    private StepMode currentMode;

    public VMRStepController(VMRVisualBridge visualBridge) {
        this.visualBridge = visualBridge;
        this.stateModel = visualBridge.getStateModel();
        this.breakpointManager = new VMRBreakpointManager();
        this.stepping = new AtomicBoolean(false);
        this.currentMode = StepMode.CONTINUE;
    }

    public void stepInto() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_INTO;
        stepping.set(true);

        visualBridge.step();
    }

    public void stepOver() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_OVER;
        stepping.set(true);

        visualBridge.step();
    }

    public void stepOut() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.STEP_OUT;
        stepping.set(true);

        visualBridge.step();
    }

    public void runToPC(int pc) {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.RUN_TO_LINE;
        stepping.set(true);

        visualBridge.start();
    }

    public void continueExecution() {
        if (stateModel.getVMState() == VMStateChangeEvent.State.HALTED) {
            return;
        }

        currentMode = StepMode.CONTINUE;
        visualBridge.resume();
    }

    public void setBreakpoint(int pc) {
        breakpointManager.setBreakpoint(pc);
        visualBridge.getVM().addBreakpoint(pc);
    }

    public void clearBreakpoint(int pc) {
        breakpointManager.clearBreakpoint(pc);
        visualBridge.getVM().removeBreakpoint(pc);
    }

    public void toggleBreakpoint(int pc) {
        breakpointManager.toggleBreakpoint(pc);

        if (visualBridge.getVM().hasBreakpoint(pc)) {
            visualBridge.getVM().removeBreakpoint(pc);
        } else {
            visualBridge.getVM().addBreakpoint(pc);
        }
    }

    public void clearAllBreakpoints() {
        java.util.Set<Integer> breakpoints = breakpointManager.getBreakpoints();
        breakpointManager.clearAllBreakpoints();
        for (int pc : breakpoints) {
            visualBridge.getVM().removeBreakpoint(pc);
        }
    }

    public VMRBreakpointManager getBreakpointManager() {
        return breakpointManager;
    }

    public StepMode getCurrentMode() {
        return currentMode;
    }

    public boolean isStepping() {
        return stepping.get();
    }
}
