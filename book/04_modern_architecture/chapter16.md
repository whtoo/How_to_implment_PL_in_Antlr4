# 第16章：端到端编译器流水线集成 (EP20)

## 本章概述

本章将带你完整串联前几章学到的所有编译器组件，从词法分析到代码生成，构建一个功能完整的端到端编译器。你将理解如何协调 EP17（调用图分析）、EP18（虚拟机）、EP19（类型系统）和 EP20（完整编译器）各个模块，实现从 Cymbol 源代码到可执行字节码的完整编译流程，并学习错误处理、多Pass协调和编译器驱动设计。

【你现在站在哪】:
```
... → [AST构建] → [符号表] → [IR生成] → [CFG构建] → [优化] → [代码生成] → ✅ [端到端集成] → ...
```

## 动机与真实场景

### 真实场景

想象你在维护一个大型遗留系统，老板要求你为系统添加一个全新的编程语言接口，让业务人员可以用自定义脚本编写自动化规则。你查阅了现有代码，发现有多个独立的组件：词法分析器、语法分析器、符号表、类型检查器、中间表示生成器、优化器和代码生成器，但它们散落在不同的目录中，从未被整合成一条完整的编译流水线。

更糟糕的是，这些组件之间缺乏清晰的接口定义和集成文档。你不知道应该先运行哪个阶段，哪个阶段的输出是下一个阶段的输入，如何处理各阶段之间的错误传播，以及如何验证整个流水线的正确性。此时，你需要的是一个完整、可工作的端到端编译器，能够自动化地串联所有阶段，提供清晰的编译报告和错误诊断能力。

### 具体问题与挑战

- **阶段协调问题**：10+个编译阶段如何有序执行？阶段间的依赖关系如何管理？
- **错误处理问题**：词法错误、语法错误、语义错误、运行时错误如何统一捕获和报告？
- **资源管理问题**：符号表、IR、CFG等数据结构的生命周期如何管理？
- **调试困难**：当编译结果错误时，如何定位是哪个阶段引入的问题？
- **扩展性问题**：如何添加新的优化Pass而不破坏现有流程？

### 缺失本章能力的影响

如果缺少端到端集成能力，你将面临：
- 各编译阶段孤立，无法形成完整工具
- 错误处理混乱，难以定位问题根源
- 无法提供用户友好的编译错误提示
- 难以扩展和维护编译器功能
- 无法进行端到端的性能分析和优化

### 本章学习目标

- 掌握编译器流水线架构设计
- 学会多Pass协调和依赖管理
- 实现统一的错误处理和诊断系统
- 设计编译器驱动和命令行接口
- 掌握集成测试和调试技术
- 学习性能分析和优化方法

## 人类工程师线：技术与实现

### 核心概念

**编译器流水线 (Compiler Pipeline)** 是将源代码转换为目标代码的多个阶段的有序序列。每个阶段都有明确的输入输出和职责，通过中间表示（IR）作为阶段间的桥梁。

通俗解释：编译器流水线就像一条汽车装配生产线。前端层将原材料（源代码）加工成零件（AST），中间层将零件组装成半成品（优化的 IR），后端层将半成品进行最终组装和调试（目标代码）。每个阶段都有自己的职责，但必须严格按顺序执行，前一个阶段的输出就是下一个阶段的输入。

[图1：完整的编译器流水线架构]
```
源代码文件 (example.cymbol)
   │
   ▼
┌────────────────────────────────────────────────────────────────┐
│                    前端层（Frontend Layer）                     │
│  ┌────────────┐  ┌────────────┐  ┌─────────────┐              │
│  │   Lexer    │──▶│   Parser   │──▶│   AST       │              │
│  │ (字符→Token)│  │ (Token→树) │  │  (构建器)    │              │
│  └────────────┘  └────────────┘  └─────────────┘              │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────────────────────────────┐
│                   中端层（Middle-End Layer）                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   Symbol    │──▶│   Type      │──▶│     IR      │           │
│  │  Resolution │  │   Checker   │  │   Builder   │           │
│  │ (符号解析)   │  │ (类型检查)   │  │ (IR生成)    │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
│                                                                │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   CFG       │──▶│   Optimizers│──▶│  Linear IR  │           │
│  │  (控制流图)  │  │  (优化Pass)  │  │ (线性化IR)   │           │
│  └─────────────┘  └─────────────┘  └─────────────┘           │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ▼
┌────────────────────────────────────────────────────────────────┐
│                    后端层（Backend Layer）                      │
│  ┌──────────────────────────────────────┐                    │
│  │   Code Generator (Assembler)         │                    │
│  │   (IR → EP18 VM Bytecode)            │                    │
│  └──────────────────────────────────────┘                    │
└────────────────────────────┬───────────────────────────────────┘
                             │
                             ▼
                         输出文件 (example.vm)
```

**数据流图**：
Cymbol源代码 → Token流 → ParseTree → AST → 符号表 + 类型信息 → 三地址码IR → 控制流图CFG → 优化后的CFG → 线性IR → EP18 VM字节码

每个数据结构都是前一个阶段的抽象和转换，就像炼金术中的物质转化过程。

**错误处理策略**：编译器流水线中的错误可以在任何阶段发生。需要统一的错误处理机制：
- **词法错误**：非法字符、未闭合的字符串
- **语法错误**：不符合语法规则的结构
- **语义错误**：类型不匹配、未声明的变量
- **编译时警告**：隐式类型转换、未使用的变量
- **运行时错误**：除零、数组越界（由VM捕获）

### 与仓库 EP 的对应关系

对应 EP：EP20（完整编译器）

目录结构：
```
ep20/
├── src/main/java/org/teachfx/antlr4/ep20/
│   ├── Compiler.java                     # 编译器主类（驱动器）
│   ├── pass/
│   │   ├── ast/
│   │   │   └── CymbolASTBuilder.java     # AST构建器
│   │   ├── symtab/
│   │   │   └── LocalDefine.java          # 符号解析（符号表构建）
│   │   ├── sematic/                      # 语义分析（类型检查）
│   │   │   └── TypeChecker.java          # 类型检查器
│   │   ├── ir/
│   │   │   └── CymbolIRBuilder.java      # IR生成器
│   │   ├── cfg/
│   │   │   ├── CFGBuilder.java           # CFG构建器
│   │   │   ├── ControlFlowAnalysis.java  # 控制流优化器
│   │   │   └── LinearIRBlock.java        # 线性IR生成器
│   │   └── codegen/
│   │       └── CymbolAssembler.java      # 字节码生成器（汇编器）
│   ├── parser/
│   │   ├── CymbolLexer.java              # 词法分析器（ANTLR4生成）
│   │   └── CymbolParser.java             # 语法分析器（ANTLR4生成）
│   └── ir/                               # IR节点定义
│       ├── IRNode.java                   # IR节点基类
│       ├── Prog.java                     # IR程序根节点
│       └── ... (各种IR节点)
└── src/test/java/org/teachfx/antlr4/ep20/
    ├── IntegrationTest.java              # 集成测试
    ├── pass/                             # 各阶段单元测试
    └── ...
```

