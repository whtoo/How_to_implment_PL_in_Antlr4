# Chapter 16: End-to-End Compiler Pipeline

**Module**: Transition Chapter (Module 3 & Module 4 Integration)
**Target Reader**: Engineers understanding complete compiler integration
**Prerequisites**: Module 3 and Module 4 all knowledge (EP17-EP20)
**EP Coverage**: EP19-EP20 comprehensive review, integration with EP17-EP18
**Previous Chapter**: Chapter 15 (Local Optimization and Code Generation)
**Next Chapter**: Chapter 17 (Advanced Optimizations - EP21)

---

## 1. Learning Objectives

After completing this chapter, you will be able to:

- **Understand the complete compiler pipeline** from source code to executable bytecode
- **Integrate all compiler phases**: lexing, parsing, AST, type checking, IR generation, CFG, optimization, code generation
- **Master module coordination techniques** between EP17 (call graph), EP18 (VM), EP19 (type system), and EP20 (full compiler)
- **Design and implement a modular compiler architecture** with clear phase separation
- **Debug and troubleshoot the full compilation process** across all phases
- **Extend the compiler with new language features** by understanding the complete pipeline
- **Optimize compilation performance** and identify bottlenecks

**Core Deliverables**:
- Build complete end-to-end compiler
- Understand integration points between modules
- Implement new language feature end-to-end
- Validate complete compilation process

---

## 2. Knowledge Prerequisites

Before diving into this chapter, ensure you have:

**Essential Background**:
- ✅ Completed Chapter 15 (Local Optimization and Code Generation)
- ✅ Understanding of all EP17-EP20 components
- ✅ Familiarity with complete compiler architecture
- ✅ Knowledge of ANTLR4, AST, IR, CFG, optimization, code generation

**Required Programming Skills**:
- ✅ Advanced Java: Modular architecture, dependency injection, design patterns
- ✅ Integration testing across multiple components
- ✅ Error handling and reporting across phases
- ✅ Performance profiling and optimization

**Compiler Theory Knowledge**:
- ✅ Complete compilation pipeline understanding
- ✅ Frontend vs middle-end vs backend separation
- ✅ Symbol tables and type systems
- ✅ Virtual machines and bytecode execution

**Software Engineering Skills**:
- ✅ Build systems (Maven multi-module projects)
- ✅ Testing strategies (unit, integration, end-to-end)
- ✅ Documentation and code organization
- ✅ Version control and release management

---

## 3. Core Concepts to Master

### 3.1 Complete Compiler Architecture

**EP20 Full Pipeline**:

```
┌─────────────────────────────────────────────────────────────┐
│                   Source Code (.cymbol)                   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│  ┌────────────┐  ┌────────────┐  ┌─────────────┐   │
│  │   Lexer    │  │   Parser   │  │   AST       │   │
│  │  (Char →  │──▶│  (Tokens →│──▶│  Builder    │   │
│  │   Tokens)  │  │  ParseTree)│  │ (→ ASTNode) │   │
│  └────────────┘  └────────────┘  └─────────────┘   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   Middle-End Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │   Symbol    │  │   Type      │  │     IR      │  │
│  │  Resolution │──▶│   Checker   │──▶│   Builder    │  │
│  │ (→ SymTab)  │  │ (→ TypeInf) │  │  (→ 3AC)    │  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
│  ┌─────────────┐  ┌─────────────┐                      │
│  │   CFG       │  │ Optimizers  │                      │
│  │   Builder   │──▶│  (CFA, DCE,│                      │
│  │ (→ CFG)     │  │   CSE, ...) │                      │
│  └─────────────┘  └─────────────┘                      │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Backend Layer                         │
│  ┌─────────────┐  ┌─────────────┐                   │
│  │   Code      │  │  Register   │                   │
│  │  Generator  │──▶│ Allocation  │                   │
│  │ (→ Bytecode)│  │ (→ RegMap)  │                   │
│  └─────────────┘  └─────────────┘                   │
└─────────────────────────────┬───────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Target Code (.vm)                        │
│                   (EP18 Bytecode)                        │
└─────────────────────────────────────────────────────────────┘
```

**Key Integrations**:
- **EP17**: Call graph analysis for function ordering
- **EP18**: Virtual machine as execution target
- **EP19**: Enhanced type system and symbol table
- **EP20**: Full compiler pipeline with IR, CFG, optimization

