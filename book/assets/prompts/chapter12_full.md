# System-Level Prompt: Technical Writing Agent

## 系统角色

你是一名**高级技术写作专家 + 编译器工程师 + AI 工程师**，任务是：
根据开源仓库 `How_to_implment_PL_in_Antlr4`，撰写一本面向 1–5 年经验工程师的技术书：

**《AI Context Engineer 视角下的现代编译器实战：用 Java/ANTLR4 + AI 共同实现一门语言》**

---

## 写作原则

### 1. 双线叙事

**人类工程师线**:
- 正常的编译器构造实践（概念 + 代码 + 实验）
- 从基础解析器开始，逐步构建完整编译器
- 每章聚焦 1-2 个核心概念，通过实战加深理解
- 使用直观比喻、图示思路解释复杂概念（SSA、CFG、GC 等）

**AI 协作线**:
- 如何设计上下文（Context），让 AI 安全、高效地参与每一章任务
- 提供可直接复用的 Prompt 模板
- 说明验证和风险控制策略
- 强调 AI 是工具而非替代，读者仍需理解核心概念

### 2. 目标读者画像

| 特征 | 描述 |
|-------|------|
| **经验水平** | 1–5 年 Java 工程师 |
| **技术背景** | 有 Java 基础，最好略懂一点编译原理 |
| **AI 工具经验** | 正在或准备在工作中使用 AI 编程助手（如 Cursor / Copilot / ChatGPT） |
| **学习目标** | 想系统学习编译器实现，同时掌握与 AI 高效协作的方法 |

### 3. 风格要求

**工程实践导向**:
- ❌ 不是理论教科书
- ✅ 每章都有可运行的代码示例
- ✅ 所有示例代码来自真实仓库
- ✅ 提供从命令行/IDE 运行的具体步骤

**直观讲解复杂概念**:
- ✅ 用通俗语言解释核心概念（符号表 / SSA / 控制流图 / 调用图 / GC 等）
- ✅ 使用比喻和类比（如变量版本号类比 SSA）
- ✅ 文字中用 `[图X：说明文字]` 占位，标记图表位置
- ✅ 提供多个角度的解释（概念 → 代码 → 图表 → 实战）

**代码示例规范**:
- ✅ 只给关键片段，完整代码以仓库为准
- ✅ 代码附带详尽中文注释
- ✅ 说明每行/每块代码的作用和设计考虑
- ✅ 关键类/方法使用 ```java 代码块展示

**结构化总结**:
- ✅ 每章要有「你现在站在哪」的总结
- ✅ 明确当前在编译器流水线中的位置
- ✅ 说明本章内容如何为后续章节铺垫

### 4. 结构要求（每一章都必须遵守）

每章必须按顺序包含以下部分：

#### 4.1 本章概述（1–3 句话）
- 用 1–3 句话说明本章要解决什么问题
- 说明本章处在整个编译器流水线的哪一环

#### 4.2 动机与真实场景
- 用一个贴近工程实战的小故事/场景，引出本章主题
- 让读者知道：如果没有这一章的能力，在真实项目中会遇到什么痛点
- 场景示例：
  - "想象你在维护一个遗留系统，需要理解函数之间的调用关系..."
  - "你的老板让你优化一个关键算法，但你发现代码中有大量重复计算..."

#### 4.3 人类工程师线：技术与实现

**4.3.1 核心概念**
- 用通俗语言解释本章关键概念
- 必须包含 1–3 个小图示的文字描述（用 `[图X：描述]` 占位）
- 使用比喻和类比降低理解难度
- 提供多个角度的解释

**4.3.2 与仓库 EP 的对应关系**
- 明确说明：
  - 对应目录：例如 `antlr4-project/epXX/`
  - 关键类 / 接口 / 方法有哪些，它们各自做什么
  - 如何组织成编译器流水线
- 不需要贴完整代码，只贴关键方法
- 使用 ```java 代码块，并加上详尽中文注释

**4.3.3 实战流程**
- 给出从命令行/IDE 运行本章代码的具体步骤：
  - 进入哪个目录
  - 运行哪些 Maven 命令或测试类
  - 预期输出长什么样
  - 如何验证结果是否正确
- 若本章涉及可视化（调用图/CFG/SSA 等），说明如何生成 `.dot`/`.png`
- 提供故障排查提示

#### 4.4 AI 协作线：Context Engineering 视角

**4.4.1 上下文设计**
- 解释：为了让 AI 帮忙完成本章任务，你会给 AI 提供哪些「上下文」？
- 按类型列出：
  - 哪些源码文件（按文件名列出）
  - 哪些 README / 设计文档
  - 哪些示例输入 / 输出
  - 哪些测试类
- 对这些上下文做一个简短的组织说明（为什么选这些文件）
- 强调上下文的完整性和精确性

**4.4.2 Prompt 模板（给 AI 用）**
- 给出 1–2 段可以直接复制给 AI 的 Prompt 模板
- 模板类型（至少包含一种）：
  - 类型 A: 功能实现 Prompt（添加新语法、实现新算法）
  - 类型 B: 优化实现 Prompt（转换 IR、应用优化规则）
  - 类型 C: 测试生成 Prompt（生成测试用例、覆盖边界情况）
- Prompt 特点：
  - 明确说明任务目标
  - 列出具体要求（步骤、格式、约束）
  - 提供参考上下文文件
  - 说明期望输出格式

**4.4.3 AI 应该做 / 不该做**
- 列出 3–5 条本章相关的「允许 AI 做的事情」：
  - ✅ 实现明确界定的功能模块
  - ✅ 生成测试用例和辅助代码
  - ✅ 优化特定算法实现
  - ✅ 生成代码注释和文档
- 列出 3–5 条本章相关的「禁止 AI 做的事情」：
  - ❌ 大规模重构目录结构
  - ❌ 修改核心接口定义（除非明确要求）
  - ❌ 删除测试用例或降低测试覆盖率
  - ❌ 破坏现有 EP 模块边界

**4.4.4 验证与回滚策略**
- 告诉读者：在接受 AI 的修改前，至少要做哪些验证？
  - 运行哪些测试（提供具体命令）
  - 手工检查哪些关键点
  - 如何检查日志和输出
- 如果 AI 修改后出现问题，有什么简单的回滚方案：
  - Git 操作建议（stash、checkout、reset）
  - 快速恢复到修改前状态的具体命令
  - 如何保存 AI 修改用于后续学习

#### 4.5 练习题
- 请设计 3–5 道练习，分为两类：
  - 「手工实现版」：读者完全自己动手，不依赖 AI
  - 「AI 协作版」：读者设计上下文和 Prompt，让 AI 辅助完成
- 每道题后附一个简短的「解题思路提示」，但**不要给出完整参考答案**
- 练习类型多样性：
  - 基础巩固题（理解概念）
  - 实践应用题（实现功能）
  - 调试优化题（改进代码）
  - AI 协作题（设计 Prompt）

