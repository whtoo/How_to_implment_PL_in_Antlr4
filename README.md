# How to Implement a Programming Language in ANTLR4

A progressive, hands-on guide to building a complete compiler from scratch using ANTLR4 and Java 21.

## 📖 Overview

This educational project teaches compiler construction through **progressive episodes (EPs)**, each building upon the previous. Starting with basic parsing and advancing to code generation, you'll build a complete compiler for the **Cymbol** programming language.

### What You'll Build

- A full-featured compiler pipeline: **lexing → parsing → AST → type checking → IR generation → code generation**
- A virtual machine (VM) for executing compiled programs
- Tools for analysis: symbol tables, control flow graphs, intermediate representations

## 🎯 Learning Path

### Phase 0: Hello, Compiler (EP01)
**Goal**: Get started with ANTLR4 and see a working compiler

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP01 | Hello, Compiler | Grammar definition, parsing basics, expression evaluation |

### Phase 1: Parsing (EP02–EP04)
**Goal**: Understand how source code becomes structured data

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP02 | Tokens | Lexical analysis, manual lexer vs. generated lexer |
| EP03 | Grammar | Parse trees, operator precedence, rule matching |
| EP04 | AST | Abstract syntax trees, visitor pattern, tree traversal |

**Outcome**: A working interpreter that evaluates expressions via AST traversal.

### Phase 2: Meaning (EP05–EP07)
**Goal**: Give meaning to programs through symbols and types

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP05 | Symbol Tables | Scoping, variable declarations, name resolution |
| EP06 | Types | Built-in types, type tables, function signatures |
| EP07 | Semantic Analysis | Static type checking, error reporting |

**Outcome**: A semantic analyzer that validates program correctness.

### Phase 3: IR & Flow (EP08–EP09)
**Goal**: Transform high-level code into intermediate representations

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP08 | IR Generation | Three-address code, IR instructions, function bodies |
| EP09 | CFG & LIR | Control flow graphs, basic blocks, low-level IR |

**Outcome**: A compiler front-end that generates structured intermediate code.

### Phase 4: Execution (EP10–EP11)
**Goal**: Run compiled code on a virtual machine

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP10 | VM Assembler | Stack-based VM, instruction set, bytecode assembly |
| EP11 | Code Generation | Compiler pipeline, code generation, VM integration |

**Outcome**: A complete compiler that generates and executes VM bytecode.

### Phase 5: Optimization (EP12–EP15)
**Goal**: Apply classic compiler optimizations

| EP | Topic | Key Concepts |
|----|-------|--------------|
| EP12 | Register Allocation | Linear scan algorithm, live ranges, spills |
| EP13 | SSA Form | Dominator tree, dominance frontiers, phi insertion, variable renaming |
| EP14 | Classic Optimizations | Dead code elimination, constant propagation, CSE, copy propagation |
| EP15 | TRO & GC | Tail recursion optimization, reference counting garbage collection |

**Outcome**: A production-quality compiler with advanced optimizations and memory management.

## 🏗️ Project Structure

```
├── ep01–ep15/          # Main compiler episodes (ep01–ep11 implemented)
│   ├── ep01/           # Hello, Compiler
│   ├── ep02/           # Tokens
│   ├── ep03/           # Grammar
│   ├── ep04/           # AST
│   ├── ep05/           # Symbol Tables
│   ├── ep06/           # Types
│   ├── ep07/           # Semantic Analysis
│   ├── ep08/           # IR Generation
│   ├── ep09/           # CFG & LIR
│   ├── ep10/           # VM Assembler
│   ├── ep11/           # Code Generation
│   ├── ep12/           # Register Allocation
│   ├── ep13/           # SSA Form
│   ├── ep14/           # Classic Optimizations
│   ├── ep15/           # TRO & GC
├── book/               # Book writing project: "How to Implement a PL in ANTLR4"
├── pom.xml             # Parent Maven POM (builds ep01–ep15)
├── AGENTS.md           # AI agent development guide
└── README.md           # This file
```

### Active Modules

The root POM currently builds **EP01–EP15**:
```xml
<modules>
    <module>ep01</module>
    <module>ep02</module>
    <module>ep03</module>
    <module>ep04</module>
    <module>ep05</module>
    <module>ep06</module>
    <module>ep07</module>
    <module>ep08</module>
    <module>ep09</module>
    <module>ep10</module>
    <module>ep11</module>
    <module>ep12</module>
    <module>ep13</module>
    <module>ep14</module>
    <module>ep15</module>
</modules>
```

## 📚 The Book Project

The `book/` directory contains the source for the companion book:

- **00_front_matter** — Preface and introduction
- **01_fundamentals** — Chapters 1–4: Basics of parsing and AST
- **02_language_features** — Chapters 6–9: Types, symbols, and semantics
- **03_compilation_basics** — Chapters 10–12: IR, CFG, and code generation
- **04_modern_architecture** — Chapters 13–16: Advanced topics
- **05_advanced_topics** — Chapters 17–20: Optimizations and future work

See `book/BOOK_IMPLEMENTATION_PLAN.md` for the writing roadmap.

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

# Build all active modules (EP01–EP15)
mvn clean compile

# Run all tests
mvn test

# Build a specific module
cd ep11
mvn clean compile test
```

### Running an Episode

```bash
# Run EP01 compiler
cd ep01
mvn exec:java -Dexec.args="src/main/resources/t.cymbol"

# Run EP11 compiler (end-to-end pipeline)
cd ep11
mvn exec:java -Dexec.args="src/main/resources/t.cymbol"
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

- **EP01–EP04**: Basic types, arithmetic, control flow, functions, AST
- **EP05–EP07**: Scoping, local/global variables, static typing, type checking
- **EP08–EP09**: Intermediate representation, control flow graphs
- **EP10–EP11**: Bytecode generation, virtual machine execution
- **EP12**: Register allocation (linear scan)
- **EP13**: SSA form construction
- **EP14**: Dead code elimination, constant propagation, CSE
- **EP15**: Tail recursion optimization, garbage collection

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

## 🧪 Testing

```bash
# Run all tests across all active modules
mvn test

# Run tests for a specific module
cd ep11
mvn test

# Run a specific test class
mvn test -Dtest=CompilerPipelineTest

# Run a specific test method
mvn test -Dtest=IRGenerationTest#testSimpleFunction
```

## 📖 Module Documentation

## 🎓 Learning Outcomes

By completing this project, you'll master:

1. **Language Theory**: Lexical analysis, parsing, grammars, AST design
2. **Compiler Design**: Multi-pass compilation, intermediate representations
3. **ANTLR4**: Grammar definition, visitor pattern, tree traversal
4. **Virtual Machines**: Stack-based execution, instruction sets, memory management
5. **Software Engineering**: Modular design, clean code, best practices

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

1. **Identify the EP** you need to work with (use EP11 for the latest complete pipeline)
2. **Navigate** to the EP directory (`cd epXX`)
3. **Build** the module (`mvn clean compile`)
4. **Test** your changes (`mvn test`)
5. **Respect** module boundaries — don't break encapsulation

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

Start with EP01 and progress through EP11 to build a complete compiler from scratch.
