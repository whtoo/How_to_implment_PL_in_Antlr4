# Symbol Resolution API

<cite>
**Referenced Files in This Document**   
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java)
- [GlobalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/GlobalScope.java)
- [LocalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/LocalScope.java)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java)
- [VariableSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/VariableSymbol.java)
- [MethodSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/MethodSymbol.java)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java)
- [TypeTable.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeTable.java)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Symbol Table Architecture](#symbol-table-architecture)
3. [Scope Management System](#scope-management-system)
4. [Symbol Creation and Definition](#symbol-creation-and-definition)
5. [Symbol Resolution Process](#symbol-resolution-process)
6. [Type System Integration](#type-system-integration)
7. [API Usage Examples](#api-usage-examples)
8. [Error Handling and Conflict Detection](#error-handling-and-conflict-detection)
9. [Conclusion](#conclusion)

## Introduction
The Symbol Resolution API provides a comprehensive system for managing symbols and their scopes within the Cymbol programming language compiler. This documentation focuses on the LocalDefine class, which plays a central role in building and maintaining the symbol table during compilation. The system supports multiple scope types including global, local, and method scopes, and handles various symbol types such as variables, functions, and structures. The API enables programmatic access to symbol information, type resolution, and conflict detection, forming the foundation for subsequent compilation phases like type checking and code generation.

## Symbol Table Architecture

```mermaid
classDiagram
class Scope {
<<interface>>
+String getScopeName()
+Scope getEnclosingScope()
+Symbol resolve(String name)
+void define(Symbol sym)
+Type lookup(String name)
}
class BaseScope {
-Scope enclosingScope
-Map<String, Symbol> symbols
+BaseScope(Scope parent)
+Type lookup(String name)
+Symbol resolve(String name)
+void define(Symbol sym)
+Scope getEnclosingScope()
}
class GlobalScope {
+GlobalScope()
+String getScopeName()
}
class LocalScope {
+LocalScope(Scope parent)
+String getScopeName()
}
class Symbol {
-String name
-Type type
-Scope scope
+Symbol(String name)
+Symbol(String name, Type type)
+String getName()
}
class VariableSymbol {
+VariableSymbol(String name)
+VariableSymbol(String name, Type type)
}
class MethodSymbol {
-ParserRuleContext blockStmt
-boolean builtin
-ParserRuleContext callee
-Map<String, Symbol> orderedArgs
+MethodSymbol(String name, Type retType, Scope parent, ParserRuleContext tree)
+MethodSymbol(String name, Scope parent, ParserRuleContext tree)
+Map<String, Symbol> getMembers()
+boolean isPrimitive()
}
class StructSymbol {
-Map<String, Symbol> fields
-Map<String, MethodSymbol> methods
+StructSymbol(String name, Scope parent, ParserRuleContext tree)
+Symbol resolveMember(String name)
+void addField(Symbol fieldSymbol)
+void addMethod(MethodSymbol methodSymbol)
+Map<String, Symbol> getFields()
+Map<String, MethodSymbol> getMethods()
+Map<String, Symbol> getMembers()
+boolean isPrimitive()
}
class TypeTable {
+static Type INT
+static Type FLOAT
+static Type BOOLEAN
+static Type CHAR
+static Type VOID
+static Type STRING
+static Type OBJECT
+static Type getTypeByName(String name)
}
Scope <|-- BaseScope
BaseScope <|-- GlobalScope
BaseScope <|-- LocalScope
Symbol <|-- VariableSymbol
Symbol <|-- MethodSymbol
Symbol <|-- StructSymbol
MethodSymbol <|-- StructSymbol
BaseScope ..> Symbol : contains
GlobalScope --> TypeTable : references
```

**Diagram sources**
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)
- [GlobalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/GlobalScope.java#L1-L15)
- [LocalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/LocalScope.java#L1-L13)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)
- [VariableSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/VariableSymbol.java#L1-L15)
- [MethodSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/MethodSymbol.java#L1-L39)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java#L1-L101)
- [TypeTable.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeTable.java)

**Section sources**
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)
- [GlobalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/GlobalScope.java#L1-L15)
- [LocalScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/LocalScope.java#L1-L13)

## Scope Management System

The scope management system implements a hierarchical structure that mirrors the lexical scoping rules of the Cymbol language. The system is built around three primary scope types: global, local, and method scopes, all of which inherit from the BaseScope class. The GlobalScope serves as the root of the scope hierarchy and is automatically populated with built-in types and functions during initialization. LocalScope instances are created for block-level scoping within functions and control structures, enabling proper handling of variable shadowing and local declarations.

The scope resolution process follows a chain-of-responsibility pattern, where symbol lookups first check the current scope and then propagate upward through the enclosing scope hierarchy until the symbol is found or the global scope is reached. This design ensures that inner scopes can access symbols from outer scopes while maintaining isolation between unrelated scopes. The system maintains a stack of active scopes during compilation, allowing the LocalDefine visitor to manage scope entry and exit operations as it traverses the abstract syntax tree.

```mermaid
sequenceDiagram
participant Compiler as Compiler
participant LocalDefine as LocalDefine
participant CurrentScope as Current Scope
participant EnclosingScope as Enclosing Scope
participant GlobalScope as Global Scope
Compiler->>LocalDefine : visitFunctionDecl()
LocalDefine->>LocalDefine : pushScope(methodScope)
LocalDefine->>CurrentScope : stashScope(ctx)
LocalDefine->>LocalDefine : visitBlock()
LocalDefine->>LocalDefine : pushScope(localScope)
LocalDefine->>LocalDefine : visitVarDecl()
LocalDefine->>CurrentScope : define(variableSymbol)
LocalDefine->>LocalDefine : popScope()
LocalDefine->>LocalDefine : popScope()
LocalDefine-->>Compiler : return
LocalDefine->>CurrentScope : resolve(symbolName)
alt Symbol found in current scope
CurrentScope-->>LocalDefine : return symbol
else Symbol not found
CurrentScope->>EnclosingScope : resolve(symbolName)
alt Symbol found in enclosing scope
EnclosingScope-->>CurrentScope : return symbol
CurrentScope-->>LocalDefine : return symbol
else Symbol not found
EnclosingScope->>GlobalScope : resolve(symbolName)
alt Symbol found in global scope
GlobalScope-->>EnclosingScope : return symbol
EnclosingScope-->>CurrentScope : return symbol
CurrentScope-->>LocalDefine : return symbol
else Symbol not found
GlobalScope-->>EnclosingScope : return null
EnclosingScope-->>CurrentScope : return null
CurrentScope-->>LocalDefine : return null
end
end
end
```

**Diagram sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)

**Section sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)

## Symbol Creation and Definition

The symbol creation and definition process is orchestrated by the LocalDefine class, which acts as an AST visitor to traverse the syntax tree and establish symbols in appropriate scopes. During initialization, the LocalDefine constructor creates a GlobalScope instance and populates it with built-in types (int, float, bool, char, void, String, Object) and built-in functions (print, main). This ensures that fundamental language elements are available throughout the compilation process.

Symbol definition occurs through the define() method inherited from the BaseScope class, which adds symbols to the current scope's symbol map and establishes the bidirectional relationship between symbols and their scopes. The system handles different symbol types through specialized visitor methods: visitVarDecl() for variable declarations, visitFunctionDecl() for function definitions, visitStructDecl() for structure declarations, and visitTypedefDecl() for type aliases. Each visitor method creates the appropriate symbol type, associates it with the current scope, and manages scope transitions when necessary.

For complex declarations like structure members, the system handles both fields and methods within the visitStructMemeber() method. Field declarations create VariableSymbol instances and add them to the structure's field map, while method declarations create MethodSymbol instances and establish a new scope for the method body. The system maintains proper scope nesting by pushing and popping scopes during traversal, ensuring that symbols are defined in the correct lexical context.

```mermaid
flowchart TD
Start([Start Symbol Definition]) --> Initialize["Initialize GlobalScope"]
Initialize --> Register["Register Built-in Types and Functions"]
Register --> Traverse["Traverse AST with LocalDefine Visitor"]
Traverse --> CheckNode{"Node Type?"}
CheckNode --> |VarDecl| HandleVar["Create VariableSymbol<br>Define in current scope"]
CheckNode --> |FunctionDecl| HandleFunc["Create MethodSymbol<br>Push function scope<br>Define in current scope"]
CheckNode --> |StructDecl| HandleStruct["Create StructSymbol<br>Push struct scope<br>Define in current scope"]
CheckNode --> |Block| HandleBlock["Create LocalScope<br>Push block scope"]
CheckNode --> |StructMember| HandleMember{"Member Type?"}
HandleMember --> |Field| HandleField["Create VariableSymbol<br>Add to struct fields"]
HandleMember --> |Method| HandleMethod["Create MethodSymbol<br>Push method scope<br>Add to struct methods"]
HandleVar --> Continue
HandleFunc --> Continue
HandleStruct --> Continue
HandleBlock --> Continue
HandleField --> Continue
HandleMethod --> Continue
Continue --> CheckChildren{"Has Children?"}
CheckChildren --> |Yes| ProcessChildren["Process Child Nodes"]
CheckChildren --> |No| Complete
ProcessChildren --> CheckNode
Complete --> End([Complete Symbol Definition])
style Start fill:#4CAF50,stroke:#388E3C
style End fill:#4CAF50,stroke:#388E3C
style Initialize fill:#2196F3,stroke:#1976D2
style Register fill:#2196F3,stroke:#1976D2
style Traverse fill:#2196F3,stroke:#1976D2
style CheckNode fill:#FFC107,stroke:#FFA000
style HandleVar fill:#FF9800,stroke:#F57C00
style HandleFunc fill:#FF9800,stroke:#F57C00
style HandleStruct fill:#FF9800,stroke:#F57C00
style HandleBlock fill:#FF9800,stroke:#F57C00
style HandleMember fill:#FFC107,stroke:#FFA000
style HandleField fill:#FF9800,stroke:#F57C00
style HandleMethod fill:#FF9800,stroke:#F57C00
style Continue fill:#2196F3,stroke:#1976D2
style CheckChildren fill:#FFC107,stroke:#FFA000
style ProcessChildren fill:#2196F3,stroke:#1976D2
style Complete fill:#2196F3,stroke:#1976D2
```

**Diagram sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)
- [VariableSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/VariableSymbol.java#L1-L15)
- [MethodSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/MethodSymbol.java#L1-L39)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java#L1-L101)

**Section sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)

## Symbol Resolution Process

The symbol resolution process enables the compiler to locate and retrieve symbol information based on identifier names within the appropriate lexical context. The resolution mechanism is implemented through the resolve() method in the BaseScope class, which performs a hierarchical search starting from the current scope and progressing through enclosing scopes until the symbol is found or all scopes have been exhausted. This approach ensures that the nearest declaration of a symbol is used, implementing the standard lexical scoping rules.

The LocalDefine class plays a crucial role in the resolution process by maintaining a ParseTreeProperty that associates AST nodes with their corresponding scopes. The stashScope() method records the current scope for each relevant AST node, creating a mapping that subsequent compilation phases can use to determine the context in which identifiers should be resolved. This information is essential for accurate symbol resolution during type checking and code generation.

For structure members, the system provides specialized resolution through the resolveMember() method in the StructSymbol class. This method searches only within the structure's own members (both fields and methods) without traversing the scope hierarchy, ensuring that structure member access is resolved correctly. The system also handles method calls and function invocations by resolving the called function's symbol in the current scope and verifying that the appropriate number and types of arguments are provided.

```mermaid
sequenceDiagram
participant LocalDefine as LocalDefine
participant Scope as Scope
participant Symbol as Symbol
participant ASTNode as AST Node
LocalDefine->>LocalDefine : visitPrimaryID(ctx)
LocalDefine->>LocalDefine : stashScope(ctx)
LocalDefine->>Scope : resolve(identifier)
alt Symbol found
Scope-->>LocalDefine : return Symbol
LocalDefine->>ASTNode : associate symbol with node
else Symbol not found
Scope->>Scope : check enclosing scope
alt Found in parent
Scope-->>LocalDefine : return Symbol
LocalDefine->>ASTNode : associate symbol with node
else Not found in hierarchy
Scope-->>LocalDefine : return null
LocalDefine->>Compiler : report undefined symbol
end
end
LocalDefine->>LocalDefine : visitExprStructFieldAccess(ctx)
LocalDefine->>StructSymbol : resolveMember(fieldName)
alt Member found
StructSymbol-->>LocalDefine : return Symbol
LocalDefine->>ASTNode : associate member symbol
else Member not found
StructSymbol-->>LocalDefine : return null
LocalDefine->>Compiler : report undefined member
end
LocalDefine->>LocalDefine : visitExprFuncCall(ctx)
LocalDefine->>Scope : resolve(functionName)
alt Function found
Scope-->>LocalDefine : return MethodSymbol
LocalDefine->>ASTNode : associate function symbol
LocalDefine->>LocalDefine : verify arguments
else Function not found
Scope-->>LocalDefine : return null
LocalDefine->>Compiler : report undefined function
end
```

**Diagram sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java#L1-L101)

**Section sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)

## Type System Integration

The symbol resolution system is tightly integrated with the type system through the TypeTable class, which maintains references to all built-in types and provides type resolution services. During symbol creation, the system associates type information with symbols either directly through the Symbol constructor or via the type field in derived symbol classes. The TypeTable contains static references to fundamental types (INT, FLOAT, BOOLEAN, CHAR, VOID, STRING, OBJECT) that are automatically available in the global scope.

Type information is stored and retrieved through multiple mechanisms: symbols maintain a direct reference to their type through the type field, while scopes provide type lookup services through the lookup() method. The system supports both explicit type declarations and implicit type resolution, allowing for flexible type handling in the language. For structure types, the StructSymbol class implements the Type interface, enabling structures to be used as first-class types in variable declarations and function signatures.

The integration between symbols and types enables comprehensive type checking during subsequent compilation phases. Method symbols maintain information about their return types and parameter types, allowing the type checker to verify function calls and assignments. Variable symbols store their declared types, enabling type safety checks for operations and assignments. The system also supports type aliases through the TypedefSymbol class, which creates new names for existing types while maintaining the underlying type information.

```mermaid
classDiagram
class TypeTable {
+static Type INT
+static Type FLOAT
+static Type BOOLEAN
+static Type CHAR
+static Type VOID
+static Type STRING
+static Type OBJECT
+static Type getTypeByName(String name)
}
class Type {
<<interface>>
+boolean isPrimitive()
}
class Symbol {
-String name
-Type type
-Scope scope
+Symbol(String name)
+Symbol(String name, Type type)
+String getName()
}
class VariableSymbol {
+VariableSymbol(String name)
+VariableSymbol(String name, Type type)
}
class MethodSymbol {
-Type returnType
-Map<String, Symbol> parameters
+MethodSymbol(String name, Type retType, Scope parent, ParserRuleContext tree)
+boolean isPrimitive()
}
class StructSymbol {
-Map<String, Symbol> fields
-Map<String, MethodSymbol> methods
+StructSymbol(String name, Scope parent, ParserRuleContext tree)
+boolean isPrimitive()
}
class TypedefSymbol {
-Type targetType
+TypedefSymbol(String name, Type targetType)
}
Type <|-- MethodSymbol
Type <|-- StructSymbol
TypeTable ..> Type : contains
Symbol --> Type : has type
VariableSymbol --> Type : has type
MethodSymbol --> Type : has return type
StructSymbol --> Type : is type
TypedefSymbol --> Type : has target type
```

**Diagram sources**
- [TypeTable.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeTable.java)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)
- [VariableSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/VariableSymbol.java#L1-L15)
- [MethodSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/MethodSymbol.java#L1-L39)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java#L1-L101)

**Section sources**
- [TypeTable.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/TypeTable.java)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)

## API Usage Examples

The Symbol Resolution API provides several key methods for programmatic access to symbol information and scope management. The primary entry point is the LocalDefine class, which processes the AST and builds the symbol table. After processing, the getScopes() method returns a ParseTreeProperty that maps AST nodes to their corresponding scopes, enabling other compilation phases to determine the context of any node.

To create and define symbols, clients can use the define() method on any Scope instance, passing a Symbol object of the appropriate type. For example, defining a variable symbol involves creating a VariableSymbol instance with the desired name and type, then calling define() on the current scope. The system automatically handles the bidirectional relationship between the symbol and its scope.

Symbol lookup is performed using the resolve() method on Scope instances, which searches for a symbol by name in the current scope and its ancestors. For structure member resolution, the resolveMember() method on StructSymbol instances should be used instead, as it searches only within the structure's members without traversing the scope hierarchy.

```mermaid
flowchart TD
A[Initialize Compiler] --> B[Create LocalDefine Instance]
B --> C[Process AST with LocalDefine]
C --> D[Retrieve Scope Information]
D --> E{Operation Type?}
E --> |Define Symbol| F[Create Symbol Instance]
F --> G[Call scope.define(symbol)]
G --> H[Symbol Added to Scope]
E --> |Resolve Symbol| I[Call scope.resolve(name)]
I --> J{Symbol Found?}
J --> |Yes| K[Return Symbol Reference]
J --> |No| L[Return Null]
E --> |Access Scope Info| M[Use getScopes() Property]
M --> N[Retrieve Scope for AST Node]
H --> O[Continue Compilation]
K --> O
L --> O
N --> O
O --> P[Proceed to Next Phase]
style A fill:#4CAF50,stroke:#388E3C
style P fill:#4CAF50,stroke:#388E3C
style B fill:#2196F3,stroke:#1976D2
style C fill:#2196F3,stroke:#1976D2
style D fill:#2196F3,stroke:#1976D2
style E fill:#FFC107,stroke:#FFA000
style F fill:#FF9800,stroke:#F57C00
style G fill:#FF9800,stroke:#F57C00
style H fill:#FF9800,stroke:#F57C00
style I fill:#FF9800,stroke:#F57C00
style J fill:#FFC107,stroke:#FFA000
style K fill:#FF9800,stroke:#F57C00
style L fill:#FF9800,stroke:#F57C00
style M fill:#FF9800,stroke:#F57C00
style N fill:#FF9800,stroke:#F57C00
style O fill:#2196F3,stroke:#1976D2
```

**Diagram sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)
- [Symbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/Symbol.java#L1-L40)

**Section sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)

## Error Handling and Conflict Detection

The symbol resolution system includes comprehensive error handling and conflict detection mechanisms to identify and report semantic errors in the source code. The primary error conditions detected include redeclarations of symbols within the same scope and references to undefined symbols. When a symbol is defined, the system checks whether a symbol with the same name already exists in the current scope, preventing duplicate declarations.

For undefined symbol references, the system returns null when resolve() is called with a name that cannot be found in the current scope or any of its enclosing scopes. Subsequent compilation phases can then generate appropriate error messages indicating that an identifier is undefined. The system also detects conflicts in structure member declarations, ensuring that field and method names within a structure are unique.

The LocalDefine class includes logging capabilities through the SLF4J framework, which outputs debug information about symbol definition and scope transitions. This logging can be invaluable for debugging compilation issues and understanding the symbol resolution process. The system follows a fail-fast approach, reporting errors as soon as they are detected rather than attempting to continue with potentially invalid symbol information.

```mermaid
flowchart TD
A[Start Symbol Operation] --> B{Operation Type?}
B --> |Define Symbol| C[Check for existing symbol]
C --> D{Symbol already exists?}
D --> |Yes| E[Report redeclaration error]
D --> |No| F[Add symbol to scope]
F --> G[Set symbol.scope reference]
B --> |Resolve Symbol| H[Search current scope]
H --> I{Found in current scope?}
I --> |Yes| J[Return symbol]
I --> |No| K[Check enclosing scope]
K --> L{Reached global scope?}
L --> |No| H
L --> |Yes| M{Found in global scope?}
M --> |Yes| J
M --> |No| N[Report undefined symbol error]
B --> |Define Struct Member| O[Check member name uniqueness]
O --> P{Name already used?}
P --> |Yes| Q[Report member conflict error]
P --> |No| R[Add member to structure]
E --> Z[Error Handling]
N --> Z
Q --> Z
J --> Z
R --> Z
Z --> X[Continue Compilation]
style A fill:#4CAF50,stroke:#388E3C
style X fill:#4CAF50,stroke:#388E3C
style B fill:#FFC107,stroke:#FFA000
style C fill:#FF9800,stroke:#F57C00
style D fill:#FFC107,stroke:#FFA000
style E fill:#F44336,stroke:#D32F2F
style F fill:#FF9800,stroke:#F57C00
style G fill:#FF9800,stroke:#F57C00
style H fill:#FF9800,stroke:#F57C00
style I fill:#FFC107,stroke:#FFA000
style J fill:#4CAF50,stroke:#388E3C
style K fill:#FF9800,stroke:#F57C00
style L fill:#FFC107,stroke:#FFA000
style M fill:#FFC107,stroke:#FFA000
style N fill:#F44336,stroke:#D32F2F
style O fill:#FF9800,stroke:#F57C00
style P fill:#FFC107,stroke:#FFA000
style Q fill:#F44336,stroke:#D32F2F
style R fill:#FF9800,stroke:#F57C00
style Z fill:#2196F3,stroke:#1976D2
```

**Diagram sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)
- [StructSymbol.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/symbol/StructSymbol.java#L1-L101)

**Section sources**
- [LocalDefine.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/pass/LocalDefine.java#L1-L324)
- [BaseScope.java](file://ep19/src/main/java/org/teachfx/antlr4/ep19/symtab/scope/BaseScope.java#L1-L67)

## Conclusion
The Symbol Resolution API provides a robust foundation for managing identifiers and their associated information throughout the compilation process. The LocalDefine class serves as the central component for building the symbol table, establishing scopes, and defining symbols in their appropriate lexical contexts. The hierarchical scope system supports the full range of Cymbol's scoping rules, from global declarations to nested block scopes, while ensuring proper symbol resolution and conflict detection.

The integration between symbols and types enables comprehensive type checking and verification in subsequent compilation phases. By maintaining bidirectional relationships between symbols and their scopes, and between symbols and their types, the system provides efficient access to semantic information needed for code generation and optimization. The API's design follows established compiler construction patterns, making it both effective for the current implementation and extensible for future language features.

The comprehensive error handling and conflict detection mechanisms ensure that semantic errors are identified early in the compilation process, providing clear feedback to developers. The logging capabilities and well-defined interfaces make the system maintainable and debuggable, supporting ongoing development and enhancement of the Cymbol compiler.