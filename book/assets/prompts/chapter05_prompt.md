# Chapter 5 Prompt: Arrays and More Complex Features

## Chapter Writing Task

Please write a complete chapter for this book.

### Chapter Basic Information

- **Chapter Title**: 第5章：数组与更复杂的特性
- **Module**: 模块 1：基础语言与解释器（EP1–EP12）
- **Target Reader**: Engineers with symbol table experience from Chapter 4
- **Prerequisites**: Statement parsing, expression evaluation
- **Repository EP Range**: EP11–EP12 (function calls, array operations, transition to complete language)
- **Position in Book**:
  - Previous Chapter: 第4章：符号表与作用域
  - Next Chapter: 第6章：AST 构建与表达式求值（模块 2 起点）
- **After completing this chapter, readers should be able to**:
  - Understand array syntax and semantics
  - Master function call implementation
  - Support more complex language features
  - Build a complete interpreter from expression parsing to complex data structures

---

## Content Structure Requirements

### 1. Chapter Overview

### 2. Motivation and Real-World Scenarios

Use a scenario about needing arrays and functions in real programs. Explain pain points without these capabilities.

### 3. Human Engineer Line: Technology and Implementation

#### 3.1 Core Concepts

**Key Concepts to Cover**:
1. **Arrays** - Contiguous memory for multiple values
2. **Array Access** - Indexing, bounds checking
3. **Function Calls** - Parameters, return values, call stack
4. **Complete Language Features** - Bringing it all together

Include 1–3 diagram placeholders showing:
- Array memory layout
- Function call stack
- Complete interpreter architecture

#### 3.2 Correspondence with Repository EPs

- Corresponding directory: EP11–EP12
- Key files: Grammar with array syntax, function implementation
- Key classes: ArrayNode, FunctionNode, CallNode

#### 3.3 Practical Workflow

Steps to:
- Parse array declarations and accesses
- Parse function definitions and calls
- Execute arrays and functions in interpreter
- Test complete programs

### 4. AI Collaboration Line

#### 4.1 Context Design

Provide context files:
- Grammar with array and function rules
- Array and function implementation code
- Test programs using arrays
- Function call examples

#### 4.2 Prompt Templates

Type A: Function implementation Prompt
- Template for implementing array bounds checking
Type B: Test generation Prompt
- Template for generating array operation test cases

#### 4.3 AI Should/Should Not Do

**Allowed**:
- Implement array access logic
- Generate function call tests
- Optimize array operations
- Generate memory layout visualizations

**Forbidden**:
- Change array syntax semantics
- Delete array tests
- Modify function parameter passing
- Break array indexing

#### 4.4 Verification and Rollback

- Test commands for array and function programs
- Manual verification of array bounds
- Git rollback strategies

### 5. Exercises

3–5 exercises covering:
- Manual: Implement multi-dimensional array support
- AI collaboration: Generate comprehensive function call test cases
- Debug: Fix array bounds check bug

### 6. Summary and Next Chapter Preview

Summarize complete language understanding. Preview Module 2 (Chapter 6: AST construction).

---

**File Version**: 1.0
**Last Updated**: 2026-01-12
**Status**: ✅ Ready
