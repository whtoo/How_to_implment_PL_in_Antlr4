package org.teachfx.antlr4.ep18r.vizvmr;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.VMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.RegisterView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.StackView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.CodeView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.MemoryView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.LogView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.StatusView;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXThemeManager;

/**
 * 虚拟机可视化启动器 - JavaFX版本
 * 使用JavaFX Application替代Swing
 */
public class VizVMRLauncher extends Application {

    private static final String TITLE = "EP18R 寄存器虚拟机可视化 (JavaFX)";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private VMRVisualBridge visualBridge;
    private Stage primaryStage;

    // JavaFX 视图组件
    private RegisterView registerView;
    private StackView stackView;
    private CodeView codeView;
    private MemoryView memoryView;
    private LogView logView;
    private StatusView statusView;

    public static void main(String[] args) {
        // 使用JavaFX Application启动
        Application.launch(args);
    }

    @Override
    public void init() {
        // 防止隐式退出
        Platform.setImplicitExit(false);
        // 在JavaFX线程启动前初始化VM
        System.out.println("初始化虚拟机配置...");
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            // 1. 创建虚拟机配置
            VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)  // 1MB 堆
                .setStackSize(1024)        // 1K 局部变量
                .setMaxStackDepth(100)     // 最大调用深度
                .setDebugMode(true)
                .build();

            // 2. 创建虚拟机
            RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

            // 3. 创建状态模型
            VMRStateModel stateModel = new VMRStateModel(
                config.getHeapSize(),
                256,  // 全局变量大小
                config.getMaxCallStackDepth()
            );

            // 4. 创建可视化桥接器
            visualBridge = new VMRVisualBridge(vm, stateModel);

            // 5. 创建 UI
            createUI();

            // 6. 配置并显示窗口
            stage.setTitle(TITLE);
            stage.setScene(createScene());
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.setMaximized(true);

            // 处理窗口关闭事件
            stage.setOnCloseRequest(e -> {
                if (visualBridge != null) {
                    visualBridge.stop();
                }
                Platform.exit();
            });

            stage.show();

            System.out.println("EP18R 寄存器虚拟机可视化工具 (JavaFX) 已启动");
            System.out.println("请使用 文件 -> 打开代码 加载虚拟机程序");

        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * 创建UI组件
     */
    private void createUI() {
        // 设置主题
        JFXThemeManager.applyTheme(primaryStage.getScene(), JFXThemeManager.Theme.LIGHT);

        // 创建视图组件
        registerView = new RegisterView(visualBridge);
        stackView = new StackView(visualBridge);
        codeView = new CodeView(visualBridge);
        memoryView = new MemoryView(visualBridge);
        logView = new LogView();
        statusView = new StatusView(visualBridge);

        // 初始化日志重定向
        logView.redirectSystemStreams();

        // 设置执行回调 - 关键：让UI响应VM执行事件
        setupExecutionCallback();

        // 初始刷新所有视图
        refreshAll();
    }

