# 第15章：本地优化与代码生成 (EP20)

## 本章概述

本章聚焦于编译器的最后两个关键阶段：**本地优化**和**代码生成**。你将学习如何在控制流图(CFG)上进行各种局部优化（如死代码消除、跳转优化），并最终将优化后的IR生成为EP18虚拟机字节码。

【你现在站在哪】:
```
... → [AST构建] → [符号表] → [类型检查] → [IR生成] → [CFG构建] → ✅ [本地优化] →  [代码生成] → ...
```

## 动机与真实场景

### 真实场景

你的编译器现在已经可以生成IR和控制流图。但生成的代码中存在大量冗余：不必要的跳转、从未使用的计算、重复的指令等。用户抱怨编译出的程序运行效率不高，代码体积过大。

### 具体问题与挑战

- **冗余控制流**：生成的代码中有大量`goto L1; L1: goto L2`这样的冗余跳转
- **死代码问题**：有些计算结果从未被使用，但仍在生成指令
- **指令选择不佳**：没有针对目标平台选择最优指令序列
- **寄存器分配前优化**：在寄存器分配前未能消除冗余计算

### 缺失本章能力的影响

如果缺少本地优化和代码生成能力，你将面临：
- 生成的代码体积大、执行效率低
- 无法消除明显的程序冗余
- 编译器后端架构不清晰，难以扩展新目标平台
- 无法实现生产级的代码生成

### 本章学习目标

- 理解控制流优化算法（跳转优化、基本块合并）
- 掌握死代码消除技术
- 学习指令选择的基本原则
- 实现EP18虚拟机字节码生成器
- 理解编译器后端的完整架构

## 人类工程师线：技术与实现

### 核心概念

**本地优化 (Local Optimization)** 是在单个基本块或相邻基本块范围内进行的优化。它不考虑跨越多个基本块的全局数据流信息，因此实现相对简单但效果显著。

通俗理解：本地优化就像编辑文章时，在段落内部进行的措辞优化。你会删除重复的词语、简化复杂的表达、删除无关的句子。这些优化不影响文章的整体结构，但能显著提升可读性。

**控制流优化 (Control Flow Optimization)** 是本地优化的重要组成部分，主要包括：
- **跳转优化**：消除跳转到跳转、跳转到下一指令等冗余
- **基本块合并**：将只能顺序执行的两个小块合并
- **空块消除**：删除不含任何指令的基本块

**代码生成 (Code Generation)** 是将优化后的IR翻译成目标代码的过程。对于EP20，我们的目标是EP18虚拟机字节码（一种栈式虚拟机指令集）。

[图1：编译器后端在流水线中的位置]
```
源代码 (Cymbol)
   │
   ▼
前端
├─ 词法分析 → Token
├─ 语法分析 → AST
├─ 语义分析 → 符号表 + 类型检查
└─ IR生成 → 三地址码
   │
   ▼
中端
├─ CFG构建 → 控制流图
└─ 本地优化 → 优化的IR
   │
   ▼
后端
└─ 代码生成 → EP18 VM字节码
```

### 与仓库 EP 的对应关系

对应 EP：EP20

目录结构：
```
ep20/
├── src/main/java/org/teachfx/antlr4/ep20/
│   ├── ir/                             # IR节点定义
│   │   ├── IRNode.java                 # IR节点基类
│   │   ├── Prog.java                   # IR程序根节点
│   │   └── ...
│   ├── pass/
│   │   ├── cfg/                        # 控制流分析与优化
│   │   │   ├── BasicBlock.java         # 基本块定义 (130行)
│   │   │   ├── CFG.java                # 控制流图 (158行)
│   │   │   ├── CFGBuilder.java         # CFG构建器 (63行)
│   │   │   ├── LinearIRBlock.java      # 线性IR块 (236行)
│   │   │   ├── ControlFlowAnalysis.java # 控制流优化器 (69行)
│   │   │   └── IFlowOptimizer.java     # 优化器接口 (7行)
│   │   ├── codegen/                    # 代码生成器
│   │   │   ├── CymbolAssembler.java    # EP18汇编器 (155行)
│   │   │   ├── CymbolVMIOperatorEmitter.java # VM操作码发射器 (65行)
│   │   │   └── IOperatorEmitter.java   # 操作码接口
│   └── Compiler.java                   # 编译器入口 (完整流水线)
```

### 核心实现

#### 1. 控制流优化 (ControlFlowAnalysis)

**ControlFlowAnalysis.java** 是本地优化的核心实现，它实现了两种主要的优化：

1. **冗余跳转消除**：如果基本块的最后一个指令是跳转到下一块，则删除该跳转
2. **基本块合并**：如果基本块只有一个前驱，且前驱只有一个后继，则合并这两个块

