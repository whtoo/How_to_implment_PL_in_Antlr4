# 第10章提示：调用图分析与可视化

## 章节概要

**章节标题**: 调用图分析与可视化

**所属模块**: 模块3 (EP17-EP18R)

**目标读者**: 已实现过编译器的工程师，学习高级编译器技术

**先修知识**: 
- 图数据结构基础（节点、边、图遍历）
- 图算法基础（DFS、BFS、拓扑排序）
- AST遍历模式（访问者模式）
- 编译器前端基础知识（词法分析、语法分析）

**EP范围**: EP17（调用图分析、DOT可视化）

**前导章节**: 第9章

**后续章节**: 第11章

## 学习目标

1. **理解调用图概念和用途**
   - 掌握调用图的数学定义和语义表示
   - 理解调用图在编译器优化中的应用价值
   - 识别不同类型的调用关系（直接调用、间接调用、递归调用）

2. **掌握Graphviz可视化技术**
   - 学习DOT文件格式规范和语法
   - 掌握Graphviz布局引擎和可视化配置
   - 实现美观的调用图渲染和输出

3. **生成函数调用关系分析报告**
   - 设计并实现调用图数据结构（节点、边、属性）
   - 集成ANTLR4访问者模式收集调用关系
   - 实现自动化报告生成和可视化输出

## 核心内容框架

### 第1节：调用图基础理论

**1.1 调用图数学定义**
- 有向图表示：G = (V, E)，V为函数节点集合，E为调用边集合
- 边的语义：(f1, f2) ∈ E 表示函数f1调用函数f2
- 节点属性：函数名、函数类型（用户定义/内置）、参数签名

**1.2 调用图类型和特征**
- 直接调用图：仅包含函数直接调用的关系
- 调用图：包含直接和间接调用关系
- 静态调用图：基于静态分析，不考虑运行时多态
- 动态调用图：基于实际执行轨迹
- 递归检测：识别自递归和相互递归

**1.3 调用图在编译器中的价值**
- 内联优化决策：识别频繁调用的函数
- 死代码消除：检测不可达函数
- 调用栈分析：分析调用深度和复杂度
- 程序理解：可视化模块依赖关系
- 测试覆盖率分析：识别未调用函数

### 第2节：调用图数据结构与算法

**2.1 图数据结构设计**
```java
// 核心图数据结构
public class Graph {
    // 节点集合（有序集合，保证输出稳定性）
    public Set<String> nodes = new OrderedHashSet<String>();
    
    // 边集合（多重映射，支持一对多关系）
    public MultiMap<String, String> edges = new MultiMap<>();
    
    // 添加边
    public void edge(String source, String target) {
        edges.map(source, target);
    }
    
    // DOT格式输出
    public String toDOT() {
        // 生成Graphviz兼容的DOT格式
    }
}
```

**2.2 调用图构建算法**
- AST遍历算法：使用ANTLR4访问者模式遍历语法树
- 节点收集：识别所有函数定义（FunctionDecl）
- 边收集：识别所有函数调用（ExprFuncCall）
- 上下文管理：跟踪当前分析函数（currentFunctionName）

**2.3 访问者模式集成**
```java
public class CallGraphVisitor extends CymbolBaseVisitor<Object> {
    private Graph callGraph;
    private String currentFunctionName = null;
    
    // 访问函数定义，注册节点
    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        currentFunctionName = ctx.ID().getText();
        callGraph.nodes.add(currentFunctionName);
        super.visitFunctionDecl(ctx);
        currentFunctionName = null;
        return null;
    }
    
    // 访问函数调用，建立边
    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        if (currentFunctionName != null) {
            String funcName = ctx.ID().getText();
            callGraph.edge(currentFunctionName, funcName);
        }
        return null;
    }
}
```

### 第3节：DOT格式与Graphviz可视化

**3.1 DOT文件格式规范**
- 图声明：`digraph G { ... }`
- 节点定义：`nodeName [attributes];`
- 边定义：`source -> target [attributes];`
- 注释：支持`//`或`/* */`注释

**3.2 图属性与布局引擎**
- 布局引擎选择：
  - `dot`: 层次化有向图（最适合调用图）
  - `neato`: 弹簧模型
  - `fdp`: 力导向布局
- 方向控制：`rankdir=LR`（从左到右）、`rankdir=TB`（从上到下）
- 节点间距：`ranksep=.25`（控制节点间距）

