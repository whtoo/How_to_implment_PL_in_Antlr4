# VizVMR Swing到JavaFX迁移计划

## 📋 概述

本文档描述vizvmr模块从Java Swing迁移到JavaFX的详细计划。

**当前状态**: 纯Swing实现（30个Java文件组件匹配，92个Swing）
**目标状态**: JavaFX实现，保持功能兼容
**迁移周期**: 7-8周

---

## 🎯 迁移目标

### 功能目标
1. 保持所有现有功能
2. 提升UI响应性能
3. 增强可视化效果（CSS动画）
4. 支持未来Web集成

### 技术目标
1. JavaFX 21.0.3 + Java 21
2. 保持事件系统兼容
3. 创建双框架支持层
4. 完整的测试覆盖

---

## 📊 当前代码库分析

### 核心UI组件

| 组件 | 文件 | 行数 | 复杂度 |
|------|------|------|--------|
| MainFrame | MainFrame.java | 558 | 高 |
| RegisterPanel | RegisterPanel.java | 179 | 中 |
| ControlPanel | ControlPanel.java | 104 | 低 |
| MemoryPanel | MemoryPanel.java | 待统计 | 中 |
| CodePanel | CodePanel.java | 待统计 | 中 |
| StackPanel | StackPanel.java | 待统计 | 低 |
| StatusPanel | StatusPanel.java | 待统计 | 低 |
| LogPanel | LogPanel.java | 待统计 | 低 |

### Swing组件使用统计

- JFrame: 1个 (MainFrame)
- JPanel: 8个 (7个面板 + 基类)
- JButton: 约20个
- JLabel: 约50个
- JMenuBar/JMenu: 完整菜单系统
- JToolBar: 1个
- JTable: 1个 (MemoryPanel)
- JSplitPane: 4个

---

## 🚀 迁移计划

### 阶段一：基础设施准备 (第1周)

#### 1.1 添加JavaFX依赖

**文件**: `pom.xml`

```xml
<properties>
    <javafx.version>21.0.3</javafx.version>
</properties>

<dependencies>
    <!-- JavaFX Controls -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <!-- JavaFX FXML -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>
    
    <!-- JavaFX Web (可选，用于未来Web集成) -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-web</artifactId>
        <version>${javafx.version}</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <release>21</release>
                <compilerArgs>
                    <arg>--add-modules=javafx.controls,javafx.fxml</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

#### 1.2 创建JavaFX基类

**文件**: `common/src/main/java/.../ui/javafx/JFXPanelBase.java`

```java
package org.teachfx.antlr4.common.visualization.ui.javafx;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * JavaFX面板基类
 * 对应Swing版本的VisualPanelBase
 */
public abstract class JFXPanelBase extends Region {
    protected final String panelId;
    protected volatile boolean initialized;
    protected volatile boolean updating;
    
    protected JFXPanelBase(String panelId) {
        this.panelId = panelId != null ? panelId : this.getClass().getSimpleName();
        this.initialized = false;
        this.updating = false;
        initializePanel();
    }
    
    /**
     * 初始化面板
     */
    private void initializePanel() {
        setId(panelId);
        setPrefSize(getPreferredWidth(), getPreferredHeight());
        setupStylesheets();
        initializeComponents();
        initialized = true;
    }
    
    /**
     * 设置样式表
     */
    private void setupStylesheets() {
        String css = getClass().getResource("/css/" + panelId.toLowerCase() + ".css").toExternalForm();
        getStylesheets().add(css);
    }
    
    /**
     * 获取首选宽度
     */
    protected double getPreferredWidth() {
        return 400;
    }
    
    /**
     * 获取首选高度
     */
    protected double getPreferredHeight() {
        return 300;
    }
    
    /**
     * 安全更新UI (线程安全)
     */
    protected final void safeUpdateUI(Runnable updateAction) {
        if (Platform.isFxApplicationThread()) {
            updateAction.run();
        } else {
            Platform.runLater(updateAction);
        }
    }
    
