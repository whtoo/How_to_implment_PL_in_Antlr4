# EP18 测试失败修复报告

## 执行概要

**修复日期**: 2025-12-20
**模块**: ep18 (StackVM虚拟机)
**修复状态**: 部分完成 (157/160 测试通过, 98.1%)

## 测试失败统计

| 状态 | 数量 | 百分比 |
|------|------|--------|
| ✅ 通过 | 157 | 98.1% |
| ❌ 失败 | 3 | 1.9% |
| ⏭️ 跳过 | 5 | - |

## 已修复的问题

### 1. VMInterpreter 参数处理缺陷 ✅

**问题描述**:
- 当函数定义包含参数但 `nlocals=0` 时，函数调用无法正确存储参数
- `testFunctionCall` 和 `testRecursiveFunction` 测试失败
- 错误: `NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "this.operands[...]" is null`

**根本原因**:
- `VMInterpreter.call()` 方法假设参数总是存储在 `locals` 数组中
- 当 `nlocals=0` 时，`locals` 数组为空，参数没有被存储
- `StackFrame` 有独立的 `parameters` 数组，但未被使用

**修复方案**:
```java
// 修改 VMInterpreter.call() 方法
protected void call(int functionConstPoolIndex) {
    FunctionSymbol fs = (FunctionSymbol) constPool[functionConstPoolIndex];
    StackFrame f = new StackFrame(fs, ip);
    calls[++fp] = f;

    // 修复: 使用 parameters 数组存储参数
    if (fs.nargs > 0 && f.getParameters() != null) {
        for (int a = fs.nargs - 1; a >= 0; a--) {
            f.getParameters()[a] = operands[sp--];
        }
    }
    ip = fs.address;
}
```

**修复状态**: ✅ 完成 - 所有 18 个 VMInterpreter 测试通过

### 2. VMInterpreter LOAD/STORE 指令参数访问缺陷 ✅

**问题描述**:
- LOAD/STORE 指令只检查 `locals` 数组，不检查 `parameters` 数组
- 当参数索引超出 `locals` 范围时，访问失败

**修复方案**:
```java
// 修改 LOAD 指令处理
case BytecodeDefinition.INSTR_LOAD:
    int loadAddr = getIntOperand();
    StackFrame currentFrame = calls[fp];

    // 先检查 locals，再检查 parameters
    if (loadAddr >= 0 && loadAddr < currentFrame.getLocals().length) {
        operands[++sp] = currentFrame.getLocals()[loadAddr];
    }
    else if (loadAddr >= 0 && currentFrame.getParameters() != null &&
             loadAddr < currentFrame.getParameters().length) {
        operands[++sp] = currentFrame.getParameters()[loadAddr];
    }
    break;
```

**修复状态**: ✅ 完成

### 3. CymbolStackVM 主函数栈帧缺失 ✅

**问题描述**:
- `execute()` 方法没有为主函数创建栈帧
- 当执行 CALL 指令时，没有调用者栈帧来保存栈深度

**修复方案**:
```java
// 在 execute() 方法中添加主函数栈帧创建
// 加载字节码到指令缓存
loadBytecode(bytecode);

// 创建主函数栈帧（模拟"call main()"）
FunctionSymbol mainSymbol = new FunctionSymbol("main", 0, 0, 0);
StackFrame mainFrame = new StackFrame(mainSymbol, -1);
callStack[++framePointer] = mainFrame;

// 开始执行
this.running = true;
this.programCounter = 0;
```

**修复状态**: ✅ 完成

## 未完全修复的问题

### 1. CymbolStackVM CALL/RET 栈管理缺陷 ⚠️

**问题描述**:
- `testFunctionCallReturn` 期望结果 20，实际得到 10
- `testNestedFunctionCalls` 期望结果 3，实际得到 1
- 函数调用后，栈上没有正确清理参数，只保留返回值

**根本原因**:
- CALL 指令需要保存当前栈深度到调用者栈帧
- RET 指令需要恢复栈状态，清理参数但保留返回值
- 需要实现完整的调用约定 (Calling Convention)

**尝试的修复方案**:
```java
// 在 executeCall() 和 callFunction() 中
private void executeCall(int instruction) {
    int targetAddress = extractOperand(instruction);
    int returnAddress = programCounter;

    // 保存当前栈深度
    int savedStackDepth = stackPointer;

    // 保存到调用者栈帧
    if (framePointer >= 0 && callStack[framePointer] != null) {
        callStack[framePointer].setDebugData("savedStackDepth", savedStackDepth);
    }
}

// 在 executeRet() 和 returnFromFunction() 中
private void executeRet() {
    StackFrame frame = callStack[framePointer--];
    int returnAddress = frame.getReturnAddress();

    // 从调用者栈帧获取保存的栈深度
    Integer savedDepth = (Integer) callStack[framePointer].getDebugData("savedStackDepth");

    if (savedDepth != null) {
        // 恢复栈状态，但保留返回值
        int returnValue = stack[stackPointer - 1];
        stackPointer = savedDepth;
        stack[stackPointer++] = returnValue;
    }

    programCounter = returnAddress;
}
```

**当前状态**: ⚠️ 代码已修改但测试仍失败
**剩余问题**: 需要进一步调试栈深度保存/恢复逻辑

### 2. testStructOutOfMemory 内存管理异常 ❌

**问题描述**:
- 测试期望创建包含 2,000,000 字段的结构体时抛出 `OutOfMemoryError`
- 实际执行没有抛出异常，测试失败

**根本原因**:
- `StructValue` 类使用 Java 堆内存 (`new Object[fieldCount]`)
- 而 VM 的内存检查逻辑基于 `heap` 数组 (int[])
- 内存分配不在同一个内存空间，导致检查失效

