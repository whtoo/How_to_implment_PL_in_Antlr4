# 第12章：Symbol Table - 符号表设计与实现

## 本章概述

本章聚焦于编译器中最重要的数据结构之一——符号表（Symbol Table）。通过学习本章，你将掌握符号表的设计原理、实现方法以及在编译器各阶段的协同应用，这是实现语义分析和类型检查的基础设施。

【你现在站在哪】:
```
... → [词法分析] → [语法分析] → [AST构建] → ✅ 符号表 → [语义分析] → [IR生成] → ...
```

## 动机与真实场景

真实场景：你在维护一个编程语言编译器，用户报告了一个令人困惑的错误信息。当他们声明两个同名的局部变量时，编译器给出的错误消息是"undefined symbol"，而不是期望的"duplicate symbol"。

具体问题或挑战：
- 编译器无法正确追踪变量的作用域，导致变量解析错误
- 符号表的查询效率低下，大型程序的编译时间过长
- 符号表与AST的绑定不紧密，语义分析阶段需要反复遍历AST
- 没有清晰的分层设计，新功能难以扩展

如果缺少本章的能力，你将面临：
- 无法正确实现变量作用域（局部、全局、块作用域）
- 语义分析阶段的错误信息不准确，用户体验差
- 编译器难以扩展以支持新的语言特性（如类、命名空间）
- 代码维护困难，任何修改都可能影响其他部分

本章将教你如何：
- 设计高效的符号表数据结构，支持快速查找和作用域管理
- 实现符号表的分层结构（全局表、函数表、局部表）
- 将符号表与AST紧密绑定，支持属性文法分析
- 实现符号的注册、查询、隐藏和解析机制

## 人类工程师线：技术与实现

### 核心概念

**符号表**是编译器中用于存储和检索程序中各种符号（如变量名、函数名、类型名）信息的数据结构。它贯穿编译器的多个阶段，从词法分析后的标识符收集，到语义分析的类型检查，再到代码生成的目标代码映射，都需要符号表的支持。

通俗解释：符号表就像一本书的索引。当你写一本书时，你需要记录每个术语第一次出现的位置、它的定义、使用的章节等信息。编译器中的符号表也是如此——它记录每个变量/函数的名字、类型、在哪里定义、在哪里使用等信息。没有这个索引，编译器就无法理解程序中的名字引用是指向哪个定义。

[图1：符号表在编译器流水线中的位置]
```
源代码
   │
   ▼
词法分析 → Token流（收集标识符）
   │
   ▼
语法分析 → AST（构建语法树）
   │
   ▼
符号表构建 → 为AST节点附加符号信息
   │
   ▼
语义分析 → 类型检查、作用域解析
   │
   ▼
IR生成 → 符号引用解析
   │
   ▼
代码生成 → 符号地址映射
```

类比理解：想象一个图书馆的索引系统：
- **图书目录** = 符号表，记录每本书（符号）的位置（内存地址）
- **索书号** = 符号的类型信息，指示如何处理这本书
- **借阅记录** = 符号的使用信息，追踪引用关系
- **书架布局** = 作用域规则，决定哪些书可见

**作用域规则**是符号表的核心功能之一。不同作用域的同名符号应该互不干扰，编译器需要能够正确处理：

[图2：嵌套作用域示例]
```
int x = 10;           // 全局变量 x

void foo() {
    int x = 20;       // foo 的局部变量 x，隐藏全局 x
    int y = x + 5;    // 使用局部 x，结果是 25
    
    if (y > 15) {
        int x = 30;   // if 块内的局部变量 x，隐藏 foo 的 x
        print(x);     // 打印 30
    }
    
    print(x);         // 打印 20
}

void bar() {
    print(x);         // 使用全局 x，打印 10
}
```

相关概念：
- **符号（Symbol）**：程序中的名字实体（变量、函数、类型等）
- **作用域（Scope）**：符号可见的代码区域
- **绑定（Binding）**：符号与其属性（类型、地址等）的关联
- **解析（Resolution）**：根据名字查找符号的过程
- **隐藏（Hiding）**：内层作用域的符号遮蔽外层同名符号

### 与仓库 EP 的对应关系

对应 EP：EP10-EP13

