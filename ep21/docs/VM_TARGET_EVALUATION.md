# EP21 目标虚拟机编译可行性评估报告

**文档版本**: v1.0
**创建日期**: 2025-12-23
**评估范围**: EP21 优化编译器 → EP18 (栈式VM) / EP18R (寄存器VM)

---

## 目录

1. [编译合理性基础](#1-编译合理性基础)
2. [EP18 与 EP18R 差异分析](#2-ep18-与-ep18r-差异分析)
3. [VM 适配器架构设计](#3-vm-适配器架构设计)
4. [可行性评估总结](#4-可行性评估总结)
5. [实现建议](#5-实现建议)

---

## 1. 编译合理性基础

### 1.1 语义保持原则

编译正确性的核心是**语义保持**（Semantic Preservation），即源程序和目标程序必须产生相同的计算结果。

```
源程序 S
    |
    | 编译 (Compilation)
    v
中间表示 IR (EP21 LIR)
    |
    | 代码生成 (Code Generation)
    v
目标程序 T
    |
    | 执行 (Execution)
    v
执行结果: result(S) = result(T)
```

### 1.2 EP21 编译流程

```
源代码 (.cy)
    |
    v
+-----------+
| AST构建    | EP3
+-----------+
    |
    v
+-----------+
| MIR构建    | EP11
+-----------+
    |
    v
+-----------+
| CFG构建    | EP17
+-----------+
    |
    v
+-----------+
| 优化Pass   | EP21
| - 常量折叠  |
| - CSE      |
| - DCE      |
+-----------+
    |
    v
+-----------+
| LIR输出    | EP21
+-----------+
    |
    v
+-------------------+
| 代码生成器        |
| - CymbolAssembler | → EP18
| - RegisterAssembler| → EP18R (待实现)
+-------------------+
    |
    v
+-----------+
| 字节码    |
+-----------+
    |
    v
+-----------+
| VM执行    |
+-----------+
```

### 1.3 语义等效性保证机制

| 层级 | 保证机制 | 适用范围 |
|------|----------|----------|
| **IR层** | SSA形式 + 数据流分析 | 优化正确性 |
| **指令映射** | 一对一/一对多转换规则 | 代码生成 |
| **调用约定** | 参数传递/返回值位置统一 | 函数调用 |
| **内存模型** | 栈帧布局兼容 | 局部变量访问 |

---

## 2. EP18 与 EP18R 差异分析

### 2.1 架构对比概览

| 特性 | EP18 (栈式VM) | EP18R (寄存器VM) |
|------|---------------|------------------|
| **架构类型** | 栈式 (Stack) | 寄存器 (Register) |
| **指令格式** | 可变长 (1+4n bytes) | 固定32位 |
| **操作数来源** | 操作数栈 | 寄存器/立即数 |
| **寄存器数量** | 0 (纯栈) | 16 (R0-R15) |
| **指令数量** | 42 | 42 |
| **ABI规范** | 自定义 | ABI定义 |

### 2.2 字节码格式差异

#### EP18 栈式VM字节码

```
指令格式: [opcode][operand1][operand2]...
- opcode: 1字节 (1-42)
- operand: 4字节有符号整数

示例: a = b + c
    load 1      ; 加载b到栈顶 (opcode=32, operand=1)
    load 2      ; 加载c到栈顶 (opcode=32, operand=2)
    iadd        ; 弹出两个int，相加，结果压栈 (opcode=1)
    store 0     ; 弹出结果到a (opcode=35, operand=0)
```

#### EP18R 寄存器VM字节码

```
指令格式: 32位固定长度 (大端序)
- R-type: [opcode:6][rd:5][rs1:5][rs2:5][未用:11]
- I-type: [opcode:6][rd:5][rs1:5][imm:16]
- J-type: [opcode:6][imm:26]

示例: a = b + c (假设a=r2, b=r3, c=r4)
    lw r3, fp, 4     ; 从栈加载b到r3
    lw r4, fp, 8     ; 从栈加载c到r4
    add r2, r3, r4   ; r2 = r3 + r4
    sw r2, fp, 0     ; 存储a到栈
```

### 2.3 调用约定差异

#### EP18 调用约定

```
返回值位置: 栈顶 (stack_top)
参数传递: 全部压栈
调用者保存: 无 (纯栈VM)
被调用者保存: 无
```

#### EP18R 调用约定

```
返回值位置: R2 (a0)
参数传递: R2-R7 (a0-a5) 前6个参数，第7+个参数压栈
调用者保存: R1(ra), R2-R7(a0-a5), R15(lr)
被调用者保存: R8-R12(s0-s4)
特殊寄存器: R0(zero), R13(sp), R14(fp)
```

#### 调用约定映射表

| 项目 | EP18 | EP18R |
|------|------|-------|
| 返回值 | 栈顶 | R2 (a0) |
| 参数1 | 栈[fp+8] | R2 (a0) |
| 参数2 | 栈[fp+12] | R3 (a1) |
| 参数3 | 栈[fp+16] | R4 (a2) |
| 参数4 | 栈[fp+20] | R5 (a3) |
| 参数5 | 栈[fp+24] | R6 (a4) |
| 参数6 | 栈[fp+28] | R7 (a5) |
| 返回地址 | 调用栈 | R1 (ra) |

### 2.4 执行模型差异

#### EP18 执行模型

```
指令执行周期:
1. 取指 (PC -> opcode)
2. 解码 (读取操作数)
3. 执行 (操作数栈 -> ALU -> 操作数栈)

操作数栈状态:
    +---+
    |   |  <-- SP (栈顶)
    +---+
    | b |  <-- 第二操作数
    +---+
    | a |  <-- 第三操作数 (更早压入)
    +---+
    ...
```

#### EP18R 执行模型

```
指令执行周期:
1. 取指 (PC -> 32位指令)
2. 解码 (提取rd, rs1, rs2/imm)
3. 执行 (寄存器 -> ALU -> 寄存器)

寄存器文件:
    +---+---+
    |R0 | 0 |  (always zero)
    +---+---+
    |R1 |   |  ra
    +---+---+
    |R2 |   |  a0 / 返回值
    +---+---+
    |R3 |   |  a1
    +---+---+
    ...      ...
    +---+---+
    |R13|   |  sp
    +---+---+
    |R14|   |  fp
    +---+---+
    |R15|   |  lr
    +---+---+
```

### 2.5 指令集映射

| 运算类型 | EP18 指令 | EP18R 指令 | 映射说明 |
|----------|-----------|------------|----------|
| 加法 | `iadd` | `add rd, rs1, rs2` | 栈→寄存器 |
| 减法 | `isub` | `sub rd, rs1, rs2` | 栈→寄存器 |
| 乘法 | `imul` | `mul rd, rs1, rs2` | 栈→寄存器 |
| 除法 | `idiv` | `div rd, rs1, rs2` | 栈→寄存器 |
| 取负 | `ineg` | `neg rd, rs1` | 栈→寄存器 |
| 小于 | `ilt` | `slt rd, rs1, rs2` | 栈→寄存器 |
| 等于 | `ieq` | `seq rd, rs1, rs2` | 栈→寄存器 |
| 加载局部 | `load idx` | `lw rd, fp, offset` | 直接映射 |
| 存储局部 | `store idx` | `sw rs, fp, offset` | 直接映射 |
| 加载常量 | `iconst n` | `li rd, n` | 直接映射 |
| 无条件跳转 | `br L` | `j L` | 语义相同 |
| 条件跳转 | `brf L` | `jf rs, L` | 栈→寄存器 |
| 函数调用 | `call f` | `call addr` | 参数传递不同 |
| 返回 | `ret` | `ret` | 返回值位置不同 |
| 停机 | `halt` | `halt` | 相同 |

---

## 3. VM 适配器架构设计

### 3.1 适配器设计原则

```
1. 抽象统一接口 - 定义 VM 无关的代码生成接口
2. 策略模式 - 不同 VM 使用不同生成策略
3. 适配器层 - 转换 LIR 到目标字节码
4. 可配置目标 - 通过配置选择编译目标
```

### 3.2 适配器架构图

```
EP21 优化编译器
    |
    v
+------------------+
| CodeGenerator    |  ← 统一代码生成接口
+------------------+
    |
    +-------+-------+
    |       |       |
    v       v       v
+--------+ +--------+ +--------+
| Stack  | |Register| |JIT     |  ← 可扩展
| Target | |Target  | |Target  |
+--------+ +--------+ +--------+
    |       |       |
    v       v       v
+--------+ +--------+ +--------+
| Cymbol | |Register| |...     |
|Assembler| |Assembler|        |
+--------+ +--------+ +--------+
    |       |       |
    v       v       v
+--------+ +--------+ +--------+
| EP18   | | EP18R  | |...     |
| .vm    | | .byte  | |        |
+--------+ +--------+ +--------+
```

### 3.3 核心接口设计

```java
// 代码生成器接口
public interface ICodeGenerator {
    void generate(List<IRNode> instructions);
    byte[] getBytecode();
    String getAssembly();
}

// 栈式VM目标生成器
public class StackVMGenerator implements ICodeGenerator {
    private StackVMEmitter emitter = new StackVMEmitter();

    @Override
    public void generate(List<IRNode> instructions) {
        instructions.forEach(instr -> instr.accept(emitter));
    }

    @Override
    public byte[] getBytecode() {
        return emitter.toBytecode();
    }
}

// 寄存器VM目标生成器
public class RegisterVMGenerator implements ICodeGenerator {
    private RegisterVMEmitter emitter = new RegisterVMEmitter();

    @Override
    public void generate(List<IRNode> instructions) {
        instructions.forEach(instr -> instr.accept(emitter));
    }

    @Override
    public byte[] getBytecode() {
        return emitter.toBytecode();
    }
}
```

### 3.4 适配器组件

#### 3.4.1 指令发射器 (Emitter)

```java
// 栈式VM发射器 (现有 CymbolAssembler)
public class StackVMEmitter implements IRVisitor<Void, Void> {
    private List<String> assembly = new ArrayList<>();

    @Override
    public Void visit(Assign assign) {
        assign.getRhs().accept(this);
        if (assign.getLhs() instanceof FrameSlot slot) {
            emit("store %d".formatted(slot.getSlotIdx()));
        }
        return null;
    }

    // ... 其他visit方法
}

// 寄存器VM发射器 (新实现)
public class RegisterVMEmitter implements IRVisitor<Void, Void> {
    private List<Integer> bytecode = new ArrayList<>();
    private Map<String, Integer> regMap = new HashMap<>();
    private int nextReg = 2; // 从a0开始

    @Override
    public Void visit(Assign assign) {
        // 1. 计算右值到寄存器
        int srcReg = emitToRegister(assign.getRhs());
        // 2. 存储结果
        if (assign.getLhs() instanceof FrameSlot slot) {
            emitStore(srcReg, slot.getSlotIdx());
        } else if (assign.getLhs() instanceof VarSlot) {
            // 保存到寄存器映射
            regMap.put(((VarSlot) assign.getLhs()).getName(), srcReg);
        }
        return null;
    }

    private int emitToRegister(IRNode expr) {
        // 将表达式结果计算到寄存器
        // ...
    }
}
```

#### 3.4.2 寄存器分配器

```java
public interface IRegisterAllocator {
    int allocate(String varName);
    void free(int reg);
    Map<String, Integer> getAllocation();
}

// 线性扫描寄存器分配器
public class LinearScanAllocator implements IRegisterAllocator {
    private List<String> variables = new ArrayList<>();
    private Map<String, Integer> allocation = new HashMap<>();
    private int[] usedRegs = new int[16];

    @Override
    public int allocate(String varName) {
        // 查找空闲寄存器
        for (int i = 2; i < 16; i++) { // 跳过R0, R1
            if (usedRegs[i] == 0) {
                usedRegs[i] = 1;
                allocation.put(varName, i);
                return i;
            }
        }
        // 溢出处理: 分配到栈
        return spillToStack(varName);
    }
}
```

#### 3.4.3 调用约定适配器

```java
public interface ICallingConvention {
    int getReturnValueReg();
    int getArgRegister(int index);
    int getStackArgOffset(int index);
    void generatePrologue(CodeBuffer buf, int numLocals);
    void generateEpilogue(CodeBuffer buf, int numLocals);
}

// EP18 调用约定 (栈式)
public class StackCallingConvention implements ICallingConvention {
    @Override
    public int getReturnValueReg() {
        throw new UnsupportedOperationException("栈式VM无返回值寄存器");
    }

    @Override
    public void generatePrologue(CodeBuffer buf, int numLocals) {
        // 无需序言 (纯栈操作)
    }
}

// EP18R 调用约定 (寄存器)
public class RegisterCallingConvention implements ICallingConvention {
    private static final int RETURN_REG = 2;  // a0
    private static final int[] ARG_REGS = {2, 3, 4, 5, 6, 7}; // a0-a5

    @Override
    public int getReturnValueReg() {
        return RETURN_REG;
    }

    @Override
    public int getArgRegister(int index) {
        return ARG_REGS[index];
    }

    @Override
    public void generatePrologue(CodeBuffer buf, int numLocals) {
        buf.emit("addi sp, sp, -%d".formatted(calculateFrameSize(numLocals)));
        buf.emit("sw fp, 0(sp)");
        buf.emit("addi fp, sp, %d".formatted(calculateFrameSize(numLocals) - 4));
    }
}
```

### 3.5 配置驱动选择器

```java
public class VMCodeGeneratorSelector {

    public static ICodeGenerator createGenerator(TargetVM target) {
        return switch (target) {
            case STACK_VM -> new StackVMGenerator();
            case REGISTER_VM -> new RegisterVMGenerator();
        };
    }

    public enum TargetVM {
        STACK_VM("ep18"),
        REGISTER_VM("ep18r");

        private final String moduleName;

        TargetVM(String moduleName) {
            this.moduleName = moduleName;
        }
    }
}
```

---

## 4. 可行性评估总结

### 4.1 EP18 (栈式VM) 可行性

| 评估维度 | 状态 | 说明 |
|----------|------|------|
| **代码生成器** | ✅ 已实现 | CymbolAssembler 完整支持 |
| **指令映射** | ✅ 完整 | 42条指令全部支持 |
| **调用约定** | ✅ 兼容 | 自定义约定，无特殊要求 |
| **测试覆盖** | ✅ 充分 | 404个测试通过 |
| **可行性等级** | **高** | 可直接使用 |

### 4.2 EP18R (寄存器VM) 可行性

| 评估维度 | 状态 | 说明 |
|----------|------|------|
| **代码生成器** | ❌ 未实现 | 无 RegisterAssembler |
| **寄存器分配** | ❌ 未实现 | 需要线性扫描分配器 |
| **调用约定** | ✅ 已定义 | ABI规范完整 |
| **指令映射** | ✅ 完整 | 42条指令对应关系明确 |
| **可行性等级** | **中** | 需要实现适配层 |

### 4.3 核心问题与解决方案

| 问题 | 严重程度 | 解决方案 |
|------|----------|----------|
| 无寄存器VM代码生成器 | 高 | 实现 RegisterVMEmitter |
| 缺少寄存器分配 | 高 | 实现线性扫描分配器 |
| 字节码格式差异 | 中 | 适配器层转换 |
| 调用约定差异 | 中 | CallingConvention 抽象 |
| 栈帧布局差异 | 低 | 偏移量映射表 |

### 4.4 工作量估算

| 组件 | 预估行数 | 复杂度 |
|------|----------|--------|
| RegisterVMEmitter | 300-400 | 中 |
| LinearScanAllocator | 200-300 | 中 |
| RegisterCallingConvention | 150-200 | 低 |
| VMCodeGeneratorSelector | 50-100 | 低 |
| 测试用例 | 200-300 | 中 |
| **总计** | **900-1300** | - |

---

## 5. 实现建议

### 5.1 分阶段实现路线图

#### Phase 1: 基础设施 (基础适配器)
```
1.1 创建 ICodeGenerator 接口
1.2 创建 VMCodeGeneratorSelector
1.3 提取现有 CymbolAssembler 为 StackVMGenerator
1.4 创建空的 RegisterVMGenerator
```

#### Phase 2: 寄存器分配
```
2.1 实现 IRegisterAllocator 接口
2.2 实现 BasicAllocator (简单分配)
2.3 实现 LinearScanAllocator (完整分配)
2.4 集成到 RegisterVMGenerator
```

#### Phase 3: 寄存器VM代码生成
```
3.1 实现 RegisterVMEmitter
3.2 实现二元运算指令映射
3.3 实现加载/存储指令映射
3.4 实现控制流指令映射
3.5 实现函数调用/返回
```

#### Phase 4: 调用约定与集成
```
4.1 实现 RegisterCallingConvention
4.2 集成到 Compiler.java
4.3 添加配置选项
4.4 端到端测试
```

### 5.2 关键设计决策

#### 决策1: 虚拟寄存器 vs 物理寄存器

**选择**: 使用虚拟寄存器，在发射时映射到物理寄存器

```java
// LIR 使用虚拟寄存器 (OperandSlot)
Assign temp1 = new Assign(new OperandSlot("t1"), binExpr);

// 寄存器分配后
Map<String, Integer> regMap = allocator.allocate("t1"); // t1 -> R3

// 发射时
emit("add R2, R3, R4");
```

#### 决策2: 栈帧布局

**选择**: 兼容 EP18R ABI 规范

```
高地址
+-----------+
| 参数7     |  ← fp + 28
+-----------+
| 参数6     |  ← fp + 24
+-----------+
| ...       |
+-----------+
| 参数1     |  ← fp + 8
+-----------+
| 返回地址  |  ← fp + 4
+-----------+
| 保存的fp  |  ← fp (新fp指向这里)
+-----------+
| 保存的s0  |  ← fp - 4
+-----------+
| ...       |
+-----------+
| 局部变量0 |  ← fp - frameSize + 4
+-----------+
| 局部变量1 |
+-----------+
低地址
```

#### 决策3: 溢出处理

**选择**: 溢出到栈，使用spill slot

```java
// 寄存器不足时，溢出到栈
private int spillToStack(String varName) {
    int spillSlot = nextSpillSlot++;
    emit("sw R%d, sp, %d".format(reg, spillSlot * 4));
    spillMap.put(varName, spillSlot);
    return -spillSlot; // 负数表示在栈上
}
```

### 5.3 测试策略

#### 单元测试
- 指令发射器测试
- 寄存器分配器测试
- 调用约定测试

#### 集成测试
- 端到端编译测试
- 函数调用测试
- 递归测试

#### 语义等价测试
- EP18 vs EP18R 输出执行结果对比
- 大规模程序测试

### 5.4 风险与缓解

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|----------|
| 寄存器分配算法复杂 | 中 | 高 | 从简单分配开始，逐步完善 |
| 调用约定不兼容 | 低 | 高 | 严格遵循ABI规范 |
| 性能问题 | 中 | 中 | 性能测试和优化 |
| 测试覆盖不足 | 中 | 中 | 自动化测试 + 语义对比 |

---

## 附录

### A. 参考资料

1. [SSA Construction](https://en.wikipedia.org/wiki/Static_single_assignment_form)
2. [Register Allocation](https://en.wikipedia.org/wiki/Register_allocation)
3. [Calling Convention](https://en.wikipedia.org/wiki/Calling_convention)
4. EP18 源代码: `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/`
5. EP18R 源代码: `ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/`

### B. 相关文件

| 文件 | 位置 | 说明 |
|------|------|------|
| BytecodeDefinition | EP18/EP18R | 字节码定义 |
| CymbolAssembler | EP21 | 现有栈式代码生成器 |
| CallingConventionUtils | EP18R | 调用约定工具 |
| StackOffsets | EP18R | 栈偏移量计算 |

### C. 修订历史

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0 | 2025-12-23 | Claude | 初始版本 |

---

**文档终**
