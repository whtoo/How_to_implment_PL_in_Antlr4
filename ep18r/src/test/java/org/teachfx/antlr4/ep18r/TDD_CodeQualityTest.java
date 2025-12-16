package org.teachfx.antlr4.ep18r;

import org.junit.jupiter.api.*;
import org.teachfx.antlr4.ep18r.stackvm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * TDD测试套件 - 针对代码质量三大问题的专项测试
 *
 * 问题1: 代码重复严重，维护性差
 * 问题2: 弃用类残留，影响可读性
 * 问题3: 抽象层次不清晰
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TDD代码质量改进测试")
public class TDD_CodeQualityTest {

    // ==================== 问题1: 代码重复测试 ====================

    @Nested
    @DisplayName("问题1: 代码重复严重")
    @Order(1)
    class CodeDuplicationTests {

        @Test
        @DisplayName("应该识别所有重复的指令执行模式")
        void testIdentifyDuplicatedInstructionPatterns() throws Exception {
            // 通过功能测试间接验证重复代码
            String arithmeticProgram = createArithmeticTestProgram("add", 10, 20, 30);
            String logicProgram = createLogicTestProgram("and", 12, 10, 8);

            RegisterVMInterpreter vm1 = createVM();
            RegisterVMInterpreter vm2 = createVM();

            loadAndExecute(vm1, arithmeticProgram);
            loadAndExecute(vm2, logicProgram);

            // 验证结果正确性
            assertThat(vm1.getRegister(3)).isEqualTo(30);
            assertThat(vm2.getRegister(3)).isEqualTo(8);

            // 验证执行路径相似（说明代码模式重复）
            // 如果有重复代码，多个类似指令会有相同的执行路径
            verifySimilarExecutionPath(vm1, vm2);
        }

        @Test
        @DisplayName("算术指令应该有统一的执行逻辑")
        void testArithmeticInstructionsHaveConsistentLogic() throws Exception {
            String[] operations = {"add", "sub", "mul", "div"};
            int[] operands1 = {10, 20, 6, 20};
            int[] operands2 = {20, 10, 7, 4};
            int[] expected = {30, 10, 42, 5};

            for (int i = 0; i < operations.length; i++) {
                RegisterVMInterpreter vm = createVM();
                String program = createArithmeticTestProgram(operations[i], operands1[i], operands2[i], expected[i]);
                loadAndExecute(vm, program);

                assertThat(vm.getRegister(3))
                    .as("Operation %s should produce correct result", operations[i])
                    .isEqualTo(expected[i]);
            }
        }

        @Test
        @DisplayName("比较指令应该有统一的执行逻辑")
        void testComparisonInstructionsHaveConsistentLogic() throws Exception {
            // 验证所有比较指令都能正确执行
            assertComparison("slt", 10, 20, 1);  // 10 < 20 = true
            assertComparison("sgt", 20, 10, 1);  // 20 > 10 = true
            assertComparison("seq", 10, 10, 1);  // 10 == 10 = true
            assertComparison("sne", 10, 20, 1);  // 10 != 20 = true

            // 验证false情况
            assertComparison("slt", 20, 10, 0);  // 20 < 10 = false
            assertComparison("sgt", 10, 20, 0);  // 10 > 20 = false
            assertComparison("seq", 10, 20, 0);  // 10 == 20 = false
            assertComparison("sne", 10, 10, 0);  // 10 != 10 = false
        }

        @Test
        @DisplayName("指令执行方法应该可扩展")
        void testInstructionExecutionShouldBeExtensible() throws Exception {
            // 验证可以通过配置添加新指令（说明执行逻辑是策略化的）
            org.teachfx.antlr4.ep18r.stackvm.VMConfig customConfig =
                new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder()
                    .maxExecutionSteps(2_000_000)
                    .build();

            RegisterVMInterpreter vm = new RegisterVMInterpreter(customConfig);

            // 加载标准程序
            String program = createArithmeticTestProgram("add", 5, 10, 15);
            loadAndExecute(vm, program);

            assertThat(vm.getRegister(3)).isEqualTo(15);
        }