目录结构：
```
common/
├── src/main/java/org/teachfx/antlr4/common/
│   └── symtab/
│       ├── Symbol.java              // 符号基类
│       ├── VariableSymbol.java      // 变量符号
│       ├── FunctionSymbol.java      // 函数符号
│       ├── TypeSymbol.java          // 类型符号
│       ├── SymbolTable.java         // 符号表主类
│       └── SymbolResolver.java      // 符号解析器
├── src/test/java/org/teachfx/antlr4/common/
│   └── symtab/
│       ├── SymbolTableTest.java     // 符号表测试
│       └── SymbolResolverTest.java  // 解析器测试
└── docs/
    └── symtab_design.md             // 符号表设计文档
```

关键类/方法说明：

**Symbol** - 符号基类
```java
/**
 * 符号基类，所有符号类型的父类
 * 
 * <p>符号是编译器中用于表示程序实体的数据结构，包括变量、函数、类型等。
 * 每个符号都有一个名字和可选的类型信息。</p>
 */
public abstract class Symbol {
    /** 符号名称 */
    protected String name;
    
    /** 符号类型（可为null） */
    protected Type type;
    
    /** 符号定义所在的AST节点（用于错误报告和调试） */
    protected ParserRuleContext definitionNode;
    
    /**
     * 构造函数
     * 
     * @param name 符号名称
     */
    public Symbol(String name) {
        this.name = name;
    }
    
    /**
     * 构造函数（带类型）
     * 
     * @param name 符号名称
     * @param type 符号类型
     */
    public Symbol(String name, Type type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * 获取符号名称
     * 
     * @return 符号名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置符号类型
     * 
     * @param type 符号类型
     */
    public void setType(Type type) {
        this.type = type;
    }
    
    /**
     * 获取符号类型
     * 
     * @return 符号类型，可能为null
     */
    public Type getType() {
        return type;
    }
    
    /**
     * 设置定义节点
     * 
     * @param node 定义该符号的AST节点
     */
    public void setDefinitionNode(ParserRuleContext node) {
        this.definitionNode = node;
    }
    
    /**
     * 获取定义节点
     * 
     * @return 定义该符号的AST节点
     */
    public ParserRuleContext getDefinitionNode() {
        return definitionNode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (type != null) {
            sb.append(": ").append(type);
        }
        return sb.toString();
    }
}
```

**VariableSymbol** - 变量符号
```java
/**
 * 变量符号，表示程序中的变量实体
 */
public class VariableSymbol extends Symbol {
    /** 变量是否只读（final） */
    private final boolean isFinal;
    
    /** 变量是否已初始化 */
    private boolean isInitialized;
    
    /** 变量的存储偏移量（代码生成阶段使用） */
    private int offset;
    
    /**
     * 构造函数
     * 
     * @param name 变量名称
     * @param type 变量类型
     */
    public VariableSymbol(String name, Type type) {
        super(name, type);
        this.isFinal = false;
        this.isInitialized = false;
    }
    
    /**
     * 构造函数（带修饰符）
     * 
     * @param name 变量名称
     * @param type 变量类型
     * @param isFinal 是否为final
     */
    public VariableSymbol(String name, Type type, boolean isFinal) {
        super(name, type);
        this.isFinal = isFinal;
        this.isInitialized = false;
    }
    
    /**
     * 检查变量是否为final
     * 
     * @return true表示是final变量
     */
    public boolean isFinal() {
        return isFinal;
    }
    
    /**
     * 标记变量已初始化
     */
    public void setInitialized() {
        this.isInitialized = true;
    }
    
    /**
     * 检查变量是否已初始化
     * 
     * @return true表示已初始化
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * 设置变量的存储偏移量
     * 
     * @param offset 存储偏移量
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    /**
     * 获取变量的存储偏移量
     * 
     * @return 存储偏移量
     */
    public int getOffset() {
        return offset;
    }
}
```

