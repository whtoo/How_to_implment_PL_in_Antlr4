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
    participant TypeChecker as 类型检查器
    participant IRBuilder as IR构建器
    participant CFGBuilder as CFG构建器
    participant Optimizer as 优化器
    participant Assembler as 代码生成器
    participant VM as 虚拟机

    User->>Compiler: 提供源代码
    Compiler->>Lexer: 词法分析
    Lexer-->>Compiler: 词法单元流
    Compiler->>Parser: 语法分析
    Parser-->>Compiler: 解析树
    Compiler->>ASTBuilder: 构建AST
    ASTBuilder-->>Compiler: 抽象语法树
    Compiler->>Symtab: 构建符号表
    Symtab-->>Compiler: 符号表
    Compiler->>TypeChecker: 语义分析
    TypeChecker-->>Compiler: 类型检查结果
    Compiler->>IRBuilder: 生成IR
    IRBuilder-->>Compiler: 中间表示
    Compiler->>CFGBuilder: 构建CFG
    CFGBuilder-->>Compiler: 控制流图
    Compiler->>Optimizer: 优化
    Optimizer-->>Compiler: 优化后的IR
    Compiler->>Assembler: 生成代码
    Assembler-->>Compiler: 虚拟机指令
    Compiler->>VM: 执行代码
    VM-->>User: 输出结果
```

## 2. AST构建详细流程

```mermaid
sequenceDiagram
    participant Parser as 语法分析器
    participant ASTBuilder as AST构建器
    participant ASTNode as AST节点
    participant Visitor as 访问者
    participant Dumper as AST可视化

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
    ASTNode->>Dumper: 生成可视化输出
    Dumper-->>User: 输出AST结构
```

## 3. IR生成详细流程

```mermaid
sequenceDiagram
    participant ASTNode as AST节点
    participant IRBuilder as IR构建器
    participant IRNode as IR节点
    participant Symtab as 符号表
    participant AddrResolver as 地址解析器

    ASTNode->>IRBuilder: accept访问者
    IRBuilder->>Symtab: 查询符号信息
    Symtab-->>IRBuilder: 返回符号(slot, type)
    IRBuilder->>AddrResolver: 解析地址
    AddrResolver-->>IRBuilder: 返回FrameSlot/OperandSlot
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
    participant Analyzer as 控制流分析器

    IRNode->>CFGBuilder: 构建CFG
    CFGBuilder->>CFGBuilder: 分析控制流
    CFGBuilder->>BasicBlock: 创建基本块
    BasicBlock-->>CFGBuilder: 基本块实例
    CFGBuilder->>CFG: 连接基本块
    CFG-->>CFGBuilder: CFG实例
    CFGBuilder->>Analyzer: 应用控制流分析
    Analyzer-->>CFGBuilder: 分析结果
    CFGBuilder-->>IRNode: 返回CFG
```

## 5. 优化详细流程

```mermaid
sequenceDiagram
    participant CFG as 控制流图
    participant Optimizer as 优化器
    participant Analysis as 分析器
    participant Transform as 转换器
    participant Liveness as 活性分析

    CFG->>Optimizer: 应用优化
    Optimizer->>Liveness: 活性分析
    Liveness-->>Optimizer: 活性信息
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
    participant VM as 虚拟机

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
    Assembler->>VM: 生成最终指令
    VM-->>IRNode: 返回执行结果
```

## 7. 调试支持流程

```mermaid
sequenceDiagram
    participant Developer as 开发者
    participant Debugger as 调试器
    participant ASTDumper as AST可视化
    participant IRDumper as IR可视化
    participant CFGDumper as CFG可视化

    Developer->>Debugger: 启动调试
    Debugger->>ASTDumper: 生成AST可视化
    ASTDumper-->>Debugger: AST结构图
    Debugger->>IRDumper: 生成IR可视化
    IRDumper-->>Debugger: IR指令序列
    Debugger->>CFGDumper: 生成CFG可视化
    CFGDumper-->>Debugger: 控制流图
    Debugger-->>Developer: 显示调试信息
```

## 8. 错误处理流程

```mermaid
sequenceDiagram
    participant Compiler as 编译器
    participant ErrorIssuer as 错误发布器
    participant Logger as 日志系统
    participant User as 用户

    Compiler->>ErrorIssuer: 检测到错误
    ErrorIssuer->>Logger: 记录错误信息
    Logger-->>ErrorIssuer: 确认记录
    ErrorIssuer->>User: 显示错误信息
    alt 致命错误
        ErrorIssuer->>Compiler: 终止编译
    else 可恢复错误
        ErrorIssuer->>Compiler: 继续编译
    end
```

## 9. 测试执行流程

```mermaid
sequenceDiagram
    participant TestRunner as 测试运行器
    participant JUnit as JUnit框架
    participant TestCase as 测试用例
    participant Assertion as 断言框架
    participant Report as 报告生成器

    TestRunner->>JUnit: 启动测试
    JUnit->>TestCase: 执行测试方法
    TestCase->>Assertion: 执行断言
    Assertion-->>TestCase: 断言结果
    TestCase-->>JUnit: 测试结果
    JUnit->>Report: 生成报告
    Report-->>TestRunner: 测试报告
```

## 10. 持续集成流程

```mermaid
sequenceDiagram
    participant Git as Git仓库
    participant CI as CI系统
    participant Maven as Maven构建
    participant Test as 测试套件
    participant Coverage as 覆盖率检查
    participant Deploy as 部署系统

    Git->>CI: 代码推送
    CI->>Maven: 触发构建
    Maven->>Test: 运行测试
    Test-->>Maven: 测试结果
    Maven->>Coverage: 检查覆盖率
    Coverage-->>Maven: 覆盖率报告
    alt 测试通过且覆盖率达标
        Maven->>Deploy: 部署应用
    else 测试失败或覆盖率不足
        Maven->>CI: 构建失败
    end
    CI-->>Git: 更新状态