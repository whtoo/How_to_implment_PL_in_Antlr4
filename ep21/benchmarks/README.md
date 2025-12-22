# EP21 编译器基准测试集

## 概述
本目录包含用于EP21编译器性能评估和优化验证的标准基准测试程序集。

## 目录结构
```
benchmarks/
├── stanford/          # Stanford基准测试程序
│   ├── fib.cym       # 斐波那契数列（递归测试）
│   ├── matmul.cym    # 矩阵乘法（循环优化测试）
│   └── quicksort.cym # 快速排序（递归和数组测试）
├── spec/              # SPEC CPU风格基准测试
│   └── numerical.cym # 数值计算（算术强度测试）
├── optimization/      # 编译器优化专项测试
│   ├── loop_invariant.cym # 循环不变量外提测试
│   ├── dead_code.cym      # 死代码消除测试
│   └── constant_prop.cym  # 常量传播测试
└── validation/        # 验证测试（待添加）
```

## 使用说明

### 1. 运行基准测试
```bash
# 使用EP21编译器编译并运行基准测试
cd ep21
./scripts/run.sh compile benchmarks/stanford/fib.cym
./scripts/run.sh run benchmarks/stanford/fib.cym
```

### 2. 性能测量
基准测试集设计用于测量：
- **编译时间**: 编译器前端、优化、代码生成各阶段耗时
- **执行时间**: 生成代码的运行时间
- **内存使用**: 编译器和生成代码的内存消耗
- **代码大小**: 生成的目标代码体积

### 3. 优化验证
每个优化专项测试都设计有明确的优化机会：
- `loop_invariant.cym`: 循环不变表达式 `z` 应被移出循环
- `dead_code.cym`: 未使用的变量 `d` 和 `f` 应被消除
- `constant_prop.cym`: 常量表达式应被折叠，死分支应被消除

## 版本管理
基准测试集版本信息记录在 `VERSION` 文件中。每次更新基准测试程序时，应更新版本号并添加变更说明。

## 扩展指南
添加新基准测试程序时：
1. 选择适当的分类目录（stanford/spec/optimization/validation）
2. 提供清晰的注释说明测试目的
3. 包含预期的优化结果或性能特征
4. 更新本README文件

## 参考标准
- **Stanford Benchmarks**: 经典编译器测试集
- **SPEC CPU**: 工业标准CPU性能测试套件
- **Compiler Optimization Tests**: 针对特定优化的微基准测试