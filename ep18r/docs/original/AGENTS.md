# AGENTS.md - Agentic Coding Guidelines

**Project**: ANTLR4 Compiler Implementation (EP1-EP21)
**Language**: Java 21+ | **Build**: Maven 3.6+ | **Testing**: JUnit 5.8.2 + AssertJ

---

## Build Commands

### Core Commands
```bash
mvn clean compile              # Build entire project
mvn clean install             # Full build with tests
mvn test                      # Run all tests
mvn test -pl ep21             # Test specific module
mvn clean -pl ep20            # Clean specific module
```

### Single Test Execution (CRITICAL)
```bash
# Test class
mvn test -Dtest=CymbolStackVMTest

# Test method
mvn test -Dtest=CymbolStackVMTest#testIntegerAddition

# Pattern match
mvn test -Dtest="*BasicBlock*"
```

### Quality & Coverage
```bash
mvn checkstyle:check -pl ep18 # Code style (Google Java Style)
mvn spotbugs:check            # Static analysis
mvn jacoco:report            # Generate coverage report
mvn test jacoco:report       # Test + coverage
```

### Run Programs
```bash
# Compile source code
mvn exec:java -pl ep20 -Dexec.args="program.cymbol"

# Run VM interpreter
mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.VMRunner" -Dexec.args="program.vm"

# Via scripts
./scripts/build.sh run ep20 program.cymbol
./scripts/build.sh test ep21
```

---

## Code Style Guidelines

### Imports
```java
// Order: ANTLR4 → Third-party → Internal → java.*
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.tuple.*;
import org.teachfx.antlr4.ep21.ast.*;
import java.io.*;
```

### Naming Conventions
```java
// Classes: PascalCase
public class CymbolASTBuilder { }

// Methods: camelCase
public void compile() { }
public boolean isValid() { }

// Variables: camelCase
ASTNode astRoot;
String fileName;

// Constants: UPPER_SNAKE_CASE
public static final int DEFAULT_STACK_SIZE = 1024;

// Packages: lowercase.dots
org.teachfx.antlr4.ep21.pass.ir
```

### Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: <120 chars
- **Braces**: K&R style (`if (cond) {`)
- **Spaces**: Around operators, after commas, in control structures

### Error Handling
```java
// Throw specific exceptions
throw new IllegalArgumentException("Parameter cannot be null");
throw new RuntimeException("Division by zero at PC=" + pc);

// Log with Log4j2
private static final Logger logger = LogManager.getLogger(ClassName.class);
logger.error("Error message: {}", error, exception);
```

### Testing Standards
```java
@Test
@DisplayName("Should correctly execute integer addition")
void testIntegerAddition() throws Exception {
    // Arrange, Act, Assert pattern
    byte[] bytecode = createBytecode(instructions);
    int result = execute(bytecode);
    assertThat(result).isEqualTo(8);
}
```

---

## Architecture Patterns

### Module Structure (EP17-EP21 active)
```
org.teachfx.antlr4.ep{NN}/
├── ast/              # AST nodes (expr, stmt, decl)
├── parser/          # ANTLR-generated code
├── symtab/          # Symbol table (symbol, scope, type)
├── pass/            # Compilation passes (ast, ir, cfg, codegen)
├── ir/              # Intermediate Representation (mir, lir)
├── analysis/        # Data flow, SSA
└── Compiler.java    # Main entry point
```

### Key Design Patterns
- **Visitor Pattern**: AST/IR traversal (accept/visit)
- **Builder Pattern**: Complex object construction
- **Factory Pattern**: Static factory methods for object creation

### Testing Structure
```
src/test/java/
├── integration/    # End-to-end tests
├── pass/           # Pass unit tests
└── test/           # Test utilities (VMTestBase, CompilerTestUtil)
```

---

## Critical Constraints

### Type Safety
- **NEVER** use `@SuppressWarnings("unchecked")`
- **NEVER** use `@SuppressWarnings("rawtypes")`
- Prefer explicit generics over raw types

### Test Coverage Requirements
- Overall: ≥85%
- Core modules (IR, CFG, Optimizer): ≥90%
- New features: 100%

### Code Quality Gates
- Checkstyle: Google Java Style
- SpotBugs: No critical issues
- JaCoCo: Meets minimum coverage thresholds

---

## Quick Reference

| Task | Command |
|------|---------|
| Build module | `mvn compile -pl epXX -am` |
| Test module | `mvn test -pl epXX` |
| Run single test | `mvn test -Dtest=ClassName#methodName` |
| Check style | `mvn checkstyle:check -pl epXX` |
| Coverage report | `mvn jacoco:report` |
| Run compiler | `mvn exec:java -pl ep20 -Dexec.args="file.cymbol"` |

---

## Key Dependencies

- **ANTLR4**: 4.13.2
- **JUnit**: 5.8.2 (Jupiter)
- **AssertJ**: 3.21.0
- **Log4j2**: 2.17.1
- **Apache Commons**: Lang3 3.12.0

---

## Language Notes

- **Comments**: Chinese and English both accepted (project uses mixed)
- **Error Messages**: Include context (line numbers, filenames)
- **Logging**: Use appropriate levels (DEBUG, INFO, WARN, ERROR)

---

**Generated**: 2026-01-08 | **Last Updated**: Based on project analysis