### 核心实现

#### 1. 编译器主驱动 (Compiler.java)

EP20的编译器入口展示了完整的编译流程：

```java
/**
 * EP20 完整编译器主类
 * 
 * <p>实现从Cymbol源代码到EP18 VM字节码的完整编译流水线。</p>
 * <p>编译流程：词法分析 → 语法分析 → AST构建 → 符号解析 → 类型检查 → IR生成 → CFG构建 → 优化 → 代码生成</p>
 */
public class Compiler {
    private static final Logger logger = LogManager.getLogger(Compiler.class);

    /**
     * 主入口方法
     */
    public static void main(String[] args) throws IOException {
        // 阶段1：输入处理（命令行参数或默认文件）
        String fileName = args.length > 0 ? args[0] : null;
        InputStream is = System.in;
        
        if (fileName != null) {
            is = new FileInputStream(fileName);
        } else {
            // 默认使用资源文件
            is = Compiler.class.getClassLoader().getResourceAsStream("t.cymbol");
            if (is == null) {
                fileName = "src/main/resources/t.cymbol";
                is = new FileInputStream(fileName);
            }
        }

        // 阶段2：词法分析（Lexer）
        CharStream charStream = CharStreams.fromStream(is);
        CymbolLexer lexer = new CymbolLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);

        // 阶段3：语法分析（Parser）
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        
        // 错误处理：检查语法错误
        if (parser.getNumberOfSyntaxErrors() > 0) {
            logger.error("语法分析发现{}个错误", parser.getNumberOfSyntaxErrors());
            System.exit(1);
        }

        // 阶段4：AST构建
        var astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);
        logger.debug("AST构建完成，根节点类型: {}", astRoot.getClass().getSimpleName());

        // 阶段5：符号解析（符号表构建）
        var symbolDefiner = new LocalDefine();
        astRoot.accept(symbolDefiner);
        var symbolTable = symbolDefiner.getSymbolTable();
        logger.debug("符号表构建完成，符号数量: {}", symbolTable.getAllSymbols().size());

        // 阶段6：类型检查
        var typeChecker = new TypeChecker(symbolTable);
        astRoot.accept(typeChecker);
        logger.debug("类型检查完成");

        // 阶段7：IR生成
        var irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        logger.debug("IR生成完成，基本块数量: {}", irBuilder.prog.blockList.size());

        // 阶段8：基本块优化（可选）
        irBuilder.prog.optimizeBasicBlock();
        logger.debug("基本块优化完成");

        // 阶段9：CFG构建 + 控制流优化 + 代码生成（并行处理每个函数）
        Stream.of(
            StreamUtils.indexStream(irBuilder.prog.blockList.stream().map(irBuilder::getCFG))
        )
        .peek(cfgPair -> {
            var cfg = cfgPair.getRight();
            var idx = cfgPair.getLeft();
            
            // 保存CFG可视化（调试用）
            saveToEp20Res(cfg.toString(), "%d_origin".formatted(idx));
            
            // 应用优化Pass
            cfg.addOptimizer(new ControlFlowAnalysis<>());
            cfg.applyOptimizers();
            
            // 保存优化后的CFG
            saveToEp20Res(cfg.toString(), "%d_optimized".formatted(idx));
            logger.debug("函数{}优化完成", idx);
        })
        .map(Pair::getRight)
        .map(CFG::getIRNodes)  // 将CFG转换为线性IR
        .reduce(new ArrayList<IRNode>(), (a, b) -> {
            a.addAll(b);
            return a;
        })
        .map(irNodeList -> {
            // 阶段10：代码生成（转换为EP18 VM字节码）
            var assembler = new CymbolAssembler();
            assembler.visit(irNodeList);
            return assembler;
        })
        .forEach(assembler -> {
            // 输出生成的字节码
            saveToEp18Res(assembler.getAsmInfo());
            logger.info("\n===== 生成的EP18 VM字节码 =====\n{}", assembler.getAsmInfo());
        });
        
        logger.info("编译完成！");
    }

    /**
     * 将生成的字节码保存到文件
     */
    protected static void saveToEp18Res(String buffer) {
        String modulePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (modulePath == null) {
            modulePath = "../ep20/target/classes";
        }
        File moduleDirectory = new File(modulePath);
        
        if (moduleDirectory.exists()) {
            var filePath = modulePath + "/t.vm";  // EP18 VM字节码文件
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                outputStream.write(buffer.getBytes());
                logger.debug("字节码已保存到: {}", filePath);
            } catch (IOException e) {
                logger.error("保存字节码失败", e);
            }
        }
    }

    /**
     * 将CFG可视化保存到文件（调试用）
     */
    protected static void saveToEp20Res(String buffer, String suffix) {
        String modulePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        if (modulePath == null) {
            modulePath = "./target/classes";
        }
        File moduleDirectory = new File(modulePath);
        
        if (moduleDirectory.exists()) {
            var filePath = modulePath + "/graph_%s.md".formatted(suffix);
            File file = new File(filePath);
            try (var outputStream = new FileOutputStream(file)) {
                if (!file.exists()) {
                    file.createNewFile();
                }
                // 使用Mermaid格式保存，便于可视化
                String template = """
                    ```mermaid
                    %s
                    ```
                    """.formatted(buffer);
                outputStream.write(template.getBytes());
                logger.debug("CFG图已保存到: {}", filePath);
            } catch (IOException e) {
                logger.error("保存CFG失败", e);
            }
        }
    }
}
```

#### 2. 编译阶段封装 (Phase Pattern)

良好的编译器设计将每个阶段封装为独立的**Phase**（阶段）：

