package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;

/**
 * 响应式寄存器视图
 * 使用RxJava自动订阅寄存器变化
 */
public class ReactiveRegisterView extends BorderPane {

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

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
    private static final int SP_REGISTER = 13;
    private static final int FP_REGISTER = 14;
    private static final int LR_REGISTER = 15;

    // Color definitions
    private static final String COLOR_ZERO = "#90EE90";
    private static final String COLOR_MODIFIED = "#FFB6C1";
    private static final String COLOR_SPECIAL = "#ADD8E6";
    private static final String COLOR_NORMAL = "#DCDCDC";
    private static final String COLOR_HIGHLIGHT = "#FFFF00";

    public ReactiveRegisterView(ReactiveVMRStateModel stateModel) {
        this.stateModel = stateModel;
        buildUI();
        bindToState();
    }

    private void buildUI() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        Label titleLabel = new Label("寄存器");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        mainLayout.getChildren().add(titleLabel);

        // Create 4x4 grid
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(5);
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

        mainLayout.getChildren().add(gridPane);
        setCenter(mainLayout);
    }

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
        registerLabels[regNum] = valueLabel;

        // Use HBox for horizontal layout
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(nameLabel, valueLabel);

        cellPane.getChildren().add(hbox);

        return cellPane;
    }

    private void bindToState() {
        // 订阅寄存器数组变化
        disposables.add(
            stateModel.getRegisters()
                .subscribeOn(Schedulers.computation())
                .subscribe(registers -> Platform.runLater(() -> updateAllRegisters(registers)),
                           Throwable::printStackTrace)
        );

        // 订阅单个寄存器变化
        disposables.add(
            stateModel.getRegisterChanges()
                .subscribeOn(Schedulers.computation())
                .subscribe(change -> Platform.runLater(() -> {
                    if (change.regNum() >= 0 && change.regNum() < 16) {
                        updateRegister(change.regNum(), change.getNewValue());
                    }
                }), Throwable::printStackTrace)
        );
    }

    private void updateAllRegisters(int[] registers) {
        if (registers != null && registers.length >= 16) {
            for (int i = 0; i < 16; i++) {
                updateRegister(i, registers[i]);
            }
        }
    }

    private void updateRegister(int regNum, int value) {
        if (regNum >= 0 && regNum < 16) {
            boolean valueChanged = (value != previousValues[regNum]);
            previousValues[regNum] = value;

            String text = String.format("0x%08X (%d)", value, value);
            registerLabels[regNum].setText(text);

            // Apply color coding
            applyColorCoding(regNum, valueChanged);
        }
    }

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

    private boolean isSpecialRegister(int regNum) {
        return regNum == SP_REGISTER || regNum == FP_REGISTER || regNum == LR_REGISTER;
    }

    public void dispose() {
        disposables.clear();
    }
}
