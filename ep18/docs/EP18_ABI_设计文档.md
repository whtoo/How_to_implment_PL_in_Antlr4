# EP18 ABI设计文档（堆栈式虚拟机）

**文档版本**: v1.0
**创建日期**: 2025-12-19
**最后更新**: 2025-12-19
**制定者**: Claude Code
**基于**: JVM字节码规范, EP18 OpenSpecKit规范

---

## 1. 概述

本文档定义EP18堆栈式虚拟机的应用程序二进制接口（Application Binary Interface, ABI），为基于堆栈的执行模型制定完整的调用约定和运行时环境规范。ABI是编译器生成的代码与虚拟机运行时系统之间的契约，确保不同模块间的二进制兼容性。

### 1.1 设计目标
- **兼容性**: 与现有EP18代码保持向后兼容
- **清晰性**: 明确栈帧布局和参数传递规则
- **可调试性**: 提供标准化的栈帧布局，便于调试工具分析
- **可扩展性**: 支持未来功能扩展（如异常处理、反射）

### 1.2 适用范围
- 函数调用和返回机制
- 参数传递和返回值约定
- 栈帧布局和栈指针管理
- 局部变量和操作数栈管理
- 异常处理和栈展开

---

## 2. 运行时数据区域约定

EP18堆栈式虚拟机采用基于栈的执行模型，运行时数据区域包括：

### 2.1 操作数栈（Operand Stack）
- **用途**: 表达式求值、参数传递、临时结果存储
- **大小**: 可配置（默认1024个槽位）
- **槽位**: 每个槽位32位，可存储整数、浮点或引用
- **操作**: LIFO（后进先出）语义

### 2.2 局部变量区（Local Variables）
- **用途**: 存储方法的局部变量
- **组织**: 每个栈帧独立的局部变量数组
- **索引**: 从0开始的连续整数索引
- **类型**: 支持整数、浮点、引用类型

### 2.3 调用栈（Call Stack）
- **用途**: 存储活动栈帧，支持嵌套调用
- **深度**: 可配置（默认1024层）
- **栈帧**: 每个栈帧包含局部变量、操作数栈、返回地址

### 2.4 堆内存（Heap Memory）
- **用途**: 动态分配对象（结构体、数组）
- **管理**: 垃圾回收器管理
- **访问**: 通过引用（对象指针）访问

---

## 3. 函数调用约定

### 3.1 参数传递规则

#### 参数传递方式
EP18采用**栈传递**参数，所有参数通过操作数栈传递：

1. **调用者准备参数**:
   - 将参数从左到右压入操作数栈
   - 最后一个参数在栈顶，第一个参数在栈底
   - 示例：调用 `foo(1, 2, 3)`，栈状态：`[1, 2, 3]`（3在栈顶）

2. **执行CALL指令**:
   - 调用指令从操作数栈弹出参数数量
   - 创建新栈帧，将参数复制到局部变量区
   - 跳转到目标函数地址

3. **被调用者访问参数**:
   - 参数作为局部变量存储在局部变量区
   - 第一个参数在局部变量索引0，第二个在索引1，依此类推
   - 通过`load`指令加载参数值

#### 参数传递示例
```assembly
# 调用函数 foo(10, 20, 30)
iconst 10    # 压入第一个参数
iconst 20    # 压入第二个参数
iconst 30    # 压入第三个参数（栈顶）
call foo     # 调用函数，参数数量由函数定义决定

# 函数内部访问参数
.def foo: args=3, locals=2
    # 参数自动存储到局部变量 0,1,2
    load 0     # 加载第一个参数 (10)
    load 1     # 加载第二个参数 (20)
    load 2     # 加载第三个参数 (30)
    # ... 函数体
```

### 3.2 返回值约定

#### 返回值传递
- **单个返回值**: 通过操作数栈顶部返回
- **返回流程**:
  1. 被调用者将返回值压入操作数栈顶部
  2. 执行`ret`指令返回
  3. 调用者从操作数栈顶部获取返回值

#### 返回值示例
```assembly
# 被调用者返回结果
.def add: args=2, locals=0
    load 0     # 加载第一个参数
    load 1     # 加载第二个参数
    iadd       # 计算和，结果在栈顶
    ret        # 返回，栈顶值作为返回值

# 调用者使用返回值
iconst 10
iconst 20
call add      # 调用后，栈顶为30（返回值）
store 0       # 存储返回值到局部变量0
```

### 3.3 函数调用/返回指令语义

