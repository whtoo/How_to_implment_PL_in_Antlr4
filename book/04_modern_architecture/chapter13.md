# 第13章：Type System - 类型系统设计与实现

## 本章概述

本章聚焦于编译器中另一个核心基础设施——类型系统（Type System）。通过学习本章，你将掌握类型表达式的表示方法、类型检查算法以及类型推断机制，这是实现静态类型语言编译器的基础。

【你现在站在哪】:
```
... → [语法分析] → [AST构建] → [符号表] → ✅ 类型系统 → [语义分析] → [IR生成] → ...
```

## 动机与真实场景

真实场景：你在为一种新的编程语言实现编译器。用户报告了一个令人困惑的类型错误："incompatible types: int cannot be converted to float"，但用户认为他们没有使用int变量。

具体问题或挑战：
- 编译器无法正确追踪变量的类型信息
- 类型转换规则不清晰，用户难以理解错误信息
- 类型检查的覆盖率不足，某些类型错误未被检测到
- 类型系统的可扩展性差，难以支持泛型等高级特性

如果缺少本章的能力，你将面临：
- 无法实现严格的类型检查，程序安全性无法保证
- 类型错误的信息不准确，用户体验差
- 难以扩展类型系统以支持新特性
- 代码生成阶段可能产生错误的类型转换

本章将教你如何：
- 设计完整的类型表达式层次结构
- 实现高效的类型检查算法
- 处理类型兼容性和隐式转换
- 支持泛型等高级类型特性

## 人类工程师线：技术与实现

### 核心概念

**类型系统**是编译器中用于追踪程序中各个表达式的类型信息的子系统。它定义了类型的表示方法、类型之间的兼容关系以及类型检查的规则。类型系统的主要目标是：
1. 在编译时检测类型错误
2. 指导代码生成阶段的类型转换
3. 为优化提供类型信息

通俗解释：类型系统就像一个严格的质检员。每当程序中使用一个值（变量、表达式、函数返回值），质检员都会检查这个值的"类型标签"是否与期望的类型匹配。例如，如果你试图将一个"苹果"（int类型）放进"橙子盒"（float类型变量），质检员会立即报错："类型不兼容"。

[图1：类型系统在编译器中的位置]
```
源代码
   │
   ▼
词法/语法分析 → AST
   │
   ▼
符号表构建 → 为符号附加类型信息
   │
   ▼
类型检查 → 验证表达式类型兼容性
   │
   ▼
IR生成 → 使用类型信息生成代码
   │
   ▼
代码生成 → 类型转换和内存布局
```

类比理解：想象一个物流系统
- **类型** = 货物类别（易碎品、液体、电子产品）
- **类型检查** = 货物打包前的兼容性验证
- **类型转换** = 货物重新包装或标注
- **类型安全** = 确保不会把牛奶放进电子产品包装

**基本类型**是语言内置的原子类型，通常包括：
- 整数类型（int, long, short）
- 浮点类型（float, double）
- 布尔类型（bool）
- 字符类型（char）
- 空类型（void）

**复合类型**是由基本类型组合而成的类型：
- 数组类型（int[], float[]）
- 函数类型（int(int, float)）
- 结构体类型
- 指针类型

### 与仓库 EP 的对应关系

对应 EP：EP10-EP13

目录结构：
```
common/
├── src/main/java/org/teachfx/antlr4/common/
│   └── type/
│       ├── Type.java                 // 类型基类
│       ├── BuiltInType.java          // 内置基本类型
│       ├── UserDefinedType.java      // 用户定义类型
│       ├── ArrayType.java            // 数组类型
│       ├── FunctionType.java         // 函数类型
│       ├── PointerType.java          // 指针类型
│       ├── TypeTable.java            // 类型表
│       └── TypeChecker.java          // 类型检查器
├── src/test/java/org/teachfx/antlr4/common/
│   └── type/
│       ├── TypeTest.java             // 类型测试
│       └── TypeCheckerTest.java      // 类型检查器测试
└── docs/
    └── type_system_design.md         // 类型系统设计文档
```

关键类/方法说明：