### 3.2 Phase Separation and Interfaces

**Frontend Phases** (Source-dependent):
1. **Lexical Analysis** (CymbolLexer)
   - Input: Character stream
   - Output: Token stream
   - Responsibility: Identify tokens (keywords, identifiers, literals)

2. **Syntax Analysis** (CymbolParser)
   - Input: Token stream
   - Output: ParseTree
   - Responsibility: Build parse tree according to grammar

3. **AST Building** (CymbolASTBuilder)
   - Input: ParseTree
   - Output: AST (Abstract Syntax Tree)
   - Responsibility: Build language-agnostic AST

**Middle-End Phases** (Target-independent):
4. **Symbol Resolution** (LocalDefine, LocalResolver)
   - Input: AST
   - Output: Symbol table with resolved symbols
   - Responsibility: Bind identifiers to symbols

5. **Type Checking** (TypeChecker)
   - Input: AST + Symbol table
   - Output: Type-annotated AST
   - Responsibility: Verify type correctness

6. **IR Generation** (CymbolIRBuilder)
   - Input: Type-checked AST
   - Output: Three-address code IR
   - Responsibility: Generate target-independent IR

7. **CFG Construction** (CFGBuilder)
   - Input: Linear IR sequence
   - Output: Control flow graph
   - Responsibility: Build basic blocks and control flow

**Backend Phases** (Target-dependent):
8. **Optimization** (ControlFlowAnalysis, DCE, CSE, etc.)
   - Input: CFG
   - Output: Optimized CFG
   - Responsibility: Transform IR for efficiency

9. **Code Generation** (CymbolAssembler)
   - Input: Optimized IR
   - Output: Target bytecode
   - Responsibility: Generate EP18 VM instructions

### 3.3 Data Flow Between Phases

**EP20 Compiler Main Pipeline**:

```java
public class Compiler {
    public static void main(String[] args) throws IOException {
        // Step 1: Lexing and Parsing
        CharStream charStream = CharStreams.fromStream(inputStream);
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();

        // Step 2: AST Building
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);

        // Step 3: Symbol Resolution
        astRoot.accept(new LocalDefine());

        // Step 4: IR Generation
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);

        // Step 5: CFG Construction
        Prog prog = irBuilder.prog;
        prog.optimizeBasicBlock();

        // Step 6: Optimization (per function)
        List<IRNode> optimizedIR = new ArrayList<>();
        for (var funcBlock : prog.blockList) {
            CFG<IRNode> cfg = new CFGBuilder(funcBlock).getCFG();

            // Add optimizers
            cfg.addOptimizer(new ControlFlowAnalysis<>());
            cfg.addOptimizer(new LivenessAnalysis<>());
            // cfg.addOptimizer(new ConstantFolding());
            // cfg.addOptimizer(new DeadCodeElimination());
            // cfg.addOptimizer(new CommonSubexpressionElimination());

            // Apply optimizations
            cfg.applyOptimizers();

            // Collect optimized IR
            optimizedIR.addAll(cfg.getIRNodes());
        }

        // Step 7: Code Generation
        CymbolAssembler assembler = new CymbolAssembler();
        assembler.visit(optimizedIR);
        String bytecode = assembler.getAsmInfo();

        // Step 8: Output
        saveBytecode(bytecode, "output.vm");
    }
}
```

**Data Structures**:
- **Token**: Terminal symbol with type and text
- **ParseTree**: Concrete syntax tree from ANTLR4
- **ASTNode**: Abstract syntax tree (language-agnostic)
- **Symbol**: Variable, function, or type symbol
- **IRNode**: Three-address code instruction
- **BasicBlock**: Group of IR instructions
- **CFG**: Graph of basic blocks
- **Bytecode**: EP18 VM instruction sequence

### 3.4 Error Handling Across Phases

**Error Types**:
1. **Lexical Errors**: Invalid characters
   - Example: `int a = @` (invalid character `@`)
   - Phase: Lexer
   - Recovery: Skip character, report error

2. **Syntax Errors**: Invalid grammar
   - Example: `int a =` (missing expression)
   - Phase: Parser
   - Recovery: Error recovery strategies, continue parsing

3. **Semantic Errors**: Invalid meaning
   - Example: `int a = "hello"` (type mismatch)
   - Phase: Type checker
   - Recovery: Report error, continue checking

