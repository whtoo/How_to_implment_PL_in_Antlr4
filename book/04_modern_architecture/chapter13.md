# 第13章：中间表示(IR)设计

## 本章概述

本章聚焦于编译器中间表示(IR)的设计与实现，它是编译器流水线中连接前端与后端的关键桥梁。通过学习本章，你将掌握三地址码的设计原理、IR节点层次架构以及如何将AST转换为IR表示，这是第14章构建控制流图和后续优化的基础。

【你现在站在哪】:
```
... → [类型检查] → ✅ [IR设计] → [CFG构建] → [代码生成] → ...
```

---

## 动机与真实场景

真实场景：你的团队正在开发一个支持多种后端目标（x86、ARM、RISC-V）的编译器。前端团队负责C++语法解析和类型检查，后端团队分为三个小组分别针对不同架构。如果没有统一的中间表示，每个后端团队都需要重复实现语义分析、优化逻辑，导致大量重复工作和维护成本。

具体挑战：
- 前端输出与后端输入之间的表示不匹配
- 每种优化（常量折叠、死代码消除）需要在三个后端重复实现
- 难以在前端和后端之间插入新的优化Pass
- 测试用例需要为每个后端单独编写

如果缺少本章的能力，你将面临：
- 无法有效分离前端和后端开发
- 优化逻辑在多个后端重复实现
- 添加新语言特性需要修改所有后端
- 编译器难以扩展和维护

本章将教你如何：
- 设计语言无关的中间表示
- 将AST转换为三地址码
- 构建可扩展的IR节点层次结构
- 为后续优化和代码生成奠定基础

---

## 人类工程师线：技术与实现

### 核心概念

#### 什么是中间表示(IR)

通俗解释：IR是源代码和目标代码之间的"通用语言"。就像联合国会议使用英语作为工作语言，让不同国家的外交官能够交流一样，IR让编译器前端（依赖源语言）和后端（依赖目标架构）能够解耦协作。

[图1：IR在编译器流水线中的位置]
```
源代码 (Cymbol)
    ↓
[词法分析 + 语法分析 + AST构建 + 类型检查]
    ↓
    ✅ IR生成 (本章) ← 你在这里
    ↓
[优化Pass (常量折叠、死代码消除)]
    ↓
[代码生成 (EP18 VM字节码)]
    ↓
目标代码
```

**IR的核心价值**：
1. **解耦**: 前端只需关心语言特性，后端只需关心目标架构
2. **优化**: 在IR层面进行优化，一次实现，所有后端受益
3. **可移植**: 同一前端可以对接多个后端
4. **分析**: 便于进行数据流分析、控制流分析

#### 三地址码设计

三地址码是IR的一种经典形式，每条指令最多包含三个操作数：两个源操作数和一个目标操作数。

[图2：三地址码结构]
```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│  结果操作数  │      =      │  操作数1    │  操作符     │  操作数2    │
├─────────────┼─────────────┼─────────────┼─────────────┤
│     a       │      =      │     b       │     +       │     c       │
│    t1       │      =      │     x       │     *       │     y       │
│    a        │      =      │     b       │     +       │     t1      │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────────┘
```

**三地址码的优点**：
- 每条指令简单明确，便于优化和分析
- 打破复杂表达式为简单指令序列
- 便于寄存器分配和指令选择

**示例转换**：
```c
// 源代码
int result = (a + b) * (c - d) / e;
```

```java
// 三地址码 IR
t1 = a + b
t2 = c - d
t3 = t1 * t2
result = t3 / e
```

#### IR节点层次架构

EP20采用清晰的面向对象设计，通过继承和多态构建灵活的IR节点系统。

[图3：EP20 IR节点层次结构]
```
IRNode (抽象基类) ← 所有IR节点的根
    ├── Stmt (语句节点) ← 表示动作，可能不返回值
    │   ├── Assign (赋值: lhs = rhs)
    │   ├── JMP (无条件跳转)
    │   ├── CJMP (条件跳转: if cond goto label)
    │   ├── Label (跳转目标标记)
    │   ├── FuncEntryLabel (函数入口标记)
    │   └── ReturnVal (返回语句)
    │
    └── Expr (表达式节点) ← 表示值，可以被赋值或作为操作数
        ├── ConstVal<T> (常量: 5, "hello", true)
        ├── BinExpr (二元运算: +, -, *, /, <, >)
        ├── UnaryExpr (一元运算: -, !)
        ├── CallFunc (函数调用)
        └── VarSlot (变量槽位)
            ├── FrameSlot (栈帧变量)
            └── OperandSlot (临时操作数)
```

