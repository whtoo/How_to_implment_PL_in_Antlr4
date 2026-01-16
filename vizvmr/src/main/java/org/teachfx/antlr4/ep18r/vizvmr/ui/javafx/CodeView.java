package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

import java.util.HashSet;
import java.util.Set;

/**
 * Code/Disassembly panel - JavaFX version
 * Displays disassembled code with PC highlighting and breakpoint support
 */
public class CodeView extends JFXPanelBase {

    private final VMRVisualBridge visualBridge;
    private ListView<String> instructionList;
    private ObservableList<String> instructionData;
    private final Set<Integer> breakpoints;
    private int currentPC = -1;

    public CodeView(VMRVisualBridge visualBridge) {
        super("CodeView");
        this.visualBridge = visualBridge;
        this.breakpoints = new HashSet<>();
        buildUI();  // 在对象完全构造后初始化UI
    }

    @Override
    protected void initializeComponents() {
        setTitle("代码");
        setMinSize(400, 400);

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Create toolbar
        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        // Create instruction list
        instructionData = FXCollections.observableArrayList();
        instructionList = new ListView<>(instructionData);
        instructionList.setCellFactory(list -> new BreakpointListCell());
        instructionList.getStyleClass().add("code-list");

        VBox.setVgrow(instructionList, Priority.ALWAYS);
        mainLayout.getChildren().add(instructionList);

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

        Button toggleBreakpointButton = new Button("切换断点");
        toggleBreakpointButton.setOnAction(e -> toggleBreakpointAtSelection());

        Button clearBreakpointsButton = new Button("清除所有");
        clearBreakpointsButton.setOnAction(e -> clearAllBreakpoints());

        toolbar.getChildren().addAll(refreshButton, toggleBreakpointButton, clearBreakpointsButton);

        return toolbar;
    }

    /**
     * Set instructions from disassembler
     */
    public void setInstructions(RegisterDisAssembler disAssembler) {
        instructionData.clear();
        if (disAssembler != null) {
            String disassembly = disAssembler.disassembleToString();
            String[] lines = disassembly.split("\n");
            instructionData.addAll(lines);
        }
    }

    /**
     * Highlight current PC position
     */
    public void highlightPC(int pc) {
        currentPC = pc;
        // Calculate line index (each instruction is 4 bytes)
        int lineIndex = pc / 4;
        if (lineIndex >= 0 && lineIndex < instructionData.size()) {
            instructionList.getSelectionModel().select(lineIndex);
            instructionList.scrollTo(lineIndex);
        }
    }

    /**
     * Toggle breakpoint at specific PC
     */
    public void toggleBreakpoint(int pc) {
        if (breakpoints.contains(pc)) {
            breakpoints.remove(pc);
        } else {
            breakpoints.add(pc);
        }
        instructionList.refresh();
    }

    /**
     * Toggle breakpoint at selected line
     */
    public void toggleBreakpointAtSelection() {
        int selectedIndex = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            int pc = selectedIndex * 4;
            toggleBreakpoint(pc);
        }
    }

    /**
     * Clear all breakpoints
     */
    public void clearAllBreakpoints() {
        breakpoints.clear();
        instructionList.refresh();
    }

    /**
     * Check if breakpoint exists at PC
     */
    public boolean isBreakpointAt(int pc) {
        return breakpoints.contains(pc);
    }

    /**
     * Get all breakpoints
     */
    public Set<Integer> getBreakpoints() {
        return new HashSet<>(breakpoints);
    }

    /**
     * Refresh display
     */
    public void refresh() {
        instructionList.refresh();
    }

    /**
     * Get instruction list for external access
     */
    public ListView<String> getInstructionList() {
        return instructionList;
    }

    /**
     * Custom list cell for displaying instructions with breakpoints and PC highlight
     */
    private class BreakpointListCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
            } else {
                setText(item);
                setGraphic(null);

                int index = getIndex();
                int pc = index * 4;

                // Apply breakpoint style
                if (breakpoints.contains(pc)) {
                    setText("● " + item);
                    setTextFill(Color.RED);
                } else {
                    setTextFill(Color.BLACK);
                }

                // Apply PC highlight
                if (index * 4 == currentPC) {
                    setBackground(new Background(new BackgroundFill(
                            Color.rgb(255, 255, 0, 0.3), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    setBackground(null);
                }
            }
        }
    }
}
