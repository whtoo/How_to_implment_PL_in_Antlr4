# Common模块 JavaFX迁移补充指南

## 📋 概述

本文档是`IMPLEMENTATION_GUIDE.md`的补充，专门针对common模块的Swing到JavaFX迁移提供详细的技术指导和最佳实践。

**适用范围**: common模块中的事件系统、数据绑定、主题管理和教育功能

---

## 🎯 迁移目标

### 核心目标

1. **框架解耦**: UI框架细节与业务逻辑完全分离
2. **双框架支持**: 同时支持Swing和JavaFX两种实现
3. **零性能损失**: 迁移后性能不降级
4. **完整功能**: 所有现有功能完全保留

### 非目标

- 不要求一次性完成所有迁移
- 不要求统一两种框架的API
- 不要求降低测试覆盖率

---

## 📁 文件变更清单

### 新增文件

```
common/src/main/java/org/teachfx/antlr4/common/visualization/
├── ui/
│   └── javafx/
│       ├── JFXPanelBase.java              # JavaFX面板基类
│       ├── JFXThemeManager.java           # JavaFX主题管理
│       └── JFXDataBinding.java            # JavaFX数据绑定
│
└── event/
    └── javafx/
        └── JFXEventAdapter.java           # 事件适配器
```

### 修改文件

```
common/src/main/java/org/teachfx/antlr4/common/visualization/
├── ui/
│   ├── VisualPanelBase.java               # 添加框架检测逻辑
│   ├── DataBinding.java                   # 添加JavaFX支持
│   └── ThemeManager.java                  # 添加CSS主题支持
│
└── event/
    ├── EventBus.java                      # 添加框架事件路由
    ├── EventSubscriber.java               # 添加JavaFX事件处理
    └── EventPublisher.java                # 添加双框架发布支持
```

### 保留文件

- 所有现有的Swing实现保持不变
- 作为后备实现，确保平滑过渡

---

## 🔧 核心技术实现

### 1. 框架抽象层

#### 1.1 UI框架枚举

```java
/**
 * UI框架类型
 */
public enum UIFramework {
    SWING("Swing", "javax.swing"),
    JAVAFX("JavaFX", "javafx");
    
    private final String name;
    private final String packagePrefix;
    
    UIFramework(String name, String packagePrefix) {
        this.name = name;
        this.packagePrefix = packagePrefix;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPackagePrefix() {
        return packagePrefix;
    }
    
    /**
     * 检测当前运行环境
     */
    public static UIFramework detect() {
        try {
            Class.forName("javafx.application.Application");
            return JAVAFX;
        } catch (ClassNotFoundException e) {
            return SWING;
        }
    }
}
```

#### 1.2 面板工厂

```java
/**
 * UI面板工厂
 * 根据运行时环境创建对应的面板实现
 */
public class UIPanelFactory {
    private static final UIFramework FRAMEWORK = UIFramework.detect();
    
    /**
     * 创建面板实例
     */
    @SuppressWarnings("unchecked")
    public static <T extends VisualPanelBase> T createPanel(
            Class<T> panelClass,
            VMRVisualBridge visualBridge) {
        
        try {
            if (FRAMEWORK == UIFramework.JAVAFX) {
                // 尝试加载JavaFX版本
                String jfxClassName = panelClass.getName()
                    .replace(".ui.panel.", ".ui.javafx.")
                    .replace("Panel", "View");
                
                Class<?> jfxClass = Class.forName(jfxClassName);
                Constructor<?> constructor = jfxClass.getConstructor(
                    VMRVisualBridge.class
                );
                return (T) constructor.newInstance(visualBridge);
            } else {
                // 使用Swing版本
                return panelClass.getConstructor(VMRVisualBridge.class)
                    .newInstance(visualBridge);
            }
        } catch (Exception e) {
            // 如果JavaFX版本不存在，使用Swing版本
            try {
                return panelClass.getConstructor(VMRVisualBridge.class)
                    .newInstance(visualBridge);
            } catch (Exception ex) {
                throw new RuntimeException(
                    "Failed to create panel: " + panelClass.getName(), ex
                );
            }
        }
    }
}
```

### 2. 事件系统扩展

#### 2.1 统一事件接口

```java
/**
 * 统一事件接口
 * 支持Swing和JavaFX事件模型
 */
public interface UnifiedEvent {
    
    /**
     * 获取事件类型
     */
    String getEventType();
    
    /**
     * 获取事件源
     */
    Object getSource();
    
    /**
     * 获取时间戳
     */
    long getTimestamp();
    
    /**
     * 转换为Swing事件
     */
    default java.util.EventObject toSwingEvent() {
        return new java.util.EventObject(this);
    }
    
    /**
     * 转换为JavaFX事件
     */
    default javafx.event.Event toJFXEvent() {
        return new javafx.event.Event(this);
    }
}
```

