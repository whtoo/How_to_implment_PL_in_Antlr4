package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashSet;
import java.util.Set;

/**
 * 响应式代码视图
 * 使用RxJava自动订阅PC变化和断点变化
 */
public class ReactiveCodeView extends BorderPane {
    
    private static final Logger logger = LogManager.getLogger(ReactiveCodeView.class);

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private ListView<String> instructionList;
    private ObservableList<String> instructionData;
    private final Set<Integer> breakpoints = new HashSet<>();
    private volatile int currentPC = -1;

    public ReactiveCodeView(ReactiveVMRStateModel stateModel) {
        logger.info("创建ReactiveCodeView");
        this.stateModel = stateModel;
        buildUI();
        bindToState();
    }

    private void buildUI() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // 工具栏
        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        // 指令列表
        instructionData = FXCollections.observableArrayList();
        instructionList = new ListView<>(instructionData);
        instructionList.setCellFactory(list -> new ReactiveListCell());
        instructionList.getStyleClass().add("code-list");

        VBox.setVgrow(instructionList, Priority.ALWAYS);
        mainLayout.getChildren().add(instructionList);

        setCenter(mainLayout);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> instructionList.refresh());

        Button toggleBreakpointButton = new Button("切换断点");
        toggleBreakpointButton.setOnAction(e -> toggleBreakpointAtSelection());

        Button clearBreakpointsButton = new Button("清除所有");
        clearBreakpointsButton.setOnAction(e -> {
            breakpoints.clear();
            instructionList.refresh();
        });

        toolbar.getChildren().addAll(refreshButton, toggleBreakpointButton, clearBreakpointsButton);
        return toolbar;
    }

    private void bindToState() {
        logger.debug("绑定状态订阅");
        
        // 订阅PC变化 - 高亮当前指令
        disposables.add(
            stateModel.getPC()
                .subscribeOn(Schedulers.computation())
                .subscribe(pc -> {
                    logger.debug("PC变化: {}", pc);
                    Platform.runLater(() -> {
                        currentPC = pc;
                        highlightPC(pc);
                    });
                }, error -> {
                    logger.error("PC订阅错误", error);
                    error.printStackTrace();
                })
        );

        // 订阅执行状态变化 - 高亮当前行
        disposables.add(
            stateModel.getExecutionStatus()
                .subscribeOn(Schedulers.computation())
                .subscribe(status -> {
                    logger.debug("VM状态变化: {}", status);
                    Platform.runLater(() -> {
                        if (status == VMStateChangeEvent.State.RUNNING ||
                            status == VMStateChangeEvent.State.PAUSED ||
                            status == VMStateChangeEvent.State.STEPPING) {
                            instructionList.refresh();
                        }
                    });
                }, error -> {
                    logger.error("状态订阅错误", error);
                    error.printStackTrace();
                })
        );
        
        logger.info("状态绑定完成");
    }

    public void setInstructions(RegisterDisAssembler disAssembler) {
        logger.info("设置反汇编指令，disAssembler: {}", disAssembler != null ? "非空" : "空");
        Platform.runLater(() -> {
            instructionData.clear();
            if (disAssembler != null) {
                String disassembly = disAssembler.disassembleToString();
                String[] lines = disassembly.split("\n");
                logger.info("添加 {} 行指令", lines.length);

                for (int i = 0; i < lines.length; i++) {
                    if (!lines[i].isEmpty()) {
                        logger.trace("行 {}: {}", i, lines[i]);
                        instructionData.add(lines[i]);
                    }
                }
            } else {
                logger.warn("反汇编器为空");
            }
            logger.info("ListView中总项数: {}", instructionData.size());
        });
    }

    private void highlightPC(int pc) {
        int lineIndex = pc / 4;
        if (lineIndex >= 0 && lineIndex < instructionData.size()) {
            instructionList.getSelectionModel().select(lineIndex);
            instructionList.scrollTo(lineIndex);
        }
        instructionList.refresh();
    }

    public void toggleBreakpoint(int pc) {
        if (breakpoints.contains(pc)) {
            breakpoints.remove(pc);
        } else {
            breakpoints.add(pc);
        }
        instructionList.refresh();
    }

    public void toggleBreakpointAtSelection() {
        int selectedIndex = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            int pc = selectedIndex * 4;
            toggleBreakpoint(pc);
        }
    }

    public void clearAllBreakpoints() {
        breakpoints.clear();
        instructionList.refresh();
    }

    public void dispose() {
        disposables.clear();
    }

    private class ReactiveListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
            } else {
                int index = getIndex();
                int pc = index * 4;

                StringBuilder prefix = new StringBuilder();

                // 断点标记
                if (breakpoints.contains(pc)) {
                    prefix.append("● ");
                } else {
                    prefix.append("  ");
                }

                // PC游标高亮
                if (index * 4 == currentPC) {
                    prefix.append("➜ ");
                    setBackground(new Background(new BackgroundFill(
                            Color.rgb(173, 216, 230, 150), CornerRadii.EMPTY, Insets.EMPTY)));
                    setTextFill(Color.BLUE);
                } else if (breakpoints.contains(pc)) {
                    setTextFill(Color.RED);
                } else {
                    setTextFill(Color.BLACK);
                    prefix.append("  ");
                }

                setText(prefix.toString() + item);
            }
        }
    }
}