**3.3 节点和边样式**
- 节点属性：
  ```
  node [shape=circle, fontname="ArialNarrow", 
        fontsize=12, fixedsize=true, height=.45];
  ```
- 边属性：
  ```
  edge [arrowsize=.5, arrowhead=vee];
  ```
- 颜色和权重：支持基于调用频率的视觉强调

**3.4 StringTemplate集成**
```java
// 使用StringTemplate 4.0.8生成DOT
public ST toST() {
    ST st = new ST(
        "digraph G {\n" +
        "  ranksep=.25; \n" +
        "  edge [arrowsize=.5]\n" +
        "  node [shape=circle, fontname=\"ArialNarrow\",\n" +
        "        fontsize=12, fixedsize=true, height=.45];\n" +
        "  <funcs:{f | <f>; }>\n" +
        "  <edgePairs:{edge| <edge.a> -> <edge.b>;}; separator=\"\\n\">\n" +
        "}\n"
    );
    st.add("edgePairs", edges.getPairs());
    st.add("funcs", nodes);
    return st;
}
```

### 第4节：编译器集成与实现

**4.1 编译器流水线集成**
```java
public class Compiler {
    public static void main(String[] args) throws Exception {
        // 1. 词法分析
        CharStream charStream = CharStreams.fromStream(inputStream);
        CymbolLexer lexer = new CymbolLexer(charStream);
        
        // 2. 语法分析
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree tree = parser.file();
        
        // 3. 调用图分析
        CallGraphVisitor collector = new CallGraphVisitor();
        collector.visit(tree);
        
        // 4. DOT文件生成
        OutputStream outputStream = new FileOutputStream("call.dot");
        outputStream.write(collector.callGraph.toDOT().getBytes());
        outputStream.close();
    }
}
```

**4.2 编译命令与工作流**
```bash
# 构建项目
cd ep17
mvn clean compile

# 运行调用图生成器
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep17.Compiler"

# 查看生成的DOT文件
cat src/main/resources/call.dot

# 使用Graphviz渲染为PNG
dot -Tpng src/main/resources/call.dot -o call_graph.png

# 使用不同布局引擎
dot -Tpng -Kneato src/main/resources/call.dot -o call_graph_neato.png
```

### 第5节：高级分析与优化

**5.1 调用频率统计**
- 追踪每条边的调用次数
- 识别热点函数（高频调用目标）
- 生成调用频率报告

**5.2 循环调用检测**
- 自递归检测：`f -> f`
- 相互递归检测：`f1 -> f2 -> ... -> f1`
- 使用DFS或Warshall算法检测强连通分量

**5.3 可达性分析**
- 从main函数开始的可达函数集合
- 死代码识别（不可达函数）
- 调用链分析（最长调用路径）

**5.4 可视化增强**
- 子图聚类：按模块分组函数
- 颜色编码：基于函数类型或调用频率着色
- 交互式可视化：集成SVG/HTML输出支持

## 实践练习

### 练习1：基础调用图构建
**目标**: 实现简单的调用图收集器
**任务**:
1. 创建`Graph`类，包含节点和边的集合
2. 实现`CallGraphVisitor`，继承ANTLR4的`CymbolBaseVisitor`
3. 在`visitFunctionDecl`中注册函数节点
4. 在`visitExprFuncCall`中建立调用边
5. 实现`toDOT()`方法，输出标准DOT格式

**测试用例**:
```cymbol
int add(int a, int b) {
    return a + b;
}

int multiply(int x, int y) {
    return x * y;
}

int calculate(int a, int b) {
    int sum = add(a, b);
    int product = multiply(a, b);
    return sum + product;
}

void main() {
    int result = calculate(5, 7);
    print(result);
}
```

**预期输出**:
```
digraph G {
    main -> calculate
    calculate -> add
    calculate -> multiply
}
```

### 练习2：递归调用检测
**目标**: 识别和处理递归调用
**任务**:
1. 在`CallGraphVisitor`中添加递归检测逻辑
2. 检测自递归：`func -> func`
3. 检测相互递归：`func1 -> func2 -> ... -> func1`
4. 在DOT输出中用不同颜色标记递归边
5. 生成递归函数报告

**测试用例**:
```cymbol
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

int fibonacci(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

void main() {
    print(factorial(5));
    print(fibonacci(6));
}
```

