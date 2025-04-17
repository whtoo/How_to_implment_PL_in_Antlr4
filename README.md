# 从解释计算的视角：如何亲手创造一门编程语言Cyson

## 项目简介
**每一个watch和star都是这场梦幻之旅的⛽️与干柴**
[![Security Status](https://www.murphysec.com/platform3/v31/badge/1718907022023983104.svg)](https://www.murphysec.com/console/report/1718907021914931200/1718907022023983104)
**兴❤️如🔥，其势如风.意许如油，汩汩不息。**

## 为什么会有这个系列的教程？
我从开始编程就一直在想，如果我是一个意图规划者，
而不是人肉编码器该有多好。

因此，我一头扎进了编程语言构造和分析的汪洋大海中去捞
那根我心中的定海神针。

现在，我真正走进了编译后端处理和程序分析后，我感到自己
有太多想记录的知识、技能、想法，以及创造好用的新工具和新过程。

所以，这就是我的起点，希望你能和我一样享受这个过程。

## 2. 项目内容
### 2.1 全局鸟瞰
- [x] [ep1](ep1)--使用antlr的EBNF语法实现Hello World解析示例。
- [x] [ep2](ep2)--使用g4语法描述嵌套数组结构`{1,2,{3,4..}...}`并实现遍历打印功能。
- [x] [ep3](ep3)--实现基础四则运算的微型计算器。
- [x] [ep4](ep4)--实现支持交互式输入的算术表达式计算器。
- [x] [ep5](ep5)--从Java源代码中提取接口定义的解析工具。
- [x] [ep6](ep6)--实现CSV文件解析和内容提取工具。
- [x] [ep7](ep7)--完整的JSON语法解析器实现。
- [x] [ep8](ep8)--从源代码生成抽象语法树(AST)的提取器。
- [x] [ep9](ep9)--ep4的增强版本，支持更多运算符和错误处理。
- [x] [ep10](ep10)--ep6的替代实现，采用不同的解析策略。
- [x] [ep11](ep11)--基于AST遍历的算术表达式解释器。
- [x] [ep12](ep12)--在ep11基础上增加变量声明和赋值语句支持。
- [x] [ep13](ep13)--ep11的简化实现版本，优化了解释器结构。
- [x] [ep14](ep14)--实现符号表数据结构，用于记录变量信息。
- [x] [ep15](ep15)--实现变量作用域分析和处理机制。
- [x] [ep16](ep16)--实现变量类型检查和解析，支持函数作用域和函数调用。
- [x] [ep17](ep17)--实现函数静态依赖分析（非调用图生成）。
- [x] [ep18](ep18)--基于栈的虚拟机实现，包含基础指令集设计。
- [x] [ep19](ep19)--实现简单结构体(record)类型和文件作用域管理。
- [x] [ep20](ep20)--中间表示(IR)和字节码生成器，目标为ep18的虚拟机。
- [x] [ep21](ep21)--实现三地址码(TAC)生成、静态单赋值(SSA)形式和控制流图(CFG)分析。

--------------------
## 4. 虚拟机设计 (基于ep18模块)

### 4.1 指令集设计
- 算术运算指令: iconst, iadd, imul
- 控制流指令: call, ret, halt
- 内存访问指令: load

### 4.2 字节码格式
```
.def 函数名: args=参数数量, locals=局部变量数量
指令序列
```

### 4.3 执行流程
1. 取指令
2. 解码指令
3. 验证指令合法性
4. 执行指令
5. 更新程序计数器
6. 检查运行时状态

### 4.4 核心组件
- 指令分派器 (Instruction Dispatcher)
- 操作数栈 (Operand Stack)
- 程序计数器 (Program Counter)
- 运行时状态寄存器 (Runtime Status Register)
- 异常处理器 (Exception Handler)

### 4.5 内存管理
- 代码区 (存储字节码)
- 数据区 (全局变量)
- 栈区 (函数调用栈)

--------------------
### 番外
❤️👀: 终于写完了，感觉好忐忑。不过，我还有另外几个也是编译原理相关的坑也要填。

首先，我得感谢父母，他们给了我莫大支持。

其次，感谢我自己和我的妻子，如果不是我们的相遇我永远也不能写完。

最后，感谢这个时代，我需要的一切都在这个时候刚刚好到来。

--------------------



## 2. 为什么会有这个系列的教程？

我从开始编程就一直在想，如果我是一个意图规划者，
而不是人肉编码器该有多好。

因此，我一头扎进了编程语言构造和分析的汪洋大海中去捞
那根我心中的定海神针。

现在，我真正走进了编译后端处理和程序分析后，我感到自己
有太多想记录的知识、技能、想法，以及创造好用的新工具和新过程。

所以，这就是我的起点，希望你能和我一样享受这个过程。

## 3. 工程体系介绍
整个工程基于Maven构建，需要以下环境支持：
- a. `JDK18+` 要求 (推荐使用OpenJDK 18或更高版本)
- b. `Maven 3.8+` 构建工具
- c. `Antlr4` 运行时支持 (已通过Maven依赖管理)

### 3.1 构建流程
1. 克隆项目后，在根目录执行:
```bash
mvn clean install
```
2. 构建特定模块(以ep20为例):
```bash
cd ep20
mvn clean package
```

### 3.2 模块化配置
项目采用Maven多模块结构，每个ep*目录都是一个独立模块，包含:
- `src/main/java` - 主代码
- `src/test/java` - 测试代码
- `pom.xml` - 模块配置

### 3.3 依赖管理
所有依赖通过Maven管理，主要依赖包括:
- Antlr4运行时
- Log4j日志
- Apache Commons工具库
### 3.1. 目录如下所述:

- `src`: the folder to maintain sources
    * `org/teachfx/antlr4` -- top package name.
        * `ep${num}` -- `num` in `{1,2,3,...,25}`
        * current `num` is `20`
- `lib`: the folder to maintain dependencies
### 3.2. 从哪儿开始？
当所有依赖都安装完毕后，以ep20为例

```Bash
cd your_project_dir

# 使用run.sh脚本
./scripts/run.sh <命令> <模块名> [额外参数]

# 示例:
./scripts/run.sh compile ep1    # 编译ep1模块
./scripts/run.sh run ep2       # 运行ep2模块
./scripts/run.sh test ep3      # 运行ep3模块的测试
./scripts/run.sh clean ep4     # 清理ep4模块
./scripts/run.sh run ep5 "参数1 参数2" # 运行ep5模块并传递参数
./scripts/run.sh run ep20 "src/main/resources/t.cymbol" # 运行ep20模块并指定输入文件
./scripts/run.sh run ep21 "src/main/resources/t.cymbol" # 运行ep21模块并指定输入文件

# 查看帮助
./scripts/run.sh help
```

## 4. 参考或者模仿材料来源
### 4.1 如何解释一个程序
- [计算机程序的构造和解释(SICP)](https://www.zhihu.com/topic/19620884/hot)
- [动手做解释器](http://www.craftinginterpreters.com/)
### 4.2 如何实现一个计算器
- [如何实现一个编程语言](http://lisperator.net/pltut/)
- [编程语言的实现模式](https://www.zhihu.com/topic/20116185/hot)
- [Antlr4权威指南](https://www.antlr.org/)
- [自顶向下算符优先分析(TDOP)](https://github.com/douglascrockford/TDOP)
- [编译原理(龙术:smile:)](https://www.zhihu.com/question/21549783/answer/22749476)
