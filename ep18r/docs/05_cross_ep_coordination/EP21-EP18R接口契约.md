# EP21-EP18R接口契约

**文档版本**: 1.0  
**创建日期**: 2026-01-11  
**最后更新**: 2026-01-11  
**维护者**: EP18R开发团队  

---

## 概述

本文档定义了EP21编译器与EP18R寄存器虚拟机之间的接口契约。EP21作为高级编译器，将使用EP18R提供的寄存器分配和代码生成功能。

---

## 接口规范

### 1. IRegisterAllocator接口

**包路径**: `org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator`

#### 核心功能
负责将虚拟寄存器（变量）映射到物理寄存器，并在物理寄存器不足时处理溢出到栈的情况。

#### 方法规范

| 方法 | 描述 | 参数 | 返回值 | 异常 |
|------|------|------|--------|------|
| `allocate(String varName)` | 为变量分配物理寄存器 | 变量名称 | 分配的物理寄存器编号 (0-15) | `IllegalStateException` - 无可用寄存器 |
| `free(String varName)` | 释放变量占用的物理寄存器 | 变量名称 | void | - |
| `getRegister(String varName)` | 获取变量对应的物理寄存器 | 变量名称 | 物理寄存器编号，未分配返回-1 | - |
| `getAllocation()` | 获取当前所有变量到寄存器的映射 | - | 不可修改的映射表 | - |
| `reset()` | 重置分配器状态 | - | void | - |
| `spillToStack(String varName)` | 将变量溢出到栈 | 变量名称 | 栈槽位偏移量 | `IllegalStateException` - 变量未分配寄存器 |
| `isSpilled(String varName)` | 检查变量是否已溢出到栈 | 变量名称 | true如果已溢出 | - |
| `getSpillSlot(String varName)` | 获取变量的栈槽位偏移量 | 变量名称 | 栈槽位偏移量，未溢出返回-1 | - |
| `getAvailableRegisterCount()` | 获取可分配寄存器数量 | - | 可分配寄存器数量 | - |
| `getAllocatedRegisterCount()` | 获取已分配寄存器数量 | - | 已分配寄存器数量 | - |
| `getAllocatedVariables()` | 获取已分配寄存器的变量名集合 | - | 已分配变量名集合 | - |
| `getSpilledVariables()` | 获取已溢出到栈的变量名集合 | - | 已溢出变量名集合 | - |

#### 寄存器规范

EP18R使用16个寄存器，其中可分配寄存器为R2-R7、R8-R12：

| 寄存器 | 名称 | 用途 | 可分配 |
|--------|------|------|--------|
| R0 | zero | 零寄存器，恒为0 | 否 |
| R1 | ra | 返回地址/临时寄存器 | 是（调用者保存） |
| R2-R7 | a0-a5 | 参数寄存器/返回值 | 是（调用者保存） |
| R8-R12 | s0-s4 | 被调用者保存寄存器 | 是（被调用者保存） |
| R13 | sp | 栈指针 | 否 |
| R14 | fp | 帧指针 | 否 |
| R15 | lr | 链接寄存器 | 是（调用者保存） |

---

### 2. RegisterAssembler接口

**包路径**: `org.teachfx.antlr4.ep18r.pass.codegen.RegisterAssembler`

#### 核心功能
将IR指令转换为32位固定格式的寄存器VM字节码，支持R-type、I-type和J-type指令格式。

#### 指令格式

**R-type**: `[opcode:6][rd:5][rs1:5][rs2:5][unused:11]` - 算术/逻辑指令  
**I-type**: `[opcode:6][rd:5][rs1:5][imm:16]` - 加载/存储/分支指令  
**J-type**: `[opcode:6][imm:26]` - 跳转指令  

#### 核心方法

| 方法类别 | 方法示例 | 描述 |
|----------|----------|------|
| 算术运算 | `emitAdd(rd, rs1, rs2)` | 生成加法指令 |
| 比较运算 | `emitSlt(rd, rs1, rs2)` | 生成小于比较指令 |
| 逻辑运算 | `emitAnd(rd, rs1, rs2)` | 生成逻辑与指令 |
| 内存访问 | `emitLw(rd, base, offset)` | 生成加载字指令 |
| 控制流 | `emitJ(target)` | 生成无条件跳转指令 |
| 标签管理 | `defineLabel(label)` | 定义标签位置 |
| 状态查询 | `getBytecode()` | 获取生成的字节码 |

#### 使用约束

1. **寄存器范围**: 所有寄存器参数必须在0-15范围内
2. **立即数范围**: 16位立即数必须有符号，26位立即数用于跳转偏移
3. **标签引用**: 引用的标签必须在后续代码中定义
4. **内存对齐**: 所有指令地址必须是4字节对齐

---

### 3. RegisterVMInterpreter接口

**包路径**: `org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter`

#### 核心功能
执行生成的寄存器VM字节码，提供完整的虚拟机运行时环境。

#### 关键方法