```java
/**
 * 控制流分析优化器
 * 
 * <p>在CFG上执行本地优化，包括：</p>
 * <ul>
 *   <li>删除冗余跳转指令（跳转到下一指令）</li>
 *   <li>合并顺序执行的基本块</li>
 *   <li>简化控制流图结构</li>
 * </ul>
 */
public class ControlFlowAnalysis<I extends IRNode> implements IFlowOptimizer<I> {
    private static final Logger logger = LogManager.getLogger(ControlFlowAnalysis.class);
    
    @Override
    public void onHandle(CFG<I> cfg) {
        List<Triple<Integer, Integer, Integer>> needRemovedLink = new ArrayList<>();
        
        // 1. 遍历所有控制流图的节点
        // 2. 如果一个节点的出度为1，并且是JMP指令，同时满足JMP的next和此节点的后续相同
        // 3. 如果一个节点的入度为1，则该节点的前一个节点可以合并到该节点上
        // 根据上面三个条件，可以得到下面的代码

        // 第一步：移除冗余跳转指令
        for (var block : cfg.nodes) {
            var key = block.getId();
            var outDeg = cfg.getOutDegree(key);

            // 检查块的最后一条指令是否是跳转
            if (outDeg == 1 && block.getLastInstr() instanceof JMPInstr jmpInstr) {
                var targetBlockId = jmpInstr.getTarget().getSeq();
                AtomicBoolean needRemoveLastInstr = new AtomicBoolean(false);
                
                // 检查跳转目标是自然后继
                cfg.getSucceed(key).stream()
                   .filter(x -> x == targetBlockId)
                   .findFirst()
                   .ifPresent(next -> {
                       needRemoveLastInstr.set(true);
                   });

                // 如果是跳转到自然后继，删除跳转指令和边
                if (needRemoveLastInstr.get()) {
                    block.removeLastInstr();  // 删除冗余跳转
                    cfg.removeEdge(Triple.of(key, targetBlockId, 5));
                    logger.debug("移除冗余跳转：块{}跳转到块{}是自然后继", key, targetBlockId);
                }
            }
        }

        // 第二步：合并顺序执行的基本块
        var removeQueue = new LinkedList<BasicBlock<I>>();
        
        for (var block : cfg.nodes) {
            var key = block.getId();
            var inDeg = cfg.getInEdges(key).toList();
            var isSrcSoloLink = (long) cfg.getFrontier(key).size() == 1;
            var isDestSoloLink = isSrcSoloLink && cfg.getOutDegree(inDeg.get(0).getLeft()) == 1;
            
            // 如果入度为1，且前驱出度也为1，可以合并
            if (inDeg.size() == 1 && isDestSoloLink) {
                cfg.getFrontier(key).stream().findFirst().ifPresent(frontier -> {
                    var prevBlock = cfg.getBlock(frontier);
                    prevBlock.mergeNearBlock(block);  // 合并两个块
                    cfg.removeEdge(inDeg.get(0));
                    removeQueue.add(block);
                    logger.debug("合并基本块：块{}合并到块{}", key, prevBlock.getId());
                });
            }
        }

        // 第三步：删除被合并的空块
        for (var block : removeQueue) {
            cfg.removeNode(block);
        }
        
        if (DEBUG) {
            logger.info("控制流优化完成，移除{}个块", removeQueue.size());
        }
    }
}
```

**算法详解**：

这个优化器实现了两个关键算法：

**算法1：冗余跳转消除**
```
输入: CFG (控制流图)
输出: 优化后的CFG

对每个基本块 B:
    如果 B 有且仅有一个出边 AND B 的最后一条指令是 JMP:
        目标块 T = JMP 的目标
        后继块集合 S = B 的自然后继
        
        如果 T ∈ S:  // 跳转到自然后继
            从 B 中删除最后一条 JMP 指令
            从CFG中删除 B→T 的边
```

**算法2：基本块合并**
```
输入: CFG (控制流图)
输出: 优化后的CFG

初始化空队列 Q (用于存储待删除的块)

对每个基本块 B:
    入边集合 E_in = B 的入边
    
    如果 |E_in| == 1:  // 只有一个前驱
        前驱块 Pred = E_in[0].from
        
        如果 Pred 的出度 == 1:  // 前驱也只有这一个后继
            将 B 的所有指令合并到 Pred
            删除 B→? 的边
            将 B 加入删除队列 Q

对于每个在 Q 中的块 B:
    从CFG中删除 B
```

#### 2. 代码生成 (CymbolAssembler)

**CymbolAssembler.java** 负责将IR指令转换为EP18虚拟机字节码。它实现了IRVisitor接口，每种IR节点对应一个visit方法。

