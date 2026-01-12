# 第15章：IR Generation - 中间表示生成

## 本章概述

本章聚焦于编译器前端的最终输出——中间表示（Intermediate Representation，IR）的生成。通过学习本章，你将掌握IR的设计原则、常用IR形式（如三地址码）以及如何将AST转换为IR。

【你现在站在哪】:
```
... → [语法分析] → [AST构建] → [符号表] → [类型系统] → [语义分析] → ✅ IR生成 → [优化] → [代码生成] → ...
```

## 动机与真实场景

真实场景：你的编译器已经能够检查程序的语义正确性，现在需要生成目标代码。用户希望编译器能够支持多种目标平台（x86、ARM、RISC-V），但你不想为每个平台都重新实现一遍语义分析。

具体问题或挑战：
- AST与目标机器紧密耦合，难以移植到不同平台
- 语义分析与代码生成混在一起，难以维护和扩展
- 没有统一的中间表示，难以进行跨平台的优化
- 生成的代码难以理解和调试

如果缺少本章的能力，你将面临：
- 难以支持多种目标平台
- 无法进行平台无关的优化
- 代码生成逻辑复杂且难以维护
- 编译器架构不够清晰

本章将教你如何：
- 设计独立于源语言和目标语言的中间表示
- 实现AST到IR的转换算法
- 使用三地址码等常用IR形式
- 为后续的优化和代码生成奠定基础

## 人类工程师线：技术与实现

### 核心概念

**中间表示（IR）**是编译器前端和后端之间的桥梁。它是一种抽象的、与源语言和目标机器都无关的程序表示，既能够表达源语言的语义，又便于进行各种分析和优化。

通俗解释：如果源语言是"中文"，目标机器代码是"法文"，那么IR就是"世界语"。世界语既能够表达中文的意思，又容易被翻译成法文。通过使用IR，我们不需要为每种源语言-目标语言的组合都写一个完整的编译器，只需要：
1. 源语言 → IR（前端）
2. IR → 目标语言（后端）

[图1：IR在编译器流水线中的位置]
```
源代码 (Cymbol)
   │
   ▼
前端
├─ 词法分析 → Token
├─ 语法分析 → AST
├─ 语义分析 → 符号表 + 类型信息
└─ IR生成 → 中间表示 (IR)
   │
   ▼
后端
├─ 优化 → 优化的IR
└─ 代码生成 → 目标代码 (ASM/Bytecode)
```

类比理解：IR就像建筑的"建筑图"。建筑工人不需要知道设计师用什么语言交流（中文/英文），只需要看建筑图就能施工。建筑师（前端）把设计意图表达在建筑图（IR）上，施工队（后端）根据建筑图施工。如果要换施工队（目标平台），只需要给同样的建筑图就行。

**三地址码（Three-Address Code，TAC）**是最常用的IR形式之一。它的特点是每条指令最多包含三个地址（操作数），例如：`x = y + z`。三地址码接近汇编语言，但又是结构化的，便于分析和优化。

[图2：三地址码示例]
```
源代码：
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

三地址码：
factorial:
    t1 = n <= 1
    if t1 goto L1
    t2 = n - 1
    t3 = factorial(t2)
    t4 = n * t3
    return t4
L1:
    return 1
```

相关概念：
- **基本块（Basic Block）**：一段顺序执行的代码，没有分支进入或退出（除了入口和出口）
- **控制流图（CFG）**：表示程序控制流的有向图，节点是基本块，边是跳转
- **静态单赋值（SSA）**：每个变量只被赋值一次的IR形式，便于优化
- **四元式（Quadruple）**：三地址码的一种实现形式：(op, arg1, arg2, result)

### 与仓库 EP 的对应关系

对应 EP：EP13-EP16

