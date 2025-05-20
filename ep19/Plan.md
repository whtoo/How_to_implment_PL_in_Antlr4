# Implementation in plan

1. *DONE*

- [x] Add Native JVM binding support(for `print`).
- [x] Basic struct support as record type.

2. **WIP**

- [x] 完整的类型检查系统实现
  - [x] 基本类型兼容性检查（赋值、运算操作）
  - [x] 函数参数类型检查
  - [x] 结构体字段类型检查

- [x] 类型定义支持
  - [x] 支持自定义类型（类似C语言的typedef）
  - [x] 类型别名解析

- [ ] 增强结构体功能
  - [x] 结构体成员方法支持
  - [ ] 结构体字段访问优化
  - [ ] 结构体实例化与初始化


3. TODO

- [ ] 结构体嵌套支持
- [ ] 整合文件作用域管理
- [ ] 类型系统文档编写

4. Bugs
- [x] 修复TypedefSymbol在LocalDefine阶段初始化时目标类型为null的问题
  - 在LocalDefine.visitTypedefDecl中尝试提前解析目标类型
  - 即使无法立即解析，也确保LocalResolver阶段能正确处理
- [x] 修复变量声明类型解析问题（lhs null *= rhs int）
  - 在LocalResolver中添加visitType方法，确保类型节点正确关联到类型对象
  - 解决了变量声明时类型为null的问题

