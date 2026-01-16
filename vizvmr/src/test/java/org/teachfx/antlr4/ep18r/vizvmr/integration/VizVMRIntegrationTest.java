package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.controller.VMRStepController;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VizVMR 端到端集成测试
 * 验证 vizvmr 与 EP18R 的完整对接功能
 */
class VizVMRIntegrationTest {
    private RegisterVMInterpreter vm;
    private VMRStateModel stateModel;
    private VMRVisualBridge visualBridge;
    private VMRStepController stepController;

    @BeforeEach
    void setUp() {
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024)
                .setStackSize(256)
                .setMaxStackDepth(32)
                .build();

        vm = new RegisterVMInterpreter(config);
        stateModel = new VMRStateModel(1024, 256, 32);
        visualBridge = new VMRVisualBridge(vm, stateModel);
        stepController = new VMRStepController(visualBridge);
    }

    @Test
    void testIntegration_Initialization() {
        assertNotNull(vm, "VM should be initialized");
        assertNotNull(stateModel, "State model should be initialized");
        assertNotNull(visualBridge, "Visual bridge should be initialized");
        assertNotNull(stepController, "Step controller should be initialized");
        assertEquals(visualBridge.getVM(), vm, "Visual bridge should reference VM");
    }

    @Test
    void testIntegration_PauseResume() {
        assertFalse(visualBridge.isPaused(), "Should not be paused initially");
        assertFalse(visualBridge.getVM().isPaused(), "VM should not be paused initially");

        visualBridge.getVM().setPaused(true);

        assertTrue(visualBridge.getVM().isPaused(), "VM should be paused after setPaused()");
        assertTrue(visualBridge.isPaused(), "Visual bridge should reflect paused state");

        visualBridge.getVM().setPaused(false);

        assertFalse(visualBridge.getVM().isPaused(), "VM should not be paused after setPaused(false)");
        assertFalse(visualBridge.isPaused(), "Visual bridge should reflect running state");
    }

    @Test
    void testIntegration_BreakpointManagement() {
        int breakpointPC = 0x100;

        assertFalse(vm.hasBreakpoint(breakpointPC), "Should not have breakpoint initially");

        stepController.setBreakpoint(breakpointPC);

        assertTrue(vm.hasBreakpoint(breakpointPC), "VM should have breakpoint after setBreakpoint()");

        stepController.clearBreakpoint(breakpointPC);

        assertFalse(vm.hasBreakpoint(breakpointPC), "VM should not have breakpoint after clearBreakpoint()");
    }

    @Test
    void testIntegration_BreakpointToggle() {
        int breakpointPC = 0x200;

        assertFalse(vm.hasBreakpoint(breakpointPC), "Should not have breakpoint initially");

        stepController.toggleBreakpoint(breakpointPC);

        assertTrue(vm.hasBreakpoint(breakpointPC), "VM should have breakpoint after first toggle");

        stepController.toggleBreakpoint(breakpointPC);

        assertFalse(vm.hasBreakpoint(breakpointPC), "VM should not have breakpoint after second toggle");
    }

    @Test
    void testIntegration_ClearAllBreakpoints() {
        stepController.setBreakpoint(0x100);
        stepController.setBreakpoint(0x200);
        stepController.setBreakpoint(0x300);

        assertEquals(3, stepController.getBreakpointManager().getBreakpointCount(),
                "Should have 3 breakpoints");

        stepController.clearAllBreakpoints();

        assertEquals(0, stepController.getBreakpointManager().getBreakpointCount(),
                "Should have 0 breakpoints after clearAllBreakpoints()");
        assertEquals(0, vm.getBreakpoints().size(),
                "VM should have 0 breakpoints after clearAllBreakpoints()");
    }

    @Test
    void testIntegration_Stop() {
        visualBridge.stop();

        assertFalse(visualBridge.isRunning(), "Should not be running after stop()");
        assertFalse(vm.isPaused(), "VM should not be paused after stop()");
    }

    @Test
    void testIntegration_StateSync() {
        int testRegNum = 5;
        int testValue = 0x12345678;
        int testAddress = 0x100;
        int testMemoryValue = 0xDEADBEEF;

        stateModel.setRegister(testRegNum, testValue);
        assertEquals(testValue, stateModel.getRegister(testRegNum),
                "State model should have register value");

        stateModel.writeHeap(testAddress, testMemoryValue);
        assertEquals(testMemoryValue, stateModel.readHeap(testAddress),
                "State model should have memory value");
    }

    @Test
    void testIntegration_StepMode() {
        vm.setStepMode(true);
        assertTrue(vm.isStepMode(), "VM should be in step mode");

        vm.setStepMode(false);
        assertFalse(vm.isStepMode(), "VM should not be in step mode");
    }

    @Test
    void testVMRInstrumentation() {
        VMRInstrumentation instrumentation = new VMRInstrumentation(vm, stateModel);
        instrumentation.instrument();

        instrumentation.syncState();

        int pc = instrumentation.getProgramCounter();
        assertEquals(0, pc);

        assertTrue(instrumentation.isInstrumented());
    }
}