**预期输出**:
```
digraph G {
    factorial -> factorial [color=red, label="自递归"];
    fibonacci -> fibonacci [color=red, label="自递归"];
    main -> factorial;
    main -> fibonacci;
}
```

### 练习3：调用频率统计
**目标**: 统计函数调用频率
**任务**:
1. 扩展`Graph`类，添加调用次数计数器
2. 修改`edge()`方法，记录边的调用频率
3. 实现`generateCallFrequencyReport()`方法
4. 在DOT输出中基于频率设置边权重
5. 生成文本报告，列出热点函数

**测试用例**:
```cymbol
int loopFunction(int n) {
    int sum = 0;
    int i = 0;
    while (i < n) {
        sum = sum + i;
        i = i + 1;
    }
    return sum;
}

void main() {
    int result1 = loopFunction(10);
    int result2 = loopFunction(20);
    print(result1 + result2);
}
```

**预期输出**:
```
调用频率报告:
- main 调用 2 次其他函数
- loopFunction 被调用 2 次
- print 被调用 2 次

DOT输出（带权重）:
main -> loopFunction [weight=2];
loopFunction -> main [weight=2];
```

### 练习4：死代码检测
**目标**: 识别不可达函数
**任务**:
1. 从main函数开始，执行图遍历（DFS/BFS）
2. 标记所有可达函数
3. 识别未标记的函数（死代码）
4. 生成死代码报告
5. 在可视化中用不同颜色标记死代码节点

**测试用例**:
```cymbol
void deadFunction() {
    print("This is never called");
}

void aliveFunction() {
    print("This is called");
}

void main() {
    aliveFunction();
}
```

**预期输出**:
```
死代码报告:
- deadFunction: 不可达函数

DOT输出:
digraph G {
    main -> aliveFunction;
    deadFunction [color=gray, style=dashed, label="死代码"];
}
```

### 练习5：模块化可视化
**目标**: 按功能模块分组函数
**任务**:
1. 定义函数命名约定（如`module_function`）
2. 解析函数名，识别模块归属
3. 使用Graphviz的`subgraph`功能创建模块集群
4. 为不同模块使用不同颜色
5. 生成模块依赖图

**测试用例**:
```cymbol
int math_add(int a, int b) { return a + b; }
int math_multiply(int x, int y) { return x * y; }
int io_print(int value) { print(value); }
int main() {
    int sum = math_add(10, 20);
    int product = math_multiply(5, 6);
    io_print(sum);
    io_print(product);
}
```

**预期输出**:
```
digraph G {
    subgraph cluster_math {
        label="Math Module";
        color=lightblue;
        math_add; math_multiply;
    }
    
    subgraph cluster_io {
        label="I/O Module";
        color=lightgreen;
        io_print;
    }
    
    main -> math_add;
    main -> math_multiply;
    math_add -> io_print;
    math_multiply -> io_print;
}
```

## 技术要点总结

### 关键数据结构
- **OrderedHashSet**: ANTLR4提供的有序集合，保证节点输出稳定性
- **MultiMap<String, String>**: 支持一对多关系的多重映射
- **StringTemplate**: 模板引擎，用于代码/文本生成

### 关键算法
- **AST遍历**: 使用ANTLR4访问者模式，O(N)时间复杂度
- **边收集**: 在遍历过程中动态建立调用关系
- **图遍历**: DFS/BFS用于可达性分析
- **递归检测**: 深度优先搜索+路径跟踪

### 关键技术
- **ANTLR4 4.13.2**: 词法/语法分析器生成
- **Graphviz**: DOT文件渲染工具，支持多种输出格式
- **访问者模式**: 分离遍历逻辑和业务逻辑
- **模板化输出**: 使用StringTemplate提高可维护性

### 设计模式
- **访问者模式**: 遍历AST结构
- **策略模式**: 不同分析策略（静态/动态/频率）
- **生成器模式**: 生成不同格式输出（DOT/JSON/文本）

## 扩展阅读

### 推荐阅读材料
1. **Graphviz官方文档**
   - Graphviz DOT语言参考
   - 布局引擎对比
   - 节点和边属性手册

2. **编译器理论基础**
   - 《编译原理》- 调用图章节
   - 《龙书》(Dragon Book) - 数据流分析
   - SSA相关论文

