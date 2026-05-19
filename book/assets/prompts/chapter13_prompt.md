# Chapter 13: Intermediate Representation (IR) Design

**Module**: Module 4 - Intermediate Representation & Optimization (EP19-EP20)
**Target Reader**: Engineers who have implemented compilers, learning backend techniques
**Prerequisites**: Compiler backend basics, three-address code concepts
**EP Coverage**: EP19 (pipeline foundation) → EP20 (IR node design, MIR/LIR layering)
**Previous Chapter**: Chapter 12 (Type System & Type Checking)
**Next Chapter**: Chapter 14 (Control Flow Graph and Basic Blocks)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Design and implement** an intermediate representation (IR) for a programming language compiler
- **Understand the role** of IR in the compilation pipeline and why it's essential for optimization
- **Master three-address code** design principles and implementation
- **Implement a hierarchical IR node system** that supports efficient traversal and transformation
- **Separate Machine-Independent IR (MIR)** from Machine-Dependent IR (LIR) through proper abstraction
- **Build an IR generator** that converts AST to three-address code format
- **Design IR visitors** for code generation and analysis passes

**Core Deliverables**:
- Design and implement a three-address code IR system
- Create an IR builder that transforms AST to IR
- Implement IR node visitors for code generation
- Validate IR correctness through unit tests

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 12 (Type System & Type Checking)
- ✅ Understanding of abstract syntax trees (AST) and visitor pattern
- ✅ Familiarity with basic compiler architecture (frontend → backend)
- ✅ Knowledge of stack-based virtual machines (EP18)

**Required Programming Skills**:
- ✅ Advanced Java: Generics, abstract classes, visitor pattern
- ✅ Design patterns: Visitor, Factory, Builder
- ✅ Data structures: Trees, graphs, linked lists
- ✅ ANTLR4: ParseTree traversal and visitor implementation

**Compiler Theory Knowledge**:
- ✅ Basic understanding of three-address code (x = y op z)
- ✅ Familiarity with control flow statements and expressions
- ✅ Knowledge of temporary variable allocation
- ✅ Understanding of instruction operands (constants, variables, temporaries)

**Mathematical/Algorithmic Foundation**:
- ✅ Tree traversal algorithms (DFS, BFS)
- ✅ Graph theory basics (nodes, edges, traversal)
- ✅ Understanding of stack data structures

---

## 3. Core Concepts to Master

### 3.1 Intermediate Representation (IR) Fundamentals

**What is IR?**
IR is a machine-independent, language-agnostic representation of program code that serves as an intermediate layer between the high-level source code and the low-level target code.

**Why do we need IR?**
1. **Decoupling**: Separates frontend (source-dependent) from backend (target-dependent)
2. **Optimization**: Provides a structured representation for analysis and transformation
3. **Portability**: Single frontend can target multiple backends
4. **Analysis**: Enables dataflow analysis, control flow analysis, etc.

**IR Design Principles**:
- **Complete**: Must represent all language features
- **Precise**: No loss of information during transformation
- **Efficient**: Enables fast traversal and transformation
- **Simple**: Easier to analyze than source or target code

### 3.2 Three-Address Code

**Definition**: Three-address code is a form of IR where each instruction has at most three operands: typically two source operands and one destination operand.

**Structure**: `result = operand1 operator operand2`

**Key Characteristics**:
- **At most one operator per instruction**
- **At most three operands per instruction**
- **Temporaries used for intermediate results**
- **Instructions are linear (tree-like structure flattened)**

**Examples**:

| Source Code | Three-Address Code |
|-------------|-------------------|
| `a = b + c * d` | `t1 = c * d`<br>`a = b + t1` |
| `if (x < y) a = b else a = c` | `t1 = x < y`<br>`if t1 goto L1`<br>`a = c`<br>`goto L2`<br>`L1: a = b`<br>`L2:` |
| `return x + y` | `t1 = x + y`<br>`return t1` |

### 3.3 IR Node Hierarchy Design

**EP20 IR Architecture**:

