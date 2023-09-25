
``` 

ParseTree --[ASTBuilder]--> AST -- 
[LocalDefine]--> RevisedAST --[LocalResolver]--> RevisedAST
--[TypeChecker]--> RevisedAST
--[DataFlowAnalysis]--> IR
--[Optimizer]--> TargetCodes

```

## DONE
- [x] 通过访问者模式构建抽象语法树
- [x] 增加ASTree printer以检查语法树构建是否合乎期望

## Removed
~~- [ ] 增加类和接口定义~~