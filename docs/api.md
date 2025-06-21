# Cymbol 编译器 (EP19) API 文档

## 1. 引言

本文档为Cymbol编译器EP19版本的主要公共类和接口提供API级别的说明。它旨在帮助开发者理解编译器的编程接口，以便使用、扩展或维护编译器。

## 2. 主要公共类与接口

### `org.teachfx.antlr4.ep19.Compiler`

*   **描述**: Cymbol编译器的主入口和控制类。提供了用于编译和执行Cymbol代码的静态方法。
*   **主要公共静态方法**:
    *   `public static void main(String[] args)`
        *   描述: 命令行工具的入口点。解析参数并调用相应的编译/执行逻辑。
        *   参数: `args` - 命令行参数数组。
    *   `public static void compileFile(String sourcePath) throws IOException`
        *   描述: 编译并立即解释执行指定路径的Cymbol源文件。
        *   参数: `sourcePath` - Cymbol源文件的路径。
        *   抛出: `IOException` - 如果文件读取失败。
    *   `public static void compileString(String sourceCode)`
        *   描述: 编译并立即解释执行给定的Cymbol源代码字符串。
        *   参数: `sourceCode` - 包含Cymbol代码的字符串。
    *   `public static ParseTree compileWithoutExecution(String sourcePath) throws IOException`
        *   描述: 编译源代码但不执行解释器。用于静态分析或获取AST。
        *   参数: `sourcePath` - Cymbol源文件的路径。
        *   返回: `ParseTree` - 经过语义分析（符号定义、解析、类型检查）的解析树。
        *   抛出: `IOException` - 如果文件读取失败。
    *   `public static void compileAndSave(String sourcePath, String outputPath) throws IOException`
        *   描述: 编译指定的Cymbol源文件，并将编译结果 (`CompilationResult`) 序列化保存到指定的输出文件。
        *   参数:
            *   `sourcePath` - Cymbol源文件的路径。
            *   `outputPath` - 保存编译结果的文件路径。
        *   抛出: `IOException` - 如果文件读写失败。
    *   `public static void executeCompiledFile(String compiledPath) throws IOException`
        *   描述: 从指定文件加载先前编译的 `CompilationResult` 对象，并执行其中的代码。
        *   参数: `compiledPath` - 已编译对象文件的路径。
        *   抛出: `IOException` - 如果文件读取或执行时发生错误。
    *   `public static void staticAnalysis(String sourcePath) throws IOException`
        *   描述: 对指定的Cymbol源文件进行静态分析（词法、语法、符号和类型检查），不执行代码。
        *   参数: `sourcePath` - Cymbol源文件的路径。
        *   抛出: `IOException` - 如果文件读取失败。

### `org.teachfx.antlr4.ep19.pipeline.CompilerPipeline` (Interface)

*   **描述**: 定义了Cymbol编译器标准编译流程中各个阶段的接口。
*   **主要方法**:
    *   `CommonTokenStream lexicalAnalysis(CharStream charStream)`
        *   描述: 对输入的字符流执行词法分析。
        *   参数: `charStream` - 源代码的字符流。
        *   返回: `CommonTokenStream` - 词法单元流。
    *   `ParseTree syntaxAnalysis(CommonTokenStream tokenStream)`
        *   描述: 对词法单元流执行语法分析。
        *   参数: `tokenStream` - 词法单元流。
        *   返回: `ParseTree` - 生成的解析树。
    *   `LocalDefine symbolDefinition(ParseTree parseTree)`
        *   描述: 执行符号定义遍，收集符号和作用域信息。
        *   参数: `parseTree` - 语法分析后得到的解析树。
        *   返回: `LocalDefine` - 完成符号定义遍的访问者对象，包含作用域信息。
    *   `LocalResolver symbolResolution(ParseTree parseTree, ScopeUtil scopeUtil)`
        *   描述: 执行符号解析遍，解析引用并赋予类型。
        *   参数:
            *   `parseTree` - 解析树。
            *   `scopeUtil` - 作用域工具类实例。
        *   返回: `LocalResolver` - 完成符号解析遍的访问者对象，包含类型注解。
    *   `TypeCheckVisitor typeChecking(ParseTree parseTree, ScopeUtil scopeUtil, LocalResolver localResolver)`
        *   描述: 执行类型检查遍。
        *   参数:
            *   `parseTree` - 解析树。
            *   `scopeUtil` - 作用域工具类实例。
            *   `localResolver` - 已完成的符号解析器，用于获取类型信息。
        *   返回: `TypeCheckVisitor` - 完成类型检查的访问者对象。
    *   `Object interpretation(ParseTree parseTree, ScopeUtil scopeUtil)`
        *   描述: 解释执行解析树。
        *   参数:
            *   `parseTree` - 解析树。
            *   `scopeUtil` - 作用域工具类实例。
        *   返回: `Object` - 解释执行的结果（通常为null，输出通过副作用如`print`完成）。
    *   `Object compile(CharStream charStream)`
        *   描述: 运行完整的编译流程，从源代码到解释执行。
        *   参数: `charStream` - 源代码的字符流。
        *   返回: `Object` - 解释执行的结果。
    *   `ParseTree compileWithoutInterpretation(CharStream charStream)`
        *   描述: 运行编译流程直到类型检查完成，不进行解释执行。
        *   参数: `charStream` - 源代码的字符流。
        *   返回: `ParseTree` - 经过语义分析的解析树。
    *   `CompilationResult compileToResult(CharStream charStream)`
        *   描述: 编译源代码并返回一个 `CompilationResult` 对象。
        *   参数: `charStream` - 源代码的字符流。
        *   返回: `CompilationResult` - 包含编译产物的对象。
    *   `Object execute(CompilationResult result)`
        *   描述: 执行先前编译生成的 `CompilationResult`。
        *   参数: `result` - 已编译的 `CompilationResult` 对象。
        *   返回: `Object` - 解释执行的结果。