**设计要点**：
- **IRNode**: 所有IR节点的抽象基类，提供统一接口
- **Stmt vs Expr**: 明确区分语句(动作)和表达式(值)
- **VarSlot抽象**: 统一处理栈变量、临时变量、参数等
- **Visitor模式**: 通过IRVisitor实现操作解耦

### 与仓库 EP 的对应关系

对应 EP：EP20 (中间表示与完整编译器)

目录结构：
```
ep20/
├── src/main/java/org/teachfx/antlr4/ep20/
│   ├── ir/                          // IR节点定义
│   │   ├── IRNode.java             // IR节点基类
│   │   ├── Prog.java               // 程序表示(包含多个基本块)
│   │   ├── IRVisitor.java          // IR访问者接口
│   │   ├── IRVisitorAdapter.java   // 默认实现适配器
│   │   ├── stmt/                   // 语句节点
│   │   │   ├── Stmt.java          // 语句基类
│   │   │   ├── Assign.java        // 赋值语句
│   │   │   ├── JMP.java           // 无条件跳转
│   │   │   ├── CJMP.java          // 条件跳转
│   │   │   ├── Label.java         // 标签
│   │   │   └── ReturnVal.java     // 返回
│   │   └── expr/                   // 表达式节点
│   │       ├── Expr.java          // 表达式基类
│   │       ├── BinExpr.java       // 二元运算
│   │       ├── UnaryExpr.java     // 一元运算
│   │       ├── ConstVal.java      // 常量
│   │       ├── VarSlot.java       // 变量槽位
│   │       └── CallFunc.java      // 函数调用
│   └── pass/ir/
│       └── CymbolIRBuilder.java   // IR生成器(将AST转换为IR)
```

#### IRNode - IR节点基类

```java
package org.teachfx.antlr4.ep20.ir;

/**
 * IRNode是所有中间表示节点的抽象基类。
 * 它本身非常简单，但通过继承和多态机制，提供了统一的IR节点接口。
 * 
 * 设计考虑：
 * - 保持基类最小化，让具体子类实现特定行为
 * - 通过Visitor模式实现操作，避免在节点中耦合具体逻辑
 */
public abstract class IRNode {
    // 基类为空，具体功能由子类实现
    // Visitor模式在IRVisitor接口中定义
}
```

#### Prog - 程序表示

```java
package org.teachfx.antlr4.ep20.ir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

/**
 * Prog表示一个完整的程序，包含多个基本块(LinearIRBlock)。
 * 
 * 核心职责：
 * 1. 管理所有基本块：blockList存储程序中的所有基本块
 * 2. 提供IR指令列表：instrs存储所有指令，用于线性遍历
 * 3. 支持空块优化：optimizeEmptyBlock方法删除空基本块并修复跳转
 * 
 * 工作流程：
 * 1. IRBuilder为每个函数创建一个或多个LinearIRBlock
 * 2. 所有LinearIRBlock添加到blockList中
 * 3. 优化Pass可能删除空块或合并块
 * 4. CodeGen遍历blockList生成目标代码
 */
public class Prog extends IRNode {
    // 基本块列表：每个基本块包含一组按顺序执行的指令
    public List<LinearIRBlock> blockList;
    
    // 所有指令的线性列表，便于遍历
    public List<IRNode> instrs = new ArrayList<>();
    
    // 需要删除的基本块集合（优化过程中标记）
    protected TreeSet<LinearIRBlock> needRemovedBlocks = new TreeSet<>();

    public Prog() {
        this.blockList = new ArrayList<>();
    }

    /**
     * 优化空基本块
     * 如果基本块为空且没有后继，直接删除
     * 如果基本块为空但有后继，将所有前驱的跳转目标重定向到后继
     */
    protected void optimizeEmptyBlock(@NotNull LinearIRBlock linearIRBlock) {
        // 如果基本块为空
        if (linearIRBlock.getStmts().isEmpty()){
            // 情况1：没有后继，直接删除该块
            if (linearIRBlock.getSuccessors().isEmpty()) {
                needRemovedBlocks.add(linearIRBlock);
            } else {
                // 情况2：有后继，重定向所有跳转
                var nextBlock = linearIRBlock.getSuccessors().get(0);
                // 更新JMP和CJMP指令的目标
                for (var ref : linearIRBlock.getJmpRefMap()) {
                    if (ref instanceof JMP jmp) {
                        jmp.setNext(nextBlock);  // 无条件跳转向后继
                    } else if (ref instanceof CJMP cjmp) {
                        cjmp.setElseBlock(nextBlock);  // 条件跳转的else分支指向后继
                    }
                }
                // 更新前驱的successors列表
                linearIRBlock.getPredecessors().forEach(prev -> {
                    prev.removeSuccessor(linearIRBlock);
                    prev.getSuccessors().add(nextBlock);
                });
            }
        }
    }
}
```

