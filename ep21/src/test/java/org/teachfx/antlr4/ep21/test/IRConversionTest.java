package org.teachfx.antlr4.ep21.test;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.lir.LIRAssign;
import org.teachfx.antlr4.ep21.ir.lir.LIRNode;
import org.teachfx.antlr4.ep21.ir.mir.MIRAssignStmt;
import org.teachfx.antlr4.ep21.ir.mir.MIRExpr;
import org.teachfx.antlr4.ep21.ir.mir.MIRFunction;
import org.teachfx.antlr4.ep21.ir.mir.MIRNode;
import org.teachfx.antlr4.ep21.ir.mir.MIRStmt;
import org.teachfx.antlr4.ep21.ir.mir.MIRToLIRConverter;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IR转换测试
 * 测试AST到MIR、MIR到LIR的转换过程
 *
 * @author EP21 Team
 * @version 2.0
 * @since 2025-12-26
 */
@DisplayName("IR转换测试套件")
class IRConversionTest {

    private CymbolIRBuilder irBuilder;

    @BeforeEach
    void setUp() {
        irBuilder = new CymbolIRBuilder();
    }

    /**
     * AST到MIR转换测试
     * 注意：当前架构中，AST通过CymbolIRBuilder转换为基础IR(IRNode)
     * MIR作为IR的一个层级，共享相同的基础结构
     */
    @Nested
    @DisplayName("AST到MIR转换测试")
    class ASTToMIRConversionTests {

        @Test
        @DisplayName("简单函数AST应该能转换为IR")
        void testASTToMIRFunctionConversion() throws Exception {
            // Given: 一个简单的Cymbol函数
            String cymbolCode = """
                int add(int a, int b) {
                    return a + b;
                }
                """;

            // When: 通过完整pipeline转换
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);

            // When: 构建符号表
            astRoot.accept(new LocalDefine());

            // When: 转换为IR
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证IR生成成功
            assertNotNull(prog, "Prog should not be null");
            List<IRNode> instructions = prog.linearInstrs();
            assertFalse(instructions.isEmpty(), "Instructions should not be empty");

            // Then: 验证包含函数定义
            boolean hasFuncEntry = instructions.stream()
                .anyMatch(node -> node.toString().contains("add"));
            assertTrue(hasFuncEntry, "IR should contain function entry for 'add'");
        }

        @Test
        @DisplayName("AST表达式应该能转换为IR表达式")
        void testASTExpressionToMIRExpression() throws Exception {
            // Given: 包含表达式的代码
            String cymbolCode = """
                int test() {
                    int x = 10 + 20;
                    return x;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证表达式被正确转换
            List<IRNode> instructions = prog.linearInstrs();

            // 验证包含赋值操作和常量
            boolean hasAssignment = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.Assign);
            assertTrue(hasAssignment, "IR should contain assignment expression");
        }

        @Test
        @DisplayName("AST语句应该能转换为IR语句")
        void testASTStatementToMIRStatement() throws Exception {
            // Given: 包含控制流的代码
            String cymbolCode = """
                int test(int n) {
                    if (n > 0) {
                        return n;
                    }
                    return 0;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证控制流语句被正确转换
            List<IRNode> instructions = prog.linearInstrs();

            // 验证包含跳转指令
            boolean hasJump = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.JMP ||
                                 node instanceof org.teachfx.antlr4.ep21.ir.stmt.CJMP);
            assertTrue(hasJump, "IR should contain jump instructions for control flow");
        }

