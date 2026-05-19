# Chapter 15: Local Optimization and Code Generation

**Module**: Module 4 - Intermediate Representation & Optimization (EP19-EP20)
**Target Reader**: Engineers with compiler experience, learning complete compiler
**Prerequisites**: CFG basics, optimization algorithms, IR knowledge (Chapter 13-14)
**EP Coverage**: EP20 (constant folding, dead code elimination, common subexpression elimination, code generation)
**Previous Chapter**: Chapter 14 (Control Flow Graph and Basic Blocks)
**Next Chapter**: Chapter 16 (End-to-End Compiler Pipeline)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Understand local optimization techniques** and their impact on code quality
- **Implement constant folding** to evaluate constant expressions at compile time
- **Implement dead code elimination** to remove unreachable and unused code
- **Implement common subexpression elimination** to reduce redundant computations
- **Design optimization Pass architecture** using visitor pattern
- **Generate target code** (EP18 VM bytecode) from optimized IR
- **Measure optimization effectiveness** through performance testing

**Core Deliverables**:
- Implement local optimization passes
- Implement code generator for EP18 VM
- Validate optimization correctness
- Measure performance improvements

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 14 (Control Flow Graph and Basic Blocks)
- ✅ Understanding of three-address code IR structure
- ✅ Knowledge of control flow analysis and CFG optimization
- ✅ Familiarity with EP18 VM instruction set

**Required Programming Skills**:
- ✅ Advanced Java: Visitor pattern, recursion, data structures
- ✅ Pattern matching and transformation algorithms
- ✅ Code generation and target architecture understanding
- ✅ Testing and validation techniques

**Compiler Theory Knowledge**:
- ✅ Dataflow analysis basics
- ✅ Optimization types (local vs global)
- ✅ Register allocation and instruction scheduling (optional)
- ✅ Understanding of bytecode and virtual machines

**Mathematical/Algorithmic Foundation**:
- ✅ Tree traversal and transformation
- ✅ Pattern matching algorithms
- ✅ Fixed-point iteration
- ✅ Understanding of computational complexity

---

## 3. Core Concepts to Master

### 3.1 Optimization Overview

**Optimization Types**:
- **Local Optimizations**: Applied within a single basic block
  - No cross-block analysis required
  - Fast and predictable
  - Examples: Constant folding, dead code elimination

- **Global Optimizations**: Applied across multiple blocks or entire function
  - Require dataflow analysis
  - More complex but more effective
  - Examples: Common subexpression elimination, loop invariant code motion

**EP20 Approach**:
- Focus on local optimizations for simplicity
- Uses CFG to identify optimization opportunities
- Applies optimizations in pass-based architecture

**Optimization Goals**:
1. **Correctness**: Must preserve program semantics
2. **Speed**: Must execute faster than original
3. **Size**: Should reduce code size (optional)
4. **Time**: Must not slow down compilation significantly

### 3.2 Constant Folding

**Definition**: Evaluate constant expressions at compile time instead of runtime.

**Examples**:
```c
// Source code
int x = 5 + 3 * 2;

// Without optimization
t0 = 3 * 2
x = 5 + t0

// With constant folding
x = 11  // 3 * 2 = 6, 5 + 6 = 11
```

**Constant Folding Rules**:
```java
if (lhs is ConstVal<T1> && rhs is ConstVal<T2>) {
    T1 leftValue = lhs.getValue();
    T2 rightValue = rhs.getValue();

    switch (op) {
        case ADD: return ConstVal.valueOf(leftValue + rightValue);
        case SUB: return ConstVal.valueOf(leftValue - rightValue);
        case MUL: return ConstVal.valueOf(leftValue * rightValue);
        case DIV: return ConstVal.valueOf(leftValue / rightValue);
        case LT:  return ConstVal.valueOf(leftValue < rightValue);
        case EQ:  return ConstVal.valueOf(leftValue == rightValue);
        // ... more operators
    }
}
```

