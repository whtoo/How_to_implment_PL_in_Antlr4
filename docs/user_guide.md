# Cymbol 语言用户指南

## 1. Cymbol 语言简介

Cymbol是一种过程式编程语言，设计用于教学和演示编译器构造的基本原理。它支持诸如变量声明、基本数据类型、控制流语句、函数、结构体和数组等核心编程概念。Cymbol编译器 (EP19版本) 能够将Cymbol源代码编译成可执行的中间形式，并提供了一个解释器来运行该代码。

## 2. 安装与运行

### 2.1 获取与构建 (假设)

(此部分内容依赖于项目的具体分发方式，以下为通用假设)

通常，您可以从项目的代码仓库获取Cymbol编译器的源代码。如果项目使用Maven进行构建 (如 `pom.xml` 所示)，您可以使用以下命令构建编译器：

```bash
# 进入项目根目录或 ep19 目录
# cd /path/to/cymbol/ep19
mvn package
```
构建成功后，通常会在 `target/` 目录下找到包含所有依赖的jar包，例如 `ep19-VERSION-jar-with-dependencies.jar`。

### 2.2 编译和运行Cymbol程序

Cymbol编译器 (`org.teachfx.antlr4.ep19.Compiler`) 提供了命令行接口来编译和执行 `.cymbol` 文件。

**基本用法:**

假设您有一个名为 `my_program.cymbol` 的源文件。

*   **编译并立即执行:**
    ```bash
    java -cp <path_to_jar_or_classes> org.teachfx.antlr4.ep19.Compiler my_program.cymbol
    ```
    如果没有提供参数，编译器会尝试加载并执行类路径下的 `t.cymbol` 文件。

*   **仅编译 (生成可序列化的编译结果):**
    ```bash
    java -cp <path_to_jar_or_classes> org.teachfx.antlr4.ep19.Compiler --compile my_program.cymbol my_program.co
    ```
    这会将编译结果（包括AST、符号表等）保存到 `my_program.co` (compiled object) 文件中。

*   **执行已编译的文件:**
    ```bash
    java -cp <path_to_jar_or_classes> org.teachfx.antlr4.ep19.Compiler --execute my_program.co
    ```

*   **静态分析 (检查语法和类型错误，不执行):**
    ```bash
    java -cp <path_to_jar_or_classes> org.teachfx.antlr4.ep19.Compiler --static-analysis my_program.cymbol
    ```

请将 `<path_to_jar_or_classes>` 替换为实际的jar包路径或编译后class文件所在的目录。

## 3. Cymbol 语言基本语法

### 3.1 注释

Cymbol 支持两种类型的注释：

*   单行注释：以 `//` 开始，直到行尾。
    ```cymbol
    // 这是一个单行注释
    int a = 10; // 变量赋值
    ```
*   多行注释：以 `/*` 开始，以 `*/` 结束。
    ```cymbol
    /*
     这是一个
     多行注释。
    */
    int b = 20;
    ```

### 3.2 变量声明

变量在使用前必须声明。声明时指定其类型和名称。可以选择在声明时进行初始化。

**语法:**
`type variableName;`
`type variableName = initialValue;`

**示例:**
```cymbol
int count;
float salary = 60000.50;
boolean isActive = true;
string message = "Hello, Cymbol!";
char initial = 'J';
```

### 3.3 数据类型

#### 3.3.1 基本类型

Cymbol 支持以下基本数据类型：

*   `int`: 表示整数，例如 `10`, `-5`, `0`。
*   `float`: 表示浮点数，例如 `3.14`, `-0.01`。
*   `boolean`: 表示布尔值，只能是 `true` 或 `false`。
*   `string`: 表示文本字符串，用双引号括起来，例如 `"hello"`。
*   `char`: 表示单个字符，用单引号括起来，例如 `'a'`。
*   `void`: 特殊类型，主要用于表示函数没有返回值。不能用于变量声明。

#### 3.3.2 结构体 (Structs)

结构体允许你将多个相关的变量组合成一个单一的命名类型。

**定义:**
```cymbol
struct Point {
    int x;
    int y;
}
```

详见后文 "结构体 (Structs)" 部分。

#### 3.3.3 数组 (Arrays)

数组是相同类型元素的有序集合。

**声明:**
```cymbol
int numbers[10]; // 声明一个包含10个整数的数组
string names[5];  // 声明一个包含5个字符串的数组
```
数组大小必须在声明时指定，且为一个整数表达式。

详见后文 "数组 (Arrays)" 部分。

### 3.4 表达式

表达式是由变量、常量、操作符和函数调用组成的序列，它们计算后会产生一个值。