```java
/**
 * 编译阶段基类
 * 
 * <p>所有编译阶段的抽象基类，定义了阶段的统一接口。</p>
 */
public abstract class Phase<Input, Output> {
    protected final String name;  // 阶段名称
    protected final Logger logger = LogManager.getLogger(getClass());
    
    public Phase(String name) {
        this.name = name;
    }
    
    /**
     * 执行阶段处理
     * 
     * @param input 输入
     * @return 输出结果
     * @throws CompileException 编译错误
     */
    public abstract Output process(Input input) throws CompileException;
    
    /**
     * 阶段前钩子
     */
    protected void beforeProcess(Input input) {
        logger.debug("开始执行阶段: {}", name);
    }
    
    /**
     * 阶段后钩子
     */
    protected void afterProcess(Output output) {
        logger.debug("阶段 {} 执行完成", name);
    }
}

/**
 * 词法分析阶段
 */
public class LexicalPhase extends Phase<InputStream, CommonTokenStream> {
    public LexicalPhase() {
        super("词法分析");
    }
    
    @Override
    public CommonTokenStream process(InputStream input) throws CompileException {
        try {
            CharStream charStream = CharStreams.fromStream(input);
            CymbolLexer lexer = new CymbolLexer(charStream);
            return new CommonTokenStream(lexer);
        } catch (IOException e) {
            throw new CompileException("读取输入失败", e);
        }
    }
}

/**
 * 语法分析阶段
 */
public class SyntaxPhase extends Phase<CommonTokenStream, ParseTree> {
    public SyntaxPhase() {
        super("语法分析");
    }
    
    @Override
    public ParseTree process(CommonTokenStream input) throws CompileException {
        CymbolParser parser = new CymbolParser(input);
        ParseTree tree = parser.file();
        
        // 错误处理
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new CompileException(
                "语法错误: 发现{}个语法错误", 
                parser.getNumberOfSyntaxErrors()
            );
        }
        
        return tree;
    }
}

/**
 * AST构建阶段
 */
public class ASTPhase extends Phase<ParseTree, ASTNode> {
    private final CymbolASTBuilder astBuilder;
    
    public ASTPhase() {
        super("AST构建");
        this.astBuilder = new CymbolASTBuilder();
    }
    
    @Override
    public ASTNode process(ParseTree input) throws CompileException {
        ASTNode astRoot = input.accept(astBuilder);
        logger.debug("AST节点数量: {}", countASTNodes(astRoot));
        return astRoot;
    }
    
    private int countASTNodes(ASTNode node) {
        // 递归统计AST节点数
        int count = 1;
        for (var child : node.getChildren()) {
            count += countASTNodes(child);
        }
        return count;
    }
}

/**
 * 符号解析阶段
 */
public class SymbolResolutionPhase extends Phase<ASTNode, SymbolTable> {
    public SymbolResolutionPhase() {
        super("符号解析");
    }
    
    @Override
    public SymbolTable process(ASTNode input) throws CompileException {
        var symbolDefiner = new LocalDefine();
        input.accept(symbolDefiner);
        var symbolTable = symbolDefiner.getSymbolTable();
        
        logger.debug("符号表构建完成，符号数量: {}", 
                     symbolTable.getAllSymbols().size());
        
        // 可以在这里进行符号表验证
        validateSymbolTable(symbolTable);
        
        return symbolTable;
    }
    
    private void validateSymbolTable(SymbolTable symbolTable) 
            throws CompileException {
        // 检查未声明的符号、重复定义等
        for (var symbol : symbolTable.getAllSymbols()) {
            if (symbol.isUndefined()) {
                throw new CompileException("符号未定义: {}", 
                                           symbol.getName());
            }
        }
    }
}

/**
 * 类型检查阶段
 */
public class TypeCheckingPhase extends Phase<Pair<ASTNode, SymbolTable>, Void> {
    public TypeCheckingPhase() {
        super("类型检查");
    }
    
    @Override
    public Void process(Pair<ASTNode, SymbolTable> input) 
            throws CompileException {
        ASTNode astRoot = input.getLeft();
        SymbolTable symbolTable = input.getRight();
        
        var typeChecker = new TypeChecker(symbolTable);
        astRoot.accept(typeChecker);
        
        // 检查类型错误
        if (typeChecker.hasErrors()) {
            throw new CompileException("类型检查发现{}个错误",
                                       typeChecker.getErrorCount());
        }
        
        logger.debug("类型检查完成，错误数: {}", 
                     typeChecker.getErrorCount());
        return null;
    }
}

/**
 * IR生成阶段
 */
public class IRGenerationPhase extends Phase<ASTNode, IRProgram> {
    public IRGenerationPhase() {
        super("IR生成");
    }
    
    @Override
    public IRProgram process(ASTNode input) throws CompileException {
        var irBuilder = new CymbolIRBuilder();
        input.accept(irBuilder);
        
        logger.debug("IR生成完成，基本块数量: {}",
                     irBuilder.prog.blockList.size());
        
        return irBuilder.prog;
    }
}

/**
 * 优化阶段
 */
public class OptimizationPhase extends Phase<IRProgram, IRProgram> {
    public OptimizationPhase() {
        super("优化");
    }
    
    @Override
    public IRProgram process(IRProgram input) throws CompileException {
        // 优化顺序：基本块优化 → CFG优化
        input.optimizeBasicBlock();  // 本地优化
        
        // CFG优化（每个函数）
        for (var block : input.blockList) {
            var cfgBuilder = new CFGBuilder();
            var cfg = cfgBuilder.buildCFG(block);
            
            // 应用优化Pass
            cfg.addOptimizer(new ControlFlowAnalysis<>());
            cfg.applyOptimizers();
            
            // 更新IR
            block = cfg.getLinearIR();
        }
        
        logger.debug("优化完成");
        return input;
    }
}

/**
 * 代码生成阶段
 */
public class CodeGenerationPhase extends Phase<IRProgram, String> {
    public CodeGenerationPhase() {
        super("代码生成");
    }
    
    @Override
    public String process(IRProgram input) throws CompileException {
        var assembler = new CymbolAssembler();
        
        // 合并所有函数的IR并生成代码
        var allIRNodes = new ArrayList<IRNode>();
        for (var block : input.blockList) {
            allIRNodes.addAll(block);
        }
        
        assembler.visit(allIRNodes);
        String bytecode = assembler.getAsmInfo();
        
        logger.debug("代码生成完成，字节码大小: {} 字节", 
                     bytecode.getBytes().length);
        
        return bytecode;
    }
}
```

#### 3. 编译器驱动器 (Pipeline模式)

使用Phase模式重新设计的编译器驱动器：

