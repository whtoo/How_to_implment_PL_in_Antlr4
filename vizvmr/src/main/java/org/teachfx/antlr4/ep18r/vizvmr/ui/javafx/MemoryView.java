package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

/**
 * 内存视图组件
 *
 * <p>显示堆内存和全局变量</p>
 */
public class MemoryView extends TabPane {

    private final TableView<MemoryRow> heapTable;
    private final TableView<MemoryRow> globalsTable;

    public MemoryView() {
        this.heapTable = createHeapTable();
        this.globalsTable = createGlobalsTable();

        Tab heapTab = new Tab("堆内存", createHeapScrollPane());
        heapTab.setClosable(false);

        Tab globalsTab = new Tab("全局变量", createGlobalsScrollPane());
        globalsTab.setClosable(false);

        getTabs().addAll(heapTab, globalsTab);
    }

    private ScrollPane createHeapScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(heapTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F5F5F5;");
        return scrollPane;
    }

    private ScrollPane createGlobalsScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(globalsTable);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #E8F5E9;");
        return scrollPane;
    }

    private TableView<MemoryRow> createHeapTable() {
        TableView<MemoryRow> table = new TableView<>();

        TableColumn<MemoryRow, String> addressCol = new TableColumn<>("地址");
        addressCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(120);

        TableColumn<MemoryRow, String> hexCol = new TableColumn<>("十六进制");
        hexCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hexValue"));
        hexCol.setPrefWidth(150);

        TableColumn<MemoryRow, String> decCol = new TableColumn<>("十进制");
        decCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("decValue"));
        decCol.setPrefWidth(100);

        TableColumn<MemoryRow, String> asciiCol = new TableColumn<>("ASCII");
        asciiCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("asciiValue"));
        asciiCol.setPrefWidth(50);

        table.getColumns().addAll(addressCol, hexCol, decCol, asciiCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        return table;
    }

    private TableView<MemoryRow> createGlobalsTable() {
        TableView<MemoryRow> table = new TableView<>();

        TableColumn<MemoryRow, String> nameCol = new TableColumn<>("名称");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<MemoryRow, String> addressCol = new TableColumn<>("地址");
        addressCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(120);

        TableColumn<MemoryRow, String> valueCol = new TableColumn<>("值");
        valueCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);

        table.getColumns().addAll(nameCol, addressCol, valueCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        return table;
    }

    public void updateHeap(int[] heap) {
        heapTable.getItems().clear();

        if (heap == null) {
            return;
        }

        for (int i = 0; i < heap.length; i += 4) {
            String address = String.format("0x%08X", i);
            String hex = formatHexRow(heap, i);
            String dec = formatDecRow(heap, i);
            String ascii = formatAsciiRow(heap, i);

            MemoryRow row = new MemoryRow(address, hex, dec, ascii);
            heapTable.getItems().add(row);
        }
    }

    public void updateGlobals(int[] globals) {
        globalsTable.getItems().clear();

        if (globals == null) {
            return;
        }

        for (int i = 0; i < globals.length; i++) {
            String name = String.format("g%d", i);
            String address = String.format("0x%08X", i);
            String value = String.valueOf(globals[i]);

            MemoryRow row = new MemoryRow(name, address, value, "");
            globalsTable.getItems().add(row);
        }
    }

    private String formatHexRow(int[] heap, int startAddr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4 && startAddr + i < heap.length; i++) {
            sb.append(String.format("%02X ", heap[startAddr + i] & 0xFF));
        }
        return sb.toString();
    }

    private String formatDecRow(int[] heap, int startAddr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4 && startAddr + i < heap.length; i++) {
            sb.append(String.format("%12d ", heap[startAddr + i]));
        }
        return sb.toString();
    }

    private String formatAsciiRow(int[] heap, int startAddr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4 && startAddr + i < heap.length; i++) {
            int val = heap[startAddr + i] & 0xFF;
            if (val >= 32 && val <= 126) {
                sb.append((char) val);
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }

    public static class MemoryRow {
        private final String address;
        private final String hexValue;
        private final String decValue;
        private final String asciiValue;

        public MemoryRow(String address, String hexValue, String decValue, String asciiValue) {
            this.address = address;
            this.hexValue = hexValue;
            this.decValue = decValue;
            this.asciiValue = asciiValue;
        }

        public MemoryRow(String name, String address, String value, Object dummy) {
            this.address = address;
            this.hexValue = null;
            this.decValue = null;
            this.asciiValue = null;
            this.name = name;
            this.value = value;
        }

        private String name;
        private String value;

        public String getAddress() { return address; }
        public String getHexValue() { return hexValue; }
        public String getDecValue() { return decValue; }
        public String getAsciiValue() { return asciiValue; }
        public String getName() { return name; }
        public String getValue() { return value; }
    }
}
