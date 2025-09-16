# Custom Optimization Passes with IFlowOptimizer

<cite>
**Referenced Files in This Document**   
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java)
- [IRNode.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRNode.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [IFlowOptimizer Interface](#iflowoptimizer-interface)
3. [Control Flow Graph (CFG) Data Structure](#control-flow-graph-cfg-data-structure)
4. [BasicBlock Structure and Traversal](#basicblock-structure-and-traversal)
5. [Integration in Compilation Pipeline](#integration-in-compilation-pipeline)
6. [Implementing Custom Optimization Passes](#implementing-custom-optimization-passes)
7. [Concrete Optimization Examples](#concrete-optimization-examples)
8. [Data Flow Analysis with LivenessAnalysis](#data-flow-analysis-with-livenessanalysis)
9. [Performance and Thread Safety Considerations](#performance-and-thread-safety-considerations)
10. [Chaining Multiple Optimizers](#chaining-multiple-optimizers)

## Introduction
The IFlowOptimizer interface provides a framework for implementing custom optimization passes in the compiler's intermediate representation (IR) processing pipeline. This document details how developers can leverage this interface to create optimization algorithms such as dead code elimination, constant propagation, and loop optimization. The optimization system operates on control flow graphs (CFG) composed of basic blocks, enabling structural and data flow optimizations during compilation.

**Section sources**
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)

## IFlowOptimizer Interface
The IFlowOptimizer interface defines the contract for optimization passes that operate on control flow graphs. It serves as the foundation for implementing various optimization algorithms by providing a standardized way to process CFG structures.

```mermaid
classDiagram
class IFlowOptimizer {
<<interface>>
+void onHandle(CFG<I> cfg)
}
class ControlFlowAnalysis {
+static Boolean DEBUG
+void onHandle(CFG<I> cfg)
}
class LivenessAnalysis {
+Set<Operand> currentUse
+Set<Operand> currentDef
+void reset()
+Void visit(BinExpr node)
+Void visit(UnaryExpr node)
+Void visit(CallFunc callFunc)
+Void visit(Assign assign)
+Void visit(ReturnVal returnVal)
}
IFlowOptimizer <|-- ControlFlowAnalysis
LivenessAnalysis ..|> IRVisitor
```

**Diagram sources**
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java)

**Section sources**
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)

## Control Flow Graph (CFG) Data Structure
The CFG class represents the control flow graph of a function, organizing basic blocks and their connections. It provides methods for graph manipulation, traversal, and optimization application.

```mermaid
classDiagram
class CFG {
+List<BasicBlock<I>> nodes
+List<Triple<Integer,Integer,Integer>> edges
+List<Pair<Set<Integer>, Set<Integer>>> links
+List<IFlowOptimizer<I>> optimizers
+BasicBlock<I> getBlock(int id)
+Set<Integer> getFrontier(int id)
+Set<Integer> getSucceed(int id)
+int getInDegree(int id)
+Stream<Triple<Integer,Integer,Integer>> getInEdges(int key)
+int getOutDegree(int id)
+void removeEdge(Triple<Integer,Integer,Integer> edge)
+void removeNode(BasicBlock<I> node)
+void addOptimizer(IFlowOptimizer<I> optimizer)
+void applyOptimizers()
+List<I> getIRNodes()
}
class BasicBlock {
+int id
+List<Loc<I>> codes
+Kind kind
+Set<Operand> def
+Set<Operand> liveUse
+Set<Operand> liveIn
+Set<Operand> liveOut
+Label label
+int getId()
+Label getLabel()
+String getOrdLabel()
+boolean isEmpty()
+List<Loc<I>> allSeq()
+List<Loc<I>> dropLabelSeq()
+I getLastInstr()
+void mergeNearBlock(BasicBlock<I> nextBlock)
+void removeLastInstr()
+Stream<I> getIRNodes()
}
CFG "1" *-- "0..*" BasicBlock
CFG "1" -- "0..*" IFlowOptimizer
```

**Diagram sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java)

**Section sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)

## BasicBlock Structure and Traversal
BasicBlock represents a sequence of instructions with a single entry and exit point. It provides methods for instruction traversal, modification, and structural operations essential for optimization.

```mermaid
flowchart TD
Start["BasicBlock Structure"] --> Properties["Properties\n- id: Block identifier\n- codes: Instruction list\n- kind: Block type\n- def/live sets: Data flow info"]
Properties --> Methods["Key Methods"]
Methods --> getId["getId()\nReturns block ID"]
Methods --> getLabel["getLabel()\nReturns block label"]
Methods --> getLastInstr["getLastInstr()\nReturns last instruction"]
Methods --> isEmpty["isEmpty()\nChecks if block is empty"]
Methods --> dropLabelSeq["dropLabelSeq()\nReturns instructions excluding label"]
Methods --> mergeNearBlock["mergeNearBlock()\nMerges with adjacent block"]
Methods --> removeLastInstr["removeLastInstr()\nRemoves final instruction"]
Methods --> getIRNodes["getIRNodes()\nStreams all IR nodes"]
Methods --> Iterators["Iterators"]
Iterators --> forward["iterator()\nForward traversal"]
Iterators --> backward["backwardIterator()\nReverse traversal"]
```

**Diagram sources**
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java)

**Section sources**
- [BasicBlock.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java)

## Integration in Compilation Pipeline
The optimization system integrates into the compilation pipeline through the CFG's optimizer registration and application mechanism. Optimizers are registered and executed in sequence during the code generation phase.

```mermaid
sequenceDiagram
participant Compiler as "Compiler"
participant CFG as "CFG"
participant Optimizer as "IFlowOptimizer"
participant Block as "BasicBlock"
Compiler->>CFG : create CFG from IR
Compiler->>CFG : addOptimizer(Optimizer1)
Compiler->>CFG : addOptimizer(Optimizer2)
Compiler->>CFG : addOptimizer(Optimizer3)
Compiler->>CFG : applyOptimizers()
loop For each optimizer
CFG->>Optimizer : onHandle(cfg)
Optimizer->>CFG : getNodes()
loop For each BasicBlock
Optimizer->>Block : analyze structure
Optimizer->>Block : modify instructions
Optimizer->>CFG : removeEdge()
Optimizer->>CFG : removeNode()
end
end
CFG-->>Compiler : return optimized CFG
```

**Diagram sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)

**Section sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)

## Implementing Custom Optimization Passes
Developers can implement custom optimization passes by creating classes that implement the IFlowOptimizer interface and overriding the onHandle method to define optimization logic.

```mermaid
flowchart TD
Start["Create New Optimizer Class"] --> Implement["Implement IFlowOptimizer Interface"]
Implement --> Extend["Class Definition"]
Extend --> ClassName["Class name ending with 'Analysis'\n(e.g., DeadCodeEliminationAnalysis)"]
Extend --> Interface["Implement IFlowOptimizer<I extends IRNode>"]
Implement --> Override["Override onHandle Method"]
Override --> Parameters["Parameter: CFG<I> cfg"]
Override --> Logic["Optimization Logic"]
Logic --> Traverse["Traverse CFG nodes"]
Traverse --> Condition["Apply optimization conditions"]
Condition --> Modify["Modify basic blocks"]
Modify --> Remove["Remove unnecessary instructions/nodes"]
Modify --> Merge["Merge adjacent blocks"]
Logic --> Register["Register optimizer in pipeline"]
Register --> Add["cfg.addOptimizer(new YourOptimizer())"]
Logic --> Test["Test optimization"]
Test --> Validate["Verify CFG structure"]
Test --> Performance["Measure performance impact"]
```

**Section sources**
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)

## Concrete Optimization Examples
The ControlFlowAnalysis class demonstrates concrete optimization patterns including basic block merging and redundant jump elimination.

```mermaid
flowchart TD
Start["ControlFlowAnalysis Optimization"] --> Step1["Step 1: Eliminate Redundant Jumps"]
Step1 --> Condition1["For each block with out-degree 1"]
Condition1 --> JumpCheck["Check if last instruction is JMP"]
JumpCheck --> TargetCheck["Verify JMP target matches successor"]
TargetCheck --> Remove["Remove JMP instruction and edge"]
Start --> Step2["Step 2: Merge Basic Blocks"]
Step2 --> InDegreeCheck["For each block with in-degree 1"]
InDegreeCheck --> SoloLink["Check single frontier and out-degree 1"]
SoloLink --> Merge["Merge with predecessor block"]
Merge --> Update["Update edges and remove node"]
Step2 --> Queue["Use removeQueue to safely remove nodes"]
Queue --> Process["Process all queued blocks"]
Step1 --> Benefits["Benefits"]
Step1 --> "Reduces instruction count"
Step1 --> "Eliminates unnecessary jumps"
Step1 --> "Improves cache performance"
Step2 --> Benefits2["Benefits"]
Step2 --> "Reduces block count"
Step2 --> "Improves branch prediction"
Step2 --> "Enables further optimizations"
```

**Diagram sources**
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)

