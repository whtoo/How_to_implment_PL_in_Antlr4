# 第5章：数组与更复杂的特性

## 1. 章节概述

本章介绍数组和函数调用等高级语言特性的实现，使编程语言从简单的表达式计算器转变为支持复杂数据结构和子程序调用的完整语言。我们将学习如何扩展语法、更新语义分析，并构建支持这些特性的解释器。

## 2. 动机

在实际编程中，简单的变量和表达式往往不足以表达复杂的算法。考虑以下场景：

**数组的需求**：
```
// 计算班级5个学生的平均成绩
int scores[5] = {85, 92, 78, 90, 88};
int sum = 0;
for (int i = 0; i < 5; i++) {
    sum = sum + scores[i];
}
int average = sum / 5;
```

没有数组，我们需要声明5个独立的变量（`score1`, `score2`, `score3`, `score4`, `score5`），代码难以维护和扩展。

**函数调用的需求**：
```
// 计算阶乘
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

int result = factorial(5);
```

没有函数，计算不同数值的阶乘需要复制粘贴相同的代码逻辑。

**完整语言的需求**：
现代编程语言需要同时支持数组、函数、结构体等特性，才能让程序员表达复杂的算法和数据结构。从第4章的符号表基础出发，本章将把这些概念整合到一个完整的语言实现中。

## 3. 人类工程师实现线

### 3.1 核心概念

#### 3.1.1 数组（Arrays）

**数组语义**：
- **连续内存**：数组元素在内存中连续存储，相同类型
- **索引访问**：通过整数索引访问元素（从0开始）
- **边界检查**：确保索引在有效范围内，防止越界访问
- **类型安全**：数组的所有元素具有相同类型

**数组语法示例**：
```c
// 声明固定大小数组
int arr[5];

// 声明并初始化
int numbers[3] = {1, 2, 3};

// 数组访问
arr[0] = 10;
int value = arr[1];

// 数组作为函数参数
int sum(int arr[3]) {
    return arr[0] + arr[1] + arr[2];
}
```

**内存布局示意图**：

```
数组声明: int arr[5] = {10, 20, 30, 40, 50};

内存布局:
+----+----+----+----+----+
| 10 | 20 | 30 | 40 | 50 |
+----+----+----+----+----+
  [0]  [1]  [2]  [3]  [4]

数组基地址 → arr
元素地址 = 基地址 + (索引 × 元素大小)
arr[2] 的地址 = arr + 2 × 4 (int大小)
```

**数组操作的语义**：
- **声明**：分配连续内存空间，记录数组的类型和大小
- **访问**：计算目标元素的内存地址，读取或写入值
- **边界检查**：验证 `0 <= index < size`，否则抛出异常
- **传递**：数组作为参数传递时，通常传递的是引用（地址）

#### 3.1.2 函数调用（Function Calls）

**函数调用语义**：
- **参数传递**：将调用者的实参传递给被调用者的形参
- **返回值**：函数执行完成后返回一个值给调用者
- **调用栈**：维护函数调用链，保存局部变量和返回地址
- **作用域**：每个函数有自己的作用域，局部变量互不干扰

**函数语法示例**：
```c
// 函数定义
int add(int a, int b) {
    return a + b;
}

int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

// 函数调用
int result = add(3, 4);           // result = 7
int fact = factorial(5);           // fact = 120
```

**函数调用栈示意图**：

```
调用: factorial(5)

调用栈从下到上:

+---------------------+  ← 栈顶
| factorial(1)        |
|   n = 1             |
|   return 1          |
+---------------------+
| factorial(2)        |
|   n = 2             |
|   return 2 * 1      |
+---------------------+
| factorial(3)        |
|   n = 3             |
|   return 3 * 2      |
+---------------------+
| factorial(4)        |
|   n = 4             |
|   return 4 * 6      |
+---------------------+
| factorial(5)        |  ← 栈底
|   n = 5             |
|   return 5 * 24     |
+---------------------+
| main()              |
|   result = 120      |
+---------------------+
```

**函数调用的语义**：
1. **参数求值**：从左到右求值每个实参
2. **栈帧分配**：为被调用函数分配新的栈帧
3. **参数绑定**：将实参绑定到形参
4. **函数执行**：执行函数体代码
5. **返回处理**：保存返回值，释放栈帧，返回调用者

#### 3.1.3 完整语言特性

**类型系统**：
- **基本类型**：`int`, `float`, `bool`, `string`, `char`, `void`
- **复合类型**：数组、结构体（struct）
- **类型检查**：编译时验证类型一致性

**控制结构**：
- **条件语句**：`if`-`else`，支持嵌套
- **循环语句**：`while`，支持 `break` 和 `continue`
- **块语句**：`{}` 创建新作用域

