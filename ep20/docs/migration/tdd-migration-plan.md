# EP19到EP20语法迁移TDD计划

## 迁移目标
将EP19支持的所有语法特性迁移到EP20，确保向后兼容性。

## TDD迁移策略
采用渐进式测试驱动开发，每个语法特性按以下步骤实施：
1. 编写测试用例
2. 实现最小可行语法
3. 通过测试
4. 重构优化
5. 更新文档

## 迁移阶段划分

### 阶段1：基础运算符支持（优先级1）
**目标**：添加缺失的运算符
- 添加%（取模）运算符
- 添加&&（逻辑与）运算符

**测试文件**：`test/operators/`
**预计工作量**：2-3小时
**依赖**：无

### 阶段2：数组语法支持（优先级2）
**目标**：添加数组声明和访问
- 数组声明：int arr[5];
- 数组访问：arr[index]

**测试文件**：`test/arrays/`
**预计工作量**：4-6小时
**依赖**：阶段1

### 阶段3：typedef支持（优先级3）
**目标**：添加类型别名
- typedef语法：typedef int MyInt;

**测试文件**：`test/typedef/`
**预计工作量**：3-4小时
**依赖**：阶段1-2

### 阶段4：结构体语法支持（优先级4）
**目标**：完整结构体支持
- struct声明
- 结构体变量声明
- 结构体方法
- 结构体字段访问
- 结构体方法调用
- new表达式

**测试文件**：`test/struct/`
**预计工作量**：8-12小时
**依赖**：阶段1-3

## 测试驱动开发流程

### 每个特性的TDD循环
1. **红色阶段**：编写失败的测试用例
2. **绿色阶段**：实现最小功能通过测试
3. **重构阶段**：优化代码结构
4. **文档阶段**：更新相关文档

### 测试用例分类
```
test/
├── operators/
│   ├── modulo_test.cym
│   ├── logical_and_test.cym
├── arrays/
│   ├── array_declaration_test.cym
│   ├── array_access_test.cym
│   └── array_assignment_test.cym
├── typedef/
│   ├── basic_typedef_test.cym
│   └── typedef_usage_test.cym
└── struct/
    ├── struct_declaration_test.cym
    ├── struct_instantiation_test.cym
    ├── struct_field_access_test.cym
    ├── struct_method_call_test.cym
    └── nested_struct_test.cym
```

## 语法文件修改计划

### 阶段修改策略
每次修改只添加一个语法特性，确保可回滚。

#### 修改文件列表
1. `Cymbol.g4` - 语法规则
2. `AST节点` - 新增语法对应的AST节点
3. `Visitor实现` - 语义分析和代码生成
4. `符号表` - 处理新类型的符号

## 验证策略
1. **单元测试**：每个语法特性的独立测试
2. **集成测试**：EP19示例代码的兼容性测试
3. **回归测试**：确保现有功能不受影响

## 风险缓解
- 每个阶段完成后创建git tag
- 保持向后兼容，逐步弃用而非删除
- 提供迁移指南和示例代码

## 时间安排
- 阶段1：第1天
- 阶段2：第2-3天
- 阶段3：第4天
- 阶段4：第5-7天
- 集成测试：第8天