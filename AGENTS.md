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