**EP20 Implementation Pattern**:
```java
public class ConstantFoldingVisitor implements IRVisitor<IRNode, Void> {
    @Override
    public IRNode visit(BinExpr expr) {
        expr.getLhs().accept(this);
        expr.getRhs().accept(this);

        if (expr.getLhs() instanceof ConstVal<?> lhs &&
            expr.getRhs() instanceof ConstVal<?> rhs) {
            // Fold constant expression
            return foldConstant(expr.getOpType(), lhs, rhs);
        }

        return expr;  // Return unchanged if not foldable
    }

    private ConstVal<?> foldConstant(OperatorType.BinaryOpType op,
                                   ConstVal<?> lhs, ConstVal<?> rhs) {
        // Apply operator and return constant value
        // ...
    }
}
```

**Limitations**:
- Only works when all operands are constants
- Must handle type compatibility
- Must handle division by zero (compile-time check)
- Must respect integer overflow semantics

### 3.3 Dead Code Elimination

**Definition**: Remove code that has no effect on program output.

**Types of Dead Code**:

#### 3.3.1 Unreachable Code
Code that can never be executed due to control flow.

**Example**:
```c
if (1 == 1) {  // Always true
    x = 1;
} else {
    y = 2;  // Unreachable!
}

// Optimized to:
x = 1;
```

#### 3.3.2 Unused Variables/Assignments
Variables that are never read after being written.

**Example**:
```c
x = 5;  // x is never used
y = 10;
return y;

// Optimized to:
y = 10;
return y;
```

#### 3.3.3 Dead Stores
Assignments to variables that are immediately overwritten.

**Example**:
```c
x = 5;
x = 10;  // First assignment is dead
return x;

// Optimized to:
x = 10;
return x;
```

**EP20 Liveness Analysis**:
```java
public class LivenessAnalysis<I extends IRNode> {
    // For each block, compute liveOut (variables live at block exit)
    public void analyze(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            // Initialize liveOut = union of successors' liveIn
            var liveOut = new HashSet<Operand>();
            for (var succId : cfg.getSucceed(block.getId())) {
                var succ = cfg.getBlock(succId);
                liveOut.addAll(succ.getLiveIn());
            }
            block.setLiveOut(liveOut);

            // Compute liveIn = (liveOut - def) ∪ use
            var use = block.getLiveUse();
            var def = block.getDef();
            var liveIn = new HashSet<Operand>(liveOut);
            liveIn.removeAll(def);
            liveIn.addAll(use);
            block.setLiveIn(liveIn);
        }
    }
}
```

**Dead Code Elimination Algorithm**:
```java
public class DeadCodeElimination implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        // Step 1: Compute liveness for all blocks
        var liveness = new LivenessAnalysis<>();
        liveness.analyze(cfg);

        // Step 2: Remove dead assignments
        for (var block : cfg.nodes) {
            var liveOut = block.getLiveOut();
            removeDeadAssignments(block, liveOut);
        }
    }

    private void removeDeadAssignments(BasicBlock<I> block, Set<Operand> liveOut) {
        var toRemove = new ArrayList<IRNode>();

        // Backward scan through block
        for (int i = block.codes.size() - 1; i >= 0; i--) {
            var instr = block.codes.get(i);

            if (instr instanceof Assign assign) {
                var lhs = assign.getLhs();

                // If lhs not in liveOut, assignment is dead
                if (!liveOut.contains(lhs)) {
                    toRemove.add(instr);
                }

                // Update liveOut: add rhs, remove lhs
                liveOut.remove(lhs);
                if (assign.getRhs() instanceof Operand rhs) {
                    liveOut.add(rhs);
                }
            }
        }

        // Remove dead assignments
        block.codes.removeAll(toRemove);
    }
}
```

### 3.4 Common Subexpression Elimination (CSE)

**Definition**: Replace redundant computation with a single result.

**Example**:
```c
// Original code
x = a + b;
y = a + b;  // Redundant computation!

// After CSE
t0 = a + b;
x = t0;
y = t0;
```

**CSE Algorithm (Local)**:
1. For each expression in basic block:
   - Compute expression signature (operator + operands)
   - Check if signature seen before
   - If seen, replace with temporary variable
   - If not seen, compute and store result in temporary