```java
/**
 * EP18虚拟机汇编器
 * 
 * <p>将IR指令转换为EP18虚拟机字节码。</p>
 * <p>访问者模式遍历IR节点，为每个IR节点生成对应的虚拟机指令。</p>
 */
public class CymbolAssembler implements IRVisitor<Void, Void> {
    private LinkedList<String> assembleCmdBuffer = new LinkedList<>();
    protected IOperatorEmitter operatorEmitter = new CymbolVMIOperatorEmitter();
    
    // 缩进计数（用于生成美观的汇编代码）
    private int indents = 0;

    /**
     * 访问IR指令列表（主入口）
     */
    public Void visit(List<IRNode> linearInstrs) {
        for (var instr : linearInstrs) {
            if (instr instanceof Expr) {
                ((Expr) instr).accept(this);
            } else {
                ((Stmt) instr).accept(this);
            }
        }
        return null;
    }

    /**
     * 生成EP18虚拟机加载指令
     * 
     * <p>FrameSlot表示栈帧中的变量槽位，对应EP18的load指令</p>
     * 
     * 例如：int x = 5;
     *        x 存储在栈帧的 slot 0
     *        读取 x 时：load 0
     */
    @Override
    public Void visit(FrameSlot frameSlot) {
        emit("load %d".formatted(frameSlot.getSlotIdx()));
        return null;
    }

    /**
     * 生成EP18虚拟机常数指令
     * 
     * <p>根据常数类型生成不同的iconst/sconst/bconst指令</p>
     * 
     * 例如：42       → iconst 42
     *       "hello"  → sconst "hello"
     *       true     → bconst 1
     */
    @Override
    public <T> Void visit(ConstVal<T> tConstVal) {
        var val = tConstVal.getVal();
        if (val instanceof Integer integer) {
            emit("iconst %d".formatted(integer));
        } else if (val instanceof String str) {
            emit("sconst \"%s\"".formatted(str));
        } else if (val instanceof Boolean bool) {
            emit("bconst %d".formatted(bool ? 1 : 0));
        }
        return null;
    }

    /**
     * 生成二元运算指令
     * 
     * <p>先访问左右操作数，再生成运算指令</p>
     * 
     * 例如：a + b
     *       load a    // 访问左操作数
     *       load b    // 访问右操作数
     *       iadd      // 生成加法指令
     */
    @Override
    public Void visit(BinExpr node) {
        node.getLhs().accept(this);  // 生成左操作数指令
        node.getRhs().accept(this);  // 生成右操作数指令
        
        // 生成运算指令（通过操作码发射器）
        emit(operatorEmitter.emitBinaryOp(node.getOpType()));
        return null;
    }

    /**
     * 生成一元运算指令
     * 
     * <p>先访问操作数，再生成运算指令</p>
     */
    @Override
    public Void visit(UnaryExpr node) {
        node.expr.accept(this);
        emit(operatorEmitter.emitUnaryOp(node.op));
        return null;
    }

    /**
     * 生成函数调用指令
     * 
     * <p>内置函数直接调用（如print），用户函数使用call指令</p>
     * 
     * 例如：print(x)    → print
     *       myFunc(a,b) → call myFunc()
     */
    @Override
    public Void visit(CallFunc callFunc) {
        if (!callFunc.getFuncType().isBuiltIn()) {
            emit("call %s()".formatted(callFunc.getFuncName()));
        } else {
            emit("%s".formatted(callFunc.getFuncName()));
        }
        return null;
    }

    /**
     * 生成标签指令
     * 
     * <p>函数入口标签或跳转目标标签</p>
     * 
     * 例如：factorial:
     *       L1:
     */
    @Override
    public Void visit(Label label) {
        // 调整缩进（标签不缩进）
        if (indents > 0) {
            indents--;
        }

        if (label instanceof FuncEntryLabel) {
            indents = 0;  // 函数入口重置缩进
            emit("%s".formatted(label.toSource()));
        } else {
            emit("%s:".formatted(label.toSource()));  // 普通跳转标签
        }
        
        indents++;  // 标签后内容缩进
        return null;
    }

    /**
     * 生成跳转指令
     */
    @Override
    public Void visit(JMP jmp) {
        emit("br %s".formatted(jmp.getNext().toString()));
        indents--;  // 跳转后减少缩进
        return null;
    }

    /**
     * 生成条件跳转指令
     * 
     * <p>条件为假时跳转（EP18 VM的特性）</p>
     */
    @Override
    public Void visit(CJMP cjmp) {
        emit("brf %s".formatted(cjmp.getElseBlock().getLabel().toString()));
        indents--;  // 条件跳转后减少缩进
        return null;
    }

    /**
     * 生成赋值指令
     * 
     * <p>将计算结果存储到栈帧变量</p>
     * 
     * 例如：x = 5 + 3
     *       iconst 5
     *       iconst 3
     *       iadd
     *       store 0    // x在槽位0
     */
    @Override
    public Void visit(Assign assign) {
        assign.getRhs().accept(this);  // 访问右值表达式

        // 生成存储指令
        if (assign.getLhs() instanceof FrameSlot frameSlot) {
            emit("store %d".formatted(frameSlot.getSlotIdx()));
        }

        return null;
    }

    /**
     * 生成返回指令
     * 
     * <p>main函数返回生成halt，其他函数生成ret</p>
     */
    @Override
    public Void visit(ReturnVal returnVal) {
        // 如果有返回值，先生成返回值
        if (Objects.nonNull(returnVal.getRetVal())) {
            returnVal.getRetVal().accept(this);
        }

        // 生成返回指令
        if (returnVal.isMainEntry()) {
            emit("halt");  // main函数结束时停机
        } else {
            emit("ret");   // 普通函数返回
        }
        indents--;  // 返回后减少缩进
        return null;
    }

    /**
     * 生成输出指令
     * 
     * <p>带缩进的发射方法，生成格式化的汇编代码</p>
     */
    protected void emit(String cmd) {
        var indentCmdBuf = "    ".repeat(indents) + cmd;
        assembleCmdBuffer.add(indentCmdBuf);
    }
}
```