目录结构：
```
common/
├── src/main/java/org/teachfx/antlr4/common/
│   └── ir/
│       ├── IRNode.java              // IR节点基类
│       ├── IRBuilder.java           // IR构建器
│       ├── tac/                     // 三地址码相关
│       │   ├── TACInstruction.java  // TAC指令
│       │   ├── TACFunction.java     // TAC函数
│       │   └── TACProgram.java      // TAC程序
│       └── ssa/                     // SSA相关
│           ├── SSABuilder.java      // SSA构建器
│           └── SSANode.java         // SSA节点
├── src/test/java/org/teachfx/antlr4/common/
│   └── ir/
│       ├── IRBuilderTest.java       // IR构建器测试
│       └── TACBuilderTest.java      // TAC构建器测试
└── docs/
    └── ir_design.md                 // IR设计文档
```

关键类/方法说明：

**IRNode** - IR节点基类
```java
/**
 * IR节点基类
 * 
 * <p>所有IR节点的父类，定义了IR节点的基本接口。</p>
 */
public abstract class IRNode {
    /** 源位置信息 */
    protected final SourceLocation location;
    
    /** IR类型 */
    protected final IRType type;
    
    /**
     * 构造函数
     * 
     * @param location 源位置信息
     * @param type IR类型
     */
    public IRNode(SourceLocation location, IRType type) {
        this.location = location;
        this.type = type;
    }
    
    /**
     * 获取源位置信息
     * 
     * @return 源位置信息
     */
    public SourceLocation getLocation() {
        return location;
    }
    
    /**
     * 获取IR类型
     * 
     * @return IR类型
     */
    public IRType getType() {
        return type;
    }
    
    /**
     * 获取结果变量（用于链式表达式）
     * 
     * @return 结果变量
     */
    public abstract IRVariable getResult();
    
    /**
     * 接受IR Visitor
     * 
     * @param visitor IR Visitor
     * @return Visitor的处理结果
     */
    public abstract <T> T accept(IRVisitor<T> visitor);
    
    @Override
    public abstract String toString();
}
```

