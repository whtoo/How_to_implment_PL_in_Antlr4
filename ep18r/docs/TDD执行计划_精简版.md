# EP18R TDD执行计划（精简版）

**版本**: v1.0 | **日期**: 2026-01-07 | **状态**: 核心VM完成，VM适配任务待执行
**目的**: 追踪EP18R寄存器VM开发进展，管理任务优先级
**相关文档**: [TDD重构计划](TDD重构计划.md) | [EP18R核心设计文档](EP18R_核心设计文档.md) | [EP18R ABI设计文档](EP18R_ABI_设计文档.md) | [单元测试案例集](单元测试案例集.md)

---

## 1. 执行摘要

| 指标 | 值 | 状态 |
|------|-----|------|
| **核心VM功能** | 完成 | ✅ |
| **测试通过率** | 100% (79/79) | ✅ |
| **ABI规范实现** | 完成 | ✅ |
| **VM适配任务** | 6个待完成 | ⏸️ |

---

## 2. 已完成里程碑 ✅

### ✅ Phase 1: 基础VM实现 (2025-12-20)
- **TASK-001**: 统一寄存器命名和用途定义
- **TASK-003**: 实现栈帧8字节对齐
- **TASK-008**: 更新VM设计文档与ABI一致

### ✅ Phase 2: 调用约定实现 (2025-12-25)
- **TASK-002**: 实现标准栈帧布局
- **TASK-006**: 实现帧指针完整支持
- **TASK-007**: 统一调用约定（调用栈+LR双重机制）

### ✅ Phase 3: 栈帧和参数传递 (2025-12-31)
- **TASK-004**: 完整支持被调用者保存寄存器（s0-s4）
- **TASK-005**: 迁移局部变量存储到堆内存
- **TASK-011**: 实现栈帧布局工具类
- **TASK-013**: 实现完整栈参数传递（第7+个参数）
- **TASK-014**: 修复递归函数实现

### ✅ Phase 4: 测试和验证 (2025-12-31)
- **TASK-009**: 编写ABI一致性测试套件（11个测试，100%通过）
- **TASK-010**: 重构汇编器支持自动序言/尾声生成（部分完成）

---

## 3. 进行中任务 (1个)

### 🔄 TASK-015: 集成自动序言/尾声生成到汇编器
- **状态**: 部分完成
- **预计工时**: 4小时
- **依赖**: CallingConventionUtils已实现，需要集成到VMAssembler

---

## 4. 计划中任务

### 🔴 VM适配高优先级任务（与EP21联动）

#### TASK-18R-VM-01: 寄存器分配器接口
- **预计工时**: 4小时
- **优先级**: 🔴 高
- **目标EP**: EP21
- **验收标准**:
  - 接口扩展完成（活跃区间查询、溢出代价计算）
  - Javadoc文档完整
  - 接口契约测试通过
- **关键文件**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/IRegisterAllocator.java`

#### TASK-18R-VM-02: 线性扫描寄存器分配器
- **预计工时**: 12小时
- **优先级**: 🔴 高
- **目标EP**: EP21
- **验收标准**:
  - 正确处理16个物理寄存器
  - 溢出指令占比 ≤15%
  - 算法复杂度 O(n log n)
- **关键文件**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/LinearScanAllocator.java`

#### TASK-18R-VM-03: EP18R代码生成器
- **预计工时**: 16小时
- **优先级**: 🔴 高
- **目标EP**: EP21
- **验收标准**:
  - 生成可执行的32位字节码
  - 支持完整的Cymbol语言
  - 通过集成测试
- **关键文件**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/pass/codegen/RegisterAssembler.java`

#### TASK-18R-VM-04: 32位字节码编码器
- **预计工时**: 8小时
- **优先级**: 高
- **目标EP**: EP21
- **验收标准**:
  - 42条指令全部支持
  - 编码正确性100%
  - 单元测试覆盖
- **关键文件**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/pass/codegen/ByteCodeEncoder.java`

#### TASK-18R-VM-05: EP18R调用约定实现
- **预计工时**: 8小时
- **优先级**: 中
- **目标EP**: EP21
- **验收标准**:
  - 与ABI规范100%一致
  - 提供清晰的API
  - 完整的测试覆盖
