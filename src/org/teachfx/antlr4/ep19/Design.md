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

## symtab

## visitor