**TACInstruction** - 三地址码指令
```java
/**
 * 三地址码指令
 * 
 * <p>表示一条三地址码指令，格式为：result = op arg1, arg2
 * 实际指令可能少于三个地址（如二元运算、一元运算、跳转）。</p>
 */
public class TACInstruction {
    /** 操作码 */
    private final TACOpcode opcode;
    
    /** 结果变量（可能为null） */
    private final IRVariable result;
    
    /** 操作数1（可能为null） */
    private final IRVariable arg1;
    
    /** 操作数2（可能为null） */
    private final IRVariable arg2;
    
    /** 源位置信息 */
    private final SourceLocation location;
    
    /** 注释（用于调试） */
    private String comment;
    
    /**
     * 指令类型
     */
    public enum TACOpcode {
        // 二元运算
        ADD, SUB, MUL, DIV, MOD,      // 算术运算
        AND, OR, XOR,                  // 位运算
        SHL, SHR,                      // 移位
        
        // 一元运算
        NEG, NOT, INC, DEC,           // 一元运算
        LOAD, STORE,                  // 内存访问
        LOAD_ADDR,                    // 取地址
        
        // 比较
        CMP_EQ, CMP_NE, CMP_LT,       // 比较运算
        CMP_LE, CMP_GT, CMP_GE,
        
        // 控制流
        JUMP, JUMP_EQ, JUMP_NE,       // 条件跳转
        JUMP_LT, JUMP_LE, JUMP_GT, JUMP_GE,
        
        // 函数调用
        CALL, CALL_PARAM,             // 函数调用
        RETURN,                       // 返回
        
        // 标签
        LABEL,                        // 标签
        
        // 类型转换
        CAST_INT_TO_FLOAT,
        CAST_FLOAT_TO_INT,
        CAST_INT_TO_BOOL,
        CAST_BOOL_TO_INT,
        
        // 特殊
        NOP, PHI,                     // NOP和Phi函数（SSA）
        COPY                          // 复制
    }
    
    /**
     * 创建二元运算指令
     */
    public static TACInstruction createBinaryOp(
            TACOpcode opcode, IRVariable result, 
            IRVariable arg1, IRVariable arg2, SourceLocation location) {
        return new TACInstruction(opcode, result, arg1, arg2, location);
    }
    
    /**
     * 创建一元运算指令
     */
    public static TACInstruction createUnaryOp(
            TACOpcode opcode, IRVariable result, 
            IRVariable arg, SourceLocation location) {
        return new TACInstruction(opcode, result, arg, null, location);
    }
    
    /**
     * 创建跳转指令
     */
    public static TACInstruction createJump(
            TACVariable target, SourceLocation location) {
        return new TACInstruction(TACOpcode.JUMP, null, target, null, location);
    }
    
    /**
     * 创建条件跳转指令
     */
    public static TACInstruction createCondJump(
            TACOpcode cmpOpcode, IRVariable condition,
            IRVariable trueTarget, IRVariable falseTarget,
            SourceLocation location) {
        // 条件跳转 = 比较 + 条件跳转
        TACInstruction cmp = new TACInstruction(
            cmpOpcode, null, condition, null, location);
        TACInstruction jump = new TACInstruction(
            TACOpcode.JUMP_EQ, null, trueTarget, null, location);
        // 这里返回的是比较指令，跳转指令需要额外创建
        return cmp;
    }
    
    /**
     * 创建标签指令
     */
    public static TACInstruction createLabel(IRVariable label) {
        return new TACInstruction(TACOpcode.LABEL, label, null, null, null);
    }
    
    /**
     * 创建返回指令
     */
    public static TACInstruction createReturn(IRVariable value, SourceLocation location) {
        return new TACInstruction(TACOpcode.RETURN, null, value, null, location);
    }
    
    /**
     * 创建函数调用指令
     */
    public static TACInstruction createCall(String functionName, IRVariable result, SourceLocation location) {
        IRVariable funcVar = new IRTemporary(functionName, IRType.INT);
        return new TACInstruction(TACOpcode.CALL, result, funcVar, null, location);
    }
    
    /**
     * 创建加载指令
     */
    public static TACInstruction createLoad(
            IRVariable result, IRVariable address, SourceLocation location) {
        return new TACInstruction(TACOpcode.LOAD, result, address, null, location);
    }
    
    /**
     * 创建存储指令
     */
    public static TACInstruction createStore(
            IRVariable value, IRVariable address, SourceLocation location) {
        return new TACInstruction(TACOpcode.STORE, null, value, address, location);
    }
    
    // 私有构造函数
    private TACInstruction(TACOpcode opcode, IRVariable result, 
                          IRVariable arg1, IRVariable arg2, 
                          SourceLocation location) {
        this.opcode = opcode;
        this.result = result;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.location = location;
    }
    
    // Getters
    public TACOpcode getOpcode() { return opcode; }
    public IRVariable getResult() { return result; }
    public IRVariable getArg1() { return arg1; }
    public IRVariable getArg2() { return arg2; }
    public SourceLocation getLocation() { return location; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    /**
     * 转换为字符串表示
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // 添加注释（如果有）
        if (comment != null && !comment.isEmpty()) {
            sb.append("    # ").append(comment).append("\n");
        }
        
        sb.append("    ");
        
        switch (opcode) {
            case LABEL:
                sb.append(result).append(":");
                break;
                
            case JUMP:
                sb.append("goto ").append(arg1);
                break;
                
            case RETURN:
                if (arg1 != null) {
                    sb.append("return ").append(arg1);
                } else {
                    sb.append("return");
                }
                break;
                
            case CALL:
                if (result != null) {
                    sb.append(result).append(" = ");
                }
                sb.append("call ").append(arg1);
                break;
                
            case LOAD:
                sb.append(result).append(" = *").append(arg1);
                break;
                
            case STORE:
                sb.append("*").append(arg2).append(" = ").append(arg1);
                break;
                
            case NOP:
                sb.append("nop");
                break;
                
            default:
                // 二元/一元运算
                if (result != null) {
                    sb.append(result).append(" = ");
                }
                
                if (opcode == TACOpcode.NEG) {
                    sb.append("-").append(arg1);
                } else if (opcode == TACOpcode.NOT) {
                    sb.append("!").append(arg1);
                } else if (opcode == TACOpcode.CAST_INT_TO_FLOAT) {
                    sb.append("(float)").append(arg1);
                } else if (opcode == TACOpcode.CAST_FLOAT_TO_INT) {
                    sb.append("(int)").append(arg1);
                } else {
                    // 二元运算
                    if (arg1 != null) sb.append(arg1);
                    sb.append(" ").append(opcode.toString().toLowerCase()).append(" ");
                    if (arg2 != null) sb.append(arg2);
                }
                break;
        }
        
        return sb.toString();
    }
}
```