    /**
     * 批量更新UI
     */
    protected final void batchUpdate(Runnable... updates) {
        if (updating) {
            return;
        }
        
        updating = true;
        try {
            safeUpdateUI(() -> {
                for (Runnable update : updates) {
                    update.run();
                }
            });
        } finally {
            updating = false;
        }
    }
    
    /**
     * 初始化组件 (抽象方法)
     */
    protected abstract void initializeComponents();
    
    /**
     * 刷新面板
     */
    public void refresh() {
        safeUpdateUI(this::requestLayout);
    }
    
    /**
     * 重置面板状态
     */
    public void reset() {
        safeUpdateUI(() -> {
            getChildren().clear();
            initializeComponents();
            requestLayout();
        });
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        // JavaFX自动管理大多数资源
    }
    
    public String getPanelId() {
        return panelId;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}
```

#### 1.3 创建事件适配器

**文件**: `common/src/main/java/.../event/JFXEventAdapter.java`

```java
package org.teachfx.antlr4.common.visualization.event.javafx;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.teachfx.antlr4.common.visualization.event.VMEvent;

/**
 * JavaFX事件适配器
 * 将Swing事件模型转换为JavaFX事件模型
 */
public class JFXEventAdapter {
    
    /**
     * 将VMEvent转换为JavaFX Event
     */
    public static Event toJFXEvent(VMEvent vmEvent) {
        if (vmEvent == null) {
            return null;
        }
        
        return new Event(vmEvent.getClass().getSimpleName());
    }
    
    /**
     * 创建JavaFX事件处理器
     */
    public static <T extends VMEvent> EventHandler<Event> createHandler(
            java.util.function.Consumer<T> handler) {
        return event -> {
            // 从event中提取原始VMEvent
            if (event.getSource() instanceof VMEvent) {
                @SuppressWarnings("unchecked")
                T vmEvent = (T) event.getSource();
                handler.accept(vmEvent);
            }
        };
    }
}
```

---

### 阶段二：核心框架迁移 (第2-3周)

#### 2.1 主窗口迁移

**文件**: `vizvmr/src/main/java/.../ui/javafx/MainStage.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Menu?>
<?import javafx.scene.control.MenuBar?>
<?import javafx.scene.control.MenuItem?>
<?import javafx.scene.control.SeparatorMenuItem?>
<?import javafx.scene.control.ToolBar?>
<?import javafx.scene.control.Button?>
<?import javafx.scene.control.SplitPane?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.layout.HBox?>

<BorderPane xmlns="http://javafx.com/javafx/21.0.3"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.MainStageController">
    
    <!-- 顶部菜单栏 -->
    <top>
        <MenuBar>
            <Menu text="文件(F)">
                <MenuItem text="打开代码(O)..." accelerator="Shortcut+O" onAction="#openFile"/>
                <MenuItem text="重新加载(R)" accelerator="Shortcut+R" onAction="#reloadFile"/>
                <SeparatorMenuItem/>
                <Menu text="最近打开">
                    <MenuItem text="（无最近文件）" disable="true"/>
                </Menu>
                <SeparatorMenuItem/>
                <MenuItem text="退出(X)" accelerator="Shortcut+Q" onAction="#exit"/>
            </Menu>
            
            <Menu text="视图(V)">
                <MenuItem text="寄存器窗口" selected="true" onAction="#toggleRegisterPanel"/>
                <MenuItem text="内存窗口" selected="true" onAction="#toggleMemoryPanel"/>
                <MenuItem text="代码窗口" selected="true" onAction="#toggleCodePanel"/>
                <MenuItem text="调用栈窗口" selected="true" onAction="#toggleStackPanel"/>
                <MenuItem text="日志窗口" selected="true" onAction="#toggleLogPanel"/>
                <SeparatorMenuItem/>
                <MenuItem text="刷新视图" accelerator="F5" onAction="#refreshAll"/>
            </Menu>
            