#### IRVisitor - IR访问者接口

```java
package org.teachfx.antlr4.ep20.ir;

/**
 * IRVisitor是访问者模式的核心接口，定义了所有IR节点的访问方法。
 * 
 * 设计优点：
 * 1. 操作解耦：将IR节点的操作从节点类中分离出来
 * 2. 易于扩展：新增操作时只需实现新的Visitor，无需修改节点类
 * 3. 类型安全：每个visit方法对应具体的节点类型
 * 
 * 使用场景：
 * - IRPrinter：打印IR代码
 * - IRAnalyzer：分析IR结构
 * - IRTransformer：转换IR形式
 * - CodeGen：生成目标代码
 */
public interface IRVisitor<S, E> {
    // 访问二元表达式
    E visit(BinExpr node);
    
    // 访问一元表达式
    E visit(UnaryExpr node);
    
    // 访问函数调用
    E visit(CallFunc callFunc);
    
    // 访问标签
    S visit(Label label);
    
    // 访问无条件跳转
    S visit(JMP jmp);
    
    // 访问条件跳转
    S visit(CJMP cjmp);
    
    // 访问赋值语句
    S visit(Assign assign);
    
    // 访问返回值
    S visit(ReturnVal returnVal);
    
    // 默认访问方法，提供统一入口
    default S visit(Stmt stmt) { return stmt.accept(this); }
    
    default S visit(Prog prog) { return null; }
    
    default S visit(ExprStmt exprStmt) { return exprStmt.accept(this); }
}
```

#### Assign - 赋值语句

```java
package org.teachfx.antlr4.ep20.ir.stmt;

import org.teachfx.antlr4.ep20.ir.IRVisitor;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.ir.expr.VarSlot;

/**
 * Assign表示赋值语句：lhs = rhs
 * 
 * 设计特点：
 * - lhs必须是VarSlot（变量槽位，可以是栈变量或临时变量）
 * - rhs是Operand（操作数），可以是常量、变量或表达式结果
 * - 使用工厂方法创建，确保对象创建的一致性
 * 
 * 示例：
 * Assign.with(frameSlot, constVal)  // x = 10
 * Assign.with(tempSlot, binExpr)    // t1 = a + b
 */
public class Assign extends Stmt {
    protected VarSlot lhs;      // 左值：必须是变量
    protected Operand rhs;      // 右值：可以是常量、变量或表达式结果

    // 工厂方法1：两个都是变量的简化版本
    @SuppressWarnings("unused")
    public static Assign with(VarSlot lhs, VarSlot rhs) {
        return new Assign(lhs, rhs);
    }
    
    // 工厂方法2：通用版本，rhs可以是任意Operand
    public static Assign with(VarSlot lhs, Operand rhs) {
        return new Assign(lhs, rhs);
    }
    
    // 构造函数（包私有，鼓励使用工厂方法）
    public Assign(VarSlot lhs, Operand rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    // Getter方法
    public VarSlot getLhs() { return lhs; }
    public Operand getRhs() { return rhs; }

    // Setter方法（用于优化阶段修改IR）
    public void setLhs(VarSlot lhs) { this.lhs = lhs; }
    public void setRhs(Operand rhs) { this.rhs = rhs; }

    @Override
    public <S, E> S accept(IRVisitor<S, E> visitor) {
        return visitor.visit(this);  // 调用visitor的visit(Assign)方法
    }

    @Override
    public StmtType getStmtType() {
        return StmtType.ASSIGN;  // 返回ASSIGN类型
    }

    @Override
    public String toString() {
        return "%s = %s".formatted(getLhs(), getRhs());
    }
}
```

#### IR生成流程：AST → IR