**IRBuilder** - IR构建器
```java
/**
 * IR构建器
 * 
 * <p>将AST转换为中间表示（IR）。使用Visitor模式遍历AST，
 * 为每个AST节点生成对应的IR节点或指令序列。</p>
 */
public class IRBuilder extends BaseASTVisitor<IRNode> {
    /** 符号表 */
    private final SymbolTable symbolTable;
    
    /** 类型系统 */
    private final TypeSystem typeSystem;
    
    /** 当前构建的函数 */
    private TACFunction currentFunction;
    
    /** 基本块栈（用于处理嵌套控制流） */
    private final Deque<TACBasicBlock> blockStack;
    
    /** 临时变量计数器 */
    private int tempCounter;
    
    /** 标签计数器 */
    private int labelCounter;
    
    /** IR程序 */
    private final TACProgram irProgram;
    
    /**
     * 构造函数
     * 
     * @param symbolTable 符号表
     * @param typeSystem 类型系统
     */
    public IRBuilder(SymbolTable symbolTable, TypeSystem typeSystem) {
        this.symbolTable = symbolTable;
        this.typeSystem = typeSystem;
        this.blockStack = new ArrayDeque<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
        this.irProgram = new TACProgram();
    }
    
    /**
     * 获取构建的IR程序
     * 
     * @return IR程序
     */
    public TACProgram getIRProgram() {
        return irProgram;
    }
    
    /**
     * 创建新的临时变量
     * 
     * @param type 变量类型
     * @return 临时变量
     */
    public IRVariable createTemp(IRType type) {
        return new IRTemporary("t" + tempCounter++, type);
    }
    
    /**
     * 创建新的标签
     * 
     * @return 标签变量
     */
    public IRVariable createLabel() {
        return new IRTemporary("L" + labelCounter++, IRType.LABEL);
    }
    
    @Override
    public IRNode visitFile(ASTFile node) {
        // 为每个函数创建TACFunction
        for (var funcDecl : node.getFunctions()) {
            TACFunction func = buildFunction(funcDecl);
            irProgram.addFunction(func);
        }
        
        return null;
    }
    
    /**
     * 构建函数
     */
    private TACFunction buildFunction(ASTFunctionDecl funcDecl) {
        currentFunction = new TACFunction(funcDecl.getName());
        
        // 创建入口块
        TACBasicBlock entryBlock = new TACBasicBlock(createLabel());
        currentFunction.setEntryBlock(entryBlock);
        
        // 进入基本块
        blockStack.push(entryBlock);
        
        // 为参数分配空间
        int paramIndex = 0;
        for (var param : funcDecl.getParameters()) {
            Symbol symbol = symbolTable.resolve(param.getName());
            IRVariable paramVar = createTemp(convertIRType(param.getType()));
            if (symbol instanceof VariableSymbol) {
                ((VariableSymbol) symbol).setIRVariable(paramVar);
            }
            paramIndex++;
        }
        
        // 构建函数体
        IRNode bodyResult = funcDecl.getBody().accept(this);
        
        // 添加返回指令（如果没有显式返回）
        if (funcDecl.getReturnType() != BuiltInType.VOID) {
            // 警告：非void函数缺少返回
        }
        
        // 结束当前基本块
        TACBasicBlock currentBlock = blockStack.pop();
        currentFunction.addBlock(currentBlock);
        
        // 构建其他基本块
        // ... (基本块分割逻辑)
        
        return currentFunction;
    }
    
    @Override
    public IRNode visitFunctionDecl(ASTFunctionDecl node) {
        // 函数声明已在buildFunction中处理
        return null;
    }
    
    @Override
    public IRNode visitReturnStmt(ASTReturnStmt node) {
        IRNode value = null;
        if (node.getValue() != null) {
            value = node.getValue().accept(this);
        }
        
        TACBasicBlock currentBlock = blockStack.peek();
        
        if (value != null) {
            IRVariable resultVar = value.getResult();
            TACInstruction retInstr = TACInstruction.createReturn(
                resultVar, new SourceLocation(node.getLine()));
            currentBlock.addInstruction(retInstr);
        } else {
            TACInstruction retInstr = TACInstruction.createReturn(
                null, new SourceLocation(node.getLine()));
            currentBlock.addInstruction(retInstr);
        }
        
        return null;
    }
    
    @Override
    public IRNode visitBinaryExpr(ASTBinaryExpr node) {
        // 递归构建左右操作数
        IRNode left = node.getLeft().accept(this);
        IRNode right = node.getRight().accept(this);
        
        IRVariable leftVar = left.getResult();
        IRVariable rightVar = right.getResult();
        
        // 创建结果临时变量
        IRVariable resultVar = createTemp(convertIRType(node.getType()));
        
        // 选择操作码
        TACInstruction.TACOpcode opcode = convertOperator(node.getOperator());
        
        // 创建指令
        TACInstruction instr = TACInstruction.createBinaryOp(
            opcode, resultVar, leftVar, rightVar,
            new SourceLocation(node.getLine())
        );
        
        TACBasicBlock currentBlock = blockStack.peek();
        currentBlock.addInstruction(instr);
        
        // 返回结果节点
        return new IRExpression(resultVar, convertIRType(node.getType()));
    }
    
    @Override
    public IRNode visitVariableDecl(ASTVariableDecl node) {
        String varName = node.getName();
        Symbol symbol = symbolTable.resolve(varName);
        
        // 创建临时变量
        IRVariable irVar = createTemp(convertIRType(node.getType()));
        if (symbol instanceof VariableSymbol) {
            ((VariableSymbol) symbol).setIRVariable(irVar);
        }
        
        // 如果有初始化，生成赋值指令
        if (node.getInitializer() != null) {
            IRNode initValue = node.getInitializer().accept(this);
            IRVariable initVar = initValue.getResult();
            
            TACInstruction assign = TACInstruction.createBinaryOp(
                TACInstruction.TACOpcode.COPY, irVar, initVar, null,
                new SourceLocation(node.getLine())
            );
            
            TACBasicBlock currentBlock = blockStack.peek();
            currentBlock.addInstruction(assign);
        }
        
        return new IRExpression(irVar, convertIRType(node.getType()));
    }
    
    @Override
    public IRNode visitIdentifier(ASTIdentifier node) {
        String name = node.getName();
        Symbol symbol = symbolTable.resolve(name);
        
        IRVariable resultVar;
        if (symbol instanceof VariableSymbol) {
            resultVar = ((VariableSymbol) symbol).getIRVariable();
        } else {
            resultVar = createTemp(convertIRType(symbol.getType()));
        }
        
        return new IRExpression(resultVar, convertIRType(symbol.getType()));
    }
    
    @Override
    public IRNode visitLiteral(ASTLiteral node) {
        Object value = node.getValue();
        IRType type;
        
        if (value instanceof Integer) {
            type = IRType.INT32;
        } else if (value instanceof Double) {
            type = IRType.FLOAT64;
        } else if (value instanceof Boolean) {
            type = IRType.INT8;
        } else {
            type = IRType.INT32;
        }
        
        // 创建常量临时变量
        IRVariable constVar = new IRConstant(value.toString(), type);
        return new IRExpression(constVar, type);
    }
    
    /**
     * 将语言类型转换为IR类型
     */
    private IRType convertIRType(Type type) {
        if (type == BuiltInType.INT) {
            return IRType.INT32;
        } else if (type == BuiltInType.FLOAT) {
            return IRType.FLOAT64;
        } else if (type == BuiltInType.BOOL) {
            return IRType.INT8;
        } else {
            return IRType.INT32;
        }
    }
    
    /**
     * 将运算符转换为TAC操作码
     */
    private TACInstruction.TACOpcode convertOperator(String op) {
        return switch (op) {
            case "+" -> TACInstruction.TACOpcode.ADD;
            case "-" -> TACInstruction.TACOpcode.SUB;
            case "*" -> TACInstruction.TACOpcode.MUL;
            case "/" -> TACInstruction.TACOpcode.DIV;
            case "%" -> TACInstruction.TACOpcode.MOD;
            case "==" -> TACInstruction.TACOpcode.CMP_EQ;
            case "!=" -> TACInstruction.TACOpcode.CMP_NE;
            case "<" -> TACInstruction.TACOpcode.CMP_LT;
            case "<=" -> TACInstruction.TACOpcode.CMP_LE;
            case ">" -> TACInstruction.TACOpcode.CMP_GT;
            case ">=" -> TACInstruction.TACOpcode.CMP_GE;
            case "&&" -> TACInstruction.TACOpcode.AND;
            case "||" -> TACInstruction.TACOpcode.OR;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };
    }
}
```

