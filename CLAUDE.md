# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**How to implement PL in ANTLR4** is a systematic compiler construction educational project that demonstrates how to implement a programming language using ANTLR4. It consists of 21 progressive episodes (EP1-EP21) that guide learners from basic lexer/parser implementation to advanced compiler optimization techniques.

The project implements the **Cymbol language** (a C-like language) with a complete compiler pipeline: frontend (lexer, parser, AST), middle-end (type system, semantic analysis, IR), and backend (code generation, VM execution).

## Architecture

### Module Structure
The project is organized as 21 Maven modules (`ep1` through `ep21`), each representing a learning stage:
- **EP1-EP10**: Foundation (lexer, parser, AST, visitors, interpreters)
- **EP11-EP20**: Compiler core (type system, symbol tables, IR, CFG, code generation)
- **EP21**: Advanced optimization (dataflow analysis, SSA form)

Each module has:
- `src/main/java/` - Java source code
- `src/main/antlr4/` - ANTLR grammar files (`.g4`)
- `src/test/java/` - Unit and integration tests
- `pom.xml` - Module-specific Maven configuration

### Key Components
- **Frontend**: `Cymbol.g4` grammar, AST nodes, visitors
- **Type System**: Symbol tables, type checking, semantic analysis
- **Intermediate Representation**: Three-address code format
- **Control Flow Graph**: Basic blocks, dominance analysis
- **Virtual Machine**: Stack-based VM (ep18)
- **Optimization Framework**: Dataflow analysis, SSA (ep21)

### Core Design Patterns
- **Visitor Pattern**: Used throughout for AST traversal
- **Builder Pattern**: For constructing complex objects
- **Factory Pattern**: For creating AST nodes
- **Singleton Pattern**: For global compiler components

## Development Commands

### Build and Test
```bash
# Build entire project
mvn clean compile

# Run all tests
mvn test

# Build specific module (e.g., ep20)
mvn clean compile -pl ep20

# Run tests for specific module
mvn test -pl ep20

# Generate test coverage report
mvn jacoco:report
```

### Using Development Scripts
The `scripts/` directory contains multi-platform runner scripts:
```bash
# Linux/macOS
./scripts/run.sh compile ep20
./scripts/run.sh run ep20 "program.cymbol"
./scripts/run.sh test ep20

# Windows PowerShell
.\scripts\run.ps1 compile ep20
.\scripts\run.ps1 run ep20 "program.cymbol"

# Windows CMD
scripts\run.bat compile ep20
scripts\run.bat run ep20 "program.cymbol"
```

### Direct Execution
```bash
# Run compiler with a Cymbol program
mvn compile exec:java -pl ep20 -Dexec.args="program.cymbol"

# Run specific main class
mvn compile exec:java -pl ep20 -Dexec.mainClass="org.teachfx.antlr4.ep20.Compiler"
```

## Testing Strategy

### Test Levels
1. **Unit Tests**: Individual classes/methods (JUnit 5 + AssertJ)
2. **Integration Tests**: Module interactions and compiler phases
3. **End-to-End Tests**: Complete compilation pipeline

### Test Coverage Requirements
- Overall coverage: ≥85%
- Core modules (IR, CFG, optimizer): ≥90%
- New functionality: 100%

### Running Tests
```bash
# Run all tests in a module
mvn test -pl ep20

# Run specific test class
mvn test -pl ep20 -Dtest=ArraysTest

# Run tests matching pattern
mvn test -pl ep20 -Dtest="*ExprNodeTest"
```

## ANTLR4 Integration

### Grammar Files
- Located in `src/main/antlr4/org/teachfx/antlr4/ep*/parser/`
- Main grammar: `Cymbol.g4` (ep20)
- ANTLR4 generates lexer, parser, and visitor classes

### Regenerating Parser Code
```bash
# Maven automatically regenerates ANTLR4 code during build
mvn generate-sources -pl ep20

# Or manually using ANTLR4 tool
java -jar antlr-4.13.2-complete.jar -visitor -no-listener Cymbol.g4
```

## Key File Locations

### Core Compiler Components
- **Main Compiler**: `ep20/src/main/java/org/teachfx/antlr4/ep20/Compiler.java`
- **Cymbol Grammar**: `ep20/src/main/antlr4/org/teachfx/antlr4/ep20/parser/Cymbol.g4`
- **AST Base Class**: `ep20/src/main/java/org/teachfx/antlr4/ep20/ast/ASTNode.java`
- **Symbol Table**: `ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/SymbolTable.java`
- **IR Generation**: `ep20/src/main/java/org/teachfx/antlr4/ep20/ir/IRGenerator.java`

### Configuration Files
- **Root POM**: `/pom.xml` (parent POM for all modules)
- **Module POMs**: `ep*/pom.xml` (module-specific configuration)
- **CI/CD**: `.github/workflows/maven.yml` (GitHub Actions workflow)

### Documentation
- **Main README**: `/README.md` (Chinese) and `/README_EN.md` (English)
- **Technical Docs**: `/.qoder/repowiki/en/` (232 files of comprehensive documentation)
- **Project Docs**: `/docs/` (course materials, testing strategy, design principles)

## Development Guidelines

### Code Style
- Follow Java 21+ syntax and features
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Maintain consistent indentation (4 spaces)

### Error Handling
- Use checked exceptions for recoverable errors
- Use runtime exceptions for programming errors
- Provide meaningful error messages with context
- Log errors using Log4j2

### Logging
- Use Log4j2 for all logging
- Different log levels: TRACE, DEBUG, INFO, WARN, ERROR
- Configure logging in `log4j2.xml` files

### Adding New Features
1. Start with tests (TDD approach)
2. Implement in appropriate module based on complexity
3. Update documentation in `.qoder/repowiki/`
4. Ensure backward compatibility
5. Run full test suite before committing

## Common Tasks

### Adding a New AST Node
1. Create class in `ep*/src/main/java/org/teachfx/antlr4/ep*/ast/`
2. Extend appropriate base class (ExprNode, StmtNode, etc.)
3. Implement visitor pattern methods
4. Add grammar rule in `Cymbol.g4`
5. Create corresponding test class

### Modifying the Grammar
1. Edit `Cymbol.g4` or create new `.g4` file
2. Run `mvn generate-sources` to regenerate parser
3. Update visitor implementations
4. Update tests to reflect changes

### Debugging Compilation Issues
1. Enable debug logging in `log4j2.xml`
2. Use `-Dlog4j.configurationFile=path/to/log4j2-debug.xml`
3. Check AST visualization tools in ep19/ep20
4. Use CFG visualization for control flow issues

## Notes for Future Claude Code Instances

- This is an **educational project** focused on compiler construction
- The 21-episode structure is **progressive** - later episodes depend on earlier ones
- **ANTLR4** is central to the project - understand grammar files and generated code
- **Visitor pattern** is used extensively for AST traversal
- **Testing is comprehensive** - maintain high coverage standards
- **Documentation is extensive** - check `.qoder/repowiki/` for detailed technical docs
- The project implements a **complete compiler pipeline** - understand the frontend/middle-end/backend separation