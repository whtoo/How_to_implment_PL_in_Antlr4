package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 寄存器显示面板
 * 4x4网格显示16个寄存器
 * 支持颜色编码：红色(最近修改)、蓝色(特殊寄存器)、灰色(未修改)、绿色(零寄存器)
 */
public class RegisterPanel extends JPanel {
    private final VMRVisualBridge visualBridge;
    private JLabel[] registerLabels;
    private JPanel[] cellPanels;
    private int[] previousValues;
    private static final String[] REGISTER_NAMES = {
        "r0", "r1", "r2", "r3",
        "r4", "r5", "r6", "r7",
        "r8", "r9", "r10", "r11",
        "r12", "r13(SP)", "r14(FP)", "r15(LR)"
    };

    // 颜色定义
    private static final Color COLOR_ZERO_REGISTER = new Color(144, 238, 144);  // 浅绿色 - r0
    private static final Color COLOR_MODIFIED = new Color(255, 182, 193);       // 浅红色 - 最近修改
    private static final Color COLOR_SPECIAL = new Color(173, 216, 230);       // 浅蓝色 - 特殊寄存器
    private static final Color COLOR_NORMAL = new Color(220, 220, 220);        // 浅灰色 - 正常寄存器
    private static final Color COLOR_HIGHLIGHT = new Color(255, 255, 0, 100); // 黄色高亮

    // 特殊寄存器索引
    private static final int SP_REGISTER = 13;  // r13
    private static final int FP_REGISTER = 14;  // r14
    private static final int LR_REGISTER = 15;  // r15

    public RegisterPanel(VMRVisualBridge visualBridge) {
        super(new GridLayout(4, 4, 5, 5));
        this.visualBridge = visualBridge;

        setBorder(BorderFactory.createTitledBorder("寄存器"));
        setPreferredSize(new Dimension(300, 250));

        initializeRegisters();
    }

    private void initializeRegisters() {
        registerLabels = new JLabel[16];
        cellPanels = new JPanel[16];
        previousValues = new int[16];

        for (int i = 0; i < 16; i++) {
            JPanel cellPanel = new JPanel(new BorderLayout(5, 0));
            cellPanel.setBackground(COLOR_NORMAL);
            cellPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            JLabel nameLabel = new JLabel(REGISTER_NAMES[i]);
            nameLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
            nameLabel.setForeground(Color.DARK_GRAY);

            JLabel valueLabel = new JLabel("0x00000000 (0)");
            valueLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            cellPanel.add(nameLabel, BorderLayout.WEST);
            cellPanel.add(valueLabel, BorderLayout.CENTER);

            cellPanels[i] = cellPanel;
            registerLabels[i] = valueLabel;
            previousValues[i] = 0;
            add(cellPanel);
        }
    }

    public void updateRegister(int regNum, int value) {
        if (regNum >= 0 && regNum < 16) {
            boolean valueChanged = (value != previousValues[regNum]);
            previousValues[regNum] = value;

            String text = String.format("0x%08X (%d)", value, value);
            registerLabels[regNum].setText(text);

            // 应用颜色编码
            applyColorCoding(regNum, valueChanged);
        }
    }

    public void refresh() {
        for (int i = 0; i < 16; i++) {
            int value = visualBridge.getRegister(i);
            updateRegister(i, value);
        }
    }

    /**
     * 应用颜色编码
     * @param regNum 寄存器索引
     * @param valueChanged 值是否发生变化
     */
    private void applyColorCoding(int regNum, boolean valueChanged) {
        Color backgroundColor;

        if (regNum == 0) {
            // r0 是零寄存器，使用绿色
            backgroundColor = COLOR_ZERO_REGISTER;
        } else if (isSpecialRegister(regNum)) {
            // 特殊寄存器 (r13-SP, r14-FP, r15-LR)，使用蓝色
            backgroundColor = COLOR_SPECIAL;
        } else if (valueChanged) {
            // 最近修改的寄存器，使用红色
            backgroundColor = COLOR_MODIFIED;
        } else {
            // 未修改的寄存器，使用灰色
            backgroundColor = COLOR_NORMAL;
        }

        cellPanels[regNum].setBackground(backgroundColor);
        // 重绘面板以应用新颜色
        cellPanels[regNum].repaint();
    }

    /**
     * 检查是否为特殊寄存器
     */
    private boolean isSpecialRegister(int regNum) {
        return regNum == SP_REGISTER || regNum == FP_REGISTER || regNum == LR_REGISTER;
    }

    /**
     * 高亮指定寄存器
     */
    public void highlightRegister(int regNum, Color highlightColor) {
        if (regNum >= 0 && regNum < 16) {
            cellPanels[regNum].setBackground(highlightColor);
            cellPanels[regNum].repaint();
        }
    }

    /**
     * 清除所有高亮
     */
    public void clearHighlights() {
        for (int i = 0; i < 16; i++) {
            applyColorCoding(i, false);
        }
    }

    /**
     * 重置所有寄存器颜色到默认状态
     */
    public void resetColors() {
        for (int i = 0; i < 16; i++) {
            previousValues[i] = 0;  // 重置为初始值
            applyColorCoding(i, false);
        }
    }

    /**
     * 获取寄存器标签组件（用于外部访问）
     */
    public JLabel getRegisterLabel(int regNum) {
        if (regNum >= 0 && regNum < 16) {
            return registerLabels[regNum];
        }
        return null;
    }

    /**
     * 获取寄存器面板组件（用于外部访问）
     */
    public JPanel getCellPanel(int regNum) {
        if (regNum >= 0 && regNum < 16) {
            return cellPanels[regNum];
        }
        return null;
    }
}
