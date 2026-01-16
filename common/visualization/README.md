# Common Visualization Framework

## 概述

common模块提供了一套完整的虚拟机可视化框架，包括事件系统、数据绑定、主题管理和教育功能。

## 模块结构

```
visualization/
├── event/                      # 事件系统
│   ├── EventBus.java          # 事件总线
│   ├── EventPublisher.java    # 事件发布者
│   ├── EventSubscriber.java   # 事件订阅者
│   └── *.java                 # 具体事件类
│
├── ui/                        # UI组件
│   ├── VisualPanelBase.java   # Swing面板基类
│   ├── ThemeManager.java      # 主题管理
│   ├── DataBinding.java       # 数据绑定
│   ├── PanelManager.java      # 面板管理
│   └── javafx/               # JavaFX支持 (新增)
│       ├── JFXPanelBase.java  # JavaFX面板基类
│       └── JFXThemeManager.java # JavaFX主题管理
│
└── *.java                    # 核心接口和模型
```

## 技术规范

| 项目 | 规范 |
|------|------|
| Java 版本 | 21+ |
| UI 框架 | Swing + JavaFX (迁移中) |
| 构建工具 | Maven 3.8+ |
| 测试框架 | JUnit 5 |

## JavaFX 迁移

### 迁移状态

- **事件系统**: ✅ 已设计适配器
- **数据绑定**: ✅ 已设计双框架支持
- **主题系统**: ✅ 已设计CSS主题
- **面板基类**: ✅ 已实现JFXPanelBase

### 文档链接

- [实现指南](./IMPLEMENTATION_GUIDE.md)
- [JavaFX迁移指南](./JAVAFX_MIGRATION_GUIDE.md)

## 使用方法

### Swing版本

```java
// 创建Swing面板
RegisterPanel panel = new RegisterPanel(visualBridge);
```

### JavaFX版本 (迁移后)

```java
// 创建JavaFX面板
RegisterView view = new RegisterView(visualBridge);
```

## 迁移到JavaFX

参考[JAVAFX_MIGRATION_GUIDE.md](./JAVAFX_MIGRATION_GUIDE.md)获取详细迁移指导。

---

**最后更新**: 2026-01-16
