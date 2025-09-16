# Testing Strategy

<cite>
**Referenced Files in This Document**   
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [CompilationResult.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilationResult.java)
- [TypeCheckVisitor.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/TypeCheckVisitor.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Unit Testing Strategy](#unit-testing-strategy)
3. [Integration Testing](#integration-testing)
4. [End-to-End Testing](#end-to-end-testing)
5. [Test Organization](#test-organization)
6. [Test Case Examples](#test-case-examples)
7. [Test Fixtures and Output Verification](#test-fixtures-and-output-verification)
8. [Test Coverage Measurement](#test-coverage-measurement)
9. [Writing New Tests](#writing-new-tests)
10. [Performance Benchmarking](#performance-benchmarking)
11. [Regression Testing](#regression-testing)

## Introduction
The testing framework for the Cymbol compiler is designed to ensure robustness, correctness, and performance across all compilation phases. The strategy employs a multi-layered approach with unit tests for individual components, integration tests for phase interactions, and end-to-end tests for complete program compilation and execution. The framework uses JUnit 5 as the primary testing framework and includes comprehensive performance benchmarking capabilities.

## Unit Testing Strategy

The unit testing strategy focuses on verifying individual compiler components in isolation. Each compilation phase has dedicated test classes that validate functionality through targeted test cases.

```mermaid
classDiagram
class CompilerTestUtil {
+compile(String, boolean) CompilationResult
+CompilationResult class
}
class ComprehensiveTest {
+testTypeCompatibilityInAssignment()
+testTypeCompatibilityInArithmetic()
+testTypeCompatibilityInComparison()
+testTypeCompatibilityInFunctionCall()
+testTypeCompatibilityInReturn()
+testSimpleArithmeticExpression()
+testSimpleBooleanExpression()
+testNestedIfStatements()
+testNestedLoops()
+testRecursiveFunction()
+testSimpleRecursion()
+testUndefinedVariable()
+testUndefinedFunction()
+testWrongNumberOfArguments()
+testReturnTypeMismatch()
+testVoidFunctionWithReturn()
+testInvalidOperandTypes()
}
class TypeCheckVisitor {
+visitVarDecl()
+visitStatAssign()
+visitExprBinary()
+visitExprBinaryMulDivPercent()
+visitExprLogicalAnd()
+visitExprUnary()
+visitExprFuncCall()
+visitExprStructMethodCall()
+visitExprGroup()
+visitExprArrayAccess()
+visitExprStructFieldAccess()
+visitStateCondition()
+visitStateWhile()
+visitStatReturn()
+visitPrimaryID()
+visitPrimaryINT()
+visitPrimaryFLOAT()
+visitPrimarySTRING()
+visitPrimaryCHAR()
+visitPrimaryBOOL()
}
CompilerTestUtil --> ComprehensiveTest : "used by"
TypeCheckVisitor --> ComprehensiveTest : "tested by"
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [TypeCheckVisitor.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/TypeCheckVisitor.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [TypeCheckVisitor.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/TypeCheckVisitor.java)

## Integration Testing

Integration tests verify the interaction between compilation phases, ensuring that the output of one phase correctly serves as input to the next phase in the compilation pipeline.

```mermaid
sequenceDiagram
participant Test as "IntegrationTest"
participant Util as "CompilerTestUtil"
participant Pipeline as "CompilerPipeline"
participant Lexer as "CymbolLexer"
participant Parser as "CymbolParser"
participant SymbolDefine as "LocalDefine"
participant SymbolResolve as "LocalResolver"
participant TypeCheck as "TypeCheckVisitor"
participant Interpreter as "Interpreter"
Test->>Util : compile(code, true)
Util->>Pipeline : lexicalAnalysis()
Pipeline->>Lexer : Create token stream
Pipeline-->>Util : Token stream
Util->>Pipeline : syntaxAnalysis()
Pipeline->>Parser : Generate parse tree
Pipeline-->>Util : Parse tree
Util->>SymbolDefine : accept()
SymbolDefine-->>Util : Symbol information
Util->>SymbolResolve : accept()
SymbolResolve-->>Util : Type information
Util->>TypeCheck : accept()
TypeCheck-->>Util : Type checking results
Util->>Interpreter : interpret()
Interpreter-->>Util : Execution output
Util-->>Test : CompilationResult
```

**Diagram sources**
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)

**Section sources**
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)

## End-to-End Testing

End-to-end tests validate the complete compilation and execution process, from source code input to program output. These tests ensure that the entire compiler pipeline functions correctly.

```mermaid
flowchart TD
Start([Start Test]) --> Compile["Compile Source Code"]
Compile --> ParseTree{"Parse Tree Generated?"}
ParseTree --> |No| ReportError["Report Syntax Error"]
ParseTree --> |Yes| SymbolDefine["Perform Symbol Definition"]
SymbolDefine --> ScopeUtil["Create Scope Utility"]
ScopeUtil --> SymbolResolve["Perform Symbol Resolution"]
SymbolResolve --> TypeCheck["Perform Type Checking"]
TypeCheck --> TypeErrors{"Type Errors?"}
TypeErrors --> |Yes| ReportTypeError["Report Type Error"]
TypeErrors --> |No| Interpret["Interpret Execution"]
Interpret --> RuntimeErrors{"Runtime Errors?"}
RuntimeErrors --> |Yes| ReportRuntimeError["Report Runtime Error"]
RuntimeErrors --> |No| VerifyOutput["Verify Output"]
VerifyOutput --> End([Test Complete])
ReportError --> End
ReportTypeError --> End
ReportRuntimeError --> End
```

**Diagram sources**
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)

**Section sources**
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)

## Test Organization

The test suite is organized by component and phase, following the structure of the compiler architecture. Each major component has its own test class, and tests are grouped by functionality.

```mermaid
graph TD
Tests[Tests] --> Unit[Unit Tests]
Tests --> Integration[Integration Tests]
Tests --> EndToEnd[End-to-End Tests]
Tests --> Performance[Performance Tests]
Unit --> Comprehensive[ComprehensiveTest]
Unit --> TypeSystem[TypeSystemTest]
Unit --> StructTypedef[StructAndTypedefTest]
Unit --> FunctionMethod[FunctionAndMethodTest]
Unit --> ErrorRecovery[ErrorRecoveryTest]
Integration --> IntegrationTest[IntegrationTest]
EndToEnd --> Comprehensive[ComprehensiveTest]
EndToEnd --> IntegrationTest[IntegrationTest]
Performance --> PerformanceBenchmark[PerformanceBenchmarkTest]
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

## Test Case Examples

The test suite includes comprehensive examples for edge cases and error conditions, ensuring robust handling of various scenarios.

```mermaid
classDiagram
class ComprehensiveTest {
+testImplicitTypeConversion()
+testImplicitFloatToIntConversion()
+testTypeCompatibilityInAssignment()
+testTypeCompatibilityInArithmetic()
+testTypeCompatibilityInComparison()
+testTypeCompatibilityInFunctionCall()
+testTypeCompatibilityInReturn()
+testSimpleArithmeticExpression()
+testSimpleBooleanExpression()
+testNestedIfStatements()
+testNestedLoops()
+testRecursiveFunction()
+testSimpleRecursion()
+testUndefinedVariable()
+testUndefinedFunction()
+testWrongNumberOfArguments()
+testReturnTypeMismatch()
+testVoidFunctionWithReturn()
+testInvalidOperandTypes()
}
class IntegrationTest {
+testBasicArithmetic()
+testVariableDeclarationAndAssignment()
+testFunctionCallAndReturn()
+testIfStatement()
+testWhileLoop()
+testStructDeclarationAndUsage()
+testStructMethodCall()
+testNestedStructs()
+testTypedef()
+testSimpleProgram()
+testComplexProgram()
+testRuntimeError()
}
class PerformanceBenchmarkTest {
+benchmarkSmallProgram()
+benchmarkMediumProgram()
+benchmarkLargeProgram()
+benchmarkNestedStructs()
+benchmarkComplexProgram()
+compareWithPreviousVersion()
}
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

## Test Fixtures and Output Verification

The testing framework uses test fixtures and expected output verification to ensure consistent and reliable test results.

```mermaid
sequenceDiagram
participant Test as "Test Method"
participant Util as "CompilerTestUtil"
participant System as "System Streams"
participant Logger as "CompilerLogger"
Test->>Util : compile(code, interpret)
Util->>System : Capture System.err
Util->>System : Capture System.out
Util->>Logger : Set error listener
Util->>Util : Perform compilation phases
Util->>Logger : Collect error messages
Util->>System : Restore System streams
Util->>Logger : Reset error listener
Util-->>Test : Return CompilationResult
Test->>Test : Verify result.success
Test->>Test : Verify errors.isEmpty()
Test->>Test : Verify output contains expected text
```

**Diagram sources**
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)

**Section sources**
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)

## Test Coverage Measurement

Test coverage is measured through comprehensive test cases that validate all major functionality paths and error conditions in the compiler.

```mermaid
graph TD
Coverage[Test Coverage] --> Unit[Unit Test Coverage]
Coverage --> Integration[Integration Test Coverage]
Coverage --> EndToEnd[End-to-End Test Coverage]
Coverage --> Performance[Performance Test Coverage]
Unit --> TypeChecking[Type Checking]
Unit --> Expression[Expression Evaluation]
Unit --> Statement[Statement Processing]
Unit --> ControlFlow[Control Flow]
Unit --> Function[Function Handling]
Unit --> Struct[Struct Handling]
Unit --> Typedef[Typedef Handling]
Integration --> PhaseInteraction[Phase Interactions]
Integration --> ErrorRecovery[Error Recovery]
Integration --> SymbolResolution[Symbol Resolution]
EndToEnd --> ProgramExecution[Program Execution]
EndToEnd --> OutputVerification[Output Verification]
EndToEnd --> RuntimeError[Runtime Error Handling]
Performance --> CompilationTime[Compilation Time]
Performance --> MemoryUsage[Memory Usage]
Performance --> Scalability[Scalability]
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

## Writing New Tests

When extending the compiler, new tests should follow the established patterns and conventions to maintain consistency and coverage.

```mermaid
flowchart TD
Start([Start New Test]) --> Identify[Identify Test Type]
Identify --> UnitTest{"Unit Test?"}
Identify --> IntegrationTest{"Integration Test?"}
Identify --> EndToEndTest{"End-to-End Test?"}
Identify --> PerformanceTest{"Performance Test?"}
UnitTest --> ChooseClass["Choose appropriate test class"]
IntegrationTest --> ChooseClass
EndToEndTest --> ChooseClass
PerformanceTest --> ChooseClass
ChooseClass --> CreateMethod["Create test method"]
CreateMethod --> Arrange["Arrange: Set up test data"]
Arrange --> Act["Act: Execute test"]
Act --> Assert["Assert: Verify results"]
Assert --> Cleanup["Cleanup: Reset state"]
Cleanup --> End([Test Complete])
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

## Performance Benchmarking

Performance benchmarking tests measure compilation time and memory usage for different program sizes and complexities.

```mermaid
classDiagram
class PerformanceBenchmarkTest {
+setUp()
+tearDown()
+measurePerformance()
+saveResults()
+generateProgram()
+generateNestedStructProgram()
+benchmarkSmallProgram()
+benchmarkMediumProgram()
+benchmarkLargeProgram()
+benchmarkNestedStructs()
+benchmarkComplexProgram()
+compareWithPreviousVersion()
+BenchmarkResult class
}
class BenchmarkResult {
+compilationTime
+memoryUsed
+success
}
PerformanceBenchmarkTest --> BenchmarkResult : "contains"
```

**Diagram sources**
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

## Regression Testing

Regression testing is performed through the comprehensive test suite, ensuring that new changes do not break existing functionality.

```mermaid
sequenceDiagram
participant Developer as "Developer"
participant CI as "CI System"
participant TestSuite as "Test Suite"
participant Results as "Results"
Developer->>CI : Commit changes
CI->>TestSuite : Run unit tests
TestSuite-->>CI : Results
CI->>TestSuite : Run integration tests
TestSuite-->>CI : Results
CI->>TestSuite : Run end-to-end tests
TestSuite-->>CI : Results
CI->>TestSuite : Run performance tests
TestSuite-->>CI : Results
CI->>Results : Aggregate results
Results-->>Developer : Report status
alt All tests pass
Results->>Developer : "Changes accepted"
else Any test fails
Results->>Developer : "Changes rejected - fix issues"
end
```

**Diagram sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)

**Section sources**
- [ComprehensiveTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/ComprehensiveTest.java)
- [IntegrationTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/IntegrationTest.java)
- [PerformanceBenchmarkTest.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/PerformanceBenchmarkTest.java)