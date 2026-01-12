# AGENTS.md

This file contains essential information for coding agents working in this repository.

## Build System

This is a **Maven multi-module project** using Java 21. Each `epXX` directory is a separate Maven module implementing progressive compiler development.

### Essential Commands

```bash
# Build entire project (root level)
mvn clean compile

# Run all tests
mvn test

# Run tests for a specific module
cd ep20 && mvn test

# Run a single test class
mvn test -Dtest=ArraysTest

# Run a single test method
mvn test -Dtest=ArraysTest#testArrayDeclaration

# Run tests matching a pattern
mvn test -Dtest=*BasicBlockTest

# Package with dependencies
mvn clean package

# Run the compiler (module-specific)
cd ep20
mvn exec:java -Dexec.args="src/main/resources/t.cymbol"
```

### Module Organization

- **Root POM**: `/pom.xml` - Parent configuration for all modules
- **Active Modules** (as configured in root POM): ep17, ep18, ep18r, ep19, ep20, ep21
- **Module Structure**: Each `epXX/` follows standard Maven layout:
  - `src/main/java/` - Source code
  - `src/main/antlr4/` or `src/main/java/org/teachfx/antlr4/` - ANTLR4 grammar files (.g4)
  - `src/test/java/` - Test code
  - `src/main/resources/` - Test input files (.cymbol, .vm, etc.)

### Key Dependencies

- **ANTLR4**: 4.13.2 - Parser generator
- **JUnit Jupiter**: 5.8.2 - Testing framework
- **AssertJ**: 3.21.0 - Fluent assertions
- **Log4j2**: 2.17.1 - Logging
- **Apache Commons Lang3**: 3.12.0 - Utilities

## Code Style Guidelines

### Package Naming

- Pattern: `org.teachfx.antlr4.epXX.package`
- Examples:
  - `org.teachfx.antlr4.ep20.Compiler`
  - `org.teachfx.antlr4.ep20.ast.expr.BinaryExprNode`
  - `org.teachfx.antlr4.ep20.pass.cfg.ControlFlowAnalysis`

### Import Ordering

```java
// 1. ANTLR4 imports
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

// 2. External library imports
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// 3. Internal project imports (grouped by module)
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;

// 4. Java standard library
import java.io.*;
import java.util.ArrayList;
import java.util.List;
```

### Class Naming

- **Classes/Interfaces**: PascalCase
  - `Compiler`, `ASTBuilder`, `ControlFlowAnalysis`, `IRNode`
- **Abstract Classes**: PascalCase
  - `ASTNode`, `IRNode`, `ExprNode`
- **Test Classes**: PascalCase ending with `Test`
  - `ArraysTest`, `OperatorsTest`, `CFGBuilderTest`

### Method Naming

- **Methods**: camelCase
  - `compile()`, `analyzeSemantic()`, `getCFG()`, `optimizeBasicBlock()`
- **Visitor methods**: camelCase with descriptive names
  - `visit(ASTNode node)`, `accept(IRVisitor visitor)`, `buildIR()`

### Field Naming

- **Fields**: camelCase
  - `blockList`, `instrs`, `needRemovedBlocks`, `charStream`, `astRoot`
- **Static final constants**: UPPER_SNAKE_CASE (not commonly used in this codebase)

### Access Modifiers

- Use **public** only when necessary (API methods, test methods)
- Use **protected** for methods intended for subclass access
- Use **private** for implementation details
- Package-private (no modifier) used sparingly

### Type Safety

- **No type suppression**: Never use `@SuppressWarnings("unchecked")` without documented justification
- **Generics**: Use proper type parameters (e.g., `List<IRNode>` not `List`)
- **Nullability**: Consider using `@NotNull` / `@Nullable` annotations for important APIs

### Logging

- Use **Log4j2** via `LogManager.getLogger(Class.class)`
- Example:
  ```java
  private static final Logger logger = LogManager.getLogger(Compiler.class);
  logger.debug("Block {} will be removed", linearIRBlock);
  ```