**Type** - 类型基类
```java
/**
 * 类型基类，所有类型的父类
 * 
 * <p>Type类定义了类型的基本接口，包括类型比较、类型名称获取等操作。
 * 具体类型（内置类型、数组类型、函数类型等）继承此类并实现特定行为。</p>
 */
public abstract class Type {
    /** 类型名称 */
    protected final String name;
    
    /** 类型大小（字节），用于代码生成 */
    protected int size;
    
    /**
     * 构造函数
     * 
     * @param name 类型名称
     */
    public Type(String name) {
        this.name = name;
        this.size = 0; // 默认大小为0，子类应设置正确值
    }
    
    /**
     * 获取类型名称
     * 
     * @return 类型名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取类型大小
     * 
     * @return 类型大小（字节）
     */
    public int getSize() {
        return size;
    }
    
    /**
     * 设置类型大小
     * 
     * @param size 类型大小（字节）
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * 检查类型是否是基本类型
     * 
     * @return true表示是基本类型
     */
    public boolean isBuiltInType() {
        return this instanceof BuiltInType;
    }
    
    /**
     * 检查类型是否是数组类型
     * 
     * @return true表示是数组类型
     */
    public boolean isArrayType() {
        return this instanceof ArrayType;
    }
    
    /**
     * 检查类型是否是函数类型
     * 
     * @return true表示是函数类型
     */
    public boolean isFunctionType() {
        return this instanceof FunctionType;
    }
    
    /**
     * 检查类型是否是指针类型
     * 
     * @return true表示是指针类型
     */
    public boolean isPointerType() {
        return this instanceof PointerType;
    }
    
    /**
     * 检查类型是否可转换为目标类型
     * 
     * @param other 目标类型
     * @return true表示可转换
     */
    public boolean isAssignableTo(Type other) {
        return this.equals(other);
    }
    
    /**
     * 获取类型的通用类型（用于类型提升）
     * 
     * @return 通用类型
     */
    public Type getCommonType(Type other) {
        if (this.equals(other)) {
            return this;
        }
        return null;
    }
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract int hashCode();
    
    @Override
    public String toString() {
        return name;
    }
}
```

**BuiltInType** - 内置基本类型
```java
/**
 * 内置基本类型
 * 
 * <p>表示语言内置的基本类型，如int、float、bool等。
 * 这些类型在编译器中预先定义，不需要用户声明。</p>
 */
public final class BuiltInType extends Type {
    /** int类型 */
    public static final BuiltInType INT = new BuiltInType("int", 4);
    
    /** float类型 */
    public static final BuiltInType FLOAT = new BuiltInType("float", 4);
    
    /** double类型 */
    public static final BuiltInType DOUBLE = new BuiltInType("double", 8);
    
    /** bool类型 */
    public static final BuiltInType BOOL = new BuiltInType("bool", 1);
    
    /** char类型 */
    public static final BuiltInType CHAR = new BuiltInType("char", 1);
    
    /** void类型 */
    public static final BuiltInType VOID = new BuiltInType("void", 0);
    
    /** 所有内置类型的注册表 */
    private static final Map<String, BuiltInType> REGISTRY = new HashMap<>();
    
    static {
        REGISTRY.put("int", INT);
        REGISTRY.put("float", FLOAT);
        REGISTRY.put("double", DOUBLE);
        REGISTRY.put("bool", BOOL);
        REGISTRY.put("char", CHAR);
        REGISTRY.put("void", VOID);
    }
    
    /**
     * 私有构造函数
     * 
     * @param name 类型名称
     * @param size 类型大小
     */
    private BuiltInType(String name, int size) {
        super(name);
        this.size = size;
    }
    
    /**
     * 根据名称获取内置类型
     * 
     * @param name 类型名称
     * @return 对应的内置类型，如果不存在返回null
     */
    public static BuiltInType fromName(String name) {
        return REGISTRY.get(name);
    }
    
    /**
     * 获取所有内置类型
     * 
     * @return 内置类型集合
     */
    public static Collection<BuiltInType> allTypes() {
        return REGISTRY.values();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return name.equals(((BuiltInType) obj).name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    /**
     * 检查是否支持隐式转换为目标类型
     * 
     * <p>内置类型的隐式转换规则：</p>
     * <ul>
     *   <li>int可以隐式转换为float、double</li>
     *   <li>float可以隐式转换为double</li>
     *   <li>bool可以隐式转换为int</li>
     * </ul>
     */
    @Override
    public boolean isAssignableTo(Type other) {
        if (this.equals(other)) {
            return true;
        }
        if (other instanceof BuiltInType) {
            BuiltInType otherBuiltIn = (BuiltInType) other;
            
            // int可以转换为float、double
            if (this == INT && (other == FLOAT || other == DOUBLE)) {
                return true;
            }
            
            // float可以转换为double
            if (this == FLOAT && other == DOUBLE) {
                return true;
            }
            
            // bool可以转换为int
            if (this == BOOL && other == INT) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取类型的通用类型（用于类型提升）
     */
    @Override
    public Type getCommonType(Type other) {
        if (this.equals(other)) {
            return this;
        }
        
        if (other instanceof BuiltInType) {
            BuiltInType otherBuiltIn = (BuiltInType) other;
            
            // int + float = float
            if (this == INT && otherBuiltIn == FLOAT) {
                return FLOAT;
            }
            if (this == FLOAT && otherBuiltIn == INT) {
                return FLOAT;
            }
            
            // int/float + double = double
            if (this == INT && otherBuiltIn == DOUBLE) {
                return DOUBLE;
            }
            if (this == FLOAT && otherBuiltIn == DOUBLE) {
                return DOUBLE;
            }
        }
        
        return null;
    }
}
```

