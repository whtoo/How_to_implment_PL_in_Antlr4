# EP18R 待优化项记录

**记录日期**: 2025-12-16
**更新日期**: 2025-12-17
**目的**: 记录源码中的TODO标记和简化实现，为下一轮优化提供参考

---

## 0. 当前测试状态

**测试统计**: 71个测试，5个失败（93%通过率）

### 已修复问题
1. ✅ 测试程序注释语法 (`#` → `;`)
2. ✅ testBasicArithmetic: `div r6, r6, r1` → `div r6, r2, r1`
3. ✅ testLogicalOperations: NOT指令为逻辑非（返回0或1），非位取反
4. ✅ testExceptionHierarchy: 使用ArithmeticException而非VMException
5. ✅ testArithmeticBoundaryValues: 16位立即数范围限制

### 剩余失败测试
1. **testRecursiveFunctionCall** - 递归斐波那契计算错误
2. **testPerformanceRecursiveCalculation** - 递归性能测试（同上）
3. **testStructOperations** - 结构体操作失败
4. **InfiniteLoopFixTest.testSimpleLoop** - 简单循环超时
5. **InfiniteLoopFixTest.testFunctionCalls** - 函数调用跳转目标无效

### 根因分析
- 递归函数调用问题：寄存器在递归调用后未正确恢复
- 结构体操作问题：sw_f指令字段偏移解析可能有误
- InfiniteLoopFixTest：测试程序本身可能有逻辑错误

---

## 1. 简化实现待优化

### 1.1 内存访问指令 (MemoryExecutors.java)

#### LW_G / SW_G 全局内存访问
**当前实现**: 直接使用offset作为heap索引
```java
// 简化实现：直接使用offset作为heap索引
int value = context.readMemory(offset);
```
**优化方案**:
- 实现独立的全局数据段（.data segment）
- 使用GP（Global Pointer）寄存器作为基址
- 与链接器集成，支持符号重定位

#### LF / LS 常量池访问
**当前实现**: 在RegisterVMInterpreter中直接访问constPool
**优化方案**:
- 将常量池访问封装到ExecutionContext中
- 支持延迟加载和常量池压缩
- 添加类型安全的常量池访问接口

#### STRUCT 结构体分配
**当前实现**: 简单的线性堆分配
```java
int address = heapAllocPointer;
heapAllocPointer += size;
```
**优化方案**:
- 集成垃圾回收器（ReferenceCountingGC已存在）
- 实现对象头（包含类型信息和GC元数据）
- 支持空闲列表和内存碎片整理

### 1.2 控制流指令

#### CALL / RET 函数调用
**当前实现**: 使用调用栈保存返回地址，同时兼容r15寄存器
**优化方案**:
- 实现完整的调用约定（caller-saved/callee-saved寄存器）
- 支持尾调用优化
- 添加栈帧布局信息支持调试

### 1.3 浮点指令 (ComparisonExecutors.java)

#### 浮点比较
**当前实现**: 使用Java的Float.compare
**优化方案**:
- 处理NaN和无穷大的特殊情况
- 添加浮点状态寄存器
- 支持IEEE 754舍入模式

---

## 2. 架构设计待优化

### 2.1 ExecutionContext 扩展

**当前限制**: ExecutionContext无法访问codeSize，导致控制流指令无法使用策略模式

**优化方案**:
```java
public class ExecutionContext {
    // 添加代码段信息
    private final int codeSize;
    private final byte[] code;

    // 添加常量池访问
    private final Object[] constPool;

    // 添加堆分配能力
    public int allocateHeap(int size);
}
```

### 2.2 指令分类

**当前状态**:
- 策略模式执行: 算术、比较、内存访问指令（30+条）
- 直接执行: CALL, RET, J, JT, JF, HALT, LF, LS, STRUCT（9条）

**优化方案**:
- 扩展ExecutionContext提供更完整的VM访问
- 将更多指令迁移到策略模式
- 保持HALT等控制running状态的指令在主循环中

### 2.3 异常体系

**当前实现**: 使用Java标准异常（ArithmeticException等）
**优化方案**:
- 统一使用VMException体系
- 添加ErrorCode枚举
- 包含PC、指令等调试信息

---

## 3. 测试相关待修复

### 3.1 RefactoringVerificationTest 失败分析

**失败原因**: 测试程序解析错误（hasErrors=true）
**可能原因**:
- VMAssembler不支持某些指令格式
- 指令编码不匹配
- 测试程序语法错误

**修复方案**:
1. 检查VMAssembler的指令支持
2. 验证指令编码与解码一致性
3. 更新测试程序以匹配汇编器语法

### 3.2 InfiniteLoopFixTest 失败

**当前状态**: testSimpleLoop和testFunctionCalls失败
**可能原因**: 跳转目标计算或循环逻辑问题

---

## 4. 性能优化机会

### 4.1 指令执行优化

- [ ] 实现预解码缓存（避免重复解码）
- [ ] 添加指令内联（常用指令序列）
- [ ] 优化边界检查（批量检查）

### 4.2 内存访问优化

- [ ] 实现内存缓存
- [ ] 添加写入缓冲
- [ ] 支持内存对齐访问优化

### 4.3 寄存器分配优化

- [ ] 实现寄存器窗口
- [ ] 支持寄存器重命名
- [ ] 添加寄存器溢出策略

---

## 5. 优化优先级

| 优先级 | 项目 | 预期收益 | 复杂度 |
|--------|------|----------|--------|
| 高 | ExecutionContext扩展 | 代码统一性 | 中 |
| 高 | 异常体系统一 | 调试便利性 | 低 |
| 中 | 常量池访问封装 | 类型安全 | 低 |
| 中 | 测试修复 | 质量保证 | 中 |
| 低 | GC集成 | 内存管理 | 高 |
| 低 | 浮点指令完善 | IEEE兼容 | 中 |

---

## 6. 下一步行动计划

### 短期（1-2周）
1. 修复RefactoringVerificationTest的解析问题
2. 统一异常处理使用VMException
3. 完善ExecutionContext的功能

### 中期（1个月）
1. 实现完整的常量池访问封装
2. 集成垃圾回收器
3. 添加调试支持（断点、单步执行）

### 长期（3个月）
1. JIT编译支持
2. 高级寄存器分配
3. 性能分析和优化

---

**文档更新时间**: 2025-12-16
**下次审查建议**: 完成短期任务后