#### CALL指令语义
```assembly
call function_address
```
**操作**:
1. **保存返回地址**: 当前PC+1保存到调用栈
2. **创建新栈帧**:
   - 分配局部变量空间（参数+局部变量）
   - 将参数从操作数栈复制到局部变量区
   - 初始化操作数栈为空
3. **跳转**: `PC = function_address`

#### RET指令语义
```assembly
ret
```
**操作**:
1. **获取返回值**: 保存当前操作数栈顶值（如果有）
2. **恢复调用者栈帧**:
   - 释放当前栈帧
   - 恢复调用者的局部变量和操作数栈
3. **恢复返回值**: 将返回值压入调用者的操作数栈
4. **跳转回**: `PC = 返回地址`

---

## 4. 栈帧布局

### 4.1 栈帧结构

每个栈帧包含以下组件：

```
栈帧结构（栈增长方向：高地址 → 低地址）
┌─────────────────┐ 高地址
│   调用者栈帧     │
├─────────────────┤ ← 调用者栈帧结束
│   返回地址       │   （存储在调用栈中）
├─────────────────┤
│   动态链接       │   （指向调用者栈帧）
├─────────────────┤
│   局部变量区     │   locals[0..n-1]
│   ┌───────────┐ │
│   │ 参数m-1   │ │   ← 参数存储区
│   │   ...     │ │
│   │ 参数0     │ │
│   ├───────────┤ │
│   │ 局部变量k  │ │   ← 局部变量存储区
│   │   ...     │ │
│   │ 局部变量0  │ │
│   └───────────┘ │
├─────────────────┤
│   操作数栈       │   operandStack[0..s-1]
│   ┌───────────┐ │
│   │    ...    │ │
│   │ 栈顶元素   │ │ ← stackTop
│   │    ...    │ │
│   │ 栈底元素   │ │
│   └───────────┘ │
└─────────────────┘ 低地址 ← 当前栈帧基址
```

### 4.2 栈帧大小计算

```
栈帧大小 = 局部变量区 + 操作数栈区 + 固定开销

其中：
- 固定开销: 返回地址 + 动态链接 = 8字节
- 局部变量区: (num_args + num_locals) * 4字节
- 操作数栈区: max_stack * 4字节
- 对齐填充: 确保栈帧大小是8字节的倍数
```

### 4.3 局部变量布局

#### 参数和局部变量组织
```
局部变量数组布局：
索引  内容
0    参数0（第一个参数）
1    参数1（第二个参数）
...
n-1  参数n-1（最后一个参数）
n    局部变量0
n+1  局部变量1
...
n+m-1 局部变量m-1
```

#### 局部变量访问规则
- **参数访问**: 通过固定索引（0到n-1）
- **局部变量访问**: 通过偏移索引（n到n+m-1）
- **`this`指针**: 如果是实例方法，局部变量0存储`this`指针

### 4.4 栈对齐要求

- **栈指针对齐**: 栈帧基址必须8字节对齐
- **局部变量对齐**: 局部变量数组按4字节对齐
- **操作数栈对齐**: 操作数栈槽位按4字节对齐
- **对齐目的**: 确保内存访问性能和跨平台兼容性

---

## 5. 汇编语言接口

### 5.1 函数定义语法

```assembly
.def function_name: args=N, locals=M, stack=S
    # 函数体
```

**参数**:
- `args=N`: 函数参数数量
- `locals=M`: 局部变量数量（不包括参数）
- `stack=S`: 操作数栈最大深度

**示例**:
```assembly
.def add: args=2, locals=0, stack=2
    load 0     # 加载第一个参数
    load 1     # 加载第二个参数
    iadd       # 结果在栈顶
    ret        # 返回栈顶值
```

### 5.2 函数调用示例

#### 调用者代码
```assembly
# 调用函数 foo(10, 20, 30)
iconst 10          # 压入第一个参数
iconst 20          # 压入第二个参数
iconst 30          # 压入第三个参数（栈顶）
call foo           # 调用函数

# 使用返回值（在栈顶）
store 0            # 存储到局部变量0
```

#### 被调用者代码
```assembly
.def foo: args=3, locals=2, stack=4
    # 参数在局部变量0,1,2中
    # 局部变量3,4用于临时存储

    load 0          # 加载参数0
    load 1          # 加载参数1
    iadd            # 计算和
    store 3         # 存储到局部变量3

    load 2          # 加载参数2
    load 3          # 加载临时结果
    imul            # 相乘
    ret             # 返回结果
```

---

## 6. 异常和栈展开

### 6.1 异常处理约定