**ArrayType** - 数组类型
```java
/**
 * 数组类型
 * 
 * <p>表示语言中的数组类型，如int[]、float[]等。
 * 数组类型包含元素类型和数组维度信息。</p>
 */
public class ArrayType extends Type {
    /** 元素类型 */
    private final Type elementType;
    
    /** 数组长度（-1表示未知/变长） */
    private final int length;
    
    /**
     * 构造函数
     * 
     * @param elementType 元素类型
     * @param length 数组长度，-1表示变长
     */
    public ArrayType(Type elementType, int length) {
        super(elementType.getName() + "[]");
        this.elementType = elementType;
        this.length = length;
        
        // 计算数组大小：元素大小 × 长度
        this.size = elementType.getSize() * (length > 0 ? length : 1);
    }
    
    /**
     * 获取元素类型
     * 
     * @return 元素类型
     */
    public Type getElementType() {
        return elementType;
    }
    
    /**
     * 获取数组长度
     * 
     * @return 数组长度，-1表示变长
     */
    public int getLength() {
        return length;
    }
    
    /**
     * 检查数组是否是定长数组
     * 
     * @return true表示是定长数组
     */
    public boolean isFixedLength() {
        return length > 0;
    }
    
    /**
     * 获取数组的总大小
     * 
     * @return 数组总大小（字节）
     */
    public int getTotalSize() {
        if (length < 0) {
            return elementType.getSize(); // 变长数组只记录元素大小
        }
        return elementType.getSize() * length;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArrayType other = (ArrayType) obj;
        return length == other.length && elementType.equals(other.elementType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(elementType, length);
    }
    
    @Override
    public String toString() {
        if (length > 0) {
            return elementType + "[" + length + "]";
        }
        return elementType + "[]";
    }
}
```

**FunctionType** - 函数类型
```java
/**
 * 函数类型
 * 
 * <p>表示函数的类型，包括参数类型列表和返回类型。
 * 函数类型用于函数指针和函数类型检查。</p>
 */
public class FunctionType extends Type {
    /** 参数类型列表 */
    private final List<Type> parameterTypes;
    
    /** 返回类型 */
    private final Type returnType;
    
    /**
     * 构造函数
     * 
     * @param returnType 返回类型
     * @param parameterTypes 参数类型列表
     */
    public FunctionType(Type returnType, List<Type> parameterTypes) {
        super(makeName(returnType, parameterTypes));
        this.returnType = returnType;
        this.parameterTypes = new ArrayList<>(parameterTypes);
        
        // 函数类型的大小（用于函数指针）
        this.size = 8; // 假设函数指针占8字节
    }
    
    /**
     * 生成函数类型名称
     */
    private static String makeName(Type returnType, List<Type> paramTypes) {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType).append("(");
        
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramTypes.get(i));
        }
        
        sb.append(")");
        return sb.toString();
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
     * 获取参数类型列表
     * 
     * @return 不可修改的参数类型列表
     */
    public List<Type> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }
    
    /**
     * 获取参数数量
     * 
     * @return 参数数量
     */
    public int getParameterCount() {
        return parameterTypes.size();
    }
    
    /**
     * 检查参数数量是否匹配
     * 
     * @param count 参数数量
     * @return true表示匹配
     */
    public boolean matchesParameterCount(int count) {
        return parameterTypes.size() == count;
    }
    
    /**
     * 检查参数类型是否兼容
     * 
     * @param actualTypes 实际参数类型列表
     * @return true表示兼容
     */
    public boolean isCompatibleWith(List<Type> actualTypes) {
        if (parameterTypes.size() != actualTypes.size()) {
            return false;
        }
        
        for (int i = 0; i < parameterTypes.size(); i++) {
            Type formalType = parameterTypes.get(i);
            Type actualType = actualTypes.get(i);
            
            // 实际参数类型必须可赋值给形式参数类型
            if (!actualType.isAssignableTo(formalType)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FunctionType other = (FunctionType) obj;
        return returnType.equals(other.returnType) && 
               parameterTypes.equals(other.parameterTypes);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(returnType, parameterTypes);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
```

