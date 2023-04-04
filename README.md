# 如何用antlr实现你自己的第一门~~简单~~编程语言(Cyson:dog:)

**🙏谢谢每一个给我一点关注的朋友，你们是我更新的动力**
## 1. 全局鸟瞰
- [x] [ep1](src/org/teachfx/antlr4/ep1)--antlr支持的EBNF语法描述hello world示例。
- [x] [ep2](src/org/teachfx/antlr4/ep2)--如何使用g4描述形如`{1,2,{3,4..}...}`的数组，并在listener中print它。
- [x] [ep3](src/org/teachfx/antlr4/ep3)--实现一个只包含加减乘除的微型计算器。
- [x] [ep4](src/org/teachfx/antlr4/ep4)--实现一个可以进行简单交互的算术计算器。
- [x] [ep5](src/org/teachfx/antlr4/ep5)--实现一个Java接口提取工具。
- [x] [ep6](src/org/teachfx/antlr4/ep6)--实现一个CVS提取器。
- [x] [ep7](src/org/teachfx/antlr4/ep7)--实现一个JSON解析工具。
- [x] [ep8](src/org/teachfx/antlr4/ep8)--抽象语法书提取器
- [x] [ep9](src/org/teachfx/antlr4/ep9)--ep4增强版本
- [x] [ep10](src/org/teachfx/antlr4/ep10)--ep6的另一种实现
- [x] [ep11](src/org/teachfx/antlr4/ep11)--基于ep8的算术解释器（AST Tree walking)
- [x] [ep12](src/org/teachfx/antlr4/ep12)--ep11增加赋值语句和变量声明。
- [x] [ep13](src/org/teachfx/antlr4/ep13)--ep11另一种简化实现。
- [x] [ep14](src/org/teachfx/antlr4/ep14)--实现符号表记录。
- [x] [ep15](src/org/teachfx/antlr4/ep15)--实现变量的作用域确定。
- [x] [ep16](src/org/teachfx/antlr4/ep16)--实现变量的消解和类型检查，并实现函数作用域与有函数调用的脚本求值。
- [x] [ep17](src/org/teachfx/antlr4/ep17)--实现函数的静态声明依赖（并不是我心里想的调用图生成，但是还是加上吧，让其他人避坑。）
- [x] [ep18](src/org/teachfx/antlr4/ep18)--(~~应该会综合《两周实现脚本语言》与《编程语言的实现模式》中对VM的设计~~)**已经采用栈解释器了，主要就是因为实现简单**😆

- [x] [ep19](src/org/teachfx/antlr4/ep19)--实现简单的struct(~~实现闭包~~)，目前实现了最简单的record类型和main函数以及file作用域，下一章增加类方法和协议支持。
- [ ] [ep20](src/org/teachfx/antlr4/ep20)--这里会增加java的native导入、类方法、协议支持、字节码生成（可能是自己实现的VM不是JVM）。
- [ ] [ep21](src/org/teachfx/antlr4/ep21)--希望能够在这一章终结。
- [ ] [ep22](src/org/teachfx/antlr4/ep22)--如果，我还能坚持写到这里，就增加汇编输出以及编译优化。应该只有SSA以及peep hole，寄存器染色也可能会出现。

PS: 终于写完了，感觉好忐忑。不过，我还有另外几个也是编译原理相关的坑也要填。

首先，我得感谢父母，他们给了我莫大支持。

其次，我得谢谢github，不然我这辈子很难自己手写构造CPS转换器并理解CPS转换的本质。SICP诚不我欺。

再次，我得说我终于从心结里面出来了，希望以后我能做自己喜欢的工作。

最后，打个广告，我是一个有着9年经验的iOS架构师（虽然是只设计过一个network的lib😳）以及同等时长的js开发者，以及一个有着1年半载技术管理经验的技术经理（真的是加起来这么长😭）。
我现在想从事大前端（包括iOS、前端在内的整个生态）工具链开发--包括但不限于静态分析工具、打包工具、代码生成。

我对编译器前端应用非常熟悉且喜爱，而且自认为对工具开发小有心得😄。希望能够在自己喜欢领域的有点贡献🎉。

## 2. 为什么会有这个系列的教程？

我一开始是看龙书以及《两周实现脚本语言》、《自制编译器》、《编程语言的实现模式》、《现代编译原理--C描述》
、《编译器设计基础》、《自制编程语言--基于C语言》等等。

结果，消耗了我1年时间，才从前端的AST艰难地移动到了IR。

好不容易终于走到了我想要的静态分析部分，一看龙书，我心碎了。

于是，我就整理了这份教程（板凳集）。希望能够给后来人提供一个简单、平滑地入门流程，尤其是不要在前端消耗如此多的时间，

虽然我学会了除渐进式解析以外的所有解析技术（`TDOP`真的有点烧脑），但真心觉得如果你能通过这份记录更快入手，

进入程序实现的静态分析和优化阶段，那就是最好的意义了。

## 3. 工程体系介绍
整个工程需要3种外部环境支持。
- a. `JDK18+` is required. (JDK环境需要>= 18,我本地是openJDK 18)
- b. `Antlr4` runtime support.(lib已经有了，并且我在ant构建文件中已经写好了。)
- c. `Ant` support. (Mac:brew install ant,其他平台：[Baidu一下Ant安装](https://www.baidu.com/s?wd=ant%E5%AE%89%E8%A3%85&rsv_spt=1&rsv_iqid=0x92a5c3ca00098ab3&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&rsv_dl=tb&oq=ant&rsv_btype=t&inputT=1837&rsv_t=ec4cvoU9XIugnSk4yfAeGzHEthu95IAGc%2BcxFt188XBik9tpLDQyKTb2S3Y4301WBs3T&rsv_pq=ea06018e001299b9&rsv_sug3=50&rsv_sug1=21&rsv_sug7=100&rsv_sug2=0&rsv_sug4=2109))。
### 3.1. 目录如下所述:

- `src`: the folder to maintain sources
    * `org/teachfx/antlr4` -- top package name.
        * `ep${num}` -- `num` in `{1,2,3,...,25}`
        * current `num` is `20`
- `lib`: the folder to maintain dependencies
### 3.2. 从哪儿开始？
当所有依赖都安装完毕后，以ep16为例

> `cd your_project_dir`

> `cd src/org/teachfx/antlr4/ep16`

> `ant gen`

> `ant run`
### 3.3. 如何从Ant构建并运行工程
这部分就是Ant的一般使用，我之前是参考《Ant使用指南》--一本很老的书。
大家可以百度一下，教程很多我就不浪费篇幅了。
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
