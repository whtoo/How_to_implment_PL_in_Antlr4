# Chapter 14: Control Flow Graph and Basic Blocks

**Module**: Module 4 - Intermediate Representation & Optimization (EP19-EP20)
**Target Reader**: Engineers with compiler experience, learning optimization techniques
**Prerequisites**: Graph algorithms, control flow analysis, IR basics (Chapter 13)
**EP Coverage**: EP20 (CFG construction, basic block partition, dominance relationships)
**Previous Chapter**: Chapter 13 (Intermediate Representation Design)
**Next Chapter**: Chapter 15 (Local Optimization and Code Generation)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Understand control flow graphs (CFG)** and their role in compiler optimization
- **Partition IR instructions into basic blocks** following rigorous algorithm
- **Build CFGs from linear IR sequences** and identify edges (fall-through, conditional, unconditional)
- **Analyze control flow properties** including predecessors, successors, and dominance
- **Implement block merging** and empty block elimination optimizations
- **Visualize CFGs** using graph visualization tools (DOT, mermaid)
- **Analyze function control flow** for dataflow analysis preparation

**Core Deliverables**:
- Implement CFG builder from linear IR
- Implement basic block partitioning algorithm
- Implement CFG visualization (DOT/mermaid output)
- Implement basic block optimization (merging, empty block removal)
- Validate CFG correctness through unit tests

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 13 (IR Design and Implementation)
- ✅ Understanding of three-address code IR structure
- ✅ Familiarity with graph theory (nodes, edges, directed graphs)
- ✅ Knowledge of control flow statements (if, while, break, continue, return)

**Required Programming Skills**:
- ✅ Advanced Java: Collections, graph data structures, algorithms
- ✅ Tree/graph traversal algorithms (DFS, BFS)
- ✅ Set operations (union, intersection, difference)
- ✅ Data structures: Lists, Sets, Maps, Stacks, Queues

**Compiler Theory Knowledge**:
- ✅ Basic understanding of instruction sequencing
- ✅ Familiarity with jump instructions (JMP, conditional jumps)
- ✅ Knowledge of labels and basic block boundaries
- ✅ Understanding of program counter and control transfer

**Mathematical/Algorithmic Foundation**:
- ✅ Graph theory basics (vertices, edges, paths, cycles)
- ✅ Set theory (intersection, union, subset)
- ✅ Recursion and post-order traversal
- ✅ Understanding of reachability in graphs

---

## 3. Core Concepts to Master

### 3.1 Control Flow Graph (CFG) Fundamentals

**Definition**: A CFG is a directed graph where nodes represent basic blocks of instructions and edges represent possible control flow between blocks.

**Key Components**:
- **Basic Blocks (Nodes)**: Maximal sequences of instructions with single entry and single exit
- **Edges**: Control flow transfers between blocks
  - **Fall-through edges**: Natural execution flow (no explicit jump)
  - **Unconditional jump edges**: JMP instruction
  - **Conditional jump edges**: CJMP instruction (true/false branches)
- **Entry block**: First block executed in a function
- **Exit block**: Block(s) where function returns

**Why CFG?**
1. **Optimization**: Enables dataflow analysis and transformation
2. **Analysis**: Identifies unreachable code, loops, dependencies
3. **Visualization**: Provides graphical representation of program structure
4. **Transformation**: Facilitates block merging, code motion, etc.

### 3.2 Basic Block Definition

**Formal Definition**: A basic block is a sequence of instructions B = [i1, i2, ..., in] such that:
1. **Single entry**: Control enters block only at first instruction (i1)
2. **Single exit**: Control leaves block only at last instruction (in)
3. **Maximality**: Cannot extend block without violating 1 or 2

**Basic Block Boundary Conditions**:
Block starts at instruction i if:
- i is first instruction of a function
- i is target of a jump instruction (has label)
- i follows a conditional/unconditional jump

Block ends at instruction i if:
- i is a jump instruction (JMP, CJMP)
- i is a return instruction
- i is followed by a label (jump target)

**Example**:
```
IR Instructions:
L0: a = 1           <- Block start (label)
    b = 2
    if a > b goto L1  <- Block end (conditional jump)
L1: c = 3           <- Block start (label)
    return c         <- Block end (return)
```

