package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;

/**
 * 响应式状态视图
 * 使用RxJava自动订阅状态变化，无需手动刷新
 */
public class ReactiveStatusView extends Region {

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // UI组件
    private final Label stateLabel = new Label("状态: 空闲");
    private final Label pcLabel = new Label("PC: 0x0000");
    private final Label stepsLabel = new Label("指令: 0");
    private final Label timeLabel = new Label("时间: 0.000s");
    private final Label instructionLabel = new Label("当前指令: -");

    public ReactiveStatusView(ReactiveVMRStateModel stateModel) {
        this.stateModel = stateModel;
        buildUI();
        bindToState();
    }

    private void buildUI() {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #F0F0F0; -fx-border-color: #CCCCCC; -fx-border-width: 1 0 0 0;");

        String fontStyle = "-fx-font-family: 'Monospaced'; -fx-font-size: 12px;";
        stateLabel.setStyle(fontStyle);
        pcLabel.setStyle(fontStyle);
        stepsLabel.setStyle(fontStyle);
        timeLabel.setStyle(fontStyle);
        instructionLabel.setStyle(fontStyle);

        instructionLabel.setMinWidth(200);
        instructionLabel.setMaxWidth(200);

        statusBar.getChildren().addAll(stateLabel, pcLabel, stepsLabel, timeLabel, instructionLabel);

        getChildren().add(statusBar);
        setMinSize(300, 60);
    }

    private void bindToState() {
        // 订阅执行状态变化
        disposables.add(
            stateModel.getExecutionStatus()
                .subscribeOn(Schedulers.computation())
                .subscribe(status -> Platform.runLater(() -> updateState(status)), Throwable::printStackTrace)
        );

        // 订阅PC变化
        disposables.add(
            stateModel.getPC()
                .subscribeOn(Schedulers.computation())
                .subscribe(pc -> Platform.runLater(() -> {
                    pcLabel.setText(String.format("PC: 0x%04X", pc));
                }), Throwable::printStackTrace)
        );

        // 订阅执行步数
        disposables.add(
            stateModel.getStepsObs()
                .subscribeOn(Schedulers.computation())
                .subscribe(steps -> Platform.runLater(() -> {
                    stepsLabel.setText(String.format("指令: %d", steps));
                    timeLabel.setText(String.format("时间: %.3fs", steps * 0.2));
                }), Throwable::printStackTrace)
        );

        // 订阅当前指令
        disposables.add(
            stateModel.getInstruction()
                .subscribeOn(Schedulers.computation())
                .subscribe(info -> Platform.runLater(() -> {
                    if (info.operands() != null && !info.operands().isEmpty()) {
                        instructionLabel.setText("当前: " + info.mnemonic() + " " + info.operands());
                    } else {
                        instructionLabel.setText("当前: " + info.mnemonic());
                    }
                }), Throwable::printStackTrace)
        );
    }

    private void updateState(ReactiveVMRStateModel.ExecutionStatus status) {
        String stateText;
        switch (status) {
            case IDLE -> stateText = "空闲";
            case RUNNING -> stateText = "运行中";
            case PAUSED -> stateText = "已暂停";
            case STEPPING -> stateText = "单步";
            case HALTED -> stateText = "已停止";
            case ERROR -> stateText = "错误";
            default -> stateText = "未知";
        }
        stateLabel.setText("状态: " + stateText);
    }

    public void dispose() {
        disposables.clear();
    }
}