*   **算术运算符:** `+` (加), `-` (减), `*` (乘), `/` (除), `%` (取模)。
    ```cymbol
    int a = 10 + 5; // 15
    float b = a * 2.0; // 30.0
    int c = 10 % 3;  // 1
    ```
*   **比较运算符:** `==` (等于), `!=` (不等于), `<` (小于), `>` (大于), `<=` (小于等于), `>=` (大于等于)。结果为 `boolean` 类型。
    ```cymbol
    boolean isEqual = (a == 15); // true
    boolean isGreater = (b > 20.0); // true
    ```
*   **逻辑运算符:**
    *   `&&` (逻辑与): `expr1 && expr2`。如果 `expr1` 和 `expr2` 都为 `true`，则结果为 `true`。
    *   `!` (逻辑非): `!expr`。如果 `expr` 为 `false`，则结果为 `true`，反之亦然。
    ```cymbol
    boolean bothTrue = (isEqual && isGreater); // true
    boolean notEqual = !isEqual; // false
    ```
*   **分组:** 可以使用圆括号 `()` 来控制运算的优先级。
    ```cymbol
    int result = (10 + 5) * 2; // 30
    ```

### 3.5 控制流

#### 3.5.1 `if/else` 语句

根据条件执行不同的代码块。`else` 部分是可选的。

**语法:**
```cymbol
if (condition) {
    // 条件为 true 时执行的代码
}

if (condition) {
    // 条件为 true 时执行的代码
} else {
    // 条件为 false 时执行的代码
}
```
`condition` 必须是一个 `boolean` 表达式。

**示例:**
```cymbol
int score = 85;
if (score >= 60) {
    print("及格");
} else {
    print("不及格");
}
```

#### 3.5.2 `while` 循环

当条件为 `true` 时，重复执行代码块。

**语法:**
```cymbol
while (condition) {
    // 条件为 true 时重复执行的代码
}
```
`condition` 必须是一个 `boolean` 表达式。

**示例:**
```cymbol
int i = 0;
while (i < 5) {
    print(i);
    i = i + 1;
}
// 输出: 0 1 2 3 4 (每个数字一行)
```

### 3.6 函数

函数是执行特定任务的可重用代码块。

#### 3.6.1 函数声明

定义函数的名称、参数和返回类型。

**语法:**
```cymbol
returnType functionName(parameterType1 paramName1, parameterType2 paramName2, ...) {
    // 函数体语句
    return returnValue; // 如果 returnType 不是 void
}
```

**示例:**
```cymbol
// 一个简单的加法函数
int add(int a, int b) {
    return a + b;
}

// 一个没有返回值的函数
void greet(string name) {
    print("Hello, " + name + "!");
}
```

#### 3.6.2 函数调用

通过函数名和传递参数来执行函数。

**示例:**
```cymbol
int sum = add(5, 3); // sum 将会是 8
greet("World");      // 输出 "Hello, World!"
```

#### 3.6.3 `return` 语句

`return` 语句用于从函数返回值。如果函数返回类型是 `void`，则 `return` 语句是可选的，或者不带任何值 (`return;`)。如果函数有非 `void` 返回类型，则必须使用 `return` 返回一个兼容类型的值。

### 3.7 结构体 (Structs)

结构体是一种用户定义的数据类型，可以将多个不同类型的项组合到一个单元中。

#### 3.7.1 结构体定义

**语法:**
```cymbol
struct StructName {
    type1 memberName1;
    type2 memberName2;
    // ...
    // 结构体方法 (可选)
    returnType methodName(parameters) {
        // 方法体
    }
}; // 注意：Cymbol EP19 的语法似乎不需要分号结束struct定义，以 .g4 文件为准。
   // 经过核对 Cymbol.g4, structDecl : 'struct' ID '{' structMemeber+ '}' ; 无需分号。
```

**示例:**
```cymbol
struct Point {
    int x;
    int y;

    // 一个简单的方法来移动点
    void move(int dx, int dy) {
        x = x + dx;
        y = y + dy;
    }
};
```

#### 3.7.2 结构体实例化

使用 `new` 关键字创建结构体的实例（对象）。

**语法:**
`StructName instanceName = new StructName();` (Cymbol EP19 的实例化似乎更简单，不需要 `new StructName()`)
`StructName instanceName;` (声明)
然后通过 `instanceName = new ActualStructType()` (如果 `ActualStructType` 是 `StructName` 的一个实现或赋值)。
从 `Cymbol.g4` 的 `exprNew` 规则 (`'new' expr '(' (expr (',' expr)* )? ')'`) 来看，实例化更像是 `instanceName = new Point();` 这种形式，但 `Compiler.java` 和 `Interpreter.java` 中对于 `new` 的处理目前主要集中在 `exprNew` visitor 方法，它期望 `new` 后面跟一个表达式，该表达式解析为一个结构体符号。实际的实例化似乎是在变量声明时隐式发生的，或者 `new` 关键字在当前EP19实现中可能并非用于用户级直接实例化，而是编译器内部使用的。