#### 异常栈帧
当异常发生时，需要保存以下上下文信息：
- **异常PC**: 发生异常的指令地址
- **异常类型**: 异常类引用
- **栈帧状态**: 当前栈帧的局部变量和操作数栈快照

#### 异常处理栈帧布局
```
异常处理栈帧：
+-------------------+
|   异常对象引用     |   （异常实例）
+-------------------+
|   异常PC          |   （发生异常的地址）
+-------------------+
|   栈帧快照        |   （局部变量和操作数栈状态）
+-------------------+
|   处理程序地址     |   （异常处理代码入口）
+-------------------+
```

### 6.2 栈展开机制

#### 栈展开过程
1. **查找异常处理程序**: 从当前栈帧开始，向上搜索异常处理表
2. **恢复栈帧**: 逐个弹出栈帧，直到找到处理程序
3. **跳转到处理程序**: 设置PC为处理程序地址
4. **清理资源**: 释放未使用的栈帧

#### 异常处理表示例
```assembly
.exception_table
    start: 0x100, end: 0x200, handler: 0x300, type: "NullPointerException"
    start: 0x150, end: 0x180, handler: 0x350, type: "DivisionByZeroException"
```

---

## 7. 数据表示和对齐

### 7.1 基本数据类型

| 数据类型 | 大小（字节） | 对齐要求 | 栈表示 |
|----------|--------------|----------|--------|
| boolean  | 1            | 1        | 零扩展至32位 |
| char     | 2            | 2        | 零扩展至32位 |
| byte     | 1            | 1        | 符号扩展至32位 |
| short    | 2            | 2        | 符号扩展至32位 |
| int      | 4            | 4        | 直接使用    |
| float    | 4            | 4        | IEEE 754   |
| reference | 4          | 4        | 对象指针    |

### 7.2 结构体和数组对齐

#### 结构体对齐规则
1. **整体对齐**: 结构体大小必须是其最大成员对齐要求的倍数
2. **成员对齐**: 每个成员必须放置在其对齐要求的倍数地址上
3. **填充字节**: 编译器在成员间插入填充字节以满足对齐要求

#### 数组对齐规则
- **元素对齐**: 数组元素按类型对齐要求对齐
- **整体对齐**: 数组起始地址按元素对齐要求对齐
- **长度存储**: 数组对象包含长度字段（4字节）

### 7.3 堆分配对象对齐
- **基本对齐**: 所有堆分配对象至少8字节对齐
- **对象头**: 每个对象包含对象头（类型指针、GC信息）
- **字段偏移**: 字段偏移计算考虑继承和填充

---

## 8. 与现有实现的兼容性

### 8.1 向后兼容性保证

EP18 ABI设计保持与以下现有特性的兼容性：

#### 栈帧兼容性
- **现有特性**: 使用`StackFrame`类管理调用栈
- **ABI扩展**: 在现有基础上添加标准化栈帧布局
- **兼容性**: 现有代码继续工作，新代码获得更好的调试支持

#### 参数传递兼容性
- **现有特性**: 参数通过操作数栈传递
- **ABI确认**: 正式确认栈传递参数规范
- **扩展**: 添加参数到局部变量的自动复制

#### 返回值兼容性
- **现有特性**: 返回值通过操作数栈顶部返回
- **ABI规定**: 正式确认栈顶返回值约定
- **扩展**: 添加返回值保存/恢复机制

### 8.2 迁移路径

#### 阶段1：ABI文档和工具支持
1. 编写本ABI文档
2. 创建栈帧布局辅助工具
3. 更新汇编器支持自动栈帧生成

#### 阶段2：逐步采用
1. 新编写的函数遵循ABI规范
2. 现有函数在修改时迁移到ABI规范
3. 测试工具验证ABI一致性

#### 阶段3：全面采用
1. 所有系统库函数遵循ABI
2. 编译器默认生成ABI兼容代码
3. 调试工具依赖ABI栈帧信息

---

## 9. 实现工具和支持

### 9.1 栈帧布局工具

#### 栈帧计算器
```java
public class StackFrameCalculator {
    /**
     * 计算栈帧布局信息
     * @param numArgs 参数数量
     * @param numLocals 局部变量数量
     * @param maxStack 最大操作数栈深度
     * @return 栈帧布局信息
     */
    public static StackFrameLayout calculate(
        int numArgs,
        int numLocals,
        int maxStack
    ) {
        // 实现栈帧偏移计算
        int localVarSize = (numArgs + numLocals) * 4;
        int operandStackSize = maxStack * 4;
        int fixedOverhead = 8; // 返回地址 + 动态链接
        int totalSize = localVarSize + operandStackSize + fixedOverhead;

        // 8字节对齐
        totalSize = (totalSize + 7) & ~7;

        return new StackFrameLayout(totalSize, localVarSize, operandStackSize);
    }
}
```