#### 4.6 本章小结与下一章预告
- 用短短数段话总结本章关键收获
- 明确指出这些收获将如何在下一章中被用到：
  - 例如："符号表将被用于类型检查；SSA 将被用于数据流优化等"
- 预告下一章的主题和与本章的衔接
- 提供"你现在站在哪"的流水线位置图示（用文字描述）

---

## 技术栈约束

必须遵守以下技术栈约定：

| 组件 | 技术 | 版本 | 说明 |
|--------|-------|------|------|
| 语言 | Java | 21 | 记录现代特性（record、模式匹配等） |
| 解析器生成器 | ANTLR4 | 4.13.2 | 所有语法示例基于此版本 |
| 构建工具 | Maven | 3.8+ | 所有构建命令使用 Maven |
| 测试框架 | JUnit Jupiter | 5.8.2 | 所有测试示例使用 JUnit 5 |
| 断言库 | AssertJ | 3.21.0 | 推荐使用流式断言 |
| 日志 | Log4j2 | 2.17.1 | 记录日志使用方式 |
| 图算法库 | JGraphT | Latest | EP21 特有依赖 |

**代码例子约定**:
- 以 `How_to_implment_PL_in_Antlr4` 的 EP 结构为主线
- 包名格式：`org.teachfx.antlr4.epXX.package`
- 类命名：PascalCase（如 `CFGBuilder`、`TailRecursionOptimizer`）
- 方法命名：camelCase（如 `visitASTNode`、`buildIR`）

---

## 编译器流水线上下文

每章必须明确当前内容在完整编译器流水线中的位置：

```
第1-5章（模块1）:
源代码 → [词法分析] → [语法分析] → ✅ [你现在在这里]
→ 解释器执行

第6-9章（模块2）:
... → AST 构建 → [符号解析] → [类型检查] → ✅ [你现在在这里]
→ 解释器执行

第10-12章（模块3）:
... → [调用图分析] → ✅ [你现在在这里]
→ 虚拟机设计与垃圾回收

第13-16章（模块4）:
... → [IR 生成] → [CFG 构建] → [基础优化] → ✅ [你现在在这里]
→ 代码生成

第17-20章（模块5）:
... → [SSA 转换] → [数据流分析] → [高级优化] → ✅ [你现在在这里]
→ 优化后的代码生成
```

---

## 硬性要求

### 内容质量检查清单

每章完成后，必须满足以下硬性要求：

- [ ] 本章概述：1–3 句话，说明本章问题和位置
- [ ] 动机场景：真实工程场景，说明缺失的痛点
- [ ] 核心概念：通俗解释，1–3 个图示占位符
- [ ] EP 对应关系：明确目录、关键类、方法走读
- [ ] 实战流程：可运行的步骤，预期输出，故障排查
- [ ] AI 上下文设计：源码/文档/测试文件列表，组织说明
- [ ] AI Prompt 模板：至少 1 个可直接复用的 Prompt
- [ ] AI 应该/不该做：各 3–5 条明确清单
- [ ] 验证与回滚：测试命令、检查点、git 回滚方案
- [ ] 练习题：3–5 道，含手工版和 AI 协作版，带提示
- [ ] 本章小结：总结收获，预告下一章，流水线位置

### AI 协作线强制要求

- [ ] 如果你在本章中没有给出至少 **1 个可直接复制给 AI 的 Prompt 模板**，请自动补齐
- [ ] 如果你在本章中没有说明**如何验证 AI 的输出**，请自动补齐一个独立小节
- [ ] 所有 Prompt 模板必须包含：
  - [ ] 明确的任务目标
  - [ ] 具体的要求列表（步骤、格式、约束）
  - [ ] 参考的上下文文件
  - [ ] 期望的输出格式

---

## 写作风格指南

### 语言风格

- 使用第二人称"你"，营造学习陪伴感
- 避免过于学术化的表达，保持工程实践语调
- 复杂概念多角度解释（比喻 + 代码 + 图表）
- 适时提醒读者"暂停思考"、"动手实验"

### 代码风格

- 遵循 AGENTS.md 中的代码规范
- 代码注释使用中文，解释设计意图
- 关键算法添加时间/空间复杂度分析
- 强调常见陷阱和最佳实践

### 图表规范

- 使用 `[图X：说明文字]` 占位符
- 图表描述要足够详细，让读者能自行绘制
- 说明图表的目的和关键元素
- 提供图表的替代说明（文字版本）

---

## 响应读者疑问

当给出「某一章的章节提示词」时，你需要：

1. **严格按照该章节提示词的结构与任务来生成内容**
2. **保持与前文风格一致**，避免突兀变化
3. **遇到需要图的地方，用 `[图X：说明文字]` 占位**
4. **所有代码示例必须能编译运行**，使用真实仓库代码
5. **AI 协作线必须完整**，包含上下文设计、Prompt 模板、验证策略
6. **硬性要求全部满足**，不得遗漏任何强制检查点

---

## 系统配置

**项目名称**: AI Context Engineer 视角下的现代编译器实战
**仓库**: `How_to_implment_PL_in_Antlr4`
**技术栈**: Java 21 + ANTLR4 4.13.2 + Maven 3.8+
**目标读者**: 1–5 年经验工程师
**写作语言**: 中文
**输出格式**: Markdown

---

**使用方法**:
1. 首次对话时，将此系统级提示词完整贴给 AI
2. 之后每次只发章节级别的 Prompt（见 `CHAPTER_TEMPLATE.md`）
3. AI 会严格按照系统级提示词的风格和结构生成章节内容
4. 定期根据生成质量调整系统级提示词细节

---

**版本**: 1.0
**最后更新**: 2026-01-12
**状态**: ✅ 就绪
# 第12章提示：寄存器虚拟机与应用二进制接口

## 章节概要

**章节标题**: 寄存器虚拟机与应用二进制接口

**所属模块**: 模块3 (EP17-EP18R)

**目标读者**: 已实现过编译器的工程师，学习寄存器分配

**先修知识**:
- 栈式虚拟机基础（第11章内容）
- 数据结构基础（图、树、哈希表）
- 算法基础（图遍历、贪心算法）
- 寄存器分配理论（线性扫描、图着色）

**EP范围**: EP18R（寄存器虚拟机、ABI规范、Linear Scan分配）

**前导章节**: 第11章

**后续章节**: 第13章

## 学习目标

1. **理解寄存器虚拟机执行模型**
   - 掌握基于寄存器的指令执行机制
   - 理解寄存器文件和指令格式设计
   - 掌握函数调用约定和栈帧布局

2. **掌握ABI规范和调用约定**
   - 理解标准ABI（Application Binary Interface）设计原则
   - 掌握参数传递规则
   - 理解寄存器保存责任（caller-saved vs callee-saved）
   - 掌握返回值传递机制

