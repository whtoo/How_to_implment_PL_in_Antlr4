package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

/**
 * Call stack panel - JavaFX version
 * Vertical list displaying call stack frames
 */
public class StackView extends JFXPanelBase {

    private final VMRVisualBridge visualBridge;
    private ListView<String> stackList;
    private ObservableList<String> stackData;

    public StackView(VMRVisualBridge visualBridge) {
        super("StackView");
        this.visualBridge = visualBridge;
        buildUI();  // 在对象完全构造后初始化UI
    }

    @Override
    protected void initializeComponents() {
        setTitle("调用栈");
        setMinSize(300, 250);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Create toolbar
        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        // Create stack list
        stackData = FXCollections.observableArrayList();
        stackList = new ListView<>(stackData);
        stackList.setCellFactory(list -> new StackFrameCell());
        VBox.setVgrow(stackList, Priority.ALWAYS);
        mainLayout.getChildren().add(stackList);

        setCenter(mainLayout);
    }

    /**
     * Create toolbar with controls
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> refresh());

        toolbar.getChildren().add(refreshButton);

        return toolbar;
    }

    /**
     * Refresh call stack display
     */
    public void refresh() {
        stackData.clear();

        StackFrame[] callStack = visualBridge.getStateModel().getCallStack();
        int depth = visualBridge.getStateModel().getCallStackDepth();

        for (int i = depth - 1; i >= 0; i--) {
            StackFrame frame = callStack[i];
            if (frame != null && frame.symbol != null) {
                String frameInfo = String.format("[%d] %s @ 0x%04X (RA: 0x%04X)",
                    i, frame.symbol.name, frame.symbol.address, frame.returnAddress);
                stackData.add(frameInfo);
            }
        }

        if (stackData.isEmpty()) {
            stackData.add("<空调用栈>");
        }
    }

    /**
     * Get stack list for external access
     */
    public ListView<String> getStackList() {
        return stackList;
    }

    /**
     * Custom list cell for displaying stack frames
     */
    private static class StackFrameCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(null);
                setFont(javafx.scene.text.Font.font("Monospaced", 12));
            }
        }
    }
}
