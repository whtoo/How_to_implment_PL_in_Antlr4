package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单命令控制器测试 - 验证基础命令-状态-事件流
 */
public class SimpleCommandControllerTest {

    @Test
    public void testLoadCodeAndAutoTransitionToReady() {
        System.out.println("=".repeat(80));
        System.out.println("TEST: 加载代码并自动转换到READY状态");
        System.out.println("=".repeat(80));

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();

        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
        VMCommandController controller = new VMCommandController(vm);

        // 初始状态应该是CREATED
        assertEquals(VMState.CREATED, controller.getCurrentState());

        // 加载代码
        String testCode = ".def main: args=0, locals=0\n    li r1, 42\n    halt\n";
        byte[] codeBytes = testCode.getBytes();
        ByteArrayInputStream codeStream = new ByteArrayInputStream(codeBytes);

        VMCommandResult loadResult = controller.loadCode(codeStream);

        // 验证加载成功
        assertTrue(loadResult.isSuccess());
        assertEquals("代码加载成功", loadResult.getMessage());

        // 验证自动转换到READY状态
        assertEquals(VMState.READY, controller.getCurrentState());

        System.out.println("\n[RESULT] ✓ 通过");
        System.out.println("  加载代码成功: " + loadResult.isSuccess());
        System.out.println("  自动转换到READY状态: " + (controller.getCurrentState() == VMState.READY));
    }

    @Test
    public void testStartCommandFromReadyState() {
        System.out.println("=".repeat(80));
        System.out.println("TEST: 从READY状态启动命令");
        System.out.println("=".repeat(80));

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();

        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
        VMCommandController controller = new VMCommandController(vm);

        // 加载代码（会自动转换到READY）
        String testCode = ".def main: args=0, locals=0\n    li r1, 42\n    halt\n";
        ByteArrayInputStream codeStream = new ByteArrayInputStream(testCode.getBytes());
        controller.loadCode(codeStream);

        assertEquals(VMState.READY, controller.getCurrentState());

        // 启动命令
        VMCommandResult startResult = controller.start();

        // 验证启动成功
        assertTrue(startResult.isSuccess());
        assertEquals("VM执行完成", startResult.getMessage());

        // 验证状态转换为RUNNING
        assertEquals(VMState.RUNNING, controller.getCurrentState());

        System.out.println("\n[RESULT] ✓ 通过");
        System.out.println("  启动命令成功: " + startResult.isSuccess());
        System.out.println("  状态变为RUNNING: " + (controller.getCurrentState() == VMState.RUNNING));
    }
}