```java
/**
 * 编译器驱动器
 * 
 * <p>使用阶段模式管理完整的编译流水线。</p>
 */
public class CompilerDriver {
    private static final Logger logger = LogManager.getLogger(CompilerDriver.class);
    
    private final List<Phase<?, ?>> phases;  // 所有编译阶段
    private final CompilationContext context;  // 编译上下文
    
    public CompilerDriver() {
        this.phases = new ArrayList<>();
        this.context = new CompilationContext();
        initializePhases();
    }
    
    /**
     * 初始化所有编译阶段
     */
    private void initializePhases() {
        // 按顺序注册阶段
        phases.add(new LexicalPhase());
        phases.add(new SyntaxPhase());
        phases.add(new ASTPhase());
        phases.add(new SymbolResolutionPhase());
        phases.add(new TypeCheckingPhase());
        phases.add(new IRGenerationPhase());
        phases.add(new OptimizationPhase());
        phases.add(new CodeGenerationPhase());
        
        logger.info("编译器驱动器初始化完成，阶段数: {}", phases.size());
    }
    
    /**
     * 执行编译
     * 
     * @param inputFile 输入源文件
     * @param outputFile 输出字节码文件
     * @return 编译结果
     */
    public CompilationResult compile(String inputFile, String outputFile) {
        logger.info("开始编译: {} → {}", inputFile, outputFile);
        
        var result = new CompilationResult();
        result.startTime = System.currentTimeMillis();
        
        try (var inputStream = new FileInputStream(inputFile)) {
            // 阶段1：词法分析
            var lexicalPhase = (LexicalPhase) phases.get(0);
            var tokenStream = lexicalPhase.process(inputStream);
            result.phaseTimings.put("lexical", lexicalPhase.getTiming());
            
            // 阶段2：语法分析
            var syntaxPhase = (SyntaxPhase) phases.get(1);
            var parseTree = syntaxPhase.process(tokenStream);
            result.phaseTimings.put("syntax", syntaxPhase.getTiming());
            
            // 阶段3：AST构建
            var astPhase = (ASTPhase) phases.get(2);
            var astRoot = astPhase.process(parseTree);
            result.phaseTimings.put("ast", astPhase.getTiming());
            result.astNodeCount = countNodes(astRoot);
            
            // 阶段4：符号解析
            var symbolPhase = (SymbolResolutionPhase) phases.get(3);
            var symbolTable = symbolPhase.process(astRoot);
            result.phaseTimings.put("symbol", symbolPhase.getTiming());
            result.symbolCount = symbolTable.getAllSymbols().size();
            
            // 阶段5：类型检查
            var typePhase = (TypeCheckingPhase) phases.get(4);
            typePhase.process(Pair.of(astRoot, symbolTable));
            result.phaseTimings.put("type-check", typePhase.getTiming());
            
            // 阶段6：IR生成
            var irPhase = (IRGenerationPhase) phases.get(5);
            var irProgram = irPhase.process(astRoot);
            result.phaseTimings.put("ir-gen", irPhase.getTiming());
            result.basicBlockCount = irProgram.blockList.size();
            
            // 阶段7：优化
            var optPhase = (OptimizationPhase) phases.get(6);
            irProgram = optPhase.process(irProgram);
            result.phaseTimings.put("optimize", optPhase.getTiming());
            
            // 阶段8：代码生成
            var codegenPhase = (CodeGenerationPhase) phases.get(7);
            var bytecode = codegenPhase.process(irProgram);
            result.phaseTimings.put("codegen", codegenPhase.getTiming());
            result.bytecodeSize = bytecode.getBytes().length;
            
            // 保存结果
            saveToFile(bytecode, outputFile);
            result.outputFile = outputFile;
            result.success = true;
            
        } catch (CompileException e) {
            logger.error("编译失败: {}", e.getMessage());
            result.errors.add(e.getMessage());
            result.success = false;
        } catch (IOException e) {
            logger.error("文件错误: {}", e.getMessage());
            result.errors.add("文件错误: " + e.getMessage());
            result.success = false;
        } catch (Exception e) {
            logger.error("编译器内部错误", e);
            result.errors.add("内部错误: " + e.getMessage());
            result.success = false;
        }
        
        result.endTime = System.currentTimeMillis();
        result.totalTime = result.endTime - result.startTime;
        
        // 输出编译报告
        printCompilationReport(result);
        
        return result;
    }
    
    /**
     * 输出编译报告
     */
    private void printCompilationReport(CompilationResult result) {
        logger.info("\n========== 编译报告 ==========");
        logger.info("状态: {}", result.success ? "成功" : "失败");
        logger.info("总耗时: {} ms", result.totalTime);
        logger.info("\n--- 阶段耗时 ---");
        
        result.phaseTimings.forEach((phase, timing) -> {
            logger.info("  {}: {} ms", phase, timing);
        });
        
        if (result.success) {
            logger.info("\n--- 统计信息 ---");
            logger.info("AST节点数: {}", result.astNodeCount);
            logger.info("符号数: {}", result.symbolCount);
            logger.info("基本块数: {}", result.basicBlockCount);
            logger.info("字节码大小: {} bytes", result.bytecodeSize);
            logger.info("输出文件: {}", result.outputFile);
        } else {
            logger.info("\n--- 错误信息 ---");
            result.errors.forEach(error -> logger.error("  ✗ {}", error));
        }
        
        logger.info("==============================\n");
    }
}

/**
 * 编译结果
 */
class CompilationResult {
    public boolean success;  // 是否成功
    public long startTime;   // 开始时间
    public long endTime;     // 结束时间
    public long totalTime;   // 总耗时（ms）
    public Map<String, Long> phaseTimings = new HashMap<>();  // 各阶段耗时
    public List<String> errors = new ArrayList<>();  // 错误信息
    
    // 统计信息
    public int astNodeCount;
    public int symbolCount;
    public int basicBlockCount;
    public int bytecodeSize;
    public String outputFile;
}

/**
 * 编译异常
 */
class CompileException extends Exception {
    public CompileException(String message) {
        super(message);
    }
    
    public CompileException(String format, Object... args) {
        super(String.format(format, args));
    }
    
    public CompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

#### 4. 命令行接口 (CLI)

专业的编译器驱动器提供丰富的命令行选项：

```java
/**
 * 命令行参数解析器
 */
public class CompilerCLI {
    private static final Logger logger = LogManager.getLogger(CompilerCLI.class);
    
    public static void main(String[] args) {
        var options = parseCommandLine(args);
        
        if (options.showHelp) {
            printHelp();
            return;
        }
        
        if (options.showVersion) {
            printVersion();
            return;
        }
        
        // 执行编译
        var driver = new CompilerDriver();
        var result = driver.compile(options.inputFile, options.outputFile);
        
        // 如果编译成功且需要立即运行
        if (result.success && options.runImmediately) {
            runVM(options.outputFile);
        }
        
        // 退出码
        System.exit(result.success ? 0 : 1);
    }
    