**表达式**：
- **算术运算**：`+`, `-`, `*`, `/`, `%`
- **关系运算**：`==`, `!=`, `<`, `<=`, `>`, `>=`
- **逻辑运算**：`&&`, `||`, `!`
- **类型转换**：显式类型转换 `(int)`, `(float)` 等

**完整解释器架构示意图**：

```
源代码 (Cymbol)
    ↓
词法分析 (Lexer)
    ↓ Token流
语法分析 (Parser)
    ↓ ParseTree
AST构建 (ASTBuilder)
    ↓ AST
符号解析 (SymbolResolver)
    ↓ AST + SymbolTable
类型检查 (TypeChecker)
    ↓ 验证通过AST
解释执行 (Interpreter)
    ↓
程序输出
```

### 3.2 EP对应关系

**EP11-EP12：变量与赋值（基础）**

目录结构：
```
ep11/
├── Math.g4                    # 语法文件（表达式解析）
├── Calc.java                  # 主程序
└── src/main/java/
    ├── AdditionNode.java      # 加法节点
    ├── SubtractionNode.java   # 减法节点
    ├── EvalExprVisitor.java   # 表达式求值访问者
    └── BuildAstVisitor.java   # AST构建访问者

ep12/
├── Math.g4                    # 语法文件（添加变量支持）
├── Calc.java                  # 主程序
└── src/main/java/
    ├── VarNode.java           # 变量节点（新增）
    ├── AssignNode.java        # 赋值节点（新增）
    ├── EvalExprVisitor.java   # 求值访问者（添加变量支持）
    └── BuildAstVisitor.java   # AST构建访问者（添加变量支持）
```

关键文件与特性：

**语法扩展（EP12 Math.g4）**：
```antlr
compileUnit
    :   varSlot EOF
    |   assign EOF
    ;

varSlot
    :   '(' varSlot ')'                         # parensExpr
    |   op=('+'|'-') varSlot                    # unaryExpr
    |   left=varSlot op=('*'|'/') right=varSlot # infixExpr
    |   left=varSlot op=('+'|'-') right=varSlot # infixExpr
    |   var=ID                               # varExpr    // 新增变量引用
    |   value=NUM                            # numberExpr
    ;

assign :  name=ID '=' value=varSlot EOF      # assignExpr  // 新增赋值语句
    ;

NUM :   [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
ID  :   [a-zA-Z]+;                           // 新增标识符定义
```

**AST节点（EP12）**：
```java
// VarNode.java - 变量节点
public class VarNode extends ExpressionNode {
    public String name;

    public VarNode(String name) {
        super();
        this.name = name;
    }
}

// AssignNode.java - 赋值节点
public class AssignNode extends ExpressionNode {
    public String varName;
    public ExpressionNode value;

    public AssignNode(String varName, ExpressionNode value) {
        super();
        this.varName = varName;
        this.value = value;
    }
}
```

**求值访问者（EP12 EvalExprVisitor.java）**：
```java
public class EvalExprVisitor implements ASTVisitor<Double> {
    protected Map<String, Double> memory;  // 变量存储

    public EvalExprVisitor() {
        this.memory = new HashMap<>();
    }

    @Override
    public Double visit(AssignNode node) {
        memory.put(node.varName, visit(node.value));
        return memory.get(node.varName);
    }

    @Override
    public Double visit(VarNode node) {
        return memory.get(node.name);
    }
}
```

**实际使用示例（EP12）**：
```
输入:
x = 5
y = 3
x + y

输出:
Result : 5.0
Result : 3.0
Result : 8.0
```

**EP16+：数组与函数（高级特性）**

虽然EP11-EP12仅实现了基础变量，但数组和函数在后续EP中引入：

**EP16+ Cymbol语法（Cymbol.g4）**：
```antlr
// 数组声明与访问
varDecl
    :   type ID ('[' expr ']')? ('=' (expr | arrayInitializer))? ';'
    ;

arrayInitializer
    :   '{' expr (',' expr)* '}'
    ;

expr
    :   expr '[' expr ']'                    # exprArrayAccess
    |   ID '(' ( expr (',' expr)* )? ')'    # exprFuncCall
    // ... 其他表达式
    ;

// 函数定义
functionDecl
    :   retType=type funcName=ID '(' params=formalParameters? ')' blockDef=block
    ;

formalParameters
    :   formalParameter (',' formalParameter)*
    ;

formalParameter
    :   type ID ('[' expr ']')?
    ;
```

