# CCLSP 使用指南

**版本**: v1.0 | **更新时间**: 2025-12-21

CCLSP (Claude Code Language Server Protocol) 为 Java/Python/TypeScript 提供代码智能分析，**优先使用CCLSP而非grep搜索**。

## 核心API

### 1. 诊断检查
```bash
mcp__cclsp__get_diagnostics file_path="绝对路径"
```
检查文件错误、警告、提示。

### 2. 查找定义
```bash
mcp__cclsp__find_definition file_path="..." symbol_name="符号名" symbol_kind="类型"
```
定位类/方法/变量的定义位置。

**类型**: class, method, function, variable

### 3. 查找引用
```bash
mcp__cclsp__find_references file_path="..." symbol_name="符号名" include_declaration=true
```
查找符号的所有引用位置。

### 4. 符号重命名
```bash
mcp__cclsp__rename_symbol file_path="..." symbol_name="旧名" new_name="新名" dry_run=true
```
安全重命名（预览或执行）。

### 5. 严格重命名
```bash
mcp__cclsp__rename_symbol_strict file_path="..." line=行号 character=列号 new_name="新名"
```
精确定位重命名。

### 6. 重启服务器
```bash
mcp__cclsp__restart_server extensions=["java"]  # 指定语言
mcp__cclsp__restart_server                      # 重启所有
```

## 最佳实践

### ✅ 优先使用CCLSP
**规则**: 任何代码研究任务中，**始终优先使用CCLSP**而非grep或手动搜索。

**优势**:
- 精确：直接获取准确信息
- 高效：避免无关匹配
- 智能：理解语法语义
- 全面：跨文件依赖分析

### 工作流
```bash
1. 检查诊断 → mcp__cclsp__get_diagnostics
2. 查找定义 → mcp__cclsp__find_definition
3. 分析引用 → mcp__cclsp__find_references
4. 深度阅读 → Read file
```

## 常见问题

### 服务器无响应
```bash
mcp__cclsp__restart_server extensions=["java"]
```

### 诊断不准确
```bash
mvn clean compile  # 重新编译
mcp__cclsp__restart_server
```

### 找不到符号
- 检查符号名称拼写
- 尝试不同 symbol_kind (class/method/function)
- 确认文件路径正确

### 重命名失败
```bash
# 先预览
mcp__cclsp__rename_symbol dry_run=true
# 检查冲突
mcp__cclsp__find_references symbol_name="新名称"
```

---

**配置**: `.claude/cclsp.json` | **MCP**: `.mcp.json` cclsp server