**Basic Blocks**:
```
Block 0: [a = 1, b = 2, if a > b goto L1]
Block 1: [c = 3, return c]
```

### 3.3 Basic Block Partitioning Algorithm

**EP20 Implementation**:

```java
public class BasicBlock<I extends IRNode> {
    public final int id;
    public List<Loc<I>> codes;
    public Kind kind;  // CONTINUOUS, END_IF, END_WHILE, etc.
    public Label label;

    public static BasicBlock<IRNode> buildFromLinearBlock(
        LinearIRBlock block,
        List<BasicBlock<IRNode>> cachedNodes
    ) {
        return new BasicBlock<IRNode>(
            block.getKind(),
            block.getStmts().stream().map(Loc::new).toList(),
            block.getLabel(),
            block.getOrd()
        );
    }
}
```

**Partitioning Steps**:
1. Initialize with first instruction as block leader
2. Scan forward through instructions
3. When encountering block end (jump/return), terminate current block
4. Next instruction (if labeled) starts new block
5. Repeat until all instructions processed

**Pseudo-code**:
```
function partitionBasicBlocks(instructions):
    blocks = []
    currentBlock = null

    for i from 0 to instructions.length - 1:
        instr = instructions[i]

        # Check if instr starts new block
        if i == 0 or instr.isLabel() or instructions[i-1].isJump():
            if currentBlock is not None:
                blocks.add(currentBlock)
            currentBlock = new BasicBlock(i)

        currentBlock.add(instr)

    if currentBlock is not None:
        blocks.add(currentBlock)

    return blocks
```

### 3.4 CFG Construction Algorithm

**EP20 CFGBuilder Implementation**:

```java
public class CFGBuilder {
    private final List<BasicBlock<IRNode>> basicBlocks;
    private final List<Triple<Integer, Integer, Integer>> edges;

    public CFGBuilder(LinearIRBlock startBlock) {
        basicBlocks = new ArrayList<>();
        edges = new ArrayList<>();
        build(startBlock, new HashSet<>());
    }

    private void build(LinearIRBlock block, Set<String> cachedEdgeLinks) {
        var currentBlock = BasicBlock.buildFromLinearBlock(block, basicBlocks);
        basicBlocks.add(currentBlock);

        var lastInstr = block.getStmts().get(block.getStmts().size() - 1);
        var currentOrd = block.getOrd();

        # Handle JMP instruction
        if (lastInstr instanceof JMP jmp) {
            var destOrd = jmp.getNext().getOrd();
            edges.add(Triple.of(currentOrd, destOrd, 5));  # Type 5: JMP edge
        }

        # Handle CJMP instruction
        else if (lastInstr instanceof CJMP cjmp) {
            var elseOrd = cjmp.getElseBlock().getOrd();
            edges.add(Triple.of(currentOrd, elseOrd, 5));  # Type 5: Conditional edge
        }

        # Add fall-through edges
        for (var successor : block.getSuccessors()) {
            var key = currentOrd + "-" + successor.getOrd() + "-" + 10;
            if (!cachedEdgeLinks.contains(key)) {
                cachedEdgeLinks.add(key);
                edges.add(Triple.of(currentOrd, successor.getOrd(), 10));  # Type 10: Fall-through
            }
            build(successor, cachedEdgeLinks);
        }
    }
}
```

**Edge Types**:
- **Type 5 (JMP edges)**: Explicit jump targets
- **Type 10 (Fall-through edges)**: Natural control flow

**Construction Steps**:
1. Partition linear IR into basic blocks
2. Identify block leaders (labels)
3. Scan each block's last instruction:
   - If JMP: Add edge to jump target
   - If CJMP: Add edges to both then and else targets
   - If fall-through: Add edge to next block
4. Recursively build CFG for all reachable blocks
5. Remove duplicate edges (cachedEdgeLinks)

### 3.5 CFG Data Structure

**EP20 CFG Implementation**:

