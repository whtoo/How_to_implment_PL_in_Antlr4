# Modular Compiler Pipeline

<cite>
**Referenced Files in This Document**   
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)
- [README.md](file://ep19/README.md)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Project Structure](#project-structure)
3. [Core Components](#core-components)
4. [Architecture Overview](#architecture-overview)
5. [Detailed Component Analysis](#detailed-component-analysis)
6. [Dependency Analysis](#dependency-analysis)
7. [Performance Considerations](#performance-considerations)
8. [Troubleshooting Guide](#troubleshooting-guide)
9. [Conclusion](#conclusion)

## Introduction
The Modular Compiler Pipeline architecture represents a significant evolution in compiler design, enabling configurable compilation workflows through flexible abstractions. This document details the implementation of the CompilerPipeline interface and its supporting components, which allow for customizable compilation stages, extensible phase definitions, and adaptable execution flows. The system has evolved from earlier implementations to support both default and configurable compilation workflows, providing a foundation for static analysis tools, incremental compilation, and specialized compilation scenarios.

## Project Structure
The modular compiler pipeline is organized within the ep19 and ep20 directories, with core pipeline components located in the `pipeline` package. The architecture follows a layered approach with clear separation between compilation phases, configuration mechanisms, and execution logic. The pipeline components are designed to work with the existing compiler infrastructure while providing enhanced configurability and extensibility.

```mermaid
graph TD
subgraph "ep19"
pipeline[pipeline package]
compiler[Compiler.java]
pass[pass package]
symtab[symtab package]
end
subgraph "ep20"
driver[driver package]
ast[ast package]
ir[ir package]
end
compiler --> pipeline
pipeline --> driver
pass --> pipeline
symtab --> pipeline
driver --> ast
driver --> ir
```

**Diagram sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)

## Core Components
The modular compiler pipeline is built around three core components: the CompilerPipeline interface, its DefaultCompilerPipeline implementation, and the ConfigurableCompilerPipeline extension. These components work together to provide a flexible foundation for compilation workflows. The Phase and Task abstractions from ep20 further enhance the pipeline's capabilities by introducing composable compilation stages.

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)

## Architecture Overview
The modular compiler pipeline architecture implements a configurable workflow system that orchestrates compilation stages through well-defined interfaces and abstractions. The design enables both standard compilation processes and customized workflows through dependency injection and functional programming patterns.

```mermaid
graph TD
A[Source Code] --> B[CompilerPipeline]
B --> C[Lexical Analysis]
C --> D[Syntax Analysis]
D --> E[Symbol Definition]
E --> F[Symbol Resolution]
F --> G[Type Checking]
G --> H[Interpretation]
H --> I[Execution Result]
B --> J[ConfigurableCompilerPipeline]
J --> K[Custom Phases]
K --> L[Static Analysis]
K --> M[Code Generation]
N[Phase<T,R>] --> O[Task<T,R>]
O --> P[then method]
P --> Q[Pipeline Composition]
style B fill:#f9f,stroke:#333
style J fill:#f9f,stroke:#333
style N fill:#bbf,stroke:#333
style O fill:#bbf,stroke:#333
```

**Diagram sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)

## Detailed Component Analysis

### CompilerPipeline Interface
The CompilerPipeline interface defines the contract for compilation workflows, specifying methods for each compilation phase. This interface enables polymorphic behavior and dependency injection, allowing different pipeline implementations to be substituted based on requirements.

```mermaid
classDiagram
class CompilerPipeline {
<<interface>>
+lexicalAnalysis(CharStream) CommonTokenStream
+syntaxAnalysis(CommonTokenStream) ParseTree
+symbolDefinition(ParseTree) LocalDefine
+symbolResolution(ParseTree, ScopeUtil) LocalResolver
+typeChecking(ParseTree, ScopeUtil, LocalResolver) TypeCheckVisitor
+interpretation(ParseTree, ScopeUtil) Object
+compile(CharStream) Object
+compileWithoutInterpretation(CharStream) ParseTree
+compileToResult(CharStream) CompilationResult
+execute(CompilationResult) Object
}
class DefaultCompilerPipeline {
-logger Logger
+lexicalAnalysis(CharStream) CommonTokenStream
+syntaxAnalysis(CommonTokenStream) ParseTree
+symbolDefinition(ParseTree) LocalDefine
+symbolResolution(ParseTree, ScopeUtil) LocalResolver
+typeChecking(ParseTree, ScopeUtil, LocalResolver) TypeCheckVisitor
+interpretation(ParseTree, ScopeUtil) Object
+compile(CharStream) Object
+compileWithoutInterpretation(CharStream) ParseTree
+compileToResult(CharStream) CompilationResult
+execute(CompilationResult) Object
}
class ConfigurableCompilerPipeline {
-logger Logger
-lexicalAnalysisPhase Function~CharStream,CommonTokenStream~
-syntaxAnalysisPhase Function~CommonTokenStream,ParseTree~
-symbolDefinitionPhase Function~ParseTree,LocalDefine~
-symbolResolutionPhase BiFunction~ParseTree,ScopeUtil,LocalResolver~
-typeCheckingPhase TriFunction~ParseTree,ScopeUtil,LocalResolver,TypeCheckVisitor~
-interpretationPhase BiFunction~ParseTree,ScopeUtil,Object~
-performInterpretation boolean
+setLexicalAnalysisPhase(Function~CharStream,CommonTokenStream~) ConfigurableCompilerPipeline
+setSyntaxAnalysisPhase(Function~CommonTokenStream,ParseTree~) ConfigurableCompilerPipeline
+setSymbolDefinitionPhase(Function~ParseTree,LocalDefine~) ConfigurableCompilerPipeline
+setSymbolResolutionPhase(BiFunction~ParseTree,ScopeUtil,LocalResolver~) ConfigurableCompilerPipeline
+setTypeCheckingPhase(TriFunction~ParseTree,ScopeUtil,LocalResolver,TypeCheckVisitor~) ConfigurableCompilerPipeline
+setInterpretationPhase(BiFunction~ParseTree,ScopeUtil,Object~) ConfigurableCompilerPipeline
+setPerformInterpretation(boolean) ConfigurableCompilerPipeline
+compile(CharStream) Object
+compileWithoutInterpretation(CharStream) ParseTree
+compileToResult(CharStream) CompilationResult
+execute(CompilationResult) Object
}
CompilerPipeline <|-- DefaultCompilerPipeline
CompilerPipeline <|-- ConfigurableCompilerPipeline
DefaultCompilerPipeline --> CustomErrorStrategy
ConfigurableCompilerPipeline --> CustomErrorStrategy
```

**Diagram sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)

### Phase and Task Abstractions
The Phase and Task abstractions provide a functional programming foundation for composable compilation stages. These components enable pipeline composition through Kleisli composition, allowing phases to be chained together with proper error handling and result propagation.

```mermaid
classDiagram
class Phase~Input,Output~ {
<<abstract>>
+name String
+Phase(String)
+transform(Input) Output
+onSucceed(Output) void
+apply(Input) Optional~Output~
}
class Task~T,R~ {
<<interface>>
+then(Task~R,V~) Task~T,V~
}
class ErrorIssuer {
<<interface>>
+hasError() boolean
+printErrors(PrintStream) void
}
Phase~Input,Output~ --> Task~Input,Output~
Phase~Input,Output~ --> ErrorIssuer
Task~T,R~ <|-- Phase~T,R~
```

**Diagram sources**
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)

**Section sources**
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)

### Pipeline Configuration and Execution
The compilation workflow is orchestrated through a sequence of phases that transform the source code through various representations. The pipeline supports both complete compilation and partial workflows for specialized use cases.

```mermaid
sequenceDiagram
participant Client as "Client Application"
participant Compiler as "Compiler"
participant Pipeline as "CompilerPipeline"
participant Phase1 as "Lexical Analysis"
participant Phase2 as "Syntax Analysis"
participant Phase3 as "Symbol Definition"
participant Phase4 as "Symbol Resolution"
participant Phase5 as "Type Checking"
participant Phase6 as "Interpretation"
Client->>Compiler : compile(source)
Compiler->>Pipeline : compile(charStream)
Pipeline->>Phase1 : lexicalAnalysis(charStream)
Phase1-->>Pipeline : tokenStream
Pipeline->>Phase2 : syntaxAnalysis(tokenStream)
Phase2-->>Pipeline : parseTree
Pipeline->>Phase3 : symbolDefinition(parseTree)
Phase3-->>Pipeline : localDefine
Pipeline->>Phase4 : symbolResolution(parseTree, scopeUtil)
Phase4-->>Pipeline : localResolver
Pipeline->>Phase5 : typeChecking(parseTree, scopeUtil, localResolver)
Phase5-->>Pipeline : typeChecker
Pipeline->>Phase6 : interpretation(parseTree, scopeUtil)
Phase6-->>Pipeline : result
Pipeline-->>Compiler : result
Compiler-->>Client : result
Note over Pipeline : Configurable workflow with<br/>optional interpretation phase
```

**Diagram sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [Compiler.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/Compiler.java)

## Dependency Analysis
The modular compiler pipeline components have well-defined dependencies that enable loose coupling and high cohesion. The architecture uses dependency injection and interface-based design to minimize direct dependencies between components.

```mermaid
graph TD
CompilerPipeline --> DefaultCompilerPipeline
CompilerPipeline --> ConfigurableCompilerPipeline
Compiler --> CompilerPipeline
DefaultCompilerPipeline --> CymbolLexer
DefaultCompilerPipeline --> CymbolParser
DefaultCompilerPipeline --> LocalDefine
DefaultCompilerPipeline --> LocalResolver
DefaultCompilerPipeline --> TypeCheckVisitor
DefaultCompilerPipeline --> Interpreter
ConfigurableCompilerPipeline --> DefaultCompilerPipeline
ConfigurableCompilerPipeline --> ScopeUtil
Phase --> Task
Phase --> ErrorIssuer
Compiler --> Phase
Compiler --> Task
style CompilerPipeline fill:#f9f,stroke:#333
style DefaultCompilerPipeline fill:#f9f,stroke:#333
style ConfigurableCompilerPipeline fill:#f9f,stroke:#333
style Phase fill:#bbf,stroke:#333
style Task fill:#bbf,stroke:#333
```

**Diagram sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [DefaultCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/DefaultCompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)
- [Task.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Task.java)

## Performance Considerations
The modular compiler pipeline introduces some overhead due to its configurable nature and functional composition patterns. However, this overhead is generally minimal compared to the computational cost of compilation phases themselves. The pipeline design allows for optimization opportunities through phase elimination, caching, and parallel execution of independent phases.

The ConfigurableCompilerPipeline implementation uses functional interfaces that may introduce slight performance penalties compared to direct method calls, but this is offset by the flexibility it provides. For performance-critical applications, the DefaultCompilerPipeline can be used directly to minimize indirection.

The Task interface's use of Optional for error handling provides clean error propagation but may create additional object allocations. However, this is generally acceptable given that compilation is typically not on a critical performance path for most applications.

## Troubleshooting Guide
When working with the modular compiler pipeline, several common issues may arise:

1. **Configuration Errors**: When using ConfigurableCompilerPipeline, ensure all phase functions are properly set before compilation begins.

2. **State Consistency**: Maintain consistency between compilation phases by ensuring that each phase receives the correct input state from the previous phase.

3. **Error Handling**: The pipeline uses Optional to represent failed phases. Always check for empty results when chaining phases.

4. **Phase Order**: Ensure compilation phases are executed in the correct order: lexical analysis → syntax analysis → symbol definition → symbol resolution → type checking → interpretation.

5. **Resource Management**: The pipeline does not automatically manage resources like file handles. Ensure proper resource cleanup in client code.

**Section sources**
- [CompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/CompilerPipeline.java)
- [ConfigurableCompilerPipeline.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pipeline/ConfigurableCompilerPipeline.java)
- [Phase.java](file://ep20/src/main/java/org/teachfx/antlr4/ep20/driver/Phase.java)

## Conclusion
The modular compiler pipeline architecture provides a flexible and extensible foundation for compilation workflows. By separating the pipeline interface from its implementations, the design enables both standard compilation processes and customized workflows. The introduction of Phase and Task abstractions enhances composability and error handling, while the ConfigurableCompilerPipeline allows for dynamic configuration of compilation stages.

This architecture supports various use cases, from complete compilation and execution to static analysis and incremental compilation. The modular design facilitates testing by allowing individual phases to be tested in isolation, and enables extensibility by providing clear extension points for custom compilation passes.

The pipeline's evolution from earlier implementations demonstrates a progression toward more flexible and configurable compilation systems, balancing performance considerations with architectural benefits. Future enhancements could include parallel phase execution, advanced caching mechanisms, and integration with external analysis tools.