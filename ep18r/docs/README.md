# EP18R 文档中心

EP18R 寄存器虚拟机文档中心 - 按主题组织的完整文档体系。

## 目录结构

```
docs/
├── 01_core_design/                    # 核心设计文档
│   └── EP18R_寄存器虚拟机设计规范_整合版.md
│
├── 02_implementation_standards/       # 实现与测试标准
│   └── EP18R_测试规范_整合版.md
│
├── 03_development_plans/             # 开发计划文档
│   └── EP18R_TDD执行计划_最终整合版.md
│
├── 04_issues_improvements/           # 问题追踪与改进
│   └── 改进计划.md
│
├── 05_cross_ep_coordination/         # 跨EP协调文档
│   └── EP18R-EP21联动融合计划.md
│
└── backup_20260109/                   # 历史备份
```

## 文档说明

### 01_core_design - 核心设计
**主文档**: `EP18R_寄存器虚拟机设计规范_整合版.md`

EP18R寄存器虚拟机的核心设计规范，包含：
- 虚拟机架构设计（寄存器文件、指令格式）
- 寄存器指令集规范（42条指令完整定义）
- 执行引擎设计（取指-解码-执行循环）
- 寄存器分配策略（ABI调用约定）
- 汇编器与反汇编器设计
- 内存管理与垃圾回收
- Struct统一设计方案

**读者**: 架构师、核心开发者、编译原理学习者

---

### 02_implementation_standards - 实现标准
**主文档**: `EP18R_测试规范_整合版.md`

EP18R的实现和测试标准，包含：
- 测试策略与架构（单元/集成/系统测试）
- TDD执行计划与方法论
- 测试用例详细规范
- 测试模板与最佳实践
- 覆盖率与质量要求

**读者**: 开发者、测试工程师、代码审查者

---

### 03_development_plans - 开发计划
**主文档**: `EP18R_TDD执行计划_最终整合版.md`

EP18R的开发执行计划，包含：
- TDD方法论在EP18R的应用
- 红-绿-重构循环标准
- 测试驱动开发工作流
- 开发里程碑和交付物
- 开发进度跟踪

**读者**: 项目管理者、开发者

---

### 04_issues_improvements - 问题与改进
**主文档**: `改进计划.md`

EP18R的问题追踪与改进计划，包含：
- 已知问题和待修复Bug
- 性能优化计划
- 功能增强建议
- 技术债务管理

**读者**: 开发者、项目管理者

---

### 05_cross_ep_coordination - 跨EP协调
**主文档**: `EP18R-EP21联动融合计划.md`

EP18R与其他EP（特别是EP21）的协调计划，包含：
- 跨EP功能依赖关系
- 数据结构和接口共享
- 测试用例复用策略
- 联合开发计划

**读者**: EP18R/EP21开发者、项目协调者

---

## 文档维护原则

### 单一事实来源
- 每个技术概念只在一个主文档中详细定义
- 其他文档使用交叉引用：`详见[相关章节](文档路径#章节)`
- 避免内容重复，保持文档一致性

### 文档更新规范
- **新增内容**: 优先补充到主文档相应章节
- **重大修改**: 创建新版本，旧版本移至backup
- **定期审查**: 每季度检查文档一致性和时效性

### 版本管理
- 每次重大文档更新创建备份
- 备份目录格式：`backup_YYYYMMDD/`
- 保留文档变更历史记录

---

## 文档使用指南

### 新手入门
1. 阅读 `01_core_design/EP18R_寄存器虚拟机设计规范_整合版.md` - 理解核心架构
2. 阅读 `02_implementation_standards/EP18R_测试规范_整合版.md` - 了解测试标准
3. 运行测试套件验证环境配置

### 开发者参考
1. 查看核心设计文档了解实现细节
2. 遵循实现标准和测试规范进行开发
3. 参考开发计划跟踪进度

### 问题排查
1. 查看 `04_issues_improvements/改进计划.md` - 已知问题列表
2. 查看测试规范了解质量标准
3. 查看核心设计文档理解系统行为

---

## 文档清理历史

**2026-01-09**: 文档清理与优化
- 合并重叠文档（ABI设计文档、OpenSpecKit规范 → 核心设计文档）
- 删除冗余文档（TDD重构计划、单元测试案例集 → 测试规范整合版）
- 创建主题化的目录结构
- 删除不属于EP18R的文档（AGENTS.md、CLAUDE.md、EP21文档等）
- 保留历史备份：`docs_backup_20260109/`

**2026-01-11**: 测试错误修复
- 修复 RefactoringVerificationTest.java 中重复定义的 VMConfig、VMException、ErrorCode 类
- 修复 TDD_CodeQualityTest.java 中错误的 import 路径
- 更新 import 路径：org.teachfx.antlr4.ep18r.stackvm.VMConfig → org.teachfx.antlr4.ep18r.stackvm.config.VMConfig
- 更新 RegisterVMInterpreter 类路径：org.teachfx.antlr4.ep18r.stackvm.RegisterVMInterpreter → org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter
- 所有 131 个测试通过

---

## 联系与反馈

如对文档有疑问或建议：
- 提交GitHub Issue
- 参与项目讨论
- 提交Pull Request改进文档

---

**最后更新**: 2026-01-11
**维护者**: EP18R开发团队
