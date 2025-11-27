# JUnit5 配置说明

## 问题诊断

在重构的测试文件中使用了 JUnit5 的参数化测试特性（`@ParameterizedTest`），但需要添加对应的依赖包。

原始错误：
```
org.junit.jupiter.params 引用为红色（无法解析）
```

## 解决方案

### 1. 父 POM 配置（已修复）

在 `/Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/pom.xml` 中添加了：

```xml
<!-- JUnit Jupiter Params for parameterized tests -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>${JUnit.version}</version>
    <scope>test</scope>
</dependency>
```

版本变量：
```xml
<JUnit.version>5.8.2</JUnit.version>
```

### 2. 子模块 POM 补充（已修复）

在 `ep21/pom.xml` 中也进行了补充：

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <scope>test</scope>
</dependency>
```

由于 ep21 pom 继承了父 pom，所以不需要指定版本号。

## 验证步骤

### 1. 重新加载 Maven 项目

在 IDEA 中：
- 右键点击 `pom.xml` 文件
- 选择 "Maven" → "Reload Project"

或者使用终端：
```bash
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep21
mvn clean compile
```

### 2. 验证依赖是否下载

```bash
mvn dependency:tree | grep junit
```

应该看到：
```
[INFO] +- org.junit.jupiter:junit-jupiter-api:jar:5.8.2:test
[INFO] +- org.junit.jupiter:junit-jupiter-engine:jar:5.8.2:test
[INFO] +- org.junit.jupiter:junit-jupiter-params:jar:5.8.2:test  <-- 新增
[INFO] \- org.assertj:assertj-core:jar:3.21.0:test
```

### 3. 运行测试验证

```bash
# 在 ep21 目录下
mvn test
```

## 新特性支持

添加此依赖后，可以使用的 JUnit5 参数化测试特性：

### 1. @ValueSource

```java
@ParameterizedTest
@ValueSource(strings = {"x", "y", "tempVar"})
void test(String input) { }
```

支持的类型：
- `short`, `byte`, `int`, `long`
- `float`, `double`
- `char`, `String`
- `Class`（类类型）

### 2. @EnumSource

```java
@ParameterizedTest
@EnumSource(LIRAssign.RegisterType.class)
void testEnum(LIRAssign.RegisterType type) { }
```

### 3. @MethodSource

```java
@ParameterizedTest
@MethodSource("instructionProvider")
void testMethod(String name, Stmt instruction) { }

static Stream<Arguments> instructionProvider() {
    return Stream.of(
        arguments("JMP", new JMP()),
        arguments("CJMP", new CJMP())
    );
}
```

### 4. @CsvSource

```java
@ParameterizedTest
@CsvSource({
    "true, true, true",
    "true, false, false"
})
void testCsv(boolean a, boolean b, boolean expected) { }
```

### 5. @NullSource & @EmptySource

```java
@ParameterizedTest
@NullSource
@EmptySource
@ValueSource(strings = {" ", "   "})
void testNullAndEmpty(String input) { }
```

### 6. @ArgumentsSource

自定义参数提供者：

```java
@ParameterizedTest
@ArgumentsSource(CustomArgumentsProvider.class)
void testCustom(CustomArg arg) { }

static class CustomArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(arguments(new CustomArg()));
    }
}
```

## 完整 JUnit5 依赖列表

现在 ep21 项目拥有完整的 JUnit5 测试套件依赖：

```xml
<!-- JUnit Jupiter API -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.8.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit Jupiter Engine -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>5.8.2</version>
    <scope>test</scope>
</dependency>

<!-- JUnit Jupiter Params（新增） -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.8.2</version>
    <scope>test</scope>
</dependency>

<!-- AssertJ -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.21.0</version>
    <scope>test</scope>
</dependency>
```

## 常见问题

### Q1: 依赖仍然无法解析？

A: 尝试以下步骤：
1. 删除 `~/.m2/repository/org/junit/jupiter/junit-jupiter-params` 目录
2. 在 IDEA 中重新导入 Maven 项目
3. 运行 `mvn clean install -U` 强制更新

### Q2: 版本兼容性问题？

A: JUnit5 5.8.2 版本完全支持我们的测试特性。如需升级：

```xml
<JUnit.version>5.10.0</JUnit.version>
```

### Q3: 如何检查实际使用的版本？

A: 在 IDEA 的 Maven 工具窗口查看依赖树，或使用命令：

```bash
mvn dependency:tree -Dverbose | grep junit
```

## 性能影响

添加 junit-jupiter-params 依赖：
- 增加约 200KB 的 JAR 文件
- 仅在测试时加载，不影响生产环境
- 不会显著增加构建时间

## 下一步

完成依赖添加后，可以运行重构的测试文件，验证所有参数化测试是否正常工作：

```bash
cd ep21
mvn clean test -Dtest=MIRTest
```

如果遇到任何问题，请检查：
1. Maven 依赖是否正确下载
2. JUnit5 版本是否匹配
3. 测试类是否正确编译

## 总结

通过添加 `junit-jupiter-params` 依赖，现在 EP21 项目的测试套件可以完整使用 JUnit5 的所有特性，特别是参数化测试功能，这将大大提高测试代码的可读性和可维护性。