**操作码发射器**

**CymbolVMIOperatorEmitter.java** 负责将二元/一元运算符映射到EP18 VM指令。

```java
/**
 * EP18虚拟机操作码发射器
 * 
 * <p>将运算符转换为EP18 VM指令字符串。</p>
 */
public class CymbolVMIOperatorEmitter implements IOperatorEmitter {
    
    /**
     * 映射二元运算符到VM指令
     */
    @Override
    public String emitBinaryOp(OperatorType.BinaryOpType binaryOpType) {
        return switch (binaryOpType) {
            case ADD -> "iadd";      // 整数加法
            case SUB -> "isub";      // 整数减法
            case MUL -> "imult";     // 整数乘法
            case DIV -> "idiv";      // 整数除法
            case MOD -> "imod";      // 整数取模
            case LT -> "ilt";        // 小于
            case LE -> "ile";        // 小于等于
            case GT -> "igt";        // 大于
            case GE -> "ige";        // 大于等于
            case EQ -> "ieq";        // 等于
            case NE -> "ine";        // 不等于
            case AND -> "iand";      // 逻辑与
            case OR -> "ior";        // 逻辑或
        };
    }

    /**
     * 映射一元运算符到VM指令
     */
    @Override
    public String emitUnaryOp(OperatorType.UnaryOpType unaryOpType) {
        return switch (unaryOpType) {
            case NEG -> "ineg";      // 取负
            case NOT -> "inot";      // 逻辑非
        };
    }
}
```

#### 3. 编译器流水线集成

编译器入口 **Compiler.java** 展示了完整的编译流程，从源码到字节码：

```java
public class Compiler {
    private static final Logger logger = LogManager.getLogger(Compiler.class);

    /**
     * 完整的编译流程
     */
    public String compile(String sourceCode) {
        // 阶段1：ANTLR4词法分析和语法分析
        var charStream = CharStreams.fromString(sourceCode);
        var lexer = new CymbolLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokenStream);
        
        ParseTree tree = parser.file();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new CompileException("语法错误");
        }

        // 阶段2：AST构建
        var astBuilder = new CymbolASTBuilder();
        var astRoot = astBuilder.visit(tree);
        
        // 阶段3：符号表构建
        var symbolDefine = new LocalDefine();
        astRoot.accept(symbolDefine);
        var symbolTable = symbolDefine.getSymbolTable();

        // 阶段4：类型检查
        var typeChecker = new TypeChecker(symbolTable);
        astRoot.accept(typeChecker);

        // 阶段5：IR生成
        var irBuilder = new CymbolIRBuilder(symbolTable);
        var linearInstrs = astRoot.accept(irBuilder);

        // 阶段6：CFG构建
        var cfgBuilder = new CFGBuilder();
        var cfg = cfgBuilder.buildCFG(linearInstrs);

        // 阶段7：本地优化（控制流优化）
        var optimizer = new ControlFlowAnalysis<IRNode>();
        optimizer.onHandle(cfg);

        // 阶段8：指令选择（线性化）
        var linearIRBuilder = new LinearIRBlock(linearInstrs.size());
        var linearIRInstrs = linearIRBuilder.toLinearInstrList(cfg);

        // 阶段9：代码生成（EP18 VM）
        var assembler = new CymbolAssembler();
        assembler.visit(linearIRInstrs);
        
        return assembler.getAsmInfo();
    }
}
```