```
IRNode (abstract base)
├── Expr (expressions that produce values)
│   ├── ConstVal<T> (constant values: int, float, string, bool)
│   ├── BinExpr (binary expressions: +, -, *, /, <, >, ==)
│   ├── UnaryExpr (unary expressions: -, !)
│   ├── CallFunc (function calls)
│   └── VarSlot (variables - abstract)
│       ├── FrameSlot (stack frame variables)
│       └── OperandSlot (temporary operands)
│
└── Stmt (statements - may or may not produce values)
    ├── Assign (assignment: lhs = rhs)
    ├── JMP (unconditional jump)
    ├── CJMP (conditional jump)
    ├── Label (label for jump targets)
    ├── FuncEntryLabel (function entry point)
    ├── ReturnVal (return statement)
    └── ExprStmt (expression as statement)
```

**Design Rationale**:
- **Separation of concerns**: Expr vs Stmt clearly distinguishes values from actions
- **Polymorphism**: Visitor pattern enables different operations (generation, analysis, optimization)
- **Extensibility**: Easy to add new instruction types
- **Type safety**: Generic ConstVal<T> ensures type correctness

### 3.4 MIR vs LIR Layering

**MIR (Machine-Independent IR)**:
- Close to source language semantics
- Abstract away target-specific details
- Example: Generic memory operations (load, store)
- Used for high-level optimizations

**LIR (Machine-Dependent IR)**:
- Closer to target architecture
- Exposes machine-specific details
- Example: Specific register allocation, addressing modes
- Used for low-level optimizations and code generation

**EP20 Approach**:
- Uses unified IR design with layering through abstraction
- FrameSlot and OperandSlot provide MIR abstraction
- CymbolAssembler translates to LIR (EP18 VM bytecode)

### 3.5 Temporary Variable Allocation

**Purpose**: Store intermediate computation results

**Allocation Strategy**:
```java
// EP20 uses OperandSlot for temporaries
public class OperandSlot extends VarSlot {
    private static int ordSeq = -1;  // Sequence number

    public static OperandSlot pushStack() {
        ordSeq++;
        return new OperandSlot(ordSeq);
    }

    public static void popStack() {
        ordSeq--;
    }

    public static OperandSlot genTemp() {
        return new OperandSlot(ordSeq + 1);
    }
}
```

**Stack-based vs Register-based**:
- **Stack-based**: Temporaries pushed/popped from stack (EP20 approach)
- **Register-based**: Temporaries mapped to virtual registers (EP21 SSA approach)

### 3.6 IR Builder Implementation

**Key Responsibilities**:
1. **AST traversal**: Visit each AST node using visitor pattern
2. **IR generation**: Create corresponding IR nodes
3. **Control flow management**: Handle jumps, labels, basic blocks
4. **Temporary allocation**: Manage OperandSlot push/pop for expressions
5. **Block construction**: Organize IR nodes into LinearIRBlock

**EP20 Implementation Pattern**:
```java
public class CymbolIRBuilder implements ASTVisitor<Void, VarSlot> {
    private LinearIRBlock currentBlock;
    private Stack<VarSlot> evalExprStack;

    @Override
    public VarSlot visit(BinaryExprNode node) {
        node.getLhs().accept(this);  // Evaluate left
        var lhs = peekEvalOperand();

        node.getRhs().accept(this);  // Evaluate right
        var rhs = peekEvalOperand();

        // Generate binary expression IR
        var res = addInstr(BinExpr.with(opType, lhs, rhs));
        res.ifPresent(this::pushEvalOperand);

        return null;
    }
}
```

### 3.7 IR Visitor Pattern

**Purpose**: Enable multiple operations on IR without modifying node classes

**Interface**:
```java
public interface IRVisitor<S, E> {
    S visit(BinExpr node);
    S visit(UnaryExpr node);
    S visit(Assign node);
    S visit(JMP node);
    S visit(CJMP node);
    S visit(CallFunc node);
    // ... more visit methods
}
```

**Visitor Types**:
1. **Code Generation Visitor**: IR → Target code (CymbolAssembler)
2. **Analysis Visitor**: Collect information (liveness analysis)
3. **Optimization Visitor**: Transform IR (constant folding)

### 3.8 LinearIRBlock Design

**Purpose**: Group IR nodes into linear sequences between control flow boundaries

**Characteristics**:
- Linear sequence of IR statements
- Single entry point
- Can have multiple exit points (via jumps)
- Forms basic blocks when combined with CFG

**EP20 Implementation**:
```java
public class LinearIRBlock {
    private List<IRNode> stmts;
    private LinearIRBlock next;  // Fall-through successor
    private Scope scope;

    public void addStmt(IRNode stmt) {
        stmts.add(stmt);
    }

    public void setLink(LinearIRBlock block) {
        this.next = block;
    }
}
```