**AST节点（EP20）**：
```java
// 数组访问节点
public class ArrayAccessNode extends ExprNode {
    public ExprNode array;
    public ExprNode index;

    public ArrayAccessNode(ExprNode array, ExprNode index) {
        this.array = array;
        this.index = index;
    }
}

// 函数调用节点
public class CallFuncNode extends ExprNode {
    public String funcName;
    public List<ExprNode> args;

    public CallFuncNode(String funcName, List<ExprNode> args) {
        this.funcName = funcName;
        this.args = args;
    }
}
```

**测试用例（EP20 ArraysTest.java）**：
```java
@Test
public void testArrayDeclaration() {
    String source = "int test() { int arr[5]; arr[0] = 10; return arr[0]; }";
    assertTrue(canParse(source), "Should parse array declaration");
}

@Test
public void testArrayWithInitialization() {
    String source = "int test() { int numbers[3] = {1, 2, 3}; return numbers[0]; }";
    assertTrue(canParse(source), "Should parse array with initialization");
}

@Test
public void testArrayAccess() {
    String source = "int test() { int arr[3] = {10, 20, 30}; return arr[1]; }";
    assertTrue(canParse(source), "Should parse array access");
}
```

### 3.3 实践工作流

#### 3.3.1 扩展语法支持数组

**步骤1：更新语法文件**

在语法文件中添加数组声明和访问的规则：

```antlr
varDecl
    :   type ID ('[' expr ']')? ('=' (expr | arrayInitializer))? ';'
    ;

arrayInitializer
    :   '{' expr (',' expr)* '}'
    ;

expr
    :   expr '[' expr ']' # exprArrayAccess
    |   // ... 其他表达式规则
    ;
```

**步骤2：生成解析器代码**

```bash
mvn clean compile
```

**步骤3：创建AST节点**

```java
// ArrayDeclNode.java
public class ArrayDeclNode extends VarDeclNode {
    public int size;
    public List<ExprNode> initialValues;

    public ArrayDeclNode(String name, Type type, int size) {
        super(name, type);
        this.size = size;
        this.initialValues = new ArrayList<>();
    }
}

// ArrayAccessNode.java
public class ArrayAccessNode extends ExprNode {
    public ExprNode arrayRef;
    public ExprNode index;

    public ArrayAccessNode(ExprNode arrayRef, ExprNode index) {
        this.arrayRef = arrayRef;
        this.index = index;
    }
}
```

**步骤4：更新AST构建器**

```java
@Override
public ASTNode visitVarDecl(CymbolParser.VarDeclContext ctx) {
    Type type = visitType(ctx.type());
    String name = ctx.ID().getText();

    if (ctx.expr() != null) {
        // 数组声明
        int size = evaluateArraySize(ctx.expr());
        ArrayDeclNode node = new ArrayDeclNode(name, type, size);

        if (ctx.arrayInitializer() != null) {
            for (CymbolParser.ExprContext exprCtx : ctx.arrayInitializer().expr()) {
                node.initialValues.add((ExprNode) visit(exprCtx));
            }
        }
        return node;
    } else {
        // 普通变量声明
        return new VarDeclNode(name, type);
    }
}
```

#### 3.3.2 实现数组语义分析

**步骤1：扩展符号表**

```java
// ArraySymbol.java
public class ArraySymbol extends VariableSymbol {
    private int size;
    private Type elementType;

    public ArraySymbol(String name, Type elementType, int size) {
        super(name, new ArrayType(elementType, size));
        this.size = size;
        this.elementType = elementType;
    }

    public int getSize() {
        return size;
    }

    public Type getElementType() {
        return elementType;
    }
}
```

**步骤2：实现边界检查**

```java
// ArrayBoundsChecker.java
public class ArrayBoundsChecker {
    public void check(ArrayAccessNode node, SymbolTable symtab) {
        // 检查数组是否存在
        Symbol symbol = symtab.resolve(node.arrayRef);
        if (!(symbol instanceof ArraySymbol)) {
            throw new SemanticException("Expression is not an array: " + node.arrayRef);
        }

        ArraySymbol arraySymbol = (ArraySymbol) symbol;
        int arraySize = arraySymbol.getSize();

        // 检查索引是否为常量表达式
        if (node.index instanceof LiteralExprNode) {
            int index = ((LiteralExprNode) node.index).getValue().intValue();
            if (index < 0 || index >= arraySize) {
                throw new SemanticException(
                    "Array index out of bounds: " + index +
                    " (valid range: 0-" + (arraySize - 1) + ")"
                );
            }
        }
        // 如果索引是变量或表达式，运行时检查
    }
}
```

#### 3.3.3 添加函数调用支持

**步骤1：扩展语法**

```antlr
functionDecl
    :   retType=type funcName=ID '(' params=formalParameters? ')' blockDef=block
    ;

formalParameters
    :   formalParameter (',' formalParameter)*
    ;

formalParameter
    :   type ID ('[' expr ']')?
    ;

expr
    :   ID '(' ( expr (',' expr)* )? ')' # exprFuncCall
    |   // ... 其他表达式规则
    ;
```