**EP20 Implementation Pattern**:
```java
public class CommonSubexpressionElimination implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            eliminateCommonSubexpressions(block);
        }
    }

    private void eliminateCommonSubexpressions(BasicBlock<I> block) {
        var exprMap = new HashMap<String, OperandSlot>();  // Expression signature → temporary
        var replacements = new HashMap<IRNode, IRNode>();

        for (var code : block.codes) {
            if (code.instr instanceof BinExpr binExpr) {
                var signature = binExpr.getOpType().toString() + ":" +
                              binExpr.getLhs() + ":" +
                              binExpr.getRhs();

                if (exprMap.containsKey(signature)) {
                    // Replace with existing temporary
                    replacements.put(binExpr, exprMap.get(signature));
                } else {
                    // Compute and store new temporary
                    var temp = OperandSlot.pushStack();
                    exprMap.put(signature, temp);
                }
            }
        }

        // Apply replacements
        for (int i = 0; i < block.codes.size(); i++) {
            var code = block.codes.get(i);
            if (replacements.containsKey(code.instr)) {
                block.codes.set(i, new Loc<>(replacements.get(code.instr), code.location));
            }
        }
    }
}
```

**Limitations**:
- Only works within basic blocks (local CSE)
- Cannot handle cross-block common subexpressions
- Must ensure operand values unchanged between occurrences

### 3.5 Copy Propagation

**Definition**: Replace variable with its assigned value when safe.

**Example**:
```c
// Original code
x = y;
z = x + 1;

// After copy propagation
z = y + 1;  // x replaced with y
```

**Copy Propagation Algorithm**:
1. Track copy statements (x = y)
2. Replace uses of x with y
3. Ensure y is not modified between copy and use

**EP20 Implementation Pattern**:
```java
public class CopyPropagation implements IFlowOptimizer<I> {
    @Override
    public void onHandle(CFG<I> cfg) {
        for (var block : cfg.nodes) {
            propagateCopies(block);
        }
    }

    private void propagateCopies(BasicBlock<I> block) {
        var copyMap = new HashMap<VarSlot, VarSlot>();

        for (var code : block.codes) {
            if (code.instr instanceof Assign assign) {
                if (assign.getRhs() instanceof VarSlot rhs) {
                    // Copy statement: x = y
                    copyMap.put(assign.getLhs(), rhs);
                } else {
                    // Non-copy: invalidate lhs in copyMap
                    copyMap.remove(assign.getLhs());
                }
            }

            // Replace variables according to copyMap
            replaceVariables(code.instr, copyMap);
        }
    }

    private void replaceVariables(IRNode instr, Map<VarSlot, VarSlot> copyMap) {
        // Recursively replace VarSlot references
        if (instr instanceof VarSlot var) {
            if (copyMap.containsKey(var)) {
                // Replace with copied value
                return copyMap.get(var);
            }
        }
        // ... handle other node types
    }
}
```

### 3.6 Optimization Pass Architecture

**EP20 Pass Interface**:
```java
public interface IFlowOptimizer<I extends IRNode> {
    void onHandle(CFG<I> cfg);
}
```

**Pass Manager**:
```java
public class PassManager {
    private List<IFlowOptimizer<IRNode>> optimizers = new ArrayList<>();

    public void addOptimizer(IFlowOptimizer<IRNode> optimizer) {
        optimizers.add(optimizer);
    }

    public void applyOptimizers(CFG<IRNode> cfg) {
        for (var optimizer : optimizers) {
            optimizer.onHandle(cfg);
        }
    }
}
```

**EP20 Compiler Integration**:
```java
// In Compiler.java
cfg.addOptimizer(new ControlFlowAnalysis<>());
cfg.addOptimizer(new ConstantFolding());
cfg.addOptimizer(new DeadCodeElimination());
cfg.addOptimizer(new CommonSubexpressionElimination());

cfg.applyOptimizers();
```

**Pass Ordering**:
1. **Constant folding**: Early pass, simplifies expressions
2. **Dead code elimination**: Remove obvious dead code
3. **Common subexpression elimination**: Requires folded constants
4. **Copy propagation**: Further simplifies code
5. **Dead code elimination (again)**: Remove newly dead code

**Iteration**: Some passes need multiple iterations to reach fixed point.

### 3.7 Code Generation to EP18 VM

