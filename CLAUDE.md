# CLAUDE.md

## MCP Servers

### tree-sitter-mcp
- **Installation**: `@nendo/tree-sitter-mcp`
- **Command**: `npx @nendo/tree-sitter-mcp --mcp`
- **Description**: 语义代码搜索和分析

### Context7 and Tree-sitter (Global MCP)

This project supports global MCP installation for enhanced code analysis.

#### Global Installation Methods

**Using npm (npx):**
```bash
# Install tree-sitter MCP globally
npm install -g @nendo/tree-sitter-mcp

# Install other useful MCP servers globally  
npm install -g @modelcontextprotocol/server-filesystem
npm install -g @modelcontextprotocol/server-git
npm install -g @modelcontextprotocol/server-postgres
```

**Using uv (Python package manager):**
```bash
# Install Context7 MCP server
uvx context7-mcp-server

# Install other Python-based MCP servers
uvx postgres-mcp-server
uvx filesystem-mcp-server
```

#### Configuration
The project's MCP configuration is located in `.roo/mcp.json` and includes:
- **tree-sitter**: `npx -y @nendo/tree-sitter-mcp`
- **context7**: `uvx context7-mcp-server` 
- **filesystem**: `@modelcontextprotocol/server-filesystem`
- **git**: `@modelcontextprotocol/server-git`

#### Usage in Claude Code
When using Claude Code with this repository, the MCP servers will be automatically available for:
- Code analysis and semantic search
- File system navigation and git integration
- Database connectivity (PostgreSQL)
- Context-aware development assistance

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

## Claude Code Skills

### Compiler Development Skill

This project includes a specialized **Compiler Development** skill that provides comprehensive guidance for working with the Cymbol language compiler implementation. The skill is located at `.claude/skills/compiler-development/SKILL.md`.

#### Skill Overview

The Compiler Development skill is a Claude Code skill that activates automatically when users ask about compiler-related topics. It provides expertise across the complete compiler pipeline implemented in this 21-episode educational project.

#### Skill Capabilities

The Compiler Development skill can assist with:

1. **ANTLR4 Grammar Design and Analysis**
   - Explain Cymbol language syntax and grammar rules
   - Debug parsing errors, ambiguities, and grammar conflicts
   - Guide grammar modifications, extensions, and best practices

2. **Abstract Syntax Tree (AST) Development**
   - Explain AST node types, hierarchy, and design patterns
   - Help traverse, analyze, and manipulate AST structures
   - Guide AST transformation and visitor pattern implementation

3. **Type System and Symbol Table Implementation**
   - Explain type checking, type inference, and type safety
   - Debug type-related errors and type conversion issues
   - Guide symbol table construction, scope management, and name resolution

4. **Intermediate Representation and Optimization**
   - Explain three-address code generation and IR design
   - Analyze control flow graphs (CFG) and basic blocks
   - Guide IR optimization techniques and dataflow analysis
   - Explain SSA form construction and optimization passes

5. **Code Generation and Virtual Machine**
   - Explain bytecode generation for stack-based virtual machine
   - Debug code generation issues and register allocation
   - Guide VM implementation, execution, and memory management
   - Explain garbage collection and runtime system design

6. **Project Navigation and Development Workflow**
   - Navigate the 21-episode progressive structure (EP1-EP21)
   - Explain module dependencies, interactions, and build system
   - Guide testing strategies, coverage requirements, and TDD practices
   - Provide development scripts and multi-platform support

#### When to Use the Skill

The Compiler Development skill activates when users ask about:
- ANTLR4 grammar design, parsing, and debugging
- Cymbol language syntax, semantics, and language design
- Compiler construction concepts, phases, and implementation
- Type systems, symbol tables, semantic analysis, and name resolution
- Intermediate representation, control flow, optimization, and code generation
- Virtual machine design, bytecode execution, and runtime systems
- Project-specific module navigation, testing, and development workflows

#### Usage Examples

```bash
# Example: Debug a complex parsing issue
User: My Cymbol program with nested structs fails to parse with "mismatched input"

Compiler Development: This is likely a grammar ambiguity issue. Let me analyze...
The problem may be in the structMember rule conflicting with varDecl.
Try adding syntactic predicates or restructuring the grammar rules.

# Example: Understand the compilation pipeline
User: How does type information flow through the compiler phases?

Compiler Development: Type information flows through these phases:
1. Lexing/Parsing: Token streams → Parse trees
2. AST Construction: Parse trees → AST with type placeholders
3. Symbol Collection: AST → Symbol table with type declarations
4. Type Checking: AST + Symbol table → Fully typed AST
5. IR Generation: Typed AST → Typed intermediate representation
Check TypeChecker.java and SymbolTable.java in ep20 for details.
```

#### Skill Location and Activation

- **Skill Directory**: `.claude/skills/compiler-development/`
- **Skill File**: `SKILL.md` (with YAML frontmatter)
- **Activation**: Automatic based on skill description matching compiler-related topics
- **Scope**: Project-specific knowledge of the complete Cymbol compiler implementation
- **Allowed Tools**: Read, Grep, Glob (for code exploration and analysis)

#### Skill Structure

The skill follows Claude Code's skill format:
- **YAML Frontmatter**: Defines skill name, description, and allowed tools
- **Project Overview**: Context about the 21-episode compiler project
- **Core Development Flow**: Detailed guidance for each compiler phase
- **Project Navigation**: Key file locations and testing strategies
- **Development Workflows**: Common tasks and best practices
- **Troubleshooting**: Solutions for common compiler development issues

#### Extending the Skill

To extend or modify the Compiler Development skill:
1. Edit `.claude/skills/compiler-development/SKILL.md`
2. Update the YAML frontmatter description if adding new capabilities
3. Add new sections for additional compiler topics or features
4. Update this section in CLAUDE.md to reflect changes
5. Test the skill with compiler-related queries to ensure proper activation

#### Integration with Development Environment

The skill integrates with the project's development tools:
- **Maven Build System**: Module-specific build and test commands
- **Multi-platform Scripts**: `scripts/` directory for Linux, macOS, and Windows
- **Testing Framework**: JUnit 5 with coverage requirements (≥85% overall)
- **Documentation**: `.qoder/repowiki/` with 232+ technical documentation files

## Notes for Future Claude Code Instances

- This is an **educational project** focused on compiler construction
- The 21-episode structure is **progressive** - later episodes depend on earlier ones
- **ANTLR4** is central to the project - understand grammar files and generated code
- **Visitor pattern** is used extensively for AST traversal
- **Testing is comprehensive** - maintain high coverage standards
- **Documentation is extensive** - check `.qoder/repowiki/` for detailed technical docs
- The project implements a **complete compiler pipeline** - understand the frontend/middle-end/backend separation