# How to Implement a Programming Language in ANTLR4

A progressive, hands-on guide to building a complete compiler from scratch using ANTLR4 and Java 21.

## 📖 Overview

This educational project teaches compiler construction through **21 progressive episodes (EPs)**, each building upon the previous. Starting with basic parsing and advancing to sophisticated optimizations, you'll build a complete compiler for the **Cymbol** programming language.

### What You'll Build

- A full-featured compiler pipeline: **lexing → parsing → AST → type checking → IR generation → optimization → code generation**
- A virtual machine (VM) with garbage collection for executing compiled programs
- Advanced compiler optimizations: SSA, dataflow analysis, tail recursion optimization
- Tools for analysis: call graphs, control flow graphs, symbol tables

## 🎯 Learning Path

### Phase 1: Foundations (EP1-EP12)
**Goal**: Learn ANTLR4 basics and build an interpreter

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP1-EP2 | Basic Parsing | Lexical analysis, grammar definition, parsing basics |
| EP3-EP4 | Expression Evaluation | Arithmetic operations, operator precedence |
| EP5-EP6 | Statements | Control flow (if/else, while), block statements |
| EP7-EP8 | Functions | Function definition, parameters, return values |
| EP9-EP10 | Symbol Tables | Scoping, variable declarations, name resolution |
| EP11-EP12 | Arrays & More | Array operations, advanced language features |

**Outcome**: A working interpreter that executes Cymbol programs directly.

### Phase 2: Compilation Basics (EP13-EP16)
**Goal**: Transform interpretation into compilation

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP13 | AST Construction | Abstract syntax trees, visitor pattern |
| EP14 | Symbol Resolution | Multi-pass compilation, scope management |
| EP15 | Type Checking | Static type analysis, error reporting |
| EP16 | Simple Code Generation | Basic bytecode generation, three-address code |

**Outcome**: A compiler that generates simple bytecode for execution.

### Phase 3: Modern Compiler Architecture (EP17-EP21)
**Goal**: Build a production-quality compiler

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP17 | Call Graph Analysis | ANTLR4 4.13.2, function call relationships, DOT visualization |
| EP18 | Virtual Machine | Stack-based VM, instruction set, memory management, garbage collection |
| EP18R | Enhanced VM | Advanced GC, optimizations, instruction set extensions |
| EP19 | IR Generation | Three-address code, SSA basics, IR design patterns |
| EP20 | CFG & Optimization | Control flow graphs, basic blocks, local optimizations |
| EP21 | Advanced Optimizations | Full SSA form, dataflow analysis, tail recursion optimization, global optimizations |

**Outcome**: A complete compiler with modern architecture and advanced optimizations.

## 🏗️ Project Structure

```
antlr4-project/
├── ep1-ep16/           # Foundation episodes (historical, not in active build)
├── ep17/               # Call graph analysis (currently active)
├── ep18/               # Virtual machine implementation
├── ep18r/              # Enhanced virtual machine
├── ep19/               # Intermediate representation generation
├── ep20/               # Full compiler with CFG and optimization
├── ep21/               # Advanced compiler with SSA and TRO
├── pom.xml             # Parent Maven POM
├── AGENTS.md           # AI agent development guide
└── README.md           # This file
```

### Active Modules

The root POM currently builds **EP17-EP21**:
```xml
<modules>
    <module>ep17</module>
    <module>ep18</module>
    <module>ep18r</module>
    <module>ep19</module>
    <module>ep20</module>
    <module>ep21</module>
</modules>
```

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Git** (for cloning)

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd How_to_implment_PL_in_Antlr4

# Build all active modules (EP17-EP21)
mvn clean compile

# Run all tests
mvn test

# Build specific module
cd ep21
mvn clean compile test
```

### Running the Compiler

```bash
# Using EP20 compiler (current production-ready version)
cd ep20
mvn exec:java -Dexec.args="src/main/resources/t.cymbol"

# Using EP21 compiler (advanced optimizations)
cd ep21
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep21.integration.EP21Compiler" \
    -Dexec.args="src/main/resources/example.cymbol output.vm"
```

## 📚 The Cymbol Language

Cymbol is a C-like educational programming language that grows with each episode:

### Basic Example
```c
int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void main() {
    int result = factorial(5);
    print(result);  // Output: 120
}
```

### Features by Episode

- **EP1-EP8**: Basic types, arithmetic, control flow, functions
- **EP9-EP10**: Scoping, local/global variables
- **EP11-EP12**: Arrays and array operations
- **EP13-EP16**: Static typing, type checking, error reporting
- **EP17-EP21**: Advanced features supporting full compilation

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Language** | Java | 21 |
| **Build Tool** | Maven | 3.8+ |
| **Parser Generator** | ANTLR4 | 4.13.2 |
| **Testing** | JUnit Jupiter | 5.8.2 |
| **Assertions** | AssertJ | 3.21.0 |
| **Logging** | Log4j2 | 2.17.1 |
| **Utilities** | Apache Commons Lang3 | 3.12.0 |
| **Graph Algorithms** (EP21) | JGraphT | Latest |

## 🧪 Testing

```bash
# Run all tests across all active modules
mvn test

