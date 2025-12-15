# 虚拟机设计文档 (VM Design)

## 1. 字节码定义和指令集

### 指令集概览
- 算术运算指令: iconst, iadd, imul
- 控制流指令: call, ret, halt
- 内存访问指令: load

### 字节码格式
```
.def 函数名: args=参数数量, locals=局部变量数量
指令序列
```

## 2. 虚拟机执行引擎

### 核心组件
- 指令分派器 (Instruction Dispatcher)
- 操作数栈 (Operand Stack)
- 程序计数器 (Program Counter)
- 运行时状态寄存器 (Runtime Status Register)
- 异常处理器 (Exception Handler)

### 执行流程
1. 取指令
2. 解码指令
3. 验证指令合法性
4. 执行指令
5. 更新程序计数器
6. 检查运行时状态

### 指令分派循环
```
while (running) {
    instruction = fetch(pc);
    opcode = decode(instruction);
    
    // 指令合法性检查
    if (!validate(opcode)) {
        raise_exception(INVALID_OPCODE);
        break;
    }
    
    // 执行指令
    switch (opcode) {
        case ICONST: execute_iconst(); break;
        case IADD: execute_iadd(); break;
        // ...其他指令处理
    }
    
    // 更新PC
    pc += instruction_size(opcode);
    
    // 检查运行时状态
    if (status_register & EXCEPTION_MASK) {
        handle_exception();
    }
}
```

### 栈帧切换机制
1. 函数调用时:
   - 保存当前栈帧状态
   - 分配新栈帧空间
   - 设置参数区和局部变量区
   - 更新PC到函数入口

2. 函数返回时:
   - 恢复调用者栈帧
   - 回收被调用者栈帧空间
   - 设置返回值
   - 恢复PC到返回地址

### 运行时检查
- 操作数栈溢出/下溢检查
- 内存访问越界检查
- 类型检查
- 除零检查
- 空指针检查

## 3. 内存管理模型

### 栈帧结构 (Stack Frame)
- 参数区
- 局部变量区
- 返回地址
- 操作数栈

### 内存区域
- 代码区 (存储字节码)
- 数据区 (全局变量)
- 栈区 (函数调用栈)

## 4. 函数调用机制

### 调用约定
1. 调用者压入参数
2. 执行call指令
3. 被调用者设置栈帧
4. 执行函数体
5. 通过ret指令返回

### 示例调用流程
```
main -> f -> ck
```

## 5. 示例代码结构

### 典型字节码示例
```
.def main: args=0, locals=0
    iconst 10
    iconst 20
    call f()
    print
    halt
```

### 核心类结构
- `VMInterpreter`: 虚拟机解释器
- `VMRunner`: 虚拟机运行器
- `ByteCodeAssembler`: 字节码汇编器
- `StackFrame`: 栈帧实现
- `FunctionSymbol`: 函数符号表

## 6. 寄存器虚拟机设计 (ep18r)

### 概述
ep18r 是基于寄存器架构的独立虚拟机，采用寄存器指令集和优化的执行模型，专注于寄存器架构的性能优势。

### 寄存器架构
- **寄存器数量**: 16个通用寄存器 (r0-r15)
  - r0: 零寄存器（恒为0）
  - r1-r12: 通用目的寄存器
  - r13: 栈指针 (SP)
  - r14: 帧指针 (FP)
  - r15: 链接寄存器 (LR，存储返回地址)
- **寄存器文件**: 管理寄存器状态，支持读写操作

### 指令格式
寄存器指令采用32位编码，支持三种格式：
1. **R类型**: opcode(6) | rd(5) | rs1(5) | rs2(5) | unused(11)
   - 用于算术运算：`add rd, rs1, rs2`
2. **I类型**: opcode(6) | rd(5) | rs1(5) | immediate(16)
   - 用于立即数加载：`li rd, immediate`
   - 用于内存访问：`lw rd, base, offset`
3. **J类型**: opcode(6) | address(26)
   - 用于跳转指令：`j target`

### 指令集映射（栈式→寄存器）
栈式指令与寄存器指令的对应关系：
- `iconst 10` → `li r1, 10`
- `iadd`（弹出v1, v2，压入v1+v2） → `add r3, r1, r2`
- `load offset`（从局部变量加载） → `lw r1, fp, offset`

### 执行模型
1. **取指**: 从指令缓存读取32位指令
2. **解码**: 解析操作码和操作数（寄存器编号或立即数）
3. **执行**: 访问寄存器文件，执行运算
4. **写回**: 将结果写回目标寄存器
5. **更新PC**: 程序计数器增加指令长度

### 内存访问
- 局部变量：通过帧指针 (FP) 和偏移量访问
- 全局变量：通过全局基址寄存器和偏移量访问
- 结构体字段：通过对象指针和字段偏移量访问

### 函数调用约定
1. 调用者将参数存入寄存器 r1-rn
2. 执行 `call target` 指令，将返回地址存入 LR (r15)
3. 被调用者保存调用者保存的寄存器（如果需要）
4. 函数执行结果存入 r1
5. 执行 `ret` 指令，从 LR 恢复 PC

### 核心类结构 (ep18r)
- `CymbolRegisterVM`: 寄存器虚拟机主类
- `RegisterVMInterpreter`: 寄存器指令解释器
- `RegisterBytecodeDefinition`: 寄存器指令集定义
- `RegisterFile`: 寄存器文件管理
- `RegisterByteCodeAssembler`: 寄存器汇编器
- `RegisterDisAssembler`: 寄存器反汇编器

### 设计目标
ep18r 专注于寄存器架构的优势：
- 减少内存访问，提升执行效率
- 支持寄存器优化算法
- 提供灵活的寄存器分配策略
- 保持模块化设计，便于扩展

### 实现状态
当前实现阶段：寄存器指令集设计完成，虚拟机核心实现进行中。



