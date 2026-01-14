package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 控制按钮面板
 * 播放、暂停、停止、单步等按钮
 */
public class ControlPanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JButton startButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton stopButton;
    private JButton stepButton;

    public ControlPanel(VMRVisualBridge visualBridge) {
        super(new FlowLayout(FlowLayout.CENTER, 10, 5));
        this.visualBridge = visualBridge;

        setBorder(BorderFactory.createTitledBorder("控制"));
        setPreferredSize(new Dimension(300, 80));

        initializeControls();
    }

    private void initializeControls() {
        // 开始按钮
        startButton = new JButton("▶ 开始");
        startButton.setToolTipText("开始执行 (F5)");
        startButton.addActionListener(e -> visualBridge.start());
        add(startButton);

        // 暂停按钮
        pauseButton = new JButton("⏸ 暂停");
        pauseButton.setToolTipText("暂停 (F6)");
        pauseButton.addActionListener(e -> visualBridge.pause());
        add(pauseButton);

        // 继续按钮
        resumeButton = new JButton("▶ 继续");
        resumeButton.setToolTipText("继续 (F7)");
        resumeButton.addActionListener(e -> visualBridge.resume());
        add(resumeButton);

        // 停止按钮
        stopButton = new JButton("⏹ 停止");
        stopButton.setToolTipText("停止 (F8)");
        stopButton.addActionListener(e -> visualBridge.stop());
        add(stopButton);

        // 分隔符
        add(new JSeparator(SwingConstants.VERTICAL));

        // 单步按钮
        stepButton = new JButton("⏭ 单步");
        stepButton.setToolTipText("单步执行 (F11)");
        stepButton.addActionListener(e -> visualBridge.step());
        add(stepButton);

        // 初始状态
        setRunning(false);
    }

    public void setRunning(boolean running) {
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        resumeButton.setEnabled(false);
        stopButton.setEnabled(running);
        stepButton.setEnabled(!running);
    }

    public void setPaused(boolean paused) {
        startButton.setEnabled(!paused);
        pauseButton.setEnabled(!paused);
        resumeButton.setEnabled(paused);
        stopButton.setEnabled(paused);
        stepButton.setEnabled(true);
    }

    public void updateState(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State state) {
        switch (state) {
            case RUNNING:
                setRunning(true);
                break;
            case PAUSED:
                setPaused(true);
                break;
            case HALTED:
                setRunning(false);
                break;
            case STEPPING:
                setPaused(true);
                break;
            default:
                break;
        }
    }
}