            <Menu text="运行(R)">
                <MenuItem text="开始执行" accelerator="F5" onAction="#start"/>
                <MenuItem text="暂停" accelerator="F6" onAction="#pause"/>
                <MenuItem text="继续" accelerator="F7" onAction="#resume"/>
                <MenuItem text="停止" accelerator="F8" onAction="#stop"/>
                <SeparatorMenuItem/>
                <MenuItem text="单步执行" accelerator="F11" onAction="#step"/>
            </Menu>
            
            <Menu text="调试(D)">
                <MenuItem text="切换断点" accelerator="F9" onAction="#toggleBreakpoint"/>
                <MenuItem text="清除所有断点" onAction="#clearAllBreakpoints"/>
            </Menu>
            
            <Menu text="帮助(H)">
                <MenuItem text="关于" onAction="#showAbout"/>
            </Menu>
        </MenuBar>
    </top>
    
    <!-- 顶部工具栏 -->
    <top>
        <ToolBar>
            <Button text="▶" onAction="#start" tooltipText="开始执行 (F5)"/>
            <Button text="⏸" onAction="#pause" tooltipText="暂停 (F6)"/>
            <Button text="▶" onAction="#resume" tooltipText="继续 (F7)"/>
            <Button text="⏹" onAction="#stop" tooltipText="停止 (F8)"/>
            <Separator/>
            <Button text="⏭" onAction="#step" tooltipText="单步执行 (F11)"/>
            <Separator/>
            <Button text="●" onAction="#toggleBreakpoint" tooltipText="切换断点 (F9)"/>
            <Separator/>
            <Button text="↻" onAction="#refreshAll" tooltipText="刷新视图 (F5)"/>
        </ToolBar>
    </top>
    
    <!-- 中央布局 -->
    <center>
        <SplitPane dividerPositions="0.4, 0.6">
            <!-- 左侧面板 -->
            <VBox>
                <SplitPane orientation="VERTICAL" dividerPositions="0.5">
                    <fx:include source="RegisterView.fxml"/>
                    <fx:include source="StackView.fxml"/>
                </SplitPane>
            </VBox>
            
            <!-- 右侧面板 -->
            <VBox>
                <SplitPane orientation="VERTICAL" dividerPositions="0.5">
                    <fx:include source="CodeView.fxml"/>
                    <fx:include source="MemoryView.fxml"/>
                </SplitPane>
            </VBox>
        </SplitPane>
    </center>
    
    <!-- 底部状态栏 -->
    <bottom>
        <VBox>
            <fx:include source="StatusView.fxml"/>
            <fx:include source="LogView.fxml"/>
        </VBox>
    </bottom>
    
</BorderPane>
```

#### 2.2 主控制器

**文件**: `vizvmr/src/main/java/.../ui/javafx/MainStageController.java`

```java
package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

import java.io.File;

public class MainStageController {
    private final VMRVisualBridge visualBridge;
    private Stage primaryStage;
    
    // 面板引用
    @FXML private RegisterViewController registerView;
    @FXML private MemoryViewController memoryView;
    @FXML private CodeViewController codeView;
    @FXML private StackViewController stackView;
    @FXML private StatusViewController statusView;
    @FXML private LogViewController logView;
    
    public MainStageController(VMRVisualBridge visualBridge) {
        this.visualBridge = visualBridge;
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        setupEventCallbacks();
    }
    
    private void setupEventCallbacks() {
        visualBridge.setExecutionCallback(new VMRVisualBridge.ExecutionCallback() {
            @Override
            public void onRegisterChanged(int regNum, int oldValue, int newValue) {
                Platform.runLater(() -> registerView.updateRegister(regNum, newValue));
            }
            
            @Override
            public void onMemoryChanged(MemoryChangeEvent.MemoryType type, int address, int oldValue, int newValue) {
                Platform.runLater(() -> memoryView.updateMemory(address, newValue));
            }
            
            @Override
            public void onPCChanged(int oldPC, int newPC) {
                Platform.runLater(() -> {
                    codeView.highlightPC(newPC);
                    statusView.updatePC(newPC);
                });
            }
            
            // ... 其他回调方法
        });
    }
    