**步骤2：创建AST节点**

```java
// FuncDeclNode.java
public class FuncDeclNode extends ASTNode {
    public String name;
    public Type returnType;
    public List<ParamNode> parameters;
    public BlockNode body;

    public FuncDeclNode(String name, Type returnType) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = new ArrayList<>();
    }
}

// ParamNode.java
public class ParamNode extends ASTNode {
    public Type type;
    public String name;
    public boolean isArray;

    public ParamNode(Type type, String name) {
        this.type = type;
        this.name = name;
    }
}

// CallFuncNode.java
public class CallFuncNode extends ExprNode {
    public String funcName;
    public List<ExprNode> args;

    public CallFuncNode(String funcName, List<ExprNode> args) {
        this.funcName = funcName;
        this.args = args;
    }
}
```

**步骤3：实现函数调用语义**

```java
// FunctionCallResolver.java
public class FunctionCallResolver {
    private SymbolTable symtab;

    public void resolve(CallFuncNode node) {
        // 查找函数符号
        Symbol symbol = symtab.resolve(node.funcName);
        if (symbol == null) {
            throw new SemanticException("Undefined function: " + node.funcName);
        }

        if (!(symbol instanceof FunctionSymbol)) {
            throw new SemanticException("Expression is not a function: " + node.funcName);
        }

        FunctionSymbol funcSymbol = (FunctionSymbol) symbol;

        // 检查参数数量
        if (node.args.size() != funcSymbol.getParameterCount()) {
            throw new SemanticException(
                "Parameter count mismatch for function " + node.funcName +
                ": expected " + funcSymbol.getParameterCount() +
                ", got " + node.args.size()
            );
        }

        // 检查参数类型
        for (int i = 0; i < node.args.size(); i++) {
            Type expectedType = funcSymbol.getParameterType(i);
            Type actualType = getType(node.args.get(i));

            if (!isAssignable(actualType, expectedType)) {
                throw new SemanticException(
                    "Parameter type mismatch for function " + node.funcName +
                    ": parameter " + (i + 1) + " expects " + expectedType +
                    ", got " + actualType
                );
            }
        }
    }
}
```

#### 3.3.4 在解释器中执行数组和函数

**步骤1：扩展内存模型**

```java
// Memory.java
public class Memory {
    private Map<String, Object> variables;
    private Map<String, Object[]> arrays;

    public Memory() {
        this.variables = new HashMap<>();
        this.arrays = new HashMap<>();
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public void setArray(String name, int index, Object value) {
        Object[] array = arrays.get(name);
        if (array == null) {
            throw new RuntimeException("Array not found: " + name);
        }
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        array[index] = value;
    }

    public Object getArray(String name, int index) {
        Object[] array = arrays.get(name);
        if (array == null) {
            throw new RuntimeException("Array not found: " + name);
        }
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Array index out of bounds: " + index);
        }
        return array[index];
    }
}
```

**步骤2：实现函数调用执行**

```java
// Interpreter.java
public class Interpreter {
    private Memory globalMemory;
    private Map<String, FunctionSymbol> functions;
    private Stack<Memory> callStack;

    public Interpreter() {
        this.globalMemory = new Memory();
        this.callStack = new Stack<>();
    }

    public Object visit(CallFuncNode node) {
        FunctionSymbol func = functions.get(node.funcName);

        // 创建新的栈帧
        Memory newFrame = new Memory();
        callStack.push(newFrame);

        // 绑定参数
        for (int i = 0; i < node.args.size(); i++) {
            Object value = visit(node.args.get(i));
            newFrame.setVariable(func.getParameterName(i), value);
        }

        // 执行函数体
        Object result = null;
        try {
            result = visit(func.getBody());
        } catch (ReturnException e) {
            result = e.getValue();
        } finally {
            callStack.pop();
        }

        return result;
    }

    public Object visit(ArrayAccessNode node) {
        String arrayName = ((VarNode) node.arrayRef).name;
        int index = (Integer) visit(node.index);

        // 在栈帧中查找数组
        Memory frame = callStack.isEmpty() ? globalMemory : callStack.peek();
        return frame.getArray(arrayName, index);
    }
}
```

#### 3.3.5 测试与验证