3. **ANTLR4进阶使用**
   - ANTLR4权威指南
   - 访问者模式深入理解
   - 语法树遍历优化

### 相关EP参考
- EP15: 类型检查和符号表
- EP16: 三地址码生成
- EP18: 虚拟机实现（调用图的下游）

## 常见问题与调试

### Q1: 调用图不包含某些函数
**原因**: 函数未被调用或分析逻辑遗漏
**解决方案**:
1. 检查`visitFunctionDecl`是否正确注册所有函数
2. 检查AST遍历是否完整访问所有节点
3. 添加调试日志，跟踪节点注册过程
4. 验证语法解析是否正确识别函数定义

### Q2: DOT文件渲染不正确
**原因**: DOT格式错误或Graphviz配置问题
**解决方案**:
1. 验证DOT语法（括号匹配、分号）
2. 检查节点和边名称是否有特殊字符
3. 尝试不同的布局引擎（dot/neato/fdp）
4. 使用`dot -v`查看详细错误信息

### Q3: 递归检测不准确
**原因**: 递归检测算法实现错误
**解决方案**:
1. 使用标准算法（DFS/DFS+着色）
2. 区分直接递归和间接递归
3. 添加单元测试验证递归检测
4. 对复杂递归情况进行人工验证

### Q4: 性能问题（大程序分析缓慢）
**原因**: 图数据结构或遍历算法效率低
**解决方案**:
1. 使用高效的数据结构（OrderedHashSet vs HashSet）
2. 避免不必要的拷贝和遍历
3. 实现增量更新（仅更新变化部分）
4. 考虑并行化大型图分析

## 技术指标与性能考虑

### 时间复杂度
- AST遍历：O(N)，N为AST节点数
- 边收集：O(E)，E为函数调用数
- DOT生成：O(V+E)
- 递归检测：O(V+E)，使用DFS

### 空间复杂度
- 图数据结构：O(V+E)
- AST遍历栈：O(H)，H为AST高度
- DOT输出缓冲区：O(V+E)

### 性能基准
- 小型程序（<100行）：瞬时完成
- 中型程序（100-1000行）：<1秒
- 大型程序（1000-10000行）：几秒到几十秒

## 关键文件清单

### 核心实现文件
- `/ep17/src/main/java/org/teachfx/antlr4/ep17/visitor/CallGraphVisitor.java` - 调用图访问者
- `/ep17/src/main/java/org/teachfx/antlr4/ep17/misc/Graph.java` - 图数据结构与DOT生成
- `/ep17/src/main/java/org/teachfx/antlr4/ep17/Compiler.java` - 编译器主入口

### 辅助文件
- `/ep17/src/main/java/org/teachfx/antlr4/ep17/parser/CymbolParser.java` - ANTLR4生成的语法分析器
- `/ep17/src/main/java/org/teachfx/antlr4/ep17/parser/CymbolBaseVisitor.java` - 访问者基类

### 测试与示例
- `/ep17/src/main/resources/t.cymbol` - 示例输入文件
- `/ep17/src/main/resources/call.dot` - 生成的DOT文件（输出）

### 构建配置
- `/ep17/pom.xml` - Maven构建配置
- `/ep17/README.md` - 模块功能文档
- `/ep17/TODO.md` - 开发任务跟踪

## 代码示例

### 示例1：完整的调用图实现
```java
package org.teachfx.antlr4.ep17.visitor;

import org.teachfx.antlr4.ep17.misc.Graph;
import org.teachfx.antlr4.ep17.parser.CymbolBaseVisitor;
import org.teachfx.antlr4.ep17.parser.CymbolParser.ExprFuncCallContext;
import org.teachfx.antlr4.ep17.parser.CymbolParser.FunctionDeclContext;

public class CallGraphVisitor extends CymbolBaseVisitor<Object> {
    public Graph callGraph;
    private String currentFunctionName = null;
    private int callFrequency = 0;

    public CallGraphVisitor() {
        super();
        this.callGraph = new Graph();
        // 内置print函数
        callGraph.nodes.add("print");
    }

    @Override
    public Object visitExprFuncCall(ExprFuncCallContext ctx) {
        super.visitExprFuncCall(ctx);
        if (currentFunctionName != null) {
            String funcName = ctx.ID().getText();
            callGraph.edge(currentFunctionName, funcName);
            callFrequency++;
        }
        return null;
    }

    @Override
    public Object visitFunctionDecl(FunctionDeclContext ctx) {
        currentFunctionName = ctx.ID().getText();
        callGraph.nodes.add(currentFunctionName);
        super.visitFunctionDecl(ctx);
        currentFunctionName = null;
        return null;
    }
}
```

