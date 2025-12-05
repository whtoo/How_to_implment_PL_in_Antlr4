# 实验1：ANTLR4基础语法

## 实验基本信息

- **实验编号**：Lab1
- **实验名称**：ANTLR4基础语法
- **所属模块**：ep1, ep2
- **实验学时**：2学时
- **实验类型**：□验证性 ☑设计性 □综合性
- **适用对象**：计算机专业本科二年级以上

## 实验目标

### 知识目标
1. 理解ANTLR4的基本概念和工作原理
2. 掌握ANTLR4语法文件(.g4)的基本结构
3. 了解词法规则和语法规则的区别

### 能力目标
1. 能够编写简单的ANTLR4语法文件
2. 能够使用ANTLR4生成词法分析器和语法分析器
3. 能够编写简单的Visitor遍历语法树

### 素质目标
1. 培养形式化描述语言的能力
2. 提高问题分析和解决能力
3. 增强工程实践能力

## 实验环境

### 软件环境
- **JDK版本**：JDK 21
- **开发工具**：IntelliJ IDEA
- **构建工具**：Apache Maven 3.6+
- **ANTLR4版本**：4.13.2

### 实验材料
1. ep1模块代码：Hello.g4语法文件
2. ep2模块代码：ArrayInit.g4语法文件
3. 实验文档和测试用例

## 实验原理

### ANTLR4简介
ANTLR4(ANother Tool for Language Recognition)是一个强大的语法分析器生成器，可以用于读取、处理、执行或翻译结构化文本或二进制文件。

### 语法文件结构
```
grammar Name;                  // 语法名称
options { ... }               // 选项
import ... ;                  // 导入其他语法
tokens { ... }                // 令牌定义
@header { ... }              // 头部代码
@members { ... }             // 成员代码

// 词法规则（大写开头）
LEXER_RULE: 'pattern' -> action;

// 语法规则（小写开头）
parserRule: subRule1 | subRule2;
```

### 实验涉及的关键概念
1. **词法规则**：定义如何将字符流转换为令牌流
2. **语法规则**：定义如何将令牌流组织成语法树
3. **Visitor模式**：用于遍历和操作语法树

## 实验内容

### 任务一：Hello World语法分析器
**任务描述**：理解并运行ep1模块中的Hello语法分析器

**实现步骤**：
1. 打开ep1模块的Hello.g4文件
2. 分析语法规则结构
3. 运行HelloMain程序
4. 观察输出结果

**关键代码分析**：
```antlr4
// Hello.g4语法文件
grammar Hello;               // 语法名称
r  : 'hello' ID ;           // 匹配hello后跟一个标识符
ID : [a-z]+ ;               // 匹配小写字母标识符
WS : [ \t\r\n]+ -> skip ;   // 跳过空白字符
```

**预期结果**：
```
$ echo "hello world" | java HelloMain
```

### 任务二：数组初始化语法分析器
**任务描述**：理解并运行ep2模块中的ArrayInit语法分析器

**实现步骤**：
1. 打开ep2模块的ArrayInit.g4文件
2. 分析语法规则结构
3. 运行ArrayInitMain程序
4. 观察输出结果

**关键代码分析**：
```antlr4
// ArrayInit.g4语法文件
grammar ArrayInit;
init : '{' value (',' value)* '}' ;  // 数组初始化语法
value : init                        // 可以是嵌套的初始化
      | INT                         // 或者整数
      ;
INT : [0-9]+ ;                     // 匹配整数
WS : [ \t\r\n]+ -> skip ;          // 跳过空白字符
```

**预期结果**：
```
$ echo "{1, 2, 3}" | java ArrayInitMain
```

### 任务三：扩展数组初始化语法
**任务描述**：扩展ArrayInit语法，支持字符串元素

**实现步骤**：
1. 修改ArrayInit.g4文件
2. 添加STRING词法规则
3. 修改value语法规则
4. 更新Visitor实现
5. 运行测试验证

**修改后的语法**：
```antlr4
grammar ArrayInit;
init : '{' value (',' value)* '}' ;
value : init
      | INT
      | STRING    // 新增：支持字符串
      ;
INT : [0-9]+ ;
STRING : '"' .*? '"' ;  // 新增：字符串规则
WS : [ \t\r\n]+ -> skip ;
```

**测试用例**：
```
{1, "hello", 3, "world"}
```

## 实验步骤

### 步骤1：环境准备
```bash
# 验证Java环境
java -version

# 验证Maven环境
mvn -version

# 克隆项目（如果尚未克隆）
git clone https://github.com/your-repo/How_to_implement_PL_in_Antlr4.git
cd How_to_implement_PL_in_Antlr4
```

### 步骤2：项目导入和构建
1. 使用IntelliJ IDEA打开项目
2. 等待Maven依赖下载完成
3. 构建项目：`mvn clean compile`
4. 运行测试：`mvn test`

### 步骤3：运行示例程序
```bash
# 进入ep1模块
cd ep1

# 编译并运行Hello示例
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.HelloMain"

# 测试输入
echo "hello world" | mvn exec:java -Dexec.mainClass="org.teachfx.antlr4.HelloMain"
```