**测试数组操作**：
```java
@Test
public void testArrayDeclarationAndAccess() {
    String code = """
    int test() {
        int arr[5];
        arr[0] = 10;
        arr[1] = 20;
        return arr[0] + arr[1];
    }
    """;

    Compiler compiler = new Compiler();
    Object result = compiler.compileAndRun(code);

    assertEquals(30, result);
}

@Test
public void testArrayInitialization() {
    String code = """
    int test() {
        int arr[3] = {1, 2, 3};
        return arr[0] + arr[1] + arr[2];
    }
    """;

    Compiler compiler = new Compiler();
    Object result = compiler.compileAndRun(code);

    assertEquals(6, result);
}

@Test
public void testArrayBoundsChecking() {
    String code = """
    int test() {
        int arr[3] = {1, 2, 3};
        return arr[5];  // 越界访问
    }
    """;

    Compiler compiler = new Compiler();
    assertThrows(RuntimeException.class, () -> compiler.compileAndRun(code));
}
```

**测试函数调用**：
```java
@Test
public void testSimpleFunctionCall() {
    String code = """
    int add(int a, int b) {
        return a + b;
    }

    int test() {
        return add(3, 4);
    }
    """;

    Compiler compiler = new Compiler();
    Object result = compiler.compileAndRun(code);

    assertEquals(7, result);
}

@Test
public void testRecursiveFunction() {
    String code = """
    int factorial(int n) {
        if (n <= 1) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    int test() {
        return factorial(5);
    }
    """;

    Compiler compiler = new Compiler();
    Object result = compiler.compileAndRun(code);

    assertEquals(120, result);
}

@Test
public void testFunctionParameterMismatch() {
    String code = """
    int add(int a, int b) {
        return a + b;
    }

    int test() {
        return add(1);  // 参数数量不匹配
    }
    """;

    Compiler compiler = new Compiler();
    assertThrows(SemanticException.class, () -> compiler.compileAndRun(code));
}
```

## 4. AI协作线

### 4.1 上下文设计

为了让AI助手高效实现数组和函数特性，需要提供充分的上下文信息：

**示例1：语法文件（Math.g4 或 Cymbol.g4）**

```antlr
grammar Math;

compileUnit
    :   varSlot EOF
    |   assign EOF
    ;

varSlot
    :   '(' varSlot ')'                         # parensExpr
    |   op=('+'|'-') varSlot                    # unaryExpr
    |   left=varSlot op=('*'|'/') right=varSlot # infixExpr
    |   left=varSlot op=('+'|'-') right=varSlot # infixExpr
    |   var=ID                               # varExpr
    |   value=NUM                            # numberExpr
    ;

assign :  name=ID '=' value=varSlot EOF      # assignExpr
    ;

NUM :   [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?;
ID  :   [a-zA-Z]+;
WS  :   [ \t\r\n]+ -> skip ;
```

**示例2：现有AST节点实现**

```java
// ExpressionNode.java
public abstract class ExpressionNode extends ASTNode {
    public abstract Type getType();
}

// VarNode.java
public class VarNode extends ExpressionNode {
    public String name;

    public VarNode(String name) {
        this.name = name;
    }

    @Override
    public Type getType() {
        // 从符号表查找类型
        return Type.UNKNOWN;
    }
}

// AssignNode.java
public class AssignNode extends ExpressionNode {
    public String varName;
    public ExpressionNode value;

    public AssignNode(String varName, ExpressionNode value) {
        this.varName = varName;
        this.value = value;
    }

    @Override
    public Type getType() {
        return value.getType();
    }
}
```

**示例3：求值访问者实现**

```java
public class EvalExprVisitor implements ASTVisitor<Double> {
    protected Map<String, Double> memory;

    public EvalExprVisitor() {
        this.memory = new HashMap<>();
    }

    @Override
    public Double visit(AdditionNode node) {
        return visit(node.left) + visit(node.right);
    }

    @Override
    public Double visit(VarNode node) {
        return memory.get(node.name);
    }

    @Override
    public Double visit(AssignNode node) {
        memory.put(node.varName, visit(node.value));
        return memory.get(node.varName);
    }
}
```

**示例4：测试用例**

```java
@Test
public void testVariableAssignment() {
    String source = "x = 5";
    InputStream is = new ByteArrayInputStream(source.getBytes());
    Parser parser = createParser(is);
    ParseTree tree = parser.compileUnit();
    ExpressionNode ast = tree.accept(new BuildAstVisitor());
    EvalExprVisitor visitor = new EvalExprVisitor();
    Double result = visitor.visit(ast);

    assertEquals(5.0, result);
}

@Test
public void testVariableReference() {
    EvalExprVisitor visitor = new EvalExprVisitor();

    // 先赋值
    visitor.visit(new AssignNode("x", new NumberNode(5)));

    // 再引用
    Double result = visitor.visit(new VarNode("x"));

    assertEquals(5.0, result);
}
```

**示例5：预期输出格式**

