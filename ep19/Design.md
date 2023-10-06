# 设计 & 构造解析



## overview

``` mermaid
    flowchart LR
        Input --> CharStream 
        CharStream --> Tokens 
        Tokens --> ParserTree
        ParserTree --Define--> AnnotatedParserTree
        AnnotatedParserTree --Resolve--> AnnotatedParserTree 
        AnnotatedParserTree --> Interpreter
```

``` plantuml
@startuml

interface Scope {
    + String getScopeName();

    + Scope getEnclosingScope();

    + void define(Symbol sym);

    + Symbol resolve(String name);

    + Type lookup(String name);
}

abstract class BaseScope implements Scope {
    # Scope enclosingScope
    # Map<String,Symbol> symbols
}

note left of BaseScope
    作用域对象，基本上是对某个词法域的符号表管理者。
    1. 生成并存储符号表
    2. 增删改符号表中的符号
    3. 提供符号查询
end note

class Symbol {

}

note top of Symbol
    符号对象
    1. 表示语法对象的符号表示
    2. 提供此符号作用域的指向（必要）
    3. 提供此符号的内存只想（不必要）
end note

BaseScope . Symbol
@enduml
```

## symbol_table
1. 字典 - 名称和映射对象的键值对
2. 作用域的表达者

## visitor
1. tree-walking
2. 无损变换

## 执行机器

### 堆栈机器

#### 关键点
   - 过程/函数的表示-栈帧 
   - 栈帧-作用域的体现者
   - 作用域-名称与变量的字典和变量生存期
   - 表达式计算-堆栈执行视角
   - 指令分排器-线性指令执行的流水线管理者
   - 自底向上-分解代码到执行指令
   - 表达式翻译-三地址与DAG
   - 临时变量-作为中间计算结果的载体
   - if-Goto Label - 跳转与分支判断
   - IR设计与实现 - 以目标生成视角的逆推产物

#### 逻辑实现
1. 表达式计算过程
   1. 表达式求值-算术求值视角
   2. 扩展基本数据类型-Float
   3. 扩展运算类型-bool逻辑
   4. 扩展求值能力-函数调用(`call func`)
2. 块作用域(BlockScope)
3. 函数作用域-带入参列表的块作用域来看
4. `call func`
   1. 求值上下文与操作数栈
   2. 上下文保存与恢复
   3. stack-frame登场
   4. 调用上下文的组成
      1. args
      2. locals
      3. return address
      4. return value
      5. 求值上下文的链接-静态链vs动态链
   5. 堆叠之栈-调用链
5. 指令跳转-IP与FP的变化
6. IF-Break-条件跳转类指令
#### [指令设计](../ep18/VM_Design.md)




