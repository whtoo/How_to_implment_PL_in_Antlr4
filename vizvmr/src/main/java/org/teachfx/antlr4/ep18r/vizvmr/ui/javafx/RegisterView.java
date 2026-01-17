package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

/**
 * 寄存器视图组件
 *
 * <p>4x4网格显示16个寄存器，支持颜色编码</p>
 */
public class RegisterView extends Pane {

    private final GridPane grid;
    private final java.util.Map<Integer, RegisterCell> cells;

    public RegisterView() {
        this.grid = new GridPane();
        this.cells = new java.util.HashMap<>();

        initializeGrid();
        setupLayout();
    }

    private void initializeGrid() {
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setStyle("-fx-padding: 10; -fx-background-color: #F5F5F5;");

        for (int i = 0; i < 16; i++) {
            RegisterCell cell = new RegisterCell(i);
            cells.put(i, cell);

            int row = i / 4;
            int col = i % 4;

            Label nameLabel = new Label(String.format("r%d", i));
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");
            grid.add(nameLabel, col, row);

            grid.add(cell.getValuePane(), col, row + 1);
        }
    }

    private void setupLayout() {
        getChildren().add(grid);
    }

    public void updateRegisters(int[] registers) {
        if (registers == null || registers.length < 16) {
            return;
        }

        for (int i = 0; i < 16; i++) {
            RegisterCell cell = cells.get(i);
            if (cell != null) {
                cell.updateValue(registers[i]);
            }
        }
    }

    public void highlightRegister(int regNum) {
        RegisterCell cell = cells.get(regNum);
        if (cell != null) {
            cell.highlight();
        }
    }

    private static class RegisterCell {
        private final int registerNumber;
        private final Pane valuePane;
        private final Label valueLabel;
        private final Rectangle background;
        private int value;

        private static final int SPECIAL_SP = 13;
        private static final int SPECIAL_FP = 14;
        private static final int SPECIAL_LR = 15;

        public RegisterCell(int registerNumber) {
            this.registerNumber = registerNumber;
            this.value = 0;

            this.background = new Rectangle(80, 30);
            background.setFill(Color.web("#DCDCDC"));
            background.setStroke(Color.web("#A9A9A9"));
            background.setStrokeWidth(1);
            background.setArcWidth(5);
            background.setArcHeight(5);

            this.valueLabel = new Label(formatValue(0));
            valueLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px;");

            this.valuePane = new Pane();
            valuePane.getChildren().addAll(background, valueLabel);
            valuePane.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    showHistory();
                }
            });
        }

        private String formatValue(int value) {
            return String.format("0x%08X (%d)", value, value);
        }

        public void updateValue(int newValue) {
            this.value = newValue;
            valueLabel.setText(formatValue(newValue));
            updateColor();
        }

        public void highlight() {
            background.setStroke(Color.web("#FFD700"));
            background.setStrokeWidth(3);

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(() -> {
                    background.setStroke(Color.web("#A9A9A9"));
                    background.setStrokeWidth(1);
                });
            }).start();
        }

        private void updateColor() {
            if (value == 0 && registerNumber == 0) {
                background.setFill(Color.web("#90EE90"));
            } else if (registerNumber == SPECIAL_SP || registerNumber == SPECIAL_FP || registerNumber == SPECIAL_LR) {
                background.setFill(Color.web("#ADD8E6"));
            } else if (value != 0) {
                background.setFill(Color.web("#FFB6C1"));
            } else {
                background.setFill(Color.web("#DCDCDC"));
            }
        }

        public Pane getValuePane() {
            return valuePane;
        }

        private void showHistory() {
            System.out.println("History for r" + registerNumber);
        }
    }
}