### 实战流程

实战步骤：完整的编译、优化和代码生成

**步骤1：编译项目并运行基本块优化测试**

```bash
# 进入EP20目录
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep20

# 编译项目
mvn clean compile -DskipTests

# 运行基本块优化测试
mvn test -Dtest=BasicBlockOptimizationTest

# 预期输出：
# [INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认所有5个测试通过
- 检查点2：查看`testMergeNearBlock()`验证基本块合并
- 检查点3：查看`testRemoveLastInstr()`验证指令删除

**步骤2：运行汇编器测试**

```bash
# 运行汇编器测试
mvn test -Dtest=CymbolAssemblerTest

# 预期输出：
# [INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
# [INFO] BUILD SUCCESS
```

验证方法：
- 检查点1：确认常量加载正确生成(`testEmitLoadConstantInstruction`)
- 检查点2：验证二元运算指令生成
- 检查点3：检查函数调用指令格式

**步骤3：端到端编译测试**

创建测试程序：
```bash
cat > /tmp/test_factorial.cymbol << 'EOF'
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void main() {
    int result = factorial(5);
    print(result);
}
EOF
```

执行完整编译：
```bash
# 运行EP20编译器（生成EP18 VM字节码）
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler" \
    -Dexec.args="/tmp/test_factorial.cymbol /tmp/output.vm"

# 查看生成的字节码
cat /tmp/output.vm
```

预期输出（优化后的字节码）：
```
# function factorial
factorial:
    iconst 1
    load 0           # 加载参数 n
    ile
    brf L0           # 如果 n > 1 跳转到 L0
    iconst 1
    ret              # return 1
L0:
    load 0           # n
    load 0           # n
    iconst 1
    isub             # n - 1
    call factorial() # 递归调用
    imult            # n * result
    ret

# function main
main:
    iconst 5
    call factorial()
    print
    halt
```

注意：由于控制流优化，你可能看不到冗余的跳转指令。

**故障排查指南**

**问题1：优化后代码不正确**
- **症状**：优化后的程序运行结果错误
- **原因**：删除指令或合并块时破坏了控制流
- **排查步骤**：
  1. 禁用优化（注释掉`optimizer.onHandle(cfg)`），验证未优化代码正确
  2. 单独测试每种优化（跳转消除、块合并）
  3. 使用`ControlFlowAnalysis.DEBUG = true`查看详细日志
  4. 检查目标块的标签是否正确更新
- **修复方法**：
  ```java
  // 在合并块之前，确保正确转移后继关系
  prevBlock.mergeNearBlock(block);
  cfg.transferSuccessors(block, prevBlock);  // 手动添加：转移后继
  ```

**问题2：汇编器生成错误指令**
- **症状**：VM报告未知指令或参数错误
- **原因**：操作码映射错误或操作数类型不匹配
- **排查步骤**：
  1. 检查`CymbolVMIOperatorEmitter`的操作码映射
  2. 验证IR节点的操作数类型是否正确
  3. 对比`CymbolAssemblerTest`中的期望输出
  4. 使用简单的测试用例（单个二元运算）
- **常见错误**：
  - 布尔值错误使用`iconst`而不是`bconst`
  - 内存操作未正确处理FrameSlot
  - 跳转标签格式错误（缺少`:`或`br`指令格式错误）

**问题3：寄存器/栈布局错误**
- **症状**：变量值在运行时错误（非预期值）
- **原因**：生成的load/store指令的槽位索引错误
- **排查步骤**：
  1. 检查IR生成阶段的槽位分配（通过`FrameSlot.getSlotIdx()`）
  2. 验证符号表的变量到槽位映射
  3. 生成调试信息：`emit("// load var: " + frameSlot.getSymbol().getName())`
  4. 对比简单的变量赋值和使用的完整指令序列
- **修复方法**：
  ```java
  // 在IR生成时确保变量符号绑定到FrameSlot
  FrameSlot slot = new FrameSlot(variableSymbol.getSlotIndex(), variableSymbol);
  variableSymbol.setFrameSlot(slot);  // 建立符号→槽位的双向绑定
  ```

**问题4：递归函数代码生成错误**
- **症状**：递归调用导致栈溢出或无限递归
- **原因**：函数调用的参数传递或返回处理错误
- **排查步骤**：
  1. 检查`CallFunc`的IR节点是否正确生成所有参数
  2. 验证参数计算顺序（从左到右）
  3. 检查返回值是否正确放置在栈顶
  4. 对比非递归函数的调用序列
- **调试技巧**：
  ```java
  // 在汇编器中增加调用调试信息
  @Override
  public Void visit(CallFunc callFunc) {
      logger.debug("生成函数调用: {} 参数个数: {}", 
                   callFunc.getFuncName(), 
                   callFunc.getArgCount());
      // ... 现有代码 ...
  }
  ```

## AI 协作线：Context Engineering 视角

### 上下文设计

为了让AI协助完成本地优化和代码生成的开发任务，需要精心设计上下文。

**源码文件（按阅读顺序）：**

1. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java`
   - 作用：基本块数据结构
   - 关键方法：`mergeNearBlock()`, `removeLastInstr()`, `getLastInstr()`

2. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java`
   - 作用：控制流图数据结构
   - 关键方法：`getOutDegree()`, `getInEdges()`, `removeEdge()`, `removeNode()`

3. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java`
   - 作用：控制流优化实现
   - 关键方法：`onHandle()`, 优化算法

4. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java`
   - 作用：代码生成器
   - 关键方法：所有`visit()`方法，`emit()`

5. `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java`
   - 作用：操作码映射
   - 关键方法：`emitBinaryOp()`, `emitUnaryOp()`

6. `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockOptimizationTest.java`
   - 作用：优化测试用例
   - 价值：展示优化算法的期望行为

7. `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssemblerTest.java`
   - 作用：汇编器测试用例
   - 价值：展示各种IR节点的代码生成结果

**文档文件：**

1. `ep20/README.md`
   - 作用：EP20模块概述
   - 相关部分：优化Pass、代码生成

2. `AGENTS.md`
   - 作用：代码规范和最佳实践
   - 相关部分：Pass设计模式、测试规范

### Prompt模板（给 AI 用）

#### 类型 A：添加新优化Pass Prompt模板

```
请为EP20编译器实现一个新的优化Pass：{优化名称}。

任务目标：
- 在CFG上实现{优化描述}
- 优化范围：{局部/全局}
- 预期效果：{具体优化效果}

具体要求：
1. 设计优化算法
   - 输入：CFG（控制流图）
   - 输出：优化后的CFG
   - 算法步骤：{详细算法描述}
   - 边界情况：{需要特殊处理的场景}

2. 实现接口
   - 创建类：{类全限定名}
   - 实现接口：IFlowOptimizer<IRNode>
   - 实现方法：onHandle(CFG<IRNode> cfg)

3. 添加测试
   - 测试文件：{测试类名}Test.java
   - 测试场景：
     * {场景1描述}
     * {场景2描述}
     * {场景3描述}

参考上下文文件：
- 优化Pass模板：ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java
- 基本块定义：ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java
- CFG定义：ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java
- 现有优化测试：ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockOptimizationTest.java

约束条件：
- 不破坏现有优化Pass的行为
- 保持代码风格一致（遵循AGENTS.md）
- 所有测试必须通过：mvn test -Dtest={类名}Test
- 算法时间复杂度控制在O(N)或O(N log N)

期望输出：
1. 优化Pass的完整实现代码
2. 单元测试代码
3. 优化前后对比示例（输入IR vs 输出IR）
4. 性能分析（时间复杂度和实际运行时间）
```

#### 类型 B：扩展代码生成器 Prompt模板

```
请为CymbolAssembler添加新IR节点的支持：{IR节点类型}。

任务目标：
- 实现{节点类型}的代码生成
- 生成正确的EP18 VM指令序列
- 处理所有边界情况

具体要求：
1. 实现visit()方法
   - 方法签名：public Void visit({NodeType} node)
   - 返回值：Void（使用visitor模式）
   - 逻辑步骤：
     a. {步骤1：访问子节点}
     b. {步骤2：生成指令}
     c. {步骤3：处理特殊情况}

2. 操作码映射
   - 如果涉及新运算符：更新CymbolVMIOperatorEmitter
   - 添加对应的{OperatorType}到EP18指令的映射

3. 添加测试
   - 测试文件：CymbolAssemblerTest.java
   - 测试方法：test{NodeType}()
   - 验证生成的指令序列正确

参考上下文文件：
- 代码生成器模板：ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java
- 操作码发射器：ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java
- 现有测试：ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssemblerTest.java
- IR节点定义：ep20/src/main/java/org/teachfx/antlr4/ep20/ir/

约束条件：
- 遵循现有的缩进和格式化规则
- 保持操作数访问顺序（左→右）
- 正确处理临时变量的生成和清理
- 所有现有测试必须继续通过
- 新增测试必须通过

