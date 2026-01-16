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
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;

/**
 * 响应式内存视图
 * 使用RxJava自动订阅内存变化
 */
public class ReactiveMemoryView extends BorderPane {

    private final ReactiveVMRStateModel stateModel;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private TableView<MemoryRow> memoryTable;
    private TextField addressField;
    private Label pageLabel;
    private static final int PAGE_SIZE = 16;
    private int currentPage = 0;
    private int totalPages = 0;

    public ReactiveMemoryView(ReactiveVMRStateModel stateModel) {
        this.stateModel = stateModel;
        buildUI();
        bindToState();
    }

    private void buildUI() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        HBox toolbar = createToolbar();
        mainLayout.getChildren().add(toolbar);

        memoryTable = createMemoryTable();
        ScrollPane scrollPane = new ScrollPane(memoryTable);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        mainLayout.getChildren().add(scrollPane);

        setCenter(mainLayout);
    }

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

    private TableView<MemoryRow> createMemoryTable() {
        TableView<MemoryRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MemoryRow, String> addressCol = new TableColumn<>("地址");
        addressCol.setCellValueFactory(data -> data.getValue().addressProperty());
        addressCol.setPrefWidth(80);

        TableColumn<MemoryRow, String> hexCol = new TableColumn<>("十六进制");
        hexCol.setCellValueFactory(data -> data.getValue().hexProperty());
        hexCol.setPrefWidth(120);

        TableColumn<MemoryRow, String> decimalCol = new TableColumn<>("十进制");
        decimalCol.setCellValueFactory(data -> data.getValue().decimalProperty());
        decimalCol.setPrefWidth(100);

        table.getColumns().addAll(addressCol, hexCol, decimalCol);

        return table;
    }

    private void bindToState() {
        disposables.add(
            stateModel.getHeap()
                .subscribeOn(Schedulers.computation())
                .subscribe(heap -> Platform.runLater(this::refreshData),
                           Throwable::printStackTrace)
        );

        disposables.add(
            stateModel.getMemoryChanges()
                .subscribeOn(Schedulers.computation())
                .subscribe(event -> Platform.runLater(() -> {
                    int rowInPage = event.getAddress() % PAGE_SIZE;
                    int pageOfAddress = event.getAddress() / PAGE_SIZE;
                    if (pageOfAddress == currentPage) {
                        refreshData();
                    }
                }), Throwable::printStackTrace)
        );
    }

    public void refresh() {
        refreshData();
    }

    private void refreshData() {
        int[] heap = stateModel.getHeapSnapshot();

        if (heap == null || heap.length == 0) {
            totalPages = 0;
            memoryTable.setItems(FXCollections.observableArrayList());
            pageLabel.setText("页: 0/0");
            return;
        }

        totalPages = (heap.length + PAGE_SIZE - 1) / PAGE_SIZE;

        ObservableList<MemoryRow> data = FXCollections.observableArrayList();
        int startAddress = currentPage * PAGE_SIZE;
        int endAddress = Math.min(startAddress + PAGE_SIZE, heap.length);

        for (int address = startAddress; address < endAddress; address++) {
            int value = heap[address];
            String addressStr = String.format("0x%04X", address);
            String hexStr = String.format("0x%08X", value);
            String decimalStr = String.valueOf(value);
            data.add(new MemoryRow(addressStr, hexStr, decimalStr));
        }

        memoryTable.setItems(data);
        pageLabel.setText(String.format("页: %d/%d", currentPage + 1, Math.max(1, totalPages)));
    }

    public void jumpToAddress(int address) {
        int[] heap = stateModel.getHeapSnapshot();

        if (heap == null || address < 0 || address >= heap.length) {
            showError("地址越界", "地址必须在有效范围内");
            return;
        }

        currentPage = address / PAGE_SIZE;
        refreshData();

        int rowInPage = address % PAGE_SIZE;
        if (rowInPage >= 0 && rowInPage < memoryTable.getItems().size()) {
            memoryTable.getSelectionModel().select(rowInPage);
            memoryTable.scrollTo(rowInPage);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void dispose() {
        disposables.clear();
    }

    public static class MemoryRow {
        private final javafx.beans.property.SimpleStringProperty address;
        private final javafx.beans.property.SimpleStringProperty hex;
        private final javafx.beans.property.SimpleStringProperty decimal;

        public MemoryRow(String address, String hex, String decimal) {
            this.address = new javafx.beans.property.SimpleStringProperty(address);
            this.hex = new javafx.beans.property.SimpleStringProperty(hex);
            this.decimal = new javafx.beans.property.SimpleStringProperty(decimal);
        }

        public javafx.beans.property.StringProperty addressProperty() {
            return address;
        }

        public javafx.beans.property.StringProperty hexProperty() {
            return hex;
        }

        public javafx.beans.property.StringProperty decimalProperty() {
            return decimal;
        }
    }
}
