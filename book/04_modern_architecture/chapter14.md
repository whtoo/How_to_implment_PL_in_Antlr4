# 第14章：语义分析与属性文法

## 本章概述

本章聚焦于编译器前端的最终阶段——语义分析（Semantic Analysis）。通过学习本章，你将掌握如何使用属性文法驱动语义分析，如何整合符号表和类型系统进行完整的语义检查，以及如何生成有意义的错误信息。

【你现在站在哪】:
```
... → [语法分析] → [AST构建] → [符号表] → [类型系统] → ✅ 语义分析 → [IR生成] → ...
```

## 动机与真实场景

真实场景：你的编译器已经能够解析语法并构建AST，但用户报告程序存在语义错误但编译器没有检测出来。例如，一个函数被调用时传递了错误数量的参数，或者一个变量在使用前没有被初始化。

具体问题或挑战：
- 编译器只能检查语法错误，无法检测语义错误
- 错误信息不够详细，用户难以理解问题所在
- 语义检查分散在代码各处，难以维护和扩展
- 没有系统性的方法进行语义分析

如果缺少本章的能力，你将面临：
- 编译器接受无效程序，导致运行时错误
- 用户体验差，错误信息不清晰
- 代码难以维护，语义检查逻辑混乱
- 难以添加新的语义检查规则

本章将教你如何：
- 使用属性文法组织语义分析
- 实现完整的语义检查流程
- 生成详细且有用的错误信息
- 构建可维护和可扩展的语义分析框架

## 人类工程师线：技术与实现

### 核心概念

**语义分析**是编译器前端的关键阶段，它在语法分析完成后，检查程序是否满足语言的语义规则。与语法分析关注"程序的结构是否正确"不同，语义分析关注"程序的意义是否正确"。

通俗解释：如果语法分析是检查一篇文章的句子结构是否正确（主语+谓语+宾语），那么语义分析就是检查文章的内容是否有意义。例如，"苹果吃我"这句话语法正确，但语义错误。编译器需要检测这类"说的都对，但没一件正经事"的问题。

[图1：语义分析在编译器流水线中的位置]
```
源代码
   │
   ▼
词法分析 → Token流
   │
   ▼
语法分析 → AST（ParseTree → AST）
   │
   ▼
语义分析 → 验证AST的语义正确性
   ├─→ 符号表构建
   ├─→ 类型检查
   ├─→ 作用域解析
   └─→ 各种语义规则检查
   │
   ▼
IR生成 → 带类型和符号信息的IR
```

类比理解：语义分析就像一个严格的编辑：
- **拼写检查** = 符号解析（确保每个名字都有定义）
- **语法检查** = 类型检查（确保类型匹配）
- **逻辑检查** = 控制流分析（确保没有死代码、无限循环）
- **风格检查** = 编码规范检查

**属性文法**是一种形式化的语义分析方法，它为语法规则的产生式附加属性计算规则。属性文法将语义分析从"程序代码"转变为"数学推导"，使语义分析更加系统化和可验证。

[图2：属性文法示例]
```
产生式：          语义规则（属性方程）：
─────────────────────────────────────────────────
Decl → Type ID   { 
    defineSymbol(ID.text, Type.type) 
}

Expr → Expr + Expr {
    Expr.type = checkTypes(Expr1.type, Expr2.type)
    Expr.code = Expr1.code + Expr2.code + "iadd"
}
```

相关概念：
- **综合属性（Synthesized Attribute）**：由子节点属性计算父节点属性（自底向上）
- **继承属性（Inherited Attribute）**：由父节点或兄弟节点属性计算子节点属性（自顶向下）
- **属性方程（Attribute Grammar）**：定义属性如何计算的规则
- **语法制导翻译（Syntax-Directed Translation）**：使用属性文法驱动代码生成

### 与仓库 EP 的对应关系

对应 EP：EP10-EP13