```java
public class CFG<I extends IRNode> {
    public List<BasicBlock<I>> nodes;
    public List<Triple<Integer, Integer, Integer>> edges;

    // BasicBlock operations
    public BasicBlock<I> getBlock(int id) {
        return nodes.stream()
            .filter(b -> b.getId() == id)
            .findFirst()
            .orElse(null);
    }

    // Edge operations
    public List<Integer> getSucceed(int id) {
        return edges.stream()
            .filter(e -> e.getLeft() == id)
            .map(Triple::getMiddle)
            .toList();
    }

    public List<Triple<Integer, Integer, Integer>> getInEdges(int id) {
        return edges.stream()
            .filter(e -> e.getMiddle() == id)
            .toList();
    }

    public int getInDegree(int id) {
        return getInEdges(id).size();
    }

    public int getOutDegree(int id) {
        return getSucceed(id).size();
    }

    // Predecessors (blocks that can reach this block)
    public List<Integer> getFrontier(int id) {
        return getInEdges(id).stream()
            .map(Triple::getLeft)
            .toList();
    }

    // Visualization
    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("graph TD\n");

        for (var edge : edges) {
            sb.append("  %d --> %d\n".formatted(edge.getLeft(), edge.getMiddle()));
        }

        for (var node : nodes) {
            sb.append("  %d[\"%s\"]\n".formatted(node.getId(), node.getOrdLabel()));
        }

        return sb.toString();
    }
}
```

**Key Operations**:
- `getInDegree(id)`: Number of incoming edges (predecessors)
- `getOutDegree(id)`: Number of outgoing edges (successors)
- `getFrontier(id)`: List of predecessor block IDs
- `getSucceed(id)`: List of successor block IDs
- `toString()`: Generate mermaid/DOT graph

### 3.6 Dominance Analysis

**Definition**: Block A dominates block B if all paths from entry to B must pass through A.

**Notation**: A ≻ B (A dominates B)

**Properties**:
- **Reflexive**: Every block dominates itself (B ≻ B)
- **Transitive**: If A ≻ B and B ≻ C, then A ≻ C
- **Antisymmetric**: If A ≠ B, then A ≻ B implies B ≺ A (B does not dominate A)

**Immediate Dominator (IDom)**:
- Block D is the immediate dominator of B if:
  - D ≻ B (D dominates B)
  - For any other block C that dominates B, either C ≻ D or C = D

**IDom Properties**:
- Every block except entry has exactly one immediate dominator
- IDom forms a tree (dominator tree)

**EP20 Extension**:
```java
public class DominanceAnalysis<I extends IRNode> implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        // Compute dominators using iterative dataflow analysis
        var nodes = cfg.nodes;
        var entry = nodes.get(0);

        // Initialize dominators
        Map<Integer, Set<Integer>> dom = new HashMap<>();
        for (var node : nodes) {
            dom.put(node.getId(), new HashSet<>(nodes.stream()
                .map(BasicBlock::getId)
                .toList()));
        }
        dom.put(entry.getId(), Set.of(entry.getId()));

        // Iterative fixed-point algorithm
        boolean changed;
        do {
            changed = false;
            for (var node : nodes) {
                if (node == entry) continue;

                var preds = cfg.getFrontier(node.getId());
                var intersection = new HashSet<>(dom.get(preds.get(0)));

                for (int i = 1; i < preds.size(); i++) {
                    intersection.retainAll(dom.get(preds.get(i)));
                }
                intersection.add(node.getId());

                if (!dom.get(node.getId()).equals(intersection)) {
                    dom.put(node.getId(), intersection);
                    changed = true;
                }
            }
        } while (changed);
    }
}
```

### 3.7 Basic Block Optimization

#### 3.7.1 Empty Block Elimination

**Goal**: Remove empty blocks that serve no purpose

**EP20 Implementation**:
```java
private void optimizeEmptyBlock(@NotNull LinearIRBlock block) {
    if (block.getStmts().isEmpty()) {
        // Remove empty block with no successors
        if (block.getSuccessors().isEmpty()) {
            needRemovedBlocks.add(block);
            return;
        }

        // Redirect jumps to empty block's successor
        var nextBlock = block.getSuccessors().get(0);
        for (var ref : block.getJmpRefMap()) {
            if (ref instanceof JMP jmp) {
                jmp.setNext(nextBlock);
            } else if (ref instanceof CJMP cjmp) {
                cjmp.setElseBlock(nextBlock);
            }
        }

        // Update predecessors' successor lists
        block.getPredecessors().forEach(prev -> {
            prev.removeSuccessor(block);
            prev.getSuccessors().add(nextBlock);
        });

        needRemovedBlocks.add(block);
    }

    // Recursive optimization of successors
    for (var successor : block.getSuccessors()) {
        optimizeEmptyBlock(successor);
    }
}
```

