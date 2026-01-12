# Chapter 4 Prompt: Symbol Tables and Scoping

## Chapter Writing Task

Please write a complete chapter for this book.

### Chapter Basic Information

- **Chapter Title**: 第4章：符号表与作用域
- **Module**: 模块 1：基础语言与解释器（EP1–EP12）
- **Target Reader**: Engineers with control flow experience from Chapter 3
- **Prerequisites**: Scope concepts, variable declarations
- **Repository EP Range**: EP9–EP10 (global/local scope, variable declarations, scope chain)
- **Position in Book**:
  - Previous Chapter: 第3章：语句与控制流
  - Next Chapter: 第5章：数组与更复杂的特性
- **After completing this chapter, readers should be able to**:
  - Understand the concept and hierarchy of scopes
  - Master variable declaration and resolution rules
  - Build a basic symbol table system
  - Handle name conflicts and variable visibility correctly

---

## Content Structure Requirements

### 1. Chapter Overview

### 2. Motivation and Real-World Scenarios

Use a scenario about variable scope confusion in large programs. Explain pain points without proper symbol table management.

### 3. Human Engineer Line: Technology and Implementation

#### 3.1 Core Concepts

**Key Concepts to Cover**:
1. **Symbol Table** - Data structure tracking variable names and types
2. **Scope** - Regions where names are visible/accessible
3. **Scope Chain** - Hierarchical nesting of scopes
4. **Variable Resolution** - Finding the right definition for each name use

Include 1–3 diagram placeholders showing:
- Scope nesting visualization
- Symbol table structure
- Variable resolution process

#### 3.2 Correspondence with Repository EPs

- Corresponding directory: EP9–EP10
- Key files: SymbolTable.java, Scope.java, Symbol.java
- Key classes: Symbol table implementation with scope management

#### 3.3 Practical Workflow

Steps to:
- Build symbol table from parsed program
- Resolve variable references
- Test with nested scopes (functions, blocks)
- Verify correct variable binding

### 4. AI Collaboration Line

#### 4.1 Context Design

Provide context files:
- Symbol table implementation
- Scope management code
- Test cases for scoping
- Parser integration examples

#### 4.2 Prompt Templates

Type A: Function implementation Prompt
- Template for implementing scope chain lookup
Type B: Test generation Prompt
- Template for generating scope boundary test cases

#### 4.3 AI Should/Should Not Do

**Allowed**:
- Implement scope resolution algorithms
- Generate test cases for nested scopes
- Optimize symbol table lookup
- Generate scope visualization helpers

**Forbidden**:
- Change scope semantics (shadowing rules)
- Delete symbol table tests
- Modify variable declaration parsing
- Break scope hierarchy

#### 4.4 Verification and Rollback

- Test commands for scoping programs
- Manual verification of variable resolution
- Git rollback strategies

### 5. Exercises

3–5 exercises covering:
- Manual: Implement simple symbol table
- AI collaboration: Generate nested scope test cases
- Debug: Fix variable shadowing bug

### 6. Summary and Next Chapter Preview

Summarize symbol table understanding. Preview Chapter 5 (arrays and complex features).

---

**File Version**: 1.0
**Last Updated**: 2026-01-12
**Status**: ✅ Ready