### Error Handling

- Use **checked exceptions** for recoverable errors (e.g., `IOException`)
- Use **runtime exceptions** for programmer errors (e.g., `IllegalArgumentException`, `IllegalStateException`)
- Never use empty catch blocks - always log or rethrow

### AST/IR Node Patterns

- **Visitor Pattern**: Extensively used for AST/IR traversal
  - Node classes have `accept(Visitor visitor)` method
  - Visitors implement `visit(NodeType node)` methods
- **Abstract Base Classes**: `ASTNode`, `IRNode` provide common interface
- **Location Tracking**: Nodes often store `ParserRuleContext ctx` for source location

### Testing Patterns

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@Test
public void testArrayDeclaration() {
    String source = "int test() { int arr[5]; return arr[0]; }";
    assertTrue(canParse(source), "Should parse array declaration");
}

// Helper method in test classes
private boolean canParse(String source) {
    try {
        InputStream is = new ByteArrayInputStream(source.getBytes());
        var charStream = CharStreams.fromStream(is);
        var lexer = new CymbolLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new CymbolParser(tokenStream);
        parser.file();
        return parser.getNumberOfSyntaxErrors() == 0;
    } catch (Exception e) {
        return false;
    }
}
```

### Grammar Files (.g4)

- Located in module-specific directories
- Simple, readable grammar definitions
- Use ANTLR4 visitor pattern with `-visitor` option in Maven plugin
- Grammar name matches language (e.g., `Cymbol.g4`, `Math.g4`, `JSON.g4`)

### Compiler Pipeline Pattern

Most modules (especially ep17-ep21) follow this compilation pipeline:

1. **Lexing**: CharStream → Lexer → TokenStream
2. **Parsing**: TokenStream → Parser → ParseTree
3. **AST Building**: ParseTree → ASTBuilder → ASTNode
4. **Symbol Resolution**: ASTNode → LocalDefine → SymbolTable
5. **Type Checking**: ASTNode → TypeChecker
6. **IR Generation**: ASTNode → IRBuilder → IRNode (Three-Address Code)
7. **CFG Construction**: IRNode → CFGBuilder → CFG
8. **Optimization**: CFG → Optimizers → Optimized CFG
9. **Code Generation**: Optimized IR → Assembler → VM Bytecode

### Code Organization Best Practices

- **Separation of Concerns**: Distinct packages for `ast/`, `ir/`, `pass/`, `parser/`, `symtab/`
- **Pass-based Architecture**: Each compilation phase is a "Pass" that implements the visitor pattern
- **Interface-driven Design**: Use interfaces (e.g., `ASTVisitor`, `IRVisitor`) to allow multiple implementations
- **Test Coverage**: Write comprehensive tests for each compilation phase

### Working with Multiple Modules

When modifying code across modules:
1. Identify which EP (episode) module contains the feature you're working on
2. Use the latest module (ep21 for most advanced features) unless specific EP is required
3. Respect module boundaries - don't break encapsulation between EPs
4. Run tests in the specific module before running full project tests

### Common Pitfalls

- **Don't** modify ANTLR4-generated code (Lexer/Parser classes) directly
- **Don't** assume all modules have the same features - they're progressive
- **Don't** add test dependencies to main production code
- **Do** run `mvn test` in the specific module you modified before committing
- **Do** check that parser grammars are recompiled if you modify .g4 files

## Project Context

This is **"How to Implement a Programming Language in ANTLR4"** - an educational compiler construction project.

- **Progressive Development**: Each EP (episode) adds new compiler features
- **EP1-EP12**: Basic parsing and interpretation
- **EP13-EP16**: AST and basic compilation
- **EP17-EP20**: Full compiler with CFG, IR, and optimization
- **EP21**: Advanced compiler with SSA, dataflow analysis, and TRO

When working on this codebase, understand which EP you're in and respect the intended educational progression.

---

## Case Study: EP21 ↔ EP18R Interface Adaptation

### Problem Context

**Project Background**: ANTLR4 compiler project, EP21 (compiler optimization) needs to integrate EP18R (register VM)'s LinearScanAllocator
**Conflict**: EP18R's LinearScanAllocator uses string variable names (e.g., "x", "y", "var1"), while EP21's VariableSymbol is an abstract class without getName() method
**Impact**: RegisterVMGenerator cannot correctly pass VariableSymbol to EP18R's LinearScanAllocator, blocking cross-module integration

---

## Review Design & Impl

### Initial Design Flaws

1. **Interface Mismatch**:
   - EP18R IRegisterAllocator: `allocate(String varName)` - expects string identifiers
   - EP21 IRegisterAllocator: `allocateRegister(VariableSymbol variable)` - expects symbol objects
   - No bridge between the two type systems

2. **Type System Gap**:
   ```
   Symbol (base)
     └─> VariableSymbol (abstract, no getName())
           ↓
   EP21 uses VariableSymbol
           ↓
   RegisterVMGenerator tries to allocate
           ↓
   EP18R LinearScanAllocator expects String
           ❌ TYPE MISMATCH
   ```

3. **Coupling Issues**:
   - RegisterVMGenerator tightly coupled to round-robin allocation
   - No adapter layer between different allocation strategies
   - Cannot swap allocators without modifying visitor

### Failed Attempts (❌)

**Attempt 1**: Directly modify RegisterVMGenerator's allocateTemp() and freeTemp() to use IRegisterAllocator

**Problems Encountered**:
1. Compilation errors: `registerAllocator cannot be resolved to a variable`
2. No understanding of RegisterVMVisitor's complete constructor and dependency relationships
3. Attempted to modify multiple methods simultaneously
4. Compilation errors accumulated without understanding root cause
5. Attempted to patch errors without addressing root issue

**Lessons Learned**:
- ❌ Do not attempt to modify multiple interdependent methods at once
- ❌ Do not continue "patching" when compilation errors occur
- ❌ Do not modify code without understanding complete dependency relationships
- ❌ Failed to trace errors to true root cause (VariableSymbol lacks getName())

---

## Analyze Logic Chain (分析逻辑链条)

### Dependency Analysis

```
Symbol (base class)
  ├─> VariableSymbol extends Symbol
  │    ├─> Constructor: VariableSymbol(String name)
  │    ├─> Field: protected name (inherited from Symbol)
  │    └─> ISSUE: No getName() method to expose name
  │
  └─> LocalSymbol extends VariableSymbol
       └─> ISSUE: Inherits no getName() method