#### 2.2 事件路由器

```java
/**
 * 事件路由器
 * 在Swing和JavaFX事件系统之间路由事件
 */
public class EventRouter {
    private final EventBus eventBus;
    private final UIFramework framework;
    
    public EventRouter(EventBus eventBus, UIFramework framework) {
        this.eventBus = eventBus;
        this.framework = framework;
    }
    
    /**
     * 路由事件到正确的框架
     */
    public void routeEvent(UnifiedEvent event) {
        switch (framework) {
            case SWING:
                routeToSwing(event);
                break;
            case JAVAFX:
                routeToJavaFX(event);
                break;
        }
    }
    
    private void routeToSwing(UnifiedEvent event) {
        // 转换为Swing事件并发布
        eventBus.publish(event.toSwingEvent());
    }
    
    private void routeToJavaFX(UnifiedEvent event) {
        // 转换为JavaFX事件并发布
        Platform.runLater(() -> {
            eventBus.publish(event.toJFXEvent());
        });
    }
}
```

### 3. 数据绑定系统

#### 3.1 可观察属性封装

```java
/**
 * 可观察属性封装
 * 统一Swing和JavaFX的属性绑定API
 */
public abstract class ObservableProperty<T> {
    
    /**
     * 获取属性值
     */
    public abstract T get();
    
    /**
     * 设置属性值
     */
    public abstract void set(T value);
    
    /**
     * 添加监听器 - Swing版本
     */
    public abstract void addSwingListener(
        java.beans.PropertyChangeListener listener);
    
    /**
     * 添加监听器 - JavaFX版本
     */
    public abstract void addJFXListener(
        javafx.beans.InvalidationListener listener);
    
    /**
     * 创建JavaFX属性
     */
    public static <T> ObservableProperty<T> create(
            T initialValue,
            java.util.function.Supplier<T> getter,
            java.util.function.Consumer<T> setter) {
        
        return new ObservableProperty<T>() {
            private T value = initialValue;
            
            @Override
            public T get() {
                return value;
            }
            
            @Override
            public void set(T newValue) {
                T oldValue = this.value;
                this.value = newValue;
                fireChange(oldValue, newValue);
            }
            
            private void fireChange(T oldValue, T newValue) {
                // 触发所有监听器
            }
            
            @Override
            public void addSwingListener(
                    java.beans.PropertyChangeListener listener) {
                // Swing监听器实现
            }
            
            @Override
            public void addJFXListener(
                    javafx.beans.InvalidationListener listener) {
                // JavaFX监听器实现
            }
        };
    }
}
```

#### 3.2 双向绑定

```java
/**
 * 双向绑定管理器
 */
public class BidirectionalBinding {
    
    /**
     * 创建Swing到JavaFX的双向绑定
     */
    public static <T> void bindSwingToJFX(
            javax.swing.JComponent swingComponent,
            String swingProperty,
            javafx.beans.property.Property<T> jfxProperty) {
        
        // Swing -> JavaFX
        swingComponent.addPropertyChangeListener(swingProperty, evt -> {
            jfxProperty.setValue((T) evt.getNewValue());
        });
        
        // JavaFX -> Swing
        jfxProperty.addListener((obs, oldVal, newVal) -> {
            // 根据组件类型设置属性
            if (swingComponent instanceof javax.swing.JLabel) {
                ((javax.swing.JLabel) swingComponent)
                    .setText(newVal != null ? newVal.toString() : "");
            } else if (swingComponent instanceof javax.swing.JTextField) {
                ((javax.swing.JTextField) swingComponent)
                    .setText(newVal != null ? newVal.toString() : "");
            }
        });
    }
}
```

### 4. 主题系统

#### 4.1 主题定义

```java
/**
 * 主题定义
 */
public class ThemeDefinition {
    private final String name;
    private final String cssFile;
    private final Map<String, String> properties;
    
    public ThemeDefinition(String name, String cssFile) {
        this.name = name;
        this.cssFile = cssFile;
        this.properties = new HashMap<>();
    }
    
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    public String getName() {
        return name;
    }
    
    public String getCssFile() {
        return cssFile;
    }
    
    public Map<String, String> getProperties() {
        return properties;
    }
    
    /**
     * 预定义主题
     */
    public static ThemeDefinition LIGHT = new ThemeDefinition(
        "Light", "themes/light.css"
    );
    
    public static ThemeDefinition DARK = new ThemeDefinition(
        "Dark", "themes/dark.css"
    );
    
    public static ThemeDefinition EDUCATIONAL = new ThemeDefinition(
        "Educational", "themes/educational.css"
    );
    
    public static ThemeDefinition HIGH_CONTRAST = new ThemeDefinition(
        "High Contrast", "themes/high-contrast.css"
    );
}
```

