# Syntactic Analysis Phase

<cite>
**Referenced Files in This Document**   
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java)
- [CymbolLexer.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolLexer.java)
- [CompilerTestUtil.java](file://ep19/src/test/java/org/teachfx/antlr4/ep19/CompilerTestUtil.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Parser and Lexer Interaction](#parser-and-lexer-interaction)
3. [Context-Free Grammar Implementation](#context-free-grammar-implementation)
4. [LL(*) Parsing Strategy](#ll-parsing-strategy)
5. [Rule Definitions for Expressions, Statements, and Declarations](#rule-definitions-for-expressions-statements-and-declarations)
6. [Error Recovery Mechanisms](#error-recovery-mechanisms)
7. [Parse Tree Construction](#parse-tree-construction)
8. [Extending the Grammar](#extending-the-grammar)
9. [Troubleshooting Parsing Conflicts](#troubleshooting-parsing-conflicts)
10. [Conclusion](#conclusion)

## Introduction
The syntactic analysis phase in the Cymbol compiler, implemented using ANTLR4, transforms a stream of tokens from the lexer into a structured ParseTree representation. This document details how CymbolParser.java implements context-free grammar rules to parse Cymbol language constructs, covering the LL(*) parsing strategy, rule definitions for various language elements, and error recovery mechanisms for malformed syntax.

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Parser and Lexer Interaction
The syntactic analysis phase begins with the lexer (CymbolLexer.java) converting source code into a token stream. The parser (CymbolParser.java) then consumes this token stream to build a ParseTree. The lexer identifies lexical tokens such as identifiers, keywords, operators, and literals, while the parser applies grammar rules to organize these tokens into a hierarchical structure representing the program's syntax.

```mermaid
sequenceDiagram
participant Source as "Source Code"
participant Lexer as "CymbolLexer"
participant TokenStream as "CommonTokenStream"
participant Parser as "CymbolParser"
participant ParseTree as "ParseTree"
Source->>Lexer : Read characters
Lexer->>TokenStream : Generate tokens
TokenStream->>Parser : Provide token stream
Parser->>Parser : Apply grammar rules
Parser->>ParseTree : Construct ParseTree
```

**Diagram sources**
- [CymbolLexer.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolLexer.java#L0-L264)
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolLexer.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolLexer.java#L0-L264)
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Context-Free Grammar Implementation
CymbolParser.java implements a context-free grammar that defines the syntax of the Cymbol programming language. The grammar consists of production rules that specify how different language constructs can be formed from tokens and other grammar rules. Each rule in the grammar corresponds to a method in the generated parser class, which returns a context object representing a node in the ParseTree.

```mermaid
classDiagram
class CymbolParser {
+file() FileContext
+varDecl() VarDeclContext
+type() TypeContext
+functionDecl() FunctionDeclContext
+block() BlockContext
+expr() ExprContext
}
class FileContext {
+functionDecl() FunctionDeclContext[]
+varDecl() VarDeclContext[]
+statetment() StatetmentContext[]
}
class VarDeclContext {
+type() TypeContext
+ID() TerminalNode
+expr() ExprContext
}
class FunctionDeclContext {
+retType TypeContext
+funcName Token
+params FormalParametersContext
+blockDef BlockContext
}
class BlockContext {
+statetment() StatetmentContext[]
}
class ExprContext {
+expr() ExprContext[]
+o Token
}
FileContext --> CymbolParser : "produces"
VarDeclContext --> CymbolParser : "produces"
FunctionDeclContext --> CymbolParser : "produces"
BlockContext --> CymbolParser : "produces"
ExprContext --> CymbolParser : "produces"
```

**Diagram sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## LL(*) Parsing Strategy
ANTLR4 employs an LL(*) parsing strategy, which is a top-down parsing approach that can handle a wide range of context-free grammars. The LL(*) parser uses adaptive prediction to determine which production rule to apply at each parsing decision point. This allows the parser to handle grammars with arbitrary lookahead requirements, making it suitable for complex programming languages like Cymbol.

```mermaid
flowchart TD
Start([Parse Start]) --> Predict["Predict Next Rule"]
Predict --> MatchToken["Match Expected Token"]
MatchToken --> ValidToken{"Token Valid?"}
ValidToken --> |Yes| ContinueParsing["Continue Parsing"]
ValidToken --> |No| ErrorRecovery["Error Recovery"]
ContinueParsing --> EndRule["End of Rule?"]
EndRule --> |No| Predict
EndRule --> |Yes| ReturnContext["Return Context Object"]
ErrorRecovery --> AttemptRecovery["Attempt Recovery"]
AttemptRecovery --> RecoverySuccess{"Recovery Successful?"}
RecoverySuccess --> |Yes| Predict
RecoverySuccess --> |No| FailParse["Fail Parse"]
ReturnContext --> End([Parse Complete])
FailParse --> End
```

**Diagram sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Rule Definitions for Expressions, Statements, and Declarations
The Cymbol grammar defines specific rules for expressions, statements, and declarations. Expression rules handle arithmetic, logical, and relational operations, with proper precedence and associativity. Statement rules cover control flow constructs like if-else and while loops, as well as variable declarations and assignments. Declaration rules define function signatures and variable types.

```mermaid
erDiagram
RULE ||--o{ EXPRESSION : "defines"
RULE ||--o{ STATEMENT : "defines"
RULE ||--o{ DECLARATION : "defines"
EXPRESSION {
string rule_name
string precedence
string associativity
}
STATEMENT {
string rule_name
string control_flow_type
boolean has_block
}
DECLARATION {
string rule_name
string declaration_type
boolean has_parameters
}
EXPRESSION ||--|{ BINARY_EXPR : "includes"
EXPRESSION ||--|{ UNARY_EXPR : "includes"
EXPRESSION ||--|{ PRIMARY_EXPR : "includes"
STATEMENT ||--|{ IF_STATEMENT : "includes"
STATEMENT ||--|{ WHILE_STATEMENT : "includes"
STATEMENT ||--|{ ASSIGNMENT : "includes"
DECLARATION ||--|{ FUNCTION_DECL : "includes"
DECLARATION ||--|{ VAR_DECL : "includes"
```

**Diagram sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Error Recovery Mechanisms
CymbolParser.java implements robust error recovery mechanisms to handle malformed syntax. When a syntax error is encountered, the parser reports the error and attempts to recover by skipping tokens until a synchronization point is reached. This allows the parser to continue processing the rest of the input, potentially identifying multiple syntax errors in a single pass.

```mermaid
stateDiagram-v2
[*] --> NormalParsing
NormalParsing --> SyntaxError : "Invalid token"
SyntaxError --> ErrorRecovery : "Report error"
ErrorRecovery --> SkipTokens : "Skip until sync point"
SkipTokens --> ResyncPoint : "Find resynchronization point"
ResyncPoint --> NormalParsing : "Resume parsing"
NormalParsing --> SuccessfulParse : "EOF reached"
SuccessfulParse --> [*]
SyntaxError --> FailedParse : "Unrecoverable error"
FailedParse --> [*]
```

**Diagram sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Parse Tree Construction
The parser constructs a ParseTree by creating context objects for each grammar rule that is successfully matched. Each context object contains references to its child contexts, forming a hierarchical tree structure that represents the syntactic structure of the input program. This ParseTree serves as the foundation for subsequent compilation phases, such as semantic analysis and code generation.

```mermaid
graph TD
A[FileContext] --> B[FunctionDeclContext]
A --> C[VarDeclContext]
A --> D[StatetmentContext]
B --> E[TypeContext]
B --> F[ID TerminalNode]
B --> G[FormalParametersContext]
B --> H[BlockContext]
C --> I[TypeContext]
C --> J[ID TerminalNode]
C --> K[ExprContext]
D --> L[StatBlockContext]
D --> M[StatContext]
D --> N[StatVarDeclContext]
H --> O[StatetmentContext]
K --> P[ExprBinaryContext]
P --> Q[ExprContext]
P --> R[Token o]
P --> S[ExprContext]
```

**Diagram sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Extending the Grammar
The Cymbol grammar can be extended to support new language constructs by adding new rules to the grammar definition. This involves defining new parser rules, updating existing rules to reference the new constructs, and ensuring that the grammar remains unambiguous. The ANTLR4 toolchain automatically generates updated parser code when the grammar is modified.

```mermaid
flowchart TD
A[Identify New Construct] --> B[Define Grammar Rule]
B --> C[Update Existing Rules]
C --> D[Check for Ambiguities]
D --> E{Ambiguities Found?}
E --> |Yes| F[Refactor Grammar]
F --> C
E --> |No| G[Generate Parser Code]
G --> H[Test New Grammar]
H --> I{Tests Pass?}
I --> |No| J[Debug and Fix]
J --> H
I --> |Yes| K[Grammar Extended Successfully]
```

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Troubleshooting Parsing Conflicts
Common parsing conflicts in the Cymbol grammar include ambiguities in expression parsing and issues with nested control structures. These can be resolved by carefully designing grammar rules with proper precedence and associativity, using syntactic predicates when necessary, and testing the parser with a comprehensive suite of valid and invalid inputs.

```mermaid
graph TD
A[Parsing Conflict] --> B[Identify Conflict Type]
B --> C{Ambiguity?}
C --> |Yes| D[Add Precedence Rules]
C --> |No| E{Left Recursion?}
E --> |Yes| F[Refactor to Right Recursion]
E --> |No| G{Infinite Loop?}
G --> |Yes| H[Add Base Cases]
G --> |No| I[Other Issue]
D --> J[Test Resolution]
F --> J
H --> J
I --> J
J --> K{Conflict Resolved?}
K --> |No| B
K --> |Yes| L[Conflict Resolved]
```

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)

## Conclusion
The syntactic analysis phase in the Cymbol compiler effectively transforms token streams into structured ParseTree representations using ANTLR4's LL(*) parsing strategy. The implementation in CymbolParser.java demonstrates how context-free grammar rules can be used to parse complex language constructs, with robust error recovery mechanisms to handle malformed syntax. The interaction between the parser and lexer ensures a smooth transition from lexical analysis to syntactic analysis, providing a solid foundation for subsequent compilation phases.

**Section sources**
- [CymbolParser.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolParser.java#L0-L1567)
- [CymbolLexer.java](file://ep16/src/main/java/org/teachfx/antlr4/ep16/parser/CymbolLexer.java#L0-L264)