    // 文件操作
    @FXML private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开虚拟机代码");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("VM代码文件", "*.vm", "*.vmr")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                visualBridge.loadCode(new FileInputStream(file));
                refreshAll();
            } catch (Exception e) {
                showError("加载失败: " + e.getMessage());
            }
        }
    }
    
    // 执行控制
    @FXML private void start() { visualBridge.start(); }
    @FXML private void pause() { visualBridge.pause(); }
    @FXML private void resume() { visualBridge.resume(); }
    @FXML private void stop() { visualBridge.stop(); }
    @FXML private void step() { visualBridge.step(); }
    
    // 视图控制
    @FXML private void refreshAll() {
        registerView.refresh();
        memoryView.refresh();
        codeView.refresh();
        stackView.refresh();
        statusView.refresh();
    }
    
    // 错误处理
    private void showError(String message) {
        // 使用Alert对话框替代JOptionPane
    }
}
```

---

### 阶段三：面板组件迁移 (第4-6周)

#### 3.1 寄存器面板

**文件**: `vizvmr/src/main/java/.../ui/javafx/RegisterView.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.GridPane?>
<?import javafx.scene.layout.ColumnConstraints?>
<?import javafx.scene.layout.RowConstraints?>
<?import javafx.scene.layout.Background?>
<?import javafx.scene.layout.BackgroundFill?>
<?import javafx.scene.layout.CornerRadius?>
<?import javafx.scene.paint.Color?>

<GridPane xmlns="http://javafx.com/javafx/21.0.3"
          xmlns:fx="http://javafx.com/fxml/1"
          fx:controller="org.teachfx.antlr4.ep18r.vizvmr.ui.javafx.RegisterViewController"
          fx:id="rootPane"
          hgap="5" vgap="5">
    
    <columnConstraints>
        <ColumnConstraints percentWidth="50"/>
        <ColumnConstraints percentWidth="50"/>
        <ColumnConstraints percentWidth="50"/>
        <ColumnConstraints percentWidth="50"/>
    </columnConstraints>
    
    <rowConstraints>
        <RowConstraints percentHeight="25"/>
        <RowConstraints percentHeight="25"/>
        <RowConstraints percentHeight="25"/>
        <RowConstraints percentHeight="25"/>
    </rowConstraints>
    
    <!-- 4x4 寄存器网格将通过Java代码动态生成 -->
    
</GridPane>
```

**文件**: `vizvmr/src/main/java/.../ui/javafx/RegisterViewController.java`

```java
package org.teachfx.antlr4.ep18r.vizvmr.ui.javafx;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.teachfx.antlr4.ep18r.vizvmr.integration.VMRVisualBridge;

public class RegisterViewController extends JFXPanelBase {
    private static final String[] REGISTER_NAMES = {
        "r0", "r1", "r2", "r3",
        "r4", "r5", "r6", "r7",
        "r8", "r9", "r10", "r11",
        "r12", "r13(SP)", "r14(FP)", "r15(LR)"
    };
    
    private static final Color COLOR_ZERO = Color.web("90EE90");
    private static final Color COLOR_MODIFIED = Color.web("FFB6C1");
    private static final Color COLOR_SPECIAL = Color.web("ADD8E6");
    private static final Color COLOR_NORMAL = Color.web("DCDCDC");
    
    private final VMRVisualBridge visualBridge;
    private final Label[] registerLabels = new Label[16];
    private final Pane[] cellPanes = new Pane[16];
    private final int[] previousValues = new int[16];
    
    @FXML private GridPane rootPane;
    
    public RegisterViewController(VMRVisualBridge visualBridge) {
        super("RegisterView");
        this.visualBridge = visualBridge;
    }
    
