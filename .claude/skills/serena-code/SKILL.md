---
name: serena-code
description: Serena智能代码分析工具使用指南，提供符号级代码探索、编辑和重构能力。
version: v1.0
tags: [serena, code-analysis, refactoring, symbols]
allowed-tools: mcp__serena_*
requires-skills: []
---

# Serena代码分析工具

## 🎯 垂直职责
**单一职责**: 符号级代码探索、搜索、编辑和重构

## 📦 核心能力

### 1. 代码探索
| 工具 | 用途 | 示例 |
|------|------|------|
| `list_dir` | 列出目录内容 | 列出`ep21/src/main/java` |
| `find_file` | 查找文件 | 查找`*Optimizer.java` |
| `get_symbols_overview` | 获取文件符号概览 | 查看类的所有方法 |
| `find_symbol` | 按名称查找符号 | 查找`SSAGraph`类 |
| `search_for_pattern` | 模式搜索 | 搜索`extends.*Optimizer` |

### 2. 代码编辑
| 工具 | 用途 | 示例 |
|------|------|------|
| `replace_symbol_body` | 替换符号体 | 重写整个方法 |
| `insert_after_symbol` | 符号后插入 | 添加新方法 |
| `insert_before_symbol` | 符号前插入 | 添加导入 |
| `rename_symbol` | 重命名符号 | 重命名类/方法/字段 |

### 3. 项目理解
| 工具 | 用途 | 示例 |
|------|------|------|
| `read_memory` | 读取项目记忆 | 读取`EP21_TECH_MEM.md` |
| `write_memory` | 写入项目记忆 | 更新技术文档 |
| `list_memories` | 列出所有记忆 | 查看可用记忆 |

### 4. 智能思考
| 工具 | 用途 | 触发时机 |
|------|------|----------|
| `think_about_collected_information` | 分析收集信息 | 搜索后 |
| `think_about_task_adherence` | 检查任务一致性 | 编辑前 |
| `think_about_whether_you_are_done` | 判断完成状态 | 任务结束 |

## 🔗 关系图
→ **无依赖** (独立工具集)
← **所有技能依赖** (被广泛使用)

## 🚀 使用流程

### 标准代码探索流程
```bash
# 1. 获取符号概览 (避免读取整个文件)
mcp__serena__get_symbols_overview("ep21/src/main/java/.../SSAGraph.java")

# 2. 查找特定符号
mcp__serena__find_symbol("SSAGraph", relative_path="ep21", depth=1)

# 3. 读取符号体 (仅读需要的部分)
mcp__serena__find_symbol("computeDominators", include_body=true)

# 4. 查找引用
mcp__serena__find_referencing_symbols("computeDominators", "SSAGraph.java")

# 5. 分析信息
mcp__serena__think_about_collected_information()
```

### 符号级编辑流程
```bash
# 1. 定位符号
mcp__serena__find_symbol("methodName", include_body=true)

# 2. 检查任务一致性
mcp__serena__think_about_task_adherence()

# 3. 替换符号体
mcp__serena__replace_symbol_body("methodName", newBody)

# 4. 验证完成
mcp__serena__think_about_whether_you_are_done()
```

### 项目记忆更新流程
```bash
# 1. 读取现有记忆
mcp__serena__read_memory("EP21_TECH_MEM.md")

# 2. 更新内容
mcp__serena__edit_memory("EP21_TECH_MEM.md", needle, repl, "literal")

# 3. 或完全重写
mcp__serena__write_memory("EP21_TECH_MEM.md", newContent)
```

## 📊 效率对比

| 操作 | 传统工具 | Serena工具 | 节省 |
|------|----------|------------|------|
| 查找方法 | grep + Read | `find_symbol` | 60% |
| 理解类结构 | Read (300行) | `get_symbols_overview` | 85% |
| 重构方法 | Edit (行号) | `replace_symbol_body` | 50% |
| 跨文件引用 | grep全局 | `find_referencing_symbols` | 70% |

## ⚠️ 最佳实践

### ✅ 使用符号级工具
```bash
# 推荐: 符号级操作
mcp__serena__find_symbol("computeDominators", include_body=true)
mcp__serena__replace_symbol_body("computeDominators", newBody)

# 避免: 读取整个文件
Read file_path="ep21/.../SSAGraph.java"
```

### ✅ 使用模式搜索
```bash
# 推荐: 搜索模式
mcp__serena__search_for_pattern("implements.*IFlowOptimizer", restrict_search_to_code_files=true)

# 避免: Grep搜索
Grep pattern="implements.*IFlowOptimizer"
```

### ✅ 使用思考工具
```bash
# 在关键节点使用思考工具
mcp__serena__think_about_collected_information()  # 搜索后
mcp__serena__think_about_task_adherence()        # 编辑前
mcp__serena__think_about_whether_you_are_done()  # 完成时
```

### ❌ 避免重复读取
```bash
# 错误: 多次读取同一文件
Read file_path="SSAGraph.java"  # 第1次
Read file_path="SSAGraph.java"  # 第2次 - 浪费

# 正确: 使用符号概览 + 选择性读取
mcp__serena__get_symbols_overview("SSAGraph.java")  # 1次
mcp__serena__find_symbol("specificMethod", include_body=true)  # 按需
```

---
*版本: v1.0 | 垂直职责: Serena代码分析工具 | 2025-12-23*
