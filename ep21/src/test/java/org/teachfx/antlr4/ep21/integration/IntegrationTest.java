package org.teachfx.antlr4.ep21.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.teachfx.antlr4.ep21.Compiler;

/**
 * 集成测试 - 验证完整的编译器流水线
 *
 * <p>集成测试验证从源代码到虚拟机字节码的完整编译流程。
 * 测试覆盖以下方面：</p>
 *
 * <ul>
 *   <li>词法分析和语法分析</li>
 *   <li>AST构建和符号表构建</li>
 *   <li>IR生成</li>
 *   <li>CFG构建</li>
 *   <li>控制流分析和优化</li>
 *   <li>数据流分析</li>
 *   <li>字节码生成</li>
 * </ul>
 */
@DisplayName("集成测试")
public class IntegrationTest {

    @Test
    @DisplayName("测试简单算术表达式")
    public void testSimpleArithmetic() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/simple_arithmetic.cymbol",
            "target/integration-test/simple_arithmetic.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 简单算术表达式");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试函数定义和调用")
    public void testFunctionDefinition() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/function_definition.cymbol",
            "target/integration-test/function_definition.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 函数定义和调用");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试条件语句（if-else）")
    public void testConditionalStatement() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/conditional.cymbol",
            "target/integration-test/conditional.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 条件语句");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试while循环")
    public void testWhileLoop() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/while_loop.cymbol",
            "target/integration-test/while_loop.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: while循环");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试for循环")
    public void testForLoop() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/for_loop.cymbol",
            "target/integration-test/for_loop.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: for循环");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试变量声明和使用")
    public void testVariableDeclaration() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/variables.cymbol",
            "target/integration-test/variables.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 变量声明和使用");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试复杂表达式")
    public void testComplexExpression() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/complex_expression.cymbol",
            "target/integration-test/complex_expression.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 复杂表达式");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试嵌套循环")
    public void testNestedLoop() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/nested_loop.cymbol",
            "target/integration-test/nested_loop.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 嵌套循环");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("测试数组访问")
    public void testArrayAccess() throws Exception {
        String[] args = new String[]{
            "src/test/resources/integration/array_access.cymbol",
            "target/integration-test/array_access.vm"
        };

        try {
            Compiler.main(args);
            System.out.println("✓ 测试通过: 数组访问");
        } catch (Exception e) {
            System.err.println("✗ 测试失败: " + e.getMessage());
            throw e;
        }
    }
}