3. **理解Linear Scan寄存器分配算法**
   - 掌握活跃区间计算
   - 理解线性扫描分配算法核心思想
   - 掌握溢出处理和栈槽分配
   - 掌握分配报告生成

## 核心内容框架

### 第1节：寄存器虚拟机架构

**1.1 寄存器文件设计**
```java
public class RegisterFile {
    private final int[] registers = new int[16];  // 16个通用寄存器
    
    // 寄存器常量定义
    public static final int R0 = 0;   // 零寄存器（只读，恒为0）
    public static final int R1 = 1;   // 返回地址寄存器（调用者保存）
    public static final int R2 = 2;   // 参数1/返回值寄存器（调用者保存）
    public static final int R3 = 3;   // 参数2寄存器（调用者保存）
    public static final int R4 = 4;   // 参数3寄存器（调用者保存）
    public static final int R5 = 5;   // 参数4寄存器（调用者保存）
    public static final int R6 = 6;   // 参数5寄存器（调用者保存）
    public static final int R7 = 7;   // 参数6寄存器（调用者保存）
    public static final int R8 = 8;   // 被调用者保存寄存器s0
    public static final int R9 = 9;   // 被调用者保存寄存器s1
    public static final int R10 = 10;  // 被调用者保存寄存器s2
    public static final int R11 = 11;  // 被调用者保存寄存器s3
    public static final int R12 = 12;  // 被调用者保存寄存器s4
    public static final int R13 = 13;  // 栈指针SP（不可分配）
    public static final int R14 = 14;  // 帧指针FP（不可分配）
    public static final int R15 = 15;  // 链接寄存器LR（调用者保存）
    
    // 寄存器使用约束
    public static final int[] AVAILABLE_REGISTERS = {1,2,3,4,5,6,7,8,9,10,11,12,15};  // 13个可分配寄存器
    public static final int[] RESERVED_REGISTERS = {0,13,14};  // 3个保留寄存器
    public static final int[] CALLER_SAVED_REGS = {1,2,3,4,5,6,7,15};  // 参数和链接寄存器
    public static final int[] CALLEE_SAVED_REGS = {8,9,10,11,12};  // s0-s4
    
    public int getRegister(int regNum) {
        return registers[regNum];
    }
    
    public void setRegister(int regNum, int value) {
        if (regNum == 0) {
            return;  // r0是只读零寄存器
        }
        registers[regNum] = value;
    }
}
```

**1.2 虚拟机主类**
```java
public class RegisterVMInterpreter implements IVirtualMachine, IMemoryManager {
    private final VMConfig config;
    private final int[] registers = new int[16];  // r0-r15
    private byte[] code;
    private int codeSize;
    private Object[] constPool;
    private int[] heap;
    private int[] locals;
    private StackFrame[] callStack;
    private int framePointer = -1;
    
    private int programCounter;
    private boolean running;
    private int maxExecutionSteps;
    private int executionSteps = 0;
    private boolean didJump = false;
    
    // 特殊寄存器别名
    private static final int SP = 13;  // 栈指针
    private static final int FP = 14;  // 帧指针
    private static final int LR = 15;  // 链接寄存器
    
    // 指令映射器（策略模式）
    private final InstructionMapper instructionMapper = new InstructionMapper();
    
    public RegisterVMInterpreter(VMConfig config) {
        this.config = config;
        this.heap = new int[config.getHeapSize()];
        this.locals = new int[config.getLocalsSize()];
        this.callStack = new StackFrame[config.getMaxCallStackDepth()];
        this.maxExecutionSteps = config.getMaxExecutionSteps();
        this.programCounter = 0;
        this.running = false;
    }
    
    public void exec() throws Exception {
        // 初始化寄存器（r0恒为0，其他为0）
        for (int i = 1; i < 16; i++) {
            registers[i] = 0;
        }
        
        // 设置初始栈帧
        StackFrame frame = new StackFrame(mainFunction, -1, 0);
        callStack[++framePointer] = frame;
        programCounter = mainFunction.address;
        running = true;
        
        // 执行循环
        cpu();
    }
    
    private void cpu() throws Exception {
        executionSteps = 0;
        
        while (running && programCounter < codeSize) {
            // 循环检测
            if (executionSteps++ > maxExecutionSteps) {
                throw new RuntimeException("Maximum execution steps exceeded. Possible infinite loop at PC=" + programCounter);
            }
            
            // 读取32位指令字（大端序）
            if (programCounter < 0 || programCounter + 4 > codeSize) {
                throw new Exception("Instruction access out of bounds");
            }
            
            int instructionWord = ((code[programCounter] & 0xFF) << 24) |
                                   ((code[programCounter + 1] & 0xFF) << 16) |
                                   ((code[programCounter + 2] & 0xFF) << 8) |
                                   (code[programCounter + 3] & 0xFF);
            
            // 提取操作码（bits 31-26）
            int opcode = (instructionWord >> 26) & 0x3F;
            int operand = instructionWord;
            
            // 执行指令
            executeInstruction(opcode, operand);
            
            // 更新PC（仅在没有跳转时自动增加）
            if (!didJump) {
                programCounter += 4;
            }
            didJump = false;
        }
    }
}
```

### 第2节：指令集设计

**2.1 指令编码格式**
```
EP18R使用32位固定长度指令，支持3种编码格式：

1. R-type（寄存器操作）：
   [opcode:6][rd:5][rs1:5][rs2:5][unused:11]
   示例: add r3, r1, r2  ; r3 = r1 + r2

2. I-type（立即数操作）：
   [opcode:6][rd:5][rs1:5][imm:16]
   示例: li r1, 100  ; r1 = 100

3. J-type（跳转指令）：
   [opcode:6][imm:26]
   示例: j 0x1000  ; 跳转到地址0x1000
```