```java
package org.teachfx.antlr4.ep20.pass.ir;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
// 其他import...

/**
 * CymbolIRBuilder将AST节点转换为IR指令序列。
 * 
 * 工作流程：
 * 1. 自顶向下遍历AST
 * 2. 为每个表达式创建临时变量存储结果
 * 3. 为每个语句生成对应的IR指令
 * 4. 维护基本块和跳转关系
 * 
 * 关键设计：
 * - evalExprStack：存储表达式求值结果，用于嵌套表达式
 * - breakStack/continueStack：处理循环中的break和continue
 * - currentBlock：当前正在构建的基本块
 */
public class CymbolIRBuilder implements ASTVisitor<Void, VarSlot> {
    private static final Logger logger = LogManager.getLogger(CymbolIRBuilder.class);
    
    // 生成的程序
    public Prog prog = null;
    
    // 当前基本块
    private LinearIRBlock currentBlock = null;
    
    // 用于break和continue语句的栈
    private Stack<LinearIRBlock> breakStack;
    private Stack<LinearIRBlock> continueStack;
    
    // 表达式求值栈，存储表达式的IR结果
    private Stack<VarSlot> evalExprStack;

    @Override
    public Void visit(CompileUnit compileUnit) {
        // 创建新的Prog对象表示整个程序
        prog = new Prog();
        // 遍历所有函数声明，为每个函数生成IR
        compileUnit.getFuncDeclarations().forEach(x -> x.accept(this));
        return null;
    }

    @Override
    public Void visit(BinaryExprNode node) {
        logger.debug("访问二元表达式: %s".formatted(node.toString()));
        
        // 递归访问左右子表达式
        node.getLhs().accept(this);
        VarSlot lhs = peekEvalOperand();  // 左操作数的IR结果
        
        node.getRhs().accept(this);
        VarSlot rhs = peekEvalOperand();  // 右操作数的IR结果
        
        // 创建新的临时变量存储运算结果
        VarSlot result = FrameSlot.get(new VariableSymbol("t" + tempCount++));
        
        // 根据运算符创建对应的二元表达式节点
        BinExpr.Operator op = mapOperator(node.getOperator());
        BinExpr binExpr = new BinExpr(op, lhs, rhs);
        
        // 生成赋值语句：result = binExpr
        addInstr(Assign.with(result, binExpr));
        
        // 将结果压入栈，供上层表达式使用
        pushEvalOperand(result);
        
        return null;
    }

    @Override
    public Void visit(AssignStmtNode node) {
        logger.debug("访问赋值语句: %s".formatted(node.toString()));
        
        // 计算右表达式的IR
        node.getRhs().accept(this);
        VarSlot rhsValue = popEvalOperand();
        
        // 获取左值的变量槽位
        VarSlot lhsSlot = getVarSlot(node.getLhs());
        
        // 生成赋值指令
        addInstr(Assign.with(lhsSlot, rhsValue));
        
        return null;
    }
    
    // 辅助方法：向当前基本块添加指令
    private void addInstr(Stmt stmt) {
        if (currentBlock != null) {
            currentBlock.addStmt(stmt);
        } else {
            prog.instrs.add(stmt);  // 如果不在任何块中，添加到全局列表
        }
    }
    
    // 辅助方法：操作数栈操作
    private void pushEvalOperand(VarSlot slot) { evalExprStack.push(slot); }
    private VarSlot popEvalOperand() { return evalExprStack.pop(); }
    private VarSlot peekEvalOperand() { return evalExprStack.peek(); }
}
```

### 实战流程

#### 步骤1：查看IR节点定义

```bash
# 进入EP20目录
cd ep20

# 查看IR节点基类
cat src/main/java/org/teachfx/antlr4/ep20/ir/IRNode.java

# 查看程序表示类
cat src/main/java/org/teachfx/antlr4/ep20/ir/Prog.java

# 查看IR访问者接口
cat src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java
```

预期输出：看到简洁的IRNode定义、Prog类的基本块管理、IRVisitor的访问方法签名。

#### 步骤2：运行IR生成测试

```bash
# 运行IR生成相关的测试
mvn test -Dtest=ThreeAddressCodeTest

# 预期输出：
# [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

测试验证内容：
- 二元表达式生成正确的IR序列
- 赋值语句生成Assign节点
- 控制流语句生成JMP/CJMP节点
- 函数调用生成CallFunc节点

#### 步骤3：手动测试IR生成

创建测试文件 `test_ir.cymbol`：
```c
int test(int a, int b) {
    int c = a + b * 2;
    return c;
}
```

```bash
# 编译并查看IR输出（需要添加IR打印逻辑）
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.driver.CompilerDriver" \
  -Dexec.args="src/test/resources/test_ir.cymbol --print-ir"