**FunctionSymbol** - 函数符号
```java
/**
 * 函数符号，表示程序中的函数实体
 */
public class FunctionSymbol extends Symbol {
    /** 参数列表 */
    private final List<VariableSymbol> parameters;
    
    /** 返回类型 */
    private Type returnType;
    
    /** 函数体AST节点 */
    private ParserRuleContext bodyNode;
    
    /** 函数是否已分析（用于避免重复分析） */
    private boolean analyzed;
    
    /**
     * 构造函数
     * 
     * @param name 函数名称
     * @param returnType 返回类型
     * @param parameterTypes 参数类型列表
     */
    public FunctionSymbol(String name, Type returnType, List<Type> parameterTypes) {
        super(name);
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
        
        // 创建参数符号
        for (int i = 0; i < parameterTypes.size(); i++) {
            parameters.add(new VariableSymbol("arg" + i, parameterTypes.get(i)));
        }
        
        this.analyzed = false;
    }
    
    /**
     * 添加参数
     * 
     * @param param 参数符号
     */
    public void addParameter(VariableSymbol param) {
        parameters.add(param);
    }
    
    /**
     * 获取参数列表
     * 
     * @return 参数符号列表
     */
    public List<VariableSymbol> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    
    /**
     * 获取参数数量
     * 
     * @return 参数数量
     */
    public int getParameterCount() {
        return parameters.size();
    }
    
    /**
     * 设置返回类型
     * 
     * @param type 返回类型
     */
    public void setReturnType(Type type) {
        this.returnType = type;
    }
    
    /**
     * 获取返回类型
     * 
     * @return 返回类型
     */
    public Type getReturnType() {
        return returnType;
    }
    
    /**
     * 设置函数体节点
     * 
     * @param node 函数体AST节点
     */
    public void setBodyNode(ParserRuleContext node) {
        this.bodyNode = node;
    }
    
    /**
     * 获取函数体节点
     * 
     * @return 函数体AST节点
     */
    public ParserRuleContext getBodyNode() {
        return bodyNode;
    }
    
    /**
     * 标记函数已分析
     */
    public void setAnalyzed() {
        this.analyzed = true;
    }
    
    /**
     * 检查函数是否已分析
     * 
     * @return true表示已分析
     */
    public boolean isAnalyzed() {
        return analyzed;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        
        // 添加参数类型
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(parameters.get(i).getType());
        }
        
        sb.append(")");
        
        if (returnType != null) {
            sb.append(": ").append(returnType);
        }
        
        return sb.toString();
    }
}
```