期望输出：
1. visit()方法的完整实现
2. 操作码映射（如需要）
3. 单元测试代码
4. 使用示例和预期输出
5. 可能遇到的陷阱和解决方案
```

### AI 应该做 / 不该做

**✅ AI 允许做的事情：**

1. **实现新的优化Pass**
   - ✅ 可以：添加新的IFlowOptimizer实现
   - ✅ 可以：扩展现有ControlFlowAnalysis
   - ❌ 不能：删除现有的优化逻辑

2. **扩展代码生成器**
   - ✅ 可以：为新的IR节点添加visit()方法
   - ✅ 可以：增强操作码发射器
   - ❌ 不能：改变IRVisitor接口

3. **添加调试和日志**
   - ✅ 可以：添加logger.debug()语句
   - ✅ 可以：实现toString()辅助方法
   - ❌ 不能：在生产代码中使用System.out.println()

4. **性能优化**
   - ✅ 可以：优化算法时间复杂度
   - ✅ 可以：添加缓存机制
   - ❌ 不能：牺牲代码可读性换取微小性能提升

**❌ AI 禁止做的事情：**

1. **破坏编译器流水线**
   - ❌ 不允许：改变优化Pass的执行顺序
   - ❌ 不允许：修改IR到VM指令的映射关系
   - 原因：pipeline的正确性依赖于执行顺序

2. **引入平台相关代码**
   - ❌ 不允许：硬编码平台特定的假设
   - ❌ 不允许：假设特定的栈深度或寄存器数量
   - 原因：EP20设计为可移植到不同目标平台

3. **删除测试代码**
   - ❌ 不允许：为了"简化"而删除测试
   - ❌ 不允许：降低测试覆盖率
   - 原因：测试是编译器正确性的保证

### 验证与回滚策略

#### 自动化验证

**步骤1：编译验证**
```bash
cd ep20
mvn clean compile -DskipTests

# 预期结果：[INFO] BUILD SUCCESS
```

**步骤2：优化Pass测试**
```bash
# 运行优化测试
mvn test -Dtest=BasicBlockOptimizationTest

# 预期：所有测试通过
```

**步骤3：代码生成测试**
```bash
# 运行汇编器测试
mvn test -Dtest=CymbolAssemblerTest

# 预期：所有测试通过
```

**步骤4：集成测试**
```bash
# 创建测试程序
cat > /tmp/opt_test.cymbol << 'EOF'
int test() {
    int a = 5;
    int b = 3;
    int c = a + b;  // 优化后可能直接计算为8
    return c;
}
EOF

# 完整编译流程测试
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler" \
    -Dexec.args="/tmp/opt_test.cymbol /tmp/opt_test.vm"

# 验证输出文件存在且非空
ls -lh /tmp/opt_test.vm
```

#### 手工检查点

**检查点1：优化正确性**
- [ ] 优化不改变程序语义
- [ ] 所有测试用例通过
- [ ] 手动验证简单用例（如factorial）

**检查点2：代码生成正确性**
- [ ] 每种IR节点都有对应的visit()方法
- [ ] 操作码映射完整（覆盖所有OperatorType）
- [ ] 生成的VM指令语法正确
- [ ] EPS示例程序能正确执行

**检查点3：代码质量**
- [ ] 遵循AGENTS.md规范
- [ ] 有充分的注释和文档
- [ ] 日志输出合理使用(logger而非print)
- [ ] 测试覆盖率>80%

**检查点4：性能**
- [ ] 优化算法时间复杂度O(N)或O(N log N)
- [ ] 无明显的性能瓶颈
- [ ] 大数据量测试（1000+行IR）在合理时间内完成

#### 回滚策略

**如果优化Pass导致错误：**
```bash
# 方法1：暂时禁用优化
cd ep20
# 在Compiler.java中注释掉：
# optimizer.onHandle(cfg);
mvn compile
```

**如果代码生成器导致错误：**
```bash
# 方法2：恢复代码生成器
git checkout -- ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java

# 恢复测试
git checkout -- ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssemblerTest.java

# 重新验证
mvn test -Dtest=CymbolAssemblerTest
```

**如果新功能导致回归：**
```bash
# 方法3：使用git bisect定位问题提交
git bisect start

# 标记当前版本为坏版本
git bisect bad

# 标记已知的好版本（上一个发布版）
git bisect good HEAD~10

# Git会自动二分查找问题提交
# 每个测试点运行：
mvn test -Dtest=RegressionTest

# 找到问题提交后，回滚或修复
git revert <problem_commit>
```

**完全回滚到上一个稳定版本：**
```bash
# 找到上一个稳定版本的commit hash
git log --oneline -20

# 创建回滚分支（安全做法）
git checkout -b rollback-optimization

# 回滚到指定版本
git reset --hard <stable_commit_hash>

# 验证回归测试通过
mvn test