**TACFunction** - TAC函数
```java
/**
 * TAC函数
 * 
 * <p>表示一个函数的TAC代码，包含基本块列表。</p>
 */
public class TACFunction {
    /** 函数名 */
    private final String name;
    
    /** 基本块列表（按执行顺序） */
    private final List<TACBasicBlock> blocks;
    
    /** 入口基本块 */
    private TACBasicBlock entryBlock;
    
    /** 参数列表 */
    private final List<IRVariable> parameters;
    
    /** 返回类型 */
    private final IRType returnType;
    
    /**
     * 构造函数
     * 
     * @param name 函数名
     */
    public TACFunction(String name) {
        this.name = name;
        this.blocks = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.returnType = IRType.VOID;
    }
    
    /**
     * 设置入口基本块
     * 
     * @param block 入口块
     */
    public void setEntryBlock(TACBasicBlock block) {
        this.entryBlock = block;
        if (!blocks.contains(block)) {
            blocks.add(block);
        }
    }
    
    /**
     * 添加基本块
     * 
     * @param block 基本块
     */
    public void addBlock(TACBasicBlock block) {
        if (!blocks.contains(block)) {
            blocks.add(block);
        }
    }
    
    /**
     * 获取基本块列表
     * 
     * @return 基本块列表
     */
    public List<TACBasicBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }
    
    /**
     * 生成函数的字符串表示
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# Function: ").append(name).append("\n");
        sb.append("# Parameters: ").append(parameters.size()).append("\n");
        sb.append("# Return type: ").append(returnType).append("\n");
        sb.append("\n");
        
        sb.append(name).append(":\n");
        
        for (var block : blocks) {
            sb.append(block.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
```

