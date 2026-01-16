package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VizVMR 核心集成测试")
class VizVMRCoreIntegrationTest {

    private RegisterVMInterpreter vm;
    private VMRVisualBridge visualBridge;

    @BeforeEach
    void setUp() throws Exception {
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024)
                .setStackSize(256)
                .setMaxStackDepth(32)
                .build();

        vm = new RegisterVMInterpreter(config);
        visualBridge = new VMRVisualBridge(vm, new VMRStateModel(1024, 256, 32));

        // 加载测试代码
        try (InputStream is = getClass().getResourceAsStream("/t.vmr")) {
            if (is != null) {
                visualBridge.loadCode(is);
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (visualBridge != null) {
            visualBridge.stop();
        }
        visualBridge = null;
        vm = null;
    }

    @Test
    @DisplayName("VMRVisualBridge 应该正确初始化")
    void visualBridge_ShouldInitializeCorrectly() {
        assertNotNull(visualBridge, "Visual bridge should be initialized");
        assertNotNull(visualBridge.getVM(), "VM should be accessible");
    }

    @Test
    @DisplayName("VM 应该支持暂停和恢复")
    void pauseResume_ShouldWorkCorrectly() {
        boolean initiallyPaused = visualBridge.getVM().isPaused();

        visualBridge.pause();

        assertTrue(visualBridge.getVM().isPaused(), "VM should be paused after pause()");
        assertTrue(visualBridge.isPaused(), "Visual bridge should reflect paused state");

        visualBridge.resume();

        assertFalse(visualBridge.getVM().isPaused(), "VM should not be paused after resume()");
        assertFalse(visualBridge.isPaused(), "Visual bridge should reflect running state");

        if (initiallyPaused) {
            visualBridge.getVM().setPaused(false);
        }
    }

    @Test
    @DisplayName("VM 应该支持单步执行模式")
    void stepMode_ShouldWorkCorrectly() {
        assertFalse(visualBridge.getVM().isStepMode(), "VM should not be in step mode initially");

        visualBridge.getVM().setStepMode(true);

        assertTrue(visualBridge.getVM().isStepMode(), "VM should be in step mode after setStepMode()");
    }

    @Test
    @DisplayName("VM 应该支持断点管理")
    void breakpointManagement_ShouldWorkCorrectly() {
        int breakpointPC = 0x100;

        assertFalse(visualBridge.getVM().hasBreakpoint(breakpointPC),
                "Should not have breakpoint initially");

        visualBridge.getVM().addBreakpoint(breakpointPC);

        assertTrue(visualBridge.getVM().hasBreakpoint(breakpointPC),
                "Should have breakpoint after addBreakpoint()");

        visualBridge.getVM().removeBreakpoint(breakpointPC);

        assertFalse(visualBridge.getVM().hasBreakpoint(breakpointPC),
                "Should not have breakpoint after removeBreakpoint()");
    }

    @Test
    @DisplayName("VM 应该支持读取代码")
    void vm_ShouldSupportCodeReading() {
        byte[] code = vm.getCode();
        int codeSize = vm.getCodeSize();

        assertNotNull(code, "Code should not be null after loading");
        assertTrue(codeSize > 0, "Code size should be positive after loading");
    }

    @Test
    @DisplayName("VM 应该支持读取寄存器")
    void vm_ShouldSupportRegisterReading() {
        vm.setRegister(1, 0x12345678);

        assertEquals(0x12345678, vm.getRegister(1), "Register value should match");
    }

    @Test
    @DisplayName("VM 应该支持读取内存")
    void vm_ShouldSupportMemoryReading() {
        vm.writeHeap(0x100, 0xDEADBEEF);

        assertEquals(0xDEADBEEF, vm.readHeap(0x100), "Memory value should match");
    }

    @Test
    @DisplayName("VMRVisualBridge start() 应该正确设置运行状态")
    void visualBridge_Start_ShouldSetRunningState() {
        visualBridge.start();

        assertTrue(visualBridge.isRunning(), "Visual bridge should be running after start()");
    }

    @Test
    @DisplayName("VMRVisualBridge stop() 应该正确停止VM")
    void visualBridge_Stop_ShouldStopVMCorrectly() {
        visualBridge.start();

        visualBridge.stop();

        assertFalse(visualBridge.isRunning(), "Visual bridge should not be running after stop()");
        assertFalse(visualBridge.isPaused(), "Visual bridge should not be paused after stop()");
    }

    @Test
    @DisplayName("VMRVisualBridge step() 应该触发单步执行")
    void visualBridge_Step_ShouldTriggerStepExecution() {
        visualBridge.step();

        assertTrue(visualBridge.getVM().isStepMode(), "VM should be in step mode after step()");
    }

    @Test
    @DisplayName("VMRVisualBridge 应该正确连接VM和状态模型")
    void visualBridge_ShouldCorrectlyConnectVMAndStateModel() {
        assertSame(visualBridge.getVM(), vm, "Visual bridge should reference same VM instance");
        assertNotNull(visualBridge.getStateModel(), "State model should not be null");
    }

    @Test
    @DisplayName("VMConfig 应该正确构建配置")
    void vmConfig_ShouldBuildCorrectly() {
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(2048)
                .setStackSize(512)
                .setMaxStackDepth(64)
                .build();

        assertEquals(2048, config.getHeapSize(), "Heap size should match");
        assertEquals(512, config.getStackSize(), "Stack size should match");
        assertEquals(64, config.getMaxStackDepth(), "Max stack depth should match");
    }
}
