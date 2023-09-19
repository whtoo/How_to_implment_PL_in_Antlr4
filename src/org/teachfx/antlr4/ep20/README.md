
``` 

ParseTree --[ASTBuilder]--> AST -- 
[LocalDefine]--> RevisedAST --[LocalResolver]--> RevisedAST
--[TypeChecker]--> RevisedAST
--[DataFlowAnalysis]--> IR
--[Optimizer]--> TargetCodes

```

Todo:
- [ ] 通过访问者模式构建抽象语法树
- [ ] 增加类型实体和相关类型处理内容
- [ ] 将作用域和变量及函数的生命周期进行关联
- [ ] 实现语义检查
- [ ] 增加类和接口定义