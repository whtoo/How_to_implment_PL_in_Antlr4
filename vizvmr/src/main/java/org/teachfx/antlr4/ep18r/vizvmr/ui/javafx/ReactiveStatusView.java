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
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.InstructionExecutionEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * 响应式状态视图
 * 使用RxJava自动订阅状态变化，无需手动刷新
 */
public class ReactiveStatusView extends Region {
    
    private static final Logger logger = LogManager.getLogger(ReactiveStatusView.class);

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // UI组件
    private final Label stateLabel = new Label("状态: 空闲");
    private final Label pcLabel = new Label("PC: 0x0000");
    private final Label stepsLabel = new Label("指令: 0");
    private final Label timeLabel = new Label("时间: 0.000s");
    private final Label instructionLabel = new Label("当前指令: -");

    public ReactiveStatusView(ReactiveVMRStateModel stateModel) {
        logger.info("创建ReactiveStatusView");
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
        logger.debug("绑定状态订阅");
        
        // 订阅执行状态变化
        disposables.add(
            stateModel.getVMStateObs()
                .subscribeOn(Schedulers.computation())
                .subscribe(status -> {
                    logger.debug("VM状态变化: {}", status);
                    Platform.runLater(() -> updateState(status));
                }, error -> {
                    logger.error("VM状态订阅错误", error);
                    error.printStackTrace();
                })
        );

        // 订阅PC变化
        disposables.add(
            stateModel.getPC()
                .subscribeOn(Schedulers.computation())
                .subscribe(pc -> {
                    logger.trace("PC变化: {}", pc);
                    Platform.runLater(() -> {
                        pcLabel.setText(String.format("PC: 0x%04X", pc));
                    });
                }, error -> {
                    logger.error("PC订阅错误", error);
                    error.printStackTrace();
                })
        );

        // 订阅执行步数
        disposables.add(
            stateModel.getStepsObs()
                .subscribeOn(Schedulers.computation())
                .subscribe(steps -> {
                    logger.trace("执行步数: {}", steps);
                    Platform.runLater(() -> {
                        stepsLabel.setText(String.format("指令: %d", steps));
                        timeLabel.setText(String.format("时间: %.3fs", steps * 0.2));
                    });
                }, error -> {
                    logger.error("执行步数订阅错误", error);
                    error.printStackTrace();
                })
        );

        // 订阅当前指令
        disposables.add(
            stateModel.getInstruction()
                .subscribeOn(Schedulers.computation())
                .subscribe(info -> {
                    logger.debug("指令执行: PC={}, 操作码={}, 助记符={}, 操作数={}",
                        info.getPC(), info.getOpcode(), info.getMnemonic(), info.getOperands());
                    Platform.runLater(() -> {
                        if (info.getOperands() != null && !info.getOperands().isEmpty()) {
                            instructionLabel.setText("当前: " + info.getMnemonic() + " " + info.getOperands());
                        } else {
                            instructionLabel.setText("当前: " + info.getMnemonic());
                        }
                    });
                }, error -> {
                    logger.error("指令订阅错误", error);
                    error.printStackTrace();
                })
        );
        
        logger.info("状态绑定完成");
    }

    private void updateState(VMStateChangeEvent.State status) {
        String stateText;
        switch (status) {
            case CREATED -> stateText = "已创建";
            case RUNNING -> stateText = "运行中";
            case PAUSED -> stateText = "已暂停";
            case STEPPING -> stateText = "单步";
            case HALTED -> stateText = "已停止";
            default -> stateText = "未知";
        }
        stateLabel.setText("状态: " + stateText);
    }

    public void dispose() {
        disposables.clear();
    }
}
