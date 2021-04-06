# 如何用antlr实现你自己的第一门~~简单~~编程语言(Cymbol:dog:)
## 1. 全局鸟瞰
- [x] [ep1](src/org/teachfx/antlr4/ep1)--antlr支持的EBNF语法描述hello world示例。
## 2. 为什么会有这个系列的教程？

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
### 3.3. 如何从Ant构建并运行工程

## 4. 参考或者模仿材料来源
1. [动手做解释器](http://www.craftinginterpreters.com/)
2. [如何实现一个编程语言](http://lisperator.net/pltut/)
3. [编程语言的实现模式](https://www.zhihu.com/topic/20116185/hot)
4. [Antlr4权威指南](https://www.antlr.org/)
5. [计算机程序的构造和解释(SICP)](https://www.zhihu.com/topic/19620884/hot)
6. [自顶向下算符优先分析(TDOP)](https://github.com/douglascrockford/TDOP)
7. [编译原理(龙术:smile:)](https://www.zhihu.com/question/21549783/answer/22749476)