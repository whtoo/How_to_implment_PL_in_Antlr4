# EP18 - Cymbol 虚拟机与垃圾回收系统

## 🤖 AI Agent 快速指南

### 🎯 EP 概述
- **主题**: 栈式虚拟机、引用计数垃圾回收、字节码汇编器、结构体统一实现、全局变量支持
- **目标**: 实现完整的编译器后端执行引擎，支持字节码解释执行和内存管理
- **在编译器流水线中的位置**: 后端执行引擎
- **依赖关系**: 
  - 内部依赖: 无
  - 外部依赖: ANTLR4 4.13.2, Mockito (测试), JMH (性能测试)

### 📁 项目结构
```
ep18/
├── src/main/java/org/teachfx/antlr4/ep18/
│   ├── stackvm/          # 虚拟机引擎 (CymbolStackVM, VMInterpreter, ByteCodeAssembler)
│   ├── gc/              # 垃圾回收系统 (ReferenceCountingGC, GCObjectHeader, GCStats)
│   ├── symtab/          # 类型系统 (StructType, StructSymbol, Scope)
│   └── parser/          # ANTLR4 生成的 VM 汇编解析器
├── src/main/antlr4/VMAssembler.g4   # VM 汇编语法定义
├── docs/                # 详细设计文档
└── examples/gc/         # GC 示例程序
```

### 🏗️ 核心组件
- **虚拟机引擎**: CymbolStackVM (主引擎), VMInterpreter (字节码解释器), ByteCodeAssembler (字节码汇编器)
- **垃圾回收系统**: ReferenceCountingGC (引用计数 GC), GCObjectHeader (GC 对象头), GCStats (统计信息)
- **类型系统**: StructValue (统一结构体运行时表示), StructType (结构体类型定义)
- **配置和异常**: VMConfig (配置管理), VMException 及其子类 (异常处理)

### 🔧 构建与测试
```bash
# 进入 EP18 目录
cd ep18

# 构建项目
mvn clean compile

# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=CymbolStackVMTest
mvn test -Dtest=VMInterpreterTest
mvn test -Dtest=GarbageCollectorTest

# 运行性能基准测试
mvn test -Dtest=PerformanceBenchmark

# 运行单个测试方法
mvn test -Dtest=CymbolStackVMTest#testAddition
```

### 🚀 常用操作
#### 编译运行示例
```bash
# 运行 VM 汇编代码
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMRunner"

# 使用 VMInterpreter 执行 VM 汇编文件
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMInterpreter" -Dexec.args="program.vm"

# 汇编 VM 汇编代码为字节码
java -cp target/classes org.teachfx.antlr4.ep18.stackvm.ByteCodeAssembler input.vm output.cvm
```

### 📝 关键注意事项
1. **指令集操作数顺序已修复**: ILT、IGT、ILE、IGE 和 FLT 指令的操作数比较顺序已修复
2. **结构体统一实现**: 使用新的 StructValue 替代旧的 StructSpace 和 int[] 堆表示
3. **全局变量支持**: 新增 `.global` 指令用于声明全局变量，使用 GLOAD/GSTORE 访问
4. **异常处理**: 支持 VMDivisionByZeroException、VMOverflowException、VMStackOverflowException
5. **GC 统计**: GCStats 提供详细的垃圾回收统计信息

### 🔍 调试技巧
1. **启用详细日志**: 使用 `VMConfig.setVerbose(true)` 启用详细执行日志
2. **性能监控**: 使用 `VMStats` 监控指令执行统计和内存使用
3. **GC 分析**: 使用 `GCStats` 分析垃圾回收性能
4. **断点调试**: VMInterpreter 支持设置断点和单步执行

---

## 概述

EP18 实现了一个功能完整的 **Cymbol 虚拟机 (CymbolStackVM)**，包含**引用计数垃圾回收器**、**结构体统一实现**、**全局变量支持**和**性能监控**等高级特性。该模块是编译器后端执行引擎，支持字节码解释执行、内存管理和运行时优化。

## 🎉 最近重大更新 (2025年12月)

### 🚀 核心功能重大改进