# 预期输出IR序列：
# func test(a, b):
#   t1 = b * 2
#   c = a + t1
#   return c
```

### 故障排查

**问题1：IR生成时报NullPointerException**
- 原因：AST节点可能缺少类型信息或符号引用
- 解决：确保在IR生成前运行LocalDefine和TypeChecker

**问题2：生成的IR指令顺序错误**
- 原因：表达式求值顺序不正确，或临时变量未正确压栈
- 解决：检查visit方法中子表达式的访问顺序和栈操作

**问题3：IRVisitor实现找不到visit方法**
- 原因：新增了IR节点类型但未在IRVisitor中添加对应方法
- 解决：在IRVisitor接口和所有实现类中添加新的visit方法

---

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI帮助完成IR相关任务，我们需要精心设计上下文。

**源码文件**（按阅读顺序）：
1. `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRNode.java`
   - 作用：IR节点基类定义
   - 关键内容：IR节点继承体系

2. `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/Prog.java`
   - 作用：程序表示，管理基本块
   - 关键方法：`addBlock()`, `optimizeEmptyBlock()`

3. `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java`
   - 作用：IR访问者接口
   - 关键内容：所有visit方法签名

4. `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/stmt/Stmt.java`
   - 作用：语句基类及所有stmt子类
   - 关键内容：StmtType枚举，Assign/JMP/CJMP等实现

5. `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/Expr.java`
   - 作用：表达式基类及所有expr子类
   - 关键内容：BinExpr/UnaryExpr/ConstVal/VarSlot等

6. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java`
   - 作用：AST到IR的转换器
   - 关键方法：所有visit()方法实现

**文档文件**：
1. `ep20/README.md`
   - 作用：EP20项目概述
   - 相关章节：IR设计、编译器流水线

2. `ep20/docs/01_core_design/EP20_技术设计规范.md`
   - 作用：IR设计详细规范
   - 关键章节：中间表示设计、IR节点层次

**测试文件**：
1. `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/ir/ThreeAddressCodeTest.java`
   - 作用：IR生成测试
   - 关键测试：二元表达式、赋值、函数调用

2. `ep20/src/test/resources/`
   - 作用：测试用例源代码
   - 示例：`simple_expression.cymbol`, `control_flow.cymbol`

**上下文组织说明**：
这些文件按照从抽象到具体、从接口到实现的顺序组织：
1. **核心接口在前**：先理解IRNode基类和IRVisitor接口
2. **具体实现在后**：再看Stmt和Expr的具体子类实现
3. **转换逻辑最后**：理解AST到IR的转换过程
4. **测试用例验证**：通过测试理解预期行为
5. **文档作为参考**：设计规范说明架构决策

### Prompt 模板

#### 类型A：实现新的IR指令类型

```markdown
请为EP20编译器添加新的IR指令类型：[指令名称]。

任务目标：
- 扩展IR节点系统，支持新的语言特性或优化需求
- 遵循现有IR节点设计模式
- 确保与IRVisitor接口兼容

具体要求：

1. 创建新的IR节点类
   - 位置：`ep20/src/main/java/org/teachfx/antlr4/ep20/ir/[stmt|expr]/`
   - 类名：[YourIRNodeName]
   - 继承：Stmt 或 Expr（根据语义选择）

2. 实现关键方法
   - 构造函数：接受必要的操作数参数
   - accept()：实现Visitor模式
   - toString()：便于调试输出
   - 如需要：get/set方法

3. 更新IRVisitor接口
   - 在`IRVisitor.java`中添加visit([YourIRNodeName])方法
   - 返回类型：根据语义决定（S for Stmt, E for Expr）

4. 更新CymbolIRBuilder
   - 在ASTVisitor中添加visit()方法，将AST节点转换为新的IR节点
   - 位置：`ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java`

5. 添加单元测试
   - 在`ep20/src/test/java/org/teachfx/antlr4/ep20/pass/ir/`下创建测试
   - 测试类名：[YourIRNodeName]Test
   - 至少3个测试用例：正常情况、边界情况、错误情况

参考上下文文件：
- 源码：
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRNode.java`（基类）
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/stmt/Assign.java`（示例）
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/BinExpr.java`（示例）
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java`（访问者接口）
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java`（IR生成器）
- 文档：
  - `ep20/README.md`（IR设计章节）
