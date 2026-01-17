package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * 代码视图组件
 *
 * <p>显示反汇编指令，高亮当前PC位置</p>
 */
public class CodeView extends VBox {

    private final VBox instructionsContainer;
    private int currentPC = -1;

    public CodeView() {
        this.instructionsContainer = new VBox(5);
        instructionsContainer.setStyle("-fx-background-color: #F5F5F5; -fx-padding: 10;");

        Label header = new Label("代码视图");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #666666;");

        getChildren().addAll(header, instructionsContainer);
    }

    public void setInstructions(String[] disassembly) {
        instructionsContainer.getChildren().clear();

        if (disassembly == null) {
            return;
        }

        for (int i = 0; i < disassembly.length; i++) {
            InstructionLine line = new InstructionLine(i, disassembly[i]);
            instructionsContainer.getChildren().add(line);
        }

        updateHighlighting();
    }

    public void highlightPC(int pc) {
        this.currentPC = pc;
        updateHighlighting();
    }

    private void updateHighlighting() {
        for (int i = 0; i < instructionsContainer.getChildren().size(); i++) {
            if (instructionsContainer.getChildren().get(i) instanceof InstructionLine) {
                InstructionLine line = (InstructionLine) instructionsContainer.getChildren().get(i);
                line.setHighlighted(line.getInstructionAddress() == currentPC);
            }
        }
    }

    private static class InstructionLine extends VBox {
        private final int instructionAddress;
        private final Rectangle background;
        private final Label instructionLabel;
        private boolean highlighted;

        public InstructionLine(int address, String instruction) {
            this.instructionAddress = address;
            this.highlighted = false;

            this.background = new Rectangle(800, 25);
            background.setFill(Color.web("#F5F5F5"));
            background.setStroke(Color.TRANSPARENT);
            background.setStrokeWidth(0);

            this.instructionLabel = new Label(String.format("0x%04X  %s", address, instruction));
            instructionLabel.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px;");

            setStyle("-fx-padding: 2;");
            getChildren().addAll(background, instructionLabel);
        }

        public void setHighlighted(boolean highlighted) {
            this.highlighted = highlighted;
            if (highlighted) {
                background.setFill(Color.web("#FFF9C4"));
            } else {
                background.setFill(Color.web("#F5F5F5"));
            }
        }

        public int getInstructionAddress() {
            return instructionAddress;
        }
    }
}