#### 偏移量定义
```java
public class StackOffsets {
    // 固定开销偏移（相对于栈帧基址）
    public static final int RETURN_ADDRESS_OFFSET = 0;
    public static final int DYNAMIC_LINK_OFFSET = 4;

    // 局部变量区偏移
    public static final int LOCAL_VARS_OFFSET = 8;

    // 操作数栈区偏移
    public static final int OPERAND_STACK_OFFSET(int localVarSize) {
        return LOCAL_VARS_OFFSET + localVarSize;
    }

    // 计算局部变量偏移
    public static int localVarOffset(int index) {
        return LOCAL_VARS_OFFSET + index * 4;
    }

    // 计算操作数栈槽位偏移
    public static int stackSlotOffset(int slotIndex, int localVarSize) {
        return OPERAND_STACK_OFFSET(localVarSize) + slotIndex * 4;
    }
}
```

### 9.2 汇编器ABI支持

#### 自动栈帧生成
汇编器在遇到函数定义时自动生成标准栈帧代码：
```assembly
.def my_func: args=2, locals=3, stack=4
    # 自动生成的栈帧代码
    .frame locals=5, stack=4   # 总局部变量=args+locals
    # 函数体
```

#### 参数复制代码生成
汇编器自动生成参数复制代码：
```assembly
# 调用者代码
iconst 10
iconst 20
call my_func

# 被调用者入口（自动生成）
.def my_func: args=2, locals=3, stack=4
    # 自动参数复制（伪代码）
    # param0 → local0
    # param1 → local1
    # 继续执行用户代码
```

### 9.3 调试信息格式

#### 栈帧调试信息
```java
public class StackFrameDebugInfo {
    private final String functionName;
    private final int frameSize;
    private final Map<String, Integer> localVarOffsets; // 变量名→偏移
    private final Map<String, Integer> paramOffsets;    // 参数名→偏移

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Function: ").append(functionName).append("\n");
        sb.append("Frame size: ").append(frameSize).append(" bytes\n");
        sb.append("Parameters:\n");
        for (Map.Entry<String, Integer> entry : paramOffsets.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": offset ")
              .append(entry.getValue()).append("\n");
        }
        sb.append("Local variables:\n");
        for (Map.Entry<String, Integer> entry : localVarOffsets.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": offset ")
              .append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
```

---

## 10. 测试和验证

### 10.1 ABI一致性测试

#### 测试套件目标
1. **参数传递测试**: 验证栈传递参数正确性
2. **返回值测试**: 验证栈顶返回值约定
3. **栈帧布局测试**: 验证栈帧偏移计算正确性
4. **局部变量访问测试**: 验证局部变量索引访问
5. **对齐测试**: 验证栈和数据对齐要求

#### 测试示例：参数传递
```java
@Test
void testParameterPassing() {
    // 测试参数通过栈正确传递
    String program = """
        .def caller: args=0, locals=0, stack=3
            iconst 100
            iconst 200
            iconst 300
            call callee
            # 验证返回值
            store 0
            halt

        .def callee: args=3, locals=0, stack=3
            # 参数应在局部变量0,1,2中
            load 0
            load 1
            iadd
            load 2
            iadd
            ret  # 返回 100+200+300=600
        """;

    loadAndExecute(program);
    assertThat(interpreter.getLocalVariable(0)).isEqualTo(600);
}
```

#### 测试示例：栈帧布局
```java
@Test
void testStackFrameLayout() {
    // 测试栈帧大小计算正确性
    String program = """
        .def test: args=2, locals=3, stack=4
            # 函数使用2个参数，3个局部变量，最大栈深度4
            # 总局部变量 = 2+3 = 5
            # 局部变量区大小 = 5*4 = 20字节
            # 操作数栈大小 = 4*4 = 16字节
            # 固定开销 = 8字节
            # 总大小 = 20+16+8 = 44字节
            # 对齐后 = 48字节 (8字节对齐)
            ret
        """;

    loadAndExecute(program);
    // 验证栈帧大小计算
    assertThat(interpreter.getFrameSize("test")).isEqualTo(48);
}
```

### 10.2 性能基准测试

#### 测试指标
1. **函数调用开销**: 测量栈帧创建/销毁的开销
2. **参数传递效率**: 测量栈传递参数的性能
3. **局部变量访问**: 测量局部变量访问速度
4. **栈操作性能**: 测量push/pop操作性能

