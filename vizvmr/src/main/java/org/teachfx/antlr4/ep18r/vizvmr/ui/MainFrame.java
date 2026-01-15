package org.teachfx.antlr4.ep18r.vizvmr.ui;

import org.teachfx.antlr4.ep18r.vizvmr.event.MemoryChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.CodePanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.ControlPanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.LogPanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.MemoryPanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.RegisterPanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.StackPanel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.panel.StatusPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 虚拟机可视化主窗口
 * 使用 BorderLayout 组织多个面板组件
 */
public class MainFrame extends JFrame {
    private final VMRVisualBridge visualBridge;

    // 面板组件
    private RegisterPanel registerPanel;
    private MemoryPanel memoryPanel;
    private CodePanel codePanel;
    private StackPanel stackPanel;
    private ControlPanel controlPanel;
    private StatusPanel statusPanel;
    private LogPanel logPanel;

    // 菜单和工具栏
    private JMenuBar menuBar;
    private JToolBar toolBar;

    // 状态
    private static final String TITLE = "EP18R 寄存器虚拟机可视化";
    private static final Dimension DEFAULT_SIZE = new Dimension(1200, 800);
    
    private static final int MAX_RECENT_FILES = 5;
    private final java.util.List<String> recentFiles = new java.util.ArrayList<>();
    private JMenu recentFilesMenu;