4. **Runtime Errors**: Invalid execution
   - Example: Division by zero, null pointer
   - Phase: VM execution
   - Recovery: Halt execution, report error

**EP20 Error Handling**:

```java
public class ErrorIssuer {
    public enum ErrorLevel {
        LEXICAL, SYNTAX, SEMANTIC, RUNTIME
    }

    private final List<CymbalError> errors = new ArrayList<>();

    public void reportError(ErrorLevel level, String message, int line, int column) {
        var error = new CymbalError(level, message, line, column);
        errors.add(error);
        logger.error("Error at {}:{}", line, message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<CymbalError> getErrors() {
        return errors;
    }

    public void clearErrors() {
        errors.clear();
    }
}
```

**Integration with Compilation Phases**:

```java
// In lexer (ANTLR4 handles this automatically)
lexer.removeErrorListeners();
lexer.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine,
                           String msg, RecognitionException e) {
        errorIssuer.reportError(ErrorLevel.LEXICAL, msg, line, charPositionInLine);
    }
});

// In parser (ANTLR4 handles this automatically)
parser.removeErrorListeners();
parser.addErrorListener(new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                           int line, int charPositionInLine,
                           String msg, RecognitionException e) {
        errorIssuer.reportError(ErrorLevel.SYNTAX, msg, line, charPositionInLine);
    }
});

// In type checker
public class TypeChecker implements ASTVisitor<Void, Type> {
    @Override
    public Type visit(AssignStmtNode node) {
        var rhsType = node.getRhs().accept(this);
        var lhsType = node.getLhs().accept(this);

        if (!rhsType.isAssignableTo(lhsType)) {
            errorIssuer.reportError(
                ErrorLevel.SEMANTIC,
                "Type mismatch: cannot assign " + rhsType + " to " + lhsType,
                node.getLine(),
                node.getColumn()
            );
        }

        return null;
    }
}

// In VM execution
public class VirtualMachine {
    public void execute(List<Instruction> instructions) {
        try {
            for (var instr : instructions) {
                instr.execute(this);
            }
        } catch (DivisionByZeroException e) {
            errorIssuer.reportError(
                ErrorLevel.RUNTIME,
                "Division by zero at PC = " + programCounter,
                getCurrentLine(),
                0
            );
            halt();
        }
    }
}
```

### 3.5 Module Integration

#### 3.5.1 EP17: Call Graph Analysis

**Purpose**: Analyze function call relationships for optimization and analysis.

**Integration Point**:
- After symbol resolution
- Before IR generation
- Input: AST with resolved symbols
- Output: Call graph (DOT format)

**Usage in EP20**:
```java
// Build call graph
CallGraphBuilder callGraphBuilder = new CallGraphBuilder(astRoot);
CallGraph callGraph = callGraphBuilder.build();

// Visualize call graph
String callGraphDot = callGraph.toDot();
saveToFile(callGraphDot, "callgraph.dot");

// Use call graph for analysis
var topologicalOrder = callGraph.getTopologicalOrder();
// Can use order for function scheduling in code generation
```

#### 3.5.2 EP18: Virtual Machine

**Purpose**: Execute compiled bytecode.

**Integration Point**:
- After code generation
- Final phase of compilation
- Input: Bytecode (.vm file)
- Output: Program execution results

**Usage in EP20**:
```java
// Generate bytecode
CymbolAssembler assembler = new CymbolAssembler();
assembler.visit(optimizedIR);
String bytecode = assembler.getAsmInfo();

// Save bytecode
saveBytecode(bytecode, "output.vm");

// Execute bytecode (optional, for testing)
VirtualMachine vm = new VirtualMachine();
ExecutionResult result = vm.execute("output.vm");
System.out.println("Output: " + result.getOutput());
```

#### 3.5.3 EP19: Enhanced Type System

**Purpose**: Provide robust type checking and symbol table.

**Integration Point**:
- After AST building
- Before IR generation
- Input: AST
- Output: Type-annotated AST, symbol table

**Usage in EP20**:
```java
// Use EP19 type checker
TypeTable typeTable = new TypeTable();
TypeChecker typeChecker = new TypeChecker(typeTable);

// Perform type checking
astRoot.accept(typeChecker);

// Check for errors
if (typeChecker.hasErrors()) {
    typeChecker.getErrors().forEach(error ->
        System.err.println(error.getMessage())
    );
    return;
}

// Type information available in IR generation
irBuilder.setTypeTable(typeTable);
```