#### 基准测试工具
```java
public class ABIBenchmark {
    // 测量函数调用开销
    public long measureCallOverhead(int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            // 调用空函数
            callEmptyFunction();
        }
        long end = System.nanoTime();
        return (end - start) / iterations;
    }

    // 测量参数传递开销
    public long measureArgPassing(int numArgs, int iterations) {
        // 测试不同数量参数的传递开销
        String program = generateTestProgram(numArgs);
        return measureExecutionTime(program, iterations);
    }
}
```

---

## 附录A：ABI版本历史

| 版本 | 日期 | 主要变更 | 兼容性 |
|------|------|----------|--------|
| v1.0 | 2025-12-19 | 初始版本，基于堆栈式虚拟机设计 | 与EP18现有代码兼容 |

## 附录B：参考文档

1. **JVM字节码规范**: https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-2.html
2. **EP18 OpenSpecKit规范**: EP18_OpenSpecKit_Specification.md
3. **EP18核心设计文档**: EP18_核心设计文档.md
4. **Cymbol语言规范**: ../../docs/Cymbol语言规范.md

## 附录C：快速参考表

### 栈帧布局快速参考
```
栈帧基址+0:   返回地址
栈帧基址+4:   动态链接
栈帧基址+8:   局部变量0 (参数0)
栈帧基址+12:  局部变量1 (参数1)
...
栈帧基址+8+4n: 局部变量n
栈帧基址+8+4*(args+locals): 操作数栈底
```

### 常用指令序列
```assembly
# 函数调用模板
# 准备参数（从左到右）
iconst arg1
iconst arg2
...
call function

# 函数定义模板
.def function: args=N, locals=M, stack=S
    # 参数在局部变量0..N-1
    # 局部变量在局部变量N..N+M-1
    # 操作数栈深度不超过S
    ret

# 返回值处理
# 被调用者：将返回值压入栈顶
# 调用者：从栈顶获取返回值
```

### 对齐计算
```
栈帧大小对齐：
frame_size = (local_vars_size + operand_stack_size + 8 + 7) & ~7

其中：
local_vars_size = (num_args + num_locals) * 4
operand_stack_size = max_stack * 4
```

---

## 附录D：当前实现与ABI规范的差异

本文档定义的是EP18堆栈虚拟机的目标ABI规范。当前实现与目标规范存在一些差异，这些差异将在未来的版本中逐步消除。

### D.1 栈帧布局差异

| 项目 | 目标ABI规范 | 当前实现 | 说明 |
|------|-------------|----------|------|
| 栈帧结构 | 标准栈帧布局（第4.1节） | 简化栈帧（`StackFrame`类） | 当前实现使用简化的栈帧结构 |
| 动态链接 | 包含动态链接字段 | 未实现 | 动态链接支持尚未实现 |
| 对齐要求 | 8字节对齐 | 未强制对齐 | 对齐要求尚未实现 |

### D.2 参数传递差异

| 项目 | 目标ABI规范 | 当前实现 | 说明 |
|------|-------------|----------|------|
| 参数复制 | 自动复制到局部变量 | 手动load/store | 当前需要手动加载参数 |
| 参数数量 | 通过函数定义指定 | 通过操作数推断 | 参数数量由操作数栈推断 |

### D.3 兼容性说明

当前实现保持与现有EP18代码的完全兼容性。新代码应尽可能遵循目标ABI规范，但需要注意上述差异。

**迁移建议**：
1. 参数传递：使用栈传递参数（与规范一致）
2. 返回值：使用栈顶返回值（与规范一致）
3. 局部变量：使用标准化局部变量布局
4. 栈帧管理：使用`StackFrameCalculator`工具类

**未来兼容性**：
随着ABI规范的逐步实现，这些差异将逐步消除。代码应做好迁移准备，特别是：
- 避免硬编码栈帧偏移，使用`StackOffsets`工具类
- 使用标准函数定义语法
- 遵循第8.2节的迁移路径

---

**文档状态**: 正式发布（目标规范）
**当前实现状态**: 与目标规范存在差异，正在逐步迁移
**下一步**:
1. 实现栈帧布局辅助工具（第9.1节）
2. 更新汇编器支持自动栈帧生成（第9.2节）
3. 编写ABI一致性测试套件（第10节）
4. 逐步消除当前实现与ABI规范的差异

**已完成的ABI迁移步骤**:
- 统一参数传递为栈传递
- 统一返回值为栈顶返回
- 建立标准化栈帧布局概念

---

**维护要求**: 任何ABI设计变更必须同步更新OpenSpecKit规范，确保两者一致。