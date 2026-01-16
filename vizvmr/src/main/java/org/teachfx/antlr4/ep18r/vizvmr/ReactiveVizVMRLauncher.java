package org.teachfx.antlr4.ep18r.vizvmr;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.core.ReactiveVMRStateModel;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.LogView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.ReactiveCodeView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.ReactiveStatusView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.ReactiveRegisterView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.ReactiveMemoryView;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.ReactiveStackView;
import org.teachfx.antlr4.common.visualization.ui.javafx.JFXThemeManager;

import java.io.File;

/**
 * 响应式虚拟机可视化启动器 - JavaFX版本
 * 使用RxJava实现响应式状态绑定，慢速播放每200ms一条指令
 */
public class ReactiveVizVMRLauncher extends Application {

    private static final String TITLE = "EP18R 寄存器虚拟机可视化 (响应式版)";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private RegisterVMInterpreter vm;
    private ReactiveVMRStateModel stateModel;
    private Stage primaryStage;

    // 响应式视图组件
    private ReactiveCodeView codeView;
    private ReactiveStatusView statusView;
    private ReactiveRegisterView registerView;
    private ReactiveMemoryView memoryView;
    private ReactiveStackView stackView;
    private LogView logView;

    // 资源清理
    private final CompositeDisposable disposables = new CompositeDisposable();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() {
        Platform.setImplicitExit(false);
        System.out.println("初始化响应式虚拟机配置...");
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        try {
            // 1. 创建虚拟机配置
            VMConfig config = new VMConfig.Builder()
                .setHeapSize(1024 * 1024)
                .setStackSize(1024)
                .setMaxStackDepth(100)
                .setDebugMode(true)
                .build();

            // 2. 创建虚拟机
            vm = new RegisterVMInterpreter(config);

            // 3. 创建响应式状态模型
            stateModel = new ReactiveVMRStateModel(vm);

            // 4. 创建响应式UI
            createReactiveUI();

            // 5. 配置并显示窗口
            stage.setTitle(TITLE);
            stage.setScene(createScene());
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);
            stage.setMaximized(true);

            // 窗口关闭处理
            stage.setOnCloseRequest(e -> cleanup());

            stage.show();

            System.out.println(TITLE + " 已启动");
            System.out.println("慢速播放: 每200ms执行一条指令");
            System.out.println("请使用 文件 -> 打开代码 加载虚拟机程序");

        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }

    /**
     * 创建响应式UI组件
     */
    private void createReactiveUI() {
        JFXThemeManager.applyTheme(primaryStage.getScene(), JFXThemeManager.Theme.LIGHT);

        // 创建响应式组件 - 自动订阅状态变化
        codeView = new ReactiveCodeView(stateModel);
        statusView = new ReactiveStatusView(stateModel);
        registerView = new ReactiveRegisterView(stateModel);
        memoryView = new ReactiveMemoryView(stateModel);
        stackView = new ReactiveStackView(stateModel);
        logView = new LogView();
        logView.redirectSystemStreams();

        logView.info("响应式状态模型已就绪");
    }

    /**
     * 创建主场景
     */
    private Scene createScene() {
        BorderPane root = new BorderPane();

        // 顶部：菜单栏和工具栏
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(createMenuBar(), createToolBar());
        root.setTop(topContainer);

        // 中央：分割面板
        SplitPane centerSplit = new SplitPane();
        centerSplit.setDividerPositions(0.5, 0.5);

        // 左侧：代码 + 调用栈
        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        leftSplit.setDividerPositions(0.6, 0.4);
        leftSplit.getItems().add(codeView);
        leftSplit.getItems().add(stackView);

        // 右侧：寄存器 + 内存
        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplit.setDividerPositions(0.5, 0.5);
        rightSplit.getItems().add(registerView);
        rightSplit.getItems().add(memoryView);

        centerSplit.getItems().add(leftSplit);
        centerSplit.getItems().add(rightSplit);

        root.setCenter(centerSplit);

        // 底部：状态栏 + 日志
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

        fileMenu.getItems().addAll(openItem, reloadItem, new SeparatorMenuItem(), exitMenuItem());

        // 运行菜单
        Menu runMenu = new Menu("运行");
        MenuItem startItem = new MenuItem("开始执行");
        startItem.setOnAction(e -> startExecution());
        MenuItem pauseItem = new MenuItem("暂停");
        pauseItem.setOnAction(e -> pauseExecution());
        MenuItem stepItem = new MenuItem("单步执行");
        stepItem.setOnAction(e -> stepExecution());
        MenuItem stopItem = new MenuItem("停止");
        stopItem.setOnAction(e -> stopExecution());

        runMenu.getItems().addAll(startItem, pauseItem, stepItem, new SeparatorMenuItem(), stopItem);

        // 速度菜单
        Menu speedMenu = new Menu("速度");
        MenuItem slowItem = new MenuItem("慢速 (200ms/条)");
        slowItem.setOnAction(e -> setSpeed(200));
        MenuItem normalItem = new MenuItem("正常 (50ms/条)");
        normalItem.setOnAction(e -> setSpeed(50));
        MenuItem fastItem = new MenuItem("快速 (10ms/条)");
        fastItem.setOnAction(e -> setSpeed(10));

        speedMenu.getItems().addAll(slowItem, normalItem, fastItem);

        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, runMenu, speedMenu, helpMenu);

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
        pauseBtn.setOnAction(e -> pauseExecution());