#### 3.5.4 EP20: Full Compiler

**Purpose**: Complete compilation pipeline from source to bytecode.

**Integration**:
- Combines EP17, EP18, EP19 components
- Provides unified compilation workflow
- Handles all compilation phases

### 3.6 Compiler Configuration and Options

**Compiler Options**:
```java
public class CompilerOptions {
    private boolean optimize = true;
    private int optimizationLevel = 2;  // 0=none, 1=basic, 2=aggressive
    private boolean generateDebugInfo = true;
    private boolean generateCallGraph = false;
    private boolean generateCFG = false;
    private String outputFilename = "output.vm";
    private LogLevel logLevel = LogLevel.INFO;

    // Getters and setters...
}
```

**Option Handling**:
```java
public class Compiler {
    public void compile(String sourceFile, CompilerOptions options) {
        // Configure logging
        configureLogging(options.getLogLevel());

        // Phase 1: Parsing
        ParseTree parseTree = parse(sourceFile);
        if (hasErrors()) return;

        // Phase 2: AST Building
        ASTNode astRoot = buildAST(parseTree);

        // Phase 3: Analysis
        analyze(astRoot, options);
        if (hasErrors()) return;

        // Phase 4: IR Generation
        IRNode ir = generateIR(astRoot);

        // Phase 5: Optimization
        if (options.isOptimize()) {
            ir = optimize(ir, options.getOptimizationLevel());
        }

        // Phase 6: Code Generation
        String bytecode = generateCode(ir, options);

        // Phase 7: Output
        saveOutput(bytecode, options.getOutputFilename());
    }

    private IRNode optimize(IRNode ir, int level) {
        switch (level) {
            case 0:
                return ir;  // No optimization
            case 1:
                return applyBasicOptimizations(ir);
            case 2:
                return applyAggressiveOptimizations(ir);
            default:
                return ir;
        }
    }
}
```

### 3.7 Compiler Performance

**Bottleneck Identification**:
```java
public class CompilerProfiler {
    private Map<String, Long> phaseTimes = new HashMap<>();

    public <T> T profilePhase(String phaseName, Supplier<T> phase) {
        var startTime = System.nanoTime();
        try {
            return phase.get();
        } finally {
            var endTime = System.nanoTime();
            var duration = (endTime - startTime) / 1_000_000;  // ms
            phaseTimes.put(phaseName, duration);
            logger.debug("Phase {}: {} ms", phaseName, duration);
        }
    }

    public void report() {
        System.out.println("=== Compiler Performance Report ===");
        phaseTimes.forEach((phase, time) ->
            System.out.printf("%-20s: %d ms\n", phase, time)
        );
        var totalTime = phaseTimes.values().stream().mapToLong(Long::longValue).sum();
        System.out.printf("%-20s: %d ms\n", "Total", totalTime);
    }
}
```

**Usage**:
```java
CompilerProfiler profiler = new CompilerProfiler();

var parseTree = profiler.profilePhase("Parsing", () -> parser.file());
var astRoot = profiler.profilePhase("AST Building", () -> parseTree.accept(astBuilder));
var ir = profiler.profilePhase("IR Generation", () -> {
    astRoot.accept(irBuilder);
    return irBuilder.prog;
});
var optimizedIR = profiler.profilePhase("Optimization", () -> {
    prog.optimizeBasicBlock();
    cfg.applyOptimizers();
    return prog.linearInstrs();
});
var bytecode = profiler.profilePhase("Code Generation", () -> {
    assembler.visit(optimizedIR);
    return assembler.getAsmInfo();
});

profiler.report();
```

**Performance Optimization Techniques**:
1. **Lazy Computation**: Only compute what's needed
2. **Caching**: Cache expensive computations (e.g., type checking results)
3. **Parallel Phases**: Run independent phases in parallel
4. **Incremental Compilation**: Recompile only changed files
5. **Memory Management**: Use object pools, reduce GC pressure

### 3.8 Compiler Testing Strategies

**Testing Pyramid**:

```
          ┌─────────────┐
          │  E2E Tests  │  ← Slow, but comprehensive
          │     (few)    │
          └──────┬──────┘
                 │
          ┌────────┴────────┐
          │ Integration Tests │  ← Medium speed, good coverage
          │    (some)      │
          └────────┬────────┘
                 │
          ┌────────┴────────┐
          │  Unit Tests     │  ← Fast, high coverage
          │   (many)       │
          └─────────────────┘
```