    /**
     * 设置执行回调 - 连接VM事件到UI更新
     */
    private void setupExecutionCallback() {
        visualBridge.setExecutionCallback(new VMRVisualBridge.ExecutionCallback() {
            @Override
            public void onRegisterChanged(int regNum, int oldValue, int newValue) {
                Platform.runLater(() -> registerView.refresh());
            }

            @Override
            public void onMemoryChanged(org.teachfx.antlr4.ep18r.vizvmr.event.MemoryChangeEvent.MemoryType type, int address, int oldValue, int newValue) {
                Platform.runLater(() -> memoryView.refresh());
            }

            @Override
            public void onPCChanged(int oldPC, int newPC) {
                Platform.runLater(() -> {
                    // 高亮代码视图中的当前PC
                    codeView.highlightPC(newPC);
                    // 更新状态视图中的PC显示
                    statusView.updatePC(newPC);
                });
            }

            @Override
            public void onStateChanged(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State oldState, org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State newState) {
                Platform.runLater(() -> {
                    statusView.updateState(newState);
                    if (newState == org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.RUNNING) {
                        statusView.startTimer();
                    } else if (newState == org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.HALTED) {
                        statusView.stopTimer();
                    }
                });
            }

            @Override
            public void onInstructionExecuted(int pc, int opcode, String mnemonic, String operands) {
                Platform.runLater(() -> {
                    statusView.updateInstruction(mnemonic, operands);
                    // 更新执行步数
                    statusView.refresh();
                });
            }

            @Override
            public void onExecutionStarted() {
                Platform.runLater(() -> {
                    logView.info("开始执行");
                });
            }

            @Override
            public void onExecutionFinished() {
                Platform.runLater(() -> {
                    statusView.stopTimer();
                    statusView.updateState(org.teachfx.antlr4.ep18r.vizvmr.event.VMStateChangeEvent.State.HALTED);
                    logView.info("执行完成");
                });
            }

            @Override
            public void onExecutionPaused() {
                Platform.runLater(() -> {
                    logView.info("执行已暂停");
                });
            }

            @Override
            public void onError(Throwable error) {
                Platform.runLater(() -> {
                    logView.error("执行错误: " + error.getMessage());
                    showError("执行错误: " + error.getMessage());
                });
            }
        });
    }

    /**
     * 创建主场景
     */
    private Scene createScene() {
        BorderPane root = new BorderPane();

        // 顶部：菜单栏
        root.setTop(createMenuBar());

        // 工具栏（在菜单栏下方）
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(createMenuBar(), createToolBar());
        root.setTop(topContainer);

        // 中央：分割面板
        SplitPane centerSplit = new SplitPane();
        centerSplit.setDividerPositions(0.4, 0.6);

        // 左侧：寄存器和栈
        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        leftSplit.setDividerPositions(0.5, 0.5);
        leftSplit.getItems().add(registerView);
        leftSplit.getItems().add(stackView);

        // 右侧：代码和内存
        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplit.setDividerPositions(0.5, 0.5);
        rightSplit.getItems().add(codeView);
        rightSplit.getItems().add(memoryView);

        centerSplit.getItems().add(leftSplit);
        centerSplit.getItems().add(rightSplit);

        root.setCenter(centerSplit);

        // 底部：状态栏和日志
        VBox bottomContainer = new VBox();
        bottomContainer.getChildren().add(statusView);
        bottomContainer.getChildren().add(logView);
        root.setBottom(bottomContainer);

        return new Scene(root, WIDTH, HEIGHT);
    }

    /**
     * 创建菜单栏
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // 文件菜单
        Menu fileMenu = new Menu("文件");
        MenuItem openItem = new MenuItem("打开代码...");
        openItem.setOnAction(e -> openFile());
        MenuItem reloadItem = new MenuItem("重新加载");
        reloadItem.setOnAction(e -> reloadFile());
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(openItem, reloadItem, new SeparatorMenuItem(), exitItem);
        
        // 视图菜单
        Menu viewMenu = new Menu("视图");
        MenuItem registersItem = new MenuItem("寄存器窗口");
        registersItem.setOnAction(e -> togglePanel("registers"));
        MenuItem memoryItem = new MenuItem("内存窗口");
        memoryItem.setOnAction(e -> togglePanel("memory"));
        MenuItem refreshItem = new MenuItem("刷新视图");
        refreshItem.setOnAction(e -> refreshAll());
        
        viewMenu.getItems().addAll(registersItem, memoryItem);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().add(refreshItem);
        
        // 运行菜单
        Menu runMenu = new Menu("运行");
        MenuItem startItem = new MenuItem("开始执行");
        startItem.setOnAction(e -> startExecution());
        MenuItem pauseItem = new MenuItem("暂停");
        pauseItem.setOnAction(e -> visualBridge.pause());
        MenuItem stopItem = new MenuItem("停止");
        stopItem.setOnAction(e -> visualBridge.stop());
        MenuItem stepItem = new MenuItem("单步执行");
        stepItem.setOnAction(e -> visualBridge.step());
        
        runMenu.getItems().addAll(startItem, pauseItem, stopItem);
        runMenu.getItems().add(new SeparatorMenuItem());
        runMenu.getItems().add(stepItem);
        
        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, viewMenu, runMenu, helpMenu);
        
        return menuBar;
    }

    /**
     * 创建工具栏
     */
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        
        Button startBtn = new Button("▶");
        startBtn.setTooltip(new javafx.scene.control.Tooltip("开始执行 (F5)"));
        startBtn.setOnAction(e -> startExecution());
        