### 示例2：增强的DOT生成（带样式）
```java
package org.teachfx.antlr4.ep17.misc;

import org.antlr.v4.runtime.misc.MultiMap;
import org.antlr.v4.runtime.misc.OrderedHashSet;

public class Graph {
    public Set<String> nodes = new OrderedHashSet<String>();
    public MultiMap<String, String> edges = new MultiMap<>();

    public void edge(String source, String target) {
        edges.map(source, target);
    }

    public String toDOT() {
        StringBuilder buf = new StringBuilder();
        buf.append("digraph CallGraph {\n");
        buf.append("  rankdir=LR;\n");
        buf.append("  node [shape=circle, fontname=\"ArialNarrow\",\n");
        buf.append("        fontsize=12, fixedsize=true, height=.45];\n");
        
        // 输出所有节点
        for (String node : nodes) {
            buf.append("  ");
            buf.append(node);
            buf.append(";\n");
        }
        
        // 输出所有边
        for (String src : edges.keySet()) {
            for (String trg : edges.get(src)) {
                buf.append("  ");
                buf.append(src);
                buf.append(" -> ");
                buf.append(trg);
                buf.append(";\n");
            }
        }
        
        buf.append("}\n");
        return buf.toString();
    }
}
```

### 示例3：编译器集成
```java
package org.teachfx.antlr4.ep17;

import org.teachfx.antlr4.ep17.visitor.CallGraphVisitor;
import org.antlr.v4.runtime.CharStreams;
import org.teachfx.antlr4.ep17.parser.CymbolLexer;
import org.teachfx.antlr4.ep17.parser.CymbolParser;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Compiler <input-file>");
            System.exit(1);
        }

        String inputFile = args[0];
        InputStream inputStream = new FileInputStream(inputFile);
        OutputStream outputStream = new FileOutputStream("call.dot");

        // 1. 词法分析
        CharStream charStream = CharStreams.fromStream(inputStream);
        CymbolLexer lexer = new CymbolLexer(charStream);
        
        // 2. 语法分析
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        
        // 3. 构建AST
        var tree = parser.file();
        
        // 4. 调用图分析
        CallGraphVisitor collector = new CallGraphVisitor();
        collector.visit(tree);
        
        // 5. DOT文件输出
        outputStream.write(collector.callGraph.toDOT().getBytes());
        outputStream.close();
        
        System.out.println("Call graph generated: call.dot");
        System.out.println("Use: dot -Tpng call.dot -o call_graph.png");
    }
}
```

## 测试验证

### 单元测试要点
1. **CallGraphVisitor测试**
   - 测试单个函数的节点注册
   - 测试函数调用的边建立
   - 测试嵌套调用的正确处理
   - 测试递归调用的情况

2. **Graph类测试**
   - 测试节点的添加和检索
   - 测试边的添加和多重映射
   - 测试DOT格式的正确性
   - 测试空图的边界情况

3. **集成测试**
   - 测试完整编译流程
   - 测试不同规模程序的调用图生成
   - 测试错误处理（语法错误、未定义函数）

### 验证命令
```bash
# 编译测试
mvn test -Dtest=CallGraphVisitorTest

# 运行示例程序
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep17.Compiler" \
    -Dexec.args="src/main/resources/t.cymbol"

# 验证DOT输出
cat call.dot | head -20

# 渲染调用图
dot -Tpng call.dot -o call_graph.png

# 验证PNG文件
file call_graph.png
```

## 章节总结

本章将引导读者从理论到实践，完整掌握调用图分析与可视化技术。通过5个递进式练习，读者将能够：

1. **理论掌握**: 理解调用图的数学定义和应用价值
2. **技术实现**: 实现完整的调用图收集和可视化系统
3. **实践应用**: 解决实际编译器优化问题（死代码消除、内联决策）
4. **工程能力**: 集成ANTLR4、Graphviz等工具构建自动化分析流水线

学习完本章后，读者将具备分析任意程序调用关系的能力，为后续编译器优化章节打下坚实基础。
