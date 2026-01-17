package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

import java.io.FileInputStream;

/**
 * 命令控制器测试类
 * 验证命令-状态-事件流在terminal中完全可观测
 * 剥离UI因素，便于定位和解决问题
 */
public class VMCommandControllerTest {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("虚拟机命令控制器测试 - 命令-状态-事件流可观测性测试");
        System.out.println("=".repeat(80));

        // 1. 创建VM和命令控制器
        System.out.println("\n[SETUP] 创建虚拟机和命令控制器...");
        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();

        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
        VMCommandController controller = new VMCommandController(vm);

        // 2. 监听状态变更
        System.out.println("[SETUP] 注册状态变更监听器...");
        controller.addStateChangeListener((oldState, newState) -> {
            System.out.println("[EVENT] ★ 状态变更: " + oldState.getDescription() + " -> " + newState.getDescription());
        });

        System.out.println("[STATE] 初始状态: " + controller.getCurrentState().getDescription());
        System.out.println("\n" + "-".repeat(80));

        // 3. 测试加载代码
        System.out.println("\n[TEST-1] 测试加载代码命令...");
        System.out.println("-".repeat(80));
        try {
            FileInputStream fis = new FileInputStream("src/main/resources/fib_simple.vmr");
            VMCommandResult result = controller.loadCode(fis);
            fis.close();

            System.out.println("\n[RESULT] 命令执行结果:");
            System.out.println("  成功: " + result.isSuccess());
            System.out.println("  消息: " + result.getMessage());
            if (result.getError() != null) {
                System.out.println("  错误: " + result.getError().getMessage());
            }
            System.out.println("  新状态: " + controller.getCurrentState().getDescription());
            System.out.println("-".repeat(80));
        } catch (Exception e) {
            System.err.println("[ERROR] 加载代码文件失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 4. 测试启动命令
        System.out.println("\n[TEST-2] 测试启动命令...");
        System.out.println("[WARNING] 注意：启动命令会执行整个程序，可能需要较长时间");
        System.out.println("-".repeat(80));
        VMCommandResult startResult = controller.start();
        System.out.println("\n[RESULT] 命令执行结果:");
        System.out.println("  成功: " + startResult.isSuccess());
        System.out.println("  消息: " + startResult.getMessage());
        System.out.println("  新状态: " + controller.getCurrentState().getDescription());
        System.out.println("-".repeat(80));

        // 如果启动失败，测试停止命令
        if (!startResult.isSuccess()) {
            System.out.println("\n[TEST-2b] 由于启动失败，测试停止命令...");
            VMCommandResult stopResult = controller.stop();
            System.out.println("\n[RESULT] 命令执行结果:");
            System.out.println("  成功: " + stopResult.isSuccess());
            System.out.println("  消息: " + stopResult.getMessage());
        }

        // 5. 测试无效命令序列
        System.out.println("\n[TEST-3] 测试无效命令序列...");
        System.out.println("-".repeat(80));

        // 在CREATED状态下尝试启动应该失败
        VMCommandResult invalidStart = controller.start();
        System.out.println("\n[RESULT] 在CREATED状态启动:");
        System.out.println("  成功: " + invalidStart.isSuccess());
        System.out.println("  消息: " + invalidStart.getMessage());
        System.out.println("-".repeat(80));

        // 6. 测试状态转换有效性
        System.out.println("\n[TEST-4] 测试状态转换有效性...");
        System.out.println("-".repeat(80));
        System.out.println("[STATE] 当前状态: " + controller.getCurrentState());
        System.out.println("[INFO] RUNNING状态可以执行的命令: Start(否), Pause(是), Stop(是), Step(否)");
        System.out.println("[INFO] PAUSED状态可以执行的命令: Start(是), Pause(否), Stop(是), Step(是)");
        System.out.println("-".repeat(80));

        // 7. 显示测试总结
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试完成 - 命令-状态-事件流完全可观测");
        System.out.println("=".repeat(80));
        System.out.println("\n[INFO] 关键观测点:");
        System.out.println("  1. [CONTROLLER] - 所有命令接收日志");
        System.out.println("  2. [COMMAND] - 命令执行日志");
        System.out.println("  3. [STATE] - 状态转换日志");
        System.out.println("  4. [EVENT] - 状态变更事件日志");
        System.out.println("  5. [RESULT] - 命令执行结果");
        System.out.println("\n[INFO] 使用此测试类可以:");
        System.out.println("  - 验证命令逻辑正确性");
        System.out.println("  - 调试状态转换问题");
        System.out.println("  - 追踪执行流程");
        System.out.println("  - 剥离UI因素快速定位问题");
    }
}