- 测试：
  - `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/ir/ThreeAddressCodeTest.java`（测试示例）

约束条件：
- 保持与现有IR节点API一致性
- 遵循AGENTS.md中的代码风格（包名、类名、方法名）
- 所有指令必须通过IRVisitor访问
- 新增IR节点后需更新所有IRVisitor实现类
- 必须添加完整的JavaDoc注释

期望输出：
1. 新IR节点类的完整实现代码
2. 更新后的IRVisitor接口（标注修改部分）
3. CymbolIRBuilder中的转换逻辑（如有需要）
4. 新增IR节点后可能需要更新的其他IRVisitor实现类
5. 完整的单元测试类（包含所有测试用例）
6. 运行测试的命令：`mvn test -Dtest=[YourIRNodeName]Test`
```

#### 类型B：优化IR指令序列

```markdown
请优化以下IR指令序列，应用[优化名称]规则：

输入IR代码：
```java
[t1 = x * 2]
[t2 = y + t1]
[t3 = x * 2]  // 重复计算
[t4 = z + t3]
```

优化目标：
1. 公共子表达式消除：识别重复计算的表达式(x * 2)
2. 重用已有结果：t3与t1相同，应直接使用t1
3. 删除冗余指令：删除重复的x * 2计算

具体要求：
1. 分析IR序列，识别优化机会
2. 应用优化规则，生成优化后的IR
3. 保持语义等价性（优化前后程序行为不变）
4. 维护SSA形式（如果已经转换）

参考上下文文件：
- 源码：
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/`（IR节点定义）
  - `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/`（IR处理Pass）
- 文档：
  - `ep20/docs/01_core_design/EP20_技术设计规范.md`（优化规则）

约束条件：
- 不能改变程序语义
- 保持IR结构完整性
- 优化后的IR必须通过所有现有测试

期望输出：
1. 优化后的IR代码序列
2. 优化过程的详细解释（识别了哪些冗余，如何消除）
3. 变更摘要（删除了几条指令，减少了几次计算）
```

### AI 应该做 / 不该做

**✅ AI 允许做的事情**：

1. **实现新的IR节点类型**
   - 可以：添加新的Stmt或Expr子类（如SwitchStmt、BitwiseAndExpr）
   - 可以：在IRVisitor中添加对应的visit方法
   - 不能：修改IRNode基类（保持简单）

2. **优化现有IR生成逻辑**
   - 可以：改进CymbolIRBuilder中的visit方法实现
   - 可以：减少临时变量使用量
   - 可以：合并冗余的IR指令
   - 不能：改变AST到IR的语义映射

3. **生成IR相关的工具方法**
   - 可以：添加IR打印、IR验证等辅助方法
   - 可以：创建IR转换工具类
   - 不能：在IR节点类中添加业务逻辑（应使用Visitor）

4. **编写IR相关的测试用例**
   - 可以：为新的IR节点编写单元测试
   - 可以：为CymbolIRBuilder添加集成测试
   - 不能：删除或跳过现有的IR测试

5. **生成IR文档和注释**
   - 可以：为IR节点类添加JavaDoc
   - 可以：编写IR设计文档
   - 不能：改变现有IR节点类的public API

**❌ AI 禁止做的事情**：

1. **改变IR节点的继承体系**
   - 不允许：修改Stmt或Expr的继承关系
   - 不允许：让Expr继承Stmt或反之
   - 原因：继承层次是经过验证的设计，改变会导致连锁反应

2. **修改IRVisitor的核心接口**
   - 不允许：改变IRVisitor<S,E>的泛型签名
   - 不允许：删除现有的visit方法
   - 除非：明确要求添加新的visit方法
   - 原因：影响所有Visitor实现类

3. **删除IR节点类**
   - 不允许：删除任何现有的Stmt或Expr子类
   - 原因：可能破坏现有代码生成逻辑

4. **破坏SSA形式的约束**
   - 如果IR已经转换为SSA形式，AI不允许：
     - 直接修改变量赋值（破坏单个赋值规则）
     - 删除Phi函数
     - 合并本应分离的变量版本
   - 原因：SSA形式是高级优化的基础，破坏后优化会失效