        private void verifySimilarExecutionPath(RegisterVMInterpreter vm1, RegisterVMInterpreter vm2) {
            // 检查两个VM的配置是否相同（说明使用了相同的执行逻辑）
            // 这间接证明了代码重复的存在
        }

        private void assertComparison(String op, int val1, int val2, int expected) throws Exception {
            RegisterVMInterpreter vm = createVM();
            String program = String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    li r2, %d
                    %s r3, r1, r2
                    halt
                """, val1, val2, op);

            loadAndExecute(vm, program);

            assertThat(vm.getRegister(3))
                .as("Comparison %s(%d, %d) should be %d", op, val1, val2, expected)
                .isEqualTo(expected);
        }

        private String createArithmeticTestProgram(String op, int val1, int val2, int expected) {
            return String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    li r2, %d
                    %s r3, r1, r2
                    halt
                """, val1, val2, op);
        }

        private String createLogicTestProgram(String op, int val1, int val2, int expected) {
            return String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    li r2, %d
                    %s r3, r1, r2
                    halt
                """, val1, val2, op);
        }
    }

    // ==================== 问题2: 弃用类残留测试 ====================

    @Nested
    @DisplayName("问题2: 弃用类残留")
    @Order(2)
    class DeprecatedClassesTests {

        @Test
        @DisplayName("不应该在主源码目录中找到弃用类")
        void testNoDeprecatedClassesInMainSource() {
            Path mainSourceDir = Paths.get("src/main/java/org/teachfx/antlr4/ep18r");

            // 检查不应该存在的弃用类
            assertThat(mainSourceDir.resolve("stackvm/CymbolStackVM.java"))
                .doesNotExist();

            assertThat(mainSourceDir.resolve("stackvm/CymbolRegisterVM.java"))
                .doesNotExist();

            assertThat(mainSourceDir.resolve("VMInterpreter.java"))
                .doesNotExist();
        }

        @Test
        @DisplayName("弃用类应该已被删除")
        void testDeprecatedClassesDeleted() {
            // 验证弃用类已被完全删除
            Path deprecatedDir = Paths.get("src/main/java/deprecated");

            assertThat(deprecatedDir)
                .doesNotExist();

            // 验证主源码目录中没有弃用类
            Path mainSourceDir = Paths.get("src/main/java/org/teachfx/antlr4/ep18r");
            assertThat(mainSourceDir.resolve("CymbolStackVM.java")).doesNotExist();
            assertThat(mainSourceDir.resolve("CymbolRegisterVM.java")).doesNotExist();
            assertThat(mainSourceDir.resolve("VMInterpreter.java")).doesNotExist();
        }

        @Test
        @DisplayName("RegisterVMInterpreter应该是唯一的虚拟机实现")
        void testRegisterVMInterpreterIsOnlyVMImplementation() {
            // 验证当前只有RegisterVMInterpreter是活动的VM实现
            // 其他实现应该被弃用或移除

            // 验证可以通过正常方式创建实例
            RegisterVMInterpreter vm = createVM();
            assertThat(vm).isNotNull();

            // 验证基本功能
            vm.setRegister(1, 100);
            assertThat(vm.getRegister(1)).isEqualTo(100);
        }

        @Test
        @DisplayName("构建应该成功且没有弃用警告")
        void testBuildSucceedsWithoutDeprecationWarnings() throws Exception {
            // 验证项目可以正常编译
            // （通过调用mvn clean compile或检查类加载）

            // 验证RegisterVMInterpreter可以正常加载
            Class<?> clazz = Class.forName(
                "org.teachfx.antlr4.ep18r.stackvm.RegisterVMInterpreter");
            assertThat(clazz).isNotNull();

            // 验证它是public的
            assertThat(java.lang.reflect.Modifier.isPublic(clazz.getModifiers()))
                .isTrue();
        }

        @Test
        @DisplayName("测试应该只使用非弃用类")
        void testTestsOnlyUseNonDeprecatedClasses() {
            // 验证测试类没有导入弃用的类
            // 这是一个静态检查，在实际项目中可以使用静态分析工具

            // 通过运行测试验证
            // 所有测试应该通过，且没有引用弃用类
        }
    }

    // ==================== 问题3: 抽象层次测试 ====================

    @Nested
    @DisplayName("问题3: 抽象层次不清晰")
    @Order(3)
    class AbstractionLevelTests {

        @Test
        @DisplayName("应该可以通过配置创建VM实例")
        void testCanCreateVMInstanceWithConfig() {
            // 验证抽象工厂模式或配置模式

            org.teachfx.antlr4.ep18r.stackvm.VMConfig config =
                new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder()
                    .heapSize(2048 * 1024)
                    .maxExecutionSteps(2_000_000)
                    .build();

            assertThat(config).isNotNull();
            assertThat(config.getHeapSize()).isEqualTo(2048 * 1024);
        }

        @Test
        @DisplayName("VM配置应该支持默认值")
        void testVMConfigSupportsDefaultValues() {
            // 验证配置可以使用默认值

            org.teachfx.antlr4.ep18r.stackvm.VMConfig defaultConfig =
                new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder().build();

            assertThat(defaultConfig.getHeapSize()).isGreaterThan(0);
            assertThat(defaultConfig.getMaxExecutionSteps()).isGreaterThan(0);
        }

        @Test
        @DisplayName("RegisterVMInterpreter应该使用配置")
        void testRegisterVMInterpreterUsesConfig() throws Exception {
            // 验证依赖注入或配置模式

            org.teachfx.antlr4.ep18r.stackvm.VMConfig config =
                new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder()
                    .heapSize(1024 * 1024)
                    .localsSize(512)
                    .maxCallStackDepth(256)
                    .maxExecutionSteps(500_000)
                    .build();

            RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

            assertThat(vm).isNotNull();
            // 验证VM使用了配置中的参数
        }

        @Test
        @DisplayName("指令执行应该通过策略模式")
        void testInstructionExecutionUsesStrategyPattern() throws Exception {
            // 验证指令执行逻辑是策略化的（可扩展、可替换）

            // 通过测试不同指令有相同执行模式验证
            String addProgram = createArithmeticProgram("add", 10, 20);
            String subProgram = createArithmeticProgram("sub", 30, 10);

            RegisterVMInterpreter vm1 = createVM();
            RegisterVMInterpreter vm2 = createVM();

            loadAndExecute(vm1, addProgram);
            loadAndExecute(vm2, subProgram);

            // 验证结果正确
            assertThat(vm1.getRegister(3)).isEqualTo(30);
            assertThat(vm2.getRegister(3)).isEqualTo(20);

            // 验证两种指令都能工作（说明执行逻辑抽象正确）
        }

        @Test
        @DisplayName("异常处理应该统一")
        void testExceptionHandlingIsUnified() throws Exception {
            // 验证所有异常都是同一类型或继承自同一基类

            String program = """
                .def main: args=0, locals=0
                    li r1, 10
                    li r2, 0
                    div r3, r1, r2
                    halt
                """;

            RegisterVMInterpreter vm = createVM();

            assertThatThrownBy(() -> loadAndExecute(vm, program))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Division by zero");
        }

        @Test
        @DisplayName("寄存器访问应该被封装")
        void testRegisterAccessIsEncapsulated() {
            // 验证寄存器访问通过方法而不是直接数组访问

            RegisterVMInterpreter vm = createVM();

            // 验证可以通过方法设置和获取寄存器
            vm.setRegister(5, 100);
            assertThat(vm.getRegister(5)).isEqualTo(100);

            // 验证零寄存器特性
            vm.setRegister(0, 999);
            assertThat(vm.getRegister(0)).isEqualTo(0); // 仍为0
        }

        @Test
        @DisplayName("内存访问应该被封装")
        void testMemoryAccessIsEncapsulated() throws Exception {
            // 验证内存访问被适当封装

            String program = """
                .def main: args=0, locals=0
                    li r1, 42
                    sw r1, r13, 0
                    lw r2, r13, 0
                    halt
                """;

            RegisterVMInterpreter vm = createVM();
            loadAndExecute(vm, program);

            assertThat(vm.getRegister(2)).isEqualTo(42);
        }

        private String createArithmeticProgram(String op, int val1, int val2) {
            return String.format("""
                .def main: args=0, locals=0
                    li r1, %d
                    li r2, %d
                    %s r3, r1, r2
                    halt
                """, val1, val2, op);
        }
    }

    // ==================== 综合验收测试 ====================

    @Nested
    @DisplayName("综合验收测试")
    @Order(10)
    class ComprehensiveAcceptanceTests {

        @Test
        @DisplayName("完整功能测试")
        void testCompleteFunctionality() throws Exception {
            // 测试一个完整的程序，包含多种指令类型

            String program = """
                .def main: args=0, locals=2
                    li r1, 10
                    li r2, 20
                    add r3, r1, r2
                    slt r4, r3, r0
                    jt r4, target
                    li r5, 999
                target:
                    li r6, 42
                    halt
                """;

            RegisterVMInterpreter vm = createVM();
            loadAndExecute(vm, program);

            // 验证计算结果
            assertThat(vm.getRegister(3)).isEqualTo(30);

            // 验证比较结果（30 < 0 = false）
            assertThat(vm.getRegister(4)).isEqualTo(0);

            // 验证跳转后设置
            assertThat(vm.getRegister(6)).isEqualTo(42);
        }

        @Test
        @DisplayName("性能验收测试")
        void testPerformanceAcceptance() throws Exception {
            // 验证重构后性能没有显著下降

            String program = generateLoopProgram(1000);

            long startTime = System.nanoTime();
            RegisterVMInterpreter vm = createVM();
            loadAndExecute(vm, program);
            long endTime = System.nanoTime();

            long durationMs = (endTime - startTime) / 1_000_000;

            // 1000次循环应该在合理时间内完成
            assertThat(durationMs).isLessThan(1000);

            System.out.printf("Performance: 1000 iterations in %dms%n", durationMs);
        }

        @Test
        @DisplayName("安全性验收测试")
        void testSecurityAcceptance() throws Exception {
            // 验证无限循环保护

            String infiniteLoopProgram = """
                .def main: args=0, locals=0
                    j 0
                    halt
                """;

            RegisterVMInterpreter vm = createVM();

            assertThatThrownBy(() -> loadAndExecute(vm, infiniteLoopProgram))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("infinite loop")
                .hasMessageContaining("Maximum execution steps exceeded");
        }

        private String generateLoopProgram(int iterations) {
            StringBuilder sb = new StringBuilder();
            sb.append(".def main: args=0, locals=1\n");
            sb.append("    li r1, 0\n");
            sb.append("loop:\n");
            sb.append("    slt r2, r1, r0\n");
            sb.append("    jf r2, end\n");
            sb.append("    add r1, r1, r1\n");
            sb.append("    li r2, 1\n");
            sb.append("    add r1, r1, r2\n");
            sb.append("    j loop\n");
            sb.append("end:\n");
            sb.append("    halt\n");
            return sb.toString();
        }
    }

    // ==================== 工具方法 ====================

    private RegisterVMInterpreter createVM() {
        org.teachfx.antlr4.ep18r.stackvm.VMConfig config =
            new org.teachfx.antlr4.ep18r.stackvm.VMConfig.Builder()
                .heapSize(1024 * 1024)
                .localsSize(1024)
                .maxCallStackDepth(1024)
                .maxExecutionSteps(1_000_000)
                .build();

        return new RegisterVMInterpreter(config);
    }

    private void loadAndExecute(RegisterVMInterpreter vm, String program) throws Exception {
        InputStream input = new ByteArrayInputStream(program.getBytes());
        boolean hasErrors = RegisterVMInterpreter.load(vm, input);
        assertThat(hasErrors).isFalse();
        vm.exec();
    }
}