**Example**:
```
Before:
Block 0: if x goto L2
Block 1: (empty)    <- Eliminate this
Block 2: x = 1

After:
Block 0: if x goto L2
Block 2: x = 1
```

#### 3.7.2 Block Merging

**Goal**: Merge consecutive blocks where possible to reduce jumps

**EP20 Implementation**:
```java
public void mergeNearBlock(BasicBlock<I> nextBlock) {
    // Remove last jump instruction
    if (getLastInstr() instanceof JMPInstr) {
        codes.remove(codes.size() - 1);
    }

    // Merge instructions and update kind
    codes.addAll(nextBlock.dropLabelSeq());
    kind = nextBlock.kind;
}
```

**Conditions for Merging**:
- Current block ends with JMP to nextBlock
- nextBlock has single predecessor (current block)
- nextBlock is not a loop header

**Example**:
```
Before:
Block 0: a = 1
         goto L1
Block 1: b = 2

After:
Block 0: a = 1
         b = 2
```

#### 3.7.3 Redundant Jump Elimination

**Goal**: Remove jump to next instruction

**EP20 Implementation**:
```java
// In ControlFlowAnalysis
if (outDeg == 1 && block.getLastInstr() instanceof JMPInstr jmpInstr) {
    var targetBlockId = jmpInstr.getTarget().getSeq();

    // Check if jump target is next block
    cfg.getSucceed(key).stream()
        .filter(x -> x == targetBlockId)
        .findFirst()
        .ifPresent(next -> {
            block.removeLastInstr();  // Remove JMP
            cfg.removeEdge(Triple.of(key, targetBlockId, 5));
        });
}
```

**Example**:
```
Before:
Block 0: a = 1
         goto L1    <- Redundant
Block 1: b = 2    <- Falls through from L0

After:
Block 0: a = 1
Block 1: b = 2
```

### 3.8 Control Flow Analysis

**EP20 ControlFlowAnalysis Implementation**:

```java
public class ControlFlowAnalysis<I extends IRNode> implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        List<Triple<Integer, Integer, Integer>> needRemovedLink = new ArrayList<>();

        // Step 1: Remove redundant jumps
        for (var block : cfg.nodes) {
            var key = block.getId();
            var outDeg = cfg.getOutDegree(key);

            if (outDeg == 1 && block.getLastInstr() instanceof JMPInstr jmpInstr) {
                var targetBlockId = jmpInstr.getTarget().getSeq();
                var needRemoveLastInstr = new AtomicBoolean(false);

                cfg.getSucceed(key).stream()
                    .filter(x -> x == targetBlockId)
                    .findFirst()
                    .ifPresent(next -> {
                        needRemoveLastInstr.set(true);
                    });

                if (needRemoveLastInstr.get()) {
                    block.removeLastInstr();
                    cfg.removeEdge(Triple.of(key, targetBlockId, 5));
                }
            }
        }

        // Step 2: Merge blocks
        var removeQueue = new LinkedList<BasicBlock<I>>();

        for (var block : cfg.nodes) {
            var key = block.getId();
            var inDeg = cfg.getInEdges(key).toList();
            var isSrcSoloLink = (long) cfg.getFrontier(key).size() == 1;
            var isDestSoloLink = isSrcSoloLink && cfg.getOutDegree(inDeg.get(0).getLeft()) == 1;

            if (inDeg.size() == 1 && isDestSoloLink) {
                cfg.getFrontier(key).stream().findFirst().ifPresent(frontier -> {
                    var prevBlock = cfg.getBlock(frontier);
                    prevBlock.mergeNearBlock(block);
                    cfg.removeEdge(inDeg.get(0));
                    removeQueue.add(block);
                });
            }
        }

        // Step 3: Remove merged blocks
        for (var block : removeQueue) {
            cfg.removeNode(block);
        }
    }
}
```