5. **在IR节点中添加复杂的构造函数**
   - 不允许：在IR节点构造函数中添加验证、计算等复杂逻辑
   - 原因：IR节点应只是数据容器，复杂逻辑应在Visitor中
   - 例外：简单的null检查或参数验证可以接受

### 验证与回滚策略

#### 自动化验证

**步骤1：编译验证**
```bash
# 进入EP20目录
cd ep20

# 清理并重新编译
mvn clean compile

# 预期输出
# [INFO] BUILD SUCCESS
# [INFO] 编译时间: X秒
```

如果编译失败：
1. 查看错误信息，定位问题文件
2. 检查是否缺少import或类名错误
3. 确认泛型类型正确
4. 如果是IRVisitor问题，检查所有实现类是否添加了新方法

**步骤2：运行IR相关测试**
```bash
# 运行所有IR测试
mvn test -Dtest=*IR*Test

# 运行特定测试
mvn test -Dtest=ThreeAddressCodeTest

# 预期输出
# [INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

如果测试失败：
1. 查看 `target/surefire-reports/` 中的测试报告
2. 运行单个测试：`mvn test -Dtest=具体测试类#具体测试方法`
3. 检查IR生成结果是否符合预期
4. 使用调试器在CymbolIRBuilder.visit()方法中设置断点

**步骤3：示例程序验证**
```bash
# 编写一个简单的Cymbol程序 test_ir.cymbol
# 运行编译并打印IR（需要添加--print-ir选项）
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.driver.CompilerDriver" \
  -Dexec.args="src/test/resources/test_ir.cymbol --print-ir"

# 手动检查输出的IR序列
# 1. 检查是否所有表达式都被正确转换
# 2. 检查临时变量命名是否连续
# 3. 检查控制流语句是否生成正确的跳转
```

#### 手工检查点

**检查1：IR节点类正确性**
- [ ] 所有IR节点类继承自IRNode或正确的子类(Stmt/Expr)
- [ ] 所有IR节点实现accept方法，调用visitor.visit(this)
- [ ] 所有IR节点有合理的toString()方法，便于调试
- [ ] 所有IR节点类有必要的getter方法(或有文档说明为什么不需要)

**检查2：IRVisitor接口完整性**
- [ ] 所有具体IR节点在IRVisitor中都有对应的visit方法
- [ ] 所有IRVisitor实现类都实现了新的visit方法
- [ ] 返回类型正确(S for Stmt, E for Expr)
- [ ] 默认方法提供合理的默认行为

**检查3：IR生成逻辑正确性**
- [ ] CymbolIRBuilder正确实现所有visit方法
- [ ] 表达式求值顺序正确（从左到右，先子表达式）
- [ ] 临时变量管理正确（push/pop/peek操作配对）
- [ ] 控制流语句生成正确的跳转目标

**检查4：代码风格合规**
- [ ] 包名：`org.teachfx.antlr4.ep20.ir`或`stmt`或`expr`
- [ ] 类名：PascalCase（如BinExpr, Assign）
- [ ] 方法名：camelCase（如getLhs(), accept()）
- [ ] 导入顺序：ANTLR4 → External → Internal → Java stdlib

#### 回滚方案

如果AI修改后出现问题：

**方案1：Git Stash（推荐）**
```bash
# 查看修改的文件
git status

# 保存修改到stash（带详细说明）
git stash push -m "AI IR modifications: [brief description]"

# 如果出现问题，恢复干净状态
git stash pop

# 如果stash已不需要
git stash drop
```

**方案2：Git Checkout（恢复特定文件）**
```bash
# 查看修改历史
git log --oneline -5

# 恢复单个文件到最后提交状态
git checkout HEAD -- ep20/src/main/java/org/teachfx/antlr4/ep20/ir/[修改的文件].java

# 恢复所有ir包文件
git checkout HEAD -- ep20/src/main/java/org/teachfx/antlr4/ep20/ir/
```

**方案3：Git Reset（谨慎使用）**
```bash
# 软重置（保留修改在暂存区）
git reset --soft HEAD~1

# 硬重置（完全丢弃提交和修改）- **危险操作**
git reset --hard HEAD~1  # 只在确定要丢弃时使用

# 混合重置（保留修改在工作区，取消暂存）- 推荐
git reset --mixed HEAD~1
```

**保存AI工作以便后续学习**：
```bash
# 创建分支保存AI尝试
git checkout -b ai-ir-experiment

# 提交AI修改
git add -A
git commit -m "AI experiment: [description]"

# 切回主分支继续工作
git checkout main
git merge ai-ir-experiment  # 如果验证通过
# 或
git branch -D ai-ir-experiment  # 如果放弃
```