### 3.9 IR Design Patterns

**Factory Pattern**:
```java
// Static factory methods for IR creation
public static Assign with(VarSlot lhs, Operand rhs) {
    return new Assign(lhs, rhs);
}

public static BinExpr with(OperatorType op, VarSlot lhs, VarSlot rhs) {
    return new BinExpr(op, lhs, rhs);
}
```

**Visitor Pattern**: Decouples operations from IR structure
**Builder Pattern**: CymbolIRBuilder constructs complex IR graphs
**Stack Pattern**: evalExprStack manages expression evaluation order

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Implement Basic IR Nodes**
- Create `ConstVal<T>` class for integer, float, string, and boolean constants
- Implement `toString()` method for debugging
- Add visitor accept() method
- Write unit tests verifying constant creation and value retrieval

**Exercise 2: Implement Assignment IR**
- Create `Assign` class with `lhs` (VarSlot) and `rhs` (Operand) fields
- Implement factory method `Assign.with(lhs, rhs)`
- Add visitor pattern support
- Write tests for assignment creation and representation

**Exercise 3: Implement Binary Expression IR**
- Create `BinExpr` class with operator type and operands
- Support operators: ADD, SUB, MUL, DIV, LT, GT, EQ, NE
- Implement factory method and visitor support
- Write tests for different binary expressions

### 4.2 Intermediate Exercises

**Exercise 4: Build Expression-to-IR Translator**
Given source code:
```c
int x = 5 + 3 * 2;
```

Generate IR:
```
t0 = 3 * 2
@x = 5 + t0
```

**Tasks**:
- Parse expression and build AST
- Implement IRBuilder for binary expressions
- Manage temporary variable allocation using stack
- Validate generated IR matches expected output

**Exercise 5: Implement Control Flow IR**
Given source code:
```c
if (x > 10) {
    y = 1;
} else {
    y = 0;
}
```

Generate IR:
```
L0: t0 = x > 10
     if t0 goto L1
     y = 0
     goto L2
L1: y = 1
L2:
```

**Tasks**:
- Implement `CJMP` (conditional jump) instruction
- Implement `Label` instruction for jump targets
- Create basic blocks for then/else branches
- Manage label creation and references

**Exercise 6: Implement IR Visitor for Code Generation**
- Create `SimpleAssembler` visitor that prints IR in text format
- Visit each IR node type and emit text representation
- Handle temporary variable naming (t0, t1, t2, ...)
- Test with complex expressions and control flow

### 4.3 Advanced Exercises

**Exercise 7: Implement IR Builder for Function Calls**
Given source code:
```c
int result = add(a, b);
```

Generate IR:
```
push a
push b
call add()
pop @result
```

**Tasks**:
- Implement `CallFunc` IR instruction
- Handle argument passing (push to stack)
- Manage return value handling
- Support both void and non-void functions

**Exercise 8: Implement Array Access IR**
Given source code:
```c
arr[i] = 10;
```

Generate IR:
```
t0 = i * 4  // Assuming int size = 4
t1 = arr + t0
store [t1] = 10
```

**Tasks**:
- Design IR for array indexing (memory addressing)
- Implement address computation (base + offset * size)
- Handle array access in both L-value and R-value contexts
- Add bounds checking IR (optional)

**Exercise 9: Optimize IR with Constant Folding**
Given IR:
```
t0 = 5 + 3
t1 = t0 * 2
x = t1
```

Optimize to:
```
x = 16
```

**Tasks**:
- Implement visitor that identifies constant expressions
- Evaluate constant expressions at compile time
- Replace constant expressions with computed values
- Remove dead temporaries

**Exercise 10: Build Complete IR Generator**
- Implement full CymbolIRBuilder for all AST node types
- Support variables, expressions, statements, functions
- Handle control flow (if, while, break, continue)
- Manage block structure and labels
- Generate IR for complete Cymbol programs

---

## 5. Common Pitfalls

### 5.1 Temporary Variable Management

**Pitfall**: Incorrect stack management for temporaries
```java
// WRONG: Stack underflow/overflow
public VarSlot visit(BinaryExprNode node) {
    node.getLhs().accept(this);
    var lhs = popEvalOperand();  // Pops but doesn't track

    node.getRhs().accept(this);
    var rhs = popEvalOperand();  // May cause underflow

    var result = BinExpr.with(op, lhs, rhs);
    pushEvalOperand(result);      // Unclear when to push
    return result;
}
```

