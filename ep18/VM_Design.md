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