**SymbolTable** - 符号表主类
```java
/**
 * 符号表实现
 * 
 * <p>符号表是编译器中用于存储和检索符号信息的数据结构。
 * 本实现采用分层作用域结构，支持嵌套作用域和符号隐藏。</p>
 * 
 * <p>设计决策：</p>
 * <ul>
 *   <li>使用List模拟作用域栈，支持嵌套作用域</li>
 *   <li>每个作用域使用HashMap实现，提供O(1)的查找复杂度</li>
 *   <li>支持符号的注册、查询、隐藏和解析</li>
 * </ul>
 */
public class SymbolTable {
    /** 作用域栈，每个元素是一个作用域（Map） */
    private final List<Map<String, Symbol>> scopeStack;
    
    /** 全局作用域（所有函数共享） */
    private final Map<String, Symbol> globalScope;
    
    /** 当前作用域索引 */
    private int currentScopeIndex;
    
    /** 符号表监听器列表（用于调试和日志） */
    private final List<SymbolTableListener> listeners;
    
    /**
     * 作用域条目，保存作用域信息和起始位置
     */
    private static class ScopeEntry {
        final Map<String, Symbol> scope;
        final String name;
        final int startLine;
        final int endLine;
        
        ScopeEntry(Map<String, Symbol> scope, String name, int startLine) {
            this.scope = scope;
            this.name = name;
            this.startLine = startLine;
            this.endLine = -1;
        }
    }
    
    /** 作用域栈（包含额外信息） */
    private final List<ScopeEntry> scopeStackWithInfo;
    
    /**
     * 创建新的符号表
     */
    public SymbolTable() {
        this.scopeStack = new ArrayList<>();
        this.scopeStackWithInfo = new ArrayList<>();
        this.globalScope = new HashMap<>();
        this.listeners = new ArrayList<>();
        
        // 初始化全局作用域
        enterScope("global", 0);
    }
    
    /**
     * 进入新的作用域
     * 
     * @param name 作用域名称（用于调试）
     * @param startLine 起始行号
     */
    public void enterScope(String name, int startLine) {
        Map<String, Symbol> newScope = new HashMap<>();
        scopeStack.add(newScope);
        scopeStackWithInfo.add(new ScopeEntry(newScope, name, startLine));
        currentScopeIndex = scopeStack.size() - 1;
        
        // 通知监听器
        for (SymbolTableListener listener : listeners) {
            listener.scopeEntered(name, startLine);
        }
    }
    
    /**
     * 退出当前作用域
     * 
     * @return 退出的作用域名称
     */
    public String exitScope() {
        if (scopeStack.size() <= 1) {
            throw new IllegalStateException("Cannot exit global scope");
        }
        
        ScopeEntry entry = scopeStackWithInfo.remove(scopeStackWithInfo.size() - 1);
        scopeStack.remove(scopeStack.size() - 1);
        currentScopeIndex = scopeStack.size() - 1;
        
        // 通知监听器
        for (SymbolTableListener listener : listeners) {
            listener.scopeExited(entry.name, entry.startLine);
        }
        
        return entry.name;
    }
    
    /**
     * 注册符号到当前作用域
     * 
     * @param symbol 要注册的符号
     * @throws SymbolAlreadyDefinedException 如果符号已存在
     */
    public void define(Symbol symbol) {
        String name = symbol.getName();
        Map<String, Symbol> currentScope = scopeStack.get(currentScopeIndex);
        
        // 检查是否已存在
        if (currentScope.containsKey(name)) {
            Symbol existing = currentScope.get(name);
            throw new SymbolAlreadyDefinedException(
                "Symbol '" + name + "' already defined at line " + 
                existing.getDefinitionNode().getStart().getLine(),
                symbol.getDefinitionNode()
            );
        }
        
        // 注册符号
        currentScope.put(name, symbol);
        
        // 通知监听器
        for (SymbolTableListener listener : listeners) {
            listener.symbolDefined(name, symbol, currentScopeIndex);
        }
    }
    
    /**
     * 在当前作用域查找符号（仅当前作用域）
     * 
     * @param name 符号名称
     * @return 找到的符号，如果未找到返回null
     */
    public Symbol lookupCurrentScope(String name) {
        return scopeStack.get(currentScopeIndex).get(name);
    }
    
    /**
     * 解析符号（从内层到外层作用域）
     * 
     * <p>从当前作用域开始向外查找，直到找到符号或到达全局作用域。</p>
     * 
     * @param name 符号名称
     * @return 找到的符号，如果未找到返回null
     */
    public Symbol resolve(String name) {
        // 从内层到外层查找
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Symbol symbol = scopeStack.get(i).get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }
    
    /**
     * 解析符号（带错误报告）
     * 
     * @param name 符号名称
     * @param errorNode 错误发生的位置
     * @return 找到的符号
     * @throws UndefinedSymbolException 如果符号未定义
     */
    public Symbol resolveOrFail(String name, ParserRuleContext errorNode) {
        Symbol symbol = resolve(name);
        if (symbol == null) {
            throw new UndefinedSymbolException(
                "Undefined symbol: '" + name + "'",
                errorNode
            );
        }
        return symbol;
    }
    
    /**
     * 检查符号是否在当前作用域可见
     * 
     * @param name 符号名称
     * @return true表示可见
     */
    public boolean isVisible(String name) {
        return resolve(name) != null;
    }
    
    /**
     * 获取当前作用域深度
     * 
     * @return 作用域数量
     */
    public int getScopeDepth() {
        return scopeStack.size();
    }
    
    /**
     * 获取当前作用域索引
     * 
     * @return 作用域索引
     */
    public int getCurrentScopeIndex() {
        return currentScopeIndex;
    }
    
    /**
     * 添加符号表监听器
     * 
     * @param listener 监听器
     */
    public void addListener(SymbolTableListener listener) {
        listeners.add(listener);
    }
    
    /**
     * 获取所有符号（用于调试）
     * 
     * @return 符号到作用域索引的映射
     */
    public Map<Symbol, Integer> getAllSymbols() {
        Map<Symbol, Integer> result = new HashMap<>();
        for (int i = 0; i < scopeStack.size(); i++) {
            for (Symbol symbol : scopeStack.get(i).values()) {
                result.put(symbol, i);
            }
        }
        return result;
    }
    
    /**
     * 生成符号表内容（用于调试）
     * 
     * @return 符号表内容字符串
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Symbol Table Contents:\n");
        
        for (int i = 0; i < scopeStackWithInfo.size(); i++) {
            ScopeEntry entry = scopeStackWithInfo.get(i);
            sb.append("\nScope ").append(i).append(": ").append(entry.name)
              .append(" (lines ").append(entry.startLine).append("-")
              .append(entry.endLine).append(")\n");
            
            for (Symbol symbol : entry.scope.values()) {
                sb.append("  ").append(symbol).append("\n");
            }
        }
        
        return sb.toString();
    }
}
```