目录结构：
```
common/
├── src/main/java/org/teachfx/antlr4/common/
│   ├── semantic/
│   │   ├── SemanticAnalyzer.java    // 语义分析主类
│   │   ├── SymbolResolver.java      // 符号解析器
│   │   ├── TypeChecker.java         // 类型检查器
│   │   ├── FlowAnalyzer.java        // 控制流分析器
│   │   └── ErrorCollector.java      // 错误收集器
│   └── ast/
│       ├── ASTNode.java             // AST节点基类
│       └── visitors/
│           ├── BaseASTVisitor.java  // 基础Visitor
│           └── SemanticASTVisitor.java // 语义分析Visitor
├── src/test/java/org/teachfx/antlr4/common/
│   └── semantic/
│       ├── SemanticAnalyzerTest.java // 语义分析测试
│       └── ErrorCollectorTest.java   // 错误收集器测试
└── docs/
    └── semantic_analysis_design.md   // 语义分析设计文档
```

关键类/方法说明：

**SemanticAnalyzer** - 语义分析主类
```java
/**
 * 语义分析主类
 * 
 * <p>SemanticAnalyzer是语义分析阶段的入口类，它协调各个语义分析组件，
 * 完成符号解析、类型检查、控制流分析等任务。</p>
 */
public class SemanticAnalyzer {
    /** 符号表 */
    private final SymbolTable symbolTable;
    
    /** 类型系统 */
    private final TypeSystem typeSystem;
    
    /** 错误收集器 */
    private final ErrorCollector errorCollector;
    
    /** 符号解析器 */
    private SymbolResolver symbolResolver;
    
    /** 类型检查器 */
    private TypeChecker typeChecker;
    
    /** 控制流分析器 */
    private FlowAnalyzer flowAnalyzer;
    
    /** AST根节点 */
    private ASTNode astRoot;
    
    /** 分析是否完成 */
    private boolean analysisComplete;
    
    /** 语义分析结果 */
    private SemanticAnalysisResult result;
    
    /**
     * 构造函数
     * 
     * @param typeSystem 类型系统
     */
    public SemanticAnalyzer(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
        this.symbolTable = new SymbolTable();
        this.errorCollector = new ErrorCollector();
        this.analysisComplete = false;
    }
    
    /**
     * 执行完整的语义分析
     * 
     * @param astRoot AST根节点
     * @return 语义分析结果
     */
    public SemanticAnalysisResult analyze(ASTNode astRoot) {
        if (astRoot == null) {
            throw new IllegalArgumentException("AST root cannot be null");
        }
        
        this.astRoot = astRoot;
        this.result = new SemanticAnalysisResult();
        
        try {
            // 第一阶段：符号解析
            performSymbolResolution();
            
            // 第二阶段：类型检查
            performTypeChecking();
            
            // 第三阶段：控制流分析
            performFlowAnalysis();
            
            // 收集分析结果
            result.setSuccessful(errorCollector.getErrorCount() == 0);
            result.setErrorCount(errorCollector.getErrorCount());
            result.setWarningCount(errorCollector.getWarningCount());
            result.setSymbolTable(symbolTable);
            
            analysisComplete = true;
            
        } catch (SemanticAnalysisException e) {
            result.setSuccessful(false);
            result.addError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 第一阶段：符号解析
     * 
     * <p>遍历AST，构建符号表，解析所有符号引用。</p>
     */
    private void performSymbolResolution() {
        logger.info("Starting symbol resolution...");
        long startTime = System.currentTimeMillis();
        
        symbolResolver = new SymbolResolver(symbolTable, errorCollector);
        symbolResolver.setTypeSystem(typeSystem);
        
        // 遍历AST进行符号解析
        astRoot.accept(symbolResolver);
        
        // 收集未解析的引用
        List<UnresolvedReference> unresolved = symbolResolver.getUnresolvedReferences();
        for (var ref : unresolved) {
            errorCollector.reportError(
                "Undefined symbol: '" + ref.getName() + "'",
                ref.getLine(),
                ref.getColumn()
            );
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Symbol resolution completed in {}ms", elapsed);
        result.setSymbolResolutionTime(elapsed);
    }
    
    /**
     * 第二阶段：类型检查
     * 
     * <p>检查所有表达式的类型是否兼容，
     * 验证赋值、函数调用等操作的类型正确性。</p>
     */
    private void performTypeChecking() {
        logger.info("Starting type checking...");
        long startTime = System.currentTimeMillis();
        
        typeChecker = new TypeChecker(symbolTable, errorCollector);
        typeChecker.setTypeSystem(typeSystem);
        
        // 遍历AST进行类型检查
        astRoot.accept(typeChecker);
        
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Type checking completed in {}ms", elapsed);
        result.setTypeCheckingTime(elapsed);
    }
    
    /**
     * 第三阶段：控制流分析
     * 
     * <p>检查程序的控制流属性，包括：
     * - 所有路径都返回值（对于非void函数）
     * - 变量使用前已初始化
     * - 没有无法到达的代码</p>
     */
    private void performFlowAnalysis() {
        logger.info("Starting flow analysis...");
        long startTime = System.currentTimeMillis();
        
        flowAnalyzer = new FlowAnalyzer(symbolTable, errorCollector);
        
        // 遍历AST进行控制流分析
        astRoot.accept(flowAnalyzer);
        
        // 收集控制流警告
        for (var warning : flowAnalyzer.getWarnings()) {
            errorCollector.reportWarning(warning.getMessage(), warning.getLine());
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("Flow analysis completed in {}ms", elapsed);
        result.setFlowAnalysisTime(elapsed);
    }
    
    /**
     * 获取符号表
     * 
     * @return 符号表
     */
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
    
    /**
     * 获取错误收集器
     * 
     * @return 错误收集器
     */
    public ErrorCollector getErrorCollector() {
        return errorCollector;
    }
    
    /**
     * 获取分析结果
     * 
     * @return 语义分析结果
     */
    public SemanticAnalysisResult getResult() {
        if (!analysisComplete) {
            throw new IllegalStateException("Analysis not yet performed");
        }
        return result;
    }
}
```