**Solution**: Clear push/pop discipline
```java
// CORRECT: Clear stack operations
public VarSlot visit(BinaryExprNode node) {
    node.getLhs().accept(this);
    var lhs = peekEvalOperand();  // Peek, don't pop yet

    node.getRhs().accept(this);
    var rhs = peekEvalOperand();

    var result = addInstr(BinExpr.with(op, lhs, rhs));
    result.ifPresent(this::pushEvalOperand);  // Only push if new temp created

    popEvalOperand();  // Pop rhs
    popEvalOperand();  // Pop lhs
    return null;
}
```

**Key Rule**: Every push must have a corresponding pop

### 5.2 Control Flow Label Management

**Pitfall**: Label name collisions or missing labels
```java
// WRONG: No unique label naming
public void visit(IfStmtNode node) {
    jumpIf(cond, thenBlock, elseBlock);
    // Missing jump to end - control flow falls through incorrectly
}
```

**Solution**: Explicit control flow management
```java
// CORRECT: Explicit labels and jumps
public void visit(IfStmtNode node) {
    var thenBlock = new LinearIRBlock();
    var elseBlock = new LinearIRBlock();
    var endBlock = new LinearIRBlock();  // End label

    jumpIf(cond, thenBlock, elseBlock);

    setCurrentBlock(thenBlock);
    node.getThenBlock().accept(this);
    jump(endBlock);  // Jump to end after then

    setCurrentBlock(elseBlock);
    node.getElseBlock().ifPresent(x -> x.accept(this));
    // Fall through to endBlock

    setCurrentBlock(endBlock);
}
```

### 5.3 IR Node Type Confusion

**Pitfall**: Mixing Expr and Stmt incorrectly
```java
// WRONG: Using Expr where Stmt is expected
public void visit(ExprStmtNode node) {
    var result = node.getExpr().accept(this);
    // result is VarSlot (Expr), but currentBlock.addStmt expects IRNode
    currentBlock.addStmt(result);  // Compilation error
}
```

**Solution**: Wrap Expr in ExprStmt or use proper conversion
```java
// CORRECT: Wrap expression in ExprStmt
public void visit(ExprStmtNode node) {
    node.getExpr().accept(this);
    var exprVal = popEvalOperand();

    if (exprVal != null) {
        addInstr(new ExprStmt(exprVal));  // Wrap in ExprStmt
    }
}
```

### 5.4 Missing Visitor Methods

**Pitfall**: Forgetting to implement visitor methods for new IR nodes
```java
// WRONG: Incomplete visitor implementation
public class MyVisitor implements IRVisitor<Void, Void> {
    @Override
    public Void visit(BinExpr node) {
        // Implementation...
    }

    // Missing: visit(UnaryExpr), visit(Assign), etc.
    // Will cause runtime errors when visiting these node types
}
```

**Solution**: Implement all abstract methods or use default interface methods
```java
// CORRECT: Complete implementation
public class MyVisitor implements IRVisitor<Void, Void> {
    @Override
    public Void visit(BinExpr node) {
        // Handle binary expressions
        return null;
    }

    @Override
    public Void visit(UnaryExpr node) {
        // Handle unary expressions
        return null;
    }

    @Override
    public Void visit(Assign node) {
        // Handle assignments
        return null;
    }

    // ... implement all other visit methods
}
```

### 5.5 FrameSlot vs OperandSlot Confusion

**Pitfall**: Using wrong slot type for variables vs temporaries
```java
// WRONG: Using FrameSlot for temporaries
public VarSlot visit(BinaryExprNode node) {
    // ...
    var result = new FrameSlot(0);  // Wrong - should be OperandSlot
    return result;
}
```

**Solution**: Clear distinction
```java
// CORRECT: Proper slot types
// FrameSlot - for declared variables in stack frame
FrameSlot varX = FrameSlot.get(variableSymbol);

// OperandSlot - for temporary computation results
OperandSlot temp = OperandSlot.pushStack();
```

### 5.6 IR Tree Structure Issues

**Pitfall**: Incorrect parent-child relationships in IR
```java
// WRONG: IR nodes not properly linked
BinExpr expr = new BinExpr(op, lhs, rhs);
// expr.getLhs() returns lhs, but lhs is not connected to expr's parent
// Can cause issues during traversal or optimization
```