**SymbolResolver** - 符号解析器
```java
/**
 * 符号解析器，使用符号表解析AST中的标识符引用
 * 
 * <p>SymbolResolver是一个Visitor模式的实现，它遍历AST，
 * 在遇到标识符时查询符号表并验证引用是否有效。</p>
 */
public class SymbolResolver extends CymbolBaseVisitor<Void> {
    /** 符号表 */
    private final SymbolTable symbolTable;
    
    /** 错误收集器 */
    private final ErrorCollector errorCollector;
    
    /** 当前正在分析的函数（用于参数处理） */
    private FunctionSymbol currentFunction;
    
    /**
     * 构造函数
     * 
     * @param symbolTable 符号表
     * @param errorCollector 错误收集器
     */
    public SymbolResolver(SymbolTable symbolTable, ErrorCollector errorCollector) {
        this.symbolTable = symbolTable;
        this.errorCollector = errorCollector;
    }
    
    @Override
    public Void visitFile(CymbolParser.FileContext ctx) {
        // 进入文件作用域
        symbolTable.enterScope("file", ctx.getStart().getLine());
        
        // 遍历声明
        super.visitFile(ctx);
        
        // 退出文件作用域
        symbolTable.exitScope();
        
        return null;
    }
    
    @Override
    public Void visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        String funcName = ctx.ID().getText();
        Type returnType = convertType(ctx.type());
        
        // 收集参数类型
        List<Type> paramTypes = new ArrayList<>();
        if (ctx.formalParameters() != null) {
            for (var param : ctx.formalParameters().formalParameter()) {
                paramTypes.add(convertType(param.type()));
            }
        }
        
        // 创建函数符号
        FunctionSymbol funcSymbol = new FunctionSymbol(funcName, returnType, paramTypes);
        funcSymbol.setDefinitionNode(ctx);
        
        // 进入函数作用域
        symbolTable.enterScope(funcName, ctx.getStart().getLine());
        
        // 定义参数
        int paramIndex = 0;
        if (ctx.formalParameters() != null) {
            for (var param : ctx.formalParameters().formalParameter()) {
                String paramName = param.ID().getText();
                Type paramType = paramTypes.get(paramIndex);
                
                VariableSymbol varSymbol = new VariableSymbol(paramName, paramType);
                varSymbol.setDefinitionNode(param);
                symbolTable.define(varSymbol);
                
                paramIndex++;
            }
        }
        
        // 保存当前函数
        FunctionSymbol previousFunction = currentFunction;
        currentFunction = funcSymbol;
        
        // 递归处理函数体
        super.visitFunctionDecl(ctx);
        
        // 恢复前一个函数
        currentFunction = previousFunction;
        
        // 退出函数作用域
        symbolTable.exitScope();
        
        // 在全局作用域定义函数
        symbolTable.define(funcSymbol);
        
        return null;
    }
    
    @Override
    public Void visitVarDecl(CymbolParser.VarDeclContext ctx) {
        String varName = ctx.ID().getText();
        Type varType = convertType(ctx.type());
        
        // 创建变量符号
        VariableSymbol varSymbol = new VariableSymbol(varName, varType);
        varSymbol.setDefinitionNode(ctx);
        
        // 定义到当前作用域
        symbolTable.define(varSymbol);
        
        // 递归处理初始值表达式（如果有）
        if (ctx.expression() != null) {
            super.visitVarDecl(ctx);
        }
        
        return null;
    }
    
    @Override
    public Void visitIdExpr(CymbolParser.IdExprContext ctx) {
        String name = ctx.ID().getText();
        
        // 解析符号
        Symbol symbol = symbolTable.resolve(name);
        if (symbol == null) {
            errorCollector.reportError(
                "Undefined symbol: '" + name + "'",
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
            );
        } else {
            // 将符号附加到AST节点（用于后续阶段使用）
            ctx.symbol = symbol;
        }
        
        return super.visitIdExpr(ctx);
    }
    
    /**
     * 将ANTLR类型上下文转换为Type对象
     */
    private Type convertType(CymbolParser.TypeContext ctx) {
        if (ctx == null) {
            return BuiltInType.VOID;
        }
        
        String typeName = ctx.getText();
        return switch (typeName) {
            case "int" -> BuiltInType.INT;
            case "float" -> BuiltInType.FLOAT;
            case "void" -> BuiltInType.VOID;
            case "bool" -> BuiltInType.BOOL;
            default -> new UserDefinedType(typeName);
        };
    }
}
```

### 实战流程

实战步骤：构建和测试符号表系统

步骤1：编译项目并运行符号表测试
```bash
# 进入项目根目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4

# 编译项目
mvn clean compile -DskipTests

# 运行符号表测试
mvn test -Dtest=SymbolTableTest

# 预期输出：
# [INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认测试通过，没有失败或错误
- 检查点2：查看测试覆盖率报告（如果配置了JaCoCo）

步骤2：运行符号解析器测试
```bash
# 运行符号解析器测试
mvn test -Dtest=SymbolResolverTest

