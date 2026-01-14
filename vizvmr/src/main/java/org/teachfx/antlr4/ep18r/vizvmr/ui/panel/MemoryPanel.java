package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 内存显示面板
 * 可滚动表格显示内存内容（堆、全局变量、局部变量）
 */
public class MemoryPanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JTable memoryTable;
    private MemoryTableModel tableModel;
    private JTextField addressField;
    private static final int PAGE_SIZE = 16;

    public MemoryPanel(VMRVisualBridge visualBridge) {
        super(new BorderLayout());
        this.visualBridge = visualBridge;

        setBorder(BorderFactory.createTitledBorder("内存"));
        setPreferredSize(new Dimension(400, 300));

        initializeMemoryTable();
    }

    private void initializeMemoryTable() {
        tableModel = new MemoryTableModel(visualBridge);
        memoryTable = new JTable(tableModel);
        memoryTable.setAutoCreateRowSorter(true);
        memoryTable.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(memoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // 工具栏
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refresh());
        toolBar.add(refreshButton);

        toolBar.add(new JLabel("地址:"));

        addressField = new JTextField(10);
        toolBar.add(addressField);

        JButton gotoButton = new JButton("跳转");
        gotoButton.addActionListener(e -> {
            try {
                int address = Integer.parseInt(addressField.getText(), 16);
                jumpToAddress(address);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "无效地址", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        toolBar.add(gotoButton);

        add(toolBar, BorderLayout.NORTH);
    }

    public void updateMemory(int address, int value) {
        tableModel.updateValue(address, value);
    }

    public void refresh() {
        tableModel.refreshData();
    }

    public void jumpToAddress(int address) {
        int pageIndex = address / PAGE_SIZE;
        tableModel.setPage(pageIndex);

        int rowInPage = address % PAGE_SIZE;
        if (rowInPage >= 0 && rowInPage < PAGE_SIZE) {
            memoryTable.setRowSelectionInterval(rowInPage, rowInPage);
            memoryTable.scrollRectToVisible(memoryTable.getCellRect(rowInPage, 0, true));
        }
    }

    // 内部类：内存表格模型
    private static class MemoryTableModel extends javax.swing.table.AbstractTableModel {
        private final VMRVisualBridge visualBridge;
        private int currentPage = 0;
        private int totalPages = 0;

        public MemoryTableModel(VMRVisualBridge visualBridge) {
            this.visualBridge = visualBridge;
            calculateTotalPages();
        }

        private void calculateTotalPages() {
            if (visualBridge.getStateModel() != null) {
                int[] heap = visualBridge.getStateModel().getHeap();
                totalPages = (heap.length + PAGE_SIZE - 1) / PAGE_SIZE;
            } else {
                totalPages = 0;
            }
        }

        public void setPage(int page) {
            if (page >= 0 && page < totalPages) {
                this.currentPage = page;
                fireTableDataChanged();
            }
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        @Override
        public int getRowCount() {
            int heapSize = getHeapSize();
            int remaining = heapSize - (currentPage * PAGE_SIZE);
            return Math.min(PAGE_SIZE, Math.max(0, remaining));
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "地址";
                case 1: return "十六进制";
                case 2: return "十进制";
                default: return "";
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            int address = currentPage * PAGE_SIZE + rowIndex;
            int value = visualBridge.getStateModel().readHeap(address);

            switch (columnIndex) {
                case 0: return String.format("0x%04X", address);
                case 1: return String.format("0x%08X", value);
                case 2: return String.valueOf(value);
                default: return null;
            }
        }

        public void updateValue(int address, int value) {
            int rowInPage = address % PAGE_SIZE;
            int pageOfAddress = address / PAGE_SIZE;

            if (pageOfAddress == currentPage) {
                fireTableCellUpdated(rowInPage, 1);
                fireTableCellUpdated(rowInPage, 2);
            }
        }

        public void refreshData() {
            calculateTotalPages();
            fireTableDataChanged();
        }

        private int getHeapSize() {
            if (visualBridge.getStateModel() != null) {
                return visualBridge.getStateModel().getHeap().length;
            }
            return 0;
        }
    }
}