#### 1. 统一结构体实现 ✅
- **StructValue 引入** - 新的统一结构体运行时表示，替代原有的 StructSpace 和 int[] 堆表示
- **类型安全增强** - 新增 StructType、StructSymbol 等类型系统组件
- **字段访问优化** - 增强对结构体字段访问的运行时类型转换支持（int ↔ Integer）
- **ID 映射机制** - 实现结构体 ID 映射以兼容旧版堆地址引用方式
- **兼容性保证** - 修改 VMInterpreter 与 CymbolStackVM 以适配新的 StructValue，保持指令兼容性

#### 2. 虚拟机核心功能完善 ✅
- **指令集扩展** - 支持函数调用、浮点运算及相关控制流指令
- **调试支持** - 新增断点设置与单步执行模式
- **栈管理** - 实现局部变量存储与调用栈管理机制
- **异常处理** - 增强除零检查和溢出检测

#### 3. 垃圾回收系统 ✅
- **引用计数 GC** - 实现完整的引用计数垃圾回收器
- **GC 组件** - GCObjectHeader、GCStats 等配套组件
- **性能监控** - 集成执行时间和内存使用追踪
- **统计功能** - 提供详细的 GC 统计信息

#### 4. 全局变量支持 ✅
- **`.global` 指令** - 新增指令用于声明全局变量
- **词法语法更新** - 更新 VMAssembler 语法以支持全局变量
- **权限配置** - 扩展运行时权限配置
- **错误处理** - 修复潜在的数组越界问题

#### 5. 重大 Bug 修复 ✅
- **比较操作修复** - 修复 VMInterpreter 中 ILT、IGT、ILE、IGE 和 FLT 指令的操作数比较顺序
- **循环执行修复** - 解决了循环无法执行的根本问题
- **局部变量初始化** - 正确初始化局部变量为 0
- **除零检查** - 增强 IDIV 除零检查机制

#### 6. ANTLR4 升级 ✅
- **版本升级** - ANTLR 4.11.0 → 4.13.2
- **代码生成** - 更新 VMAssembler 语法解析器和词法分析器
- **Java 兼容性** - 适配新版 Java 编译要求

## 架构概览

### 核心组件

#### 虚拟机引擎
- **CymbolStackVM** - 栈式虚拟机主引擎，支持全部基础指令
- **VMInterpreter** - 字节码解释器，执行虚拟机指令
- **ByteCodeAssembler** - 字节码汇编器，将 VM 汇编代码转换为字节码
- **BytecodeDefinition** - 字节码指令定义

#### 垃圾回收系统
- **ReferenceCountingGC** - 引用计数垃圾回收器实现
- **GCObjectHeader** - GC 对象头，包含引用计数信息
- **GCStats** - GC 统计信息收集器
- **GarbageCollector** - GC 接口定义

#### 类型系统
- **StructValue** - 统一的结构体运行时表示
- **StructSpace** - 结构体存储空间（兼容层）
- **StructType** - 结构体类型定义
- **StructSymbol** - 结构体符号

#### 配置和异常
- **VMConfig** - 虚拟机配置管理
- **VMException** - 虚拟机异常基类
- **VMDivisionByZeroException** - 除零异常
- **VMOverflowException** - 溢出异常
- **VMStackOverflowException** - 栈溢出异常

#### 性能监控
- **VMStats** - 虚拟机执行统计
- **PerformanceBenchmark** - 性能基准测试

## 功能特性详解

### ✅ 已实现功能

#### 1. 指令集系统
支持完整的虚拟机指令集：

**算术指令**
- `IADD`, `ISUB`, `IMUL`, `IDIV` - 整数运算
- `FADD`, `FSUB`, `FMUL`, `FDIV` - 浮点运算
- `INEG`, `FNEG` - 取负

**比较指令**
- `ILT`, `IGT`, `ILE`, `IGE` - 整数比较
- `FLT`, `FGT`, `FLE`, `FGE` - 浮点比较
- `IEQ`, `INEQ` - 相等比较

**逻辑指令**
- `IAND`, `IOR`, `IXOR` - 位运算
- `I2F`, `F2I` - 类型转换