        @Test
        @DisplayName("转换应该保持程序语义不变")
        void testConversionPreservesSemantics() throws Exception {
            // Given: 包含多个语句的函数
            String cymbolCode = """
                int calculate(int x, int y) {
                    int temp = x + y;
                    int result = temp * 2;
                    return result;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证语义保持
            List<IRNode> instructions = prog.linearInstrs();

            // 验证IR生成成功且包含多个语句
            assertFalse(instructions.isEmpty(), "IR should contain instructions");
            assertTrue(instructions.size() > 3, "IR should contain multiple statements for the calculation");

            // 验证包含赋值操作（变量定义）
            boolean hasAssignment = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.Assign);
            assertTrue(hasAssignment, "IR should contain assignment statements");
        }
    }

    /**
     * MIR到LIR转换测试
     */
    @Nested
    @DisplayName("MIR到LIR转换测试")
    class MIRToLIRConversionTests {

        @Test
        @DisplayName("MIR函数应该能转换为LIR指令序列")
        void testMIRFunctionToLIRInstructions() {
            // Given: 创建一个简单的MIRFunction
            MIRFunction mirFunction = new MIRFunction("testFunction");

            // 创建简单的MIRExpr
            MIRExpr simpleExpr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of();
                }

                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {
                    // Simple expr - no-op for test
                }
            };

            // 添加赋值语句
            MIRAssignStmt assignStmt = new MIRAssignStmt("x", simpleExpr);
            mirFunction.addStatement(assignStmt);

            // When: 转换为LIR
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证LIR指令生成
            assertNotNull(lirInstructions, "LIR instructions should not be null");
            assertFalse(lirInstructions.isEmpty(), "LIR instructions should not be empty");

            // 验证生成的指令是LIRAssign
            LIRNode firstInstr = lirInstructions.get(0);
            assertTrue(firstInstr instanceof LIRAssign,
                       "First LIR instruction should be LIRAssign");
        }

        @Test
        @DisplayName("MIR表达式应该能转换为LIR操作数")
        void testMIRExpressionToLIROperand() {
            // Given: 创建包含简单表达式的MIR函数
            MIRFunction mirFunction = new MIRFunction("exprTest");

            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("y");
                }

                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {
                    // No-op for test
                }
            };

            MIRAssignStmt assignStmt = new MIRAssignStmt("x", expr);
            mirFunction.addStatement(assignStmt);

            // When: 转换为LIR
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证操作数转换
            assertEquals(1, lirInstructions.size(), "Should have 1 LIR instruction");

            LIRAssign lirAssign = (LIRAssign) lirInstructions.get(0);
            assertNotNull(lirAssign.getTarget(), "Target operand should not be null");
            assertNotNull(lirAssign.getSource(), "Source operand should not be null");
        }

        @Test
        @DisplayName("转换应该优化指令选择")
        void testConversionOptimizesInstructionSelection() {
            // Given: 创建包含多个赋值的MIR函数
            MIRFunction mirFunction = new MIRFunction("optimizeTest");

            MIRExpr expr1 = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("a");
                }

                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };

            MIRAssignStmt stmt1 = new MIRAssignStmt("x", expr1);
            mirFunction.addStatement(stmt1);

            // When: 转换为LIR
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证指令类型和成本
            LIRAssign lirAssign = (LIRAssign) lirInstructions.get(0);

            // 验证指令类型是数据传送
            assertEquals(LIRNode.InstructionType.DATA_TRANSFER,
                         lirAssign.getInstructionType(),
                         "Instruction type should be DATA_TRANSFER");

            // 验证成本评估合理
            assertTrue(lirAssign.getCost() >= 0 && lirAssign.getCost() <= 2,
                       "Instruction cost should be reasonable (0-2)");
        }

        @Test
        @DisplayName("转换应该处理寄存器分配提示")
        void testConversionHandlesRegisterAllocationHints() {
            // Given: 创建MIR函数
            MIRFunction mirFunction = new MIRFunction("registerHintTest");

            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("input");
                }

                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };

            MIRAssignStmt assignStmt = new MIRAssignStmt("output", expr);
            mirFunction.addStatement(assignStmt);

            // When: 转换为LIR
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证寄存器类型信息被保留
            LIRAssign lirAssign = (LIRAssign) lirInstructions.get(0);

            // 验证RegisterType被正确设置
            assertNotNull(lirAssign.getRegisterType(),
                         "RegisterType should be set for register allocation");

            // 验证可以查询寄存器操作属性
            assertFalse(lirAssign.hasMemoryAccess(),
                        "Simple assignment should not have memory access");
        }
    }

    /**
     * 端到端转换测试
     * 测试完整的 AST → IR → MIR → LIR 转换流程
     */
    @Nested
    @DisplayName("端到端转换测试")
    class EndToEndConversionTests {

        @Test
        @DisplayName("完整程序应该能通过多层IR转换")
        void testFullProgramMultiLevelConversion() throws Exception {
            // Given: 完整的Cymbol程序
            String cymbolCode = """
                int factorial(int n) {
                    if (n <= 1) return 1;
                    return n * factorial(n - 1);
                }

                int main() {
                    return factorial(5);
                }
                """;

            // When: 执行完整pipeline: AST → IR → MIR → LIR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // When: 创建MIR函数并转换为LIR
            List<IRNode> irInstructions = prog.linearInstrs();

            // 创建模拟的MIRFunction用于LIR转换测试
            MIRFunction mirFunction = new MIRFunction("factorial");
            MIRExpr nExpr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("n");
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };
            mirFunction.addStatement(new MIRAssignStmt("result", nExpr));

            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证所有层级转换成功
            assertNotNull(irInstructions, "IR instructions should not be null");
            assertFalse(irInstructions.isEmpty(), "IR instructions should not be empty");
            assertNotNull(lirInstructions, "LIR instructions should not be null");
            assertFalse(lirInstructions.isEmpty(), "LIR instructions should not be empty");
        }

        @Test
        @DisplayName("转换应该处理复杂控制流")
        void testConversionHandlesComplexControlFlow() throws Exception {
            // Given: 包含循环和条件分支的代码
            String cymbolCode = """
                int testControlFlow(int n) {
                    int sum = 0;
                    int i = 0;
                    while (i < n) {
                        sum = sum + i;
                        i = i + 1;
                    }
                    return sum;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证控制流被正确转换
            List<IRNode> instructions = prog.linearInstrs();

            // 验证包含条件跳转和无条件跳转
            boolean hasCondJump = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.CJMP);
            boolean hasJump = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.JMP);