**Optimization Pipeline**:
1. **Redundant Jump Elimination**: Remove jumps to next instruction
2. **Block Merging**: Combine consecutive blocks
3. **Empty Block Elimination**: Remove empty blocks
4. **Dead Code Elimination**: Remove unreachable blocks (if implemented)

### 3.9 CFG Visualization

**Mermaid Format**:
```mermaid
graph TD
    0 --> 1
    0 --> 2
    1 --> 3
    2 --> 3
    3[Exit]
```

**EP20 DOT/Mermaid Generation**:
```java
public class CFG<I extends IRNode> {
    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("graph TD\n");

        // Add edges
        for (var edge : edges) {
            sb.append("  %d --> %d\n".formatted(edge.getLeft(), edge.getMiddle()));
        }

        // Add nodes with labels
        for (var node : nodes) {
            sb.append("  %d[\"%s\"]\n".formatted(node.getId(), node.getOrdLabel()));
        }

        return sb.toString();
    }
}
```

**Visualization Tools**:
- **Mermaid**: Built-in support in markdown viewers
- **Graphviz DOT**: Professional graph visualization
- **Online tools**: Mermaid Live Editor, Graphviz Online

### 3.10 Loop Identification

**Natural Loop**: Set of blocks in a loop defined by:
1. **Header**: A block that dominates all blocks in the loop
2. **Back edge**: An edge from a block in the loop to the header
3. **Loop body**: All blocks reachable from back edge without passing through header

**Loop Detection Algorithm**:
1. Find all back edges (edges where successor dominates predecessor)
2. For each back edge (n → header):
   - Loop body = header ∪ {all blocks reachable from n without passing through header}

**EP20 Extension**:
```java
public class LoopDetection<I extends IRNode> implements IFlowOptimizer<I> {
    private Map<Integer, Set<Integer>> dominators;

    @Override
    public void onHandle(CFG<I> cfg) {
        // Assume dominators already computed
        var loops = detectNaturalLoops(cfg);

        for (var loop : loops) {
            System.out.println("Loop header: " + loop.getHeader());
            System.out.println("Loop body: " + loop.getBody());
        }
    }

    private List<NaturalLoop> detectNaturalLoops(CFG<I> cfg) {
        var loops = new ArrayList<NaturalLoop>();

        for (var edge : cfg.edges) {
            var src = edge.getLeft();
            var dst = edge.getMiddle();

            // Check if dst dominates src (back edge)
            if (dominators.get(src).contains(dst)) {
                var loop = new NaturalLoop(dst);
                loop.addBlock(dst);

                // Find all blocks in loop body
                var visited = new HashSet<Integer>();
                collectLoopBody(cfg, src, dst, visited, loop);

                loops.add(loop);
            }
        }

        return loops;
    }

    private void collectLoopBody(CFG<I> cfg, int current, int header,
                                 Set<Integer> visited, NaturalLoop loop) {
        if (current == header) return;
        if (visited.contains(current)) return;

        visited.add(current);
        loop.addBlock(current);

        for (var succ : cfg.getSucceed(current)) {
            collectLoopBody(cfg, succ, header, visited, loop);
        }
    }
}
```

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Identify Basic Blocks**
Given IR instructions:
```
L0: a = 1
    b = 2
    if a > b goto L2
L1: c = 3
    goto L3
L2: d = 4
L3: return d
```

**Tasks**:
- Identify all basic block leaders (block starts)
- Identify all block boundaries (block ends)
- Partition into basic blocks
- List each block's instructions

**Expected Answer**:
```
Block 0 (L0): [a = 1, b = 2, if a > b goto L2]
Block 1 (L1): [c = 3, goto L3]
Block 2 (L2): [d = 4]
Block 3 (L3): [return d]
```

**Exercise 2: Build CFG for Simple Sequence**
Given basic blocks:
```
Block 0: a = 1
         goto L1
Block 1: b = 2
```

**Tasks**:
- Identify edges between blocks
- Determine edge types (JMP vs fall-through)
- Draw CFG graph
- Write data structure representing CFG

**Expected Answer**:
```
Edges: [(0, 1, type=JMP)]
Graph: 0 --> 1
```