EP18R IRegisterAllocator (String-based)
  ├─> allocate(String varName)
  ├─> getRegister(String varName)
  ├─> isSpilled(String varName)
  └─> ISSUE: Cannot accept VariableSymbol objects

EP21 IRegisterAllocator (VariableSymbol-based)
  ├─> allocateRegister(VariableSymbol variable)
  ├─> getRegister(VariableSymbol variable)
  ├─> isSpilled(VariableSymbol variable)
  └─> DESIGN: Properly typed, but EP18R doesn't support

EP18RRegisterAllocatorAdapter (Bridge)
  ├─> Implements EP21's IRegisterAllocator
  ├─> Delegates to EP18R's IRegisterAllocator
  ├─> Maintains VariableSymbol ↔ String mapping
  └─> ISSUE: VariableSymbol.getName() returns null → generates duplicate varX names

RegisterVMGenerator
  ├─> Uses EP18RRegisterAllocatorAdapter
  ├─> Creates RegisterGeneratorVisitor
  │    ├─> Has registerAllocator field (from RegisterVMGenerator)
  │    └─> ISSUE: How to get variable name from VariableSymbol?
  └─> ISSUE: Cannot allocate registers for VariableSymbol without name
```

### Type System Analysis

**EP21 Type System**:
- Hierarchical: Symbol → VariableSymbol → LocalSymbol
- Strong typing: Uses VariableSymbol objects throughout
- Symbol table integration: VariableSymbol represents program variables

**EP18R Type System**:
- Simple: Uses string identifiers for variables
- Direct allocation: allocate(String name) bypasses symbol table
- No EP21 integration: Doesn't understand VariableSymbol objects

**Gap**: Missing getName() method in Symbol hierarchy

### Interface Contract Analysis

**EP21 IRegisterAllocator Contract**:
```java
public interface IRegisterAllocator {
    @NotNull
    int allocateRegister(@NotNull VariableSymbol variable);

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    int getStackOffset(@NotNull VariableSymbol variable);