**TypeChecker** - 类型检查器
```java
/**
 * 类型检查器
 * 
 * <p>TypeChecker使用Visitor模式遍历AST，检查表达式的类型正确性。
 * 它使用符号表获取变量的类型信息，并验证类型兼容性。</p>
 */
public class TypeChecker extends CymbolBaseVisitor<Type> {
    /** 符号表 */
    private final SymbolTable symbolTable;
    
    /** 错误收集器 */
    private final ErrorCollector errorCollector;
    
    /** 当前函数类型（用于返回语句检查） */
    private Type currentFunctionReturnType;
    
    /** 是否在循环中（用于break/continue检查） */
    private boolean inLoop;
    
    /**
     * 构造函数
     * 
     * @param symbolTable 符号表
     * @param errorCollector 错误收集器
     */
    public TypeChecker(SymbolTable symbolTable, ErrorCollector errorCollector) {
        this.symbolTable = symbolTable;
        this.errorCollector = errorCollector;
        this.currentFunctionReturnType = BuiltInType.VOID;
        this.inLoop = false;
    }
    
    @Override
    public Type visitFile(CymbolParser.FileContext ctx) {
        return super.visitFile(ctx);
    }
    
    @Override
    public Type visitFunctionDecl(CymbolParser.FunctionDeclContext ctx) {
        // 获取函数符号
        String funcName = ctx.ID().getText();
        Symbol symbol = symbolTable.resolve(funcName);
        
        if (symbol instanceof FunctionSymbol) {
            FunctionSymbol funcSymbol = (FunctionSymbol) symbol;
            currentFunctionReturnType = funcSymbol.getReturnType();
        } else {
            currentFunctionReturnType = BuiltInType.VOID;
        }
        
        // 检查函数体
        Type result = super.visitFunctionDecl(ctx);
        
        // 检查函数是否有返回语句
        if (currentFunctionReturnType != BuiltInType.VOID) {
            checkFunctionHasReturn(ctx, result);
        }
        
        currentFunctionReturnType = BuiltInType.VOID;
        return result;
    }
    
    @Override
    public Type visitReturnStmt(CymbolParser.ReturnStmtContext ctx) {
        Type returnType = BuiltInType.VOID;
        
        if (ctx.expression() != null) {
            returnType = visit(ctx.expression());
        }
        
        // 检查返回类型是否兼容
        if (!returnType.isAssignableTo(currentFunctionReturnType)) {
            errorCollector.reportError(
                "incompatible types: " + returnType + " cannot be converted to " + currentFunctionReturnType,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
            );
        }
        
        return returnType;
    }
    
    @Override
    public Type visitBinaryExpr(CymbolParser.BinaryExprContext ctx) {
        Type leftType = visit(ctx.expression(0));
        Type rightType = visit(ctx.expression(1));
        
        String op = ctx.op.getText();
        
        switch (op) {
            case "+":
            case "-":
            case "*":
            case "/":
                // 算术运算：需要数值类型
                if (!isNumericType(leftType) || !isNumericType(rightType)) {
                    errorCollector.reportError(
                        "operator " + op + " cannot be applied to " + leftType + ", " + rightType,
                        ctx.getStart().getLine(),
                        ctx.getStart().getCharPositionInLine()
                    );
                    return BuiltInType.INT; // 返回默认值
                }
                // 类型提升
                return leftType.getCommonType(rightType);
                
            case "==":
            case "!=":
                // 相等运算：需要可比较类型
                if (!isComparableType(leftType) || !isComparableType(rightType)) {
                    errorCollector.reportError(
                        "operator " + op + " cannot be applied to " + leftType + ", " + rightType,
                        ctx.getStart().getLine(),
                        ctx.getStart().getCharPositionInLine()
                    );
                }
                return BuiltInType.BOOL;
                
            case "<":
            case ">":
            case "<=":
            case ">=":
                // 比较运算：需要可比较类型
                if (!isNumericType(leftType) || !isNumericType(rightType)) {
                    errorCollector.reportError(
                        "operator " + op + " cannot be applied to " + leftType + ", " + rightType,
                        ctx.getStart().getLine(),
                        ctx.getStart().getCharPositionInLine()
                    );
                }
                return BuiltInType.BOOL;
                
            case "&&":
            case "||":
                // 逻辑运算：需要布尔类型
                if (!isBooleanType(leftType) || !isBooleanType(rightType)) {
                    errorCollector.reportError(
                        "operator " + op + " cannot be applied to " + leftType + ", " + rightType,
                        ctx.getStart().getLine(),
                        ctx.getStart().getCharPositionInLine()
                    );
                }
                return BuiltInType.BOOL;
                
            default:
                errorCollector.reportError(
                    "unknown operator: " + op,
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine()
                );
                return BuiltInType.INT;
        }
    }
    
    @Override
    public Type visitVarDecl(CymbolParser.VarDeclContext ctx) {
        String varName = ctx.ID().getText();
        Type declaredType = convertType(ctx.type());
        
        // 获取符号
        Symbol symbol = symbolTable.resolve(varName);
        if (symbol instanceof VariableSymbol) {
            ((VariableSymbol) symbol).setType(declaredType);
        }
        
        // 检查初始值类型兼容性
        if (ctx.expression() != null) {
            Type initType = visit(ctx.expression());
            
            if (!initType.isAssignableTo(declaredType)) {
                errorCollector.reportError(
                    "incompatible types: " + initType + " cannot be converted to " + declaredType,
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine()
                );
            }
        }
        
        return declaredType;
    }
    
    @Override
    public Type visitCallExpr(CymbolParser.CallExprContext ctx) {
        String funcName = ctx.ID().getText();
        
        // 解析函数符号
        Symbol symbol = symbolTable.resolve(funcName);
        if (!(symbol instanceof FunctionSymbol)) {
            errorCollector.reportError(
                "cannot find symbol: " + funcName,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
            );
            return BuiltInType.INT;
        }
        
        FunctionSymbol funcSymbol = (FunctionSymbol) symbol;
        List<Type> paramTypes = funcSymbol.getParameterTypes();
        
        // 检查参数数量
        int actualCount = ctx.expressionList() != null ? 
            ctx.expressionList().expression().size() : 0;
        if (!funcSymbol.matchesParameterCount(actualCount)) {
            errorCollector.reportError(
                "wrong number of arguments: expected " + paramTypes.size() + 
                ", found " + actualCount,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
            );
            return funcSymbol.getReturnType();
        }
        
        // 检查参数类型
        if (ctx.expressionList() != null) {
            List<Type> actualTypes = new ArrayList<>();
            for (var expr : ctx.expressionList().expression()) {
                actualTypes.add(visit(expr));
            }
            
            if (!funcSymbol.getFunctionType().isCompatibleWith(actualTypes)) {
                errorCollector.reportError(
                    "argument type mismatch",
                    ctx.getStart().getLine(),
                    ctx.getStart().getCharPositionInLine()
                );
            }
        }
        
        return funcSymbol.getReturnType();
    }
    
    @Override
    public Type visitIdExpr(CymbolParser.IdExprContext ctx) {
        String name = ctx.ID().getText();
        Symbol symbol = symbolTable.resolve(name);
        
        if (symbol == null) {
            errorCollector.reportError(
                "undefined symbol: " + name,
                ctx.getStart().getLine(),
                ctx.getStart().getCharPositionInLine()
            );
            return BuiltInType.INT;
        }
        
        if (symbol instanceof VariableSymbol) {
            return ((VariableSymbol) symbol).getType();
        }
        
        if (symbol instanceof FunctionSymbol) {
            return ((FunctionSymbol) symbol).getFunctionType();
        }
        
        return BuiltInType.INT;
    }
    
    // 辅助方法
    private boolean isNumericType(Type type) {
        return type == BuiltInType.INT || 
               type == BuiltInType.FLOAT || 
               type == BuiltInType.DOUBLE;
    }
    
    private boolean isBooleanType(Type type) {
        return type == BuiltInType.BOOL;
    }
    
    private boolean isComparableType(Type type) {
        return isNumericType(type) || type == BuiltInType.BOOL;
    }
    
    private Type convertType(CymbolParser.TypeContext ctx) {
        if (ctx == null) {
            return BuiltInType.VOID;
        }
        
        String typeName = ctx.getText();
        return BuiltInType.fromName(typeName);
    }
    
    private void checkFunctionHasReturn(CymbolParser.FunctionDeclContext ctx, Type bodyType) {
        // 如果函数体类型是void但声明了非void返回类型，需要返回语句
        if (bodyType == BuiltInType.VOID && currentFunctionReturnType != BuiltInType.VOID) {
            // 这里可以进一步检查是否有return语句
            // 简化处理：只记录警告
        }
    }
}
```