```
测试文件: VariableTest.java
目标: 验证变量声明、赋值和引用的正确性

输入程序:
x = 10
y = 20
x + y

预期输出:
Result: 10.0
Result: 20.0
Result: 30.0
```

### 4.2 提示词模板

#### 提示词类型A：实现数组边界检查

**背景**：
我们正在为Cymbol语言添加数组支持。语法已经扩展，AST节点已创建。现在需要实现数组边界检查的语义分析Pass。

**输入**：
```
当前语法规则:
expr '[' expr ']' # exprArrayAccess

AST节点:
public class ArrayAccessNode extends ExprNode {
    public ExprNode arrayRef;  // 数组引用
    public ExprNode index;    // 索引表达式
}

符号表:
public interface Symbol {
    String getName();
    Type getType();
}

public class ArraySymbol implements Symbol {
    private int size;
    public int getSize() { return size; }
}
```

**任务**：
实现 `ArrayBoundsChecker` 类，包含以下功能：
1. 检查数组引用是否为有效的数组类型
2. 对于常量索引（如 `arr[5]`），在编译时检查边界
3. 对于变量索引（如 `arr[i]`），在运行时检查边界
4. 提供清晰的错误信息，指出越界位置和有效范围

**要求**：
- 使用访问者模式遍历AST
- 在遍历过程中执行边界检查
- 抛出 `SemanticException` 标识错误
- 编写单元测试验证以下场景：
  - 正常访问：`arr[2]`（数组大小为5）
  - 越界访问：`arr[5]`（数组大小为5）
  - 负索引：`arr[-1]`
  - 非数组类型：`int x; x[0]`

**输出**：
提供完整的Java类实现和测试用例。

---

#### 提示词类型B：生成数组操作测试用例

**背景**：
我们已经实现了数组语法、AST节点和边界检查。现在需要全面的测试用例来验证数组功能的正确性。

**现有功能**：
- 数组声明：`int arr[5]`
- 数组初始化：`int arr[3] = {1, 2, 3}`
- 数组访问：`arr[0]`
- 数组赋值：`arr[1] = 10`
- 数组作为函数参数：`int sum(int arr[3])`

**任务**：
生成全面的JUnit测试用例，覆盖以下场景：

1. **基础测试**
   - 声明固定大小数组
   - 声明并初始化数组
   - 读取数组元素
   - 写入数组元素

2. **边界测试**
   - 访问第一个元素（index=0）
   - 访问最后一个元素（index=size-1）
   - 越界访问（index=size）
   - 负索引访问

3. **类型测试**
   - int数组
   - float数组
   - bool数组

4. **表达式测试**
   - 使用变量作为索引：`arr[i]`
   - 使用表达式作为索引：`arr[i+1]`
   - 复合表达式：`arr[i * 2]`

5. **函数参数测试**
   - 数组作为参数传递
   - 在函数中修改数组元素
   - 返回数组元素

**要求**：
- 使用JUnit 5和AssertJ
- 每个测试场景至少有一个测试方法
- 提供清晰的测试方法名称和注释
- 使用参数化测试覆盖多种输入

**输出**：
提供完整的测试类代码，包含所有测试方法。

---

### 4.3 AI应该做的和不应该做的

#### AI应该做的：

1. **理解项目结构**
   - 识别EP11-EP12的目录布局和关键文件
   - 理解Math.g4语法文件的结构
   - 识别现有AST节点和访问者模式

2. **遵循编码规范**
   - 使用项目指定的包命名：`org.teachfx.antlr4.ep12.ast`
   - 遵循类命名约定：PascalCase（如 `ArrayAccessNode`）
   - 遵循方法命名约定：camelCase（如 `visitArrayAccess`）
   - 按照AGENTS.md中的导入顺序组织代码

3. **实现完整功能**
   - 不仅仅提供片段，而是提供完整的可运行类
   - 包含必要的构造函数、字段和方法
   - 正确处理异常和错误情况

4. **编写可测试代码**
   - 提供清晰的测试用例
   - 使用适当的断言验证预期行为
   - 覆盖正常和错误场景

5. **提供清晰的解释**
   - 解释实现的关键设计决策
   - 说明与现有代码的集成方式
   - 标注需要注意的边界情况

#### AI不应该做的：

1. **不要修改无关代码**
   - 不要更改Math.g4语法文件，除非任务明确要求
   - 不要修改现有的AST节点，除非扩展其功能
   - 不要更改BuildAstVisitor，除非添加新节点支持

2. **不要引入外部依赖**
   - 不要添加未在项目pom.xml中声明的依赖
   - 不要使用未在项目中使用的第三方库
   - 不要假设存在工具类或辅助函数

3. **不要忽略错误处理**
   - 不要忽略可能出现的NullPointerException
   - 不要忽略类型不匹配的情况
   - 不要忽略数组越界的边界情况