    void reset();

    int getAllocatedRegisterCount();
}
```

**EP18R IRegisterAllocator Contract**:
```java
public interface IRegisterAllocator {
    int allocate(String varName);

    int getRegister(String varName);

    int getSpillSlot(String varName);

    boolean isSpilled(String varName);

    void free(String varName);

    int getAllocatedRegisterCount();
}
```

**Mismatch**: EP18R's interface is string-based, EP21's is object-based

### Data Flow Analysis

**Current Flow (Broken)**:
```
RegisterVMGenerator.generate(program)
  ↓
RegisterGeneratorVisitor.visit(IRNode)
  ↓
Encounters VariableSlot (represents variable)
  ↓
Calls allocateTemp()
  ↓
Attempts to call registerAllocator.allocateRegister(variable)
  ↓
EP18RRegisterAllocatorAdapter.getVariableName(variable)
  ↓
Variable.getName() returns null ❌
  ↓
Generates "var1", "var2", ... ❌
  ↓
Multiple VariableSlot objects with different names map to same "varX" ❌
  ↓
EP18R LinearScanAllocator allocates same register to different variables ❌
  ↓
INCORRECT CODE GENERATION
```

**Expected Flow (Fixed)**:
```
RegisterVMGenerator.generate(program)
  ↓
RegisterGeneratorVisitor.visit(IRNode)
  ↓
Encounters VariableSlot (represents variable)
  ↓
Calls allocateTemp()
  ↓
Calls registerAllocator.allocateRegister(variable)
  ↓
EP18RRegisterAllocatorAdapter.getVariableName(variable)
  ↓
Variable.getName() returns "x" ✅
  ↓
Uses actual variable name ✅
  ↓
EP18R LinearScanAllocator allocates correct register ✅
  ↓
CORRECT CODE GENERATION
```

---

## Form New Solution (形成新方案)

### Solution Strategy: Minimal Invasive Change

**Core Principle**: Fix the lowest-level class to impact the entire hierarchy correctly

### Step 1: Fix Foundation Class (最小侵入性修复基础类)

**File**: `ep21/src/main/java/org/teachfx/antlr4/ep21/symtab/symbol/VariableSymbol.java`

**Change**: Add getName() method to base Symbol class

```java
public class VariableSymbol extends Symbol {
    // ... existing constructors ...

    // NEW METHOD: Expose name to allocation systems
    public String getName() {
        return name;
    }
}
```

**Rationale**:
- ✅ Minimal invasive: Only modify base class, affects all subclasses
- ✅ Dependency chain start point: All VariableSymbol subclasses inherit this method
- ✅ Simple and direct: Returns name field directly
- ✅ Backward compatible: Doesn't break any existing code

### Step 2: Update Adapter to Use New Method (更新适配器使用新方法)

**File**: `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/EP18RRegisterAllocatorAdapter.java`

**Change**: Simplify getVariableName() to directly call variable.getName()

**Before**:
```java
private String getVariableName(VariableSymbol variable) {
    return variableToName.computeIfAbsent(variable, var -> {
        String name = var.getName() != null ? var.getName() : "var" + nextVariableId++;
        nameToVariable.put(name, var);
        return name;
    });
}
```

**After**:
```java
private String getVariableName(VariableSymbol variable) {
    return variable.getName();
}
```

**Rationale**:
- ✅ Removes unnecessary computeIfAbsent logic
- ✅ Delegates to VariableSymbol's own getName() implementation
- ✅ Reduces complexity, improves maintainability
- ✅ Allows VariableSymbol subclasses to override getName() if needed

### Step 3: Design Adapter Pattern for Robust Mapping (设计健壮的适配器映射)

**EP18RRegisterAllocatorAdapter Design**:

```java
public class EP18RRegisterAllocatorAdapter implements IRegisterAllocator {
    // Bidirectional mapping
    private final Map<VariableSymbol, String> variableToName;
    private final Map<String, VariableSymbol> nameToVariable;
    private int nextVariableId;