**SymbolResolver** - 符号解析器（增强版）
```java
/**
 * 符号解析器
 * 
 * <p>使用Visitor模式遍历AST，完成符号表的构建和符号引用解析。
 * 这是语义分析的第一阶段。</p>
 */
public class SymbolResolver extends BaseASTVisitor<Void> {
    /** 符号表 */
    protected final SymbolTable symbolTable;
    
    /** 错误收集器 */
    protected final ErrorCollector errorCollector;
    
    /** 类型系统 */
    protected TypeSystem typeSystem;
    
    /** 当前函数符号 */
    protected FunctionSymbol currentFunction;
    
    /** 未解析的引用 */
    protected final List<UnresolvedReference> unresolvedReferences;
    
    /** 作用域深度（用于调试） */
    protected int scopeDepth;
    
    /**
     * 构造函数
     * 
     * @param symbolTable 符号表
     * @param errorCollector 错误收集器
     */
    public SymbolResolver(SymbolTable symbolTable, ErrorCollector errorCollector) {
        this.symbolTable = symbolTable;
        this.errorCollector = errorCollector;
        this.unresolvedReferences = new ArrayList<>();
        this.scopeDepth = 0;
    }
    
    /**
     * 设置类型系统
     * 
     * @param typeSystem 类型系统
     */
    public void setTypeSystem(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }
    
    @Override
    public Void visitFile(ASTFile node) {
        // 进入文件作用域
        symbolTable.enterScope("file", node.getLine());
        scopeDepth++;
        
        super.visitFile(node);
        
        scopeDepth--;
        symbolTable.exitScope();
        
        return null;
    }
    
    @Override
    public Void visitFunctionDecl(ASTFunctionDecl node) {
        String funcName = node.getName();
        
        // 收集参数类型
        List<Type> paramTypes = new ArrayList<>();
        for (var param : node.getParameters()) {
            paramTypes.add(param.getType());
        }
        
        // 创建函数符号
        FunctionSymbol funcSymbol = new FunctionSymbol(
            funcName,
            node.getReturnType(),
            paramTypes
        );
        funcSymbol.setDefinitionNode(node);
        
        // 进入函数作用域
        symbolTable.enterScope(funcName, node.getLine());
        scopeDepth++;
        
        // 保存前一个当前函数
        FunctionSymbol previousFunction = currentFunction;
        currentFunction = funcSymbol;
        
        // 定义参数
        for (int i = 0; i < node.getParameters().size(); i++) {
            var param = node.getParameters().get(i);
            VariableSymbol varSymbol = new VariableSymbol(param.getName(), param.getType());
            varSymbol.setDefinitionNode(param);
            symbolTable.define(varSymbol);
        }
        
        // 递归处理函数体
        super.visitFunctionDecl(node);
        
        // 恢复前一个当前函数
        currentFunction = previousFunction;
        
        scopeDepth--;
        symbolTable.exitScope();
        
        // 在全局作用域定义函数
        symbolTable.define(funcSymbol);
        
        return null;
    }
    
    @Override
    public Void visitVariableDecl(ASTVariableDecl node) {
        String varName = node.getName();
        Type varType = node.getType();
        
        // 创建变量符号
        VariableSymbol varSymbol = new VariableSymbol(varName, varType);
        varSymbol.setDefinitionNode(node);
        
        // 定义到当前作用域
        try {
            symbolTable.define(varSymbol);
        } catch (SymbolAlreadyDefinedException e) {
            // 重复定义错误
            errorCollector.reportError(
                "Duplicate declaration of variable: '" + varName + "'",
                node.getLine(),
                node.getColumn()
            );
        }
        
        // 递归处理初始值
        super.visitVariableDecl(node);
        
        return null;
    }
    
    @Override
    public Void visitIdentifier(ASTIdentifier node) {
        String name = node.getName();
        
        // 解析符号
        Symbol symbol = symbolTable.resolve(name);
        
        if (symbol == null) {
            // 符号未定义
            errorCollector.reportError(
                "Undefined symbol: '" + name + "'",
                node.getLine(),
                node.getColumn()
            );
            
            // 记录未解析的引用
            unresolvedReferences.add(new UnresolvedReference(
                name, node.getLine(), node.getColumn(), node
            ));
        } else {
            // 符号已解析，附加到AST节点
            node.setSymbol(symbol);
            
            // 如果是变量，标记为已使用
            if (symbol instanceof VariableSymbol) {
                ((VariableSymbol) symbol).setUsed(true);
            }
        }
        
        return super.visitIdentifier(node);
    }
    
    /**
     * 获取未解析的引用列表
     * 
     * @return 未解析的引用列表
     */
    public List<UnresolvedReference> getUnresolvedReferences() {
        return Collections.unmodifiableList(unresolvedReferences);
    }
}
```