**Section sources**
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)

## Data Flow Analysis with LivenessAnalysis
The LivenessAnalysis class implements data flow analysis to track variable usage and definition, essential for optimizations like dead code elimination and register allocation.

```mermaid
classDiagram
class LivenessAnalysis {
-Set<Operand> currentUse
-Set<Operand> currentDef
+Set<Operand> getCurrentUse()
+Set<Operand> getCurrentDef()
+void reset()
}
LivenessAnalysis ..|> IRVisitor
IRVisitor <|-- LivenessAnalysis
class IRVisitor {
<<interface>>
+Void visit(BinExpr node)
+Void visit(UnaryExpr node)
+Void visit(CallFunc callFunc)
+Void visit(Assign assign)
+Void visit(ReturnVal returnVal)
+Void visit(OperandSlot operandSlot)
+Void visit(FrameSlot frameSlot)
}
LivenessAnalysis --> "tracks" Operand : "in currentUse/currentDef"
class Operand {
<<abstract>>
}
class Assign {
+Operand getLhs()
+IRNode getRhs()
}
class BinExpr {
+IRNode getLhs()
+IRNode getRhs()
}
Assign --> Operand : "lhs defines"
BinExpr --> Operand : "operands use"
LivenessAnalysis ..> Assign : "visit determines def/use"
LivenessAnalysis ..> BinExpr : "visit determines use"
```

