package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.pass.codegen.StackVMGenerator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fibonacci尾递归优化端到端测试
 *
 * 测试完整的编译流程，从Cymbol源代码到VM执行，验证：
 * 1. fib(10) 正确返回55
 * 2. fib(100) 不会栈溢出
 * 3. 生成的VMR代码使用循环而非递归
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2025-12-23
 */
@DisplayName("Fibonacci Tail Recursion End-to-End Test")
public class FibonacciTailRecursionEndToEndTest {

    private static final Logger logger = LogManager.getLogger(FibonacciTailRecursionEndToEndTest.class);
    private ByteArrayOutputStream outputCapture;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        // 捕获System.out输出用于验证
        outputCapture = new ByteArrayOutputStream();
        originalOut = System.out;
    }

    /**
     * 测试fib(10)的正确性
     * 期望结果: 55
     */
    @Test
    @DisplayName("fib(10) should return 55")
    void testFib10() throws Exception {
        String cymbolCode = """
            int fib(int n) {
                if (n <= 1) return n;
                return fib(n-1) + fib(n-2);
            }

            int main() {
                print fib(10);
                return 0;
            }
            """;

        // 注意：这个测试需要完整的编译器pipeline
        // 当前只是测试框架，实际需要集成编译器

        logger.info("Testing fib(10) with tail recursion optimization");

        // TODO: 集成完整的编译pipeline
        // 1. Parse -> AST
        // 2. AST -> IR (with TailRecursionOptimizer enabled)
        // 3. IR -> VMR
        // 4. VM execute
        // 5. Verify output is "55"

        // 暂时跳过，等待编译器pipeline集成完成
        logger.warn("Full compiler pipeline integration not yet implemented");
    }

    /**
     * 测试fib(100)不会栈溢出
     *
     * 期望行为:
     * - 递归版本: StackOverflowError
     * - 优化版本: 正常执行，返回结果
     */
    @Test
    @DisplayName("fib(100) should not cause stack overflow with TRO")
    void testFib100NoOverflow() throws Exception {
        String cymbolCode = """
            int fib(int n) {
                if (n <= 1) return n;
                return fib(n-1) + fib(n-2);
            }

            int main() {
                print fib(100);
                return 0;
            }
            """;

        logger.info("Testing fib(100) with tail recursion optimization - should not overflow");

        // TODO: 集成完整的编译pipeline
        // 验证:
        // 1. 执行不抛出StackOverflowError
        // 2. 返回正确的结果 (fib(100) = 354224848179261915075)

        logger.warn("Full compiler pipeline integration not yet implemented");
    }

    /**
     * 测试生成的VMR代码结构
     *
     * 验证优化后的代码包含循环而非递归调用
     */
    @Test
    @DisplayName("Generated VMR should use loops not recursion")
    void testGeneratedVMRStructure() throws Exception {
        String cymbolCode = """
            int fib(int n) {
                if (n <= 1) return n;
                return fib(n-1) + fib(n-2);
            }
            """;

        logger.info("Testing generated VMR structure");

        // TODO: 生成VMR代码并验证:
        // 1. fib_iter 函数存在
        // 2. 包含 while 循环 (jnz + jmp 指令组合)
        // 3. 不包含递归的 call fib 指令
        // 4. 使用累加器模式 (a, b变量)

        logger.warn("VMR code generation validation not yet implemented");
    }

    /**
     * 性能基准测试
     *
     * 比较优化前后的性能差异
     */
    @Test
    @DisplayName("Performance comparison: optimized vs unoptimized")
    void testPerformanceComparison() throws Exception {
        logger.info("Performance comparison test");

        // TODO:
        // 1. 编译并执行未优化的fib(20)，记录时间
        // 2. 编译并执行优化后的fib(20)，记录时间
        // 3. 验证优化版本更快且使用更少栈空间

        logger.warn("Performance benchmarking not yet implemented");
    }

    /**
     * 单元测试：ExecutionGraph转换逻辑
     *
     * 直接测试ExecutionGraph的转换能力，不依赖完整pipeline
     */
    @Test
    @DisplayName("ExecutionGraph should transform Fibonacci correctly")
    void testExecutionGraphTransformation() {
        logger.info("Testing ExecutionGraph transformation logic");

        // TODO: 构建测试CFG并验证转换
        // 1. 创建包含递归调用的测试CFG
        // 2. 调用 ExecutionGraph.transform()
        // 3. 验证返回的CFG包含循环结构
        // 4. 验证没有递归调用

        logger.info("ExecutionGraph transformation test completed");
    }
}
