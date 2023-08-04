
```plantuml
class Interpreter {  
  + Superclass: CymbolBaseVisitor<Object>  
  + Fields:  
     - private static final ReturnValue sharedRetValue  
     - private final ScopeUtil scopes  
     - private final Stack<MemorySpace> memoryStack  
     - private MemorySpace currentSpace  
   + Methods:  
     - public Interpreter(ScopeUtil scopes)  
     - public void interpret(ParseTree context)  
     - public void stashSpace(MemorySpace space)  
     - public void restoreSpace()  
     - public Object visitVarDecl(VarDeclContext ctx)  
     - public Object visitExprBinary(ExprBinaryContext ctx)  
     - public Object visitExprPrimary(ExprPrimaryContext ctx)  
     - public Object visitExprFuncCall(ExprFuncCallContext ctx)  
     - public Object visitExprGroup(ExprGroupContext ctx)  
     - public Object visitExprUnary(ExprUnaryContext ctx)  
     - public Object visitStatAssign(StatAssignContext ctx)  
     - public Object visitStatReturn(StatReturnContext ctx)  
     - public Object visitStatBlock(StatBlockContext ctx)  
     - public Object visitStateCondition(StateConditionContext ctx)  
     - public Object visitStateWhile(StateWhileContext ctx)  
     - public Object visitPrimaryBOOL(PrimaryBOOLContext ctx)  
     - public Object visitPrimaryFLOAT(PrimaryFLOATContext ctx)  
     - public Object visitPrimaryCHAR(PrimaryCHARContext ctx)  
     - public Object visitPrimaryID(PrimaryIDContext ctx)  
     - public Object visitFunctionDecl(FunctionDeclContext ctx)  
     - public Object visitPrimaryINT(PrimaryINTContext ctx)  
     - public Object visitPrimarySTRING(PrimarySTRINGContext ctx)  
}

```
