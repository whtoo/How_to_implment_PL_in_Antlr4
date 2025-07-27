# EP20语法扩展技术设计文档

## 设计目标
扩展EP20语法以完全支持EP19的所有语法特性，同时保持EP20的架构优势。

## 架构设计

### 1. 语法分层设计
```
Cymbol.g4
├── 基础语法层（已存在）
│   ├── 变量声明
│   ├── 函数声明
│   ├── 控制流
│   └── 基础表达式
├── 扩展语法层（新增）
│   ├── 运算符扩展
│   ├── 数组语法
│   ├── 类型别名
│   └── 结构体系统
└── 词法规则层
    ├── 保留字
    ├── 运算符
    └── 字面量
```

### 2. 模块扩展设计

#### 2.1 语法分析模块
- **文件位置**：`ep20/src/main/antlr4/Cymbol.g4`
- **扩展点**：
  - 添加新的语法规则
  - 扩展现有规则
  - 新增词法符号

#### 2.2 AST节点设计
```
AST节点层次结构
├── Expr（表达式节点）
│   ├── ArrayAccessExpr（数组访问）
│   ├── StructFieldAccessExpr（结构体字段访问）
│   ├── StructMethodCallExpr（结构体方法调用）
│   ├── NewExpr（对象创建）
│   └── BinaryExpr扩展（支持&&和%）
├── Decl（声明节点）
│   ├── StructDecl（结构体声明）
│   ├── TypedefDecl（类型别名声明）
│   └── ArrayVarDecl（数组变量声明）
└── Type（类型节点）
    ├── ArrayType（数组类型）
    └── StructType（结构体类型）
```

#### 2.3 符号表扩展
```
符号表层次结构
├── BaseScope（基础作用域）
├── StructScope（结构体作用域）
└── GlobalScope（全局作用域）

符号类型
├── VariableSymbol（变量符号）
├── FunctionSymbol（函数符号）
├── StructSymbol（结构体符号）
├── TypedefSymbol（类型别名符号）
└── ArraySymbol（数组符号）
```

### 3. 语义分析扩展

#### 3.1 类型检查
- **数组类型检查**：验证数组索引类型为int
- **结构体类型检查**：验证字段存在性和类型匹配
- **类型别名解析**：将typedef名称解析为实际类型

#### 3.2 作用域管理
- **结构体作用域**：处理结构体内部的字段和方法
- **嵌套作用域**：支持结构体嵌套和访问控制

### 4. 代码生成策略

#### 4.1 数组代码生成
- **内存布局**：连续内存块
- **索引计算**：基址 + 索引 * 元素大小
- **边界检查**：运行时检查数组越界

#### 4.2 结构体代码生成
- **内存布局**：字段按声明顺序排列
- **字段访问**：基址 + 字段偏移
- **方法调用**：隐式this指针传递

### 5. 错误处理设计

#### 5.1 语法错误
- **友好的错误消息**：指出具体的语法错误位置
- **建议修复**：提供可能的修复建议

#### 5.2 语义错误
- **类型错误**：详细的类型不匹配信息
- **作用域错误**：未定义符号和作用域冲突

### 6. 兼容性保证

#### 6.1 向后兼容
- 所有EP20现有语法保持不变
- 新增语法作为扩展，不影响现有代码

#### 6.2 迁移路径
- 提供自动迁移工具
- 详细的迁移指南
- 渐进式迁移支持

## 实现细节

### 1. 语法规则实现
```antlr
// 数组语法
arrayDecl : type ID '[' expr ']' ('=' '{' exprList '}')? ';' ;
arrayAccess : expr '[' expr ']' ;

// 结构体语法
structDecl : 'struct' ID '{' structMember+ '}' ;
structMember : type ID ';' | functionDecl ;
structAccess : expr '.' ID ;
methodCall : expr '.' ID '(' exprList? ')' ;

// 类型别名
typedefDecl : 'typedef' type ID ';' ;
```

### 2. AST节点实现
```java
// 数组访问表达式
public class ArrayAccessExpr extends Expr {
    private Expr array;
    private Expr index;
}

// 结构体成员访问
public class StructFieldAccessExpr extends Expr {
    private Expr struct;
    private String fieldName;
}
```

### 3. 符号表实现
```java
// 结构体符号
public class StructSymbol extends Symbol implements Scope {
    private Map<String, Symbol> members = new HashMap<>();
    private Scope enclosingScope;
}
```

## 测试策略

### 1. 单元测试
- 每个语法特性的独立测试
- AST节点正确性验证
- 符号表操作测试

### 2. 集成测试
- 完整程序编译测试
- 运行时行为验证
- 错误场景测试

### 3. 回归测试
- EP19示例代码兼容性测试
- 性能基准测试

## 性能考虑

### 1. 内存优化
- 符号表的延迟加载
- AST节点的内存池

### 2. 编译速度
- 增量编译支持
- 语法分析的优化

## 部署计划

### 阶段1：基础框架
- AST节点定义
- 符号表扩展

### 阶段2：语法实现
- 语法规则添加
- 语义分析实现

### 阶段3：代码生成
- 目标代码生成
- 运行时支持

### 阶段4：测试验证
- 完整测试套件
- 性能优化