### 步骤4：实现扩展功能
1. 修改ep2/src/main/antlr4/ArrayInit.g4
2. 更新Visitor实现
3. 编写测试用例
4. 运行测试验证

### 步骤5：实验验证
```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=ArrayInitTest
```

## 测试用例

### 单元测试示例
```java
@Test
public void testArrayInitWithStrings() {
    String input = "{1, \"hello\", 3, \"world\"}";
    ArrayInitLexer lexer = new ArrayInitLexer(CharStreams.fromString(input));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    ArrayInitParser parser = new ArrayInitParser(tokens);

    ParseTree tree = parser.init();
    ArrayInitVisitorImpl visitor = new ArrayInitVisitorImpl();
    String result = visitor.visit(tree);

    assertEquals("预期结果", result);
}
```

### 集成测试
```java
@Test
public void testMultipleArrayInits() {
    String[] inputs = {
        "{1, 2, 3}",
        "{\"a\", \"b\", \"c\"}",
        "{1, \"mixed\", 3}"
    };

    for (String input : inputs) {
        testSingleArrayInit(input);
    }
}
```

## 常见问题与解决方法

### 问题1：ANTLR4插件未安装
**现象**：.g4文件没有语法高亮
**原因**：IntelliJ IDEA未安装ANTLR4插件
**解决方法**：
1. 打开File → Settings → Plugins
2. 搜索"ANTLR4"并安装
3. 重启IDEA

### 问题2：Maven依赖下载失败
**现象**：pom.xml文件有错误
**原因**：网络问题或仓库配置问题
**解决方法**：
1. 检查网络连接
2. 使用阿里云镜像：
```xml
<mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
</mirror>
```

### 问题3：语法文件修改后未生效
**现象**：修改.g4文件后，生成的Java代码未更新
**原因**：需要重新生成解析器代码
**解决方法**：
1. 删除target/generated-sources目录
2. 运行`mvn generate-sources`
3. 或使用IDE的"Generate ANTLR Recognizer"功能

## 实验报告要求

### 报告内容
1. **实验目的**：简述实验目标
2. **实验环境**：列出使用的软硬件环境
3. **实验过程**：详细记录实验步骤
4. **实验结果**：展示运行结果和测试通过情况
5. **问题分析**：分析遇到的问题和解决方法
6. **实验总结**：总结收获和体会

### 提交材料
1. **实验报告**：PDF格式
2. **修改的代码**：ArrayInit.g4和相关Java文件
3. **测试用例**：新增的测试代码

## 评分标准

### 代码实现（50分）
- **语法正确性**：20分（语法文件正确无误）
- **功能完整性**：15分（完成所有任务要求）
- **代码规范性**：15分（代码规范、注释清晰）

### 实验报告（30分）
- **内容完整性**：10分（内容完整、结构清晰）
- **分析深度**：10分（分析深入、见解独到）
- **规范性**：10分（格式规范、语言流畅）

### 实验过程（20分）
- **完成度**：10分（按时完成所有任务）
- **问题解决**：10分（独立解决问题能力）

## 扩展任务（可选）

### 扩展任务一：支持布尔值
**任务描述**：扩展ArrayInit语法，支持布尔值(true/false)
**难度**：★☆☆☆☆
**建议时间**：30分钟

### 扩展任务二：支持嵌套注释
**任务描述**：扩展语法支持/* */格式的注释
**难度**：★★☆☆☆
**建议时间**：45分钟

### 扩展任务三：错误恢复机制
**任务描述**：实现基本的错误恢复和报告机制
**难度**：★★★☆☆
**建议时间**：60分钟

## 参考资料

### 必读资料
1. ANTLR4官方文档：https://github.com/antlr/antlr4
2. 《ANTLR4权威指南》第1-3章
3. ep1, ep2模块的README文档

### 在线资源
1. ANTLR4在线教程：https://tomassetti.me/antlr-mega-tutorial/
2. 语法示例：https://github.com/antlr/grammars-v4
3. 视频教程：B站搜索"ANTLR4入门"

## 实验注意事项

### 学术诚信
1. 独立完成实验任务
2. 可以讨论思路，但不能直接复制代码
3. 引用参考资料需注明出处

### 时间管理
1. 建议提前阅读实验材料
2. 合理安排实验时间
3. 预留调试和测试时间

### 代码管理
1. 使用Git进行版本控制
2. 提交有意义的commit message
3. 定期备份代码

## 教师指导建议

### 课前准备
1. 确保实验环境可用
2. 准备常见问题解答
3. 预演实验步骤

### 课中指导重点
1. ANTLR4语法文件结构
2. 词法规则和语法规则的区别
3. Visitor模式的使用

### 课后评估要点
1. 语法设计的合理性
2. 代码实现的正确性
3. 实验报告的完整性

---

**实验设计**：编译器课程教学团队
**版本**：1.0
**日期**：2025年12月5日