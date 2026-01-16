package org.teachfx.antlr4.ep18r.vizvmr;

import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.ep18r.vizvmr.ui.MainFrame;

import javax.swing.*;

/**
 * 虚拟机可视化启动器
 * 创建虚拟机、状态模型、桥接器并启动主窗口
 */
public class VizVMRLauncher {

    public static void main(String[] args) {
        // 设置 Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 使用默认 Look and Feel
        }

        // 在 EDT 中创建 GUI
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        // 创建虚拟机配置
        VMConfig config = new VMConfig.Builder()
            .setHeapSize(1024 * 1024)  // 1MB 堆
            .setStackSize(1024)        // 1K 局部变量
            .setMaxStackDepth(100)     // 最大调用深度
            .setDebugMode(true)
            .build();

        // 创建虚拟机
        RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

        // 创建状态模型
        VMRStateModel stateModel = new VMRStateModel(
            config.getHeapSize(),
            256,  // 全局变量大小
            config.getMaxCallStackDepth()
        );

        // 创建可视化桥接器
        VMRVisualBridge visualBridge = new VMRVisualBridge(vm, stateModel);

        // 创建主窗口
        MainFrame mainFrame = new MainFrame(visualBridge);

        // 添加窗口关闭监听器，防止 JVM 提前退出
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // 停止 VM
                visualBridge.stop();
                System.out.println("EP18R 寄存器虚拟机可视化工具已关闭");
            }
        });

        // 显示窗口
        mainFrame.setVisible(true);

        System.out.println("EP18R 寄存器虚拟机可视化工具已启动");
        System.out.println("请使用 文件 -> 打开代码 加载虚拟机程序");
    }
}