**FlowAnalyzer** - 控制流分析器
```java
/**
 * 控制流分析器
 * 
 * <p>分析程序的控制流属性，包括：
 * - 返回语句检查
 * - 变量初始化检查
 * - 死代码检测</p>
 */
public class FlowAnalyzer extends BaseASTVisitor<Void> {
    /** 符号表 */
    private final SymbolTable symbolTable;
    
    /** 错误收集器 */
    private final ErrorCollector errorCollector;
    
    /** 当前函数返回类型 */
    private Type currentFunctionReturnType;
    
    /** 是否在循环中 */
    private boolean inLoop;
    
    /** 控制流警告列表 */
    private final List<FlowWarning> warnings;
    
    /** 已访问的节点集合（用于检测循环） */
    private final Set<ASTNode> visitedNodes;
    
    /**
     * 构造函数
     * 
     * @param symbolTable 符号表
     * @param errorCollector 错误收集器
     */
    public FlowAnalyzer(SymbolTable symbolTable, ErrorCollector errorCollector) {
        this.symbolTable = symbolTable;
        this.errorCollector = errorCollector;
        this.warnings = new ArrayList<>();
        this.visitedNodes = new HashSet<>();
        this.inLoop = false;
    }
    
    /**
     * 获取控制流警告列表
     * 
     * @return 警告列表
     */
    public List<FlowWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    @Override
    public Void visitFunctionDecl(ASTFunctionDecl node) {
        currentFunctionReturnType = node.getReturnType();
        
        // 检查非void函数是否有返回语句
        if (currentFunctionReturnType != BuiltInType.VOID) {
            boolean hasReturn = containsReturnStatement(node.getBody());
            if (!hasReturn) {
                errorCollector.reportError(
                    "Missing return statement in function returning " + currentFunctionReturnType,
                    node.getLine(),
                    node.getColumn()
                );
            }
        }
        
        super.visitFunctionDecl(node);
        
        currentFunctionReturnType = BuiltInType.VOID;
        return null;
    }
    
    @Override
    public Void visitIfStmt(ASTIfStmt node) {
        // 分析条件表达式
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
        }
        
        // 检查条件是否为常量
        if (isConstantExpression(node.getCondition())) {
            ASTExpression condition = node.getCondition();
            Object value = evaluateConstantExpression(condition);
            
            if (Boolean.FALSE.equals(value)) {
                // 条件恒为false，if分支不可达
                warnings.add(new FlowWarning(
                    "Unreachable code in if-branch (condition is always false)",
                    node.getLine()
                ));
                
                // 仍然分析if分支以进行初始化检查
                if (node.getThenBranch() != null) {
                    node.getThenBranch().accept(this);
                }
            } else if (Boolean.TRUE.equals(value)) {
                // 条件恒为true，else分支不可达
                warnings.add(new FlowWarning(
                    "Unreachable code in else-branch (condition is always true)",
                    node.getLine()
                ));
                
                // 分析else分支
                if (node.getElseBranch() != null) {
                    node.getElseBranch().accept(this);
                }
            }
        } else {
            // 正常分析两个分支
            if (node.getThenBranch() != null) {
                node.getThenBranch().accept(this);
            }
            if (node.getElseBranch() != null) {
                node.getElseBranch().accept(this);
            }
        }
        
        return null;
    }
    
    @Override
    public Void visitWhileStmt(ASTWhileStmt node) {
        boolean wasInLoop = inLoop;
        inLoop = true;
        
        super.visitWhileStmt(node);
        
        inLoop = wasInLoop;
        return null;
    }
    
    @Override
    public Void visitReturnStmt(ASTReturnStmt node) {
        // 返回语句检查在SymbolResolver中处理
        return super.visitReturnStmt(node);
    }
    
    @Override
    public Void visitVariableUse(ASTVariableUse node) {
        String varName = node.getIdentifier().getName();
        Symbol symbol = symbolTable.resolve(varName);
        
        if (symbol instanceof VariableSymbol) {
            VariableSymbol varSymbol = (VariableSymbol) symbol;
            
            // 检查变量是否已初始化
            if (!varSymbol.isInitialized() && !inLoop) {
                // 在循环中，变量可能在循环前初始化
                if (!isInitializedBeforeUse(varSymbol, node)) {
                    errorCollector.reportError(
                        "Variable '" + varName + "' might not have been initialized",
                        node.getLine(),
                        node.getColumn()
                    );
                }
            }
        }
        
        return super.visitVariableUse(node);
    }
    
    /**
     * 检查函数体是否包含返回语句
     */
    private boolean containsReturnStatement(ASTStatement node) {
        if (node instanceof ASTReturnStmt) {
            return true;
        }
        
        if (node instanceof ASTCompoundStmt) {
            for (var stmt : ((ASTCompoundStmt) node).getStatements()) {
                if (containsReturnStatement(stmt)) {
                    return true;
                }
            }
        }
        
        if (node instanceof ASTIfStmt) {
            ASTIfStmt ifStmt = (ASTIfStmt) node;
            boolean thenHasReturn = containsReturnStatement(ifStmt.getThenBranch());
            boolean elseHasReturn = ifStmt.getElseBranch() != null && 
                                    containsReturnStatement(ifStmt.getElseBranch());
            return thenHasReturn && elseHasReturn;
        }
        
        return false;
    }
    
    /**
     * 检查表达式是否为常量表达式
     */
    private boolean isConstantExpression(ASTExpression node) {
        if (node instanceof ASTLiteral) {
            return true;
        }
        
        if (node instanceof ASTBinaryExpr) {
            ASTBinaryExpr binaryExpr = (ASTBinaryExpr) node;
            return isConstantExpression(binaryExpr.getLeft()) && 
                   isConstantExpression(binaryExpr.getRight());
        }
        
        if (node instanceof ASTUnaryExpr) {
            return isConstantExpression(((ASTUnaryExpr) node).getOperand());
        }
        
        return false;
    }
    
    /**
     * 计算常量表达式的值
     */
    private Object evaluateConstantExpression(ASTExpression node) {
        if (node instanceof ASTLiteral) {
            return ((ASTLiteral) node).getValue();
        }
        
        if (node instanceof ASTBinaryExpr) {
            ASTBinaryExpr binaryExpr = (ASTBinaryExpr) node;
            Object left = evaluateConstantExpression(binaryExpr.getLeft());
            Object right = evaluateConstantExpression(binaryExpr.getRight());
            
            String op = binaryExpr.getOperator();
            
            if (left instanceof Integer && right instanceof Integer) {
                int l = (Integer) left;
                int r = (Integer) right;
                return switch (op) {
                    case "+" -> l + r;
                    case "-" -> l - r;
                    case "*" -> l * r;
                    case "/" -> l / r;
                    case "==" -> l == r;
                    case "!=" -> l != r;
                    case "<" -> l < r;
                    case ">" -> l > r;
                    case "<=" -> l <= r;
                    case ">=" -> l >= r;
                    default -> null;
                };
            }
            
            // 处理浮点数...
        }
        
        return null;
    }
    
    /**
     * 检查变量在使用前是否已初始化
     */
    private boolean isInitializedBeforeUse(VariableSymbol varSymbol, ASTNode useNode) {
        // 简化实现：检查变量定义和使用在同一基本块
        // 完整实现需要数据流分析
        return varSymbol.isInitialized();
    }
}
```