        Button stepBtn = new Button("⏭");
        stepBtn.setTooltip(new javafx.scene.control.Tooltip("单步执行 (F11)"));
        stepBtn.setOnAction(e -> stepExecution());

        Button stopBtn = new Button("⏹");
        stopBtn.setTooltip(new javafx.scene.control.Tooltip("停止 (F8)"));
        stopBtn.setOnAction(e -> stopExecution());

        toolBar.getItems().addAll(startBtn, pauseBtn, stepBtn, stopBtn, new Separator());

        return toolBar;
    }

    /**
     * 打开文件
     */
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开虚拟机代码");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("VM代码文件", "*.vm", "*.vmr")
        );

        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                java.io.FileInputStream fis = new java.io.FileInputStream(file);

                // 使用旧版API加载代码
                boolean hasErrors = org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter.load(vm, fis);
                fis.close();

                if (!hasErrors) {
                    // 更新反汇编视图
                    byte[] code = vm.getCode();
                    int codeSize = vm.getCodeSize();
                    Object[] constPool = vm.getConstantPool();

                    logView.info("代码大小: " + codeSize + " bytes");

                    if (code != null && codeSize > 0) {
                        RegisterDisAssembler disAssembler = new RegisterDisAssembler(code, codeSize, constPool);
                        String disassembly = disAssembler.disassembleToString();

                        logView.info("反汇编输出 (" + (disassembly.split("\n").length) + " 行):");
                        logView.info(disassembly.substring(0, Math.min(200, disassembly.length())) + "...");

                        codeView.setInstructions(disAssembler);
                        logView.info("已更新代码视图");
                    } else {
                        logView.warn("代码为空或大小为0");
                    }

                    // 同步初始状态
                    stateModel.syncFromVM();
                    logView.info("已加载: " + file.getName());
                } else {
                    showError("代码加载错误");
                }
            } catch (Exception e) {
                showError("加载失败: " + e.getMessage());
            }
        }
    }

    /**
     * 重新加载
     */
    private void reloadFile() {
        stateModel.syncFromVM();
        logView.info("视图已刷新");
    }

    /**
     * 开始执行（慢速播放模式）
     */
    private void startExecution() {
        if (vm == null) {
            logView.warn("请先加载代码文件");
            return;
        }
        logView.info("开始执行 (慢速播放模式)");
        stateModel.start(); // 自动设置200ms延迟
    }

    /**
     * 暂停执行
     */
    private void pauseExecution() {
        stateModel.pause();
        logView.info("执行已暂停");
    }

    /**
     * 单步执行
     */
    private void stepExecution() {
        if (vm == null) {
            logView.warn("请先加载代码文件");
            return;
        }
        logView.info("单步执行");
        stateModel.step();
    }

    /**
     * 停止执行
     */
    private void stopExecution() {
        stateModel.stop();
        logView.info("执行已停止");
    }

    /**
     * 设置执行速度
     */
    private void setSpeed(int delayMs) {
        stateModel.setAutoStepDelay(delayMs);
        logView.info("执行速度: " + delayMs + "ms/条");
    }

    /**
     * 显示关于对话框
     */
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText(TITLE);
        alert.setContentText("使用RxJava响应式框架\n慢速播放: 每200ms执行一条指令\n\n基于 JavaFX 21 构建");
        alert.showAndWait();
    }

    /**
     * 显示错误对话框
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("发生错误");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 退出菜单项
     */
    private MenuItem exitMenuItem() {
        MenuItem item = new MenuItem("退出");
        item.setOnAction(e -> {
            cleanup();
            Platform.exit();
        });
        return item;
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        if (stateModel != null) {
            stateModel.stop();
        }
        if (codeView != null) {
            codeView.dispose();
        }
        if (statusView != null) {
            statusView.dispose();
        }
        if (registerView != null) {
            registerView.dispose();
        }
        if (memoryView != null) {
            memoryView.dispose();
        }
        if (stackView != null) {
            stackView.dispose();
        }
        disposables.clear();
        System.out.println(TITLE + " 已关闭");
    }

    @Override
    public void stop() {
        cleanup();
    }
}