**EP18 VM Instruction Set**:
```
Stack Operations:
  push <value>    - Push constant or variable onto stack
  pop             - Pop top value from stack
  load <slot>      - Load variable from stack frame slot
  store <slot>     - Store top value to stack frame slot

Arithmetic:
  iadd, isub, imul, idiv  - Integer arithmetic
  fadd, fsub, fmul, fdiv  - Float arithmetic
  lt, gt, eq, ne           - Comparisons

Control Flow:
  br <label>      - Unconditional branch
  brf <label>     - Branch if false (pop value)
  call <func>      - Function call
  ret             - Return from function
  halt            - Stop execution

Built-in Functions:
  print           - Print top of stack
```

**EP20 CymbolAssembler Implementation**:

```java
public class CymbolAssembler implements IRVisitor<Void, Void> {
    private LinkedList<String> assembleCmdBuffer = new LinkedList<>();
    private int indents = 0;

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

    // Binary expressions
    @Override
    public Void visit(BinExpr node) {
        node.getLhs().accept(this);
        node.getRhs().accept(this);

        switch (node.getOpType()) {
            case ADD: emit("iadd"); break;
            case SUB: emit("isub"); break;
            case MUL: emit("imul"); break;
            case DIV: emit("idiv"); break;
            case LT:  emit("lt"); break;
            case GT:  emit("gt"); break;
            case EQ:  emit("eq"); break;
            case NE:  emit("ne"); break;
        }
        return null;
    }

    // Unary expressions
    @Override
    public Void visit(UnaryExpr node) {
        node.expr.accept(this);

        switch (node.op) {
            case NEG: emit("ineg"); break;
            case NOT: emit("not"); break;
        }
        return null;
    }

    // Constants
    @Override
    public <T> Void visit(ConstVal<T> constVal) {
        var value = constVal.getVal();
        if (value instanceof Integer i) {
            emit("iconst %d".formatted(i));
        } else if (value instanceof Float f) {
            emit("fconst %f".formatted(f));
        } else if (value instanceof Boolean b) {
            emit("bconst %d".formatted(b ? 1 : 0));
        } else if (value instanceof String s) {
            emit("sconst \"%s\"".formatted(s));
        }
        return null;
    }

    // Variables
    @Override
    public Void visit(FrameSlot frameSlot) {
        emit("load %d".formatted(frameSlot.getSlotIdx()));
        return null;
    }

    // Assignment
    @Override
    public Void visit(Assign assign) {
        assign.getRhs().accept(this);

        if (assign.getLhs() instanceof FrameSlot frameSlot) {
            emit("store %d".formatted(frameSlot.getSlotIdx()));
        }
        return null;
    }

    // Labels
    @Override
    public Void visit(Label label) {
        if (indents > 0) indents--;

        if (label instanceof FuncEntryLabel) {
            emit("%s".formatted(label.toSource()));
        } else {
            emit("%s:".formatted(label.toSource()));
        }
        indents++;
        return null;
    }

    // Jumps
    @Override
    public Void visit(JMP jmp) {
        emit("br %s".formatted(jmp.getNext().toString()));
        indents--;
        return null;
    }

    @Override
    public Void visit(CJMP cjmp) {
        emit("brf %s".formatted(cjmp.getElseBlock().getLabel().toString()));
        indents--;
        return null;
    }

    // Function calls
    @Override
    public Void visit(CallFunc callFunc) {
        if (!callFunc.getFuncType().isBuiltIn()) {
            emit("call %s()".formatted(callFunc.getFuncName()));
        } else {
            emit("%s".formatted(callFunc.getFuncName()));
        }
        return null;
    }

    // Return
    @Override
    public Void visit(ReturnVal returnVal) {
        if (returnVal.getRetVal() != null) {
            returnVal.getRetVal().accept(this);
        }

        if (returnVal.isMainEntry()) {
            emit("halt");
        } else {
            emit("ret");
        }
        indents--;
        return null;
    }

    private void emit(String cmd) {
        var indentCmdBuf = "    ".repeat(indents) + cmd;
        assembleCmdBuffer.add(indentCmdBuf);
    }

    public String getAsmInfo() {
        return String.join("\n", assembleCmdBuffer).concat("\n");
    }
}
```

### 3.8 Code Generation Example

**IR Code**:
```
main:
L0: @0 = 5
    @1 = 10
    t0 = @0 + @1
    @2 = t0
    print @2
    return

```