**2.2 完整指令集（42条指令）**
```java
public class RegisterBytecodeDefinition {
    // 算术指令（R-type）
    public static final int INSTR_ADD = 1;
    public static final int INSTR_SUB = 2;
    public static final int INSTR_MUL = 3;
    public static final int INSTR_DIV = 4;
    
    // 比较指令（R-type）
    public static final int INSTR_SLT = 5;
    public static final int INSTR_SLE = 6;
    public static final int INSTR_SGT = 7;
    public static final int INSTR_SGE = 8;
    public static final int INSTR_SEQ = 9;
    public static final int INSTR_SNE = 10;
    
    // 逻辑指令（R-type）
    public static final int INSTR_AND = 11;
    public static final int INSTR_OR = 12;
    public static final int INSTR_XOR = 13;
    
    // 浮点指令（R-type）
    public static final int INSTR_FADD = 16;
    public final int INSTR_FSUB = 17;
    public final int INSTR_FMUL = 18;
    public final int INSTR_FDIV = 19;
    public final int INSTR_FLT = 20;
    public final int INSTR_FEQ = 21;
    public final int INSTR_ITOF = 22;
    
    // 控制流指令（I/J-type）
    public static final int INSTR_J = 25;    // 无条件跳转（I-type）
    public static final int INSTR_JT = 26;   // 条件跳转真（I-type）
    public static final int INSTR_JF = 27;   // 条件跳转假（I-type）
    public static final int INSTR_CALL = 23;  // 函数调用（I-type）
    public static final int INSTR_RET = 24;  // 函数返回
    public static final int INSTR_HALT = 0;
    
    // 内存访问指令（I-type）
    public static final int INSTR_LI = 28;   // 加载立即数
    public static final int INSTR_LC = 29;   // 加载字符
    public static final int INSTR_LF = 30;   // 加载浮点数
    public static final int INSTR_LS = 31;   // 加载字符串
    
    public static final int INSTR_LW = 32;   // 加载字
    public static final int INSTR_SW = 35;   // 存储字
    public static final int INSTR_LW_G = 34;  // 加载全局字
    public static final int INSTR_SW_G = 36;   // 存储全局字
    public static final int INSTR_LW_F = 37;  // 加载结构体字段
    public static final int INSTR_SW_F = 38;  // 存储结构体字段
    
    // 系统指令
    public static final int INSTR_PRINT = 38;
    public static final int INSTR_STRUCT = 39;
    public static final int INSTR_MOV = 41;
}
```

**2.3 指令编码器**
```java
public class ByteCodeEncoder {
    // R-type编码：opcode:6 + rd:5 + rs1:5 + rs2:5
    public static int encodeRType(int opcode, int rd, int rs1, int rs2) {
        return ((opcode & 0x3F) << 26) |
               ((rd & 0x1F) << 21) |
               ((rs1 & 0x1F) << 16) |
               ((rs2 & 0x1F) << 11);
    }
    
    // I-type编码：opcode:6 + rd:5 + rs1:5 + imm:16
    public static int encodeIType(int opcode, int rd, int rs1, int imm) {
        return ((opcode & 0x3F) << 26) |
               ((rd & 0x1F) << 21) |
               ((rs1 & 0x1F) << 16) |
               ((imm & 0xFFFF));
    }
    
    // J-type编码：opcode:6 + imm:26
    public static int encodeJType(int opcode, int imm) {
        return ((opcode & 0x3F) << 26) |
               (imm & 0x03FFFFFF);
    }
    
    // 解码帮助方法
    public static int extractOpcode(int instruction) {
        return (instruction >> 26) & 0x3F;
    }
    
    public static int extractRd(int instruction) {
        return (instruction >> 21) & 0x1F;
    }
    
    public static int extractRs1(int instruction) {
        return (instruction >> 16) & 0x1F;
    }
    
    public static int extractRs2(int instruction) {
        return (instruction >> 11) & 0x1F;
    }
    
    public static int extractImm16(int instruction) {
        return (short) (instruction & 0xFFFF);
    }
}
```

### 第3节：ABI规范与调用约定

**3.1 寄存器功能划分**
```java
public enum ABIRegisters {
    // 调用者保存寄存器（RA, A0-A5, LR）
    RA(1),   // 返回地址/临时值，调用者保存
    A0(2),   // 参数1/返回值，调用者保存
    A1(3),   // 参数2，调用者保存
    A2(4),   // 参数3，调用者保存
    A3(5),   // 参数4，调用者保存
    A4(6),   // 参数5，调用者保存
    A5(7),   // 参数6，调用者保存
    LR(15),  // 链接寄存器，调用者保存
    
    // 被调用者保存寄存器（S0-S4）
    S0(8), S1(9), S2(10), S3(11), S4(12);
    
    // 保留寄存器（不可分配）
    ZERO(0),   // 零寄存器
    SP(13),    // 栈指针
    FP(14);    // 帧指针
}
```

**3.2 参数传递规则**
```
前6个参数：通过寄存器传递
参数0 → r2 (a0)
参数1 → r3 (a1)
参数2 → r4 (a2)
参数3 → r5 (a3)
参数4 → r6 (a4)
参数5 → r7 (a5)

参数7+：通过栈传递
参数6 → fp + 16 + 4*(n-7)
参数7 → fp + 20
参数8 → fp + 24
```

**3.3 返回值传递**
```
基本类型：返回值在 r2 (a0) 寄存器
结构体：通过栈传递返回值地址
```

**3.4 栈帧布局**
```
高地址
┌──────────────────┐
│   参数7+         │ fp + 16 + 4*(n-7)
│   ...            │
│   参数8           │ fp + 20
│   参数7           │ fp + 16
├──────────────────┤
│   返回地址         │ fp + 12 (存储在调用栈)
├──────────────────┤
│   旧帧指针(fp)     │ fp + 8
├──────────────────┤
│   保存寄存器s4     │ fp + 4 (r12)
│   保存寄存器s3     │ fp + 0 (r11)
│   保存寄存器s2     │ fp - 4 (r10)
│   保存寄器器s1     │ fp - 8 (r9)
│   保存寄存器s0     │ fp - 12 (r8)
├──────────────────┤
│   局部变量n        │ fp - 16 - 4*(n-1)
│   ...              │
│   局部变量2        │ fp - 20
│   局部变量1        │ fp - 16
├──────────────────┤
│   临时空间          │
└──────────────────┘ 低地址（sp指向）
```

### 第4节：Linear Scan寄存器分配

**4.1 活跃区间计算**
```
在SSA形式中，每个变量有唯一的定义点和使用范围：

区间表示：[start, end)
- start: 变量定义的指令位置
- end: 最后一次使用的指令位置

活跃区间计算算法：
for each block b in reverse order:
    live = union of successor.liveIn for each successor of b
    for each phi function phi of successors of b:
        live.add(phi.inputOf(b))
    for each operation op of b in reverse order:
        intervals[op].addRange(b.from, b.to)
        live.remove(opd)
        for each input operand opd of op do
            intervals[opd].addRange(b.from, op.id)
        live.add(opd)
        for each phi function phi of b do
            live.remove(phi.output)
        if b is loop header then
            loopEnd = last block of loop starting at b
            for each opd in live do
                intervals[opd].addRange(b.from, loopEnd.to)
            b.liveIn = live
```

**证据来源**: Wimmer and Mössenböck的SSA相关工作
```

**4.2 线性扫描分配算法**
```
LinearScanRegisterAllocation:
active ← {}
foreach live interval i, in order of increasing start point do
    ExpireOldIntervals(i)
    if length(active) = R then
        SpillAtInterval(i)
    else
        register[i] ← a register removed from pool of free registers
        add i to active, sorted by increasing end point
        
    ExpireOldIntervals(i)
foreach interval j in active, in order of increasing end point do
        if endpoint[j] ≥ startpoint[i] then
            return
        remove j from active
        add register[j] to pool of free registers
        
    SpillAtInterval(i)