        Button pauseBtn = new Button("⏸");
        pauseBtn.setTooltip(new javafx.scene.control.Tooltip("暂停 (F6)"));
        pauseBtn.setOnAction(e -> visualBridge.pause());
        
        Button stopBtn = new Button("⏹");
        stopBtn.setTooltip(new javafx.scene.control.Tooltip("停止 (F8)"));
        stopBtn.setOnAction(e -> visualBridge.stop());
        
        toolBar.getItems().addAll(startBtn, pauseBtn, stopBtn, new Separator());
        
        Button stepBtn = new Button("⏭");
        stepBtn.setTooltip(new javafx.scene.control.Tooltip("单步执行 (F11)"));
        stepBtn.setOnAction(e -> visualBridge.step());
        
        toolBar.getItems().add(stepBtn);
        
        return toolBar;
    }

    /**
     * 打开文件
     */
    private void openFile() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("打开虚拟机代码");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("VM代码文件", "*.vm", "*.vmr")
        );
        
        java.io.File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                boolean hasErrors = visualBridge.loadCode(fis);
                fis.close();

                if (!hasErrors) {
                    codeView.setInstructions(visualBridge.getDisAssembler());
                    refreshAll();
                    System.out.println("已加载: " + file.getName());
                } else {
                    showError("代码加载错误");
                }
            } catch (Exception e) {
                showError("加载失败: " + e.getMessage());
            }
        }
    }

    /**
     * 开始执行
     */
    private void startExecution() {
        System.out.println("开始执行");
        if (visualBridge.getVM() == null) {
            System.out.println("VM未初始化");
            showError("VM未初始化");
            return;
        }

        if (visualBridge.getVM().getCode() == null || visualBridge.getVM().getCodeSize() == 0) {
            System.out.println("VM代码未加载");
            showError("请先加载代码文件");
            return;
        }

        try {
            visualBridge.start();
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            showError("启动失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载文件
     */
    private void reloadFile() {
        refreshAll();
    }

    /**
     * 切换面板可见性
     */
    private void togglePanel(String panelId) {
        // TODO: 实现面板切换
        System.out.println("切换面板: " + panelId);
    }

    /**
     * 刷新所有面板
     */
    private void refreshAll() {
        if (registerView != null) {
            registerView.refresh();
        }
        if (stackView != null) {
            stackView.refresh();
        }
        if (codeView != null) {
            codeView.refresh();
        }
        if (memoryView != null) {
            memoryView.refresh();
        }
        if (statusView != null) {
            statusView.refresh();
        }
        if (logView != null) {
            logView.info("视图已刷新");
        }
    }

    /**
     * 显示关于对话框
     */
    private void showAbout() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle("关于");
        alert.setHeaderText("EP18R 寄存器虚拟机可视化工具");
        alert.setContentText("版本: 1.0 (JavaFX版)\n\n基于 JavaFX 21 构建");
        alert.showAndWait();
    }

    /**
     * 显示错误对话框
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("错误");
        alert.setHeaderText("发生错误");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        // 清理资源
        if (visualBridge != null) {
            visualBridge.stop();
        }
        System.out.println("EP18R 寄存器虚拟机可视化工具已关闭");
    }
}