**ErrorCollector** - 错误收集器
```java
/**
 * 错误收集器
 * 
 * <p>收集语义分析过程中的错误和警告，
 * 并提供错误报告功能。</p>
 */
public class ErrorCollector {
    /** 错误列表 */
    private final List<CompilationError> errors;
    
    /** 警告列表 */
    private final List<CompilationWarning> warnings;
    
    /** 最大错误数量（超过则停止分析） */
    private static final int MAX_ERRORS = 20;
    
    /** 是否应继续分析 */
    private boolean shouldContinue;
    
    /**
     * 记录错误
     * 
     * @param message 错误消息
     * @param line 行号
     * @param column 列号
     */
    public void reportError(String message, int line, int column) {
        errors.add(new CompilationError(message, line, column));
        
        if (errors.size() >= MAX_ERRORS) {
            shouldContinue = false;
        }
    }
    
    /**
     * 记录错误（带位置对象）
     * 
     * @param message 错误消息
     * @param location 位置信息
     */
    public void reportError(String message, ParserRuleContext location) {
        reportError(message, location.getStart().getLine(), 
                    location.getStart().getCharPositionInLine());
    }
    
    /**
     * 记录警告
     * 
     * @param message 警告消息
     * @param line 行号
     * @param column 列号
     */
    public void reportWarning(String message, int line, int column) {
        warnings.add(new CompilationWarning(message, line, column));
    }
    
    /**
     * 记录警告（带位置对象）
     * 
     * @param message 警告消息
     * @param location 位置信息
     */
    public void reportWarning(String message, ParserRuleContext location) {
        reportWarning(message, location.getStart().getLine(),
                     location.getStart().getCharPositionInLine());
    }
    
    /**
     * 获取错误数量
     * 
     * @return 错误数量
     */
    public int getErrorCount() {
        return errors.size();
    }
    
    /**
     * 获取警告数量
     * 
     * @return 警告数量
     */
    public int getWarningCount() {
        return warnings.size();
    }
    
    /**
     * 检查是否应该继续分析
     * 
     * @return true表示应该继续
     */
    public boolean shouldContinue() {
        return shouldContinue && errors.size() < MAX_ERRORS;
    }
    
    /**
     * 获取所有错误
     * 
     * @return 错误列表
     */
    public List<CompilationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }
    
    /**
     * 获取所有警告
     * 
     * @return 警告列表
     */
    public List<CompilationWarning> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
    
    /**
     * 生成错误报告
     * 
     * @return 格式化的错误报告
     */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            sb.append("No errors or warnings.\n");
            return sb.toString();
        }
        
        // 输出错误
        if (!errors.isEmpty()) {
            sb.append("Errors (").append(errors.size()).append("):\n");
            for (var error : errors) {
                sb.append("  Line ").append(error.getLine())
                   .append(", Column ").append(error.getColumn())
                   .append(": ").append(error.getMessage()).append("\n");
            }
        }
        
        // 输出警告
        if (!warnings.isEmpty()) {
            sb.append("\nWarnings (").append(warnings.size()).append("):\n");
            for (var warning : warnings) {
                sb.append("  Line ").append(warning.getLine())
                   .append(": ").append(warning.getMessage()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 清除所有错误和警告
     */
    public void clear() {
        errors.clear();
        warnings.clear();
        shouldContinue = true;
    }
}
```

