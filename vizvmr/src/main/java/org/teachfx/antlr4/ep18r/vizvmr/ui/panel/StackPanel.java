package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.stackvm.StackFrame;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 调用栈面板
 * 垂直列表显示调用栈
 */
public class StackPanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JList<String> stackList;
    private DefaultListModel<String> listModel;

    public StackPanel(VMRVisualBridge visualBridge) {
        super(new BorderLayout());
        this.visualBridge = visualBridge;

        setBorder(BorderFactory.createTitledBorder("调用栈"));
        setPreferredSize(new Dimension(300, 250));

        initializeStackView();
    }

    private void initializeStackView() {
        listModel = new DefaultListModel<>();
        stackList = new JList<>(listModel);
        stackList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(stackList);
        add(scrollPane, BorderLayout.CENTER);

        // 工具栏
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refresh());
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
    }

    public void refresh() {
        listModel.clear();

        StackFrame[] callStack = visualBridge.getStateModel().getCallStack();
        int depth = visualBridge.getStateModel().getCallStackDepth();

        for (int i = depth - 1; i >= 0; i--) {
            StackFrame frame = callStack[i];
            if (frame != null && frame.symbol != null) {
                String frameInfo = String.format("[%d] %s @ 0x%04X (RA: 0x%04X)",
                    i, frame.symbol.name, frame.symbol.address, frame.returnAddress);
                listModel.addElement(frameInfo);
            }
        }

        if (listModel.isEmpty()) {
            listModel.addElement("<空调用栈>");
        }
    }
}