### 实战流程

实战步骤：构建和测试类型系统

步骤1：编译项目并运行类型系统测试
```bash
# 进入项目根目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4

# 编译项目
mvn clean compile -DskipTests

# 运行类型系统测试
mvn test -Dtest=TypeTest

# 预期输出：
# [INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认测试通过，没有失败或错误

步骤2：运行类型检查器测试
```bash
# 运行类型检查器测试
mvn test -Dtest=TypeCheckerTest

# 预期输出：
# [INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

步骤3：运行端到端类型检查测试
```bash
# 创建一个包含类型错误的测试程序
cat > /tmp/type_error.cymbol << 'EOF'
int test() {
    int x = 5;
    float y = 3.14;
    x = y;  // 错误：float不能直接赋值给int
    return x;
}
EOF

# 运行编译器
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.common.Compiler" \
    -Dexec.args="/tmp/type_error.cymbol"

# 预期输出（错误信息）：
# Line 5: incompatible types: float cannot be converted to int
```

故障排查：

**问题1：类型检查不工作**
- 原因：TypeChecker可能没有被正确调用
- 解决方法：
  1. 检查编译器的调用顺序：先符号解析，再类型检查
  2. 确认AST节点附加了符号信息
  3. 验证错误收集器是否正确配置

**问题2：类型转换规则不正确**
- 原因：isAssignableTo或getCommonType方法实现有误
- 解决方法：
  1. 检查内置类型的转换规则
  2. 验证类型提升逻辑
  3. 参考语言规范确认预期行为

