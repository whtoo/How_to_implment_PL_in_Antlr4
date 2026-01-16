package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

/**
 * Status bar panel - JavaFX version
 * Displays execution statistics: instruction count, runtime, VM state
 */
public class StatusView extends JFXPanelBase {

    private final VMRVisualBridge visualBridge;
    private Label stateLabel = new Label("状态: 空闲");
    private Label pcLabel = new Label("PC: 0x0000");
    private Label stepsLabel = new Label("指令: 0");
    private Label timeLabel = new Label("时间: 0.000s");
    private Label instructionLabel = new Label("当前指令: -");
    private final AnimationTimer timer;
    private long startTime;

    public StatusView(VMRVisualBridge visualBridge) {
        super("StatusView");
        this.visualBridge = visualBridge;
        this.startTime = 0;

        // Create animation timer for elapsed time
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startTime > 0) {
                    double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                    Platform.runLater(() -> {
                        timeLabel.setText(String.format("时间: %.3fs", elapsed));
                    });
                }
            }
        };
        buildUI();  // 在对象完全构造后初始化UI
    }

    @Override
    protected void initializeComponents() {
        setTitle("状态");
        setMinSize(300, 60);

        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: #CCCCCC; -fx-border-width: 1 0 0 0;");

        // Initialize labels
        stateLabel = new Label("状态: 空闲");
        pcLabel = new Label("PC: 0x0000");
        stepsLabel = new Label("指令: 0");
        timeLabel = new Label("时间: 0.000s");
        instructionLabel = new Label("当前指令: -");

        // Set monospaced font for all labels
        String fontStyle = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px;";
        stateLabel.setStyle(fontStyle);
        pcLabel.setStyle(fontStyle);
        stepsLabel.setStyle(fontStyle);
        timeLabel.setStyle(fontStyle);
        instructionLabel.setStyle(fontStyle);

        // Set fixed width for instruction label
        instructionLabel.setMinWidth(200);
        instructionLabel.setMaxWidth(200);

        statusBar.getChildren().addAll(stateLabel, pcLabel, stepsLabel, timeLabel, instructionLabel);

        setCenter(statusBar);
    }

    /**
     * Update VM state display
     */
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

    /**
     * Update PC display
     */
    public void updatePC(int pc) {
        pcLabel.setText(String.format("PC: 0x%04X", pc));
    }

    /**
     * Update current instruction display
     */
    public void updateInstruction(String mnemonic, String operands) {
        if (operands != null && !operands.isEmpty()) {
            instructionLabel.setText("当前: " + mnemonic + " " + operands);
        } else {
            instructionLabel.setText("当前: " + mnemonic);
        }
    }

    /**
     * Start execution timer
     */
    public void startTimer() {
        startTime = System.currentTimeMillis();
        timer.start();
    }

    /**
     * Stop execution timer
     */
    public void stopTimer() {
        timer.stop();
    }

    /**
     * Refresh all status displays
     */
    public void refresh() {
        updateState(visualBridge.getStateModel().getVMState());
        updatePC(visualBridge.getCurrentPC());
        stepsLabel.setText("指令: " + visualBridge.getStateModel().getExecutionSteps());
    }

    /**
     * Update status message
     */
    public void updateStatus(String message) {
        stateLabel.setText(message);
    }
}
