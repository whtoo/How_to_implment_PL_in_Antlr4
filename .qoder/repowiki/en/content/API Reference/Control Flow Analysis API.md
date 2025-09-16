# Control Flow Analysis API

<cite>
**Referenced Files in This Document**   
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java)
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java)
- [LinearIRBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LinearIRBlock.java)
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Control Flow Graph Construction](#control-flow-graph-construction)
3. [CFG API for Traversal and Analysis](#cfg-api-for-traversal-and-analysis)
4. [Liveness Analysis Implementation](#liveness-analysis-implementation)
5. [Compiler Optimizations Enabled by CFG](#compiler-optimizations-enabled-by-cfg)
6. [Programmatic Usage Examples](#programmatic-usage-examples)

## Introduction
The Control Flow Analysis API provides a comprehensive framework for constructing and analyzing control flow graphs (CFGs) from intermediate representation (IR) code. This system enables sophisticated compiler optimizations through data flow analysis, with a focus on liveness analysis to identify variables that are live at each program point. The API is designed to transform linear IR code into structured control flow graphs composed of basic blocks, facilitating advanced analysis and optimization passes.

**Section sources**
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java#L9-L62)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)

## Control Flow Graph Construction
The CFG construction process begins with linear IR blocks that are transformed into basic blocks and connected through control flow edges. The `CFGBuilder` class analyzes `LinearIRBlock` instances to create a structured control flow graph.

```mermaid
classDiagram
class CFGBuilder {
+CFG<IRNode> cfg
+List<BasicBlock<IRNode>> basicBlocks
+List<Triple<Integer,Integer,Integer>> edges
+CFGBuilder(LinearIRBlock startBlock)
+CFG<IRNode> getCFG()
}
class LinearIRBlock {
+int ord
+List<IRNode> stmts
+List<LinearIRBlock> successors
+List<LinearIRBlock> predecessors
+Kind kind
+addStmt(IRNode stmt)
+getStmts() List<IRNode>
+getSuccessors() List<LinearIRBlock>
+getPredecessors() List<LinearIRBlock>
+getKind() Kind
}
class BasicBlock {
+int id
+List<Loc<IRNode>> codes
+Kind kind
+Set<Operand> def
+Set<Operand> liveUse
+Set<Operand> liveIn
+Set<Operand> liveOut
+Label label
+BasicBlock(Kind, List<Loc<IRNode>>, Label, int)
+static BasicBlock<IRNode> buildFromLinearBlock(LinearIRBlock, List<BasicBlock<IRNode>>)
+getBlock(int id) BasicBlock<I>
+getFrontier(int id) Set<Integer>
+getSucceed(int id) Set<Integer>
}
class CFG {
+List<BasicBlock<IRNode>> nodes
+List<Triple<Integer,Integer,Integer>> edges
+List<Pair<Set<Integer>,Set<Integer>>> links
+List<IFlowOptimizer<IRNode>> optimizers
+CFG(List<BasicBlock<I>>, List<Triple<Integer,Integer,Integer>>)
+getBlock(int id) BasicBlock<I>
+getFrontier(int id) Set<Integer>
+getSucceed(int id) Set<Integer>
+getInDegree(int id) int
+getOutDegree(int id) int
+addOptimizer(IFlowOptimizer<I>)
+applyOptimizers()
+getIRNodes() List<I>
}
CFGBuilder --> CFG : "creates"
CFGBuilder --> LinearIRBlock : "analyzes"
CFGBuilder --> BasicBlock : "creates"
LinearIRBlock --> BasicBlock : "converted to"
CFG --> BasicBlock : "contains"
CFG --> IFlowOptimizer : "applies"
```

**Diagram sources**
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java#L9-L62)
- [LinearIRBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LinearIRBlock.java#L13-L236)
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java#L15-L130)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)

**Section sources**
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java#L9-L62)
- [LinearIRBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LinearIRBlock.java#L13-L236)

## CFG API for Traversal and Analysis
The CFG class provides a comprehensive API for traversing control flow paths and performing data flow analysis. The graph structure enables bidirectional navigation through predecessor and successor relationships, with methods to query node connectivity and perform graph-wide operations.

```mermaid
flowchart TD
Start["CFG Traversal Start"] --> GetBlock["getBlock(id)"]
GetBlock --> InDegree["getInDegree(id)"]
GetBlock --> OutDegree["getOutDegree(id)"]
GetBlock --> Frontier["getFrontier(id)"]
GetBlock --> Succeed["getSucceed(id)"]
GetBlock --> InEdges["getInEdges(key)"]
Frontier --> Predecessors["Returns Set<Integer> of predecessor block IDs"]
Succeed --> Successors["Returns Set<Integer> of successor block IDs"]
InDegree --> Count["Returns integer count of incoming edges"]
OutDegree --> Count["Returns integer count of outgoing edges"]
InEdges --> Stream["Returns Stream<Triple> of incoming edges"]
CFG --> Iterator["Implements Iterable<BasicBlock<I>>"]
Iterator --> ForEach["Enables for-each iteration over all blocks"]
CFG --> Optimizers["Contains List<IFlowOptimizer<I>>"]
Optimizers --> Add["addOptimizer(IFlowOptimizer<I>)"]
Optimizers --> Apply["applyOptimizers()"]
Add --> Register["Registers optimizer for later application"]
Apply --> Execute["Executes all registered optimizers on CFG"]
CFG --> IRNodes["getIRNodes()"]
IRNodes --> FlatList["Returns flattened List<I> of all IR nodes in CFG"]
style Start fill:#4CAF50,stroke:#388E3C
style GetBlock fill:#2196F3,stroke:#1976D2
style InDegree fill:#2196F3,stroke:#1976D2
style OutDegree fill:#2196F3,stroke:#1976D2
style Frontier fill:#2196F3,stroke:#1976D2
style Succeed fill:#2196F3,stroke:#1976D2
style InEdges fill:#2196F3,stroke:#1976D2
style Predecessors fill:#FFC107,stroke:#FFA000
style Successors fill:#FFC107,stroke:#FFA000
style Count fill:#FFC107,stroke:#FFA000
style Stream fill:#FFC107,stroke:#FFA000
style Iterator fill:#9C27B0,stroke:#7B1FA2
style ForEach fill:#FFC107,stroke:#FFA000
style Optimizers fill:#F44336,stroke:#D32F2F
style Add fill:#2196F3,stroke:#1976D2
style Apply fill:#2196F3,stroke:#1976D2
style Register fill:#FFC107,stroke:#FFA000
style Execute fill:#FFC107,stroke:#FFA000
style IRNodes fill:#2196F3,stroke:#1976D2
style FlatList fill:#FFC107,stroke:#FFA000
```

**Diagram sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)

**Section sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)

## Liveness Analysis Implementation
The liveness analysis implementation uses the visitor pattern to traverse IR nodes and identify variables that are live at each program point. This data flow analysis determines which variables are used before being redefined in subsequent code paths.

```mermaid
sequenceDiagram
participant LA as "LivenessAnalysis"
participant CFG as "Control Flow Graph"
participant BB as "BasicBlock"
participant Stmt as "Statement"
participant Expr as "Expression"
LA->>CFG : Initialize analysis
CFG->>BB : Iterate through all basic blocks
BB->>Stmt : Process statements in reverse order
loop For each statement
Stmt->>LA : Accept visitor
alt Assignment Statement
LA->>Expr : Visit right-hand side expression
LA-->>LA : Collect use set from RHS
LA->>LA : Add LHS to def set
LA-->>Stmt : Set use and def information
else Conditional Jump
LA->>Expr : Visit condition expression
LA-->>LA : Collect use set from condition
LA-->>Stmt : Set use information
else Return Statement
LA->>Expr : Visit return value expression
LA-->>LA : Collect use set from return value
LA-->>Stmt : Set use information
else Other Statements
LA-->>Stmt : Reset use/def sets
end
Stmt->>BB : Store liveness information
end
BB->>CFG : Update block liveness sets
CFG->>LA : Perform iterative data flow analysis
LA->>LA : Compute liveIn and liveOut sets
LA-->>CFG : Complete liveness analysis
Note over LA,CFG : Liveness analysis identifies variables<br/>that are live at each program point<br/>for optimization purposes
```

**Diagram sources**
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java#L16-L146)

**Section sources**
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java#L16-L146)

## Compiler Optimizations Enabled by CFG
The CFG structure enables various compiler optimizations through the `IFlowOptimizer` interface, which allows registration and application of optimization passes on the control flow graph.

```mermaid
graph TD
subgraph "Optimization Pipeline"
A["CFG Structure"] --> B["Optimization Opportunities"]
B --> C["Dead Code Elimination"]
B --> D["Jump Optimization"]
B --> E["Redundant Code Removal"]
B --> F["Constant Propagation"]
B --> G["Loop Optimization"]
end
subgraph "Optimization Mechanism"
H["IFlowOptimizer Interface"] --> I["onHandle(CFG<I> cfg)"]
I --> J["Register Optimizers"]
J --> K["CFG.addOptimizer()"]
K --> L["Apply Optimizations"]
L --> M["CFG.applyOptimizers()"]
M --> N["Iterate through all registered optimizers"]
N --> O["Execute optimization on CFG"]
end
subgraph "Optimization Targets"
P["BasicBlock"] --> Q["Remove unreachable blocks"]
P --> R["Merge adjacent blocks"]
P --> S["Eliminate empty blocks"]
Q --> C
R --> D
S --> E
T["Control Flow Edges"] --> U["Remove redundant jumps"]
T --> V["Optimize conditional jumps"]
U --> D
V --> D
W["IR Instructions"] --> X["Remove unused assignments"]
W --> Y["Propagate constants"]
W --> Z["Eliminate redundant operations"]
X --> C
Y --> F
Z --> E
end
A --> H
H --> P
H --> T
H --> W
style A fill:#4CAF50,stroke:#388E3C
style B fill:#2196F3,stroke:#1976D2
style C fill:#FFC107,stroke:#FFA000
style D fill:#FFC107,stroke:#FFA000
style E fill:#FFC107,stroke:#FFA000
style F fill:#FFC107,stroke:#FFA000
style G fill:#FFC107,stroke:#FFA000
style H fill:#9C27B0,stroke:#7B1FA2
style I fill:#2196F3,stroke:#1976D2
style J fill:#2196F3,stroke:#1976D2
style K fill:#2196F3,stroke:#1976D2
style L fill:#2196F3,stroke:#1976D2
style M fill:#2196F3,stroke:#1976D2
style N fill:#2196F3,stroke:#1976D2
style O fill:#2196F3,stroke:#1976D2
style P fill:#F44336,stroke:#D32F2F
style Q fill:#FFC107,stroke:#FFA000
style R fill:#FFC107,stroke:#FFA000
style S fill:#FFC107,stroke:#FFA000
style T fill:#F44336,stroke:#D32F2F
style U fill:#FFC107,stroke:#FFA000
style V fill:#FFC107,stroke:#FFA000
style W fill:#F44336,stroke:#D32F2F
style X fill:#FFC107,stroke:#FFA000
style Y fill:#FFC107,stroke:#FFA000
style Z fill:#FFC107,stroke:#FFA000
```

**Diagram sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java#L4-L6)

**Section sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java#L4-L6)

## Programmatic Usage Examples
The Control Flow Analysis API can be used programmatically to perform optimization and analysis tasks on the intermediate representation code.

```mermaid
flowchart TD
A["Create CFGBuilder with start block"] --> B["Build CFG from LinearIRBlock"]
B --> C["Obtain CFG instance"]
C --> D["Register Optimizers"]
D --> E["Add IFlowOptimizer implementations"]
E --> F["Apply all optimizations"]
F --> G["Perform Data Flow Analysis"]
G --> H["Execute LivenessAnalysis"]
H --> I["Access liveness information"]
I --> J["Use results for optimization"]
K["CFG Traversal"] --> L["Iterate through all blocks"]
L --> M["For each block, get predecessors"]
M --> N["For each block, get successors"]
N --> O["Analyze control flow paths"]
O --> P["Identify unreachable code"]
P --> Q["Optimize branch prediction"]
R["Basic Block Analysis"] --> S["Access block instructions"]
S --> T["Analyze instruction sequence"]
T --> U["Identify optimization opportunities"]
U --> V["Modify instruction stream"]
V --> W["Update CFG structure"]
X["Liveness Analysis"] --> Y["Visit each IR node"]
Y --> Z["Track use and def sets"]
Z --> AA["Compute liveIn and liveOut sets"]
AA --> AB["Apply liveness information"]
AB --> AC["Register allocation"]
AC --> AD["Dead code elimination"]
style A fill:#4CAF50,stroke:#388E3C
style B fill:#2196F3,stroke:#1976D2
style C fill:#2196F3,stroke:#1976D2
style D fill:#2196F3,stroke:#1976D2
style E fill:#2196F3,stroke:#1976D2
style F fill:#2196F3,stroke:#1976D2
style G fill:#2196F3,stroke:#1976D2
style H fill:#2196F3,stroke:#1976D2
style I fill:#2196F3,stroke:#1976D2
style J fill:#FFC107,stroke:#FFA000
style K fill:#4CAF50,stroke:#388E3C
style L fill:#2196F3,stroke:#1976D2
style M fill:#2196F3,stroke:#1976D2
style N fill:#2196F3,stroke:#1976D2
style O fill:#2196F3,stroke:#1976D2
style P fill:#FFC107,stroke:#FFA000
style Q fill:#FFC107,stroke:#FFA000
style R fill:#4CAF50,stroke:#388E3C
style S fill:#2196F3,stroke:#1976D2
style T fill:#2196F3,stroke:#1976D2
style U fill:#2196F3,stroke:#1976D2
style V fill:#2196F3,stroke:#1976D2
style W fill:#FFC107,stroke:#FFA000
style X fill:#4CAF50,stroke:#388E3C
style Y fill:#2196F3,stroke:#1976D2
style Z fill:#2196F3,stroke:#1976D2
style AA fill:#2196F3,stroke:#1976D2
style AB fill:#2196F3,stroke:#1976D2
style AC fill:#FFC107,stroke:#FFA000
style AD fill:#FFC107,stroke:#FFA000
```

**Diagram sources**
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java#L9-L62)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java#L16-L146)

**Section sources**
- [CFGBuilder.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java#L9-L62)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java#L17-L157)
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java#L16-L146)