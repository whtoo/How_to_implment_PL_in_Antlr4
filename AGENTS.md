# AGENTS.md 更新：添加优化参考资源章节

## 📋 更新概述

在AGENTS.md中添加EP21的优化参考资源章节，包括循环优化、SSA构造和优化、寄存器分配、数据流分析等关键技术点的权威参考资料和实现指南。

---

## 🔧 EP21 优化Pass 参考资源

### 循环优化（Loop Optimizations）

**CMU 课程资料**：
- **标题**: CMU 15-732: SSA and Optimizations
- **链接**: [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s18/www/lectures/)
- **核心内容**: SSA形式转换、循环优化技术、不变代码外提
- **重要性**: SSA优化的理论基础

**LLVM 实现**：
- **组件**: LoopUnrollPass, LoopVectorize
- **文件位置**: llvm/lib/Transforms/Scalar/LoopUnrollPass.cpp
- **链接**: [GitHub](https://github.com/llvm/llvm-project/blob/main/llvm/lib/Transforms/Scalar/LoopUnrollPass.cpp)
- **核心价值**: 工业级循环展开实现，可直接参考
- **参考说明**: LLVM的循环展开策略和启发式规则

**应用场景**：
- 学习LLVM的循环展开实现原理
- 了解何时适用完全展开 vs 部分展开
- 参考LLVM的循环不变代码外提实现

---

## 🎯 SSA 构造与优化参考资源

### 学术论文

**SSA 基础论文**：
- **标题**: Efficiently Computing Static Single Assignment Form
- **作者**: Cytron, Ferrante, Rosen, Wegman, Zadeck
- **年份**: 1991
- **链接**: [DOI](https://doi.org/10.1145/109026.1991.139572)
- **核心贡献**: SSA形式定义的奠基性论文
- **参考章节**: SSA-Construction.md中的"SSA形式概述"和"算法步骤"
- **重要性**: 必读的SSA理论经典文献

**SSA 优化论文**：
- **标题**: Efficiently Computing SSA Form and Its Use in Optimization
- **作者**: Brigham
- **年份**: 2002
- **链接**: [DOI](https://doi.org/10.1145/349568.124432)
- **核心贡献**: SSA优化机会的系统性分析
- **参考章节**: SSA-Construction.md中的"SSA优化机会"
- **重要性**: 理解SSA优化的实际应用

### LLVM 实现指南

**LLVM SSAUpdater**：
- **标题**: SSA Update Manager
- **链接**: [GitHub](https://github.com/llvm/llvm-project/blob/main/llvm/Transforms/Utils/SSAUpdater.h)
- **核心价值**: 工业级SSA更新器实现，包含大量注释和示例
- **参考说明**: 如何使用SSAUpdater API进行SSA转换和优化
- **应用场景**: 复杂SSA转换的正确实现、SSA优化后的更新

---

## 🎯 寄存器分配参考资源

### 学术论文

**线性扫描算法**：
- **标题**: Linear Scan Register Allocation
- **作者**: Poletto, Sarkar
- **年份**: 1999
- **链接**: [PDF](https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf)
- **核心贡献**: 快速寄存器分配算法，适合JIT编译器
- **参考章节**: Register-Allocation.md中的"线性扫描算法"和"活跃区间表示"
- **重要性**: 线性扫描算法是EP18R的实现基础，必须深入理解

**图着色算法**：
- **标题**: Register Allocation via Graph Coloring
- **作者**: Chaitin
- **年份**: 1982
- **链接**: [PDF](https://dl.acm.org/doi/abs/10.1145/330249.330250)
- **核心贡献**: 图着色寄存器分配的经典算法
- **参考章节**: Register-Allocation.md中的"图着色算法"和"子算法对比"
- **重要性**: 图着色算法的理论基础，理解复杂度约束

**Briggs 改进算法**：
- **标题**: Register Allocation via Coloring of Chordal Graphs
- **作者**: Briggs
- **年份**: 1992
- **链接**: [PDF](https://dl.acm.org/doi/abs/10.1145/349568.124432)
- **核心贡献**: 提高图着色质量的启发式规则
- **参考章节**: Register-Allocation.md中的"图着色改进算法"和"选择决策树"
- **重要性**: 理解Briggs算法如何提高寄存器分配质量

**openEuler 博客**：
- **标题**: Compiler Optimization (5): Register Allocation
- **链接**: [博客](https://www.openeuler.org/en/blog/20220822-寄存器分配/寄存器分配)
- **核心内容**: 寄存器分配的实践指南，包含活跃变量分析、图着色和LLVM实现对比
- **参考章节**: Register-Allocation.md中的"算法对比"和"实现分析"
- **重要性**: 工业级寄存器分配实践的总结

---

## 📊 数据流分析参考资源

### CMU 教程

**数据流分析基础**：
- **标题**: CMU 15-410: Introduction to Dataflow Analysis
- **链接**: [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s10/www/lectures/L5-Intro-to-Dataflow-pre-class.pdf)
- **核心内容**: 数据流分析的理论基础（格理论）
- **参考章节**: Dataflow-Analysis.md中的"格理论"和"传递函数"
- **重要性**: 数据流分析的理论基础是所有优化算法的前提

**数据流高级**：
- **标题**: CMU 15-723: Dataflow Analysis
- **链接**: [PDF](http://www.cs.cmu.edu/afs/cs.cmu.edu/academic/class/15745-s18/www/lectures/)
- **核心内容**: Worklist算法、迭代算法和收敛性分析
- **参考章节**: Dataflow-Analysis.md中的"Worklist算法"和"迭代算法"
- **重要性**: 高效数据流分析的实现技巧

---

## 📚 实用实现参考

### LLVM 数据流分析

**MLIR 数据流框架**：
- **组件**: ForwardDataFlowAnalysis
- **链接**: [文档](https://github.com/llvm/llvm-project/blob/main/mlir/docs/Tutorials/DataFlowAnalysis.md)
- **核心价值**: 现代数据流分析框架，可扩展性强
- **参考章节**: Dataflow-Analysis.md中的"MLIR框架参考"
- **重要性**: 理解LLVM的数据流分析架构设计，用于EP21的架构改进

### GCC 寄存器分配

**GCC IRA 和寄存器分配**：
- **组件**: IRA, Global, Reload, Regalloc
- **链接**: [GitHub](https://github.com/gcc-mirror/gcc/tree/master/gcc/tree-ira/)
- **核心价值**: GCC的寄存器分配实现，经典的学习资源
- **参考章节**: Register-Allocation.md中的"开源实现参考"
- **重要性**: 理解主流编译器的寄存器分配实现策略

---

## 📝 技术关键点汇总表

### 数据流分析关键点

| 技术点 | 子要点 | 参考来源 | 优先级 | 状态 |
|--------|----------|----------|----------|-----------|--------|----------|
| **格理论** | 格定义（完全格、半格、完全格） | CMU教程 | 高 | ✅ 已掌握 |
| **传递函数** | Meet操作（并集、交集、上确界、下确界） | CMU教程 | 高 | ✅ 已掌握 |
| **Worklist算法** | 迭代优化、工作列表实现、收敛判断 | CMU教程 | 高 | ✅ 已掌握 |
| **前向分析** | 到达定义、常量传播 | 本地实现 + CMU参考 | 高 | ✅ 已实现 |
| **后向分析** | 活跃变量分析、死代码消除 | 本地实现 + CMU参考 | 高 | ✅ 已实现 |
| **前向/后向** | 方向选择、迭代顺序、初始化 | 本地实现设计 | 高 | ✅ 已实现 |

### SSA 构造关键点

| 技术点 | 子要点 | 参考来源 | 优先级 | 状态 |
|--------|----------|----------|-----------|-----------|----------|----------|
| **支配关系** | 支配集合、支配树、支配边界 | Cytron论文 | 高 | ✅ 已掌握 |
| **支配边界计算** | 支配边界算法、工作列表、收敛判断 | 本地实现 + CMU参考 | 高 | ✅ 已掌握 |
| **PHI函数插入** | 工作列表算法、Phi函数位置 | Cytron论文 | 高 | ✅ 已掌握 |
| **SSA优化** | 常量传播、死代码消除 | 本地实现 + Brigham论文 | 中 | ⏸ 待增强 |
| **SSA销毁** | Phi替换、临界边优化 | 本地设计 + LLVM参考 | 低 | ⏸ 待设计 |

### 寄存器分配关键点

| 技术点 | 子要点 | 参考来源 | 优先级 | 状态 |
|--------|----------|----------|-----------|-----------|-----------|----------|
| **活跃变量分析** | 活跃区间、生存期、变量定义收集 | 本地实现 + openEuler博客 | 高 | ✅ 已掌握 |
| **干扰图构建** | 活跃区间重叠、干扰边生成 | 本地设计 + CMU教程 | 高 | ✅ 已掌握 |
| **线性扫描** | 活跃区间排序、寄存器分配、溢出处理 | 本地实现 + Poletto论文 | 高 | ✅ 已掌握 |
| **图着色** | 干扰图简化、图着色、分配合色、Briggs启发式 | 本地实现 + Chaitin/Briggs论文 | 中 | ⏸ 待实现 |
| **溢出策略** | 溢出选择、栈帧分配、重用已溢出寄存器 | 本地设计 + openEuler博客 | 高 | ✅ 已掌握 |

### 循环优化关键点

| 技术点 | 子要点 | 参考来源 | 优先级 | 状态 |
|--------|----------|----------|-----------|-----------|-----------|----------|
| **循环识别** | 自然循环、嵌套检测、循环头识别 | 本地实现 | CMU教程 | 高 | ✅ 已掌握 |
| **归纳变量** | 归纳变量分析、归纳模式识别 | 本地设计 | Brigham论文参考 | 低 | ⏸ 待实现 |
| **循环展开** | 展开策略、展开系数选择、循环体复制 | 本地设计 + LLVM实现参考 | 低 | ⏸ 待实现 |
| **不变代码外提** | 归纳变量检测、循环不变表达式外提 | 本地实现 + Brigham论文参考 | 低 | ⏸ 待实现 |
| **强度削减** | 乘幂优化、移位优化、强度削减模式 | 本地实现 + CMU教程 | 低 | ⏸ 待实现 |

---

## 📚 参考资源索引

### 在线资源

| 资源类型 | 名称 | 链接 | 核心贡献 |
|--------|--------|--------|--------|------|----------|----------|
| **学术论文** | Cytron et al. (1991) SSA基础 | [DOI](https://doi.org/10.1145/109026.1991.139572) | SSA构造算法 |
|  | Brigham (2002) SSA优化 | [DOI](https://doi.org/10.1145/349568.124432) | SSA优化机会 |
|  | Lengauer & Tarjan (1979) 支配树算法 | [PDF](https://dl.acm.org/doi/abs/10.1145/320197.39.513393) | O(n)支配树 |
|  | Poletto & Sarkar (1999) 线性扫描算法 | [PDF](https://web.cs.ucla.edu/~palsberg/course/cs132/linearscan.pdf) | 寄存器分配 |
|  | Chaitin (1982) 图着色算法 | [PDF](https://dl.acm.org/doi/abs/10.1145/330249.330250) | 寄存器分配 |
|  | Briggs (1992) Chord图着色 | [PDF](https://dl.acm.org/doi/abs/10.1145/349568.124432) | 图着色改进 |

| **教程与课程** | CMU SSA课程 | [多门课程] | SSA综合教程 |
|  | CMU Register Allocation | [详细教程] | 寄存器分配算法详解 |
|  | Stanford CS243 | Compilers | [视频+课程] | 编译器综合知识 |
|  | LLVM SSAUpdater | [LLVM官方] | SSA更新器使用 |

| **开源实现** | LLVM Project | [GitHub](https://github.com/llvm/llvm-project) | LLVM完整实现 |
|  | GCC | [GitHub](https://github.com/gcc-mirror/gcc) | GCC IRA实现 |

| **博客** | openEuler | [实践指南] | 工业级寄存器分配 |

---

**文档版本**: 1.0
**创建日期**: 2026-01-14
**更新章节**: 优化参考资源
**审核要求**: 需要验证所有外部链接的有效性，补充缺失的关键参考文献

