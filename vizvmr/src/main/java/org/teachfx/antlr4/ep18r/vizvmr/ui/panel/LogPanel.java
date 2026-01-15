package org.teachfx.antlr4.ep18r.vizvmr.ui.panel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志面板 - 显示虚拟机和GUI的日志信息
 * 支持不同级别的日志（INFO, WARN, ERROR）
 */
public class LogPanel extends JPanel {
    private final JTextPane logTextPane;
    private final JScrollPane scrollPane;
    private final JButton clearButton;
    private final JCheckBox autoScrollCheckBox;
    private final JLabel statusLabel;

    // 日志级别颜色
    private final SimpleAttributeSet infoAttr;
    private final SimpleAttributeSet warnAttr;
    private final SimpleAttributeSet errorAttr;
    private final SimpleAttributeSet debugAttr;

    // 原始输出流备份
    private final PrintStream originalOut;
    private final PrintStream originalErr;

    public LogPanel() {
        super(new BorderLayout());

        // 创建文本区域
        logTextPane = new JTextPane();
        logTextPane.setEditable(false);
        logTextPane.setFont(new Font("Monospaced", Font.PLAIN, 12));

        scrollPane = new JScrollPane(logTextPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 创建工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clearButton = new JButton("清除日志");
        clearButton.addActionListener(e -> clearLog());

        autoScrollCheckBox = new JCheckBox("自动滚动", true);
        statusLabel = new JLabel("就绪");

        toolbar.add(clearButton);
        toolbar.add(autoScrollCheckBox);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(statusLabel);

        // 设置布局
        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 初始化日志样式
        infoAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(infoAttr, Color.BLACK);
        StyleConstants.setBold(infoAttr, false);

        warnAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(warnAttr, Color.ORANGE.darker());
        StyleConstants.setBold(warnAttr, true);

        errorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(errorAttr, Color.RED);
        StyleConstants.setBold(errorAttr, true);

        debugAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(debugAttr, Color.BLUE);
        StyleConstants.setBold(debugAttr, false);

        // 保存原始输出流
        originalOut = System.out;
        originalErr = System.err;

        // 设置默认大小
        setPreferredSize(new Dimension(600, 200));
    }

    /**
     * 添加日志消息
     */
    public void addLog(String level, String message) {
        SwingUtilities.invokeLater(() -> {
            try {
                Document doc = logTextPane.getDocument();
                SimpleAttributeSet attr;

                // 根据级别选择样式
                switch (level.toUpperCase()) {
                    case "WARN":
                        attr = warnAttr;
                        break;
                    case "ERROR":
                        attr = errorAttr;
                        break;
                    case "DEBUG":
                        attr = debugAttr;
                        break;
                    default:
                        attr = infoAttr;
                        break;
                }

                // 添加时间戳和消息
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
                String logLine = String.format("[%s] [%s] %s\n", timestamp, level, message);

                doc.insertString(doc.getLength(), logLine, attr);

                // 自动滚动到底部
                if (autoScrollCheckBox.isSelected()) {
                    logTextPane.setCaretPosition(doc.getLength());
                }

                // 更新状态标签
                statusLabel.setText("日志已更新: " + level);

            } catch (BadLocationException e) {
                // 如果GUI日志失败，输出到原始流
                originalErr.println("Failed to add log to GUI: " + e.getMessage());
            }
        });
    }

    /**
     * 添加INFO级别日志
     */
    public void info(String message) {
        addLog("INFO", message);
    }

    /**
     * 添加WARN级别日志
     */
    public void warn(String message) {
        addLog("WARN", message);
    }

    /**
     * 添加ERROR级别日志
     */
    public void error(String message) {
        addLog("ERROR", message);
    }

    /**
     * 添加ERROR级别日志（带异常）
     */
    public void error(String message, Throwable t) {
        error(message + ": " + t.getMessage());
        // 可以添加堆栈跟踪的详细输出
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        t.printStackTrace(ps);
        error("堆栈跟踪: " + baos.toString());
    }

    /**
     * 添加DEBUG级别日志
     */
    public void debug(String message) {
        addLog("DEBUG", message);
    }

    /**
     * 清除所有日志
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            logTextPane.setText("");
            statusLabel.setText("日志已清除");
        });
    }

    /**
     * 获取日志内容
     */
    public String getLogContent() {
        return logTextPane.getText();
    }

    /**
     * 保存日志到文件
     */
    public void saveToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存日志文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("日志文件", "log", "txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().endsWith(".log") && !file.getName().endsWith(".txt")) {
                    file = new java.io.File(file.getAbsolutePath() + ".log");
                }

                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(getLogContent());
                writer.close();

                info("日志已保存到: " + file.getAbsolutePath());
            } catch (Exception e) {
                error("保存日志失败", e);
            }
        }
    }

    /**
     * 重定向System.out和System.err到日志面板
     */
    public void redirectSystemStreams() {
        // 重定向System.out
        System.setOut(new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void flush() {
                String output = this.toString();
                if (!output.trim().isEmpty()) {
                    info("STDOUT: " + output);
                }
                reset(); // 清除缓冲区
            }
        }));

        // 重定向System.err
        System.setErr(new PrintStream(new ByteArrayOutputStream() {
            @Override
            public void flush() {
                String output = this.toString();
                if (!output.trim().isEmpty()) {
                    error("STDERR: " + output);
                }
                reset(); // 清除缓冲区
            }
        }));
    }

    /**
     * 恢复原始System.out和System.err
     */
    public void restoreSystemStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * 获取日志文本组件（用于其他面板访问）
     */
    public JTextPane getLogTextPane() {
        return logTextPane;
    }
}