#### 验证流程总结

```
AI生成IR代码
    ↓
编译验证（mvn clean compile）
    ↓
IR生成测试（mvn test -Dtest=*IR*Test）
    ↓
手工检查（IR节点结构、Visitor完整性、代码风格）
    ↓
示例程序验证（手动检查IR输出）
    ↓
✅ 验证通过 → 集成到主分支
    ❌ 验证失败 → 回滚（stash/checkout/reset）
```

---

## 练习题

### 练习1：实现取模运算IR节点（手工实现版）

**难度**：⭐⭐☆☆☆  
**预计时间**：30-45分钟

**题目描述**：
EP20的IR目前支持加减乘除运算，但不支持取模运算(%)。请实现ModExpr IR节点，支持整数取模操作。

**要求**：
- 完全手工实现，不依赖AI
- 理解IR节点设计模式后独立完成
- 可参考Assign或BinExpr的实现，但不能直接复制

**验收标准**：
- [ ] ModExpr类正确继承自Expr
- [ ] 包含left和right两个Operand字段
- [ ] 实现accept方法，在IRVisitor中有对应visit方法
- [ ] CymbolIRBuilder中visit(BinaryExprNode)能处理%运算符
- [ ] 添加至少3个单元测试，覆盖正数、负数、边界情况
- [ ] 代码能通过mvn test

**💡 解题思路提示**：
1. 参考`ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/`目录下现有的BinExpr.java
2. 在IRVisitor.java中添加`visit(ModExpr node)`方法
3. 在CymbolIRBuilder的visit(BinaryExprNode)中添加MOD运算符映射
4. 测试用例：5 % 3 = 2，-5 % 3 = -2，5 % 0（应触发错误或异常）

---

### 练习2：使用AI实现位运算IR节点（AI协作版）

**难度**：⭐⭐⭐☆☆  
**预计时间**：45-60分钟

**题目描述**：
为EP20编译器添加完整的位运算支持：按位与(&)、按位或(|)、按位异或(^)、左移(<<)、右移(>>)。

**AI协作要求**：
1. **设计上下文**：准备所有需要提供给AI的文件（参考本章"上下文设计"部分）
2. **设计Prompt**：使用Prompt模板，设计适合此任务的详细Prompt
3. **验证AI输出**：使用本章的"验证与回滚策略"
4. **理解AI代码**：确保你能解释AI生成的每一部分

**验收标准**：
- [ ] AI生成的代码能编译通过（mvn clean compile）
- [ ] 所有5个位运算符都有对应的IR节点（BitAndExpr, BitOrExpr, BitXorExpr, LeftShiftExpr, RightShiftExpr）
- [ ] 所有IR节点都有完整的单元测试
- [ ] 所有测试通过（mvn test）

---

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

**1. 核心概念**
- 理解了中间表示(IR)的定义和目的
- 掌握了三地址码的设计原理
- 学会了IR节点层次架构的设计方法

**2. 关键技术**
- 实现了IR节点基类和具体IR节点（Stmt, Expr）
- 掌握了IRVisitor访问者模式的应用
- 理解了AST到IR的转换流程

**3. 实战技能**
- 能够设计和实现编译器的IR系统
- 学会了IR生成的基本方法
- 掌握了IR验证和测试技巧

### 【你现在站在】:
```
... → [类型检查] → ✅ [IR设计] → [CFG构建] → [代码生成] → ...
```

**当前在编译器流水线的位置**：
- 本章实现了编译器中端的核心IR基础设施
- IR是连接前端和后端的关键桥梁
- 为第14章CFG构建和后续优化奠定了基础

### 下一章预告

第14章将聚焦于**控制流图与基础块**，你将学习：
- 如何将IR指令组织成基本块(Basic Block)
- 如何构建控制流图(CFG)表示程序执行路径
- 理解支配关系(Dominance)和支配边界
- 掌握CFG在优化中的应用

**准备**：为了学习下一章，建议：
- [ ] 复习本章的IR节点设计
- [ ] 运行IR生成相关测试
- [ ] 阅读EP20的CFG相关代码
- [ ] 思考如何从线性IR序列构建基本块

继续加油！IR设计是编译器架构的核心，掌握了它，你的编译器就能支持复杂的优化和代码生成功能！