**TACProgram** - TAC程序
```java
/**
 * TAC程序
 * 
 * <p>表示完整的TAC程序，包含多个函数。</p>
 */
public class TACProgram {
    /** 函数列表 */
    private final List<TACFunction> functions;
    
    /** 全局变量 */
    private final List<IRVariable> globals;
    
    /**
     * 构造函数
     */
    public TACProgram() {
        this.functions = new ArrayList<>();
        this.globals = new ArrayList<>();
    }
    
    /**
     * 添加函数
     * 
     * @param func 函数
     */
    public void addFunction(TACFunction func) {
        functions.add(func);
    }
    
    /**
     * 获取函数列表
     * 
     * @return 函数列表
     */
    public List<TACFunction> getFunctions() {
        return Collections.unmodifiableList(functions);
    }
    
    /**
     * 生成程序的字符串表示
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# TAC Program\n");
        sb.append("# Functions: ").append(functions.size()).append("\n");
        sb.append("# Globals: ").append(globals.size()).append("\n");
        sb.append("\n");
        
        for (var func : functions) {
            sb.append(func.toString()).append("\n");
        }
        
        return sb.toString();
    }
}
```

### 实战流程

实战步骤：构建和测试IR生成系统

步骤1：编译项目并运行IR生成测试
```bash
# 进入项目根目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4

# 编译项目
mvn clean compile -DskipTests

# 运行IR生成测试
mvn test -Dtest=IRBuilderTest

# 预期输出：
# [INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认测试通过

步骤2：运行TAC构建器测试
```bash
# 运行TAC构建器测试
mvn test -Dtest=TACBuilderTest

