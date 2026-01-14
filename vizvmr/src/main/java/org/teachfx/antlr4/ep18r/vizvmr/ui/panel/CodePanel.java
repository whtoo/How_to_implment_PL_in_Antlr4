package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 代码/反汇编面板
 * 显示反汇编代码，高亮当前PC位置，支持断点标记
 */
public class CodePanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JList<String> instructionList;
    private DefaultListModel<String> listModel;
    private final Set<Integer> breakpoints;
    private int currentPC = -1;

    public CodePanel(VMRVisualBridge visualBridge) {
        super(new BorderLayout());
        this.visualBridge = visualBridge;
        this.breakpoints = new HashSet<>();

        setBorder(BorderFactory.createTitledBorder("代码"));
        setPreferredSize(new Dimension(400, 400));

        initializeCodeView();
    }

    private void initializeCodeView() {
        listModel = new DefaultListModel<>();
        instructionList = new JList<>(listModel);
        instructionList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        instructionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 自定义渲染器显示断点标记
        instructionList.setCellRenderer(new BreakpointListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(instructionList);
        add(scrollPane, BorderLayout.CENTER);

        // 工具栏
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refresh());
        toolBar.add(refreshButton);

        JButton toggleBreakpointButton = new JButton("切换断点");
        toggleBreakpointButton.addActionListener(e -> toggleBreakpointAtSelection());
        toolBar.add(toggleBreakpointButton);

        JButton clearBreakpointsButton = new JButton("清除所有");
        clearBreakpointsButton.addActionListener(e -> clearAllBreakpoints());
        toolBar.add(clearBreakpointsButton);

        add(toolBar, BorderLayout.NORTH);
    }

    public void setInstructions(RegisterDisAssembler disAssembler) {
        listModel.clear();
        if (disAssembler != null) {
            String disassembly = disAssembler.disassembleToString();
            String[] lines = disassembly.split("\n");
            for (String line : lines) {
                listModel.addElement(line);
            }
        }
    }

    public void highlightPC(int pc) {
        currentPC = pc;
        // 计算行号（每条指令4字节）
        int lineIndex = pc / 4;
        if (lineIndex >= 0 && lineIndex < listModel.size()) {
            instructionList.setSelectedIndex(lineIndex);
            instructionList.scrollRectToVisible(instructionList.getCellBounds(lineIndex, lineIndex));
        }
        repaint();
    }

    public void toggleBreakpoint(int pc) {
        if (breakpoints.contains(pc)) {
            breakpoints.remove(pc);
        } else {
            breakpoints.add(pc);
        }
        repaint();
    }

    public void toggleBreakpointAtSelection() {
        int selectedIndex = instructionList.getSelectedIndex();
        if (selectedIndex >= 0) {
            int pc = selectedIndex * 4;
            toggleBreakpoint(pc);
        }
    }

    public void clearAllBreakpoints() {
        breakpoints.clear();
        repaint();
    }

    public boolean isBreakpointAt(int pc) {
        return breakpoints.contains(pc);
    }

    public Set<Integer> getBreakpoints() {
        return new HashSet<>(breakpoints);
    }

    public void refresh() {
        repaint();
    }

    // 内部类：自定义列表单元渲染器
    private class BreakpointListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            int pc = index * 4;
            if (breakpoints.contains(pc)) {
                setText("● " + getText());
                setForeground(Color.RED);
            }

            if (index * 4 == currentPC) {
                setBackground(new Color(255, 255, 0, 100)); // 黄色高亮
            }

            return this;
        }
    }
}