**问题3：函数调用类型检查失败**
- 原因：参数类型列表或返回类型获取有误
- 解决方法：
  1. 检查FunctionSymbol的参数类型设置
  2. 验证符号解析是否正确
  3. 确认FunctionType的isCompatibleWith方法

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI帮助完成类型系统相关的开发任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：

1. `common/src/main/java/org/teachfx/antlr4/common/type/Type.java`
   - 作用：定义类型的基类和基本接口
   - 关键方法：`isAssignableTo()`, `getCommonType()`, `equals()`

2. `common/src/main/java/org/teachfx/antlr4/common/type/BuiltInType.java`
   - 作用：内置基本类型的定义和转换规则
   - 关键方法：`fromName()`, `isAssignableTo()`, `getCommonType()`

3. `common/src/main/java/org/teachfx/antlr4/common/type/ArrayType.java`
   - 作用：数组类型的表示
   - 关键方法：`getElementType()`, `getLength()`

4. `common/src/main/java/org/teachfx/antlr4/common/type/FunctionType.java`
   - 作用：函数类型的表示
   - 关键方法：`isCompatibleWith()`, `getParameterTypes()`

5. `common/src/main/java/org/teachfx/antlr4/common/type/TypeChecker.java`
   - 作用：类型检查器的实现
   - 关键方法：`visitBinaryExpr()`, `visitCallExpr()`, `visitReturnStmt()`