- **输出**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/RegisterCallingConvention.java`

#### TASK-18R-VM-06: EP18R→EP21适配器接口
- **预计工时**: 8小时
- **优先级**: 中
- **目标EP**: EP21
- **验收标准**:
  - 接口定义清晰
  - EP21可无缝调用
  - 集成测试通过
- **输出**: `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/pass/codegen/RegisterVMAdapter.java`

**VM适配任务总工时**: 56小时（~7天）

### 📝 文档完善任务

#### TASK-DOC-01: 完善VM设计文档
- **预计工时**: 4小时
- **优先级**: 中
- **内容**: 同步最新实现状态，更新接口说明

#### TASK-DOC-02: 创建代码生成器文档
- **预计工时**: 6小时
- **优先级**: 中
- **内容**: RegisterAssembler使用指南，寄存器分配策略说明

### ⏸️ 低优先级任务

#### TASK-PERF-01: 性能基准测试
- **预计工时**: 8小时
- **优先级**: 低
- **内容**: 测量标准序言/尾声开销，建立性能基准

#### TASK-015: 集成自动序言/尾声生成到汇编器
- **状态**: 部分完成
- **预计工时**: 4小时
- **优先级**: 中

---

## 5. 项目看板

| 任务ID | 描述 | 状态 | 测试数 | 完成日期 |
|--------|------|------|--------|----------|
| TASK-001 | 统一寄存器命名 | ✅ | - | 2025-12-20 |
| TASK-002 | 标准栈帧布局 | ✅ | - | 2025-12-25 |
| TASK-003 | 栈帧8字节对齐 | ✅ | - | 2025-12-22 |
| TASK-004 | 被调用者保存寄存器 | ✅ | - | 2025-12-28 |
| TASK-005 | 局部变量堆内存 | ✅ | - | 2025-12-30 |
| TASK-006 | 帧指针支持 | ✅ | - | 2025-12-23 |
| TASK-007 | 调用约定统一 | ✅ | - | 2025-12-21 |
| TASK-008 | VM文档更新 | ✅ | - | 2025-12-19 |
| TASK-009 | ABI测试套件 | ✅ | 11 | 2025-12-31 |
| TASK-010 | 自动序言/尾声 | 🔄 | - | 进行中 |
| TASK-011 | 栈帧工具类 | ✅ | - | 2025-12-27 |
| TASK-013 | 栈参数传递 | ✅ | - | 2025-12-31 |
| TASK-014 | 递归函数修复 | ✅ | - | 2025-12-29 |

**已完成**: 12/15任务（80%） + 6个VM适配任务待开始

---

## 6. 核心组件状态

| 组件 | 状态 | 测试数 | 说明 |
|------|------|--------|------|
| **RegisterVMInterpreter** | ✅ 完成 | 79 | 寄存器VM解释器 |
| **RegisterBytecodeDefinition** | ✅ 完成 | - | 42条指令定义 |
| **CallingConventionUtils** | ✅ 完成 | - | 调用约定工具 |
| **StackOffsets** | ✅ 完成 | - | 栈帧偏移计算 |
| **RegisterByteCodeAssembler** | ✅ 完成 | - | 汇编器 |
| **RegisterDisAssembler** | ✅ 完成 | - | 反汇编器 |
| **IRegisterAllocator** | ⏸️ 待实现 | - | 寄存器分配器接口 |
| **LinearScanAllocator** | ⏸️ 待实现 | - | 线性扫描分配器 |
| **RegisterAssembler** | ⏸️ 待实现 | - | 代码生成器 |
| **ByteCodeEncoder** | ⏸️ 待实现 | - | 字节码编码器 |

---

## 7. EP21-EP18R联动说明

### 7.1 接口契约

#### 契约1: IRegisterAllocator（EP18R定义）
```java
public interface IRegisterAllocator {
    int allocate(String varName);
    void free(String varName);
    int getRegister(String varName);
    Map<String, Integer> getAllocation();
}
```
- **定义位置**: EP18R
- **实现者**: EP18R (LinearScanAllocator, GraphColoringAllocator)
- **使用者**: EP21 RegisterVMGenerator (可选集成)

#### 契约2: IR格式（EP21定义）
- **定义**: LIR节点体系
- **传递方向**: EP21 → EP18R
- **验证**: 语义等价性测试

### 7.2 里程碑

| 里程碑 | 时间 | EP18R标志 | EP21标志 | 验收标准 |
|--------|------|-----------|----------|----------|
| M1 | Week 2 | VM-01,02完成 | CG-01,02完成 | 测试通过 |
| M2 | Week 3 | VM-03,04,05完成 | CG-03, TEST-01完成 | 接口可用 |
| M3 | Week 4 | VM-06完成 | TEST-02完成 | 适配器集成 |
| M4 | Week 5 | TEST-01,02完成 | DOC-01,02完成 | 文档同步 |

### 7.3 同步机制

- **双周同步会议**: 每两周周五
- **接口变更通知**: 接口变更需提前1周通知
- **集成测试触发**: 任一EP完成关键任务时运行完整集成测试

---

## 8. 成功标准

| 指标 | 目标值 | 当前值 | 状态 |
|------|--------|--------|------|
| **测试覆盖率** | ≥85% | 100% | ✅ |
| **ABI规范符合率** | 100% | 100% | ✅ |
| **VM适配任务完成率** | 100% | 0% | ⏸️ |
| **文档完整性** | 100% | 80% | 🔄 |

---

## 9. 相关资源

- **详细TDD计划**: [TDD重构计划.md](TDD重构计划.md)
- **核心设计文档**: [EP18R_核心设计文档.md](EP18R_核心设计文档.md)
- **ABI设计文档**: [EP18R_ABI_设计文档.md](EP18R_ABI_设计文档.md)
- **单元测试案例**: [单元测试案例集.md](单元测试案例集.md)
- **OpenSpecKit规范**: [OPENSPECKIT_SPECIFICATION.md](OPENSPECKIT_SPECIFICATION.md)

---

## 附录A: 快速命令参考

```bash
# 项目构建
mvn clean compile              # 编译EP18R
mvn test -pl ep18r             # 测试EP18R
mvn clean install            # 完整构建

# 测试
mvn test -Dtest=*RegisterVM*  # 运行RegisterVM测试
mvn test -Dtest=*ABI*         # 运行ABI测试

# 代码分析
Serena:find_symbol("ClassName", "ep18r")
Serena:search_for_pattern("pattern", "ep18r/src")
```

---

## 附录B: 版本历史

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2026-01-07 | 创建精简版TDD执行计划，参考EP21格式，添加EP21联动说明 |

---

**最后更新**: 2026-01-07
**维护者**: Claude Code
**状态**: ✅ 核心VM完成，⏸️ VM适配任务待执行