**Generated EP18 VM Bytecode**:
```
main
    L0:
        iconst 5
        store 0
        iconst 10
        store 1
        load 0
        load 1
        iadd
        store 2
        load 2
        print
        ret
        halt
```

### 3.9 Optimization Verification

**Semantic Equivalence Testing**:
```java
@Test
void testConstantFoldingPreservesSemantics() {
    // Compile and optimize
    var optimizedIR = compileAndOptimize("int x = 5 + 3 * 2;");
    var unoptimizedIR = compile("int x = 5 + 3 * 2;");

    // Generate bytecode
    var optimizedBytecode = generateBytecode(optimizedIR);
    var unoptimizedBytecode = generateBytecode(unoptimizedIR);

    // Execute and compare results
    var optimizedResult = execute(optimizedBytecode);
    var unoptimizedResult = execute(unoptimizedBytecode);

    assertThat(optimizedResult).isEqualTo(unoptimizedResult);
}
```

**Performance Testing**:
```java
@Test
void testOptimizationImprovesPerformance() {
    var sourceCode = """
        int factorial(int n) {
            if (n <= 1) return 1;
            return n * factorial(n - 1);
        }

        void main() {
            print(factorial(10));
        }
        """;

    // Measure unoptimized performance
    long unoptimizedTime = measureExecutionTime(sourceCode, false);

    // Measure optimized performance
    long optimizedTime = measureExecutionTime(sourceCode, true);

    // Optimized should be faster (or equal)
    assertThat(optimizedTime).isLessThanOrEqualTo(unoptimizedTime * 1.1);
}
```

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Implement Constant Folding**
Given IR:
```
t0 = 5
t1 = 10
t2 = t0 + t1
```

**Tasks**:
- Implement constant folding visitor
- Fold `t0 + t1` to constant 15
- Update IR to `t2 = 15`
- Write unit tests for arithmetic operators

**Exercise 2: Identify Dead Code**
Given IR:
```
t0 = 5      // Unused
t1 = 10     // Used
t2 = t1 + 1  // Uses t1
return t2
```

**Tasks**:
- Implement liveness analysis
- Identify `t0 = 5` as dead assignment
- Remove dead assignment from IR
- Validate correctness with execution tests

**Exercise 3: Implement Assignment Emission**
Given IR assignment:
```
@0 = 5
```

**Tasks**:
- Implement `visit(Assign)` in assembler
- Emit `iconst 5` then `store 0`
- Handle both constants and variables on RHS
- Test with various assignment patterns

### 4.2 Intermediate Exercises

**Exercise 4: Implement Common Subexpression Elimination**
Given IR:
```
t0 = a + b
t1 = a + b  // Redundant!
x = t0
y = t1
```

**Tasks**:
- Track expression signatures
- Replace second `a + b` with `t0`
- Update IR to eliminate redundant computation
- Verify output correctness

**Exercise 5: Implement Function Call Code Generation**
Given IR:
```
push a
push b
call add()
pop @result
```

**Tasks**:
- Implement `visit(CallFunc)` in assembler
- Emit `call add()` for user-defined functions
- Handle built-in functions (`print`, `read`)
- Manage return value handling

**Exercise 6: Implement Control Flow Code Generation**
Given IR:
```
L0: t0 = x > 10
    if t0 goto L2
    y = 0
    goto L3
L1: y = 1
L2: return y
```

**Tasks**:
- Emit conditional jump with `brf`
- Emit unconditional jumps with `br`
- Handle label emission
- Verify control flow correctness

### 4.3 Advanced Exercises

**Exercise 7: Implement Copy Propagation**
Given IR:
```
x = y
z = x + 1
```

**Tasks**:
- Track copy statements (x = y)
- Replace `x` with `y` in subsequent uses
- Update IR to `z = y + 1`
- Handle copy invalidation when y is modified

**Exercise 8: Implement Dead Code Elimination with Liveness**
Given IR:
```
x = 1
y = 2
x = 3  // Dead: x overwritten
return y
```

**Tasks**:
- Compute live variables at each point
- Identify dead assignments
- Remove dead assignments from IR
- Verify liveness correctness

**Exercise 9: Build Complete Optimizer Pipeline**
- Implement multiple optimization passes:
  - Constant folding
  - Dead code elimination
  - Common subexpression elimination
  - Copy propagation