4. **不要过度设计**
   - 不要为简单的任务创建复杂的抽象
   - 不要添加不必要的接口或抽象类
   - 不要优化尚未存在的性能问题

5. **不要跳过验证**
   - 不要提供未经编译的代码
   - 不要提供未经测试的实现
   - 不要假设代码会"自动工作"

### 4.4 验证与回滚策略

#### 验证步骤

**步骤1：编译验证**

```bash
# 进入EP12目录
cd ep12

# 清理并编译
mvn clean compile

# 预期输出：
# [INFO] BUILD SUCCESS
# [INFO] Total time:  X.XXX s
```

**步骤2：单元测试验证**

```bash
# 运行所有测试
mvn test

# 预期输出：
# Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

**步骤3：功能验证**

```bash
# 运行特定测试类
mvn test -Dtest=VariableTest

# 预期输出：
# testVariableAssignment PASSED
# testVariableReference PASSED
# testMultipleVariables PASSED
```

**步骤4：集成验证**

```bash
# 运行Calc主程序
echo -e "x=5\ny=3\nx+y" | mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep12.Calc"

# 预期输出：
# Result : 5.0
# Result : 3.0
# Result : 8.0
```

#### 数组功能验证（EP16+）

**测试数组边界检查**：

```bash
# 编译测试程序
cat > test_array_bounds.cymbol << 'EOF'
int test() {
    int arr[5] = {1, 2, 3, 4, 5};
    return arr[5];  // 越界访问
}
EOF

# 运行编译器
cd ep20
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler" \
    -Dexec.args="test_array_bounds.cymbol"

# 预期输出：
# Semantic error: Array index out of bounds: 5 (valid range: 0-4)
```

**测试函数调用**：

```bash
# 创建测试程序
cat > test_function.cymbol << 'EOF'
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

int main() {
    return factorial(5);
}
EOF

# 运行编译器
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler" \
    -Dexec.args="test_function.cymbol"

# 预期输出：
# 120
```

#### Git回滚策略

**策略1：小步提交，便于回滚**

```bash
# 每完成一个小功能，就提交一次
git add src/main/java/org/teachfx/antlr4/ep12/ast/VarNode.java
git commit -m "Add VarNode AST node"

git add src/main/java/org/teachfx/antlr4/ep12/ast/AssignNode.java
git commit -m "Add AssignNode AST node"

git add src/main/java/org/teachfx/antlr4/ep12/visitor/EvalExprVisitor.java
git commit -m "Update EvalExprVisitor to support variables"
```

**策略2：功能分支开发**

```bash
# 创建功能分支
git checkout -b feature/array-support

# 开发功能
# ... 编码和测试 ...

# 测试失败时，回滚到最后一个成功的提交
git reset --hard HEAD~1

# 重新实现
# ... 重新编码 ...
```

**策略3：使用Git stash保存临时修改**

```bash
# 尝试实现新功能
# ... 编码 ...

# 发现编译失败，保存当前工作
git stash save "WIP: array implementation"

# 回到最后一个成功的状态
git reset --hard HEAD~1

# 重新检查问题
# ... 调试 ...

# 恢复保存的修改
git stash pop
```

**策略4：使用Git bisect定位引入Bug的提交**

```bash
# 当发现Bug时，使用bisect定位问题
git bisect start

# 标记已知的好版本
git bisect good v0.1.0

# 标记已知的坏版本
git bisect bad HEAD

# Git会自动切换到中间版本，测试每个版本
git bisect bad  # 如果这个版本有Bug
# 或
git bisect good # 如果这个版本正常