**文档文件**：

1. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：代码风格、类型安全规范

2. `common/docs/type_system_design.md`
   - 作用：类型系统设计文档
   - 关键章节：类型层次结构、类型转换规则

**测试文件**：

1. `common/src/test/java/org/teachfx/antlr4/common/type/TypeTest.java`
   - 作用：类型基本功能测试
   - 关键测试方法：`testBuiltInTypes()`, `testArrayTypes()`, `testFunctionTypes()`

2. `common/src/test/java/org/teachfx/antlr4/common/type/TypeCheckerTest.java`
   - 作用：类型检查器测试
   - 关键测试方法：`testBinaryExprType()`, `testFunctionCallType()`, `testTypeErrors()`

### Prompt 模板（给 AI 用）

**类型 A：类型系统扩展 Prompt 模板**

```
请为类型系统实现{新类型}。

任务目标：
- 实现{类型名称}类型
- 支持{功能描述}
- 保持与现有类型API的一致性

具体要求：
1. 实现类型类
   - 创建{TypeName}类继承Type
   - 实现所有抽象方法
   - 添加适当的构造函数和工厂方法

2. 实现类型检查逻辑
   - 实现isAssignableTo()方法
   - 实现getCommonType()方法（如果适用）
   - 处理类型比较和哈希

3. 添加单元测试
   - 测试类型创建
   - 测试类型转换
   - 测试类型比较
   - 使用JUnit 5和AssertJ

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/type/Type.java
  - common/src/main/java/org/teachfx/antlr4/common/type/BuiltInType.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/type/TypeTest.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不修改Type基类的公共接口
- 不破坏现有类型的功能
- 所有新增代码必须通过mvn test
- 遵循AGENTS.md中的代码风格规范

期望输出：
1. 新类型类的完整代码
2. 单元测试代码
3. 使用示例（如果有）
```

**类型 B：类型检查器增强 Prompt 模板**

```
请增强类型检查器以支持{新特性}。

任务目标：
- 在TypeChecker中添加{功能描述}
- 正确处理{具体场景}
- 生成有意义的错误信息

具体要求：
1. 修改TypeChecker
   - 在{相关方法}中添加逻辑
   - 处理{新的AST节点类型}
   - 正确报告类型错误

2. 添加测试
   - 测试{新特性}的各个方面
   - 测试错误情况
   - 测试与现有功能的兼容性

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/type/TypeChecker.java
  - common/src/main/java/org/teachfx/antlr4/common/type/Type.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/type/TypeCheckerTest.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不修改TypeChecker的Visitor模式结构
- 错误信息要有意义且帮助定位问题
- 所有新增代码必须通过mvn test

期望输出：
1. 增强后的TypeChecker代码
2. 测试代码
3. 使用示例和预期行为说明
```

### AI 应该做 / 不该做

**✅ AI 允许的事情**：

1. **实现新的类型**
   - ✅ 可以：添加新的类型类（如结构体类型、枚举类型）
   - ✅ 可以：扩展现有类型的功能
   - ❌ 不能：修改Type基类的核心方法签名

2. **增强类型检查器**
   - ✅ 可以：添加新的类型检查规则
   - ✅ 可以：改进错误信息
   - ✅ 可以：优化类型检查算法
   - ❌ 不能：改变TypeChecker的Visitor模式结构

3. **添加类型工具**
   - ✅ 可以：实现类型打印工具
   - ✅ 可以：实现类型比较工具
   - ✅ 可以：生成类型信息报告

**❌ AI 禁止做的事情**：

1. **修改类型核心接口**
   - ❌ 不允许：改变Type基类的equals/hashCode约定
   - ❌ 不允许：修改isAssignableTo的核心逻辑
   - 原因：类型系统的核心契约必须保持稳定

2. **破坏类型安全**
   - ❌ 不允许：放宽类型检查规则
   - ❌ 不允许：忽略边界情况
   - 原因：类型系统的目的是保证类型安全

3. **删除测试用例**
   - ❌ 不允许：删除现有的类型测试
   - ❌ 不允许：降低测试覆盖率
   - 原因：测试是类型系统正确性的保证

### 验证与回滚策略

## 自动化验证