**Exercise 3: Implement Basic Block Partitioning**
- Write function `partitionBasicBlocks(List<IRNode> instructions)`
- Return list of BasicBlock objects
- Handle labels, jumps, returns as block boundaries
- Write unit tests for various IR sequences

### 4.2 Intermediate Exercises

**Exercise 4: Build CFG for If-Else**
Given IR:
```
L0: t0 = x > 10
    if t0 goto L2
L1: y = 0
    goto L3
L2: y = 1
L3: return y
```

**Tasks**:
- Partition into basic blocks
- Identify all edges (including fall-through from conditional)
- Draw CFG
- Compute in-degree and out-degree for each block

**Expected Answer**:
```
Blocks:
  B0 (L0): [t0 = x > 10, if t0 goto L2]
  B1 (L1): [y = 0, goto L3]
  B2 (L2): [y = 1]
  B3 (L3): [return y]

Edges:
  B0 --> B1 (fall-through, else branch)
  B0 --> B2 (conditional, then branch)
  B1 --> B3 (JMP)
  B2 --> B3 (fall-through)

In-degrees:  [0, 1, 1, 2]
Out-degrees: [2, 1, 1, 0]
```

**Exercise 5: Build CFG for While Loop**
Given IR:
```
L0: t0 = i < 10
    if t0 goto L2
    goto L3
L1: i = i + 1
    goto L0
L2: sum = sum + i
    goto L1
L3: return sum
```

**Tasks**:
- Partition into basic blocks
- Identify back edge (loop)
- Identify loop header
- Draw CFG
- Identify natural loops

**Expected Answer**:
```
Blocks:
  B0 (L0): [t0 = i < 10, if t0 goto L2, goto L3]
  B1 (L1): [i = i + 1, goto L0]
  B2 (L2): [sum = sum + i, goto L1]
  B3 (L3): [return sum]

Edges:
  B0 --> B2 (conditional, true)
  B0 --> B3 (conditional, false)
  B0 --> B3 (explicit goto)
  B1 --> B0 (back edge!)
  B2 --> B1 (JMP)

Loop:
  Header: B0
  Back edge: B1 --> B0
  Loop body: {B0, B1, B2}
```

**Exercise 6: Implement Empty Block Elimination**
Given CFG with empty block:
```
Block 0: if x goto L2
Block 1: (empty)  <- Remove this
Block 2: y = 1
```

**Tasks**:
- Detect empty blocks
- Redirect jumps to empty block's successor
- Update predecessor lists
- Remove empty block from CFG
- Test with unit tests

### 4.3 Advanced Exercises

**Exercise 7: Implement Block Merging Optimization**
Given consecutive blocks:
```
Block 0: a = 1
         goto L1
Block 1: b = 2
```

**Tasks**:
- Detect blocks that can be merged
- Check merge conditions (single predecessor, no loops)
- Merge block instructions
- Remove JMP instruction
- Update CFG structure

**Exercise 8: Implement Dominance Analysis**
- Write function `computeDominators(CFG cfg)`
- Use iterative fixed-point algorithm
- Return Map<Integer, Set<Integer>> where key is block ID, value is set of dominators
- Verify entry block only dominates itself
- Test on various CFG structures (diamond, loop, complex)

**Exercise 9: Implement Redundant Jump Elimination**
Given CFG:
```
Block 0: a = 1
         goto L1
Block 1: b = 2    <- Falls through from L0
```

**Tasks**:
- Detect jumps to next block
- Check if target is successor in fall-through
- Remove redundant JMP
- Update edge list
- Verify CFG correctness

**Exercise 10: Build Complete CFG Optimizer**
- Implement CFGBuilder from linear IR
- Implement ControlFlowAnalysis with all optimizations:
  - Redundant jump elimination
  - Block merging
  - Empty block elimination
- Generate DOT/mermaid visualization
- Validate with test programs

---

## 5. Common Pitfalls

### 5.1 Incorrect Basic Block Partitioning

**Pitfall**: Missing block boundaries at jump targets
```java
// WRONG: Not creating new block at label
List<IRNode> blocks = new ArrayList<>();
BasicBlock currentBlock = new BasicBlock();

for (var instr : instructions) {
    if (instr instanceof Label label) {
        // Missing: Finish current block and start new block at label
        blocks.add(currentBlock);
        currentBlock = new BasicBlock(label);
    }
    currentBlock.add(instr);
}
```

