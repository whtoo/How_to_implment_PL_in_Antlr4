package org.teachfx.antlr4.ep18r.vizvmr.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.MemoryChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GUI执行路径测试")
public class VizVMRExecutionTest {

    @Test
    @DisplayName("GUI执行路径应与测试执行路径一致")
    void testGUIExecutionPath() throws Exception {
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        CountDownLatch executionDone = new CountDownLatch(1);
        
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("mov_test.vmr");
            assertThat(input).as("mov_test.vmr资源文件未找到").isNotNull();

            VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();
            
            RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
            vm.setTrace(true);

            VMRStateModel stateModel = new VMRStateModel(
                config.getHeapSize(),
                256,
                config.getMaxCallStackDepth()
            );

            VMRVisualBridge visualBridge = new VMRVisualBridge(vm, stateModel);
            
            visualBridge.setExecutionCallback(new VMRVisualBridge.ExecutionCallback() {
                @Override
                public void onRegisterChanged(int regNum, int oldValue, int newValue) {}
                @Override
                public void onMemoryChanged(MemoryChangeEvent.MemoryType type, 
                                          int address, int oldValue, int newValue) {}
                @Override
                public void onPCChanged(int oldPC, int newPC) {}
                @Override
                public void onStateChanged(VMStateChangeEvent.State oldState,
                                         VMStateChangeEvent.State newState) {}
                @Override
                public void onInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {}
                @Override
                public void onExecutionStarted() {}
                @Override
                public void onExecutionFinished() {
                    executionDone.countDown();
                }
                @Override
                public void onExecutionPaused() {}
                @Override
                public void onError(Throwable error) {
                    error.printStackTrace();
                    executionDone.countDown();
                }
            });

            boolean hasErrors = visualBridge.loadCode(input);
            assertThat(hasErrors).as("mov_test.vmr加载失败").isFalse();

            visualBridge.start();

            boolean completed = executionDone.await(5, TimeUnit.SECONDS);
            assertThat(completed).as("执行未在5秒内完成").isTrue();

            String diagnosticOutput = errContent.toString();
            System.err.println("[CAPTURED DIAGNOSTIC OUTPUT]");
            System.err.println(diagnosticOutput);

            int result = visualBridge.getRegister(2);
            assertThat(result).as("r2应该等于42").isEqualTo(42);
            
        } finally {
            System.setErr(originalErr);
        }
    }
}