    @Override
    protected void initializeComponents() {
        setTitle("寄存器");
        
        for (int i = 0; i < 16; i++) {
            Pane cellPane = createRegisterCell(i);
            cellPanes[i] = cellPane;
            
            int row = i / 4;
            int col = i % 4;
            GridPane.setConstraints(cellPane, col, row);
            rootPane.getChildren().add(cellPane);
        }
    }
    
    private Pane createRegisterCell(int regNum) {
        VBox cell = new VBox(2);
        cell.setPadding(new Insets(5));
        cell.setBorder(new Border(new BorderStroke(
            Color.LIGHT_GRAY, BorderStrokeStyle.SOLID, 
            new CornerRadii(3), BorderWidths.DEFAULT
        )));
        
        Label nameLabel = new Label(REGISTER_NAMES[regNum]);
        nameLabel.setFont(javafx.scene.text.Font.font("Monospaced", 
            javafx.scene.text.FontWeight.BOLD, 12));
        
        Label valueLabel = new Label("0x00000000 (0)");
        valueLabel.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        registerLabels[regNum] = valueLabel;
        
        cell.getChildren().addAll(nameLabel, valueLabel);
        return cell;
    }
    
    public void updateRegister(int regNum, int value) {
        if (regNum >= 0 && regNum < 16) {
            boolean changed = value != previousValues[regNum];
            previousValues[regNum] = value;
            
            String text = String.format("0x%08X (%d)", value, value);
            registerLabels[regNum].setText(text);
            
            applyColorCoding(regNum, changed);
        }
    }
    
    private void applyColorCoding(int regNum, boolean valueChanged) {
        Color bgColor;
        
        if (regNum == 0) {
            bgColor = COLOR_ZERO;
        } else if (regNum >= 13) {
            bgColor = COLOR_SPECIAL;
        } else if (valueChanged) {
            bgColor = COLOR_MODIFIED;
        } else {
            bgColor = COLOR_NORMAL;
        }
        
        cellPanes[regNum].setBackground(new Background(
            new BackgroundFill(bgColor, new CornerRadii(3), Insets.EMPTY)
        ));
    }
    
    public void refresh() {
        for (int i = 0; i < 16; i++) {
            updateRegister(i, visualBridge.getRegister(i));
        }
    }
}
```

---

### 阶段四：测试和优化 (第7-8周)

#### 4.1 测试策略

1. **单元测试**: 保持现有JUnit测试覆盖
2. **UI测试**: 使用TestFX替代AssertJ-Swing
3. **集成测试**: 端到端功能验证
4. **性能测试**: 响应时间和内存使用基准

#### 4.2 测试示例

```java
@Test
public void testRegisterUpdate() {
    // 创建RegisterViewController
    RegisterViewController controller = new RegisterViewController(visualBridge);
    
    // 测试寄存器更新
    controller.updateRegister(0, 100);
    
    // 验证UI更新
    verify(registerLabel).setText("0x00000064 (100)");
}
```

---

## ⚠️ 风险和缓解措施

### 高风险项

1. **事件系统重构**
   - 缓解: 创建适配器层，保持接口兼容
   - 验证: 单元测试覆盖所有事件类型

2. **布局重构**
   - 缓解: 使用FXML可视化设计
   - 验证: 对比测试布局行为

3. **第三方依赖**
   - 缓解: 分阶段替换，先核心功能
   - 验证: 保持核心逻辑测试

### 中风险项

1. **学习曲线**
   - 缓解: 提供培训材料和代码示例

2. **性能差异**
   - 缓解: 进行基准测试，优化热点代码

---

## 📚 参考资源

- [OpenJFX官方文档](https://openjdk.org/projects/openjfx/)
- [JavaFX 21迁移指南](https://wiki.openjdk.org/spaces/OpenJFX/pages/162889752/JavaFX+Migration+Guide)
- [Scene Builder](https://gluonhq.com/products/scene-builder/)
- [TestFX](https://github.com/TestFX/TestFX)

---

**文档版本**: 1.0
**创建日期**: 2026-01-16
**状态**: 待执行
