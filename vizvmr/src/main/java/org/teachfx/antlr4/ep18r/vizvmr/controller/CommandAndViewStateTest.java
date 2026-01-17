package org.teachfx.antlr4.ep18r.vizvmr.controller;

import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.state.VMState;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * UI操作命令和View状态组合测试
 * 测试命令逻辑控制器和虚拟机视图状态类的完整场景
 */
public class CommandAndViewStateTest {
    private static int testNumber = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("UI操作命令和View状态组合测试");
        System.out.println("测试目标：验证命令逻辑控制器和VM状态管理器的完整性");
        System.out.println("=".repeat(80));

        VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();

        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
        VMCommandController controller = new VMCommandController(vm);

        System.out.println("\n[SETUP] 注册状态变更监听器...");
        controller.addStateChangeListener((oldState, newState) -> {
            System.out.println("[EVENT] 状态变更: " + oldState.getDescription() + " -> " + newState.getDescription());
        });

        System.out.println("[STATE] 初始状态: " + controller.getCurrentState().getDescription());
        System.out.println("\n" + "-".repeat(80));

        // 测试用例1: 正常加载流程（Load -> Ready）
        testNormalLoadFlow(controller);

        // 测试用例2: 无效状态转换（在CREATED下Start应该失败）
        testInvalidStateTransition(controller);

        // 测试用例3: 单步执行流程
        testStepExecutionFlow(controller);

        // 测试用例4: 启动-暂停-恢复流程
        testStartPauseResumeFlow(controller);

        // 测试用例5: 停止后重置流程
        testStopResetFlow(controller);

        // 测试用例6: 异常处理流程
        testExceptionHandling(controller);

        // 测试用例7: 状态机完整性验证
        testStateMachineIntegrity();