### `org.teachfx.antlr4.ep19.pipeline.DefaultCompilerPipeline` (Class)

*   **描述**: `CompilerPipeline` 接口的默认实现。它按顺序调用词法分析器、语法分析器、各个语义分析遍（`LocalDefine`, `LocalResolver`, `TypeCheckVisitor`）以及解释器（`Interpreter`）。

### `org.teachfx.antlr4.ep19.pipeline.CompilationResult` (Class)

*   **描述**: 一个可序列化的数据传输对象 (DTO)，用于封装一次编译成功后的产物。这允许编译过程和执行过程分离。
*   **主要公共方法/可访问内容**:
    *   `public boolean isSuccessful()`: 检查编译是否成功（无语法或类型错误）。
    *   `public String getErrorMessage()`: 如果编译失败，获取错误信息。
    *   `public ParseTree getParseTree()`: 获取编译生成的解析树。
    *   `public ScopeUtil getScopeUtil()`: 获取作用域工具类实例，包含了符号表信息。
    *   (还可能包含对 `LocalDefine`, `LocalResolver`, `TypeCheckVisitor` 实例的访问器，以便后续阶段如代码生成或进一步分析使用)。

### `org.teachfx.antlr4.ep19.symtab.Symbol` (Class)

*   **描述**: 符号表中所有符号（变量、函数、类型等）的基类。
*   **主要公共字段/方法**:
    *   `public String name`: 符号的名称。
    *   `public Type type`: 符号的类型。
    *   `public Scope scope`: 符号所在的作用域。
    *   `public String getName()`: 获取符号名称。
    *   `public String toString()`: 返回符号的字符串表示，通常包含名称、类型和作用域。

### `org.teachfx.antlr4.ep19.symtab.scope.Scope` (Interface)

*   **描述**: 定义了一个作用域（或称符号表）的行为，用于管理和查找符号。
*   **主要方法**:
    *   `String getScopeName()`: 获取作用域的名称。
    *   `Scope getEnclosingScope()`: 获取此作用域所嵌套的外部作用域（父作用域）。
    *   `void define(Symbol sym)`: 在此作用域中定义一个符号。
    *   `Symbol resolve(String name)`: 在此作用域或其父作用域中解析（查找）具有给定名称的符号。

### `org.teachfx.antlr4.ep19.symtab.Type` (Interface)

*   **描述**: 代表Cymbol语言中数据类型的接口。
*   **主要方法**:
    *   `String getName()`: 获取类型的名称（例如："int", "float", "MyStruct"）。
    *   `boolean isPrimitive()`: 判断该类型是否为基本类型。

## 3. 关键的语义分析遍 (Passes)

这些类是 `CymbolBaseVisitor` 的子类，用于遍历AST并执行特定的语义分析任务。它们通常不是直接由外部用户实例化，而是由 `CompilerPipeline` 在内部使用。

*   **`org.teachfx.antlr4.ep19.pass.LocalDefine`**:
    *   职责：构建符号表，定义作用域和符号。
*   **`org.teachfx.antlr4.ep19.pass.LocalResolver`**:
    *   职责：解析符号引用，初步确定表达式类型。
*   **`org.teachfx.antlr4.ep19.pass.TypeCheckVisitor`**:
    *   职责：进行详细的类型检查。
*   **`org.teachfx.antlr4.ep19.pass.Interpreter`**:
    *   职责：解释执行AST。

## 4. ANTLR 生成的类

编译器大量使用由ANTLR v4工具根据 `Cymbol.g4` 语法文件生成的类。这些类包括：

*   `CymbolLexer`: 词法分析器。
*   `CymbolParser`: 语法分析器。
*   `CymbolVisitor` / `CymbolBaseVisitor`: Visitor模式的接口和基类，用于遍历解析树。
*   `CymbolListener` / `CymbolBaseListener`: Listener模式的接口和基类。

对于这些类的详细API，请参考ANTLR v4的官方文档。

---
*本文档提供了Cymbol EP19编译器关键公共API的概览。*
