package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes;

/**
 * 状态视图组件
 *
 * <p>显示执行统计、VM状态、PC、指令信息</p>
 */
public class StatusView extends HBox {

    private final Label stateLabel;
    private final Label pcLabel;
    private final Label stepsLabel;
    private final Label instructionLabel;
    private final Label timeLabel;

    public StatusView() {
        setSpacing(15);
        setStyle("-fx-padding: 10; -fx-background-color: #2C3E50; -fx-border-color: #1F2937; -fx-border-width: 1;");

        stateLabel = createStatusLabel("状态:", Color.web("#666666"));
        pcLabel = createStatusLabel("PC:", Color.web("#666666"));
        stepsLabel = createStatusLabel("指令:", Color.web("#666666"));

        timeLabel = new Label("时间: 0.0s");
        timeLabel.setTextFill(Color.web("#999999"));
        timeLabel.setStyle("-fx-font-size: 14px;");

        instructionLabel = new Label("当前: -");
        instructionLabel.setTextFill(Color.web("#999999"));
        instructionLabel.setStyle("-fx-font-size: 14px;");

        getChildren().addAll(stateLabel, pcLabel, stepsLabel, timeLabel, instructionLabel);
    }

    private Label createStatusLabel(String text, Color textColor) {
        Label label = new Label(text);
        label.setTextFill(textColor);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }

    public void updateState(VMTypes.VMState state) {
        stateLabel.setText(String.format("状态: %s", getStateText(state)));
        updateStateColor(state);
    }

    public void updatePC(int pc) {
        pcLabel.setText(String.format("PC: 0x%04X", pc));
    }

    public void updateSteps(long steps) {
        stepsLabel.setText(String.format("指令: %d", steps));
    }

    public void updateTime(double time) {
        timeLabel.setText(String.format("时间: %.3fs", time));
    }

    public void updateInstruction(String instruction) {
        instructionLabel.setText(String.format("当前: %s", instruction));
    }

    private String getStateText(VMTypes.VMState state) {
        switch (state) {
            case CREATED: return "已创建";
            case LOADED: return "已加载";
            case RUNNING: return "运行中";
            case PAUSED: return "已暂停";
            case STEPPING: return "单步执行";
            case HALTED: return "已停止";
            case ERROR: return "错误";
            default: return state.toString();
        }
    }

    private void updateStateColor(VMTypes.VMState state) {
        Color color = Color.web("#666666");
        switch (state) {
            case CREATED:
            case LOADED:
                color = Color.web("#999999");
                break;
            case RUNNING:
                color = Color.web("#90EE90");
                break;
            case PAUSED:
                color = Color.web("#FFB6C1");
                break;
            case HALTED:
                color = Color.web("#DCDCDC");
                break;
            case ERROR:
                color = Color.web("#FF0000");
                break;
        }
        stateLabel.setTextFill(color);
    }
}