### 实战流程

实战步骤：构建和测试语义分析系统

步骤1：编译项目并运行语义分析测试
```bash
# 进入项目根目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4

# 编译项目
mvn clean compile -DskipTests

# 运行语义分析测试
mvn test -Dtest=SemanticAnalyzerTest

# 预期输出：
# [INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认测试通过

步骤2：运行错误收集器测试
```bash
# 运行错误收集器测试
mvn test -Dtest=ErrorCollectorTest

# 预期输出：
# [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

步骤3：端到端语义分析测试
```bash
# 创建包含语义错误的测试程序
cat > /tmp/semantic_error.cymbol << 'EOF'
int test() {
    int x = 5;
    int y = 10;
    
    // 错误1：未定义的符号
    print(z);
    
    // 错误2：类型不匹配
    x = "hello";
    
    // 错误3：重复定义
    int x = 20;
    
    return x;
}
EOF

# 运行编译器
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.common.Compiler" \
    -Dexec.args="/tmp/semantic_error.cymbol"

# 预期输出（错误信息）：
# Line 5: Undefined symbol: 'z'
# Line 8: incompatible types: String cannot be converted to int
# Line 11: Duplicate declaration of variable: 'x'
```

故障排查：

**问题1：语义分析不完整**
- 原因：某些AST节点类型没有在Visitor中处理
- 解决方法：
  1. 检查所有AST节点类型是否都有对应的visit方法
  2. 确认递归调用正确处理所有子节点
  3. 添加缺失的AST节点类型处理