**当前状态**: ❌ 未修复
**建议**: 需要重构结构体内存分配，使用统一的内存管理机制

## 技术债务和设计问题

### 1. VMInterpreter vs CymbolStackVM 不一致

**问题**:
- 两个虚拟机实现有不同的调用约定
- 参数和局部变量的存储方式不同
- 难以维护和调试

**建议**:
- 统一调用约定
- 创建通用的栈帧管理接口
- 重构为单一 VM 实现

### 2. 指令工厂 vs 遗留代码双重路径

**问题**:
- 存在两套指令执行路径:
  1. 新的策略模式 (InstructionFactory)
  2. 旧的 switch 语句 (executeInstructionLegacy)
- 需要在两个地方保持同步更新

**建议**:
- 完成指令迁移到新工厂模式
- 移除遗留代码路径
- 统一异常处理机制

### 3. JaCoCo 代码覆盖工具异常

**问题**:
```
java.lang.instrument.IllegalClassFormatException:
Error while instrumenting sun/util/resources/cldr/provider/CLDRLocaleDataMetaInfo
```

**影响**: 不影响功能，但污染测试输出

**建议**:
- 升级 JaCoCo 版本到最新
- 或禁用特定类的代码覆盖
- 考虑使用替代工具 (如 JaCoCo 0.8.12+)

## 调试输出摘要

### testFunctionCallReturn 调试信息
```
[DEBUG] CALL: saving stack depth 1 before calling function at 4
[DEBUG] CALL: saved depth in caller frame
// 缺少 RET 调试信息，说明新指令路径未被触发
expected: 20
 but was: 10
```

**分析**: RET 指令可能通过遗留路径执行，没有应用栈恢复逻辑

### 字节码指令分析
```
Instruction 0: opcode=29 (ICONST), operand=10
Instruction 1: opcode=23 (CALL), operand=4
Instruction 2: opcode=42 (HALT)
Instruction 3: opcode=29 (ICONST), operand=0  // 填充
Instruction 4: opcode=29 (ICONST), operand=20
Instruction 5: opcode=24 (RET)
```

**分析**: CALL 和 RET 指令可能使用了不同的执行路径

## 测试用例分析

### testFunctionCallReturn
```java
// 字节码流程:
// 0: ICONST 10        // 主程序：常量10
// 1: CALL 4           // 调用函数（跳转到地址4）
// 2: HALT             // 函数返回后继续执行
// 3: (未使用)          // 填充
// 4: ICONST 20        // 函数体：常量20
// 5: RET              // 返回主程序

// 期望: 栈顶应该是20（最后压入的值）
// 实际: 栈顶是10（调用前的值未被清理）
```

### testNestedFunctionCalls
```java
// 字节码流程:
// 0: ICONST 1          // main: 常量1
// 1: CALL 4            // 调用func1
// 2: HALT              // 返回后结束
// 3: (未使用)           // 填充
// 4: ICONST 2          // func1: 常量2
// 5: CALL 8            // 调用func2
// 6: RET               // func1返回
// 7: (未使用)           // 填充
// 8: ICONST 3          // func2: 常量3
// 9: RET               // func2返回

// 期望: 栈顶应该是3（最后压入的值）
// 实际: 栈顶是1（调用前的值未被清理）
```

## 建议的解决方案

### 短期 (1-2天)

1. **调试 CymbolStackVM CALL/RET 栈恢复逻辑**
   - 添加更详细的调试输出
   - 验证栈深度保存和恢复
   - 确保 RET 指令使用正确的执行路径

2. **修复 testStructOutOfMemory**
   - 统一内存分配机制
   - 或调整测试期望

### 中期 (1周)

1. **统一 VM 调用约定**
   - 创建通用的栈帧接口
   - 重构 VMInterpreter 和 CymbolStackVM
   - 移除重复代码

2. **完成指令工厂迁移**
   - 将所有指令迁移到新模式
   - 移除遗留代码路径
   - 统一异常处理

### 长期 (2-4周)

1. **重构内存管理**
   - 统一堆、栈、结构体内存管理
   - 实现垃圾回收机制
   - 添加内存保护

2. **优化性能**
   - 减少栈帧创建开销
   - 优化指令执行速度
   - 添加 JIT 编译支持

## 结论

本次修复成功解决了 VMInterpreter 的参数处理缺陷，提升了测试通过率从 97% 到 98.1%。主要的技术挑战在于 CymbolStackVM 的栈管理机制，需要实现完整的调用约定来正确处理函数调用和返回。

建议优先解决 CALL/RET 栈恢复问题，这将使测试通过率提升到 99% 以上。然后考虑重构统一两个 VM 实现，消除技术债务。

## 附录

### 修改的文件

1. `/ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java`
   - `call()` 方法
   - `LOAD` 指令处理
   - `STORE` 指令处理

2. `/ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java`
   - `execute()` 方法
   - `executeCall()` 方法
   - `executeRet()` 方法
   - `callFunction()` 方法
   - `returnFromFunction()` 方法
   - `loadBytecode()` 方法
   - `executeInstruction()` 方法

### 调试命令

```bash
# 运行特定测试
mvn test -pl ep18 -Dtest=VMInterpreterTest

# 运行失败的测试
mvn test -pl ep18 -Dtest=CymbolStackVMTest#testFunctionCallReturn

# 查看详细报告
cat /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18/target/surefire-reports/org.teachfx.antlr4.ep18.CymbolStackVMTest.txt
```

### 相关文档

- [EP18 项目概览](README.md)
- [StackVM 设计文档](docs/EP18_VM_Design.md)
- [调用约定规范](docs/Calling_Convention.md)