**Unit Tests** (per phase):
```java
@Test
void testLexer() {
    String input = "int x = 5;";
    var lexer = new CymbolLexer(CharStreams.fromString(input));
    var tokens = lexer.getAllTokens();
    assertThat(tokens).hasSize(5);  // int, x, =, 5, ;
}

@Test
void testASTBuilding() {
    String input = "int x = 5;";
    var parser = new CymbolParser(tokenStream);
    var astBuilder = new CymbolASTBuilder();
    ASTNode ast = parser.file().accept(astBuilder);
    assertThat(ast).isInstanceOf(CompileUnit.class);
}

@Test
void testIRGeneration() {
    var astNode = buildAST("int x = 5;");
    var irBuilder = new CymbolIRBuilder();
    astNode.accept(irBuilder);
    assertThat(irBuilder.prog.instrs).hasSize(2);  // const 5, store x
}
```

**Integration Tests** (multiple phases):
```java
@Test
void testCompilationPipeline() {
    String source = "int x = 5 + 3; return x;";
    var bytecode = compile(source);
    assertThat(bytecode).isNotEmpty();
    assertThat(bytecode).contains("iconst");
    assertThat(bytecode).contains("iadd");
}

@Test
void testOptimizationPipeline() {
    String source = "int x = 5 + 3 * 2; return x;";
    var optimized = compileAndOptimize(source);
    var unoptimized = compile(source, false);
    assertThat(optimized.length()).isLessThan(unoptimized.length());
}
```

**End-to-End Tests** (complete compilation + execution):
```java
@Test
void testCompleteCompilation() {
    String source = """
        int add(int a, int b) {
            return a + b;
        }

        void main() {
            print(add(5, 3));
        }
        """;

    // Compile
    var bytecode = compile(source);
    saveBytecode(bytecode, "test.vm");

    // Execute
    var vm = new VirtualMachine();
    var result = vm.execute("test.vm");

    // Verify
    assertThat(result.getOutput()).isEqualTo("8");
    assertThat(result.getExitCode()).isEqualTo(0);
}
```

---

## 4. Practical Exercises

### 4.1 Foundation Exercises

**Exercise 1: Trace Complete Compilation**
Given source code:
```c
int add(int a, int b) {
    return a + b;
}

void main() {
    print(add(5, 3));
}
```

**Tasks**:
- Manually trace through all compilation phases
- Document intermediate representations at each phase:
  - Tokens
  - ParseTree
  - AST
  - IR
  - CFG
  - Optimized IR
  - Bytecode
- Verify each transformation is correct

**Exercise 2: Implement Phase Timing**
- Add timing instrumentation to Compiler.java
- Measure time for each compilation phase
- Identify bottlenecks
- Generate performance report

**Exercise 3: Build Error Reporting System**
- Implement ErrorIssuer class
- Add error reporting to lexer, parser, type checker
- Generate error messages with file, line, column
- Test with various error scenarios

### 4.2 Intermediate Exercises

**Exercise 4: Implement Compiler Options**
- Create CompilerOptions class
- Support options: -O (optimization level), -g (debug info), -o (output file)
- Parse command-line arguments
- Apply options to compilation pipeline

**Exercise 5: Integrate EP17 Call Graph**
- Use EP17 CallGraphBuilder in EP20 compiler
- Generate call graph for compiled programs
- Save call graph in DOT format
- Visualize call graph structure

**Exercise 6: Implement Incremental Compilation**
- Track file modification times
- Only recompile changed files
- Cache compilation results (AST, IR)
- Measure compilation time improvement

### 4.3 Advanced Exercises

**Exercise 7: Add New Language Feature**
Choose a feature and implement it end-to-end:
- **Option A**: For loops
- **Option B**: Switch statements
- **Option C**: Enums
- **Option D**: Function overloading

**Tasks**:
1. Update grammar (Cymbol.g4)
2. Implement AST nodes
3. Update type checker
4. Update IR builder
5. Update code generator
6. Write comprehensive tests

**Exercise 8: Implement Parallel Compilation**
- Identify independent compilation phases
- Use parallel streams or threads
- Measure performance improvement
- Handle thread safety carefully

