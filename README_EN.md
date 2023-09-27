# How to implement your own first ~~simple~~ programming language with antlr (Cyson:dog:)

**🙏 Thank you to everyone who gave me a little bit of attention, you guys are my motivation for updating **
## 1. global bird's eye view
- [x] [ep1](src/org/teachfx/antlr4/ep1) - Example of antlr supported EBNF syntax describing hello world.
- [x] [ep2](src/org/teachfx/antlr4/ep2) - How to use g4 to describe examples of syntax like `{1,2,{3,4...} ...} ` array and print it in the listener.
- [x] [ep3] (src/org/teachfx/antlr4/ep3) - Implements a mini-calculator containing only addition, subtraction, multiplication and division.
- [x] [ep4] (src/org/teachfx/antlr4/ep4) - implements an arithmetic calculator that allows simple interaction.
- [x] [ep5] (src/org/teachfx/antlr4/ep5) - Implementation of a Java interface extraction tool.
- [x] [ep6] (src/org/teachfx/antlr4/ep6) - implements a CVS extractor.
- [x] [ep7] (src/org/teachfx/antlr4/ep7) - Implementing a JSON parsing tool.
- [x] [ep8] (src/org/teachfx/antlr4/ep8) - abstract grammar book extractor
- [x] [ep9](src/org/teachfx/antlr4/ep9) - ep4 enhancements
- [x] [ep10](src/org/teachfx/antlr4/ep10) - an alternative implementation of ep6
- [x] [ep11](src/org/teachfx/antlr4/ep11) - arithmetic interpreter based on ep8 (AST Tree walking)
- [x] [ep12](src/org/teachfx/antlr4/ep12) - ep11 add assignment statements and variable declarations.
- [x] [ep13](src/org/teachfx/antlr4/ep13) - Another simplified implementation of ep11.
- [x] [ep14](src/org/teachfx/antlr4/ep14) - Implementation of symbol table recording.
- [x] [ep15](src/org/teachfx/antlr4/ep15)-Implementation of scope determination for variables.
- [x] [ep16] (src/org/teachfx/antlr4/ep16) - implements variable disambiguation and type-checking, and implements function scoping with script evaluation with function calls.
- [x] [ep17] (src/org/teachfx/antlr4/ep17) - implement static declaration dependencies for functions (not exactly what I had in mind for call-graph generation, but let's add it anyway so others can avoid the pitfalls.)
- [x] [ep18](src/org/teachfx/antlr4/ep18) - (~~ should synthesize the design of the VM from "Two Weeks to Implement a Scripting Language" and "Implementation Patterns for Programming Languages" ~~) ** already uses a stack interpreter, mostly because of the simplicity of the implementation **😆)

- [x] [ep19](src/org/teachfx/antlr4/ep19)-implementation of simple struct (~~implementation of closures~~), currently implements the simplest record type and the main function and the file scope, next chapter adds class methods and protocol support.
- [ ] [ep20](src/org/teachfx/antlr4/ep20) - java's native import, class methods, protocol support, and bytecode generation will be added here (it may be that one's own implementation of the VM is not a JVM).
- [ ] [ep21](src/org/teachfx/antlr4/ep21) - Hopefully it will end in this chapter.
- [ ] [ep22](src/org/teachfx/antlr4/ep22) - If, and when, I stick around to write this, add assembly output as well as compilation optimizations. There should be only SSA as well as peep holes, and register staining may also appear.

PS: I'm finally done writing, and I feel so apprehensive. However, I have a couple other compilation principle related holes to fill in as well.

First of all, I have to thank my parents, who gave me great support.

Second, I have to thank github, otherwise it would have been very difficult for me to construct a CPS converter and understand the nature of CPS conversion by hand.

Once again, I have to say that I'm finally out of the knot, and hopefully I can do what I like to do in the future.

Lastly, to advertise, I'm an iOS architect with 9 years of experience (albeit a network-only lib 😳) as well as a js developer with the same amount of hours, and a technical manager with a year and a half of experience in technical management (it really does add up to that long 😭).
I'm now looking to work on big front-end (entire ecosystem including iOS, front-end) toolchain development - including but not limited to static analysis tools, packaging tools, code generation.

I'm very familiar with and love compiler front-end apps, and consider myself a little bit of a tool developer 😄. I hope to be able to contribute a bit in my favorite field 🎉.

## 2. Why this tutorial series?

I started out reading the Dragon book as well as Two Weeks to Implement a Scripting Language, Make Your Own Compiler, Implementation Patterns for Programming Languages, Modern Compilation Principles - A Description of C
, Fundamentals of Compiler Design, Homebrew Programming Languages - C Based, and so on.

As a result, it consumed 1 year of my time to move hard from the front end AST to IR.

It was hard to finally get to the static analysis section I wanted, and one look at the Dragon book broke my heart.

So I put together this tutorial (bench set). In the hope that it will provide an easy and smooth entry process for those who come after me, especially if they don't consume so much time on the front-end.

Although I learned all parsing techniques except progressive parsing (`TDOP` is really a bit of a brain burner), I honestly feel that if you can get started faster with this record.

into the static analysis and optimization phases of program implementation, that makes the best sense.

## 3. Introduction to the engineering system
The whole project requires 3 types of external environment support.
- a. `JDK18+` is required. (JDK environment >= 18, my local is openJDK 18)
- b. `Antlr4` runtime support. (The libs are already there and I have written them in the ant build file.)
- c. `Ant` support. (Mac:brew install ant, other platforms: [Baidu for Ant install](https://www.baidu.com/s?wd=ant%E5%AE%89%E8%A3%85&rsv_spt=1&rsv_iqid=0x92a5c3ca00098ab3&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&rsv_dl=tb&oq=ant&rsv_btype=t&inputT=1837&rsv_t=ec4cvoU9XIugnSk4yfAeGzHEthu95IAGc%2BcxFt188XBik9tpLDQyKTb2S3Y4301WBs3T&rsv_pq=ea06018e001299b9&rsv_sug3=50&rsv_sug1=21&rsv_sug7=100&rsv_sug2=0&rsv_sug4=2109)).
### 3.1. the catalog is described as follows.

- `src`: the folder to maintain sources
    * `org/teachfx/antlr4` -- top package name.
        * `ep${num}` -- `num` in `{1,2,3,... ,25}`
        * current `num` is `20`.
- `lib`: the folder to maintain dependencies
### 3.2. Where to start?
Once all the dependencies are installed, take ep16 as an example

``Bash
cd your_project_dir

cd src/org/teachfx/antlr4/ep16

ant gen

ant run
```

### 3.3. How to build and run a project from Ant
This is the general use of Ant, I was referring to the "Ant User's Guide" - a very old book.
There are a lot of tutorials out there, so I won't waste any more space.
## 4. Sources of reference or parody material
## 4.1 How to interpret a program
- [Construction and Interpretation of Computer Programs (SICP)](https://www.zhihu.com/topic/19620884/hot)
- [Hands-On Interpreter](http://www.craftinginterpreters.com/)
### 4.2 How to implement a calculator
- [How to implement a programming language](http://lisperator.net/pltut/)
- [Programming Language Implementation Patterns](https://www.zhihu.com/topic/20116185/hot)
- [The Definitive Guide to Antlr4](https://www.antlr.org/)
- [Top-Down Operator Precedence Analysis (TDOP)](https://github.com/douglascrockford/TDOP)
- [Compilation Principles (Dragon Book:smile:)](https://www.zhihu.com/question/21549783/answer/22749476)

Translated with www.DeepL.com/Translator (free version)