# 预期输出：
# [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

步骤3：使用调试模式查看符号表状态
```bash
# 创建一个测试程序
cat > /tmp/test_symbols.cymbol << 'EOF'
int globalVar = 10;

int add(int a, int b) {
    int localVar = a + b;
    return localVar + globalVar;
}

void main() {
    int x = 5;
    print(add(x, 3));
}
EOF

# 使用调试模式运行编译器
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.common.Compiler" \
    -Dexec.args="--debug-symbols /tmp/test_symbols.cymbol"

# 预期输出：
# Symbol Table Contents:
# 
# Scope 0: global (lines 1-8)
#   globalVar: int
#   add(int, int): int
#   main(void): void
# 
# Scope 1: add (lines 3-6)
#   a: int
#   b: int
#   localVar: int
# 
# Scope 2: main (lines 8-8)
#   x: int
```

故障排查：

**问题1：符号解析失败，报告"undefined symbol"**
- 原因：符号可能尚未定义就被引用
- 解决方法：
  1. 检查符号的定义顺序（函数需要在调用前定义）
  2. 确认符号是否在正确的作用域中定义
  3. 验证符号表的作用域进入/退出是否正确匹配

**问题2：符号定义重复**
- 原因：同一个作用域中定义了同名符号
- 解决方法：
  1. 检查变量的作用域是否正确（可能需要在更内层的作用域定义）
  2. 确认不是在循环中重复定义变量
  3. 检查函数的参数名是否与局部变量冲突

**问题3：符号隐藏未按预期工作**
- 原因：内层作用域的符号应该隐藏外层同名符号，但可能没有
- 解决方法：
  1. 确认解析顺序是从内层到外层
  2. 检查是否在正确的时机进入/退出作用域
  3. 验证符号表的作用域栈管理是否正确

进阶技巧：

1. **使用符号表监听器进行调试**：实现SymbolTableListener接口，监听符号定义和作用域变化
2. **导出DOT格式的符号表**：生成可视化的符号表关系图
3. **性能优化**：对于大型程序，考虑使用更高效的数据结构（如链式哈希表）

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让 AI 帮助完成符号表相关的开发任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：

1. `common/src/main/java/org/teachfx/antlr4/common/symtab/Symbol.java`
   - 作用：定义符号的基类和基本属性
   - 关键方法：`getName()`, `getType()`, `setDefinitionNode()`

2. `common/src/main/java/org/teachfx/antlr4/common/symtab/VariableSymbol.java`
   - 作用：表示变量的具体符号类型
   - 关键方法：`isFinal()`, `isInitialized()`, `setOffset()`

3. `common/src/main/java/org/teachfx/antlr4/common/symtab/FunctionSymbol.java`
   - 作用：表示函数的具体符号类型
   - 关键方法：`getParameters()`, `getReturnType()`, `addParameter()`

4. `common/src/main/java/org/teachfx/antlr4/common/symtab/SymbolTable.java`
   - 作用：符号表的核心实现
   - 关键方法：`enterScope()`, `exitScope()`, `define()`, `resolve()`

5. `common/src/main/java/org/teachfx/antlr4/common/symtab/SymbolResolver.java`
   - 作用：使用符号表解析AST中的标识符
   - 关键方法：`visitIdExpr()`, `visitVarDecl()`, `visitFunctionDecl()`

**文档文件**：

1. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：代码风格、命名规范、测试模式

2. `common/docs/symtab_design.md`
   - 作用：符号表设计文档
   - 关键章节：数据结构设计、作用域管理、算法复杂度

**测试文件**：

1. `common/src/test/java/org/teachfx/antlr4/common/symtab/SymbolTableTest.java`
   - 作用：符号表功能测试
   - 关键测试方法：`testScopeManagement()`, `testSymbolDefinition()`, `testSymbolResolution()`

2. `common/src/test/java/org/teachfx/antlr4/common/symtab/SymbolResolverTest.java`
   - 作用：符号解析器测试
   - 关键测试方法：`testVariableResolution()`, `testFunctionResolution()`, `testErrorReporting()`

**示例输入/输出**：

1. `common/src/test/resources/symtab_test1.cymbol`
   - 作用：测试符号表功能的示例程序
   - 包含：变量声明、函数定义、嵌套作用域

上下文组织说明：

这些文件按照**从抽象到具体**的原则组织：

