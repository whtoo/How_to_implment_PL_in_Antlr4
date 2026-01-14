package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 状态信息面板
 * 显示已执行指令数、运行时间、虚拟机状态
 */
public class StatusPanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JLabel stateLabel;
    private JLabel pcLabel;
    private JLabel stepsLabel;
    private JLabel timeLabel;
    private JLabel instructionLabel;
    private Timer timer;
    private long startTime;

    public StatusPanel(VMRVisualBridge visualBridge) {
        super(new FlowLayout(FlowLayout.LEFT, 15, 5));
        this.visualBridge = visualBridge;

        setBorder(BorderFactory.createTitledBorder("状态"));
        setPreferredSize(new Dimension(300, 60));

        initializeStatusLabels();
        initializeTimer();
    }

    private void initializeStatusLabels() {
        Font labelFont = new Font("Monospaced", Font.PLAIN, 12);

        stateLabel = new JLabel("状态: 空闲");
        stateLabel.setFont(labelFont);
        add(stateLabel);

        pcLabel = new JLabel("PC: 0x0000");
        pcLabel.setFont(labelFont);
        add(pcLabel);

        stepsLabel = new JLabel("指令: 0");
        stepsLabel.setFont(labelFont);
        add(stepsLabel);

        timeLabel = new JLabel("时间: 0.000s");
        timeLabel.setFont(labelFont);
        add(timeLabel);

        instructionLabel = new JLabel("当前指令: -");
        instructionLabel.setFont(labelFont);
        instructionLabel.setPreferredSize(new Dimension(200, 20));
        add(instructionLabel);
    }

    private void initializeTimer() {
        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startTime > 0) {
                    double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                    timeLabel.setText(String.format("时间: %.3fs", elapsed));
                }
            }
        });
    }

    public void updateState(VMStateChangeEvent.State state) {
        String stateText;
        switch (state) {
            case CREATED:
                stateText = "已创建";
                break;
            case LOADED:
                stateText = "已加载";
                break;
            case RUNNING:
                stateText = "运行中";
                break;
            case PAUSED:
                stateText = "已暂停";
                break;
            case STEPPING:
                stateText = "单步";
                break;
            case HALTED:
                stateText = "已停止";
                break;
            case ERROR:
                stateText = "错误";
                break;
            default:
                stateText = "未知";
        }
        stateLabel.setText("状态: " + stateText);
    }

    public void updatePC(int pc) {
        pcLabel.setText(String.format("PC: 0x%04X", pc));
    }

    public void updateInstruction(String mnemonic, String operands) {
        if (operands != null && !operands.isEmpty()) {
            instructionLabel.setText("当前: " + mnemonic + " " + operands);
        } else {
            instructionLabel.setText("当前: " + mnemonic);
        }
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        timer.start();
    }

    public void stopTimer() {
        timer.stop();
    }

    public void refresh() {
        updateState(visualBridge.getStateModel().getVMState());
        updatePC(visualBridge.getCurrentPC());
        stepsLabel.setText("指令: " + visualBridge.getStateModel().getExecutionSteps());
    }

    public void updateStatus(String message) {
        stateLabel.setText(message);
    }
}
