package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXPanelBase;

/**
 * Memory display panel - JavaFX version
 * Scrollable table displaying memory content (heap, global variables, local variables)
 */
public class MemoryView extends JFXPanelBase {

    private final VMRVisualBridge visualBridge;
    private TableView<MemoryRow> memoryTable;
    private TextField addressField;
    private Label pageLabel;
    private static final int PAGE_SIZE = 16;
    private int currentPage = 0;
    private int totalPages = 0;

    public MemoryView(VMRVisualBridge visualBridge) {
        super("MemoryView");
        this.visualBridge = visualBridge;
        buildUI();  // 在对象完全构造后初始化UI
    }

    @Override
    protected void initializeComponents() {
        setTitle("内存");
        setMinSize(400, 300);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        // Create toolbar
        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        // Create table
        memoryTable = createMemoryTable();
        ScrollPane scrollPane = new ScrollPane(memoryTable);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.getChildren().add(scrollPane);

        // Add layout to center of BorderPane
        setCenter(mainLayout);
        
        // Load initial data after table is created
        refreshData();
    }

    /**
     * Create toolbar with controls
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button refreshButton = new Button("刷新");
        refreshButton.setOnAction(e -> refresh());

        Label addressLabel = new Label("地址:");
        addressField = new TextField();
        addressField.setPrefColumnCount(10);

        Button gotoButton = new Button("跳转");
        gotoButton.setOnAction(e -> {
            try {
                int address = Integer.parseInt(addressField.getText(), 16);
                jumpToAddress(address);
            } catch (NumberFormatException ex) {
                showError("无效地址", "请输入有效的十六进制地址");
            }
        });

        pageLabel = new Label("页: 0/0");

        toolbar.getChildren().addAll(refreshButton, addressLabel, addressField, gotoButton, new Separator(), pageLabel);

        return toolbar;
    }

    /**
     * Create memory table
     */
    private TableView<MemoryRow> createMemoryTable() {
        TableView<MemoryRow> table = new TableView<>();

        // Configure table
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Address column
        TableColumn<MemoryRow, String> addressCol = new TableColumn<>("地址");
        addressCol.setCellValueFactory(data -> data.getValue().addressProperty());
        addressCol.setPrefWidth(80);
        addressCol.setMinWidth(60);

        // Hex value column
        TableColumn<MemoryRow, String> hexCol = new TableColumn<>("十六进制");
        hexCol.setCellValueFactory(data -> data.getValue().hexProperty());
        hexCol.setPrefWidth(120);
        hexCol.setMinWidth(100);

        // Decimal value column
        TableColumn<MemoryRow, String> decimalCol = new TableColumn<>("十进制");
        decimalCol.setCellValueFactory(data -> data.getValue().decimalProperty());
        decimalCol.setPrefWidth(100);
        decimalCol.setMinWidth(80);

        table.getColumns().addAll(addressCol, hexCol, decimalCol);

        return table;
    }

    /**
     * Update memory at specific address
     */
    public void updateMemory(int address, int value) {
        safeUpdateUI(() -> {
            refreshData();
        });
    }

    /**
     * Refresh memory display
     */
    public void refresh() {
        refreshData();
    }

    /**
     * Refresh table data
     */
    private void refreshData() {
        calculateTotalPages();

        ObservableList<MemoryRow> data = FXCollections.observableArrayList();

        if (visualBridge.getStateModel() != null) {
            int heapSize = visualBridge.getStateModel().getHeap().length;
            int startAddress = currentPage * PAGE_SIZE;
            int endAddress = Math.min(startAddress + PAGE_SIZE, heapSize);

            for (int address = startAddress; address < endAddress; address++) {
                int value = visualBridge.getStateModel().readHeap(address);
                String addressStr = String.format("0x%04X", address);
                String hexStr = String.format("0x%08X", value);
                String decimalStr = String.valueOf(value);
                data.add(new MemoryRow(addressStr, hexStr, decimalStr));
            }
        }

        memoryTable.setItems(data);
        pageLabel.setText(String.format("页: %d/%d", currentPage + 1, Math.max(1, totalPages)));
    }

    /**
     * Calculate total pages
     */
    private void calculateTotalPages() {
        if (visualBridge.getStateModel() != null) {
            int heapSize = visualBridge.getStateModel().getHeap().length;
            totalPages = (heapSize + PAGE_SIZE - 1) / PAGE_SIZE;
        } else {
            totalPages = 0;
        }
    }

    /**
     * Jump to specific address
     */
    public void jumpToAddress(int address) {
        if (visualBridge.getStateModel() == null) {
            return;
        }

        int heapSize = visualBridge.getStateModel().getHeap().length;
        if (address < 0 || address >= heapSize) {
            showError("地址越界", "地址必须在有效范围内");
            return;
        }

        currentPage = address / PAGE_SIZE;
        refreshData();

        // Select the row
        int rowInPage = address % PAGE_SIZE;
        if (rowInPage >= 0 && rowInPage < memoryTable.getItems().size()) {
            memoryTable.getSelectionModel().select(rowInPage);
            memoryTable.scrollTo(rowInPage);
        }
    }

    /**
     * Go to previous page
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            refreshData();
        }
    }

    /**
     * Go to next page
     */
    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            refreshData();
        }
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Inner class for memory row data
     */
    public static class MemoryRow {
        private final SimpleStringProperty address;
        private final SimpleStringProperty hex;
        private final SimpleStringProperty decimal;

        public MemoryRow(String address, String hex, String decimal) {
            this.address = new SimpleStringProperty(address);
            this.hex = new SimpleStringProperty(hex);
            this.decimal = new SimpleStringProperty(decimal);
        }

        public StringProperty addressProperty() {
            return address;
        }

        public StringProperty hexProperty() {
            return hex;
        }

        public StringProperty decimalProperty() {
            return decimal;
        }

        public String getAddress() {
            return address.get();
        }

        public String getHex() {
            return hex.get();
        }

        public String getDecimal() {
            return decimal.get();
        }
    }
}