**Solution**: Always create new block at labels
```java
// CORRECT: Create new block at label
for (var instr : instructions) {
    if (instr instanceof Label label) {
        if (!currentBlock.isEmpty()) {
            blocks.add(currentBlock);
        }
        currentBlock = new BasicBlock(label);
    }
    currentBlock.add(instr);
}
```

### 5.2 Missing Fall-Through Edges

**Pitfall**: Only adding explicit jump edges, missing fall-through
```java
// WRONG: Only adding JMP edges
if (lastInstr instanceof JMP jmp) {
    edges.add(edge(blockId, jmp.getTargetId()));
}
// Missing fall-through edge!
```

**Solution**: Add fall-through edges explicitly
```java
// CORRECT: Handle both explicit and fall-through edges
if (lastInstr instanceof JMP jmp) {
    edges.add(edge(blockId, jmp.getTargetId()));
} else if (lastInstr instanceof CJMP cjmp) {
    edges.add(edge(blockId, cjmp.getTrueBranch()));
    edges.add(edge(blockId, cjmp.getFalseBranch()));
} else {
    // Fall-through to next block
    if (hasNextBlock) {
        edges.add(edge(blockId, nextBlockId));
    }
}
```

### 5.3 Duplicate Edges

**Pitfall**: Adding same edge multiple times
```java
// WRONG: Duplicate edges in complex control flow
for (var block : blocks) {
    for (var successor : block.getSuccessors()) {
        edges.add(new Edge(block, successor));  // May add duplicates
    }
}
```

**Solution**: Use set to track unique edges
```java
// CORRECT: Track visited edges
Set<String> cachedEdgeLinks = new HashSet<>();

for (var block : blocks) {
    for (var successor : block.getSuccessors()) {
        var key = block.getId() + "-" + successor.getId();
        if (!cachedEdgeLinks.contains(key)) {
            cachedEdgeLinks.add(key);
            edges.add(new Edge(block, successor));
        }
    }
}
```

### 5.4 Incorrect Dominance Computation

**Pitfall**: Incorrect initialization in iterative algorithm
```java
// WRONG: Wrong initialization
Map<Integer, Set<Integer>> dom = new HashMap<>();
for (var node : nodes) {
    dom.put(node.getId(), Set.of());  // Wrong: All start empty
}
```

**Solution**: Correct initialization (all nodes dominate all except entry)
```java
// CORRECT: Proper initialization
Map<Integer, Set<Integer>> dom = new HashMap<>();
for (var node : nodes) {
    if (node == entry) {
        dom.put(entry.getId(), Set.of(entry.getId()));
    } else {
        // All other nodes initially dominated by all nodes
        dom.put(node.getId(), new HashSet<>(allNodeIds));
    }
}
```

### 5.5 Block Merging Breaking Loops

**Pitfall**: Merging blocks in loop header
```java
// WRONG: Merging loop header into predecessor
if (canMerge(block, nextBlock)) {
    block.mergeNearBlock(nextBlock);
    // If nextBlock is loop header, loop detection breaks!
}
```

**Solution**: Preserve loop headers
```java
// CORRECT: Check for loop header before merging
if (canMerge(block, nextBlock) && !isLoopHeader(nextBlock)) {
    block.mergeNearBlock(nextBlock);
}
```

### 5.6 Empty Block Elimination Breaking Control Flow

**Pitfall**: Removing empty block without updating all references
```java
// WRONG: Removing empty block without redirecting jumps
if (block.isEmpty()) {
    cfg.removeNode(block);  // Jumps to this block are broken!
}
```

**Solution**: Redirect all jumps before removal
```java
// CORRECT: Redirect jumps then remove
if (block.isEmpty()) {
    var successors = block.getSuccessors();
    for (var pred : block.getPredecessors()) {
        pred.removeSuccessor(block);
        pred.getSuccessors().addAll(successors);
    }
    cfg.removeNode(block);
}
```

### 5.7 Incorrect CFG Visualization