**步骤1：运行类型系统相关测试**
```bash
# 运行所有类型系统测试
cd common
mvn test -Dtest=*Type*

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

**关键测试**：
- `TypeTest.java` - 验证类型基本功能
- `TypeCheckerTest.java` - 验证类型检查

**验证标准**：
- ✅ 所有测试通过
- ✅ 类型检查正确
- ✅ 类型转换规则正确

## 手工检查点

**检查1：类型系统正确性**
- [ ] 基本类型定义正确
- [ ] 类型转换规则符合语言规范
- [ ] 类型检查覆盖所有表达式类型

**检查2：代码风格**
- [ ] 遵循AGENTS.md规范
- [ ] 有充分的注释和JavaDoc
- [ ] 命名清晰一致

## 回滚方案

```bash
# 恢复特定文件
git checkout -- common/src/main/java/org/teachfx/antlr4/common/type/
```

## 练习题

### 练习1：实现指针类型（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
为类型系统添加指针类型（PointerType），支持C语言风格的指针操作。

要求：
- 完全手工实现，不依赖AI
- 创建PointerType类
- 实现指针类型的基本操作（解引用、地址获取）
- 编写测试验证功能

验收标准：
- [ ] 代码能编译通过
- [ ] 正确处理指针类型
- [ ] 代码风格符合规范
- [ ] 通过所有测试

**💡 解题思路提示**：
- 参考ArrayType的实现结构
- 指针类型包含一个基础类型
- 实现isAssignableTo时考虑指针兼容性

### 练习2：实现泛型类型支持（AI协作版）

难度：⭐⭐⭐⭐☆
预计时间：60–90 分钟

题目描述：
实现泛型类型支持，使类型系统能够处理类型参数。

AI协作要求：
1. 设计上下文：列出需要提供给AI的文件和说明
2. 设计Prompt：参考本章的Prompt模板
3. 验证AI输出：使用本章的验证策略
4. 理解AI代码：确保你能解释AI生成的每一部分

验收标准：
- [ ] AI生成的代码能编译通过
- [ ] 正确处理泛型类型参数
- [ ] 支持泛型实例化
- [ ] 通过所有测试

### 练习3：实现类型推断（综合挑战）

难度：⭐⭐⭐⭐☆
预计时间：90–120 分钟

题目描述：
实现基本类型推断功能，支持局部变量类型推断（如C++的auto、Java的var）。

要求：
- 可以选择手工实现或AI协作
- 如果选择AI协作，需要详细记录协作过程
- 提交时说明AI参与部分和人工修改部分

验收标准：
- [ ] 功能完整
- [ ] 正确推断变量类型
- [ ] 代码可读性良好
- [ ] 有完整的测试覆盖

**💡 解题思路提示**：
- 在类型检查阶段进行类型推断
- 根据初始化表达式推断变量类型
- 处理复杂表达式的类型推断

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **类型系统核心概念**
   - 理解了类型系统在编译器中的位置和作用
   - 掌握了类型的层次结构（基本类型、复合类型）
   - 学会了类型兼容性和转换规则

2. **类型系统实现技术**
   - 学会了使用类层次结构表示类型
   - 掌握了类型检查器的Visitor模式实现
   - 理解了类型提升和隐式转换

3. **实战技能**
   - 能够实现完整的类型系统
   - 学会了类型检查器的开发
   - 掌握了测试类型系统的方法

### 【你现在站在】:
```
... → [符号表] → ✅ [类型系统] → [语义分析] → [IR生成] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器前端的核心类型基础设施
- 类型系统是语义分析和代码生成的基础
- 下一章将整合符号表和类型系统进行完整的语义分析

### 下一章预告

第14章将聚焦于**语义分析**，你将学习：
- 如何整合符号表和类型系统进行完整的语义检查
- 如何检测各种语义错误（未定义符号、类型不匹配、作用域错误）
- 如何实现属性文法驱动的语义分析
- 如何生成有意义的错误信息和警告

**准备**：为了学习下一章，建议：
- [ ] 复习本章的类型系统实现
- [ ] 运行类型系统相关测试
- [ ] 阅读AGENTS.md中的语义分析相关部分
- [ ] 思考如何将符号表和类型系统结合使用

继续加油！类型系统是编译器的"类型安全卫士"，掌握了它，你的编译器就能捕获大部分程序错误！