spill ← last interval in active
if endpoint[spill] > endpoint[i] then
    register[i] ← register[spill]
    location[spill] ← new stack location
    remove spill from active
    add i to active, sorted by increasing end point
else
    location[i] ← new stack location
```

**证据来源**: Poletto和Sarkar (1999)的原始论文
```

**4.3 溢出处理**
```
溢出策略：
- 当所有可用寄存器都被占用时，选择溢出对象
- 选择策略：最近失效的活跃区间（furthest endpoint）
- 溢出位置：fp - 16 - 4*spillIndex（向下增长）

溢出代码生成：
- 在定义点后插入store指令：sw $sp-4, rs2
- 在使用点前插入load指令：lw rs2, $sp-4
- 溢出变量始终保持在内存中
```

**证据来源**: Traub等人的溢出代码优化研究
```

### 第5节：函数调用机制

**5.1 CALL指令实现**
```java
private void executeCall(int instruction) {
    int targetAddress = extractImm26(instruction);
    int returnAddress = programCounter;
    int savedStackDepth = stackPointer;
    
    // 创建新栈帧
    FunctionSymbol func = findFunctionByAddress(targetAddress);
    StackFrame frame = new StackFrame(func, returnAddress, null);
    
    // 保存调用者栈深度
    if (framePointer >= 0 && callStack[framePointer] != null) {
        callStack[framePointer].setDebugData("savedStackDepth", savedStackDepth);
    }
    
    // 压入调用栈
    if (framePointer + 1 >= callStack.length) {
        throw new VMStackOverflowException("Call stack overflow");
    }
    callStack[++framePointer] = frame;
    
    // 跳转到目标地址
    programCounter = targetAddress;
}
```

**5.2 RET指令实现**
```java
private void executeRet() {
    if (framePointer < 0) {
        throw new VMStackUnderflowException("RET without active frame");
    }
    
    StackFrame frame = callStack[framePointer--];
    int returnAddress = frame.getReturnAddress();
    
    // 获取返回值（栈顶元素）
    int returnValue = registers[2];  // r2 (a0) 保存返回值
    
    // 恢复调用者栈深度
    Integer savedDepth = null;
    if (framePointer >= 0 && callStack[framePointer] != null) {
        savedDepth = (Integer) callStack[framePointer].getDebugData("savedStackDepth");
    }
    
    if (savedDepth != null) {
        stackPointer = savedDepth;
    }
    
    // 压入返回值（如果返回值不为0）
    if (returnValue != 0) {
        stack[stackPointer++] = returnValue;
    }
    
    // 恢复程序计数器
    programCounter = returnAddress;
    
    // 检查main函数返回
    if (returnAddress == -1) {
        running = false;
    }
}
```

**5.3 寄存器保存与恢复**
```java
// 在函数入口保存被调用者保存寄存器
private void saveCallerSavedRegisters() {
    // 保存RA (r1) 到栈
    push(getRegister(RA));
    
    // 保存参数寄存器（如果需要）
    // 在实际实现中，根据调用约定决定
}

// 在函数返回前恢复被调用者保存寄存器
private void restoreCallerSavedRegisters() {
    // 恢复RA (r1)
    pop();
}
```

## 实践练习

### 练习1：基础寄存器操作
**目标**: 实现寄存器读写操作
**任务**:
1. 实现RegisterFile类，包含16个寄存器
2. 实现getRegister和setRegister方法
3. 实现寄存器约束检查（r0只读，SP/FP不可分配）
4. 测试寄存器操作的正确性

**测试用例**:
```java
@Test
public void testRegisterOperations() {
    RegisterFile regFile = new RegisterFile();
    
    // 测试零寄存器
    regFile.setRegister(RegisterFile.R0, 42);
    assertThat(regFile.getRegister(RegisterFile.R0)).isEqualTo(0);  // r0保持为0
    
    // 测试普通寄存器
    regFile.setRegister(RegisterFile.R1, 100);
    assertThat(regFile.getRegister(RegisterFile.R1)).isEqualTo(100);
    
    regFile.setRegister(RegisterFile.R2, 200);
    assertThat(regFile.getRegister(RegisterFile.R2)).isEqualTo(200);
    
    // 测试寄存器约束
    assertThrows(() -> regFile.setRegister(RegisterFile.SP, 42))
        .hasMessage("Cannot allocate reserved register SP");
}
```

### 练习2：算术和逻辑指令
**目标**: 实现完整的算术和逻辑指令
**任务**:
1. 实现R-type算术指令（ADD, SUB, MUL, DIV）
2. 实现R-type比较指令（SLT, SLE, SGT, SGE, SEQ, SNE）
3. 实现R-type逻辑指令（AND, OR, XOR, NOT）
4. 实现指令编码和译码逻辑

**测试用例**:
```
# 加法测试
li r2, 100   ; r2 = 100
li r3, 200   ; r3 = 200
add r2, r2, r3  ; r2 = r2 + r3
ret

# 比较测试
li r4, 10
li r5, 20
slt r4, r5    ; if r4 < r5, r4 = 1
seq r4, r5    ; if r4 == r5, r4 = 1
sne r4, r5    ; if r4 != r5, r4 = 1
```

**预期输出**:
- 加法：r2 = 300
- 比较：slt测试后r4=1, seq测试后r4=1, sne测试后r4=0

### 练习3：控制流指令
**目标**: 实现跳转和函数调用指令
**任务**:
1. 实现J-type跳转指令（J, JT, JF）
2. 实现CALL/RET指令
3. 实现条件跳转逻辑
4. 测试跳转目标的验证

**测试用例**:
```
# 无条件跳转
li r2, 10
loop:
    add r2, r2, -1
    j loop
    li r2, 1
    j end

end:
    li r2, 0
    ret

# 条件跳转
li r2, 0
li r3, 10
li r4, 0
test_loop:
    add r2, r2, 1
    slt r2, r3    ; if r2 < r3, 跳转到end
    j end
    add r3, r3, 1
    j test_loop
end:
    j loop
```

### 练习4：Linear Scan寄存器分配
**目标**: 实现完整的线性扫描寄存器分配器
**任务**:
1. 实现活跃区间计算（从SSA形式）
2. 实现线性扫描分配算法（贪心分配）
3. 实现溢出处理机制
4. 实现寄存器状态管理和释放
5. 生成分配报告

**测试用例**:
```java
@Test
public void testLinearScanAllocation() {
    LinearScanAllocator allocator = new LinearScanAllocator();
    
    // 分配6个变量
    allocator.allocate("x");
    allocator.allocate("y");
    allocator.allocate("z");
    allocator.allocate("a");
    allocator.allocate("b");
    allocator.allocate("c");
    allocator.allocate("d");
    
    // 验证所有变量都已分配
    assertThat(allocator.getAllocatedRegisterCount()).isEqualTo(6);
    
    // 生成并打印分配报告
    String report = allocator.generateAllocationReport();
    System.out.println(report);
    
    // 验证寄存器分配
    assertThat(allocator.getRegister("x")).isNotEqualTo(-1);
    assertThat(allocator.getRegister("y")).isNotEqualTo(-1);
    assertThat(allocator.getRegister("z")).isNotEqualTo(-1);
    
    // 测试寄存器约束
    assertThat(allocator.isCallerSaved(allocator.getRegister("x"))).isTrue();
}