1. **符号类型在前**：先提供Symbol、VariableSymbol、FunctionSymbol的抽象定义
2. **符号表实现在中**：提供SymbolTable的核心实现
3. **解析器在最后**：提供SymbolResolver，展示如何使用符号表
4. **测试作为验证**：最后提供测试文件，用于验证正确性

为什么这样组织：
- AI 可以先理解符号的类型系统，再看如何使用
- 符号表的实现展示了数据结构和算法
- 解析器展示了符号表在实际编译中的应用
- 测试用例让AI知道预期行为

### Prompt 模板（给 AI 用）

**类型 A：符号表扩展实现 Prompt 模板**

```
请为符号表系统实现{新功能}。

任务目标：
- 实现{功能描述}
- 支持{使用场景}
- 保持与现有符号表API的一致性

具体要求：
1. 扩展符号类型
   - 在{existing_file}中添加{新方法/新字段}
   - 保持向后兼容性
   - 添加适当的文档注释

2. 实现功能逻辑
   - 遵循{设计原则}的设计
   - 处理{边界情况}
   - 保持时间复杂度在可接受范围

3. 添加单元测试
   - 测试{正常情况}
   - 测试{边界情况}
   - 测试{错误情况}
   - 使用JUnit 5和AssertJ

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/symtab/SymbolTable.java
  - common/src/main/java/org/teachfx/antlr4/common/symtab/{相关类}.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/symtab/SymbolTableTest.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不修改现有符号表的核心数据结构
- 不破坏向后兼容性
- 所有新增代码必须通过mvn test
- 遵循AGENTS.md中的代码风格规范

期望输出：
1. 新功能实现的完整代码
2. 单元测试代码
3. 使用示例（如果有）
4. 设计决策说明
```

**类型 B：符号解析增强 Prompt 模板**

```
请增强符号解析器以支持{新特性}。

任务目标：
- 在SymbolResolver中添加{功能描述}
- 正确处理{具体场景}
- 生成有意义的错误信息

具体要求：
1. 修改SymbolResolver
   - 在{相关方法}中添加逻辑
   - 处理{新的AST节点类型}
   - 正确更新符号表状态

2. 实现{功能逻辑}
   - 遵循现有代码风格
   - 保持Visitor模式的一致性
   - 正确处理错误情况

3. 添加集成测试
   - 测试{新特性}的各个方面
   - 测试错误情况
   - 测试与现有功能的兼容性

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/symtab/SymbolResolver.java
  - common/src/main/java/org/teachfx/antlr4/common/symtab/SymbolTable.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/symtab/SymbolResolverTest.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不修改符号表的核心数据结构
- 保持Visitor模式的调用约定
- 错误信息要有意义且帮助定位问题
- 所有新增代码必须通过mvn test

期望输出：
1. 增强后的SymbolResolver代码
2. 集成测试代码
3. 使用示例和预期行为说明
```

### AI 应该做 / 不该做

**✅ AI 允许做的事情**：

1. **实现明确界定的符号表功能**
   - ✅ 可以：添加新的符号类型（如类符号、枚举符号）
   - ✅ 可以：增强符号解析逻辑
   - ✅ 可以：添加符号表导出功能
   - ❌ 不能：改变SymbolTable的核心数据结构

2. **生成符号表相关的代码**
   - ✅ 可以：实现新的符号类型
   - ✅ 可以：添加辅助方法
   - ✅ 可以：生成测试用例
   - ❌ 不能：删除现有符号类型

3. **添加符号表工具**
   - ✅ 可以：实现符号表导出（DOT、JSON格式）
   - ✅ 可以：实现符号表比较工具
   - ✅ 可以：生成符号表报告
   - ❌ 不能：修改符号表的查询算法

**❌ AI 禁止做的事情**：

1. **修改符号表核心结构**
   - ❌ 不允许：改变SymbolTable的作用域管理方式
   - ❌ 不允许：修改Symbol的基类定义
   - 原因：核心结构是整个符号表系统的基础

2. **破坏符号解析逻辑**
   - ❌ 不允许：改变SymbolResolver的Visitor模式
   - ❌ 不允许：修改符号解析的顺序
   - 原因：可能引入难以发现的bug

3. **删除测试用例**
   - ❌ 不允许：删除现有的符号表测试
   - ❌ 不允许：降低测试覆盖率
   - 原因：测试是符号表正确性的保证

### 验证与回滚策略

## 自动化验证