**Exercise 9: Build Compiler Driver with REPL**
- Implement read-eval-print loop
- Compile and execute expressions interactively
- Support command-line interface
- Add error recovery for REPL

**Exercise 10: Build Complete Test Suite**
- Write unit tests for each phase (100+ tests)
- Write integration tests (20+ tests)
- Write end-to-end tests (10+ tests)
- Measure test coverage (JaCoCo)
- Target 80%+ coverage

---

## 5. Common Pitfalls

### 5.1 Incorrect Phase Ordering

**Pitfall**: Phases in wrong order causing errors
```java
// WRONG: Type checking before symbol resolution
astRoot.accept(new TypeChecker());
astRoot.accept(new LocalDefine());  // Symbols not resolved yet!
```

**Solution**: Correct phase order
```java
// CORRECT: Resolve symbols before type checking
astRoot.accept(new LocalDefine());
astRoot.accept(new TypeChecker());
```

### 5.2 Not Checking for Errors Between Phases

**Pitfall**: Continuing compilation despite errors
```java
// WRONG: No error checking
astRoot.accept(new TypeChecker());
var ir = buildIR(astRoot);  // May have type errors!
```

**Solution**: Check for errors before continuing
```java
// CORRECT: Check errors before proceeding
astRoot.accept(new TypeChecker());
if (errorIssuer.hasErrors()) {
    return;  // Stop compilation
}
var ir = buildIR(astRoot);
```

### 5.3 Incorrect Data Flow Between Phases

**Pitfall**: Phases not using results from previous phases
```java
// WRONG: Type checker not using symbol table
TypeChecker typeChecker = new TypeChecker();  // No symbol table!
astRoot.accept(typeChecker);
```

**Solution**: Pass data between phases correctly
```java
// CORRECT: Type checker uses symbol table
astRoot.accept(new LocalDefine());
SymbolTable symbolTable = getSymbolTable();

TypeChecker typeChecker = new TypeChecker(symbolTable);
astRoot.accept(typeChecker);
```

### 5.4 Memory Leaks in Long-Running Compiler

**Pitfall**: Not releasing resources between compilations
```java
// WRONG: Accumulating data across compilations
public Compiler() {
    this.symbolTable = new SymbolTable();  // Never cleared!
    this.irBuilder = new CymbolIRBuilder();  // Never reset!
}

public void compile(String source) {
    // Uses same symbolTable and irBuilder
    // Data accumulates across compilations!
}
```

**Solution**: Reset or recreate resources
```java
// CORRECT: Reset between compilations
public void compile(String source) {
    this.symbolTable = new SymbolTable();  // New symbol table
    this.irBuilder = new CymbolIRBuilder();  // New IR builder

    // Compilation...
}
```

### 5.5 Thread Safety Issues in Parallel Compilation

**Pitfall**: Shared mutable state in parallel phases
```java
// WRONG: Shared state across threads
public class IRBuilder {
    private static int tempCounter = 0;  // Shared!
    // ...
}

// Parallel compilation
sources.parallelStream().forEach(source -> {
    new IRBuilder().build(source);  // Race condition!
});
```

**Solution**: Use thread-local or instance state
```java
// CORRECT: Instance state, not shared
public class IRBuilder {
    private int tempCounter = 0;  // Instance, not static
    // ...
}

// Parallel compilation
sources.parallelStream().forEach(source -> {
    var builder = new IRBuilder();  // Separate instance per compilation
    builder.build(source);
});
```

### 5.6 Incorrect Error Recovery

**Pitfall**: Stopping compilation at first error
```java
// WRONG: Stop at first error
for (var phase : phases) {
    phase.execute(astRoot);
    if (errorIssuer.hasErrors()) {
        return;  // Stop!
    }
}
```

**Solution**: Continue compilation for better error reporting
```java
// CORRECT: Continue to report multiple errors
for (var phase : phases) {
    phase.execute(astRoot);
    // Don't return, collect all errors
}

// Report all errors at end
errorIssuer.getErrors().forEach(error ->
    System.err.println(error)
);
```

### 5.7 Not Validating Compiler Output

**Pitfall**: Not verifying generated bytecode
```java
// WRONG: No validation of output
String bytecode = generateCode(ir);
saveBytecode(bytecode, outputFilename);
```