# 重复直到定位到引入Bug的具体提交
```

## 5. 练习

### 练习1（手动实现）：支持多维数组

**任务描述**：
扩展Cymbol语言，支持多维数组的声明和访问。

**需求**：
1. 语法支持：`int matrix[3][4]`（声明3行4列的矩阵）
2. 初始化支持：`int matrix[2][2] = {{1, 2}, {3, 4}}`
3. 访问支持：`matrix[1][0]`（访问第2行第1列）
4. 边界检查：检查每个维度的索引边界

**示例代码**：
```c
int main() {
    int matrix[2][3] = {
        {1, 2, 3},
        {4, 5, 6}
    };

    int value = matrix[1][2];  // value = 6
    return value;
}
```

**提示**：
- 扩展 `ArrayDeclNode`，添加维度列表
- 更新语法规则，支持 `ID '[' expr ']' ('[' expr ']')*`
- 计算元素地址时需要考虑所有维度
- 内存布局可以是行优先或列优先

**验证**：
编写测试用例验证：
- 二维数组声明和初始化
- 二维数组访问和赋值
- 边界检查（每个维度）
- 不同维度的数组（2D、3D）

---

### 练习2（AI辅助）：生成全面的函数调用测试用例

**任务描述**：
使用AI助手生成全面的函数调用测试用例，覆盖各种边界情况和错误场景。

**需求**：
1. **参数传递测试**
   - 基本类型参数（int, float, bool）
   - 数组参数
   - 多参数函数调用

2. **返回值测试**
   - 有返回值的函数
   - void函数
   - 返回复杂类型

3. **递归测试**
   - 简单递归（阶乘）
   - 相互递归
   - 尾递归

4. **作用域测试**
   - 局部变量遮蔽全局变量
   - 嵌套函数调用
   - 函数间的变量共享

5. **错误场景测试**
   - 参数数量不匹配
   - 参数类型不匹配
   - 调用未定义的函数
   - 调用非函数表达式

**预期输出**：
提供完整的测试类代码，包含：
- 至少20个测试方法
- 使用JUnit 5和AssertJ
- 参数化测试覆盖多种输入
- 清晰的测试方法名称和注释

---

### 练习3（调试）：数组边界检查Bug

**场景描述**：
以下代码在运行时产生数组越界错误，但编译器没有检测到。

**Bug代码**：
```c
int main() {
    int arr[5];
    int i = 0;
    while (i <= 5) {  // 注意：应该是 i < 5
        arr[i] = i * 2;
        i = i + 1;
    }
    return arr[5];  // 这一行会越界
}
```

**任务**：
1. 定位为什么编译器没有检测到这个越界错误
2. 修改编译器，使其能够检测并报告这个错误
3. 提供更好的错误信息，帮助程序员理解问题

**思考方向**：
- 编译器是否应该能够检测所有越界访问？
- 对于运行时才能确定的索引，如何处理？
- 如何改进错误信息，使其更具指导性？

**验证**：
修改后的编译器应该能够检测到以下场景：
```
int arr[5];
for (int i = 0; i <= 5; i++) {  // 检测到可能的越界
    arr[i] = i;
}
```

---

## 6. 总结与预览

### 本章总结

本章介绍了数组和函数调用等高级语言特性的实现，使编程语言从简单的表达式计算器转变为支持复杂数据结构和子程序调用的完整语言。

**关键收获**：

1. **数组语义与实现**
   - 数组是连续内存的数据结构，通过索引访问元素
   - 边界检查是确保程序安全的重要机制
   - 数组可以作为参数传递，传递的是引用而非拷贝

2. **函数调用机制**
   - 函数调用涉及参数传递、栈帧管理、返回值处理
   - 递归调用依赖于调用栈的正确维护
   - 类型检查确保参数和返回值的一致性

3. **完整语言架构**
   - 从词法分析到解释执行的完整流水线
   - 符号表和类型系统的作用
   - 访问者模式在AST遍历中的应用

4. **工程实践**
   - 分步骤扩展语言特性
   - 测试驱动的开发方法
   - Git版本管理和回滚策略

**技术要点**：
- 语法扩展：在ANTLR4语法文件中添加新规则
- AST设计：创建对应的AST节点类
- 语义分析：实现符号表和类型检查
- 解释执行：扩展解释器支持新特性
- 测试验证：编写全面的测试用例

### 下章预告：第6章 - 抽象语法树（AST）构建

**章节主题**：
第6章将深入讲解抽象语法树（AST）的设计与构建，这是现代编译器前端的核心组件。

**学习目标**：
1. 理解AST的设计原理和最佳实践
2. 掌握访问者模式在AST遍历中的应用
3. 学习如何从ParseTree构建AST
4. 实现完整的AST构建器

**主要内容**：
- **AST设计原则**：节点类型、访问者模式、不可变性
- **ANTLR4与AST**：使用访问者模式从ParseTree生成AST
- **AST遍历**：深度优先、广度优先、特定节点访问
- **AST优化**：常量折叠、死代码消除、符号解析

**实践项目**：
- 为Cymbol语言设计完整的AST层次结构
- 实现ASTBuilder，将ParseTree转换为AST
- 编写AST访问者，实现符号解析和类型检查
- 使用AST实现静态分析和优化

**为什么AST重要**：
AST是编译器前端和中端的桥梁，提供了：
- 结构化的程序表示
- 与语法无关的抽象
- 易于分析和转换的数据结构
- 支持多遍编译和优化

从第6章开始，我们将进入**模块2：编译基础（EP13-EP16）**，从解释器转向编译器的开发，构建完整的编译器前端。

---

**恭喜完成第5章！** 你已经掌握了数组和函数调用的实现，为构建完整的编程语言打下了坚实的基础。下一章，我们将深入AST的世界，开启编译器前端开发的新篇章。
