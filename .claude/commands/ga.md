---
name: ga
description: 智能暂存文件，自动识别并暂存相关文件
---

# Git Add 快速命令

## 命令用途
智能暂存文件，支持按模式自动识别并暂存相关文件。

## 使用方式
```
/ga [文件模式]
```

## 参数说明

### 模式选项
- `all` / `.` - 暂存所有修改
- `ep{NN}` - 暂存指定EP的所有修改（例如：ep18、ep21）
- `docs` - 暂存所有文档
- `test` - 暂存所有测试文件
- `*.java` - 暂存所有Java文件
- `*.md` - 暂存所有Markdown文件
- 无参数 - 列出可暂存的修改，等待选择

## 执行流程

### 1. 查看未暂存修改
```bash
git status --short
```

### 2. 按模式匹配
- 如果指定 `ep{NN}`: 暂存 `ep{NN}/**/*`
- 如果指定 `docs`: 暂存 `docs/**/*`
- 如果指定 `test`: 暂存 `**/test/**/*.java`
- 如果指定扩展名: 暂存匹配的所有文件
- 如果无参数: 显示分类列表供选择

### 3. 确认暂存
显示即将暂存的文件列表，等待确认。

## 示例

### 示例1: 暂存EP21的所有修改
```
/ga ep21
```
**执行结果**:
```
暂存以下文件：
  ep21/src/main/java/.../SSAGraph.java
  ep21/src/test/java/.../SSATest.java
确认暂存? [Y/n]
```

### 示例2: 暂存所有文档
```
/ga docs
```
**执行结果**:
```
暂存以下文件：
  docs/EP21_设计文档.md
  README.md
确认暂存? [Y/n]
```

### 示例3: 暂存所有Java文件
```
/ga *.java
```
**执行结果**:
```
暂存以下文件：
  ep18/src/main/java/.../CymbolStackVM.java
  ep21/src/main/java/.../SSAGraph.java
确认暂存? [Y/n]
```

### 示例4: 查看所有修改
```
/ga
```
**执行结果**:
```
未暂存的修改：

EP18 (3 files):
  - ep18/src/main/java/.../CymbolStackVM.java
  - ep18/src/main/java/.../StackFrame.java
  - ep18/src/test/.../VMTest.java

文档 (2 files):
  - README.md
  - docs/EP18_设计文档.md

使用 /ga [模式] 暂存文件
```

## 文件分类

| 类别 | 模式 | 匹配路径 |
|------|------|---------|
| EP18 | `ep18` | `ep18/**/*` |
| EP18R | `ep18r` | `ep18r/**/*` |
| EP19 | `ep19` | `ep19/**/*` |
| EP20 | `ep20` | `ep20/**/*` |
| EP21 | `ep21` | `ep21/**/*` |
| 文档 | `docs` | `docs/**/*`, `*.md` |
| 测试 | `test` | `**/test/**/*.java` |
| 配置 | `config` | `.claude/**/*`, `.gitignore` |
| Java源码 | `*.java` | `**/*.java` |

## 最佳实践

1. **按EP暂存**: 先确认修改属于哪个EP，再暂存
2. **单主题暂存**: 避免混合多个主题的文件
3. **确认后暂存**: 暂存前仔细检查文件列表
4. **配合/gc使用**: 暂存后使用 `/gc` 提交

## 注意事项
- 暂存前会显示文件列表供确认
- 支持多个模式（空格分隔）
- 暂存后可使用 `/gc` 快速提交

---
