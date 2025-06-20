# Project Guidelines

## Project Overview

This is "How to Implement a Programming Language in ANTLR4" (从解释计算的视角：如何亲手创造一门编程语言Cyson), a comprehensive tutorial series that demonstrates how to build a complete programming language from scratch using ANTLR4. The project consists of 21 episodes (ep1-ep21) that progressively build from basic parsing to a full compiler with virtual machine implementation.

### Project Purpose
- Educational tutorial series on programming language implementation
- Demonstrates practical use of ANTLR4 for language parsing and analysis
- Covers the complete pipeline from lexing/parsing to code generation and execution
- Implements a custom language called "Cyson" with modern language features

## Project Structure

### Root Directory Structure
- `ep1/` through `ep21/` - Individual tutorial episodes, each as a Maven module
- `src/` - Shared source code and utilities
- `scripts/` - Build and execution scripts
- `docs/` - Documentation and task tracking
- `pom.xml` - Root Maven configuration for multi-module project

### Episode Progression
- **ep1-ep10**: Basic parsing (Hello World, arrays, calculators, CSV, JSON, AST)
- **ep11-ep17**: Language semantics (interpreters, variables, scoping, type checking, functions)
- **ep18-ep21**: Advanced features (virtual machine, IR generation, SSA, control flow analysis)

### Module Structure
Each episode follows standard Maven structure:
```
ep${num}/
├── src/main/java/org/teachfx/antlr4/ep${num}/
├── src/test/java/org/teachfx/antlr4/ep${num}/
├── src/main/resources/
└── pom.xml
```

## Build and Test Instructions

### Environment Requirements
- **JDK 18+** (OpenJDK 18 or higher recommended)
- **Maven 3.8+** for build management
- **ANTLR4** runtime (managed via Maven dependencies)

### Build Process
1. **Full project build**: `mvn clean install` (from root directory)
2. **Individual module build**: `cd ep${num} && mvn clean package`
3. **Using build scripts**: `./scripts/run.sh compile ep${num}`

### Running Tests
- **Standard approach**: `mvn test` in individual episode directories
- **Script approach**: `./scripts/run.sh test ep${num}`
- **Specific test**: Use standard Maven test execution with `-Dtest=TestClassName`

### Running Episodes
- **Script method**: `./scripts/run.sh run ep${num} [arguments]`
- **Direct execution**: Follow individual episode README instructions
- **With input files**: `./scripts/run.sh run ep20 "src/main/resources/t.cymbol"`

## Testing Guidelines

### Test Execution Strategy
- **Always run tests** to verify correctness of proposed solutions
- Focus on the specific episode being modified
- Run comprehensive tests for episodes ep19-ep21 as they contain the most complex functionality
- Use the provided test utilities in `CompilerTestUtil.java` for consistent testing

### Test Categories
- **Unit tests**: Individual component testing
- **Integration tests**: End-to-end compilation and execution
- **Performance tests**: Benchmarking (especially in ep19)
- **Error recovery tests**: Parser error handling

## Code Style and Conventions

### Java Conventions
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Maintain consistent indentation (4 spaces)
- Add appropriate comments for complex logic

### ANTLR4 Grammar Style
- Use clear, descriptive rule names
- Maintain consistent grammar formatting
- Document complex grammar rules
- Follow ANTLR4 best practices for left-recursion handling

### Package Structure
- Main package: `org.teachfx.antlr4.ep${num}`
- Subpackages: `pass/` (for compiler passes), `vm/` (for virtual machine), etc.
- Test package mirrors main package structure

## Special Considerations

### Virtual Machine (ep18-ep21)
- The project implements a custom stack-based virtual machine
- Bytecode format uses `.def` function definitions
- Core components include instruction dispatcher, operand stack, and program counter

### Compiler Passes (ep16+)
- Multi-pass compilation architecture
- Symbol table management across scopes
- Type checking and semantic analysis
- IR generation and optimization

### Dependencies
- All dependencies managed through Maven
- Primary dependencies: ANTLR4 runtime, Log4j, Apache Commons
- No external build tools required beyond Maven

## Debugging and Development

### Logging
- Use Log4j for consistent logging across modules
- Debug output should be meaningful and structured
- Performance-critical sections should have appropriate logging levels

### Error Handling
- Implement proper error recovery in parsers
- Provide meaningful error messages to users
- Handle edge cases gracefully

This project represents a complete journey through programming language implementation, from basic parsing to advanced compiler techniques. Each episode builds upon previous work, so understanding the progression is crucial for effective development.