**Solution**: Validate before output
```java
// CORRECT: Validate bytecode
String bytecode = generateCode(ir);

// Validate bytecode syntax
if (!isValidBytecode(bytecode)) {
    throw new CompilerException("Invalid bytecode generated");
}

// Validate bytecode semantics
var vm = new VirtualMachine();
if (!vm.canExecute(bytecode)) {
    throw new CompilerException("Bytecode cannot be executed");
}

saveBytecode(bytecode, outputFilename);
```

### 5.8 Poor Test Coverage

**Pitfall**: Only testing happy paths
```java
// WRONG: Only testing valid code
@Test
void testValidCompilation() {
    String source = "int x = 5;";
    var result = compile(source);
    assertThat(result.isSuccess()).isTrue();
}
```

**Solution**: Test error paths and edge cases
```java
// CORRECT: Test various scenarios
@Test
void testValidCompilation() {
    String source = "int x = 5;";
    var result = compile(source);
    assertThat(result.isSuccess()).isTrue();
}

@Test
void testSyntaxError() {
    String source = "int x =";  // Missing expression
    var result = compile(source);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrors()).hasSize(1);
}

@Test
void testTypeError() {
    String source = "int x = \"hello\";";  // Type mismatch
    var result = compile(source);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getErrors()[0].getType()).isEqualTo(ErrorType.SEMANTIC);
}
```

---

## 6. Additional Resources

### 6.1 Recommended Reading

**Compiler Architecture**:
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
  - Complete compiler architecture overview
  - Phase-by-phase implementation details

- **"Engineering a Compiler"** by Cooper & Torczon
  - Chapter 1: Introduction to Compilation
  - Chapter 2: Scanners
  - Chapter 3: Parsers
  - ... all phases

**Software Engineering**:
- **"Clean Architecture"** by Robert C. Martin
  - Chapter on compiler architecture patterns

- **"Building Microservices"** by Sam Newman
  - Modularity and dependency management (applicable to compiler phases)

### 6.2 Online Resources

**Tutorials & Documentation**:
- [LLVM Compiler Architecture](https://llvm.org/docs/CompilerInfrastructure.html)
- [GCC Internals Manual](https://gcc.gnu.org/onlinedocs/gccint/)
- [Compilers: The Frontend and Backend](https://www.tutorialspoint.com/compiler_design/compiler_design_phases_of_compilation.htm)

**Testing**:
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [JaCoCo User Guide](https://www.jacoco.org/jacoco/trunk/doc/)

**Performance**:
- [Java Performance Tuning Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/performance/)
- [JProfiler Documentation](https://www.ej-technologies.com/products/jprofiler/overview.html)

### 6.3 Practice Problems

**Beginner**:
- Trace compilation through all phases
- Implement phase timing
- Build error reporting system

**Intermediate**:
- Integrate EP17 call graph
- Implement compiler options
- Add new language feature end-to-end

**Advanced**:
- Implement incremental compilation
- Build parallel compilation pipeline
- Create comprehensive test suite

### 6.4 Debugging Tools

**EP20 Built-in Tools**:
- `Compiler.java`: Main compilation pipeline
- `ErrorIssuer`: Error reporting
- `Dumper`: AST/IR visualization
- `CFG.toString()`: CFG visualization (DOT/mermaid)

**External Tools**:
- **JProfiler**: Profile Java performance
- **VisualVM**: Monitor memory and CPU
- **GDB**: Debug VM execution
- **Graphviz**: Visualize CFG and call graphs

### 6.5 Key Files to Study

**Complete Pipeline**:
- `ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java` - Main compiler
- `ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java` - Phase management
- `ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java` - Compilation task

**Integration**:
- `ep17/src/main/java/org/teachfx/antlr4/ep17/CallGraphBuilder.java` - Call graph
- `ep18/src/main/java/org/teachfx/antlr4/ep18/vm/VirtualMachine.java` - VM
- `ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java` - Pipeline

**Tests**:
- `ep20/src/test/java/org/teachfx/antlr4/ep20/pass/codegen/EndToEndCompilationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/IntegrationTest.java`
- `ep20/src/test/java/org/teachfx/antlr4/ep20/ComprehensiveTest.java`

---

**Summary**: This chapter covers end-to-end compiler pipeline integration, focusing on phase separation, data flow, error handling, module integration (EP17-EP20), testing strategies, and performance optimization. Mastering the complete compiler pipeline is essential for building production-quality compilers and extending them with new features.