**控制流指令**
- `IFEQ`, `IFNEQ`, `IFLT` 等 - 条件跳转
- `GOTO` - 无条件跳转
- `CALL`, `RET` - 函数调用和返回
- `HALT` - 虚拟机停止

**栈操作指令**
- `ICONST`, `FCONST` - 常量压栈
- `ILOAD`, `FLOAD` - 变量加载
- `ISTORE`, `FSTORE` - 变量存储
- `POP`, `DUP` - 栈操作

**数组和结构体指令**
- `IALOAD`, `IASTORE` - 数组访问
- `NEWARRAY` - 数组创建
- `NEWSTRUCT` - 结构体创建
- `STRUCTGET`, `STRUCTSET` - 结构体字段访问

**全局变量指令**
- `.global` - 全局变量声明
- `GLOAD`, `GSTORE` - 全局变量访问

#### 2. 垃圾回收机制

**引用计数算法**
- 每个对象维护引用计数
- 计数为 0 时自动回收
- 循环引用检测和处理

**GC 统计信息**
- 分配对象数量
- 回收对象数量
- 内存使用情况
- GC 频率和耗时

#### 3. 结构体系统

**统一表示**
```java
public class StructValue {
    private final int id;
    private final Map<String, Object> fields;
    private int referenceCount;
}
```

**字段访问**
```cymbol
struct Point {
    int x;
    int y;
}

void main() {
    Point p;
    p.x = 10;
    p.y = 20;
}
```

#### 4. 异常处理

**异常类型**
- 除零异常 (`VMDivisionByZeroException`)
- 溢出异常 (`VMOverflowException`)
- 栈溢出异常 (`VMStackOverflowException`)
- 通用虚拟机异常 (`VMException`)

#### 5. 性能监控

**统计指标**
- 指令执行次数
- 内存分配/回收
- 执行时间
- GC 性能数据

## 使用示例

### 构建项目
```bash
cd ep18
mvn clean compile
```

### 运行虚拟机
```bash
# 运行 VM 汇编代码
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMRunner"

# 使用 VMInterpreter
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMInterpreter" -Dexec.args="program.vm"
```

### 字节码汇编
```bash
# 汇编 VM 汇编代码为字节码
java -cp target/classes org.teachfx.antlr4.ep18.stackvm.ByteCodeAssembler input.vm output.cvm
```

### 运行示例程序

**算术运算示例** (`c.vm`)
```vm
iconst_5
iconst_3
iadd
print
halt
```

**函数调用示例** (`t.vm`)
```vm
iconst_5
iconst_7
call add
print
halt

add:
    load 0
    load 1
    iadd
    ret
```

**全局变量示例**
```vm
.global counter
iconst_0
gstore counter
gload counter
iconst_1
iadd
gstore counter
gload counter
print
halt
```

**结构体示例**
```vm
newstruct Point
dup
iconst_0
iconst_10
structset x
dup
iconst_1
iconst_20
structset y
structget x
print
halt
```

### 测试执行
```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=CymbolStackVMTest
mvn test -Dtest=VMInterpreterTest
mvn test -Dtest=GarbageCollectorTest

# 运行性能测试
mvn test -Dtest=PerformanceBenchmark
```

## 测试和验证

### 测试套件概览
| 测试文件 | 状态 | 说明 |
|---------|------|------|
| CymbolStackVMTest | ✅ 通过 | 核心虚拟机功能测试 |
| VMInterpreterTest | ✅ 通过 | 字节码解释器测试 |
| GarbageCollectorTest | ✅ 通过 | 垃圾回收器测试 |
| PerformanceBenchmark | ✅ 通过 | 性能基准测试 |

### 测试场景
- **算术运算** - 验证所有算术指令正确性
- **比较操作** - 验证比较指令（特别是修复后的 ILT/IGT 等）
- **函数调用** - 验证 CALL/RET 指令
- **循环执行** - 验证 while 循环等控制流
- **全局变量** - 验证 .global 指令和 GLOAD/GSTORE
- **结构体操作** - 验证 StructValue 的创建和字段访问
- **垃圾回收** - 验证引用计数机制
- **异常处理** - 验证除零、溢出等异常