**问题2：错误信息不准确**
- 原因：错误位置或消息计算有误
- 解决方法：
  1. 检查ParserRuleContext的使用
  2. 验证行号和列号的获取
  3. 确认错误消息的格式

**问题3：控制流分析遗漏**
- 原因：控制流分析逻辑不完整
- 解决方法：
  1. 检查控制流分析器是否正确处理所有语句类型
  2. 验证常量表达式求值逻辑
  3. 添加更多边界情况的测试

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI帮助完成语义分析相关的开发任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：

1. `common/src/main/java/org/teachfx/antlr4/common/semantic/SemanticAnalyzer.java`
   - 作用：语义分析主类
   - 关键方法：`analyze()`, `performSymbolResolution()`, `performTypeChecking()`

2. `common/src/main/java/org/teachfx/antlr4/common/semantic/SymbolResolver.java`
   - 作用：符号解析器
   - 关键方法：`visitIdentifier()`, `visitVariableDecl()`

3. `common/src/main/java/org/teachfx/antlr4/common/semantic/TypeChecker.java`
   - 作用：类型检查器
   - 关键方法：`visitBinaryExpr()`, `visitCallExpr()`

4. `common/src/main/java/org/teachfx/antlr4/common/semantic/FlowAnalyzer.java`
   - 作用：控制流分析器
   - 关键方法：`visitFunctionDecl()`, `visitIfStmt()`

5. `common/src/main/java/org/teachfx/antlr4/common/semantic/ErrorCollector.java`
   - 作用：错误收集器
   - 关键方法：`reportError()`, `generateReport()`

**文档文件**：

1. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：语义分析规范、错误处理

2. `common/docs/semantic_analysis_design.md`
   - 作用：语义分析设计文档
   - 关键章节：分析阶段、错误处理

### Prompt 模板（给 AI 用）

**类型 A：语义分析扩展 Prompt 模板**

```
请为语义分析系统实现{新功能}。

任务目标：
- 实现{功能描述}
- 支持{使用场景}
- 保持与现有语义分析API的一致性

具体要求：
1. 实现功能逻辑
   - 在{相关类}中添加{方法/字段}
   - 处理{边界情况}
   - 正确更新分析结果

2. 添加测试
   - 测试{新功能}的各个方面
   - 测试错误情况
   - 测试与现有功能的兼容性

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/semantic/SemanticAnalyzer.java
  - common/src/main/java/org/teachfx/antlr4/common/semantic/{相关类}.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/semantic/{相关测试}.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不破坏现有的分析流程
- 不修改核心Visitor模式
- 所有新增代码必须通过mvn test

期望输出：
1. 新功能实现的完整代码
2. 测试代码
3. 使用示例和预期行为说明
```

