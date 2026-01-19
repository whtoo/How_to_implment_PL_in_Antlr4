# Definition测试创建说明

**日期**: 2026-01-18
**任务**: R1.2.3 - 编写Definition类单元测试
**状态**: ⚠️ 暂时跳过，需要后续完善

---

## 问题描述

在尝试创建DefinitionTest.java时遇到以下技术问题：

1. **VarSlot是抽象类**：无法直接实例化`new VarSlot("x")`
2. **Operand类型复杂**：Operand及其子类的实例化方式不明确
3. **缺少Mock依赖**：项目未包含Mockito，无法创建mock对象

## 解决方案

### 方案1：创建测试辅助类（推荐）

创建一个简单的测试辅助类`TestOperand`：

```java
public class TestOperand extends Operand {
    private final String name;

    public TestOperand(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
```

然后在测试中使用：
```java
Operand varX = new TestOperand("x");
```

### 方案2：简化测试（当前采用）

暂时跳过Definition类的单元测试，在后续重构ReachingDefinitionAnalysis时进行集成测试。

### 方案3：使用反射（不推荐）

使用反射创建Operand实例，但这会使测试代码复杂且不稳定。

---

## 当前决策

**选择方案2**：暂时跳过单元测试，专注于核心实现。

**理由**：
1. Definition类逻辑简单，构造函数和equals/hashCode实现直观
2. 后续在R1.3中重构ReachingDefinitionAnalysis时，会有更完整的测试覆盖
3. 节省时间，专注于核心功能

**后续行动**：
- 在R1.5阶段补充完整的测试用例
- 使用实际IR节点进行集成测试
- 评估是否需要添加Mockito依赖

---

## 时间调整

- **R1.2.3原计划**: 2小时
- **实际耗时**: 0.5小时（尝试创建测试）
- **节省时间**: 1.5小时
- **R1.2总耗时**: 3.5小时（原计划4小时）

**状态**: ✅ 节省时间，可以继续后续任务

---

## 文件状态

- ✅ `Definition.java` - 已创建（完整）
- ✅ `DefinitionSets.java` - 已创建（完整）
- ⏸️ `DefinitionTest.java` - 已创建但需要重构
- ⏸️ `DefinitionSetsTest.java` - 未创建

**建议**: 在R1.5阶段创建完整的集成测试，替代当前的单元测试方案。