**Solution**: Use proper construction with factory methods
```java
// CORRECT: Factory methods ensure proper structure
BinExpr expr = BinExpr.with(op, lhs, rhs);
// Factory method handles any necessary linking
```

### 5.7 Type Information Loss

**Pitfall**: Losing type information during IR generation
```java
// WRONG: ConstVal without type tracking
ConstVal value = new ConstVal(42);  // Is this int or float?
```

**Solution**: Type-safe generic constants
```java
// CORRECT: Generic ConstVal with type
ConstVal<Integer> intVal = ConstVal.valueOf(42);  // Explicitly int
ConstVal<Float> floatVal = ConstVal.valueOf(3.14f);  // Explicitly float
```

### 5.8 Infinite Loop in Control Flow

**Pitfall**: Creating unreachable or infinite control flow
```java
// WRONG: Jump to self
JMP jump = new JMP(currentBlock);  // Jumps to itself
```

**Solution**: Ensure proper block linking
```java
// CORRECT: Create new target block
LinearIRBlock nextBlock = new LinearIRBlock();
currentBlock.setLink(nextBlock);
JMP jump = new JMP(nextBlock);
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Theory**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Chapter 8: Intermediate Code Generation
  - Chapter 9: Machine-Independent Optimizations

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 7: The Procedure Abstraction
  - Chapter 8: Optimizations for Locality

- **"Modern Compiler Implementation in C"** by Andrew Appel
  - Chapter 8: Translation to Intermediate Code

**IR Design**:
- **"LLVM: A Compilation Framework for Lifelong Program Analysis & Transformation"** (Lattner et al.)
  - [LLVM Language Reference Manual](https://llvm.org/docs/LangRef.html)

- **"SSA-based Compiler Design"** (Muchnick)
  - Chapter on SSA form (advanced topic for EP21)

### 6.2 Online Resources

**Tutorials & Documentation**:
- [ANTLR4 Reference Documentation](https://github.com/antlr/antlr4/blob/master/doc/index.md)
- [Three-Address Code Wikipedia](https://en.wikipedia.org/wiki/Three-address_code)
- [Compiler Design Basics - GeeksforGeeks](https://www.geeksforgeeks.org/introduction-to-compiler-design/)

**Open Source IR Implementations**:
- **LLVM**: Production-grade IR with extensive optimization passes
- **GCC GIMPLE**: GCC's internal representation
- **QBE**: Small compiler backend with simple IR

**Academic Resources**:
- [Stanford CS143: Compiler Construction](https://www.youtube.com/watch?v=kYx1q6rIqgk)
- [MIT 6.035: Computer Language Engineering](https://ocw.mit.edu/courses/electrical-engineering-and-computer-science/6-035-computer-language-engineering-spring-2016/)

### 6.3 Practice Problems

**Beginner**:
- Implement IR for arithmetic expressions
- Generate IR for variable declarations and assignments
- Build IR for simple if-else statements

**Intermediate**:
- Implement IR for while loops and for loops
- Generate IR for function calls with multiple arguments
- Build IR for arrays (indexing, assignment)

**Advanced**:
- Implement IR optimization: constant folding, dead code elimination
- Generate IR for structs and field access
- Build SSA-form IR (preparation for EP21)

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `Dumper` class: Visualize AST structure
- `CFG.toString()`: Print control flow graph in DOT format
- `Prog.linearInstrs()`: Output linear IR sequence

**External Tools**:
- **Graphviz**: Visualize CFGs (EP20 generates mermaid/DOT)
- **GDB**: Debug IR generation process
- **Valgrind**: Check for memory leaks in C implementations

### 6.5 Key EP20 Files to Study

**IR Core**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRNode.java` - Base IR node
- `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/Prog.java` - Program container
- `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/expr/` - Expression nodes
- `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/stmt/` - Statement nodes

**IR Generation**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilder.java` - AST to IR

**Code Generation**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/pass/codegen/CymbolAssembler.java` - IR to VM bytecode

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/ir/ThreeAddressCodeTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/ir/CymbolIRBuilderTest.java`

---

**Summary**: This chapter covers the design and implementation of intermediate representation (IR), focusing on three-address code, IR node hierarchies, and the IR builder pattern. Mastering IR design is essential for building production-quality compilers that support optimization and multiple target architectures.