- Apply passes in correct order
- Iterate until fixed point
- Validate optimizations preserve semantics

**Exercise 10: Optimize Real Cymbol Program**
Given program:
```c
int fibonacci(int n) {
    if (n <= 1) return 1;
    return fibonacci(n - 1) + fibonacci(n - 2);
}

void main() {
    print(fibonacci(10));
}
```

**Tasks**:
- Compile to IR
- Apply all optimization passes
- Generate optimized bytecode
- Measure performance improvement
- Compare code size

---

## 5. Common Pitfalls

### 5.1 Incorrect Constant Folding

**Pitfall**: Folding overflow or division by zero
```java
// WRONG: No overflow checking
public ConstVal<Integer> foldAdd(ConstVal<Integer> lhs, ConstVal<Integer> rhs) {
    return ConstVal.valueOf(lhs.getValue() + rhs.getValue());  // May overflow!
}
```

**Solution**: Handle overflow correctly
```java
// CORRECT: Use checked arithmetic or document behavior
public ConstVal<Integer> foldAdd(ConstVal<Integer> lhs, ConstVal<Integer> rhs) {
    var result = lhs.getValue() + rhs.getValue();
    // Check for overflow (optional, depending on language semantics)
    return ConstVal.valueOf(result);
}
```

### 5.2 Incorrect Dead Code Detection

**Pitfall**: Removing assignments that affect side effects
```java
// WRONG: Removing all unused assignments
if (!liveOut.contains(lhs)) {
    removeAssignment(assign);  // May remove side-effecting calls!
}
```

**Solution**: Check for side effects
```java
// CORRECT: Preserve side-effecting code
if (!liveOut.contains(lhs) && !hasSideEffects(assign.getRhs())) {
    removeAssignment(assign);
}
```

### 5.3 CSE Breaking Program Semantics

**Pitfall**: Replacing expression that has side effects
```java
// WRONG: CSE without checking side effects
if (exprMap.containsKey(signature)) {
    replacements.put(binExpr, exprMap.get(signature));
    // What if operands have side effects (e.g., function calls)?
}
```

**Solution**: Only CSE pure expressions
```java
// CORRECT: Check for side effects
if (exprMap.containsKey(signature) && isPureExpression(binExpr)) {
    replacements.put(binExpr, exprMap.get(signature));
}
```

### 5.4 Incorrect Copy Propagation

**Pitfall**: Propagating copy across modification of source
```java
// WRONG: Not tracking variable modifications
x = y
z = x + 1
y = 10    // y modified!
w = x + 2  // Cannot propagate x → y here
```

**Solution**: Invalidate copies on modification
```java
// CORRECT: Track modifications
if (assign.getLhs().equals(y)) {
    copyMap.remove(x);  // Invalidate copy x = y
}
```

### 5.5 Incorrect Code Generation for Jumps

**Pitfall**: Wrong jump target format
```java
// WRONG: Incorrect label format
@Override
public Void visit(JMP jmp) {
    emit("br %s".formatted(jmp.getNext().toString()));  // May output wrong format
    return null;
}
```

**Solution**: Use correct label format
```java
// CORRECT: Get label name properly
@Override
public Void visit(JMP jmp) {
    var label = jmp.getNext().getLabel().toSource();
    emit("br %s".formatted(label));
    return null;
}
```

### 5.6 Stack Imbalance in Code Generation

**Pitfall**: Incorrect stack operations
```java
// WRONG: Stack imbalance
public Void visit(BinExpr node) {
    node.getLhs().accept(this);   // Pushes 1 value
    node.getRhs().accept(this);   // Pushes 1 value
    emit("iadd");                // Pops 2, pushes 1
    // Stack: 1 value (correct)
    emit("pop");                 // Pops 1 value
    // Stack: 0 values (may be wrong!)
    return null;
}
```

**Solution**: Track stack balance carefully
```java
// CORRECT: Verify stack operations
public Void visit(BinExpr node) {
    node.getLhs().accept(this);   // Stack: +1
    node.getRhs().accept(this);   // Stack: +1
    emit("iadd");                // Stack: -2+1 = -1
    // Stack: 1 value (correct)
    return null;
}
```