# 如果一切正常，强制推送到主分支（谨慎操作）
git checkout main
git reset --hard rollback-optimization
git push --force-with-lease
```

## 练习题

### 练习1：实现常量折叠优化（手工实现版）

难度：⭐⭐⭐☆☆
预计时间：45-60 分钟

题目描述：
在为LinearIRBlock实现常量折叠优化。遍历基本块中的指令序列，
ewline如果检测到操作数都是常量的运算指令，直接计算结果并替换为ICONST指令。

示例优化：
```
优化前：          优化后：
iconst 5          iconst 8
iconst 3          ...
iadd
```

要求：
- 完全手工实现，不依赖AI
- 在LinearIRBlock或ControlFlowAnalysis中添加
- 处理二元运算（如ADD、SUB、MUL）
- 编写至少3个测试用例

验收标准：
- [ ] 能正确折叠整数常量运算
- [ ] 代码编译通过，无lsp错误
- [ ] 添加单元测试且全部通过
- [ ] 集成到Compile流水线并成功运行

### 练习2：为汇编器添加数组访问支持（AI协作版）

难度：⭐⭐⭐⭐☆
预计时间：60-90 分钟

题目描述：
当前CymbolAssembler不支持数组元素的访问（load_array、store_array）。
需要实现ArrayAccess IR节点的代码生成。

示例：
a[5] = 42;  → 生成 load a, iconst 5, iadd, iconst 42, store_array
int x = a[3]; → 生成 load a, iconst 3, iadd, load_array, store x

AI协作要求：
1. 设计上下文：提供IR节点定义、测试用例和预期输出
2. 设计Prompt：使用类型B模板，明确数组操作的语义
3. 验证AI输出：检查生成的指令序列是否正确
4. 理解AI代码：确保你能解释每条生成的指令

验收标准：
- [ ] AI生成的代码能正确编译
- [ ] 生成正确的数组访问指令序列
- [ ] 添加至少2个测试用例（赋值和读取）
- [ ] 测试用例验证通过

### 练习3：实现死代码消除（高级挑战）

难度：⭐⭐⭐⭐⭐
预计时间：90-120 分钟

题目描述：
实现一个死代码消除Pass，删除那些计算结果从未被使用的指令。

示例：
```
优化前：          优化后：
iconst 5     →    iconst 5
iconst 3     →    # 删除：结果从未使用
iadd         →    # 删除：结果从未使用
dup          →    dup
```

要求：
- 实现一个完整的IFlowOptimizer
- 使用数据流分析（或简单的使用-定义链）
- 处理赋值语句、运算表达式等
- 编写全面的测试用例
- 与AI协作设计算法（但手工编码实现）

验收标准：
- [ ] 能识别真正的死代码
- [ ] 不删除有副作用的代码（如print）
- [ ] 不删除影响控制流的代码
- [ ] 代码通过所有测试用例
- [ ] 添加性能测试（大函数优化速度）

## 本章小结与下一章预告

### 本章小结

通过本章的学习，你已经掌握了：

1. **本地优化技术**
   - 理解了控制流优化的核心算法
   - 学会了冗余跳转消除和基本块合并
   - 掌握了优化Pass的设计模式

2. **代码生成技术**
   - 学习了指令选择的基本原则
   - 掌握了将IR映射到VM指令的方法
   - 理解了完整编译器后端架构

3. **实战技能**
   - 能够调试优化相关的问题
   - 学会了验证优化正确性的方法
   - 掌握了编译器集成和测试

4. **AI协作**
   - 学会了设计优化算法的Prompt
   - 掌握了代码生成的Prompt技巧
   - 理解了编译器任务的验证策略

### 【你现在站在】:
```
... → [CFG构建] → ✅ [本地优化] → ✅ [代码生成]
```

**当前在编译器流水线的位置**：
- 你已经完成了编译器后端的核心功能
- 生成的代码可以在EP18虚拟机上运行
- 本地优化提升了代码质量和执行效率

### 下一章预告

第16章将聚焦于**端到端编译器流水线**，你将学习：
- 如何将前端、中端、后端集成为一个完整的编译器
- 错误处理和诊断系统
- 编译器驱动和命令行接口
- 多Pass协调和依赖管理

**准备**：为了学习下一章，建议：
- [ ] 复习EP20的完整编译流程
- [ ] 运行所有EP20测试：mvn test
- [ ] 阅读Compiler.java的集成代码
- [ ] 思考如何扩展支持新优化Pass

继续加油！你已经掌握了编译器后端的核心技术，完整编译器就在眼前！

---

**本章关联代码**：
- 优化器：`ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java`
- 汇编器：`ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java`
- 操作码：`ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java`
- 测试：`ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockOptimizationTest.java`
- 测试：`ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssemblerTest.java`

**验证命令**：
```bash
cd ep20
mvn test -Dtest=BasicBlockOptimizationTest,CymbolAssemblerTest
```