    /**
     * 解析命令行参数
     */
    private static CompilerOptions parseCommandLine(String[] args) {
        var options = new CompilerOptions();
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-h", "--help" -> options.showHelp = true;
                case "-v", "--version" -> options.showVersion = true;
                case "-o" -> {
                    // 输出文件
                    if (i + 1 < args.length) {
                        options.outputFile = args[++i];
                    } else {
                        logger.error("选项 -o 需要参数");
                        System.exit(1);
                    }
                }
                case "-O" -> {
                    // 优化级别
                    if (i + 1 < args.length) {
                        options.optimizationLevel = Integer.parseInt(args[++i]);
                    }
                }
                case "-g" -> options.generateDebugInfo = true;
                case "--time" -> options.showTimings = true;
                case "--run", "-r" -> options.runImmediately = true;
                case "--dump-ast" -> options.dumpAST = true;
                case "--dump-ir" -> options.dumpIR = true;
                case "--dump-cfg" -> options.dumpCFG = true;
                case "-Werror" -> options.treatWarningsAsErrors = true;
                case "-Wall" -> options.showAllWarnings = true;
                default -> {
                    // 输入文件
                    if (arg.startsWith("-")) {
                        logger.warn("未知选项: {}", arg);
                    } else if (options.inputFile == null) {
                        options.inputFile = arg;
                    } else {
                        logger.error("多余参数: {}", arg);
                        System.exit(1);
                    }
                }
            }
        }
        
        // 设置默认值
        if (options.inputFile == null) {
            options.inputFile = "src/main/resources/t.cymbol";
        }
        
        if (options.outputFile == null) {
            // 根据输入文件名生成输出文件名
            String baseName = options.inputFile;
            if (baseName.endsWith(".cymbol")) {
                baseName = baseName.substring(0, baseName.length() - 7);
            }
            options.outputFile = baseName + ".vm";
        }
        
        return options;
    }
    
    /**
     * 打印帮助信息
     */
    private static void printHelp() {
        System.out.println("""
EP20 Cymbol Compiler - 完整编译器

用法: compiler [选项] <输入文件>

选项:
  -h, --help              显示帮助信息
  -v, --version           显示版本信息
  -o <文件>               指定输出文件
  -O <级别>               设置优化级别 (0-3, 默认: 1)
  -g                      生成调试信息
  --time, -t              显示各阶段耗时
  --run, -r               编译后立即运行
  --dump-ast              输出AST结构（调试用）
  --dump-ir               输出IR指令（调试用）
  --dump-cfg              输出CFG图（调试用）
  -Wall                   显示所有警告
  -Werror                 将警告视为错误

示例:
  compiler example.cymbol          # 编译example.cymbol到example.vm
  compiler -O2 -o out.vm in.cymbol # 使用级别2优化，输出到out.vm
  compiler --run program.cymbol    # 编译并立即运行
  compiler --dump-ast test.cymbol  # 编译并显示AST结构
            """);
    }
    
    /**
     * 打印版本信息
     */
    private static void printVersion() {
        System.out.println("EP20 Cymbol Compiler v2.0.0");
        System.out.println("基于 ANTLR4 和 Java 21");
        System.out.println("Copyright (c) 2025 teachfx");
    }
    
    /**
     * 运行EP18虚拟机
     */
    private static void runVM(String bytecodeFile) {
        try {
            logger.info("运行EP18 VM: {}", bytecodeFile);
            // 这里调用EP18虚拟机的执行接口
            // VM.execute(bytecodeFile);
        } catch (Exception e) {
            logger.error("VM执行失败: {}", e.getMessage());
        }
    }
}

/**
 * 编译选项
 */