## 性能特性

### 性能指标
- **指令执行速度** - 优化的字节码解释执行
- **内存效率** - 引用计数 GC 实现零停顿回收
- **栈操作效率** - 高效的栈帧管理
- **结构体访问** - 优化的字段访问机制

### 性能优化
- **常量折叠** - 编译时计算常量表达式
- **栈缓存** - 减少栈操作开销
- **内联缓存** - 优化函数调用
- **GC 优化** - 批量回收减少停顿

## 技术文档

### 重要文档位置
- `docs/struct-unification-plan.md` - 结构体统一实现技术规划
- `docs/垃圾回收机制实现.md` - 垃圾回收详细实现
- `docs/字节码解释器实现方案.md` - 字节码解释器设计
- `docs/优化实施报告.md` - 性能优化报告

### Bug 修复记录
- `BUG_FIX_SUMMARY.md` - Bug 修复总结
- `VM_INTERPRETER_BUG_ANALYSIS.md` - VM 解释器 Bug 分析

## 虚拟机指令参考

### 完整指令集

| 指令 | 描述 | 示例 |
|------|------|------|
| `iconst_n` | 压入整数常量 | `iconst_5` |
| `fconst_n` | 压入浮点常量 | `fconst_1` |
| `iload n` | 加载局部变量 | `iload 0` |
| `istore n` | 存储局部变量 | `istore 1` |
| `iadd` | 整数加法 | `iadd` |
| `isub` | 整数减法 | `isub` |
| `imul` | 整数乘法 | `imul` |
| `idiv` | 整数除法 | `idiv` |
| `ilt` | 整数小于比较 | `ilt` |
| `igt` | 整数大于比较 | `igt` |
| `ifeq label` | 等于跳转 | `ifeq loop` |
| `goto label` | 无条件跳转 | `goto start` |
| `call label` | 函数调用 | `call add` |
| `ret` | 函数返回 | `ret` |
| `.global` | 声明全局变量 | `.global counter` |
| `gload name` | 加载全局变量 | `gload counter` |
| `gstore name` | 存储全局变量 | `gstore counter` |
| `newstruct` | 创建结构体 | `newstruct Point` |
| `structget n` | 获取结构体字段 | `structget 0` |
| `structset n` | 设置结构体字段 | `structset 1` |
| `print` | 打印栈顶值 | `print` |
| `halt` | 停止虚拟机 | `halt` |

## 开发指南

### 添加新指令
1. 在 `BytecodeDefinition.java` 中定义指令
2. 在 `VMInterpreter.java` 中实现解释逻辑
3. 更新 `ByteCodeAssembler.java` 支持汇编
4. 添加测试用例

### 调试技巧
- 使用 `VMConfig` 启用详细日志
- 使用 `VMStats` 监控执行统计
- 使用 `GCStats` 监控 GC 性能
- 使用断点和单步执行

### 性能调优
- 调整 GC 参数
- 优化栈帧大小
- 使用常量池
- 减少函数调用开销

## 未来改进方向

### 短期目标
1. **JIT 编译** - 实现即时编译提升性能
2. **优化器** - 添加字节码优化 Pass
3. **调试器** - 增强调试功能
4. **异常处理** - 完善异常处理机制

### 长期目标
1. **SSA 形式** - 转换为 SSA 形式优化
2. **寄存器分配** - 实现寄存器分配算法
3. **并行 GC** - 支持并行垃圾回收
4. **AOT 编译** - 支持预先编译

## 依赖和版本

### 主要依赖
- **ANTLR4** - 4.13.2
- **Java** - 18+
- **Maven** - 3.8+
- **JUnit** - 5.x
- **AssertJ** - 测试框架

### 生成的代码
- `VMAssemblerLexer.java` - VM 汇编词法分析器
- `VMAssemblerParser.java` - VM 汇编语法分析器

## 贡献指南

欢迎贡献虚拟机相关改进：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/new-vm-feature`)
3. 遵循代码规范
4. 添加完整的测试用例
5. 确保所有测试通过
6. 提交 Pull Request

## 许可证

本项目遵循项目根目录的许可证条款。
