# 从解释计算的视角：如何亲手创造一门编程语言Cyson

**每一个watch和star都是这场梦幻之旅的⛽️与干柴**
[![Security Status](https://www.murphysec.com/platform3/v31/badge/1718907022023983104.svg)](https://www.murphysec.com/console/report/1718907021914931200/1718907022023983104)
**兴❤️如🔥，其势如风.意许如油，汩汩不息。**

## 1. 全局鸟瞰
- [x] [ep1](ep1)--antlr支持的EBNF语法描述hello world示例。
- [x] [ep2](ep2)--如何使用g4描述形如`{1,2,{3,4..}...}`的数组，并在listener中print它。
- [x] [ep3](ep3)--实现一个只包含加减乘除的微型计算器。
- [x] [ep4](ep4)--实现一个可以进行简单交互的算术计算器。
- [x] [ep5](ep5)--实现一个Java接口提取工具。
- [x] [ep6](ep6)--实现一个CVS提取器。
- [x] [ep7](ep7)--实现一个JSON解析工具。
- [x] [ep8](ep8)--抽象语法书提取器
- [x] [ep9](ep9)--ep4增强版本
- [x] [ep10](ep10)--ep6的另一种实现
- [x] [ep11](ep11)--基于ep8的算术解释器（AST Tree walking)
- [x] [ep12](ep12)--ep11增加赋值语句和变量声明。
- [x] [ep13](ep13)--ep11另一种简化实现。
- [x] [ep14](ep14)--实现符号表记录。
- [x] [ep15](ep15)--实现变量的作用域确定。
- [x] [ep16](ep16)--实现变量的消解和类型检查，并实现函数作用域与有函数调用的脚本求值。
- [x] [ep17](ep17)--实现函数的静态声明依赖（并不是我心里想的调用图生成，但是还是加上吧，让其他人避坑。）
- [x] [ep18](ep18)--采用栈解释器，目前很简陋。增加VM指令，更新[VM设计文档](src%2Forg%2Fteachfx%2Fantlr4%2Fep18%2FVM_Design.md)
- [x] [ep19](ep19)--实现简单的struct(~~实现闭包~~)，目前实现了最简单的record类型和main函数以及file作用域~~下一章增加类方法和协议支持~~。
- [x] [ep20](ep20)--重点放在IR和字节码生成，生成的字节码目标机就是我们[ep18](ep18)实现的VM。这么做的原因是这个过程足够简单、精确地表现编译后端中最重要的一步是如何执行的。
- [x] [ep21](ep21)--实现了TAC生成、SSA与CFG分析。

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
