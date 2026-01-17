package org.teachfx.antlr4.ep18r.vizvmr;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.IRxVMStateManager;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.IVM;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.IVMAdapter;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes;
import org.teachfx.antlr4.ep18r.vizvmr.unified.core.VMTypes.Command;
import org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.*;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import java.io.File;
import java.util.concurrent.Executor;

public class UnifiedVizVMRLauncher extends Application {

    private static final Logger logger = LogManager.getLogger(UnifiedVizVMRLauncher.class);

    private IRxVMStateManager stateManager;
    private IVM vm;

    private RegisterView registerView;
    private CodeView codeView;
    private StackView stackView;
    private MemoryView memoryView;
    private StatusView statusView;

    private Disposable[] disposables;

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("启动统一响应式VM可视化");

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        vm = new IVMAdapter(interpreter);
        stateManager = new org.teachfx.antlr4.ep18r.vizvmr.unified.core.RxVMStateManagerImpl(vm, 4096, 256);

        createComponents();
        BorderPane root = createMainLayout();
        bindDataStreams();

        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("统一响应式VM可视化 - EP18R");
        primaryStage.setScene(scene);
        primaryStage.show();

        logger.info("统一响应式VM可视化启动完成");
    }

    private void createComponents() {
        registerView = new RegisterView();
        codeView = new CodeView();
        stackView = new StackView();
        memoryView = new MemoryView();
        statusView = new StatusView();
    }

    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();

        VBox topBox = new VBox();
        topBox.getChildren().addAll(createMenuBar(), createToolBar());
        root.setTop(topBox);

        SplitPane centerSplit = new SplitPane();
        centerSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);

        SplitPane leftSplit = new SplitPane();
        leftSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        leftSplit.getItems().addAll(registerView, stackView);
        leftSplit.setDividerPositions(0.5);

        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplit.getItems().addAll(codeView, memoryView);
        rightSplit.setDividerPositions(0.5);

        centerSplit.getItems().addAll(leftSplit, rightSplit);
        centerSplit.setDividerPositions(0.4);

        root.setCenter(centerSplit);

        HBox bottomBox = new HBox(10);
        bottomBox.setStyle("-fx-padding: 10; -fx-background-color: #2C3E50;");
        bottomBox.getChildren().addAll(statusView, createControlPanel());
        root.setBottom(bottomBox);

        return root;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("文件");
        MenuItem loadItem = new MenuItem("加载代码...");
        loadItem.setOnAction(e -> loadCode());
        MenuItem reloadItem = new MenuItem("重新加载");
        reloadItem.setOnAction(e -> reloadCode());
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(e -> Platform.exit());
        fileMenu.getItems().addAll(loadItem, reloadItem, new SeparatorMenuItem(), exitItem);

        Menu viewMenu = new Menu("视图");
        MenuItem refreshItem = new MenuItem("刷新");
        refreshItem.setOnAction(e -> refreshAll());
        viewMenu.getItems().addAll(refreshItem);

        Menu runMenu = new Menu("运行");
        MenuItem startItem = new MenuItem("开始");
        startItem.setOnAction(e -> stateManager.start());
        MenuItem pauseItem = new MenuItem("暂停");
        pauseItem.setOnAction(e -> stateManager.pause());
        MenuItem stopItem = new MenuItem("停止");
        stopItem.setOnAction(e -> stateManager.stop());
        MenuItem stepItem = new MenuItem("单步");
        stepItem.setOnAction(e -> stateManager.step());
        runMenu.getItems().addAll(startItem, pauseItem, stopItem, new SeparatorMenuItem(), stepItem);

        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(e -> showAbout());
        helpMenu.getItems().addAll(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, runMenu, helpMenu);
        return menuBar;
    }

    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();

        Button btnStart = new Button("▶ 开始");
        btnStart.setOnAction(e -> stateManager.start());

        Button btnPause = new Button("⏸ 暂停");
        btnPause.setOnAction(e -> stateManager.pause());

        Button btnStop = new Button("⏹ 停止");
        btnStop.setOnAction(e -> stateManager.stop());

        Button btnStep = new Button("⏭ 单步");
        btnStep.setOnAction(e -> stateManager.step());

        toolBar.getItems().addAll(btnStart, btnPause, btnStop, btnStep);

        return toolBar;
    }

    private HBox createControlPanel() {
        HBox panel = new HBox(10);
        panel.setStyle("-fx-padding: 10;");

        Button btnStart = new Button("▶ 开始");
        btnStart.setOnAction(e -> stateManager.executeAsync(Command.START));

        Button btnPause = new Button("⏸ 暂停");
        btnPause.setOnAction(e -> stateManager.executeAsync(Command.PAUSE));

        Button btnStop = new Button("⏹ 停止");
        btnStop.setOnAction(e -> stateManager.executeAsync(Command.STOP));

        Button btnStep = new Button("⏭ 单步");
        btnStep.setOnAction(e -> stateManager.step());

        panel.getChildren().addAll(btnStart, btnPause, btnStop, btnStep);
        return panel;
    }

    private void bindDataStreams() {
        disposables = new Disposable[6];

        disposables[0] = stateManager.registers()
            .subscribe(registers -> Platform.runLater(() -> registerView.updateRegisters(registers)));

        disposables[1] = stateManager.pc()
            .subscribe(pc -> Platform.runLater(() -> {
                codeView.highlightPC(pc);
                statusView.updatePC(pc);
            }));

        disposables[2] = stateManager.heap()
            .subscribe(heap -> Platform.runLater(() -> memoryView.updateHeap(heap)));

        disposables[3] = stateManager.globals()
            .subscribe(globals -> Platform.runLater(() -> memoryView.updateGlobals(globals)));

        disposables[4] = stateManager.callStack()
            .subscribe(stack -> Platform.runLater(() -> stackView.updateStack(stack)));

        disposables[5] = stateManager.state()
            .subscribe(state -> Platform.runLater(() -> statusView.updateState(state)));

        disposables[6] = stateManager.executionSteps()
            .subscribe(steps -> Platform.runLater(() -> statusView.updateSteps(steps)));
    }

    private void loadCode() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("加载VM代码");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("VM Assembly (*.vm)", "*.vm"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(statusView.getScene().getWindow());
        if (file != null) {
            try (java.io.InputStream is = new java.io.FileInputStream(file)) {
                stateManager.loadCode(is).thenAccept(result -> {
                    Platform.runLater(() -> {
                        if (result.isSuccess()) {
                            logger.info("代码加载成功: {}", file.getName());
                        } else {
                            showError("加载失败", result.getMessage());
                        }
                    });
                });
            } catch (Exception e) {
                logger.error("加载代码异常", e);
            }
        }
    }

    private void reloadCode() {
        logger.info("重新加载代码");
    }

    private void refreshAll() {
        logger.info("刷新所有视图");
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("统一响应式VM可视化 - EP18R");
        alert.setContentText("版本: 1.0.0\n使用RxJava + JavaFX构建响应式虚拟机可视化界面");
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        logger.info("关闭VM可视化");

        if (disposables != null) {
            for (Disposable disposable : disposables) {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