**Diagram sources**
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java)
- [IRVisitor.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRVisitor.java)

**Section sources**
- [LivenessAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java)

## Performance and Thread Safety Considerations
Optimization passes must consider performance implications and thread safety when modifying shared data structures.

```mermaid
flowchart TD
Performance["Performance Considerations"] --> Complexity["Time Complexity"]
Complexity --> "O(n) for CFG traversal"
Complexity --> "O(1) for edge/node operations"
Complexity --> "Minimize redundant traversals"
Performance --> Memory["Memory Usage"]
Memory --> "Avoid unnecessary object creation"
Memory --> "Reuse collections when possible"
Memory --> "Consider memory footprint of data flow sets"
ThreadSafety["Thread Safety"] --> SingleThread["Single-threaded assumption"]
SingleThread --> "No synchronization needed"
SingleThread --> "CFG modifications during traversal"
ThreadSafety --> Concurrent["Concurrent Execution"]
Concurrent --> "Not supported by current design"
Concurrent --> "Sequential application required"
Concurrent --> "Immutable CFG between passes"
BestPractices["Best Practices"] --> Efficient["Efficient Traversal"]
BestPractices --> "Use iterator() and backwardIterator()"
BestPractices --> "Cache frequently accessed properties"
BestPractices --> SafeModification["Safe Modification"]
BestPractices --> "Use removeQueue for node removal"
BestPractices --> "Update edges before removing nodes"
BestPractices --> "Validate CFG integrity after modifications"
```

**Section sources**
- [ControlFlowAnalysis.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java)
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)

## Chaining Multiple Optimizers
Multiple optimizers can be chained together to perform comprehensive optimization, with each pass potentially enabling further optimizations in subsequent passes.

```mermaid
sequenceDiagram
participant Pipeline as "Optimization Pipeline"
participant CFG as "CFG"
participant O1 as "Optimizer 1<br/>(e.g., Dead Code Elimination)"
participant O2 as "Optimizer 2<br/>(e.g., Constant Propagation)"
participant O3 as "Optimizer 3<br/>(e.g., Loop Optimization)"
Pipeline->>CFG : Initialize with IR
Pipeline->>CFG : addOptimizer(O1)
Pipeline->>CFG : addOptimizer(O2)
Pipeline->>CFG : addOptimizer(O3)
Pipeline->>CFG : applyOptimizers()
CFG->>O1 : onHandle(cfg)
O1->>CFG : Modify structure
O1->>CFG : Remove dead code
O1-->>CFG : Return
CFG->>O2 : onHandle(cfg)
O2->>CFG : Propagate constants
O2->>CFG : Simplify expressions
O2-->>CFG : Return
CFG->>O3 : onHandle(cfg)
O3->>CFG : Optimize loops
O3->>CFG : Unroll where beneficial
O3-->>CFG : Return
CFG-->>Pipeline : Return optimized CFG
Note right of Pipeline : Optimizers applied<br/>in registration order<br/>Each pass benefits from<br/>previous optimizations
```

**Diagram sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)
- [IFlowOptimizer.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java)

**Section sources**
- [CFG.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java)