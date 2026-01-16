package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

/**
 * Register display panel - JavaFX version
 * 4x4 grid displaying 16 registers with color coding
 */
public class RegisterView extends JFXPanelBase {

    private final VMRVisualBridge visualBridge;
    private Label[] registerLabels = new Label[16];
    private Label[] nameLabels = new Label[16];
    private StackPane[] cellPanes = new StackPane[16];
    private int[] previousValues = new int[16];

    // Register names
    private static final String[] REGISTER_NAMES = {
        "r0", "r1", "r2", "r3",
        "r4", "r5", "r6", "r7",
        "r8", "r9", "r10", "r11",
        "r12", "r13(SP)", "r14(FP)", "r15(LR)"
    };

    // Special register indices
    private static final int SP_REGISTER = 13;  // r13
    private static final int FP_REGISTER = 14;  // r14
    private static final int LR_REGISTER = 15;  // r15

    // Color definitions
    private static final String COLOR_ZERO = "#90EE90";      // light green - r0
    private static final String COLOR_MODIFIED = "#FFB6C1";  // light red - recently modified
    private static final String COLOR_SPECIAL = "#ADD8E6";   // light blue - special registers
    private static final String COLOR_NORMAL = "#DCDCDC";    // light gray - normal
    private static final String COLOR_HIGHLIGHT = "#FFFF00"; // yellow highlight

    public RegisterView(VMRVisualBridge visualBridge) {
        super("RegisterView");
        this.visualBridge = visualBridge;
        buildUI();  // 在对象完全构造后初始化UI
    }

    @Override
    protected void initializeComponents() {
        setTitle("寄存器");
        setMinSize(300, 250);

        // Create 4x4 grid
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10));
        gridPane.setAlignment(Pos.TOP_LEFT);

        // Set column constraints
        for (int col = 0; col < 4; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(25);
            colConstraints.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(colConstraints);
        }

        // Set row constraints
        for (int row = 0; row < 4; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(25);
            rowConstraints.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rowConstraints);
        }

        // Initialize each register cell
        for (int i = 0; i < 16; i++) {
            StackPane cellPane = createRegisterCell(i);
            cellPanes[i] = cellPane;

            int row = i / 4;
            int col = i % 4;
            gridPane.add(cellPane, col, row);
        }

        // Add grid to center of BorderPane
        setCenter(gridPane);
    }

    /**
     * Create a single register cell
     */
    private StackPane createRegisterCell(int regNum) {
        StackPane cellPane = new StackPane();
        cellPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        cellPane.setPadding(new Insets(8));
        cellPane.setBackground(new Background(new BackgroundFill(
                Color.web(COLOR_NORMAL), CornerRadii.EMPTY, Insets.EMPTY)));

        // Register name label
        Label nameLabel = new Label(REGISTER_NAMES[regNum]);
        nameLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #404040;");
        nameLabel.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(nameLabel, Pos.TOP_LEFT);
        nameLabels[regNum] = nameLabel;

        // Register value label
        Label valueLabel = new Label("0x00000000 (0)");
        valueLabel.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        valueLabel.setAlignment(Pos.CENTER_RIGHT);
        StackPane.setAlignment(valueLabel, Pos.CENTER_RIGHT);
        HBox.setHgrow(valueLabel, Priority.ALWAYS);
        registerLabels[regNum] = valueLabel;

        // Use HBox for horizontal layout
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(nameLabel, valueLabel);

        cellPane.getChildren().add(hbox);

        return cellPane;
    }

    /**
     * Update a single register value
     */
    public void updateRegister(int regNum, int value) {
        if (regNum >= 0 && regNum < 16) {
            boolean valueChanged = (value != previousValues[regNum]);
            previousValues[regNum] = value;

            String text = String.format("0x%08X (%d)", value, value);
            registerLabels[regNum].setText(text);

            // Apply color coding
            applyColorCoding(regNum, valueChanged);
        }
    }

    /**
     * Refresh all register displays
     */
    public void refresh() {
        for (int i = 0; i < 16; i++) {
            int value = visualBridge.getRegister(i);
            updateRegister(i, value);
        }
    }

    /**
     * Apply color coding
     */
    private void applyColorCoding(int regNum, boolean valueChanged) {
        String backgroundColor;

        if (regNum == 0) {
            // r0 is zero register, use green
            backgroundColor = COLOR_ZERO;
        } else if (isSpecialRegister(regNum)) {
            // Special registers (r13-SP, r14-FP, r15-LR), use blue
            backgroundColor = COLOR_SPECIAL;
        } else if (valueChanged) {
            // Recently modified register, use red
            backgroundColor = COLOR_MODIFIED;
        } else {
            // Unmodified register, use gray
            backgroundColor = COLOR_NORMAL;
        }

        cellPanes[regNum].setBackground(new Background(new BackgroundFill(
                Color.web(backgroundColor), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    /**
     * Check if register is a special register
     */
    private boolean isSpecialRegister(int regNum) {
        return regNum == SP_REGISTER || regNum == FP_REGISTER || regNum == LR_REGISTER;
    }

    /**
     * Highlight a specific register
     */
    public void highlightRegister(int regNum) {
        if (regNum >= 0 && regNum < 16) {
            cellPanes[regNum].setBackground(new Background(new BackgroundFill(
                    Color.web(COLOR_HIGHLIGHT), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    /**
     * Highlight a specific register with custom color
     */
    public void highlightRegister(int regNum, String color) {
        if (regNum >= 0 && regNum < 16) {
            cellPanes[regNum].setBackground(new Background(new BackgroundFill(
                    Color.web(color), CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    /**
     * Clear all highlights
     */
    public void clearHighlights() {
        for (int i = 0; i < 16; i++) {
            applyColorCoding(i, false);
        }
    }

    /**
     * Reset all register colors to default state
     */
    public void resetColors() {
        for (int i = 0; i < 16; i++) {
            previousValues[i] = 0;
            applyColorCoding(i, false);
        }
    }

    /**
     * Get register value label (for external access)
     */
    public Label getRegisterLabel(int regNum) {
        if (regNum >= 0 && regNum < 16) {
            return registerLabels[regNum];
        }
        return null;
    }

    /**
     * Get register cell pane (for external access)
     */
    public StackPane getCellPane(int regNum) {
        if (regNum >= 0 && regNum < 16) {
            return cellPanes[regNum];
        }
        return null;
    }
}