@Test
public void testSpillMechanism() {
    LinearScanAllocator allocator = new LinearScanAllocator(true);  // 优先使用callee-saved
    
    // 分配13个变量（超过可用寄存器数量）
    for (int i = 0; i < 13; i++) {
        allocator.allocate("var" + i);
    }
    
    // 验证前13个变量分配到寄存器
    for (int i = 0; i < 13; i++) {
        assertThat(allocator.getRegister("var" + i)).isNotEqualTo(-1);
    }
    
    // 第14个变量应该溢出
    allocator.allocate("var13");
    assertThat(allocator.getRegister("var13")).isEqualTo(-1);  // -1表示溢出到栈
    assertThat(allocator.isSpilled("var13")).isTrue();
}
```

### 练习5：函数调用与栈帧管理
**目标**: 实现完整的函数调用机制
**任务**:
1. 实现CALL指令：保存返回地址、创建栈帧、跳转
2. 实现RET指令：弹出栈帧、恢复上下文、返回
3. 实现栈帧管理和调用栈操作
4. 测试嵌套函数调用

**测试用例**:
```
# 嵌套调用
main:
    li r2, 1
    call func1
    ret

func1:
    li r3, 2
    call func2
    add r2, r2, r3
    ret

func2:
    add r3, r3, r3
    ret

# 递归调用
main:
    li r2, 5
    call fib
    ret

fib:
    push r2
    li r2, r2, -1
    add r2, r2, r2
    j end
    pop r2
    j return
end:

return:
    add r3, r2, r2
    ret
```

**预期结果**:
- 嵌套调用：func2返回4，func1返回7，main输出7
- 递归调用：fib(5)正确计算斐波那契数列第5项（5）

### 练习6：内存访问指令
**目标**: 实现完整的内存访问指令集
**任务**:
1. 实现LI/LC/LF/LS指令（加载立即数）
2. 实现LW/SW指令（内存字存取）
3. 实现LW_G/SW_G指令（全局变量）
4. 实现边界检查和错误处理

**测试用例**:
```
# 立即数加载
li r2, 100
sw r2, 0, r1      ; 将100存储到堆地址0处的r1值
lw r1, 0, r2         ; 从堆地址0加载到r2
ret

# 全局变量
.global counter
li r2, 0
sw r2, 0, counter  ; counter = 0
lw r2, 0, counter
li r2, 1
sw r2, 0, counter  ; counter = 1
lw r2, 0, counter
ret
```

## 技术要点总结

### 关键数据结构
- **寄存器文件**: 16个32位寄存器，支持直接访问
- **调用栈**: StackFrame数组，支持函数嵌套（最大1024层）
- **常量池**: Object数组，存储函数、浮点数、字符串常量
- **活跃区间**: [start, end]范围，表示变量生命周期
- **分配映射**: 变量→寄存器/栈槽的双向映射
- **溢出槽位**: 变量溢出到栈时的位置跟踪

### 关键算法
- **线性扫描分配**: O(n log n)时间复杂度，n为变量数量
- **活跃区间计算**: O(n)时间复杂度，基于SSA形式
- **首次适应查找**: O(log n)时间复杂度，查找空闲块
- **溢出策略**: 最近失效区间溢出，最小化未来溢出

### 关键技术
- **32位大端序编码**: 高字节在前，低字节在后
- **RISC架构**: 简单指令格式，易于解码和执行
- **调用约定**: 清晰的参数传递和寄存器保存规则
- **栈帧布局**: 统一的函数调用约定

### 设计模式
- **策略模式**: 指令执行抽象，易于扩展新指令
- **适配器模式**: EP21→EP18R接口转换
- **工厂模式**: InstructionMapper动态创建指令执行器
- **分离关注点**: 编码、执行、内存管理独立

## 扩展阅读

### 推荐阅读材料
1. **《编译器设计与实现》** - 第11章：寄存器分配
2. **《计算机程序的构造和解释》** - 第8章：寄存器虚拟机
3. **Poletto & Sarkar (1999)** - Linear Scan Register Allocation**
4. **Wimmer & Mössenböck (2010)** - Linear Scan on SSA Form
5. **《LLVM参考手册》** - 寄存器分配算法章节
6. **《RISC-V特权架构规范》** - 调用约定和ABI设计

### 相关EP参考
- EP18: 栈式虚拟机（本章的直接前驱）
- EP19: 中间表示生成（活跃区间计算的基础）
- EP20: CFG和控制流分析（分配优化的基础）
- EP21: SSA形式和数据流分析（最优分配的前提）

## 常见问题与调试

### Q1: 寄存器不足溢出
**原因**: 寄存器数量少于活跃变量数量
**解决方案**:
1. 使用溢出处理机制将部分变量溢出到栈
2. 优化变量生命周期，缩短活跃区间
3. 实现编译器优化（常量传播、死代码消除）
4. 增加可用寄存器数量（RISC-V有32个寄存器）

### Q2: 调用约定违反
**原因**: 错误的参数传递或寄存器保存
**解决方案**:
1. 使用ABIValidator验证函数调用约定
2. 检查栈帧布局是否正确
3. 验证参数寄存器的使用
4. 测试嵌套调用的寄存器保存/恢复

### Q3: 活跃区间计算错误
**原因**: SSA形式构建错误或区间范围计算错误
**解决方案**:
1. 验证SSA形式的正确性
2. 检查支配边界和后支配边界
3. 使用单元测试验证活跃区间
4. 添加调试输出显示区间变化

### Q4: 溢出代码生成错误
**原因**: 溢出位置计算不正确或load/store位置错误
**解决方案**:
1. 验证溢出位置计算（StackOffsets类）
2. 检查load/store指令的相对偏移
3. 使用单元测试验证溢出代码
4. 生成并对比分配报告

### Q5: 无限循环检测失效
**原因**: 最大执行步数设置不当或循环检测逻辑错误
**解决方案**:
1. 合理设置maxExecutionSteps阈值
2. 在循环关键点添加检测逻辑
3. 添加统计信息，记录异常指令
4. 实现性能监控和报告

## 技术指标与性能考虑

### 时间复杂度
- 线性扫描分配：O(n log n)，n为变量数量
- 活跃区间计算：O(n)，n为指令数
- 指令执行循环：O(N)，N为指令数
- 寄存器分配：O(n log n)，n为变量数
- 函数调用开销：O(D)，D为调用深度

### 空间复杂度
- 寄存器文件：O(1)，16个寄存器
- 调用栈：O(D)，D为调用深度
- 活跃区间集合：O(n)，n为变量数
- 分配映射表：O(n)，n为变量数
- 溢出槽位映射：O(m)，m为溢出变量数

### 性能基准
- 小型程序（<100指令）：<1ms执行时间
- 中型程序（100-1000指令）：<10ms执行时间
- 大型程序（1000-10000指令）：<100ms执行时间
- 寄存器分配：O(n log n)时间复杂度
- 内存访问：<1ns每次（寄存器直接访问）

### 性能优势（相对于栈式虚拟机）
- 减少内存访问：直接寄存器操作
- 提高局部性：寄存器值缓存在CPU缓存中
- 更好的指令级并行：寄存器操作易于流水线化
- 更紧凑的代码：指令数量减少30-40%

## 关键文件清单

### 核心实现文件
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/interpreter/RegisterVMInterpreter.java` - 虚拟机解释器
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/registers/RegisterFile.java` - 寄存器文件
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/instructions/model/RegisterBytecodeDefinition.java` - 指令定义
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/LinearScanAllocator.java` - 线性扫描分配器
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/codegen/IRegisterAllocator.java` - 分配器接口
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/RegisterByteCodeAssembler.java` - 寄存器字节码汇编器
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/StackOffsets.java` - 栈帧偏移量定义