# 预期输出：
# [INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

步骤3：端到端IR生成测试
```bash
# 创建测试程序
cat > /tmp/ir_test.cymbol << 'EOF'
int add(int a, int b) {
    return a + b;
}

void main() {
    int x = 5;
    int y = 10;
    int z = add(x, y);
    print(z);
}
EOF

# 运行编译器生成IR
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.common.Compiler" \
    -Dexec.args="--ir /tmp/ir_test.cymbol"

# 预期输出（TAC代码）：
# # TAC Program
# # Functions: 2
# 
# add:
#     t0 = a
#     t1 = b
#     t2 = t0 + t1
#     return t2
# 
# main:
#     t3 = 5
#     x = t3
#     t4 = 10
#     y = t4
#     t5 = x
#     t6 = y
#     t7 = call add
#     z = t7
#     t8 = z
#     print t8
#     return
```

故障排查：

**问题1：IR生成不正确**
- 原因：AST遍历顺序或IR指令生成有误
- 解决方法：
  1. 检查visit方法的递归顺序
  2. 验证临时变量的创建和使用
  3. 确认基本块管理正确

**问题2：类型转换丢失**
- 原因：语言类型到IR类型的转换不正确
- 解决方法：
  1. 检查convertIRType方法
  2. 验证类型映射是否正确
  3. 确认所有类型都有对应的IR类型

**问题3：控制流结构不正确**
- 原因：if/while等控制流的IR生成有误
- 解决方法：
  1. 检查基本块的创建和链接
  2. 验证跳转指令的目标
  3. 确认标签的生成和使用

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI帮助完成IR生成相关的开发任务，我们需要精心设计上下文。

上下文文件列表：

**源码文件**（按阅读顺序）：

1. `common/src/main/java/org/teachfx/antlr4/common/ir/IRNode.java`
   - 作用：IR节点基类
   - 关键方法：`getResult()`, `accept()`

2. `common/src/main/java/org/teachfx/antlr4/common/ir/tac/TACInstruction.java`
   - 作用：三地址码指令
   - 关键方法：`createBinaryOp()`, `createJump()`, `toString()`

3. `common/src/main/java/org/teachfx/antlr4/common/ir/IRBuilder.java`
   - 作用：IR构建器
   - 关键方法：`visitBinaryExpr()`, `visitFunctionDecl()`, `createTemp()`

4. `common/src/main/java/org/teachfx/antlr4/common/ir/tac/TACFunction.java`
   - 作用：TAC函数
   - 关键方法：`addBlock()`, `toString()`

**文档文件**：

1. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：IR设计规范

2. `common/docs/ir_design.md`
   - 作用：IR设计文档
   - 关键章节：三地址码格式、指令集

### Prompt 模板（给 AI 用）

**类型 A：IR生成扩展 Prompt 模板**

```
请为IR生成系统实现{新功能}。

任务目标：
- 实现{功能描述}
- 支持{使用场景}
- 保持与现有IR API的一致性

具体要求：
1. 实现功能逻辑
   - 在{相关类}中添加{方法/字段}
   - 处理{边界情况}
   - 正确生成IR指令

2. 添加测试
   - 测试{新功能}的各个方面
   - 测试边界情况
   - 测试与现有功能的兼容性

参考上下文文件：
- 源码：
  - common/src/main/java/org/teachfx/antlr4/common/ir/IRBuilder.java
  - common/src/main/java/org/teachfx/antlr4/common/ir/tac/TACInstruction.java
- 测试：
  - common/src/test/java/org/teachfx/antlr4/common/ir/IRBuilderTest.java
- 代码规范：
  - AGENTS.md

约束条件：
- 不破坏现有的IR生成流程
- 不修改核心IR数据结构
- 所有新增代码必须通过mvn test

期望输出：
1. 新功能实现的完整代码
2. 测试代码
3. 使用示例和预期行为说明
```

### AI 应该做 / 不该做

**✅ AI 允许的事情**：

1. **实现新的IR指令**
   - ✅ 可以：添加新的TAC指令类型
   - ✅ 可以：扩展指令创建方法
   - ❌ 不能：改变核心指令格式

