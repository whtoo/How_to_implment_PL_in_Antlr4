---
name: gs
description: 快速查看git状态，显示分类的修改文件列表
---

# Git Status 快速命令

## 命令用途
快速查看 git 状态，按EP和类型分类显示修改文件。

## 使用方式
```
/gs [详细程度]
```

## 参数说明

### 详细程度
- 无参数 / `short` - 显示简要分类
- `full` / `detail` - 显示完整状态
- `diff` - 显示变更内容

## 执行流程

### 1. 获取git状态
```bash
git status --short
```

### 2. 分类整理
按以下规则分类：
- **已暂存**: 绿色 M 标记
- **未暂存**: 红色 M 标记
- **未跟踪**: ?? 标记
- 按EP和文件类型分组

### 3. 格式化输出
```
📊 Git 状态概览

✅ 已暂存 (Staged) - 3 files
  EP18:
    ✓ ep18/src/main/java/.../CymbolStackVM.java
    ✓ ep18/src/test/.../VMTest.java
  文档:
    ✓ README.md

🔄 未暂存 (Modified) - 2 files
  EP21:
    ✗ ep21/src/main/java/.../SSAGraph.java
    ✗ ep21/src/test/.../SSATest.java

❓ 未跟踪 (Untracked) - 1 file
  新建:
    ? docs/NEW_EP.md

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
总计: 6 files (3 staged, 2 modified, 1 untracked)
```

## 示例

### 示例1: 简要状态
```
/gs
```
**执行结果**:
```
📊 Git 状态概览

🔄 未暂存 (Modified) - 2 files
  EP21:
    ✗ ep21/src/main/java/.../SSAGraph.java
    ✗ ep21/src/test/.../SSATest.java

总计: 2 files (0 staged, 2 modified, 0 untracked)
```

### 示例2: 完整状态
```
/gs full
```
**执行结果**:
```
📊 Git 状态概览

✅ 已暂存 (Staged) - 3 files
  EP18:
    ✓ ep18/src/main/java/.../CymbolStackVM.java (12 lines changed)
    ✓ ep18/src/test/.../VMTest.java (5 lines changed)
  文档:
    ✓ README.md (3 lines changed)

🔄 未暂存 (Modified) - 2 files
  EP21:
    ✗ ep21/src/main/java/.../SSAGraph.java (24 lines changed)
    ✗ ep21/src/test/.../SSATest.java (8 lines changed)

❓ 未跟踪 (Untracked) - 1 file
  新建:
    ? docs/NEW_EP.md

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
总计: 6 files (3 staged, 2 modified, 1 untracked)

当前分支: feature/ssa-optimization
上游分支: origin/feature/ssa-optimization
最新提交: feat(ep21): implement SSA transformation
```

### 示例3: 显示变更内容
```
/gs diff
```
**执行结果**:
```
📊 变更内容

🔄 ep21/src/main/java/.../SSAGraph.java
@@ -45,7 +45,9 @@
 public void buildSSA() {
     this.blocks = cfg.getBasicBlocks();
-    computeDominanceFrontier();
+    // 先计算支配边界
+    computeDominanceFrontier();
+    insertPhiFunctions();
 }
```

## 状态符号说明

| 符号 | 含义 |
|------|------|
| ✅ / ✓ | 已暂存 (Staged) |
| 🔄 / ✗ | 未暂存 (Modified) |
| ❓ / ? | 未跟踪 (Untracked) |
| 🗑️ / D | 已删除 (Deleted) |
| ➕ / A | 新增 (Added) |

## EP分类

| EP | 路径 | 描述 |
|----|------|------|
| EP1-12 | `ep{NN}/` | 前端基础 |
| EP18 | `ep18/` | 栈式虚拟机 |
| EP18R | `ep18r/` | 寄存器虚拟机 |
| EP19 | `ep19/` | 编译器解释器 |
| EP20 | `ep20/` | 完整编译器 |
| EP21 | `ep21/` | 高级优化 |

## 文件类型分类

| 类型 | 扩展名/路径 | 示例 |
|------|-----------|------|
| Java源码 | `*.java` | CymbolStackVM.java |
| 文档 | `*.md` | README.md |
| 配置 | `.gitignore`, `.xml` | pom.xml |
| 测试 | `**/test/**/*.java` | SSATest.java |
| 语法 | `*.g4` | Cymbol.g4 |

## 快捷操作

显示状态后，可直接使用：

- `/ga [模式]` - 暂存文件
- `/gc [信息]` - 提交修改
- `git diff [文件]` - 查看详细差异
- `git checkout [文件]` - 恢复文件

## 注意事项
- 状态信息实时获取
- 显示路径为相对路径
- 文件修改行数为近似值

---