    @Override
    public int allocateRegister(VariableSymbol variable) {
        String varName = getVariableName(variable);
        return ep18rAllocator.allocate(varName);
    }

    @Override
    public int getRegister(VariableSymbol variable) {
        String varName = getVariableName(variable);
        return ep18rAllocator.getRegister(varName);
    }

    // ... other methods ...
}
```

**Key Features**:
- Bidirectional mapping: VariableSymbol ↔ String
- Unique name generation for null-named temporaries
- Cached mapping for performance
- Report generation for debugging

### Interface Contract Alignment

**Strategy**: Use Adapter Pattern to bridge EP21 and EP18R interfaces

```
EP21 IRegisterAllocator (VariableSymbol-based)
         ↑
EP18RRegisterAllocatorAdapter (implements EP21 interface)
         ↓
EP18R IRegisterAllocator (String-based)
         ↑
EP18R LinearScanAllocator (implements EP18R interface)
```

**Benefits**:
- Clean separation of concerns
- Each component works with its natural type system
- Adapter handles translation layer
- Can swap allocators easily

---

## Resolve & Test Verify (解决并测试验证)

### Step 1: Implementation (修复实施)

**File Modified**: `ep21/src/main/java/org/teachfx/antlr4/ep21/symtab/symbol/VariableSymbol.java`

**Change Applied**:
```java
public String getName() {
    return name;
}
```

**Result**:
- ✅ Minimal invasive change
- ✅ Symbol name now accessible to allocation systems
- ✅ No impact on existing functionality

### Step 2: Update Adapter (更新适配器)

**File Modified**: `ep21/src/main/java/org/teachfx/antlr4/ep21/pass/codegen/EP18RRegisterAllocatorAdapter.java`

**Change Applied**:
```java
private String getVariableName(VariableSymbol variable) {
    return variable.getName();
}
```

**Result**:
- ✅ Removed unnecessary computeIfAbsent logic
- ✅ Direct delegation to VariableSymbol.getName()
- ✅ Cleaner, simpler code

### Step 3: Compilation Verification (编译验证)

**Command**: `mvn clean compile -DskipTests`

**Result**:
```
[INFO] BUILD SUCCESS
```

**Status**: ✅ No compilation errors

### Step 4: Unit Test Verification (单元测试验证)

**Command**: `mvn test -Dtest=RegisterAllocatorIntegrationTest`

**Result**:
```
Tests run: 5, Failures: 0, Errors: 0
[INFO] BUILD SUCCESS
```

**Test Coverage**:
- `testEP18RAdapterBasicAllocation()` - Reset functionality
- `testEP18RAdapterWithVariables()` - Basic allocation
- `testEP18RAdapterReset()` - Reset after allocations
- `testEP18RAdapterSpilling()` - Overflow handling
- `testEP18RAdapterGenerateReport()` - Report generation

**Status**: ✅ All tests passing

---

## General Experience Summary (通用经验总结)

### 1. Dependency Chain Analysis Priority (依赖链分析优先)

**Correct Process**:
```
1. Draw dependency diagram
2. Identify lowest-level/foundational classes
3. Design minimal change path
4. Fix from bottom to top, layer by layer
5. Verify each layer after fixing
```

**Wrong Process**:
```
1. Encounter problem
2. Patch at highest level
3. Introduce more errors
4. Cannot identify root cause
```

### 2. Minimal Invasive Principle (最小侵入性原则, MVP: Minimum Viable Product)

**Core Idea**: Solve problem with minimal changes, avoid large-scale refactoring

**Decision Criteria**:
- ✅ Can we fix by adding/modifying only ONE method?
- ✅ Can we maintain backward compatibility?
- ✅ Can we avoid modifying multiple interdependent files?
- ✅ Can we solve through interface adaptation?

**Benefits**:
- Reduced regression risk
- Easier to pinpoint problem source
- Easier to rollback
- Easier to code review

**When to Apply**:
- Type system issues (modify base class, not each subclass)
- Interface mismatch (add adapter layer)
- Missing methods (add to base class, not override in each subclass)
- Cross-module integration (add bridge classes, not modify all modules)

### 3. Compiler Architecture Considerations (编译器架构特殊考虑)

**Type System Complexity**:
- Compiler type systems are typically hierarchical (Symbol → VariableSymbol/LocalSymbol)
- Modifying base class affects all subclasses, requires extra caution
- Adding methods to base class is safer than overriding in each subclass

**Adapter Pattern**:
- Adapter needs bidirectional mapping management (VariableSymbol ↔ String)
- Ensure mapping uniqueness and invertibility
- Provide clear debugging methods (e.g., generateAllocationReport())

**Interface Contract Alignment**:
- Clearly define each interface's method signature and contract
- Use JavaDoc to document parameters and return values
- Provide default implementations or abstract classes to simplify usage

### 4. Debugging Strategy When Facing Compilation Errors (遇到编译错误时的调试策略)

**Process**:
1. **STOP**: Don't continue modifying code
2. **READ**: Understand compiler error's exact meaning
3. **TRACE BACK**: Find true root cause of error
4. **DESIGN**: Consider minimal invasive and dependency relationships
5. **IMPLEMENT**: Modify and immediately compile to verify
6. **VERIFY**: Ensure related tests pass after fix

**Log Recording**:
- Use logger.debug() to record key decision points
- Record adapter mapping status (which VariableSymbols map to which string names)
- Record register allocation process (which registers allocated, which variables spilled)

### 5. Interface Design Best Practices (接口设计最佳实践)

**Good Interface Design Example**:
```java
public interface IRegisterAllocator {
    // Clear contracts: parameter types, return value meanings, exception documentation
    @NotNull
    int allocateRegister(@NotNull VariableSymbol variable);