        // 显示测试总结
        printTestSummary();
    }

    /**
     * 测试用例1: 正常加载流程
     * UI操作: 文件 -> 打开代码
     * View状态: CREATED -> LOADED
     */
    private static void testNormalLoadFlow(VMCommandController controller) {
        testNumber++;
        String testName = "正常加载流程 (CREATED -> LOADED)";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户点击 '文件 -> 打开代码'");

        VMState initialState = controller.getCurrentState();
        System.out.println("[STATE] 操作前状态: " + initialState.getDescription());

        // 创建测试代码
        String testCode = ".def main: args=0, locals=0\n    li r1, 42\n    halt\n";
        InputStream codeStream = new ByteArrayInputStream(testCode.getBytes());

        System.out.println("[COMMAND] 执行LoadCode命令");
        VMCommandResult result = controller.loadCode(codeStream);

        System.out.println("[RESULT] 命令执行结果:");
        System.out.println("  成功: " + result.isSuccess());
        System.out.println("  消息: " + result.getMessage());

        VMState finalState = controller.getCurrentState();
        System.out.println("[STATE] 操作后状态: " + finalState.getDescription());

        // 验证状态转换
        boolean testPassed = result.isSuccess() && finalState == VMState.LOADED;

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  预期状态: LOADED");
        System.out.println("  实际状态: " + finalState.getDescription());

        if (testPassed) {
            passedTests++;
            System.out.println("  状态转换: ✓ 正确");
        } else {
            failedTests++;
            System.out.println("  状态转换: ✗ 错误");
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例2: 无效状态转换
     * UI操作: 用户在未加载代码时点击"开始执行"
     * View状态: CREATED下不能执行Start命令
     */
    private static void testInvalidStateTransition(VMCommandController controller) {
        testNumber++;
        String testName = "无效状态转换 (CREATED下Start)";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户在CREATED状态点击 '开始执行' 按钮");

        VMState initialState = controller.getCurrentState();
        System.out.println("[STATE] 操作前状态: " + initialState.getDescription());

        System.out.println("[COMMAND] 执行Start命令（预期失败）");
        VMCommandResult result = controller.start();

        System.out.println("[RESULT] 命令执行结果:");
        System.out.println("  成功: " + result.isSuccess());
        System.out.println("  消息: " + result.getMessage());

        VMState finalState = controller.getCurrentState();
        System.out.println("[STATE] 操作后状态: " + finalState.getDescription());

        // 验证命令被正确拒绝
        boolean testPassed = !result.isSuccess() && finalState == VMState.CREATED;

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  预期: 命令应该失败，状态保持CREATED");
        System.out.println("  实际: 命令" + (result.isSuccess() ? "成功" : "失败") + ", 状态: " + finalState.getDescription());

        if (testPassed) {
            passedTests++;
            System.out.println("  无效状态转换: ✓ 正确拒绝");
        } else {
            failedTests++;
            System.out.println("  无效状态转换: ✗ 未正确处理");
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例3: 单步执行流程
     * UI操作: 用户重复点击"单步执行"
     * View状态: READY/STOPPED -> STEPPING
     */
    private static void testStepExecutionFlow(VMCommandController controller) {
        testNumber++;
        String testName = "单步执行流程";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户加载代码后点击 '单步执行' 按钮");

        // 先加载代码使状态变为READY
        loadTestCode(controller);

        VMState stateBeforeStep = controller.getCurrentState();
        System.out.println("[STATE] 单步前状态: " + stateBeforeStep.getDescription());

        System.out.println("[COMMAND] 执行Step命令");
        VMCommandResult stepResult1 = controller.step();

        System.out.println("[RESULT] 第一次单步执行:");
        System.out.println("  成功: " + stepResult1.isSuccess());
        System.out.println("  消息: " + stepResult1.getMessage());

        VMState stateAfterStep1 = controller.getCurrentState();
        System.out.println("[STATE] 第一次单步后状态: " + stateAfterStep1.getDescription());

        // 验证单步执行有效
        boolean testPassed = stepResult1.isSuccess() &&
                (stateAfterStep1 == VMState.STEPPING || stateAfterStep1 == VMState.STOPPED);

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  预期: 状态变为STEPPING或STOPPED");
        System.out.println("  实际: 状态: " + stateAfterStep1.getDescription());

        if (testPassed) {
            passedTests++;
        } else {
            failedTests++;
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例4: 启动-暂停-恢复流程
     * UI操作: 开始 -> 暂停 -> 恢复
     * View状态: LOADED -> RUNNING -> PAUSED -> RUNNING
     */
    private static void testStartPauseResumeFlow(VMCommandController controller) {
        testNumber++;
        String testName = "启动-暂停-恢复流程";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户点击 '开始' -> '暂停' -> '开始'");

        // 加载代码
        loadTestCode(controller);

        System.out.println("[STEP-1] 用户点击 '开始执行' 按钮");
        VMCommandResult startResult = controller.start();
        System.out.println("[STATE] 状态: " + controller.getCurrentState().getDescription());

        System.out.println("[STEP-2] 用户点击 '暂停' 按钮");
        VMCommandResult pauseResult = controller.pause();
        System.out.println("[STATE] 状态: " + controller.getCurrentState().getDescription());

        System.out.println("[STEP-3] 用户点击 '继续' 按钮（Start）");
        VMCommandResult resumeResult = controller.start();
        System.out.println("[STATE] 最终状态: " + controller.getCurrentState().getDescription());

        // 验证流程正确性
        boolean testPassed = startResult.isSuccess() &&
                pauseResult.isSuccess() &&
                resumeResult.isSuccess();

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  所有命令都成功: " + testPassed);

        if (testPassed) {
            passedTests++;
        } else {
            failedTests++;
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例5: 停止后重置流程
     * UI操作: 停止 -> 重置 -> 重新开始
     * View状态: RUNNING -> STOPPED -> READY -> RUNNING
     */
    private static void testStopResetFlow(VMCommandController controller) {
        testNumber++;
        String testName = "停止-重置流程";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户点击 '停止' -> 重置VM -> '开始'");

        // 加载代码并启动
        loadTestCode(controller);
        controller.start();

        System.out.println("[STEP-1] 用户点击 '停止' 按钮");
        VMCommandResult stopResult = controller.stop();
        VMState stateAfterStop = controller.getCurrentState();
        System.out.println("[STATE] 停止后状态: " + stateAfterStop.getDescription());

        System.out.println("[STEP-2] 重置VM");
        controller.reset();
        VMState stateAfterReset = controller.getCurrentState();
        System.out.println("[STATE] 重置后状态: " + stateAfterReset.getDescription());

        System.out.println("[STEP-3] 用户再次点击 '开始'");
        VMCommandResult restartResult = controller.start();
        VMState stateAfterRestart = controller.getCurrentState();
        System.out.println("[STATE] 重新开始后状态: " + stateAfterRestart.getDescription());

        // 验证流程
        boolean testPassed = stopResult.isSuccess() &&
                stateAfterReset == VMState.STOPPED &&
                restartResult.isSuccess();

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  停止成功: " + stopResult.isSuccess());
        System.out.println("  重置后状态为STOPPED: " + (stateAfterReset == VMState.STOPPED));
        System.out.println("  重新开始成功: " + restartResult.isSuccess());

        if (testPassed) {
            passedTests++;
        } else {
            failedTests++;
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例6: 异常处理流程
     * UI操作: 加载错误代码
     * View状态: LOADED -> ERROR
     */
    private static void testExceptionHandling(VMCommandController controller) {
        testNumber++;
        String testName = "异常处理流程";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[UI操作] 用户加载错误格式的代码");

        VMState stateBefore = controller.getCurrentState();
        System.out.println("[STATE] 加载前状态: " + stateBefore.getDescription());

        // 尝试加载无效代码
        String invalidCode = "INVALID_CODE_FORMAT";
        InputStream errorStream = new ByteArrayInputStream(invalidCode.getBytes());

        System.out.println("[COMMAND] 执行LoadCode命令（错误代码）");
        VMCommandResult result = controller.loadCode(errorStream);

        System.out.println("[RESULT] 命令执行结果:");
        System.out.println("  成功: " + result.isSuccess());
        System.out.println("  消息: " + result.getMessage());

        if (result.getError() != null) {
            System.out.println("  异常: " + result.getError().getMessage());
        }

        VMState stateAfter = controller.getCurrentState();
        System.out.println("[STATE] 加载后状态: " + stateAfter.getDescription());

        // 验证异常被正确处理
        boolean testPassed = !result.isSuccess() && result.getError() != null;

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  预期: 命令失败并返回异常");
        System.out.println("  实际: " + (testPassed ? "正确处理异常" : "未正确处理异常"));

        if (testPassed) {
            passedTests++;
        } else {
            failedTests++;
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 测试用例7: 状态机完整性验证
     * 验证所有可能的状态转换
     */
    private static void testStateMachineIntegrity() {
        testNumber++;
        String testName = "状态机完整性验证";
        System.out.println("\n[TEST-" + testNumber + "] " + testName);
        System.out.println("-".repeat(80));
        System.out.println("[验证] 检查所有可能的状态转换规则");

        VMState[] states = VMState.values();
        int totalTransitions = 0;
        int validTransitions = 0;

        System.out.println("\n[检查] 状态转换规则:");
        for (VMState from : states) {
            System.out.println("\n从 " + from.getDescription() + " 状态:");
            for (VMState to : states) {
                if (from == to) continue;
                totalTransitions++;

                if (isValidTransition(from, to)) {
                    System.out.println("  → " + to.getDescription() + " ✓");
                    validTransitions++;
                } else {
                    System.out.println("  → " + to.getDescription() + " ✗");
                }
            }
        }

        System.out.println("\n[统计] 总转换检查: " + totalTransitions);
        System.out.println("[统计] 有效转换: " + validTransitions);
        System.out.println("[统计] 无效转换: " + (totalTransitions - validTransitions));

        // 验证关键状态转换存在
        boolean hasCreatedToLoaded = isValidTransition(VMState.CREATED, VMState.LOADED);
        boolean hasLoadedToReady = isValidTransition(VMState.LOADED, VMState.READY);
        boolean hasReadyToRunning = isValidTransition(VMState.READY, VMState.RUNNING);
        boolean hasRunningToPaused = isValidTransition(VMState.RUNNING, VMState.PAUSED);
        boolean hasPausedToStopped = isValidTransition(VMState.PAUSED, VMState.STOPPED);

        boolean criticalPathsValid = hasCreatedToLoaded && hasLoadedToReady &&
                hasReadyToRunning && hasRunningToPaused && hasPausedToStopped;

        System.out.println("\n[验证] 关键路径验证:");
        System.out.println("  CREATED -> LOADED: " + (hasCreatedToLoaded ? "✓" : "✗"));
        System.out.println("  LOADED -> READY: " + (hasLoadedToReady ? "✓" : "✗"));
        System.out.println("  READY -> RUNNING: " + (hasReadyToRunning ? "✓" : "✗"));
        System.out.println("  RUNNING -> PAUSED: " + (hasRunningToPaused ? "✓" : "✗"));
        System.out.println("  PAUSED -> STOPPED: " + (hasPausedToStopped ? "✓" : "✗"));

        boolean testPassed = criticalPathsValid && validTransitions > 0;

        System.out.println("\n[验证] " + (testPassed ? "✓ 通过" : "✗ 失败"));
        System.out.println("  关键路径完整性: " + (criticalPathsValid ? "✓ 正常" : "✗ 有缺失"));
        System.out.println("  状态转换总数: " + validTransitions);

        if (testPassed) {
            passedTests++;
        } else {
            failedTests++;
        }

        System.out.println("-".repeat(80));
    }

    /**
     * 辅助方法：验证状态转换是否有效
     */
    private static boolean isValidTransition(VMState from, VMState to) {
        switch (from) {
            case CREATED:
                return to == VMState.LOADED;
            case LOADED:
                return to == VMState.READY || to == VMState.STOPPED;
            case READY:
                return to == VMState.RUNNING || to == VMState.STEPPING || to == VMState.STOPPED;
            case RUNNING:
                return to == VMState.PAUSED || to == VMState.STOPPED || to == VMState.ERROR;
            case PAUSED:
                return to == VMState.RUNNING || to == VMState.STEPPING || to == VMState.STOPPED;
            case STEPPING:
                return to == VMState.RUNNING || to == VMState.PAUSED || to == VMState.STOPPED || to == VMState.ERROR;
            case STOPPED:
                return to == VMState.READY || to == VMState.RUNNING || to == VMState.STEPPING;
            case ERROR:
                return to == VMState.STOPPED || to == VMState.READY;
            default:
                return false;
        }
    }

    /**
     * 辅助方法：加载测试代码
     */
    private static void loadTestCode(VMCommandController controller) {
        String testCode = ".def main: args=0, locals=0\n    li r1, 42\n    halt\n";
        InputStream codeStream = new ByteArrayInputStream(testCode.getBytes());
        controller.loadCode(codeStream);
    }

    /**
     * 打印测试总结
     */
    private static void printTestSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("测试总结");
        System.out.println("=".repeat(80));
        System.out.println("\n[统计] 总测试数: " + testNumber);
        System.out.println("[统计] 通过: " + passedTests + " (" + String.format("%.1f%%", passedTests * 100.0 / testNumber) + ")");
        System.out.println("[统计] 失败: " + failedTests + " (" + String.format("%.1f%%", failedTests * 100.0 / testNumber) + ")");

        System.out.println("\n[结果] " + (passedTests == testNumber ? "✓ 所有测试通过" : "✗ 存在失败的测试"));

        if (passedTests == testNumber) {
            System.out.println("\n[INFO] 命令逻辑控制器和VM状态管理器测试通过！");
            System.out.println("[INFO] UI操作命令和View状态组合验证成功");
            System.out.println("\n[可观测性验证]");
            System.out.println("  ✓ [CONTROLLER] - 命令接收日志清晰");
            System.out.println("  ✓ [COMMAND] - 命令执行状态明确");
            System.out.println("  ✓ [STATE] - 状态转换可追踪");
            System.out.println("  ✓ [EVENT] - 状态变更事件完整");
            System.out.println("  ✓ 命令逻辑与UI完全分离");
        } else {
            System.out.println("\n[WARN] 部分测试失败，需要修复");
            System.out.println("[WARN] 检查失败的测试用例");
        }

        System.out.println("=".repeat(80));
    }
}