**Pitfall**: Generating invalid graph syntax
```java
// WRONG: Invalid mermaid syntax
public String toString() {
    return "graph TD\n" +
           "  0 --> 1\n" +
           "  1 --> 2\n" +
           // Missing closing quotes on labels
           "  2[Exit]\n";  // Error: Unquoted text
}
```

**Solution**: Proper quoting of labels
```java
// CORRECT: Valid mermaid syntax
public String toString() {
    return "graph TD\n" +
           "  0 --> 1\n" +
           "  1 --> 2\n" +
           "  2[\"Exit\"]\n";  // Quoted label
}
```

### 5.8 Infinite Loop in CFG Traversal

**Pitfall**: Not tracking visited nodes during CFG construction
```java
// WRONG: Infinite loop in cyclic CFG
private void buildCFG(LinearIRBlock block) {
    var basicBlock = createBasicBlock(block);
    cfg.addBlock(basicBlock);

    for (var successor : block.getSuccessors()) {
        buildCFG(successor);  // May revisit same block infinitely!
    }
}
```

**Solution**: Track visited blocks
```java
// CORRECT: Track visited to avoid infinite loops
private void buildCFG(LinearIRBlock block, Set<Integer> visited) {
    if (visited.contains(block.getId())) return;

    visited.add(block.getId());
    var basicBlock = createBasicBlock(block);
    cfg.addBlock(basicBlock);

    for (var successor : block.getSuccessors()) {
        buildCFG(successor, visited);
    }
}
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Theory**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Chapter 9: Machine-Independent Optimizations
  - Section 9.6: Control-Flow Analysis

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 10: Data-Flow Analysis
  - Section 10.2: Redundancy Elimination

- **"Modern Compiler Implementation in Java"** by Andrew Appel
  - Chapter 18: Control Flow Graphs

**Graph Theory**:
- **"Introduction to Algorithms"** (CLRS) by Cormen et al.
  - Chapter 22: Elementary Graph Algorithms
  - Chapter 23: Minimum Spanning Trees

### 6.2 Online Resources

**Tutorials & Documentation**:
- [Control Flow Graph Wikipedia](https://en.wikipedia.org/wiki/Control-flow_graph)
- [Basic Block Wikipedia](https://en.wikipedia.org/wiki/Basic_block)
- [Dominance (Graph Theory) Wikipedia](https://en.wikipedia.org/wiki/Dominator_(graph_theory))

**Visualization Tools**:
- [Mermaid Live Editor](https://mermaid.live/)
- [Graphviz Online](https://dreampuf.github.io/GraphvizOnline/)
- [DOT Language Documentation](https://graphviz.org/doc/info/lang.html)

**Academic Resources**:
- [Stanford CS243: Program Analysis and Optimizations](https://web.stanford.edu/class/cs243/)
- [MIT 6.035: Computer Language Engineering](https://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-035-computer-language-engineering-spring-2016/)

### 6.3 Practice Problems

**Beginner**:
- Partition simple linear IR into basic blocks
- Build CFG for if-else statements
- Compute in-degree and out-degree for blocks

**Intermediate**:
- Build CFG for nested loops
- Implement empty block elimination
- Implement redundant jump elimination

**Advanced**:
- Implement dominance analysis
- Implement natural loop detection
- Build CFG optimizer with multiple passes

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `CFGBuilder`: Build CFG from linear IR
- `CFG.toString()`: Generate mermaid/DOT output
- `BasicBlock.dump()`: Visualize block contents

**External Tools**:
- **Graphviz**: Professional graph visualization and layout
- **Gephi**: Interactive graph exploration and analysis
- **NetworkX**: Python library for graph algorithms

### 6.5 Key EP20 Files to Study

**CFG Core**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFG.java` - CFG data structure
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilder.java` - CFG construction
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlock.java` - Basic block implementation

**Optimization**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java` - CFG optimization
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java` - Optimizer interface

**Linear IR**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LinearIRBlock.java` - Linear IR blocks
- `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/Prog.java` - Program container with optimization

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/CFGBuilderTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowGraphTest.java`

---

**Summary**: This chapter covers control flow graphs and basic blocks, focusing on CFG construction, basic block partitioning, dominance analysis, and CFG optimization. Mastering CFGs is essential for implementing dataflow analysis, optimization passes, and understanding program control flow structure.