**更准确的实例化 (基于 `varDecl` 和 `Interpreter`):**
当声明一个结构体类型的变量时，解释器会自动创建一个 `StructInstance`。

```cymbol
struct Vector {
    int x;
    int y;
};

Vector v1; // 声明一个Vector类型的变量 v1，此时v1会被初始化为一个Vector实例
v1.x = 10;
v1.y = 20;
```
EP19 的 `new` 关键字在语法层面存在 (`exprNew` 规则)，但在 `Interpreter` 中对 `visitExprNew` 的实现似乎不完整或与常见的 `new MyStruct()` 形式不同。用户指南将基于变量声明即创建实例的方式。

#### 3.7.3 访问成员

使用点 `.` 操作符访问结构体的字段和方法。

**示例:**
```cymbol
struct Person {
    string name;
    int age;

    void printDetails() {
        print("Name: " + name + ", Age: " + age);
    }
};

Person p1;
p1.name = "Alice";
p1.age = 30;
p1.printDetails(); // 输出: Name: Alice, Age: 30
```

### 3.8 数组 (Arrays)

数组是一系列相同类型元素的集合。

#### 3.8.1 数组声明

在声明数组时，需要指定元素类型和数组大小（元素数量）。数组大小必须是整数。

**语法:**
`elementType arrayName[size];`

**示例:**
```cymbol
int scores[5];      // 一个包含5个整数的数组
float prices[10];   // 一个包含10个浮点数的数组
Point path[3];      // 一个包含3个Point结构体实例的数组
```

#### 3.8.2 数组元素访问

通过索引访问数组中的元素。索引从 `0` 开始，到 `size - 1` 结束。

**语法:**
`arrayName[index]`

**示例:**
```cymbol
int data[3];
data[0] = 10;
data[1] = 20;
data[2] = data[0] + data[1]; // data[2] 将是 30

print(data[1]); // 输出: 20
```
**注意:** Cymbol EP19 不会自动检查数组索引是否越界，越界访问可能导致未定义行为。

### 3.9 类型定义 (`typedef`)

`typedef` 允许为现有的数据类型创建一个新的名称（别名）。

**语法:**
`typedef existingType newTypeName;`

**示例:**
```cymbol
typedef int Integer;
typedef string Name;
typedef Point Vector2D;

Integer count = 100;
Name personName = "Bob";
Vector2D startPoint;
startPoint.x = 0;
startPoint.y = 0;
```

## 4. 标准库 / 内置函数

Cymbol EP19 提供了一个主要的内置函数：

### `print(...)`

`print` 函数用于向控制台输出信息。它可以接受一个或多个任意类型的参数。参数会依次打印出来，最后输出一个换行符。

**示例:**
```cymbol
print("Hello, World!");                 // 输出: Hello, World!
print("Age:", 25, ", Score:", 99.5); // 输出: Age:25, Score:99.5
int x = 10;
print(x);                               // 输出: 10
```

## 5. 简单代码示例

### 示例 1: Hello World 和变量

```cymbol
// my_program.cymbol
string message = "Hello from Cymbol!";
print(message);

int a = 10;
int b = 20;
int sum = a + b;
print("The sum of a and b is: " + sum);

if (sum > 25) {
    print("Sum is greater than 25.");
} else {
    print("Sum is not greater than 25.");
}
```

### 示例 2: 函数和结构体

```cymbol
// functions_and_structs.cymbol

struct Rectangle {
    int width;
    int height;

    int area() {
        return width * height;
    }

    void display() {
        print("Rectangle: " + width + "x" + height);
    }
};

// 函数：计算两个数中的较大者
int max(int num1, int num2) {
    if (num1 > num2) {
        return num1;
    } else {
        return num2;
    }
}

// 主程序逻辑通常在全局作用域或特定的入口函数（如main，如果编译器支持）
// 对于EP19，可以直接在全局作用域编写执行代码

int m = max(15, 7);
print("Max value is: " + m);

Rectangle rect1;
rect1.width = 5;
rect1.height = 10;

rect1.display();                       // 输出: Rectangle: 5x10
print("Area of rect1: " + rect1.area()); // 输出: Area of rect1: 50

Rectangle rect2;
rect2.width = 3;
rect2.height = 7;
print("Area of rect2: " + rect2.area()); // 输出: Area of rect2: 21
```

---
*本用户指南基于对Cymbol EP19编译器实现的分析编写。*