# Run tests for a specific module
cd ep21
mvn test

# Run a specific test class
mvn test -Dtest=CFGBuilderTest

# Run a specific test method
mvn test -Dtest=TailRecursionOptimizerTest#testSimpleTailRecursion
```

## 📖 Module Documentation

Each active module has its own comprehensive README:

- **[EP17](ep17/README.md)** - Call Graph Analysis & ANTLR4 Upgrade
- **[EP18](ep18/README.md)** - Virtual Machine & Garbage Collection
- **[EP18R](ep18r/docs/README.md)** - Enhanced VM
- **[EP19](ep19/README.md)** - IR Generation
- **[EP20](ep20/README.md)** - Complete Compiler with CFG & Optimization
- **[EP21](ep21/README.md)** - Advanced Optimizations (SSA, Dataflow, TRO)

## 🎓 Learning Outcomes

By completing this project, you'll master:

1. **Language Theory**: Lexical analysis, parsing, grammars, AST design
2. **Compiler Design**: Multi-pass compilation, intermediate representations, optimization passes
3. **ANTLR4**: Grammar definition, visitor pattern, tree traversal
4. **Virtual Machines**: Stack-based execution, instruction sets, memory management
5. **Optimizations**: Local and global optimizations, SSA form, dataflow analysis
6. **Testing**: Unit testing, integration testing, end-to-end validation
7. **Software Engineering**: Modular design, clean code, best practices

## 💡 Code Style & Conventions

This project follows strict conventions documented in **[AGENTS.md](AGENTS.md)**:

- **Package Naming**: `org.teachfx.antlr4.epXX.package`
- **Import Ordering**: ANTLR4 → External libraries → Internal → Java stdlib
- **Class Naming**: PascalCase (e.g., `CFGBuilder`, `TailRecursionOptimizer`)
- **Method Naming**: camelCase (e.g., `visitASTNode`, `buildIR`)
- **Access Modifiers**: Public only when necessary, prefer private/protected
- **Type Safety**: Never suppress type warnings
- **Error Handling**: No empty catch blocks, proper logging
- **Visitor Pattern**: Extensively used for AST/IR traversal

## 🤖 AI Agent Support

This project is designed to work seamlessly with AI coding agents. The **[AGENTS.md](AGENTS.md)** file provides:

- Build system commands and workflows
- Code style guidelines and best practices
- Compiler pipeline patterns and conventions
- Testing strategies and patterns
- Common pitfalls and solutions

## 📊 Development Workflow

### Working with Multiple EPs

1. **Identify the EP** you need to work with (use EP21 for latest features)
2. **Navigate** to the EP directory (`cd epXX`)
3. **Build** the module (`mvn clean compile`)
4. **Test** your changes (`mvn test`)
5. **Respect** module boundaries - don't break encapsulation

### Adding New Features

1. Understand which EP the feature belongs to
2. Check existing patterns in that EP and earlier EPs
3. Follow the visitor pattern for AST/IR operations
4. Write comprehensive tests
5. Update documentation if needed

## 🔍 Debugging Tips

### Common Issues

1. **ANTLR4 Generation Issues**
   - Delete generated code in `target/generated-sources/`
   - Rebuild with `mvn clean compile`

2. **Test Failures**
   - Check if you're running tests in the correct EP directory
   - Ensure dependencies are built (`mvn install` from root)
   - Review test logs in `target/surefire-reports/`

3. **Virtual Machine Errors**
   - Verify bytecode generation is correct
   - Check stack operations in the VM
   - Use logging to trace execution

### Visualization Tools

```bash
# Visualize call graphs (EP17)
dot -Tpng src/main/resources/call.dot -o call_graph.png

# Visualize control flow graphs (EP20-EP21)
# CFGBuilder supports DOT output for graph visualization
```

## 🚧 Contributing

This is an educational project. Contributions are welcome when they:

- Add new educational content or examples
- Improve code clarity and documentation
- Fix bugs in existing code
- Enhance testing coverage

Please ensure:
1. All tests pass before submitting
2. Code follows project conventions
3. Changes are well-documented
4. Pull requests reference relevant EP(s)

## 📄 License

[Add your license here]

## 🙏 Acknowledgments

This project is inspired by:
- **"The Definitive ANTLR 4 Reference"** by Terence Parr
- **"Compilers: Principles, Techniques, and Tools"** (Dragon Book) by Aho et al.
- **Modern compiler design** patterns and best practices

## 🔗 Resources

- [ANTLR4 Documentation](https://www.antlr.org/)
- [ANTLR4 Grammar Repository](https://github.com/antlr/grammars-v4)
- [Maven Documentation](https://maven.apache.org/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

**Happy Compiling!** 🎉

Start with EP17-EP21 for the most modern compiler architecture, or explore EP1-EP16 to understand the progressive development journey.