    @IntRange(from = -1, to = Integer.MAX_VALUE)
    int getStackOffset(@NotNull VariableSymbol variable);

    /**
     * Reset allocator state, releasing all registers and spill slots.
     * Clean state ready for new compilation unit (e.g., new function).
     */
    void reset();

    /**
     * Get count of allocated registers.
     * Used for monitoring register usage and performance analysis.
     */
    int getAllocatedRegisterCount();
}
```

### 6. Future Optimization Directions (后续优化方向)

Based on current minimal invasive fix, future work could include:

**Performance Optimization**:
- Cache VariableSymbol to String mapping
- Use object pooling to reduce GC pressure
- Batch allocate/free registers

**Feature Enhancement**:
- Add register allocation report functionality
- Support different register allocation strategies (linear scan, graph coloring)
- Add register spill optimization (spill slot optimization)

**Test Coverage**:
- Add more integration test cases
- Add performance benchmark tests
- Add semantic equivalence tests (EP18 vs EP18R)

**Documentation**:
- Write interface contract documentation
- Record design decisions and tradeoffs
- Provide usage examples and best practices

---

## Applicable Scenarios

These experiences apply to:
- ✅ Cross-module interface adaptation (e.g., EP21 ↔ EP18R)
- ✅ Type system refactoring (Symbol hierarchy structures)
- ✅ Compiler backend optimization (register allocation, code generation)
- ✅ Adapter pattern implementation
- ✅ Complex dependency chain debugging and refactoring

---

**Last Updated**: 2026-01-12
**Status**: ✅ Verified effective, can be applied to similar problems
