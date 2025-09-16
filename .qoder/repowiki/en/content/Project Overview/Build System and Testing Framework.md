# Build System and Testing Framework

<cite>
**Referenced Files in This Document**   
- [pom.xml](file://ep19/pom.xml)
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [TypeCheckerTest.java](file://ep20/src/test/java/org/teachfx/antlr4/ep20/pass/sematic/TypeCheckerTest.java)
- [run.sh](file://scripts/run.sh)
- [run.py](file://scripts/run.py)
- [log4j2.xml](file://ep19/src/main/resources/log4j2.xml)
</cite>

## Table of Contents
1. [Maven Build Infrastructure](#maven-build-infrastructure)
2. [Dependency Management](#dependency-management)
3. [Compilation and Execution Scripts](#compilation-and-execution-scripts)
4. [Testing Methodology](#testing-methodology)
5. [Unit Testing Strategy](#unit-testing-strategy)
6. [Integration and End-to-End Testing](#integration-and-end-to-end-testing)
7. [Test Organization and Assertion Patterns](#test-organization-and-assertion-patterns)
8. [Writing and Running Tests](#writing-and-running-tests)
9. [Code Coverage and Continuous Integration](#code-coverage-and-continuous-integration)

## Maven Build Infrastructure

The project utilizes Apache Maven as its primary build automation tool, configured through the `pom.xml` file located in each episode directory (e.g., `ep19/pom.xml`). This standardized build configuration enables consistent compilation, dependency resolution, and execution across the project's progressive implementations. The Maven POM defines the project structure, specifies source directories, configures compilation plugins, and manages test execution via the Surefire plugin. The build lifecycle supports standard phases including `compile`, `test`, `package`, and `clean`, ensuring reproducible builds across development environments.

**Section sources**
- [pom.xml](file://ep19/pom.xml#L1-L100)

## Dependency Management

The Maven configuration centrally manages dependencies critical to the compiler implementation. Key dependencies include:
- **ANTLR4**: Used for generating lexers and parsers from grammar specifications, enabling syntactic analysis of source code.
- **JUnit 5**: Provides the foundation for unit and integration testing, supporting assertions, test lifecycle management, and parameterized tests.
- **Log4j2**: Handles logging requirements across compiler phases, allowing traceability of execution flow and error diagnostics.

These dependencies are declared in the `pom.xml` with appropriate scopes (`compile` for ANTLR4 runtime, `test` for JUnit), ensuring correct classpath configuration during build and test execution. The use of Maven coordinates guarantees version consistency and simplifies dependency updates across the codebase.

**Section sources**
- [pom.xml](file://ep19/pom.xml#L20-L60)

## Compilation and Execution Scripts

The project provides cross-platform execution scripts in the `scripts/` directory to simplify compilation and running of compiler implementations. The `run.sh` script enables execution on Unix-like systems, while `run.py` offers platform-independent invocation using Python. These scripts abstract the underlying Maven commands, allowing developers to compile and execute specific compiler episodes without memorizing complex command-line arguments. The scripts typically accept parameters to specify the target episode, input files, and execution mode, providing a consistent interface across development environments.

**Section sources**
- [run.sh](file://scripts/run.sh#L1-L30)
- [run.py](file://scripts/run.py#L1-L40)

## Testing Methodology

The project implements a comprehensive, multi-layered testing strategy to validate compiler correctness at various levels of abstraction. This approach ensures robustness across individual components and their integrated behavior. The testing pyramid includes:
- **Unit tests**: Validate isolated components such as AST nodes, symbol table entries, and visitor implementations.
- **Integration tests**: Verify interactions between compiler phases (parsing, semantic analysis, code generation).
- **End-to-end tests**: Confirm complete compilation workflows from source input to executable output.

This hierarchical testing approach enables early detection of defects while ensuring that component-level correctness translates to system-level reliability.

**Section sources**
- [pom.xml](file://ep19/pom.xml#L70-L90)

## Unit Testing Strategy

Unit testing focuses on individual components within the compiler architecture, with dedicated test classes such as `TypeCheckerTest.java`. These tests employ mocking strategies to isolate components from their dependencies, using JUnit's assertion framework to validate expected behavior. Common patterns include:
- Testing visitor implementations against controlled AST structures
- Validating symbol table operations (insertion, lookup, scoping)
- Verifying type checking rules and error reporting mechanisms

Test cases are designed to cover both normal operation and edge cases, including invalid inputs and error recovery scenarios. The use of parameterized tests allows efficient validation of multiple input-output pairs within a single test method.

**Section sources**
- [TypeCheckerTest.java](file://ep20/src/test/java/org/teachfx/antlr4/ep20/pass/sematic/TypeCheckerTest.java#L1-L100)

## Integration and End-to-End Testing

Integration and end-to-end testing validate the compiler's complete functionality through comprehensive test suites like `ComprehensiveTest.java`. These tests verify the correct sequencing of compiler phases and the integrity of intermediate representations. The `ComprehensiveTest.java` class orchestrates full compilation workflows, from parsing source code to generating executable output, ensuring that all components work together as intended. Integration tests specifically examine phase interactions, such as how semantic analysis results influence code generation, while end-to-end tests validate the compiler's ability to process realistic input programs and produce correct executable artifacts.

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java#L1-L150)

## Test Organization and Assertion Patterns

Tests are organized by component and responsibility, following the same package structure as the production code. Each test class focuses on a specific aspect of compiler functionality, with clearly named test methods that describe the scenario being validated. Assertion patterns emphasize clarity and precision, using JUnit's rich assertion library to verify:
- Expected exceptions during error conditions
- Structural integrity of generated ASTs
- Correctness of symbol table contents
- Accuracy of generated intermediate or machine code

Test fixtures are carefully constructed to minimize setup complexity while maximizing coverage. The use of helper methods in classes like `CompilerTestUtil.java` promotes code reuse across test cases while maintaining readability.

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java#L50-L120)
- [TypeCheckerTest.java](file://ep20/src/test/java/org/teachfx/antlr4/ep20/pass/sematic/TypeCheckerTest.java#L30-L80)

## Writing and Running Tests

New tests should be added to the appropriate package under `src/test/java`, following the naming convention `*Test.java`. Developers should leverage existing test utilities and adhere to established patterns for fixture creation and assertion. Tests can be executed through multiple channels:
- Direct invocation via Maven: `mvn test`
- IDE integration using built-in JUnit runners
- Script-based execution using the provided `run.sh` or `run.py` scripts

Test results are reported in standard Surefire output format, with detailed information about passed, failed, and skipped tests. The build fails if any test does not pass, enforcing test-driven development practices.

**Section sources**
- [pom.xml](file://ep19/pom.xml#L90-L120)
- [run.sh](file://scripts/run.sh#L20-L30)
- [run.py](file://scripts/run.py#L30-L40)

## Code Coverage and Continuous Integration

While explicit code coverage requirements are not specified in the current configuration, the comprehensive test suite suggests a strong emphasis on test coverage. The Maven build integrates with standard CI/CD pipelines, where test execution is automated on code changes. The presence of consistent logging configuration via `log4j2.xml` facilitates debugging in CI environments. Future enhancements could include integration with coverage tools like JaCoCo to enforce minimum coverage thresholds and generate visual reports. The modular episode structure supports incremental testing, allowing CI pipelines to validate changes in specific compiler phases without requiring full regression testing.

**Section sources**
- [pom.xml](file://ep19/pom.xml#L120-L150)
- [log4j2.xml](file://ep19/src/main/resources/log4j2.xml#L1-L50)