| 方法 | 描述 | 参数 | 返回值 | 异常 |
|------|------|------|--------|------|
| `load(interp, input)` | 加载汇编代码 | 虚拟机实例，输入流 | 是否有错误 | Exception - 加载失败 |
| `exec()` | 执行字节码 | - | - | Exception - 执行错误 |
| `getRegister(regNum)` | 获取寄存器值 | 寄存器编号(0-15) | 寄存器值 | IllegalArgumentException |
| `setRegister(regNum, value)` | 设置寄存器值 | 寄存器编号，值 | - | IllegalArgumentException |
| `getConfig()` | 获取VM配置 | - | VMConfig | - |

#### 运行时特性

1. **寄存器文件**: 16个32位寄存器，R0恒为0
2. **内存模型**: 堆、栈、全局变量分离
3. **调用约定**: 标准寄存器分配和栈帧管理
4. **异常处理**: 除零、栈溢出、内存越界等异常检测
5. **安全机制**: 最大执行步数限制，防止无限循环

---

## 调用示例

### 示例1: 基本算术运算

```java
// 创建寄存器分配器
IRegisterAllocator allocator = new BasicRegisterAllocator();

// 创建汇编器
RegisterAssembler assembler = new RegisterAssembler(allocator);

// 生成代码
assembler.emitLi(2, 10);    // li r2, 10
assembler.emitLi(3, 20);    // li r3, 20
assembler.emitAdd(4, 2, 3); // add r4, r2, r3
assembler.emitHalt();       // halt

// 获取字节码
List<Integer> bytecode = assembler.getBytecode();
```

### 示例2: 条件跳转

```java
// 生成条件跳转
assembler.emitLi(1, 0);        // li r1, 0
assembler.emitJf(1, "end");    // jf r1, end
assembler.emitLi(2, 42);       // li r2, 42
assembler.defineLabel("end");  // end:
assembler.emitHalt();          // halt
```

### 示例3: 内存访问

```java
// 加载和存储
assembler.emitLi(1, 100);      // li r1, 100 (地址)
assembler.emitLi(2, 42);       // li r2, 42 (值)
assembler.emitSw(2, 1, 0);     // sw r2, r1, 0
assembler.emitLw(3, 1, 0);     // lw r3, r1, 0
assembler.emitHalt();          // halt
```

---

## 异常处理

### 运行时异常

| 异常类型 | 触发条件 | 处理方式 |
|----------|----------|----------|
| `ArithmeticException` | 除零操作 | 抛出异常，终止执行 |
| `IndexOutOfBoundsException` | 内存访问越界 | 抛出异常，终止执行 |
| `RuntimeException` | 超过最大执行步数 | 抛出异常，可能是无限循环 |
| `IllegalArgumentException` | 无效寄存器编号 | 抛出异常，参数验证失败 |
| `ClassCastException` | 类型转换错误 | 抛出异常，常量池类型不匹配 |

### 错误处理策略

1. **预防性检查**: 在指令执行前进行参数验证
2. **边界检查**: 所有内存访问都进行边界检查
3. **类型检查**: 常量池访问时进行类型检查
4. **资源限制**: 设置最大执行步数和栈深度限制

---

## 性能特性

### 执行效率

1. **指令缓存**: 32位指令字，高效解码
2. **寄存器访问**: 直接寄存器文件访问，无内存延迟
3. **分支预测**: 简单的顺序执行，跳转指令直接设置PC

### 内存管理

1. **堆分配**: 线性分配，简单高效
2. **栈管理**: 固定大小栈，快速压栈/出栈
3. **常量池**: 编译时常量，运行时只读访问

### 优化建议

1. **寄存器分配**: 优先使用调用者保存寄存器减少保存/恢复
2. **指令选择**: 合理使用立即数指令减少加载操作
3. **内存访问**: 尽量使用寄存器直接操作，减少内存访问

---

## 版本兼容性

### 向后兼容

1. **指令集**: 新增指令不会改变现有指令编码
2. **寄存器**: 寄存器数量和用途保持稳定
3. **内存模型**: 堆、栈、全局变量布局保持一致

### 向前兼容

1. **扩展指令**: 预留操作码空间支持新指令
2. **寄存器扩展**: 架构支持扩展到32个寄存器
3. **内存扩展**: 支持更大的堆和栈空间

---

## 测试建议

### 单元测试

1. **指令测试**: 每个指令单独测试功能正确性
2. **边界测试**: 测试寄存器边界、内存边界、立即数范围
3. **异常测试**: 验证异常触发条件和错误消息

### 集成测试

1. **程序测试**: 完整程序的编译和执行测试
2. **调用测试**: 函数调用和返回的正确性测试
3. **内存测试**: 堆、栈、全局变量的交互测试

### 性能测试

1. **执行速度**: 指令执行时间基准测试
2. **内存使用**: 内存分配和访问效率测试
3. **扩展性**: 大规模程序和数据的处理能力测试

---

## 维护记录

| 日期 | 版本 | 变更内容 | 维护者 |
|------|------|----------|--------|
| 2026-01-11 | 1.0 | 初始版本创建 | EP18R团队 |

---

**文档状态**: ✅ 已发布  
**审核状态**: ✅ 已审核  
**发布状态**: ✅ 公开  

---

*此文档遵循EP项目接口规范，是EP21与EP18R集成开发的权威参考。*