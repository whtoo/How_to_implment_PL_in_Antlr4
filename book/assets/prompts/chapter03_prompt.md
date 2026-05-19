# Chapter 3 Prompt: Statements and Control Flow

## Chapter Writing Task

Please write a complete chapter for this book.

### Chapter Basic Information

- **Chapter Title**: 第3章：语句与控制流
- **Module**: 模块 1：基础语言与解释器（EP1–EP12）
- **Target Reader**: Engineers with expression evaluation experience from Chapter 2
- **Prerequisites**: Expression evaluation, variable management concepts
- **Repository EP Range**: EP5–EP6 (if/else, while, statement parsing, recursive descent)
- **Position in Book**:
  - Previous Chapter: 第2章：表达式、运算与解释器基础
  - Next Chapter: 第4章：符号表与作用域
- **After completing this chapter, readers should be able to**:
  - Understand control flow statements' syntax and semantics
  - Master recursive descent parsing techniques
  - Represent conditional branches and loops in a compiler
  - Implement a basic interpreter that supports if/else and while

---

## Content Structure Requirements

### 1. Chapter Overview

### 2. Motivation and Real-World Scenarios

Use a scenario about needing conditionals and loops in programs. Explain pain points without these capabilities.

### 3. Human Engineer Line: Technology and Implementation

#### 3.1 Core Concepts

**Key Concepts to Cover**:
1. **Control Flow Statements** - if, if/else, while, block statements
2. **Recursive Descent Parsing** - Top-down parsing technique
3. **Statement vs Expression** - Semantic difference
4. **Control Flow Graph (Basic)** - Visual representation of program flow

Include 1–3 diagram placeholders showing:
- Control flow visualization (if branches, loops)
- Parse tree for statements with expressions
- Interpreter execution flow

#### 3.2 Correspondence with Repository EPs

- Corresponding directory: EP5–EP6
- Key files: Cymbol grammar with statement rules, parser implementation
- Key classes: Statement node types (IfNode, WhileNode, BlockNode)

#### 3.3 Practical Workflow

Steps to:
- Parse if/else statements
- Parse while loops
- Execute control flow in interpreter
- Test with example programs

### 4. AI Collaboration Line

#### 4.1 Context Design

Provide context files:
- Grammar files with statement rules
- Parser implementation
- Test files for control flow
- Interpreter execution examples

#### 4.2 Prompt Templates

Type A: Function implementation Prompt
- Template for adding new control flow statement (e.g., for loop)
Type B: Test generation Prompt
- Template for generating control flow test cases

#### 4.3 AI Should/Should Not Do

**Allowed**:
- Implement new statement types
- Generate test cases for control flow
- Optimize parsing logic
- Generate code comments

**Forbidden**:
- Modify existing statement semantics
- Delete control flow tests
- Change parser architecture
- Break AST node structure

#### 4.4 Verification and Rollback

- Test commands for control flow programs
- Manual verification points
- Git rollback strategies

### 5. Exercises

3–5 exercises covering:
- Manual: Implement for loop parsing
- AI collaboration: Generate comprehensive control flow tests
- Debug: Fix control flow interpretation bug

### 6. Summary and Next Chapter Preview

Summarize control flow mastery. Preview Chapter 4 (symbol tables and scoping).

---

**File Version**: 1.0
**Last Updated**: 2026-01-12
**Status**: ✅ Ready