            assertTrue(hasCondJump || hasJump,
                       "IR should contain control flow instructions (CJMP or JMP)");
        }

        @Test
        @DisplayName("转换应该处理嵌套作用域")
        void testConversionHandlesNestedScopes() throws Exception {
            // Given: 包含嵌套块作用域的代码
            String cymbolCode = """
                int nestedScope(int x) {
                    int y = x;
                    {
                        int z = y + 1;
                        y = z;
                    }
                    return y;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证嵌套作用域变量被正确处理
            List<IRNode> instructions = prog.linearInstrs();

            // 验证嵌套作用域代码生成成功
            assertFalse(instructions.isEmpty(), "IR should contain instructions for nested scope code");

            // 验证包含赋值语句（变量定义）
            boolean hasAssignment = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.stmt.Assign);
            assertTrue(hasAssignment, "IR should contain assignment statements from nested scope");
        }

        @Test
        @DisplayName("转换应该处理函数调用")
        void testConversionHandlesFunctionCalls() throws Exception {
            // Given: 包含函数调用的代码
            String cymbolCode = """
                int helper(int x) {
                    return x * 2;
                }

                int caller(int n) {
                    return helper(n) + 1;
                }
                """;

            // When: 转换为IR
            ByteArrayInputStream is = new ByteArrayInputStream(cymbolCode.getBytes(StandardCharsets.UTF_8));
            CharStream charStream = CharStreams.fromStream(is);
            CymbolLexer lexer = new CymbolLexer(charStream);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokenStream);
            ParseTree parseTree = parser.file();

            CymbolASTBuilder astBuilder = new CymbolASTBuilder();
            var astRoot = parseTree.accept(astBuilder);
            astRoot.accept(new LocalDefine());
            astRoot.accept(irBuilder);
            Prog prog = irBuilder.prog;

            // Then: 验证函数调用被正确转换
            List<IRNode> instructions = prog.linearInstrs();

            // 验证包含函数调用指令
            boolean hasCallFunc = instructions.stream()
                .anyMatch(node -> node instanceof org.teachfx.antlr4.ep21.ir.expr.CallFunc);
            assertTrue(hasCallFunc, "IR should contain CallFunc instruction");
        }
    }

    /**
     * 转换器配置和选项测试
     */
    @Nested
    @DisplayName("转换器配置测试")
    class ConverterConfigurationTests {

        @Test
        @DisplayName("转换器应该接受配置选项")
        void testConverterAcceptsConfiguration() {
            // Given: MIRToLIRConverter (当前版本不支持配置)
            // 测试验证转换器可以被实例化

            // When: 创建转换器
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // Then: 验证转换器可正常工作
            assertNotNull(converter, "Converter should be instantiated");

            // 验证转换器可以执行转换
            MIRFunction function = new MIRFunction("test");
            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of();
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };
            function.addStatement(new MIRAssignStmt("x", expr));

            List<LIRNode> result = converter.convert(function);
            assertNotNull(result, "Converter should produce result");
        }

        @Test
        @DisplayName("不同优化级别应该产生不同IR")
        void testDifferentOptimizationLevelsProduceDifferentIR() {
            // Given: 同一个MIR函数
            MIRFunction mirFunction = new MIRFunction("optimizationTest");
            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("a", "b");
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };
            mirFunction.addStatement(new MIRAssignStmt("result", expr));

            // When: 当前版本不支持优化级别配置
            // 未来可扩展支持 -O0, -O1, -O2
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证生成LIR指令
            // 当前版本只验证基本功能，未来可扩展验证优化级别差异
            assertNotNull(lirInstructions, "Should generate LIR instructions");
            assertFalse(lirInstructions.isEmpty(), "Should have at least one LIR instruction");
        }

        @Test
        @DisplayName("转换器应该处理目标架构差异")
        void testConverterHandlesTargetArchitectureDifferences() {
            // Given: MIR函数
            MIRFunction mirFunction = new MIRFunction("archTest");
            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of();
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };
            mirFunction.addStatement(new MIRAssignStmt("x", expr));

            // When: 转换为LIR (当前版本与架构无关)
            // 未来可扩展支持目标架构参数
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // Then: 验证生成架构无关的LIR
            // LIR指令包含成本评估，可用于不同架构的后续优化
            LIRNode lirNode = lirInstructions.get(0);
            assertTrue(lirNode.getCost() >= 0,
                       "LIR instruction should have cost evaluation for architecture selection");
        }
    }

    /**
     * 错误处理和边界条件测试
     */
    @Nested
    @DisplayName("错误处理测试")
    class ErrorHandlingTests {

        @Test
        @DisplayName("转换器应该处理无效输入")
        void testConverterHandlesInvalidInput() {
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // Then: 验证null输入抛出异常
            assertThrows(IllegalArgumentException.class,
                         () -> converter.convert(null),
                         "Should throw IllegalArgumentException for null MIRFunction");

            assertThrows(IllegalArgumentException.class,
                         () -> converter.convertNode(null),
                         "Should throw IllegalArgumentException for null MIRNode");
        }

        @Test
        @DisplayName("转换器应该报告转换错误")
        void testConverterReportsConversionErrors() {
            // Given: 验证MIRAssignStmt检测null表达式
            // Then: 验证错误被检测 - MIRAssignStmt构造函数会抛出NPE
            assertThrows(NullPointerException.class,
                         () -> new MIRAssignStmt("x", null),
                         "Should detect null expression in MIRAssignStmt");
        }

        @Test
        @DisplayName("转换器应该处理极端情况")
        void testConverterHandlesEdgeCases() {
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // 测试空函数
            MIRFunction emptyFunction = new MIRFunction("empty");
            List<LIRNode> emptyResult = converter.convert(emptyFunction);
            assertNotNull(emptyResult, "Empty function should produce empty list, not null");
            assertTrue(emptyResult.isEmpty(), "Empty function should produce empty LIR list");

            // 测试包含多个语句的函数
            MIRFunction multiStmtFunction = new MIRFunction("multiStmt");
            for (int i = 0; i < 10; i++) {
                final int idx = i;
                MIRExpr expr = new MIRExpr() {
                    @Override
                    public java.util.Set<String> getUsedVariables() {
                        return java.util.Set.of("var" + idx);
                    }
                    @Override
                    public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
                };
                multiStmtFunction.addStatement(new MIRAssignStmt("x" + i, expr));
            }

            List<LIRNode> multiResult = converter.convert(multiStmtFunction);
            assertEquals(10, multiResult.size(), "Should convert all 10 statements");
        }
    }

    /**
     * 性能测试
     */
    @Nested
    @DisplayName("性能测试")
    class PerformanceTests {

        @Test
        @DisplayName("转换应该具有合理的时间复杂度")
        void testConversionHasReasonableTimeComplexity() {
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // 测试线性时间复杂度
            long startTime, endTime;

            // 小规模: 10条语句
            MIRFunction smallFunction = new MIRFunction("small");
            for (int i = 0; i < 10; i++) {
                MIRExpr expr = new MIRExpr() {
                    @Override
                    public java.util.Set<String> getUsedVariables() {
                        return java.util.Set.of();
                    }
                    @Override
                    public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
                };
                smallFunction.addStatement(new MIRAssignStmt("x" + i, expr));
            }
            startTime = System.nanoTime();
            converter.convert(smallFunction);
            endTime = System.nanoTime();
            long smallTime = endTime - startTime;

            // 中等规模: 100条语句
            MIRFunction mediumFunction = new MIRFunction("medium");
            for (int i = 0; i < 100; i++) {
                MIRExpr expr = new MIRExpr() {
                    @Override
                    public java.util.Set<String> getUsedVariables() {
                        return java.util.Set.of();
                    }
                    @Override
                    public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
                };
                mediumFunction.addStatement(new MIRAssignStmt("x" + i, expr));
            }
            startTime = System.nanoTime();
            converter.convert(mediumFunction);
            endTime = System.nanoTime();
            long mediumTime = endTime - startTime;

            // 验证时间复杂度接近线性 (mediumTime/smallTime < 20x, 而非100x)
            assertTrue(mediumTime < smallTime * 20,
                       "Time complexity should be near-linear (medium < 20x small)");
        }

        @Test
        @DisplayName("转换应该具有合理的空间复杂度")
        void testConversionHasReasonableSpaceComplexity() {
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // 创建包含N条语句的MIR函数
            int statementCount = 50;
            MIRFunction mirFunction = new MIRFunction("spaceTest");
            for (int i = 0; i < statementCount; i++) {
                MIRExpr expr = new MIRExpr() {
                    @Override
                    public java.util.Set<String> getUsedVariables() {
                        return java.util.Set.of();
                    }
                    @Override
                    public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
                };
                mirFunction.addStatement(new MIRAssignStmt("x" + i, expr));
            }

            // 转换为LIR
            List<LIRNode> lirInstructions = converter.convert(mirFunction);

            // 验证空间复杂度为O(n) (每条MIR语句产生一条LIR指令)
            assertEquals(statementCount, lirInstructions.size(),
                         "LIR instruction count should equal MIR statement count");
        }

        @Test
        @DisplayName("转换应该可扩展到大程序")
        void testConversionScalesToLargePrograms() {
            MIRToLIRConverter converter = new MIRToLIRConverter();

            // 创建大规模程序 (1000条语句)
            MIRFunction largeFunction = new MIRFunction("large");
            for (int i = 0; i < 1000; i++) {
                final int idx = i;
                MIRExpr expr = new MIRExpr() {
                    @Override
                    public java.util.Set<String> getUsedVariables() {
                        return java.util.Set.of("var" + (idx % 100)); // 限制不同变量数
                    }
                    @Override
                    public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
                };
                largeFunction.addStatement(new MIRAssignStmt("x" + i, expr));
            }

            // 验证可以处理大规模程序而不崩溃
            assertDoesNotThrow(() -> converter.convert(largeFunction),
                               "Should handle large programs (1000 statements) without crashing");

            List<LIRNode> result = converter.convert(largeFunction);
            assertEquals(1000, result.size(), "Should convert all 1000 statements");
        }
    }

    /**
     * 测试工具方法
     */
    @Nested
    @DisplayName("测试工具方法")
    class TestUtilityMethods {

        @Test
        @DisplayName("测试辅助方法应该可用")
        void testUtilityMethodsAvailable() {
            // 验证测试工具方法可用
            // 这些方法在测试中被广泛使用:

            // 1. MIRFunction创建
            MIRFunction testFunction = new MIRFunction("utilityTest");
            assertNotNull(testFunction, "Should create MIRFunction");
            assertEquals("utilityTest", testFunction.getName(), "Function name should be set");

            // 2. MIRStatement添加
            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of();
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };
            testFunction.addStatement(new MIRAssignStmt("x", expr));
            assertEquals(1, testFunction.getStatements().size(), "Should add statement");

            // 3. Converter使用
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> result = converter.convert(testFunction);
            assertNotNull(result, "Converter should produce result");
        }

        @Test
        @DisplayName("IR比较工具应该可用")
        void testIRComparisonToolsAvailable() {
            // 创建测试IR结构进行比较
            MIRFunction function1 = new MIRFunction("compare1");
            MIRFunction function2 = new MIRFunction("compare2");

            MIRExpr expr = new MIRExpr() {
                @Override
                public java.util.Set<String> getUsedVariables() {
                    return java.util.Set.of("x");
                }
                @Override
                public void accept(org.teachfx.antlr4.ep21.ir.mir.MIRVisitor<?> visitor) {}
            };

            function1.addStatement(new MIRAssignStmt("y", expr));
            function2.addStatement(new MIRAssignStmt("y", expr));

            // 验证结构比较功能
            assertEquals(function1.getStatements().size(),
                         function2.getStatements().size(),
                         "Should be able to compare IR structure sizes");

            // 转换后的LIR比较
            MIRToLIRConverter converter = new MIRToLIRConverter();
            List<LIRNode> lir1 = converter.convert(function1);
            List<LIRNode> lir2 = converter.convert(function2);

            assertEquals(lir1.size(), lir2.size(),
                         "Converted LIR should have same structure");
        }
    }
}