**步骤1：运行符号表相关测试**
```bash
# 运行所有符号表测试
cd common
mvn test -Dtest=*SymbolTable*,*SymbolResolver*,*Symbol*

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `SymbolTableTest.java` - 验证符号表基本功能
- `SymbolResolverTest.java` - 验证符号解析
- 其他符号相关测试

**验证标准**：
- ✅ 所有测试通过
- ✅ 符号解析正确
- ✅ 作用域管理正确

**步骤2：编译验证**
```bash
# 清理并重新编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
```

## 手工检查点

**检查1：符号解析正确性**
- [ ] 变量引用正确解析到定义
- [ ] 作用域隐藏规则正确工作
- [ ] 错误信息准确且有帮助

**检查2：代码风格**
- [ ] 遵循AGENTS.md规范
- [ ] 有充分的注释和JavaDoc
- [ ] 命名清晰一致

## 回滚方案

如果AI修改后出现问题：
```bash
# 恢复特定文件
git checkout -- common/src/main/java/org/teachfx/antlr4/common/symtab/

# 或恢复到之前的提交
git checkout <commit_hash> -- common/src/main/java/org/teachfx/antlr4/common/symtab/
```

## 练习题

### 练习1：实现类符号（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
为符号表系统添加类符号（ClassSymbol），支持面向对象编程中的类声明。

要求：
- 完全手工实现，不依赖AI
- 创建ClassSymbol类，包含类名、父类、成员变量、成员方法
- 在符号表中注册类符号
- 编写测试验证功能

验收标准：
- [ ] 代码能编译通过
- [ ] 正确处理类声明和成员
- [ ] 代码风格符合规范
- [ ] 通过所有测试

**💡 解题思路提示**：
- 参考FunctionSymbol的实现结构
- 类的成员需要在类作用域中定义
- 考虑使用List存储成员变量和方法

### 练习2：实现作用域链可视化（AI协作版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
实现符号表的作用域链可视化功能，生成DOT格式的作用域关系图。

AI协作要求：
1. 设计上下文：列出需要提供给AI的文件和说明
2. 设计Prompt：参考本章的Prompt模板
3. 验证AI输出：使用本章的验证策略
4. 理解AI代码：确保你能解释AI生成的每一部分

验收标准：
- [ ] AI生成的代码能编译通过
- [ ] 正确生成DOT格式输出
- [ ] 可视化展示作用域嵌套关系
- [ ] 通过所有测试

### 练习3：实现符号表持久化（综合挑战）

难度：⭐⭐⭐☆☆
预计时间：60–90 分钟

题目描述：
实现符号表的持久化功能，支持将符号表导出为JSON格式，并能够从JSON文件恢复。

要求：
- 可以选择手工实现或AI协作
- 如果选择AI协作，需要详细记录协作过程
- 提交时说明AI参与部分和人工修改部分

验收标准：
- [ ] 功能完整
- [ ] 支持导出和导入
- [ ] 代码可读性良好
- [ ] 有完整的测试覆盖

**💡 解题思路提示**：
- 使用JSON库（如Jackson）进行序列化
- 符号的JSON表示需要包含类型信息
- 考虑处理循环引用（虽然符号表不应该有）

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **符号表核心概念**
   - 理解了符号表在编译器中的位置和作用
   - 掌握了符号的类型体系（变量、函数、类型）
   - 学会了作用域规则和符号解析

2. **符号表实现技术**
   - 学会了使用分层作用域结构
   - 掌握了HashMap实现的高效查找
   - 理解了Visitor模式在符号解析中的应用

3. **实战技能**
   - 能够实现完整的符号表系统
   - 学会了符号解析器的开发
   - 掌握了测试和调试符号表的方法

### 【你现在站在】:
```
... → [AST构建] → ✅ [符号表] → [语义分析] → [IR生成] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器前端的核心基础设施
- 符号表是语义分析和后续阶段的基础
- 下一章将使用符号表进行语义分析和类型检查

### 下一章预告

第13章将聚焦于**语义分析**，你将学习：
- 如何使用符号表进行类型检查
- 如何验证表达式的类型兼容性
- 如何检测语义错误（如类型不匹配、未定义符号）
- 如何生成有意义的错误信息和警告

**准备**：为了学习下一章，建议：
- [ ] 复习本章的符号表实现
- [ ] 运行符号表相关测试，理解输出
- [ ] 阅读AGENTS.md中的语义分析相关部分
- [ ] 思考符号表如何支持类型检查

继续加油！符号表是编译器的"记忆"，掌握了它，你就掌握了理解程序的关键！