    public MainFrame(VMRVisualBridge visualBridge) {
        super(TITLE);
        this.visualBridge = visualBridge;

        initializeUI();
        setupLayout();
        setupEventListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(DEFAULT_SIZE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void initializeUI() {
        // 初始化面板
        registerPanel = new RegisterPanel(visualBridge);
        memoryPanel = new MemoryPanel(visualBridge);
        codePanel = new CodePanel(visualBridge);
        stackPanel = new StackPanel(visualBridge);
        controlPanel = new ControlPanel(visualBridge);
        statusPanel = new StatusPanel(visualBridge);
        logPanel = new LogPanel();

        logPanel.redirectSystemStreams();

        // 初始化菜单栏
        initializeMenuBar();

        // 初始化工具栏
        initializeToolBar();
    }

    private void initializeMenuBar() {
        menuBar = new JMenuBar();

        // 文件菜单
        JMenu fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic('F');

        JMenuItem openItem = new JMenuItem("打开代码(O)...", 'O');
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        JMenuItem reloadItem = new JMenuItem("重新加载(R)", 'R');
        reloadItem.setAccelerator(KeyStroke.getKeyStroke("control R"));
        reloadItem.addActionListener(e -> reloadFile());
        fileMenu.add(reloadItem);

        fileMenu.addSeparator();

        recentFilesMenu = new JMenu("最近打开");
        updateRecentFilesMenu();
        fileMenu.add(recentFilesMenu);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("退出(X)", 'X');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // 视图菜单
        JMenu viewMenu = new JMenu("视图(V)");
        viewMenu.setMnemonic('V');

        JMenuItem registersItem = new JMenuItem("寄存器窗口", 'R');
        registersItem.setSelected(true);
        registersItem.addActionListener(e -> togglePanel(registerPanel));
        viewMenu.add(registersItem);

        JMenuItem memoryItem = new JMenuItem("内存窗口", 'M');
        memoryItem.setSelected(true);
        memoryItem.addActionListener(e -> togglePanel(memoryPanel));
        viewMenu.add(memoryItem);

        JMenuItem codeItem = new JMenuItem("代码窗口", 'C');
        codeItem.setSelected(true);
        codeItem.addActionListener(e -> togglePanel(codePanel));
        viewMenu.add(codeItem);

        JMenuItem stackItem = new JMenuItem("调用栈窗口", 'S');
        stackItem.setSelected(true);
        stackItem.addActionListener(e -> togglePanel(stackPanel));
        viewMenu.add(stackItem);

        JMenuItem logItem = new JMenuItem("日志窗口", 'L');
        logItem.setSelected(true);
        logItem.addActionListener(e -> togglePanel(logPanel));
        viewMenu.add(logItem);

        viewMenu.addSeparator();

        JMenuItem refreshItem = new JMenuItem("刷新视图", 'F');
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshAllPanels());
        viewMenu.add(refreshItem);

        // 运行菜单
        JMenu runMenu = new JMenu("运行(R)");
        runMenu.setMnemonic('R');

        JMenuItem startItem = new JMenuItem("开始执行", 'S');
        startItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        startItem.addActionListener(e -> visualBridge.start());
        runMenu.add(startItem);

        JMenuItem pauseItem = new JMenuItem("暂停", 'P');
        pauseItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
        pauseItem.addActionListener(e -> visualBridge.pause());
        runMenu.add(pauseItem);

        JMenuItem resumeItem = new JMenuItem("继续", 'R');
        resumeItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
        resumeItem.addActionListener(e -> visualBridge.resume());
        runMenu.add(resumeItem);

        JMenuItem stopItem = new JMenuItem("停止", 'T');
        stopItem.setAccelerator(KeyStroke.getKeyStroke("F8"));
        stopItem.addActionListener(e -> visualBridge.stop());
        runMenu.add(stopItem);

        runMenu.addSeparator();

        JMenuItem stepItem = new JMenuItem("单步执行", 'S');
        stepItem.setAccelerator(KeyStroke.getKeyStroke("F11"));
        stepItem.addActionListener(e -> visualBridge.step());
        runMenu.add(stepItem);

        // 调试菜单
        JMenu debugMenu = new JMenu("调试(D)");
        debugMenu.setMnemonic('D');

        JMenuItem breakpointItem = new JMenuItem("切换断点", 'B');
        breakpointItem.setAccelerator(KeyStroke.getKeyStroke("F9"));
        breakpointItem.addActionListener(e -> toggleBreakpoint());
        debugMenu.add(breakpointItem);

        JMenuItem clearBreakpointsItem = new JMenuItem("清除所有断点", 'C');
        clearBreakpointsItem.addActionListener(e -> clearAllBreakpoints());
        debugMenu.add(clearBreakpointsItem);

        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = new JMenuItem("关于", 'A');
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        // 添加菜单到菜单栏
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(runMenu);
        menuBar.add(debugMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void initializeToolBar() {
        toolBar = new JToolBar("工具栏");
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        // 开始按钮
        JButton startButton = new JButton("▶");
        startButton.setToolTipText("开始执行 (F5)");
        startButton.addActionListener(e -> visualBridge.start());
        toolBar.add(startButton);

        // 暂停按钮
        JButton pauseButton = new JButton("⏸");
        pauseButton.setToolTipText("暂停 (F6)");
        pauseButton.addActionListener(e -> visualBridge.pause());
        toolBar.add(pauseButton);

        // 继续按钮
        JButton resumeButton = new JButton("▶");
        resumeButton.setToolTipText("继续 (F7)");
        resumeButton.addActionListener(e -> visualBridge.resume());
        toolBar.add(resumeButton);

        // 停止按钮
        JButton stopButton = new JButton("⏹");
        stopButton.setToolTipText("停止 (F8)");
        stopButton.addActionListener(e -> visualBridge.stop());
        toolBar.add(stopButton);

        toolBar.addSeparator();

        // 单步按钮
        JButton stepButton = new JButton("⏭");
        stepButton.setToolTipText("单步执行 (F11)");
        stepButton.addActionListener(e -> visualBridge.step());
        toolBar.add(stepButton);

        toolBar.addSeparator();

        // 断点按钮
        JButton breakpointButton = new JButton("●");
        breakpointButton.setToolTipText("切换断点 (F9)");
        breakpointButton.addActionListener(e -> toggleBreakpoint());
        toolBar.add(breakpointButton);

        toolBar.addSeparator();

        // 刷新按钮
        JButton refreshButton = new JButton("↻");
        refreshButton.setToolTipText("刷新视图 (F5)");
        refreshButton.addActionListener(e -> refreshAllPanels());
        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private void setupLayout() {
        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitPane.setTopComponent(statusPanel);
        bottomSplitPane.setBottomComponent(logPanel);
        bottomSplitPane.setDividerLocation(150);

        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setTopComponent(registerPanel);
        leftSplitPane.setBottomComponent(stackPanel);
        leftSplitPane.setDividerLocation(300);

        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerSplitPane.setTopComponent(codePanel);
        centerSplitPane.setBottomComponent(memoryPanel);
        centerSplitPane.setDividerLocation(400);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(leftSplitPane);
        mainSplitPane.setRightComponent(centerSplitPane);
        mainSplitPane.setDividerLocation(400);

        JSplitPane finalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        finalSplitPane.setTopComponent(mainSplitPane);
        finalSplitPane.setBottomComponent(bottomSplitPane);
        finalSplitPane.setDividerLocation(600);

        add(finalSplitPane, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        // 设置面板事件监听
        visualBridge.setExecutionCallback(new VMRVisualBridge.ExecutionCallback() {
            @Override
            public void onRegisterChanged(int regNum, int oldValue, int newValue) {
                SwingUtilities.invokeLater(() -> registerPanel.updateRegister(regNum, newValue));
            }

            @Override
            public void onMemoryChanged(MemoryChangeEvent.MemoryType type, int address, int oldValue, int newValue) {
                SwingUtilities.invokeLater(() -> memoryPanel.updateMemory(address, newValue));
            }

            @Override
            public void onPCChanged(int oldPC, int newPC) {
                SwingUtilities.invokeLater(() -> {
                    codePanel.highlightPC(newPC);
                    statusPanel.updatePC(newPC);
                });
            }

            @Override
            public void onStateChanged(VMStateChangeEvent.State oldState, VMStateChangeEvent.State newState) {
                SwingUtilities.invokeLater(() -> {
                    statusPanel.updateState(newState);
                    controlPanel.updateState(newState);
                });
            }

            @Override
            public void onInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {
                SwingUtilities.invokeLater(() -> {
                    codePanel.highlightPC(pc);
                    statusPanel.updateInstruction(mnemonic, operands);
                });
            }

            @Override
            public void onExecutionStarted() {
                SwingUtilities.invokeLater(() -> {
                    controlPanel.setRunning(true);
                    statusPanel.startTimer();
                });
            }

            @Override
            public void onExecutionFinished() {
                SwingUtilities.invokeLater(() -> {
                    controlPanel.setRunning(false);
                    statusPanel.stopTimer();
                });
            }

            @Override
            public void onExecutionPaused() {
                SwingUtilities.invokeLater(() -> {
                    controlPanel.setPaused(true);
                });
            }

            @Override
            public void onError(Throwable error) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "执行错误: " + error.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    // ==================== 事件处理方法 ====================

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("打开虚拟机代码");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("VM代码文件", "vm", "vmr"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                boolean hasErrors = visualBridge.loadCode(fis);
                fis.close();

                if (!hasErrors) {
                    refreshAllPanels();
                    codePanel.setInstructions(visualBridge.getDisAssembler());
                    statusPanel.updateStatus("已加载: " + file.getName());
                    addToRecentFiles(file.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "代码加载错误", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reloadFile() {
        refreshAllPanels();
    }

    private void togglePanel(JPanel panel) {
        panel.setVisible(!panel.isVisible());
    }

    private void refreshAllPanels() {
        registerPanel.refresh();
        memoryPanel.refresh();
        codePanel.refresh();
        stackPanel.refresh();
        statusPanel.refresh();
    }

    private void toggleBreakpoint() {
        int pc = visualBridge.getCurrentPC();
        codePanel.toggleBreakpoint(pc);
    }

    private void clearAllBreakpoints() {
        codePanel.clearAllBreakpoints();
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "EP18R 寄存器虚拟机可视化工具\n\n" +
            "版本: 1.0.0\n" +
            "基于 Java Swing 构建",
            "关于",
            JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== 公共方法 ====================

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public MemoryPanel getMemoryPanel() {
        return memoryPanel;
    }

    public CodePanel getCodePanel() {
        return codePanel;
    }

    public StackPanel getStackPanel() {
        return stackPanel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    private void updateRecentFilesMenu() {
        recentFilesMenu.removeAll();

        if (recentFiles.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("（无最近文件）");
            emptyItem.setEnabled(false);
            recentFilesMenu.add(emptyItem);
            return;
        }

        for (int i = 0; i < recentFiles.size(); i++) {
            String filePath = recentFiles.get(i);
            String displayName = getDisplayName(filePath, i + 1);
            
            JMenuItem fileItem = new JMenuItem(displayName);
            fileItem.addActionListener(e -> openRecentFile(filePath));
            recentFilesMenu.add(fileItem);
        }

        recentFilesMenu.addSeparator();
        JMenuItem clearItem = new JMenuItem("清除最近文件");
        clearItem.addActionListener(e -> clearRecentFiles());
        recentFilesMenu.add(clearItem);
    }

    private String getDisplayName(String filePath, int index) {
        java.io.File file = new java.io.File(filePath);
        String fileName = file.getName();
        String parent = file.getParent();
        
        if (parent == null) {
            return String.format("%d. %s", index, fileName);
        }
        
        String parentName = new java.io.File(parent).getName();
        return String.format("%d. %s (%s)", index, fileName, parentName);
    }

    private void addToRecentFiles(String filePath) {
        recentFiles.remove(filePath);
        
        recentFiles.add(0, filePath);
        
        while (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.remove(recentFiles.size() - 1);
        }
        
        updateRecentFilesMenu();
        
        if (logPanel != null) {
            logPanel.info("添加到最近文件: " + filePath);
        }
    }

    private void openRecentFile(String filePath) {
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, 
                    "文件不存在: " + filePath, 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                    
                recentFiles.remove(filePath);
                updateRecentFilesMenu();
                return;
            }

            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            boolean hasErrors = visualBridge.loadCode(fis);
            fis.close();

            if (!hasErrors) {
                refreshAllPanels();
                codePanel.setInstructions(visualBridge.getDisAssembler());
                statusPanel.updateStatus("已加载: " + file.getName());
                
                if (logPanel != null) {
                    logPanel.info("打开最近文件: " + filePath);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "代码加载错误", 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "加载失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
                
            if (logPanel != null) {
                logPanel.error("打开最近文件失败", e);
            }
        }
    }

    private void clearRecentFiles() {
        recentFiles.clear();
        updateRecentFilesMenu();
        
        if (logPanel != null) {
            logPanel.info("已清除最近文件列表");
        }
    }
}