### AI 应该做 / 不该做

**✅ AI 允许的事情**：

1. **实现新的语义检查**
   - ✅ 可以：添加新的语义规则检查
   - ✅ 可以：增强现有检查的覆盖范围
   - ❌ 不能：破坏现有的检查流程

2. **改进错误报告**
   - ✅ 可以：改进错误消息的格式和内容
   - ✅ 可以：添加新的错误类型
   - ❌ 不能：改变ErrorCollector的核心接口

3. **添加分析工具**
   - ✅ 可以：实现分析统计工具
   - ✅ 可以：实现代码质量度量
   - ✅ 可以：生成分析报告

**❌ AI 禁止做的事情**：

1. **破坏语义分析流程**
   - ❌ 不允许：改变语义分析的阶段顺序
   - ❌ 不允许：跳过必要的分析步骤
   - 原因：语义分析流程是经过验证的

2. **删除语义检查**
   - ❌ 不允许：移除现有的语义检查
   - ❌ 不允许：降低检查的严格程度
   - 原因：语义检查保证程序正确性

### 验证与回滚策略

## 自动化验证

**步骤1：运行语义分析相关测试**
```bash
# 运行所有语义分析测试
cd common
mvn test -Dtest=*Semantic*,*Flow*,*Error*

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

## 手工检查点

**检查1：语义分析完整性**
- [ ] 所有语义规则都被检查
- [ ] 错误信息准确且有帮助
- [ ] 控制流分析覆盖所有情况

## 回滚方案

```bash
# 恢复特定文件
git checkout -- common/src/main/java/org/teachfx/antlr4/common/semantic/
```

## 练习题

### 练习1：实现未使用变量检测（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
在控制流分析器中添加未使用变量检测功能，报告声明但从未使用的变量。

要求：
- 完全手工实现，不依赖AI
- 跟踪变量的使用情况
- 在分析结束时报告未使用的变量
- 编写测试验证功能

验收标准：
- [ ] 代码能编译通过
- [ ] 正确检测未使用变量
- [ ] 代码风格符合规范
- [ ] 通过所有测试

### 练习2：实现常量条件检测（AI协作版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
增强if语句的条件分析，检测恒真或恒假的条件，并报告不可达的代码分支。

AI协作要求：
1. 设计上下文：列出需要提供给AI的文件和说明
2. 设计Prompt：参考本章的Prompt模板
3. 验证AI输出：使用本章的验证策略
4. 理解AI代码：确保你能解释AI生成的每一部分

验收标准：
- [ ] AI生成的代码能编译通过
- [ ] 正确检测恒定条件
- [ ] 报告不可达代码
- [ ] 通过所有测试

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **语义分析核心概念**
   - 理解了语义分析在编译器中的位置和作用
   - 掌握了属性文法的基本原理
   - 学会了如何使用Visitor模式进行语义分析

2. **语义分析实现技术**
   - 学会了符号解析、类型检查、控制流分析的协调
   - 掌握了错误收集和报告的方法
   - 理解了语义分析的分阶段流程

3. **实战技能**
   - 能够实现完整的语义分析系统
   - 学会了生成有用的错误信息
   - 掌握了测试语义分析的方法

### 【你现在站在】:
```
... → [类型系统] → ✅ [语义分析] → [IR生成] → ...
```

**当前在编译器流水线的位置**：
- 本章完成了编译器前端的所有检查
- 下一阶段将进入中间表示的生成
- 语义分析为IR生成提供类型和符号信息

### 下一章预告

第15章将聚焦于**中间表示（IR）生成**，你将学习：
- 如何设计中间表示数据结构
- 如何将AST转换为IR
- 如何在IR生成中使用符号表和类型信息
- 如何实现三地址码等常用IR形式

**准备**：为了学习下一章，建议：
- [ ] 复习本章的语义分析实现
- [ ] 运行语义分析相关测试
- [ ] 阅读AGENTS.md中的IR生成相关部分
- [ ] 思考语义分析结果如何传递给IR生成阶段

继续加油！语义分析是编译器的"逻辑检查员"，掌握了它，你的编译器就能捕获大部分程序逻辑错误！