### 5.7 Optimization Pass Ordering

**Pitfall**: Passes in wrong order
```java
// WRONG: DCE before CSE
optimizers.add(new DeadCodeElimination());  // Removes unused temporaries
optimizers.add(new CommonSubexpressionElimination());  // Can't find CSE opportunities!
```

**Solution**: Order passes correctly
```java
// CORRECT: CSE before DCE
optimizers.add(new ConstantFolding());           // First
optimizers.add(new CommonSubexpressionElimination());  // Second
optimizers.add(new CopyPropagation());         // Third
optimizers.add(new DeadCodeElimination());      // Last (removes newly dead code)
```

### 5.8 Fixed-Point Iteration Issues

**Pitfall**: Not iterating to fixed point
```java
// WRONG: Single iteration
optimizers.forEach(optimizer -> optimizer.onHandle(cfg));
```

**Solution**: Iterate until fixed point
```java
// CORRECT: Fixed-point iteration
boolean changed;
do {
    changed = false;
    for (var optimizer : optimizers) {
        var before = cfg.toString();
        optimizer.onHandle(cfg);
        var after = cfg.toString();
        if (!before.equals(after)) {
            changed = true;
        }
    }
} while (changed);
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Optimization**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Chapter 9: Machine-Independent Optimizations
  - Section 9.1: The Principal Sources of Optimization

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 8: Optimizations for Locality
  - Chapter 10: Data-Flow Analysis

- **"Optimizing Compilers for Modern Architectures"** by Muchnick
  - Comprehensive coverage of optimization techniques

**Code Generation**:
- **"The Design and Evolution of C++"** by Stroustrup
  - Chapter on code generation strategies

- **"LLVM Compiler Infrastructure"** (Lattner et al.)
  - [LLVM Code Generation Documentation](https://llvm.org/docs/CodeGenerator.html)

### 6.2 Online Resources

**Tutorials & Documentation**:
- [Compiler Optimization Wikipedia](https://en.wikipedia.org/wiki/Optimizing_compiler)
- [Dead Code Elimination Wikipedia](https://en.wikipedia.org/wiki/Dead_code_elimination)
- [Common Subexpression Elimination Wikipedia](https://en.wikipedia.org/wiki/Common_subexpression_elimination)

**Optimization Techniques**:
- [GCC Optimization Flags](https://gcc.gnu.org/onlinedocs/gcc/Optimize-Options.html)
- [LLVM Optimization Passes](https://llvm.org/docs/Passes.html)

**Virtual Machines**:
- [EP18 VM Instruction Set Reference](../../ep18/README.md)
- [Stack-Based vs Register-Based VMs](https://en.wikipedia.org/wiki/Stack-oriented_programming)

### 6.3 Practice Problems

**Beginner**:
- Implement constant folding for arithmetic operators
- Implement dead code elimination for simple cases
- Generate bytecode for basic expressions

**Intermediate**:
- Implement common subexpression elimination
- Implement copy propagation
- Generate bytecode for control flow statements

**Advanced**:
- Implement loop invariant code motion
- Implement strength reduction
- Build complete optimizer pipeline with multiple passes

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `CymbolAssembler`: Generate bytecode from IR
- `ControlFlowAnalysis`: Optimize CFG
- `Prog.optimizeBasicBlock()`: Basic block optimization
- `CFG.toString()`: Visualize optimization results

**External Tools**:
- **GDB**: Debug bytecode execution
- **VM Profiler**: Measure execution time
- **Diff Tools**: Compare optimized vs unoptimized output

### 6.5 Key EP20 Files to Study

**Optimization**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/ControlFlowAnalysis.java` - CFG optimization
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/LivenessAnalysis.java` - Liveness analysis
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/IFlowOptimizer.java` - Optimizer interface

**Code Generation**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java` - IR to VM bytecode
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolVMIOperatorEmitter.java` - Operator emission

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/EndToEndCompilationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/VMInstructionTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/cfg/BasicBlockOptimizationTest.java`

---

**Summary**: This chapter covers local optimization techniques and code generation, focusing on constant folding, dead code elimination, common subexpression elimination, and generation of EP18 VM bytecode. Mastering optimization and code generation is essential for building efficient, production-quality compilers.
