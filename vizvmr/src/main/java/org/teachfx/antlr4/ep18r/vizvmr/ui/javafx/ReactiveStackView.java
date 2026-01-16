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
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

/**
 * 响应式调用栈视图
 * 完整显示栈帧信息，包括函数名、返回地址、保存的寄存器等
 */
public class ReactiveStackView extends BorderPane {

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private ListView<StackFrame> stackList;
    private ObservableList<StackFrame> stackData;

    private int currentDepth = 0;

    public ReactiveStackView(ReactiveVMRStateModel stateModel) {
        this.stateModel = stateModel;
        buildUI();
        bindToState();
    }

    private void buildUI() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        stackData = FXCollections.observableArrayList();
        stackList = new ListView<>(stackData);
        stackList.setCellFactory(list -> new StackFrameCell());
        stackList.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 11px;");

        VBox.setVgrow(stackList, Priority.ALWAYS);
        mainLayout.getChildren().add(stackList);

        setCenter(mainLayout);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> refresh());

        Label depthLabel = new Label("深度: 0");

        toolbar.getChildren().addAll(refreshButton, new Separator(), depthLabel);

        return toolbar;
    }

    private void bindToState() {
        disposables.add(
            stateModel.getExecutionStatus()
                .subscribeOn(Schedulers.computation())
                .subscribe(status -> Platform.runLater(() -> {
                    if (status == ReactiveVMRStateModel.ExecutionStatus.RUNNING ||
                        status == ReactiveVMRStateModel.ExecutionStatus.PAUSED ||
                        status == ReactiveVMRStateModel.ExecutionStatus.STEPPING) {
                        refresh();
                    }
                }), Throwable::printStackTrace)
        );
    }

    public void refresh() {
        StackFrame[] callStack = stateModel.getCallStack();

        stackData.clear();

        if (callStack == null || callStack.length == 0) {
            stackData.add(createEmptyStackFrame());
        } else {
            for (int i = 0; i < callStack.length; i++) {
                StackFrame frame = callStack[i];
                if (frame != null) {
                    stackData.add(frame);
                }
            }
        }

        updateDepthDisplay();
        stackList.refresh();
    }

    private StackFrame createEmptyStackFrame() {
        return new StackFrame(null, 0, 0);
    }

    private void updateDepthDisplay() {
        int depth = stateModel.getCallStackDepth();
        currentDepth = depth;
    }

    public void dispose() {
        disposables.clear();
    }

    private static class StackFrameCell extends ListCell<StackFrame> {

        @Override
        protected void updateItem(StackFrame frame, boolean empty) {
            super.updateItem(frame, empty);

            if (empty || frame == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
                setTooltip(null);
            } else {
                String functionName = formatFunctionName(frame);
                String returnAddr = formatReturnAddress(frame);
                String frameBase = formatFrameBase(frame);
                String savedRegs = formatSavedRegisters(frame);
                String depthIndicator = formatDepthIndicator(getIndex());

                String displayText = String.format("%s %s | %s | %s",
                        depthIndicator, functionName, returnAddr, savedRegs);

                setText(displayText);
                setGraphic(null);
                setStyle(getFrameStyle(getIndex()));
                setTooltip(createTooltip(frame));
            }
        }

        private String formatFunctionName(StackFrame frame) {
            if (frame.getFunctionSymbol() != null) {
                return String.format("函数: %s", frame.getFunctionSymbol().name);
            }
            return "函数: <匿名>";
        }

        private String formatReturnAddress(StackFrame frame) {
            return String.format("返回: 0x%04X", frame.getReturnAddress());
        }

        private String formatFrameBase(StackFrame frame) {
            return String.format("帧基址: 0x%04X", frame.getFrameBasePointer());
        }

        private String formatSavedRegisters(StackFrame frame) {
            int[] savedRegs = frame.getSavedCallerRegisters();
            if (savedRegs == null || savedRegs.length == 0) {
                return "保存寄存器: 无";
            }

            StringBuilder sb = new StringBuilder("保存寄存器: ");
            for (int i = 0; i < savedRegs.length; i++) {
                String regName = getRegisterName(i);
                String value = formatRegisterValue(savedRegs[i]);
                sb.append(regName).append("=").append(value);
                if (i < savedRegs.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        private String getRegisterName(int index) {
            String[] names = {"r1", "r2", "r3", "r4", "r5", "r6", "r7", "LR"};
            return index >= 0 && index < names.length ? names[index] : "r" + (index + 1);
        }

        private String formatRegisterValue(int value) {
            return String.format("0x%08X(%d)", value, value);
        }

        private String formatDepthIndicator(int index) {
            return String.format("[%d]", index);
        }

        private String getFrameStyle(int index) {
            if (index == 0) {
                return "-fx-background-color: #FFE4E1; -fx-text-fill: #8B0000; -fx-font-weight: bold;";
            } else if (index % 2 == 0) {
                return "-fx-background-color: #F5F5DC; -fx-text-fill: #000000;";
            } else {
                return "-fx-background-color: #FFFFFF; -fx-text-fill: #333333;";
            }
        }

        private Tooltip createTooltip(StackFrame frame) {
            StringBuilder tooltip = new StringBuilder();
            tooltip.append("函数名: ").append(frame.getFunctionSymbol() != null ?
                    frame.getFunctionSymbol().name : "<匿名>").append("\n");
            tooltip.append("返回地址: ").append(String.format("0x%04X", frame.getReturnAddress())).append("\n");
            tooltip.append("帧基址: ").append(String.format("0x%04X", frame.getFrameBasePointer())).append("\n");

            int[] savedRegs = frame.getSavedCallerRegisters();
            if (savedRegs != null && savedRegs.length > 0) {
                tooltip.append("保存的寄存器:\n");
                for (int i = 0; i < savedRegs.length; i++) {
                    String regName = getRegisterName(i);
                    tooltip.append(String.format("  %s = %s (0x%08X, %d)\n",
                            regName, formatRegisterValue(savedRegs[i]), savedRegs[i]));
                }
            }

            return new Tooltip(tooltip.toString());
        }
    }
}
