# 如何用antlr实现你自己的第一门~~简单~~编程语言(Cymbol:dog:)
## 1. 全局鸟瞰
- [x] [ep1](src/org/teachfx/antlr4/ep1)--antlr支持的EBNF语法描述hello world示例。
- [x] [ep2](src/org/teachfx/antlr4/ep2)--如何使用g4描述形如`{1,2,{3,4..}...}`的数组，并在listener中print它。
- [x] [ep3](src/org/teachfx/antlr4/ep3)--实现一个只包含加减乘除的微型计算器。
- [x] [ep4](src/org/teachfx/antlr4/ep4)--
- [x] [ep5](src/org/teachfx/antlr4/ep5)--
- [x] [ep6](src/org/teachfx/antlr4/ep6)--
- [x] [ep7](src/org/teachfx/antlr4/ep7)
- [x] [ep8](src/org/teachfx/antlr4/ep8)
- [x] [ep9](src/org/teachfx/antlr4/ep9)
- [x] [ep10](src/org/teachfx/antlr4/ep10)
- [x] [ep11](src/org/teachfx/antlr4/ep11)
- [x] [ep12](src/org/teachfx/antlr4/ep12)
- [x] [ep13](src/org/teachfx/antlr4/ep13)
- [x] [ep14](src/org/teachfx/antlr4/ep14)
- [x] [ep15](src/org/teachfx/antlr4/ep15)
- [x] [ep16](src/org/teachfx/antlr4/ep16)
- [x] [ep17](src/org/teachfx/antlr4/ep17)

## 2. 为什么会有这个系列的教程？
我一开始是看龙书以及《两周实现脚本语言》、《自制编译器》、《编程语言的实现模式》、《现代编译原理--C描述》
、《编译器设计基础》、《自制编程语言--基于C语言》等等。结果，消耗了我1年时间，才从前端的AST艰难地移动到了IR。
好不容易终于走到了我想要的静态分析部分，一看龙书，我心碎了。于是，我就整理了这份教程（幼稚板凳集）。希望能够
给后来人提供一个简单、平滑地入门流程，尤其是不要在前端消耗如此多的时间，虽然我学会了除渐进式解析以外的所有解析
技术（TDOP真的有点烧脑），但真心觉得如果你能通过这份记录更快入手，进入程序实现的静态分析和优化阶段，那就是最好
的意义了。
## 3. 工程体系介绍
整个工程需要3种外部环境支持。
- a. `JDK8+` is required. (JDK环境需要>= 8,我本地是openJDK 14)
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

## 4. 参考或者模仿材料来源
1. [动手做解释器](http://www.craftinginterpreters.com/)
2. [如何实现一个编程语言](http://lisperator.net/pltut/)
3. [编程语言的实现模式](https://www.zhihu.com/topic/20116185/hot)
4. [Antlr4权威指南](https://www.antlr.org/)
5. [计算机程序的构造和解释(SICP)](https://www.zhihu.com/topic/19620884/hot)
6. [自顶向下算符优先分析(TDOP)](https://github.com/douglascrockford/TDOP)
7. [编译原理(龙术:smile:)](https://www.zhihu.com/question/21549783/answer/22749476)