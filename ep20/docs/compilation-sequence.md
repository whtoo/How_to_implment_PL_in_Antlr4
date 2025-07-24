# EP20编译流程序列图

## 1. 整体编译流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Compiler as 编译器主程序
    participant Lexer as 词法分析器
    participant Parser as 语法分析器
    participant ASTBuilder as AST构建器
    participant Symtab as 符号表
    participant IRBuilder as IR构建器
    participant CFGBuilder as CFG构建器
    participant Optimizer as 优化器
    participant Assembler as 代码生成器

    User->>Compiler: 提供源代码
    Compiler->>Lexer: 词法分析
    Lexer-->>Compiler: 词法单元流
    Compiler->>Parser: 语法分析
    Parser-->>Compiler: 解析树
    Compiler->>ASTBuilder: 构建AST
    ASTBuilder-->>Compiler: 抽象语法树
    Compiler->>Symtab: 构建符号表
    Symtab-->>Compiler: 符号表
    Compiler->>IRBuilder: 生成IR
    IRBuilder-->>Compiler: 中间表示
    Compiler->>CFGBuilder: 构建CFG
    CFGBuilder-->>Compiler: 控制流图
    Compiler->>Optimizer: 优化
    Optimizer-->>Compiler: 优化后的IR
    Compiler->>Assembler: 生成代码
    Assembler-->>Compiler: 虚拟机指令
    Compiler->>User: 输出结果
```

## 2. AST构建详细流程

```mermaid
sequenceDiagram
    participant Parser as 语法分析器
    participant ASTBuilder as AST构建器
    participant ASTNode as AST节点
    participant Visitor as 访问者

    Parser->>ASTBuilder: visit方法调用
    ASTBuilder->>ASTBuilder: 创建相应AST节点
    ASTBuilder-->>Parser: 返回AST节点
    loop 遍历子节点
        Parser->>ASTBuilder: 递归调用visit
        ASTBuilder->>ASTBuilder: 创建子节点
        ASTBuilder-->>Parser: 返回子节点
    end
    Parser->>Visitor: accept访问者
    Visitor->>ASTNode: 处理节点
```

## 3. IR生成详细流程

```mermaid
sequenceDiagram
    participant ASTNode as AST节点
    participant IRBuilder as IR构建器
    participant IRNode as IR节点
    participant Symtab as 符号表

    ASTNode->>IRBuilder: accept访问者
    IRBuilder->>Symtab: 查询符号信息
    Symtab-->>IRBuilder: 返回符号
    IRBuilder->>IRBuilder: 创建IR节点
    IRBuilder-->>ASTNode: 返回IR节点
    loop 处理子节点
        ASTNode->>IRBuilder: 递归调用accept
        IRBuilder->>IRBuilder: 创建子IR节点
        IRBuilder-->>ASTNode: 返回子IR节点
    end
```

## 4. CFG构建详细流程

```mermaid
sequenceDiagram
    participant IRNode as IR节点
    participant CFGBuilder as CFG构建器
    participant BasicBlock as 基本块
    participant CFG as 控制流图

    IRNode->>CFGBuilder: 构建CFG
    CFGBuilder->>CFGBuilder: 分析控制流
    CFGBuilder->>BasicBlock: 创建基本块
    BasicBlock-->>CFGBuilder: 基本块实例
    CFGBuilder->>CFG: 连接基本块
    CFG-->>CFGBuilder: CFG实例
    CFGBuilder-->>IRNode: 返回CFG
```

## 5. 优化详细流程

```mermaid
sequenceDiagram
    participant CFG as 控制流图
    participant Optimizer as 优化器
    participant Analysis as 分析器
    participant Transform as 转换器

    CFG->>Optimizer: 应用优化
    Optimizer->>Analysis: 数据流分析
    Analysis-->>Optimizer: 分析结果
    Optimizer->>Transform: 应用转换
    Transform->>CFG: 修改CFG
    CFG-->>Transform: 更新后的CFG
    Transform-->>Optimizer: 优化后的CFG
    Optimizer-->>CFG: 返回优化后的CFG
```

## 6. 代码生成详细流程

```mermaid
sequenceDiagram
    participant IRNode as IR节点
    participant Assembler as 代码生成器
    participant Emitter as 操作符发射器
    participant Buffer as 指令缓冲区

    IRNode->>Assembler: accept访问者
    Assembler->>Emitter: 查询指令
    Emitter-->>Assembler: 指令字符串
    Assembler->>Buffer: 添加指令
    Buffer-->>Assembler: 更新缓冲区
    loop 处理子节点
        IRNode->>Assembler: 递归调用accept
        Assembler->>Buffer: 添加指令
        Buffer-->>Assembler: 更新缓冲区
    end
    Assembler-->>IRNode: 返回汇编代码