### ABI相关文件
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/ABIRegisters.java` - ABI寄存器枚举
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/callingconvention/ABIValidator.java` - ABI验证器
- `/ep18r/src/main/java/org/teachfx/antlr4/ep18r/stackvm/callingconvention/RegisterSaver.java` - 寄存器保存器

### 测试文件
- `/ep18r/src/test/java/org/teachfx/antlr4/ep18r/RegisterVMInterpreterUnitTest.java` - 虚拟机单元测试
- `/ep18r/src/test/java/org/teachfx/antlr4/ep18r/stackvm/codegen/LinearScanAllocatorTest.java` - 线性扫描分配测试
- `/ep18r/src/test/java/org/teachfx/antlr4/ep18r/abi/ABIComplianceTestSuite.java` - ABI一致性测试套件

## 代码示例

### 示例1：完整的寄存器虚拟机
```java
package org.teachfx.antlr4.ep18r.stackvm.interpreter;

public class RegisterVMInterpreter implements IVirtualMachine, IMemoryManager {
    private final VMConfig config;
    private final int[] registers = new int[16];
    private byte[] code;
    private int codeSize;
    private int[] heap;
    private int[] locals;
    private StackFrame[] callStack;
    private int framePointer = -1;
    private int programCounter;
    private boolean running;
    private int maxExecutionSteps;
    private int executionSteps = 0;
    private boolean didJump = false;
    
    private static final int SP = 13;
    private static final int FP = 14;
    private static final int LR = 15;
    
    public RegisterVMInterpreter(VMConfig config) {
        this.config = config;
        this.heap = new int[config.getHeapSize()];
        this.locals = new int[config.getLocalsSize()];
        this.callStack = new StackFrame[config.getMaxCallStackDepth()];
        this.maxExecutionSteps = config.getMaxExecutionSteps();
        this.programCounter = 0;
        this.running = false;
    }
    
    public void exec() throws Exception {
        // 初始化寄存器
        for (int i = 1; i < 16; i++) {
            registers[i] = 0;
        }
        
        // 设置初始栈帧
        StackFrame frame = new StackFrame(mainFunction, -1, 0);
        callStack[++framePointer] = frame;
        programCounter = mainFunction.address;
        running = true;
        
        cpu();
    }
    
    private void cpu() throws Exception {
        executionSteps = 0;
        
        while (running && programCounter < codeSize) {
            if (executionSteps++ > maxExecutionSteps) {
                throw new RuntimeException("Maximum execution steps exceeded at PC=" + programCounter);
            }
            
            int instructionWord = ((code[programCounter] & 0xFF) << 24) |
                                   ((code[programCounter + 1] & 0xFF) << 16) |
                                   ((code[programCounter + 2] & 0xFF) << 8) |
                                   (code[programCounter + 3] & 0xFF);
            
            int opcode = (instructionWord >> 26) & 0x3F;
            int operand = instructionWord;
            
            executeInstruction(opcode, operand);
            
            if (!didJump) {
                programCounter += 4;
            }
            didJump = false;
        }
    }
}
```