#### 4.2 主题应用器

```java
/**
 * 主题应用器
 */
public class ThemeApplicator {
    
    /**
     * 应用Swing主题
     */
    public static void applySwingTheme(ThemeDefinition theme) {
        try {
            for (Map.Entry<String, String> entry : 
                 theme.getProperties().entrySet()) {
                UIManager.put(entry.getKey(), entry.getValue());
            }
            
            // 刷新所有窗口
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to apply Swing theme: " + theme.getName(), e
            );
        }
    }
    
    /**
     * 应用JavaFX主题
     */
    public static void applyJFXTheme(javafx.scene.Scene scene, 
                                      ThemeDefinition theme) {
        try {
            String cssUrl = ThemeApplicator.class.getResource(
                "/css/" + theme.getCssFile()
            ).toExternalForm();
            
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssUrl);
            
            // 应用动态属性
            for (Map.Entry<String, String> entry : 
                 theme.getProperties().entrySet()) {
                scene.getRoot().setStyle(
                    scene.getRoot().getStyle() + 
                    String.format("-%s: %s;", entry.getKey(), entry.getValue())
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to apply JavaFX theme: " + theme.getName(), e
            );
        }
    }
}
```

---

## 📝 迁移步骤详解

### 步骤一：创建基础设施

#### 1.1 添加依赖

在`common/pom.xml`中添加：

```xml
<!-- JavaFX Dependencies -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>${javafx.version}</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>${javafx.version}</version>
</dependency>
```

#### 1.2 创建基类

创建`JFXPanelBase.java`，参考实现见`IMPLEMENTATION_GUIDE.md`。

#### 1.3 创建适配器

创建`JFXEventAdapter.java`，参考实现见`IMPLEMENTATION_GUIDE.md`。

### 步骤二：迁移事件系统

#### 2.1 修改EventBus

在`EventBus.java`中添加：

```java
/**
 * 发布JavaFX事件
 */
public <T extends javafx.event.Event> void publishJFX(T event) {
    history.addEvent(event);
    subscribers.forEach(subscriber -> {
        if (subscriber instanceof JFXEventHandler) {
            ((JFXEventHandler) subscriber).handle(event);
        }
    });
}
```

#### 2.2 创建事件适配器

创建`JFXEventAdapter`类，实现Swing和JavaFX事件的双向转换。

### 步骤三：迁移数据绑定

#### 3.1 扩展DataBinding

在`DataBinding.java`中添加JavaFX绑定方法。

#### 3.2 创建ObservableProperty

实现统一的属性观察接口。

### 步骤四：迁移主题系统

#### 4.1 创建CSS主题

在`resources/css/themes/`目录下创建CSS文件。

#### 4.2 实现主题应用器

创建`JFXThemeManager`类，支持主题切换。

---

## 🧪 测试指南

### 单元测试

```java
@Test
public void testEventRouting() {
    EventBus eventBus = new EventBus();
    EventRouter router = new EventRouter(eventBus, UIFramework.JAVAFX);
    
    // 测试事件路由
    TestEvent event = new TestEvent();
    router.routeEvent(event);
    
    // 验证事件被正确路由
    assertTrue(eventBus.getHistory().contains(event));
}
```

### 集成测试

```java
@Test
public void testPanelCreation() {
    VMRVisualBridge bridge = createMockBridge();
    
    // 测试Swing面板创建
    RegisterPanel swingPanel = new RegisterPanel(bridge);
    assertNotNull(swingPanel);
    
    // 测试JavaFX面板创建（如果可用）
    if (UIFramework.detect() == UIFramework.JAVAFX) {
        RegisterView jfxPanel = new RegisterView(bridge);
        assertNotNull(jfxPanel);
    }
}
```

---

## 🚨 常见问题

### Q1: 如何在运行时切换框架？

使用系统属性：
```java
-Dui.framework=swing  # 强制使用Swing
-Dui.framework=javafx # 强制使用JavaFX
```

### Q2: 迁移过程中如何保持功能？

1. 保持Swing实现不变
2. 创建新的JavaFX实现
3. 使用工厂模式选择实现
4. 逐步迁移面板组件

### Q3: 事件系统如何兼容？

1. 使用统一事件接口
2. 创建事件适配器
3. 在EventBus中添加路由逻辑

---

## 📚 参考资源

- [OpenJFX官方文档](https://openjfx.io/openjfx-docs/)
- [JavaFX 21迁移指南](https://wiki.openjdk.org/spaces/OpenJFX/pages/162889752/JavaFX+Migration+Guide)
- [TestFX GitHub](https://github.com/TestFX/TestFX)

---

**文档版本**: 1.0
**创建日期**: 2026-01-16
**最后更新**: 2026-01-16
**状态**: 待审阅
