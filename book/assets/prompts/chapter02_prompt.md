# Chapter 2 Prompt: Expressions, Operations, and Interpreter Basics

## Chapter Writing Task

Please write a complete chapter for this book.

### Chapter Basic Information

- **Chapter Title**: 第2章：表达式、运算与解释器基础
- **Module**: 模块 1：基础语言与解释器（EP1–EP12）
- **Target Reader**: Engineers with Java experience and Maven knowledge, interested in building their own programming language
- **Prerequisites**: Java basics, command line, basic Git operations, experience from Chapter 1
- **Repository EP Range**: EP3–EP4 (expression evaluation, visitor pattern, variable memory)
- **Position in Book**:
  - Previous Chapter: 第1章：为人和 AI 搭建最小工作台
  - Next Chapter: 第3章：语句与控制流
- **After completing this chapter, readers should be able to**:
  - Understand the core mechanism of expression evaluation (parsing + calculation)
  - Master the design principles and implementation of the visitor pattern
  - Represent and compute mathematical expressions in a compiler
  - Understand basic variable representation in memory

---

## Content Structure Requirements (Must follow in order)

### 1. Chapter Overview

Use 1–3 sentences to explain what problem this chapter solves, which stage of the compiler pipeline it is in, and clarify the learning value.

### 2. Motivation and Real-World Scenarios

Use a real engineering scenario/story to introduce the chapter theme. Explain what pain points you'll face in real projects without this chapter's capabilities.

### 3. Human Engineer Line: Technology and Implementation

#### 3.1 Core Concepts

Explain key concepts in plain language. Must include 1–3 textual descriptions of small diagrams (using `[图X: description]` placeholders). Use metaphors and analogies to reduce understanding difficulty.

**Key Concepts to Cover**:
1. **Expression Evaluation** - How computers parse and calculate expressions
2. **Abstract Syntax Tree (AST)** - Tree representation of expressions
3. **Visitor Pattern** - Design pattern for traversing and transforming AST
4. **Operator Precedence** - How multiplication/division binds tighter than addition/subtraction

#### 3.2 Correspondence with Repository EPs

Clearly explain:
- Corresponding directory: EP3-EP4 in the repository
- Key classes/interfaces/methods and what they do
- Don't paste full code, only key methods
- Use ```java code blocks with detailed Chinese comments

**Key Files to Cover**:
- EP3 grammar files (LabeledExpr.g4)
- EP4 expression evaluation visitor (EvalVisitor.java)
- AST node classes (AddNode, SubNode, MulNode, DivNode, IntNode)

#### 3.3 Practical Workflow

Provide specific steps to run this chapter's code from command line/IDE:
- Which directory to enter
- Which Maven commands or test classes to run
- What the expected output looks like
- How to verify results are correct
- Provide troubleshooting tips

### 4. AI Collaboration Line: Context Engineering Perspective

#### 4.1 Context Design

Explain: To help AI complete this chapter's tasks, what "context" will you provide to AI? List by type:
- Which source code files (by filename)
- Which README/design documents
- Which example inputs/outputs
- Which test classes
- Provide brief organization explanation for these contexts
- Emphasize context completeness and precision

#### 4.2 Prompt Templates (for AI)

Provide 1–2 Prompt templates that can be directly copied to AI. Template types (at least one):
- Type A: Function implementation Prompt (add new syntax, implement new algorithm)
- Type B: Optimization implementation Prompt (transform IR, apply optimization rules)
- Type C: Test generation Prompt (generate test cases, cover edge cases)

Prompt features:
- Clearly state task objectives
- List specific requirements (steps, format, constraints)
- Provide reference context files
- Describe expected output format

#### 4.3 AI Should Do / Should Not Do

List 3–5 items for "things AI is allowed to do" related to this chapter:
- ✅ Implement clearly defined functional modules
- ✅ Generate test cases and helper code
- ✅ Optimize specific algorithm implementations
- ✅ Generate code comments and documentation

List 3–5 items for "things AI is forbidden to do" related to this chapter:
- ❌ Large-scale refactoring of directory structure
- ❌ Modify core interface definitions (unless explicitly requested)
- ❌ Delete test cases or reduce test coverage
- ❌ Break existing EP module boundaries

#### 4.4 Verification and Rollback Strategy

Tell readers: Before accepting AI's modifications, at least what verifications should be done?
- Which tests to run (provide specific commands)
- What key points to check manually
- How to check logs and outputs
- If AI modifications cause problems, what simple rollback strategies exist:
  - Git operation suggestions (stash, checkout, reset)
  - Specific commands to quickly restore to pre-modification state
  - How to save AI modifications for later learning

### 5. Exercises

Design 3–5 exercises, divided into two types:
- "Manual Implementation Version": Reader does it completely themselves, not relying on AI
- "AI Collaboration Version": Reader designs context and Prompt, lets AI assist in completion

Append a brief "problem-solving tip" after each exercise, but **do not provide complete reference answers**.

Exercise type diversity:
- Basic consolidation exercises (understand concepts)
- Practical application exercises (implement features)
- Debugging/optimization exercises (improve code)
- AI collaboration exercises (design Prompts)

### 6. Chapter Summary and Next Chapter Preview

Summarize key learnings in a few paragraphs. Clearly indicate how these learnings will be used in the next chapter:
- For example: "Symbol tables will be used for type checking; SSA will be used for dataflow optimization, etc."

Preview the next chapter's theme and connection to this chapter. Provide "where are you now" pipeline position diagram (textual description).

---

**Hard Requirements**:

- [ ] Chapter overview: 1–3 sentences explaining chapter problem and position
- [ ] Motivation scenario: Real engineering scenario, explaining missing pain points
- [ ] Core concepts: Plain explanation, 1–3 diagram placeholders
- [ ] EP correspondence: Clear directories, key classes, method walkthrough
- [ ] Practical workflow: Runnable steps, expected output, troubleshooting
- [ ] AI context design: Source code/documentation/test file lists, organization explanation
- [ ] AI Prompt templates: At least 1 reusable Prompt template
- [ ] AI should/should not do: 3–5 items each in clear lists
- [ ] Verification and rollback: Test commands, checkpoints, git rollback strategies
- [ ] Exercises: 3–5 questions, including manual and AI collaboration versions, with hints
- [ ] Chapter summary: Summarize learnings, preview next chapter, pipeline position

---

**File Version**: 1.0
**Last Updated**: 2026-01-12
**Status**: ✅ Ready to send to technical writing agent