class CompilerOptions {
    public String inputFile;           // 输入文件
    public String outputFile;          // 输出文件
    public int optimizationLevel = 1;  // 优化级别
    public boolean generateDebugInfo;  // 生成调试信息
    public boolean showTimings;        // 显示各阶段耗时
    public boolean runImmediately;     // 编译后立即运行
    public boolean showHelp;           // 显示帮助
    public boolean showVersion;        // 显示版本
    public boolean dumpAST;            // 输出AST
    public boolean dumpIR;             // 输出IR
    public boolean dumpCFG;            // 输出CFG
    public boolean treatWarningsAsErrors;  // 警告视为错误
    public boolean showAllWarnings;    // 显示所有警告
}
```

### 实战流程

#### 步骤1：创建测试程序

```bash
cat > /tmp/fib.cymbol << 'EOF'
// 计算斐波那契数列
int fibonacci(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

void main() {
    int n = 10;
    int result = fibonacci(n);
    print(result);  // 应该输出55
}
EOF
```

#### 步骤2：编译并查看详细报告

```bash
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep20

# 完整编译（带性能分析）
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.CompilerDriver" \
    -Dexec.args="--time /tmp/fib.cymbol /tmp/fib.vm"

# 预期输出：
# ========== 编译报告 ==========
# 状态: 成功
# 总耗时: 123 ms
#
# --- 阶段耗时 ---
#   lexical: 5 ms
#   syntax: 12 ms
#   ast: 8 ms
#   symbol: 15 ms
#   type-check: 23 ms
#   ir-gen: 28 ms
#   optimize: 18 ms
#   codegen: 14 ms
#
# --- 统计信息 ---
# AST节点数: 67
# 符号数: 8
# 基本块数: 5
# 字节码大小: 847 bytes
# 输出文件: /tmp/fib.vm
# ==============================
```

#### 步骤3：查看生成的字节码

```bash
cat /tmp/fib.vm

# 预期输出（优化的字节码）：
# function fibonacci:
# fibonacci:
#     iconst 1
#     load 0           # 加载参数n
#     ile              # n <= 1?
#     brf L0           # 如果n > 1跳转到L0
#     load 0           # return n
#     ret
# L0:
#     load 0           # n
#     iconst 1
#     isub             # n - 1
#     call fibonacci() # fibonacci(n-1)
#     load 0           # n
#     iconst 2
#     isub             # n - 2
#     call fibonacci() # fibonacci(n-2)
#     iadd             # 相加
#     ret
#
# function main:
# main:
#     iconst 10        # n = 10
#     store 0
#     load 0           # 加载n
#     call fibonacci() # fibonacci(n)
#     store 1          # result
#     load 1
#     print
#     halt
```

#### 步骤4：调试模式（查看中间表示）

```bash
# 查看AST结构
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.CompilerCLI" \
    -Dexec.args="--dump-ast /tmp/fib.cymbol"

# 预期输出（AST简化视图）：
# CompilationUnit
# └── FunctionDecl: fibonacci
#     ├── Parameters: [n]
#     └── Block
#         └── IfStmt
#             ├── Condition: BinaryExpr (n <= 1)
#             ├── ThenBranch: ReturnStmt (n)
#             └── ElseBranch: ReturnStmt (fibonacci(n-1) + fibonacci(n-2))
#
# └── FunctionDecl: main
#     └── Block
#         └── ...

# 查看IR（三地址码）
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.CompilerCLI" \
    -Dexec.args="--dump-ir /tmp/fib.cymbol"

# 预期输出（简化IR）：
# function fibonacci:
# L0:
#     t0 = n
#     t1 = 1
#     t2 = t0 <= t1
#     if t2 goto L1
#     t3 = n - 1
#     t4 = call fibonacci(t3)
#     t5 = n - 2
#     t6 = call fibonacci(t5)
#     t7 = t4 + t6
#     return t7
# L1:
#     return n
#
# function main:
# ...
```

#### 步骤5：运行EP18虚拟机验证

```bash
# 编译后立即运行
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18

mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.driver.VMDriver" \
    -Dexec.args="/tmp/fib.vm"

# 预期输出：
# 55
```

### 故障排查与调试

#### 问题1：编译失败，找不到符号

**症状**：
```
错误: 符号未定义: someVariable
符号表大小: 12
```

**排查步骤**：
1. 使用`--dump-ast`查看AST结构，确认变量声明是否存在
2. 检查符号解析阶段的日志，查看变量作用域
3. 确认变量名拼写是否正确（大小写敏感）
4. 检查变量声明位置是否在引用位置之前（执行顺序）

**调试代码**：
```java
// 在SymbolResolutionPhase中添加详细日志
@Override
public SymbolTable process(ASTNode input) throws CompileException {
    var symbolDefiner = new LocalDefine();
    input.accept(symbolDefiner);
    var symbolTable = symbolDefiner.getSymbolTable();
    
    // 添加符号表详细日志
    logger.debug("=== 符号表内容 ===");
    for (var symbol : symbolTable.getAllSymbols()) {
        logger.debug("符号: {} ({}), 作用域: {}, 已定义: {}",
                     symbol.getName(),
                     symbol.getClass().getSimpleName(),
                     symbol.getScope(),
                     !symbol.isUndefined());
    }
    logger.debug("================");
    
    return symbolTable;
}
```

#### 问题2：类型检查错误

**症状**：
```
错误: 类型检查发现3个错误
```

**排查步骤**：
1. 启用`-Wall`查看详细警告信息
2. 使用`--dump-ast`定位类型不匹配的位置
3. 检查二元运算符的操作数类型（如字符串+整数）
4. 验证函数调用参数类型与声明是否匹配
5. 查看类型检查器的详细日志

**调试技巧**：
```bash
# 查看详细类型检查日志
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.CompilerCLI" \
    -Dexec.args="-Wall --dump-ast /tmp/test.cymbol"
```

#### 问题3：生成的字节码运行错误

**症状**：
```
EP18 VM错误: 栈溢出 / 指令错误
```

**排查步骤**：
1. 使用`--dump-ir`查看生成的IR是否正确
2. 对比正确程序的IR结构
3. 检查函数调用的参数传递和返回值处理
4. 验证跳转指令的标签是否正确
5. 使用EP18 VM的调试模式单步执行

**调试代码**：
```java
// 在代码生成阶段添加详细日志
@Override
public String process(IRProgram input) throws CompileException {
    var assembler = new CymbolAssembler();
    var allIRNodes = new ArrayList<IRNode>();
    
    for (var block : input.blockList) {
        logger.debug("处理函数块: {} ({} 条指令)",
                     block.getFunctionName(),
                     block.size());
        allIRNodes.addAll(block);
    }
    
    assembler.visit(allIRNodes);
    String bytecode = assembler.getAsmInfo();
    
    // 保存中间IR（调试用）
    saveIRDebug(allIRNodes, "debug_ir.txt");
    
    return bytecode;
}
```

#### 问题4：性能问题（编译慢）

**症状**：
```
编译报告: 总耗时: 5234 ms  # 太慢了！
```

**排查步骤**：
1. 使用`--time`查看各阶段耗时
2. 识别瓶颈阶段（通常是优化或符号解析）
3. 检查算法复杂度（如CFG构建、数据流分析）
4. 添加缓存机制（如符号查找、类型检查）
5. 使用Java Profiler定位热点方法

**优化示例**：
```java
// 在SymbolTable中添加符号缓存
public class SymbolTable {
    private final Map<String, Symbol> symbolCache = new HashMap<>();
    
    public Symbol resolve(String name) {
        // 先查缓存
        if (symbolCache.containsKey(name)) {
            return symbolCache.get(name);
        }
        
        // 缓存未命中，正常查找
        Symbol symbol = doResolve(name);
        symbolCache.put(name, symbol);
        return symbol;
    }
}
```

#### 问题5：内存溢出（OOM）

**症状**：
```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

**排查步骤**：
1. 检查递归编译的终止条件（如递归函数）
2. 优化数据结构内存占用（如使用Stream代替List）
3. 增加JVM内存限制：`-Xmx2g`
4. 检查是否有内存泄漏（未释放的符号表、IR、CFG）
5. 使用大文件测试，定位具体位置

**解决方案**：
```bash
# 增加JVM堆内存
export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
mvn exec:java -Dexec... (省略)
```

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI协助完成端到端编译器的开发，需要精心设计上下文：

**源码文件（按阅读顺序）：**

1. `ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java`
   - 作用：编译器主入口
   - 关键方法：`main()`，完整编译流程
   - 重要性：展示各阶段集成顺序

2. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ast/CymbolASTBuilder.java`
   - 作用：AST构建器
   - 重要性：前端核心，理解AST结构

3. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/symtab/LocalDefine.java`
   - 作用：符号解析
   - 重要性：理解符号表构建

4. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java`
   - 作用：IR生成器
   - 重要性：IR构建逻辑

5. `ep20/src/main/java/org/teachfx/antlr4/ep20/CompilerDriver.java`
   - 作用：编译器驱动（重构后的版本）
   - 重要性：阶段管理、错误处理

6. `ep20/src/main/java/org/teachfx/antlr4/ep20/CompilerCLI.java`
   - 作用：命令行接口
   - 重要性：用户交互、选项解析

7. `ep20/src/test/java/org/teachfx/antlr4/ep20/IntegrationTest.java`
   - 作用：集成测试
   - 重要性：验证完整流程

**文档文件：**

1. `ep20/README.md`
   - 作用：EP20模块文档
   - 相关部分：编译流程、架构设计

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：Pass设计、测试规范

### Prompt模板（给AI用）

#### 类型A：添加新的编译阶段Prompt模板

```markdown
请为EP20编译器添加一个新的编译阶段：{阶段名称}。

任务目标：
- 在{前端/中端/后端}添加新的处理阶段
- 处理{任务描述}
- 与现有阶段良好集成

具体要求：
1. 设计阶段接口
   - 继承Phase<Input, Output>基类
   - 实现process()方法
   - 定义输入/输出类型
   - 添加错误处理

2. 集成到流水线
   - 在CompilerDriver的initializePhases()中注册
   - 确定正确的执行顺序（参考相邻阶段）
   - 处理与前驱阶段的数据传递

3. 添加测试
   - 单元测试：测试阶段的独立功能
   - 集成测试：测试在完整流程中的行为
   - 边界测试：测试错误处理

参考上下文文件：
- 阶段基类：ep20/src/main/java/org/teachfx/antlr4/ep20/Phase.java
- 驱动器：ep20/src/main/java/org/teachfx/antlr4/ep20/CompilerDriver.java
- 编译器入口：ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java
- 测试：ep20/src/test/java/org/teachfx/antlr4/ep20/IntegrationTest.java

约束条件：
- 不破坏现有编译流程
- 保持代码风格一致（遵循AGENTS.md）
- 所有测试必须通过
- 时间复杂度控制在O(N)或O(N log N)
- 添加适当的日志输出

期望输出：
1. 阶段实现代码
2. 单元测试代码
3. 集成测试代码
4. 使用示例和预期行为说明
5. 性能考虑和潜在优化点
```

#### 类型B：增强错误处理Prompt模板

```markdown
请为EP20编译器增强错误处理系统：{具体需求}。

任务目标：
- 实现统一的错误收集和报告机制
- 支持错误分级（错误/警告/提示）
- 生成用户友好的错误信息

具体要求：
1. 错误收集
   - 创建ErrorIssuer类（如果还不存在）
   - 支持多阶段的错误收集
   - 维护错误上下文（文件名、行号、列号）

2. 错误报告
   - 生成格式化的错误信息
   - 支持语法高亮（如果可能）
   - 提供错误建议和修复提示

3. 集成到各阶段
   - 修改{相关阶段}以使用新的错误处理
   - 保持向后兼容性
   - 提供配置选项（是否停止在第一个错误）

4. 测试
   - 单元测试：测试错误收集和报告
   - 集成测试：测试完整编译流程的错误处理
   - 使用真实错误案例验证

参考上下文文件：
- 编译器入口：ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java
- 驱动器：ep20/src/main/java/org/teachfx/antlr4/ep20/CompilerDriver.java
- 各阶段实现（如TypeChecker、LocalDefine）
- 测试：ep20/src/test/java/org/teachfx/antlr4/ep20/ErrorHandlingTest.java

约束条件：
- 不破坏现有错误处理（如果已存在）
- 保持错误信息的准确性和可读性
- 所有测试必须通过
- 添加性能测试（错误处理不应显著影响编译速度）

期望输出：
1. ErrorIssuer实现代码
2. 各阶段的错误处理集成代码
3. 单元测试和集成测试
4. 错误信息格式示例
5. 使用文档
```

### AI 应该做 / 不该做

**✅ AI 允许做的事情：**

1. **重构编译流程**
   - ✅ 可以：将monolithic的main()重构为Phase模式
   - ✅ 可以：添加统一的错误处理
   - ❌ 不能：改变各阶段的执行顺序（无充分理由）

2. **增强CLI接口**
   - ✅ 可以：添加命令行选项（--dump-ast等）
   - ✅ 可以：改进帮助信息和错误提示
   - ✅ 可以：添加彩色输出支持
   - ❌ 不能：删除现有选项（破坏兼容性）

3. **优化阶段性能**
   - ✅ 可以：添加缓存和增量编译
   - ✅ 可以：并行处理独立阶段
   - ❌ 不能：牺牲正确性换取性能

4. **改进诊断信息**
   - ✅ 可以：改进错误信息的可读性
   - ✅ 可以：添加错误位置高亮
   - ✅ 可以：提供修复建议
   - ❌ 不能：改变错误语义（如无理由）

**❌ AI 禁止做的事情：**

1. **破坏编译正确性**
   - ❌ 不允许：改变阶段间数据流而不验证
   - ❌ 不允许：删除关键的验证步骤
   - 原因：编译器的首要目标是生成正确的代码

2. **过度设计**
   - ❌ 不允许：引入不必要的抽象层
   - ❌ 不允许：添加用户不需要的复杂选项
   - 原因：保持简单和可维护

3. **忽略错误处理**
   - ❌ 不允许：假定输入总是合法
   - ❌ 不允许：忽略异常处理
   - 原因：健壮性是生产级编译器的关键

4. **破坏性改变**
   - ❌ 不允许：修改公共API而不提供迁移路径
   - ❌ 不允许：改变默认行为（无通知）
   - 原因：向后兼容性很重要

### 验证与回滚策略

#### 自动化验证

**步骤1：编译验证**
```bash
cd ep20
mvn clean compile -DskipTests
# 预期: [INFO] BUILD SUCCESS
```

**步骤2：单元测试**
```bash
# 运行各阶段单元测试
mvn test -Dtest=*Phase*Test
mvn test -Dtest=*Driver*Test

# 预期: 所有测试通过
```

**步骤3：集成测试**
```bash
# 运行完整编译流程测试
mvn test -Dtest=IntegrationTest

# 预期: 测试通过，包含多个Cymbol程序
```

**步骤4：回归测试**
```bash
# 使用已知正确的程序测试
mvn exec:java -Dexec.args="src/main/resources/t.cymbol"

# 验证输出是否符合预期
```

**步骤5：性能基准测试**
```bash
# 比较重构前后的编译速度
# 使用大文件测试（1000+行代码）
time mvn exec:java -Dexec.args="large_program.cymbol"

# 预期: 性能不下降超过10%
```

#### 手工检查点

**检查点1：编译流程完整性**
- [ ] 所有阶段按正确顺序执行
- [ ] 阶段间数据传递正确
- [ ] 错误在中间阶段被正确捕获
- [ ] 编译报告包含所有必要信息

**检查点2：错误处理**
- [ ] 语法错误被正确报告
- [ ] 语义错误被正确报告
- [ ] 错误位置信息准确（行号、列号）
- [ ] 错误恢复机制正常工作

**检查点3：CLI功能**
- [ ] 所有命令行选项工作正常
- [ ] 帮助信息清晰完整
- [ ] 错误提示用户友好
- [ ] 调试选项（--dump-*）输出正确

**检查点4：代码质量**
- [ ] 遵循AGENTS.md规范
- [ ] 有充分的注释和文档
- [ ] 日志输出合理使用
- [ ] 测试覆盖率>80%

**检查点5：性能**
- [ ] 编译速度满足要求
- [ ] 内存占用合理
- [ ] 大文件编译不OOM
- [ ] 增量编译有缓存优化

#### 回滚策略

**如果新驱动器引入Bug:**

```bash
# 方法1：恢复单个文件
git checkout -- ep20/src/main/java/org/teachfx/antlr4/ep20/CompilerDriver.java

# 验证回归测试通过
mvn test -Dtest=IntegrationTest
```

**如果重构破坏现有功能:**

```bash
# 方法2：整体回滚到上一个稳定版本
git stash  # 暂存当前工作
mvn test   # 验证测试通过

# 或者
git reset --hard <stable_commit>
```

**如果CLI改变导致脚本失败:**

```bash
# 方法3：提供兼容性shim
# 创建wrapper脚本保持向后兼容
#!/bin/bash
# compiler-compat.sh
# 将旧选项映射到新选项
exec compiler "$@"
```

**性能回退应急方案:**

```bash
# 如果新驱动器太慢，临时使用旧Compiler
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.OldCompiler" \
    -Dexec.args="..."
```

## 练习题

### 练习1：跟踪完整编译流程（手工实现版）

难度：⭐⭐⭐☆☆
预计时间：60-90 分钟

题目描述：
给定以下递归计算阶乘的Cymbol源代码，手工跟踪整个编译流程，记录每个阶段的中间表示：

```c
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void main() {
    int x = 5;
    int result = factorial(x);
    print(result);
}
```

要求：
1. 手工写出Token序列（至少20个Token，包括类型）
2. 绘制ParseTree的完整结构（至少3层深度）
3. 列出AST的所有节点及其父子关系
4. 手工生成factorial函数的IR（三地址码形式，至少5条指令）
5. 手工生成main函数的IR（至少3条指令）
6. 绘制factorial()函数的CFG（包含分支）
7. 应用控制流优化（合并冗余块，删除无用跳转）
8. 写出最终生成的字节码（EP18 VM指令）
9. 记录每个阶段的预期输出和数据结构状态

验收标准：
- [ ] Token序列完整且类型正确
- [ ] ParseTree结构清晰，层级正确
- [ ] AST节点关系正确
- [ ] IR指令序列符合三地址码格式
- [ ] CFG正确反映控制流（if分支）
- [ ] 优化后的IR正确消除冗余
- [ ] EP18 VM字节码语法正确
- [ ] 每个阶段的状态描述清晰

### 练习2：实现性能分析工具（AI协作版）

难度：⭐⭐⭐⭐☆
预计时间：90-120 分钟

题目描述：
为EP20编译器添加性能分析工具，测量每个编译阶段的耗时和内存使用：

```java
class CompilerProfiler {
    // 需要实现:
    // - phaseTimings: Map<String, Long> 各阶段耗时
    // - memoryUsage: Map<String, Long> 各阶段内存使用
    // - startPhase(String phaseName): 开始计时
    // - endPhase(String phaseName): 结束计时
    // - generateReport(): 生成性能报告
}
```

AI协作要求：
1. 设计上下文：提供CompilerDriver.java、各Phase实现，明确性能指标需求
2. 设计Prompt：使用类型A模板，明确需要测量时间和内存，生成HTML报告
3. 验证AI输出：检查性能数据是否准确，报告格式是否美观
4. 理解AI代码：确保你能解释每个性能指标的含义和测量方法

验收标准：
- [ ] AI生成的代码能完整编译（无lsp错误）
- [ ] 正确测量各阶段耗时（误差<5ms）
- [ ] 正确测量内存使用（使用Runtime.getRuntime()）
- [ ] 生成HTML或Markdown格式的性能报告
- [ ] 报告包含统计图表（可使用简单SVG）
- [ ] 测试多种输入规模（小程序、大程序）
- [ ] 所有测试通过
- [ ] 你能解释AI生成的每行代码

### 练习3：实现增量编译（高级挑战）

难度：⭐⭐⭐⭐⭐
预计时间：120-180 分钟

题目描述：
实现增量编译功能：当源文件修改时，只重新编译受影响的部分，提升大型项目的编译速度。

关键功能：
1. 文件哈希：记录每个源文件的MD5，只编译修改过的文件
2. 依赖分析：分析函数/变量依赖关系，确定哪些符号受影响
3. 缓存IR：为未修改的函数直接重用上次编译的IR
4. 智能链接：将新编译的函数与缓存的函数合并

步骤：
1. 创建DependencyAnalyzer类，分析符号依赖关系
2. 创建CompileCache类，管理编译缓存（文件哈希→IR）
3. 创建IncrementalCompiler类，继承CompilerDriver
4. 重写compile()方法，添加增量逻辑
5. 添加缓存持久化（保存到磁盘）
6. 编写全面测试

与AI协作：使用类型B Prompt，让AI帮助设计依赖分析算法，手工编码实现缓存管理

验收标准：
- [ ] 正确识别修改过的文件
- [ ] 正确分析符号依赖关系
- [ ] 未修改部分从缓存加载（不重新编译）
- [ ] 性能测试：大项目第二次编译速度提升80%+
- [ ] 正确性测试：结果与全量编译一致
- [ ] 缓存持久化正常工作
- [ ] 边界测试：处理文件删除、新增、重命名
- [ ] 并发测试：多文件同时修改

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **编译器流水线架构**
   - 理解了前端、中端、后端的分层设计
   - 掌握了阶段模式（Phase Pattern）
   - 学会了阶段间的数据传递和依赖管理

2. **错误处理和诊断**
   - 实现了统一的错误收集和报告机制
   - 学会了错误分级（错误/警告/提示）
   - 掌握了错误定位和上下文信息

3. **编译器驱动和CLI**
   - 设计了专业的命令行接口
   - 实现了丰富的编译选项
   - 学会了用户友好的输出格式

4. **性能分析和优化**
   - 掌握了各阶段性能测量方法
   - 学会了识别性能瓶颈
   - 了解了增量编译的基本原理

5. **集成测试和调试**
   - 掌握了端到端测试技术
   - 学会了编译器调试方法
   - 了解了回归测试的重要性

### 【你现在站在】:
```
... → [代码生成] → ✅ [端到端集成] → [生产级工具]
```

**当前在编译器流水线的位置**：
- 你已经完成了完整的编译器实现
- 编译器可以处理真实世界的程序
- 可以进行性能分析和优化
- 下一阶段是高级优化技术

### 下一章预告

第17章将聚焦于**SSA和数据流分析**，你将学习：
- 静态单赋值（SSA）形式的原理和优势
- 支配关系（Dominance）和支配边界
- 数据流分析框架
- 活跃变量分析和到达定义分析
- 为全局优化奠定基础

**准备**：为了学习下一章，建议：
- [ ] 复习控制流图（CFG）的基本概念
- [ ] 理解基本块的定义和性质
- [ ] 预习集合论（交集、并集）
- [ ] 运行EP21的SSA相关代码（如果有）
- [ ] 思考如何将数据流分析集成到编译流水线

继续加油！你已经掌握了编译器工程的核心技能，高级优化技术将让你的编译器达到生产级质量！

---

**本章关联代码**：
- 编译器主类：`ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java`
- 各阶段实现：`ep20/src/main/java/org/teachfx/antlr4/ep20/pass/`
- 集成测试：`ep20/src/test/java/org/teachfx/antlr4/ep20/IntegrationTest.java`

**验证命令**：
```bash
cd ep20
mvn test -Dtest=IntegrationTest
# 预期: 所有集成测试通过
```

**性能检查**：
```bash
# 使用--time检查各阶段耗时
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.CompilerCLI" \
    -Dexec.args="--time /tmp/test.cymbol"
```