2. **增强IR构建器**
   - ✅ 可以：添加新的AST节点处理
   - ✅ 可以：优化临时变量管理
   - ❌ 不能：改变Visitor模式结构

3. **添加IR工具**
   - ✅ 可以：实现IR打印工具
   - ✅ 可以：实现IR验证工具
   - ✅ 可以：生成IR可视化

**❌ AI 禁止做的事情**：

1. **破坏IR核心结构**
   - ❌ 不允许：改变IRNode基类的接口
   - ❌ 不允许：修改TACInstruction的基本格式
   - 原因：IR是前端后端的桥梁，必须保持稳定

2. **删除IR功能**
   - ❌ 不允许：移除现有的IR指令
   - ❌ 不允许：禁用重要的IR生成逻辑
   - 原因：IR生成是编译器核心功能

### 验证与回滚策略

## 自动化验证

**步骤1：运行IR生成相关测试**
```bash
# 运行所有IR生成测试
cd common
mvn test -Dtest=*IR*,*TAC*

# 预期输出：
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
```

## 手工检查点

**检查1：IR生成正确性**
- [ ] 所有AST节点都正确生成IR
- [ ] 临时变量命名正确
- [ ] 控制流结构正确

**检查2：代码风格**
- [ ] 遵循AGENTS.md规范
- [ ] 有充分的注释
- [ ] 命名清晰一致

## 回滚方案

```bash
# 恢复特定文件
git checkout -- common/src/main/java/org/teachfx/antlr4/common/ir/
```

## 练习题

### 练习1：实现函数调用指令生成（手工实现版）

难度：⭐⭐☆☆☆
预计时间：30–45 分钟

题目描述：
完善IRBuilder中的函数调用指令生成，确保参数传递和返回值处理正确。

要求：
- 完全手工实现，不依赖AI
- 处理参数列表
- 正确生成CALL指令
- 编写测试验证功能

验收标准：
- [ ] 代码能编译通过
- [ ] 正确生成函数调用IR
- [ ] 代码风格符合规范
- [ ] 通过所有测试

### 练习2：实现控制流指令生成（AI协作版）

难度：⭐⭐⭐☆☆
预计时间：45–60 分钟

题目描述：
完善IRBuilder中的if和while语句的IR生成，正确生成跳转指令和标签。

AI协作要求：
1. 设计上下文：列出需要提供给AI的文件和说明
2. 设计Prompt：参考本章的Prompt模板
3. 验证AI输出：使用本章的验证策略
4. 理解AI代码：确保你能解释AI生成的每一部分

验收标准：
- [ ] AI生成的代码能编译通过
- [ ] 正确生成if/while的IR
- [ ] 跳转指令正确
- [ ] 通过所有测试

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **IR核心概念**
   - 理解了IR在编译器中的位置和作用
   - 掌握了三地址码的基本格式和指令集
   - 学会了IR构建的Visitor模式

2. **IR实现技术**
   - 学会了设计IR数据结构
   - 掌握了AST到IR的转换算法
   - 理解了临时变量和标签的管理

3. **实战技能**
   - 能够实现完整的IR生成器
   - 学会了处理各种语句和表达式的IR生成
   - 掌握了测试IR生成的方法

### 【你现在站在】:
```
... → [语义分析] → ✅ [IR生成] → [优化] → [代码生成] → ...
```

**当前在编译器流水线的位置**：
- 本章完成了编译器前端的最终输出
- IR是后端优化的基础
- 下一阶段将进行IR优化

### 下一章预告

第16章将聚焦于**控制流分析与优化**，你将学习：
- 如何构建程序的控制流图（CFG）
- 如何进行基本块划分
- 如何实现局部优化（如常量折叠、死代码消除）
- 如何进行控制流优化

**准备**：为了学习下一章，建议：
- [ ] 复习本章的IR生成实现
- [ ] 运行IR生成相关测试
- [ ] 阅读AGENTS.md中的优化相关部分
- [ ] 思考IR如何支持各种优化

继续加油！中间表示是编译器的"通用语言"，掌握了它，你的编译器就能支持多种源语言和目标平台！