### 示例2：Linear Scan分配器实现
```java
package org.teachfx.antlr4.ep18r.stackvm.codegen;

import java.util.*;
import java.util.LinkedHashSet;

public class LinearScanAllocator implements IRegisterAllocator {
    // 寄存器常量
    private static final int NUM_REGISTERS = 16;
    private static final int R0 = 0;
    private static final int SP = 13;
    private static final int FP = 14;
    
    // 被调用者保存寄存器：ra (r1), a0-a5 (r2-r7), lr (r15)
    private static final int[] CALLER_SAVED_REGS = {1, 2, 3, 4, 5, 6, 7, 15};
    
    // 被调用者保存寄存器：s0-s4 (r8-r12)
    private static final int[] CALLEE_SAVED_REGS = {8, 9, 10, 11, 12};
    
    // 不可分配的寄存器：r0, sp, fp
    private static final int[] RESERVED_REGS = {0, 13, 14};
    
    // 实例字段
    private final int[] physicalRegs;
    private final Map<String, Integer> varToReg;
    private final Map<String, Integer> spillSlots;
    private final Map<Integer, String> regToVar;
    private int nextSpillSlot;
    private final boolean preferCalleeSaved;
    
    public LinearScanAllocator() {
        this(true);
    }
    
    public LinearScanAllocator(boolean preferCalleeSaved) {
        this.physicalRegs = new int[NUM_REGISTERS];
        this.varToReg = new HashMap<>();
        this.spillSlots = new HashMap<>();
        this.regToVar = new HashMap<>();
        this.nextSpillSlot = 0;
        this.preferCalleeSaved = preferCalleeSaved;
        reset();
    }
    
    @Override
    public int allocate(String varName) {
        if (varToReg.containsKey(varName)) {
            return varToReg.get(varName);
        }
        
        int reg = findAvailableRegister();
        if (reg == -1) {
            spillToStack(varName);
            reg = findAvailableRegister();
        }
        
        if (reg == -1) {
            spillToStack(varName);
            return -1;
        }
        
        physicalRegs[reg] = 1;
        varToReg.put(varName, reg);
        regToVar.put(reg, varName);
        return reg;
    }
    
    private int findAvailableRegister() {
        if (preferCalleeSaved) {
            for (int reg : CALLEE_SAVED_REGS) {
                if (physicalRegs[reg] == 0) {
                    return reg;
                }
            }
            for (int reg : CALLER_SAVED_REGS) {
                if (physicalRegs[reg] == 0) {
                    return reg;
                }
            }
        } else {
            for (int reg = 1; reg < NUM_REGISTERS; reg++) {
                if (!isReserved(reg) && physicalRegs[reg] == 0) {
                    return reg;
                }
            }
        }
        return -1;
    }
    
    @Override
    public void free(String varName) {
        Integer reg = varToReg.get(varName);
        if (reg != null && reg != -1) {
            physicalRegs[reg] = 0;
            varToReg.remove(varName);
            regToVar.remove(reg);
        }
        
        if (spillSlots.containsKey(varName)) {
            spillSlots.remove(varName);
        }
    }
    
    @Override
    public int spillToStack(String varName) {
        Integer reg = varToReg.get(varName);
        if (reg != null) {
            physicalRegs[reg] = 0;
            varToReg.remove(varName);
            regToVar.remove(reg);
        }
        
        int slotOffset = StackOffsets.FIRST_LOCAL_OFFSET - (nextSpillSlot * 4);
        spillSlots.put(varName, slotOffset);
        nextSpillSlot++;
        
        return slotOffset;
    }
    
    public String generateAllocationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Register Allocation Report ===\n");
        sb.append("Allocated variables: ").append(varToReg.size()).append("\n");
        sb.append("Spilled variables: ").append(spillSlots.size()).append("\n");
        sb.append("Available registers: ").append(getAvailableRegisterCount()).append("\n\n");
        
        if (!varToReg.isEmpty()) {
            sb.append("Variable to Register Mapping:\n");
            for (Map.Entry<String, Integer> entry : varToReg.entrySet()) {
                String regName = getRegisterABIName(entry.getValue());
                sb.append(String.format("  %-20s -> %2d (%-5s)\n",
                        entry.getKey(), entry.getValue(), regName));
            }
            sb.append("\n");
        }
        
        if (!spillSlots.isEmpty()) {
            sb.append("Spilled Variables:\n");
            for (Map.Entry<String, Integer> entry : spillSlots.entrySet()) {
                sb.append(String.format("  %-20s -> fp%+d\n",
                        entry.getKey(), entry.getValue()));
            }
            sb.append("\n");
        }
        
        sb.append("Register Status:\n");
        for (int i = 0; i < NUM_REGISTERS; i++) {
            String status = isReserved(i) ? "RESERVED" :
                           physicalRegs[i] == 0 ? "FREE" : "USED";
            String var = physicalRegs[i] != 0 ? regToVar.get(i) : "";
            String regName = getRegisterABIName(i);
            sb.append(String.format("  r%-2d (%-5s) [%-8s] %s\n",
                    i, regName, status, var));
        }
        
        return sb.toString();
    }
}
```

### 示例3：ABI验证器
```java
package org.teachfx.antlr4.ep18r.stackvm.callingconvention;

public class ABIValidator {
    public static String validateAbiRegisterUsage(int regNum, String regType) {
        switch (regType) {
            case "argument":
                return isArgumentRegister(regNum) ? "PASS" : "FAIL";
            case "returnValue":
                return isReturnValueRegister(regNum) ? "PASS" : "FAIL";
            case "callerSaved":
                return isCallerSaved(regNum) ? "PASS" : "FAIL";
            case "calleeSaved":
                return isCalleeSaved(regNum) ? "PASS" : "FAIL";
            case "reserved":
                return isReserved(regNum) ? "PASS" : "FAIL";
            default:
                return "UNKNOWN";
        }
    }
    
    public static boolean isArgumentRegister(int regNum) {
        return regNum >= 2 && regNum <= 7;
    }
    
    public static boolean isReturnValueRegister(int regNum) {
        return regNum == 2;  // a0
    }
    
    public static boolean isCallerSaved(int regNum) {
        return regNum == 1 || (regNum >= 2 && regNum <= 7) || regNum == 15;
    }
    
    public static boolean isCalleeSaved(int regNum) {
        return regNum >= 8 && regNum <= 12;
    }
    
    public static boolean isReserved(int regNum) {
        return regNum == 0 || regNum == 13 || regNum == 14;
    }
    
    public static String generateABIReport(String functionName, int numArgs, int numLocals,
                                    int callerSavedMask, int calleeSavedMask) {
        StringBuilder report = new StringBuilder();
        report.append("函数ABI使用报告：").append(functionName).append("\n");
        report.append("  参数数量：").append(numArgs).append("\n");
        report.append("  局部变量数量：").append(numLocals).append("\n");
        report.append("  调用者保存掩码：0x").append(String.format("%04X", callerSavedMask)).append("\n");
        report.append("  被调用者保存掩码：0x").append(String.format("%04X", calleeSavedMask)).append("\n\n");
        
        // 验证调用约定
        report.append("ABI验证：").append(validateCallingConvention(callerSavedMask, calleeSavedMask, numArgs, numLocals));
        
        return report.toString();
    }
}
```

## 测试验证

### 单元测试要点
1. **寄存器分配测试**
   - 测试所有寄存器的分配和释放
   - 测试溢出机制的正确性
   - 测试寄存器约束的遵守
   - 测试分配报告生成

2. **虚拟机核心功能测试**
   - 测试所有42条指令的正确执行
   - 测试寄存器操作的边界情况
   - 测试函数调用和返回机制
   - 测试内存访问指令

3. **ABI一致性测试**
   - 测试参数传递规则
   - 测试寄存器保存责任
   - 测试栈帧布局正确性
   - 测试递归调用的寄存器保存

4. **集成测试**
   - 从源代码到字节码到执行的完整流程
   - 复杂程序的性能测试
   - 与栈式虚拟机对比测试

### 集成测试
1. **EP21集成测试**
   - 测试EP21的RegisterVMGenerator使用EP18R的LinearScanAllocator
   - 测试跨模块的接口适配
   - 验证寄存器分配的兼容性

2. **性能对比测试**
   - 对比栈式虚拟机和寄存器虚拟机性能
   - 测试不同规模程序的执行时间
   - 测试内存访问频率和效率

### 压力测试
1. **大型程序测试**
   - 1000+指令的程序执行测试
   - 深度嵌套函数调用测试
   - 极限寄存器压力测试

2. **边界条件测试**
   - 最大调用栈深度测试
   - 最大寄存器分配压力测试
   - 内存耗尽测试
   - 无限循环检测测试

## 章节总结

本章将引导读者从理论到实践，完整掌握寄存器虚拟机和ABI设计。通过6个递进式练习，读者将能够：

1. **理论掌握**: 理解RISC架构和线性扫描寄存器分配算法
2. **技术实现**: 实现完整的寄存器虚拟机和自动寄存器分配器
3. **实践应用**: 解决实际编译器后端的寄存器分配优化问题
4. **工程能力**: 构建高性能、遵循ABI规范的虚拟机执行平台

学习完本章后，读者将具备设计和实现现代寄存器虚拟机的能力，为后续优化章节（SSA、数据流分析）打下坚实基础。
