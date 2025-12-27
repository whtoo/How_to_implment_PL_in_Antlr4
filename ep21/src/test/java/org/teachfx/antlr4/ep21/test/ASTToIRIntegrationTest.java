package org.teachfx.antlr4.ep21.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.CymbolLexer;
import org.teachfx.antlr4.ep21.CymbolParser;
import org.teachfx.antlr4.ep21.ast.CompileUnit;
import org.teachfx.antlr4.ep21.ast.stmt.*;
import org.teachfx.antlr4.ep21.ir.Prog;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep21.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep21.pass.cfg.LinearIRBlock;
import org.teachfx.antlr4.ep21.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep21.pass.symtab.LocalDefine;
import org.teachfx.antlr4.ep21.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AST到IR转换集成测试套件 (TASK-2.1.4)
 *
 * 测试完整的AST→IR转换流程，验证各种语句和表达式组合的正确转换：
 * - 函数定义与调用
 * - 控制流语句（if、while）
 * - 表达式计算
 * - 变量声明与赋值
 * - 边界情况处理
 *
 * @author EP21 Team
 * @version 1.0
 * @since 2025-12-26
 */
@DisplayName("AST到IR转换集成测试套件 (TASK-2.1.4)")
class ASTToIRIntegrationTest {

    private GlobalScope globalScope;
    private BuiltInTypeSymbol intType;
    private BuiltInTypeSymbol voidType;

    @BeforeEach
    void setUp() {
        globalScope = new GlobalScope();
        intType = new BuiltInTypeSymbol("int");
        voidType = new BuiltInTypeSymbol("void");
    }

    /**
     * 辅助方法：解析源代码并生成IR
     */
    private Prog compileToIR(String sourceCode) {
        CymbolLexer lexer = new CymbolLexer(CharStreams.fromString(sourceCode));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CymbolParser parser = new CymbolParser(tokens);

        ParseTree parseTree = parser.file();

        // 构建AST
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        var astRoot = parseTree.accept(astBuilder);

        // 构建符号表
        astRoot.accept(new LocalDefine());

        // 转换为IR
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);

        return irBuilder.prog;
    }

    /**
     * 简单函数测试
     */
    @Nested
    @DisplayName("简单函数定义测试")
    class SimpleFunctionTests {

        @Test
        @DisplayName("应该正确转换无参函数")
        void testNoParameterFunction() {
            // Given: 简单的无参函数
            String source = """
                void main() {
                    return;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该生成一个基本块
            assertNotNull(prog);
            assertEquals(1, prog.blockList.size());

            LinearIRBlock block = prog.blockList.get(0);
            assertTrue(block.getStmts().get(0) instanceof FuncEntryLabel);
            assertTrue(block.getStmts().get(block.getStmts().size() - 1) instanceof ReturnVal);
        }

        @Test
        @DisplayName("应该正确转换带参数函数")
        void testParameterFunction() {
            // Given: 带参数的函数
            String source = """
                int add(int a, int b) {
                    return a + b;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: FuncEntryLabel应该记录参数数量
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);
            FuncEntryLabel entryLabel = (FuncEntryLabel) block.getStmts().get(0);
            assertTrue(entryLabel.getRawLabel().contains("args=2"));
        }
    }

    /**
     * 表达式转换测试
     */
    @Nested
    @DisplayName("表达式转换测试")
    class ExpressionConversionTests {

        @Test
        @DisplayName("应该正确转换二元表达式")
        void testBinaryExpression() {
            // Given: 包含二元表达式的函数
            String source = """
                int test() {
                    int x = 10 + 20;
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含BinExpr指令
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            boolean hasBinExpr = block.getStmts().stream()
                .anyMatch(stmt -> stmt instanceof BinExpr);
            assertTrue(hasBinExpr, "应该包含BinExpr指令");
        }

        @Test
        @DisplayName("应该正确转换一元表达式")
        void testUnaryExpression() {
            // Given: 包含一元表达式的函数
            String source = """
                int test() {
                    int x = -10;
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含UnaryExpr指令
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            boolean hasUnaryExpr = block.getStmts().stream()
                .anyMatch(stmt -> stmt instanceof UnaryExpr);
            assertTrue(hasUnaryExpr, "应该包含UnaryExpr指令");
        }

        @Test
        @DisplayName("应该正确转换函数调用表达式")
        void testFunctionCallExpression() {
            // Given: 包含函数调用的代码
            String source = """
                int helper() {
                    return 42;
                }
                int main() {
                    return helper();
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含CallFunc指令
            assertNotNull(prog);
            boolean hasCallFunc = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof CallFunc);
            assertTrue(hasCallFunc, "应该包含CallFunc指令");
        }

        @Test
        @DisplayName("应该正确转换复杂嵌套表达式")
        void testComplexNestedExpression() {
            // Given: 包含复杂嵌套表达式的函数
            String source = """
                int test() {
                    int x = (10 + 20) * (30 - 5);
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含多个BinExpr指令
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            long binExprCount = block.getStmts().stream()
                .filter(stmt -> stmt instanceof BinExpr)
                .count();
            assertTrue(binExprCount >= 2, "应该包含至少2个BinExpr指令");
        }
    }

    /**
     * 控制流语句测试
     */
    @Nested
    @DisplayName("控制流语句转换测试")
    class ControlFlowTests {

        @Test
        @DisplayName("应该正确转换if语句（无else）")
        void testIfWithoutElse() {
            // Given: 包含if语句的代码
            String source = """
                int test(int x) {
                    if (x > 0) {
                        return 1;
                    }
                    return 0;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含CJMP指令
            assertNotNull(prog);
            boolean hasCJMP = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof CJMP);
            assertTrue(hasCJMP, "if语句应该产生CJMP指令");
        }

        @Test
        @DisplayName("应该正确转换if-else语句")
        void testIfWithElse() {
            // Given: 包含if-else语句的代码
            String source = """
                int test(int x) {
                    if (x > 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含CJMP指令和多个基本块
            assertNotNull(prog);
            assertTrue(prog.blockList.size() >= 2, "if-else应该产生多个基本块");
        }

        @Test
        @DisplayName("应该正确转换while循环")
        void testWhileLoop() {
            // Given: 包含while循环的代码
            String source = """
                int sum(int n) {
                    int total = 0;
                    while (n > 0) {
                        total = total + n;
                        n = n - 1;
                    }
                    return total;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含CJMP指令和多个基本块（条件块+循环体+出口块）
            assertNotNull(prog);
            boolean hasCJMP = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof CJMP);
            assertTrue(hasCJMP, "while循环应该产生CJMP指令");
            assertTrue(prog.blockList.size() >= 2, "while循环应该产生多个基本块");
        }

        @Test
        @DisplayName("应该正确转换break语句")
        void testBreakStatement() {
            // Given: 包含break的while循环
            String source = """
                int test(int n) {
                    int i = 0;
                    while (i < n) {
                        if (i > 5) {
                            break;
                        }
                        i = i + 1;
                    }
                    return i;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该正确处理break跳转
            assertNotNull(prog);
            // break语句会产生JMP指令
            boolean hasJMP = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof JMP);
            assertTrue(hasJMP, "break语句应该产生JMP指令");
        }

        @Test
        @DisplayName("应该正确转换continue语句")
        void testContinueStatement() {
            // Given: 包含continue的while循环
            String source = """
                int test(int n) {
                    int total = 0;
                    int i = 0;
                    while (i < n) {
                        i = i + 1;
                        if (i % 2 == 0) {
                            continue;
                        }
                        total = total + i;
                    }
                    return total;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该正确处理continue跳转
            assertNotNull(prog);
            boolean hasJMP = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof JMP);
            assertTrue(hasJMP, "continue语句应该产生JMP指令");
        }
    }

    /**
     * 变量声明和赋值测试
     */
    @Nested
    @DisplayName("变量声明和赋值测试")
    class VariableDeclarationTests {

        @Test
        @DisplayName("应该正确转换带初始化的变量声明")
        void testVariableDeclarationWithInitializer() {
            // Given: 包含变量初始化的代码
            String source = """
                int test() {
                    int x = 42;
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含Assign指令
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            boolean hasAssign = block.getStmts().stream()
                .anyMatch(stmt -> stmt instanceof Assign);
            assertTrue(hasAssign, "变量初始化应该产生Assign指令");
        }

        @Test
        @DisplayName("应该正确转换多个变量声明")
        void testMultipleVariableDeclarations() {
            // Given: 包含多个变量声明的代码
            String source = """
                int test() {
                    int x = 10;
                    int y = 20;
                    int z = 30;
                    return x + y + z;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含多个Assign指令
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            long assignCount = block.getStmts().stream()
                .filter(stmt -> stmt instanceof Assign)
                .count();
            assertTrue(assignCount >= 3, "应该包含至少3个Assign指令");
        }

        @Test
        @DisplayName("应该正确转换赋值语句")
        void testAssignmentStatement() {
            // Given: 包含赋值语句的代码
            String source = """
                int test() {
                    int x = 10;
                    x = 20;
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含2个Assign指令（声明+赋值）
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);

            long assignCount = block.getStmts().stream()
                .filter(stmt -> stmt instanceof Assign)
                .count();
            assertTrue(assignCount >= 2, "应该包含至少2个Assign指令");
        }
    }

    /**
     * 边界情况测试
     */
    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("应该正确转换空函数体")
        void testEmptyFunctionBody() {
            // Given: 空函数体
            String source = """
                void empty() {
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该至少包含FuncEntryLabel和ReturnVal
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);
            assertTrue(block.getStmts().get(0) instanceof FuncEntryLabel);
            assertTrue(block.getStmts().get(block.getStmts().size() - 1) instanceof ReturnVal);
        }

        @Test
        @DisplayName("应该正确转换只有return的函数")
        void testOnlyReturnStatement() {
            // Given: 只有return语句的函数
            String source = """
                int justReturn() {
                    return 42;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含FuncEntryLabel和ReturnVal
            assertNotNull(prog);
            LinearIRBlock block = prog.blockList.get(0);
            assertTrue(block.getStmts().get(0) instanceof FuncEntryLabel);
            assertTrue(block.getStmts().get(block.getStmts().size() - 1) instanceof ReturnVal);
        }

        @Test
        @DisplayName("应该正确转换嵌套控制流")
        void testNestedControlFlow() {
            // Given: 包含嵌套if和while的代码
            String source = """
                int test(int x, int y) {
                    if (x > 0) {
                        while (y > 0) {
                            if (y < 10) {
                                return x + y;
                            }
                            y = y - 1;
                        }
                    }
                    return 0;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该产生多个基本块和跳转指令
            assertNotNull(prog);
            assertTrue(prog.blockList.size() >= 3, "嵌套控制流应该产生多个基本块");

            boolean hasCJMP = prog.blockList.stream()
                .flatMap(block -> block.getStmts().stream())
                .anyMatch(stmt -> stmt instanceof CJMP);
            assertTrue(hasCJMP, "应该包含CJMP指令");
        }

        @Test
        @DisplayName("应该正确转换多个函数定义")
        void testMultipleFunctionDefinitions() {
            // Given: 包含多个函数的代码
            String source = """
                int func1() {
                    return 1;
                }
                int func2() {
                    return 2;
                }
                int main() {
                    return func1() + func2();
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 应该包含3个函数的基本块
            assertNotNull(prog);
            assertEquals(3, prog.blockList.size(), "应该有3个函数的基本块");
        }
    }

    /**
     * IR正确性验证测试
     */
    @Nested
    @DisplayName("IR正确性验证测试")
    class IRCorrectnessTests {

        @Test
        @DisplayName("每个函数都应该以FuncEntryLabel开头")
        void testEveryFunctionStartsWithEntryLabel() {
            // Given: 包含多个函数的代码
            String source = """
                int func1() { return 1; }
                int func2() { return 2; }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 每个基本块都应该以FuncEntryLabel开头
            assertNotNull(prog);
            prog.blockList.forEach(block -> {
                assertTrue(block.getStmts().get(0) instanceof FuncEntryLabel,
                    "每个函数块都应该以FuncEntryLabel开头");
            });
        }

        @Test
        @DisplayName("每个函数都应该以ReturnVal结束")
        void testEveryFunctionEndsWithReturn() {
            // Given: 包含多个函数的代码
            String source = """
                int func1() { return 1; }
                int func2() { return 2; }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: 每个基本块都应该以ReturnVal结束
            assertNotNull(prog);
            prog.blockList.forEach(block -> {
                assertTrue(block.getStmts().get(block.getStmts().size() - 1) instanceof ReturnVal,
                    "每个函数块都应该以ReturnVal结束");
            });
        }

        @Test
        @DisplayName("FrameSlot应该正确关联变量符号")
        void testFrameSlotVariableSymbolAssociation() {
            // Given: 使用局部变量的函数
            String source = """
                int test() {
                    int x = 42;
                    return x;
                }
                """;

            // When: 编译为IR
            Prog prog = compileToIR(source);

            // Then: Assign指令的LHS应该是FrameSlot
            LinearIRBlock block = prog.blockList.get(0);
            Assign assign = (Assign) block.getStmts().stream()
                .filter(stmt -> stmt instanceof Assign)
                .filter(stmt -> ((Assign) stmt).getLhs() instanceof FrameSlot)
                .findFirst()
                .orElse(null);

            assertNotNull(assign, "应该找到FrameSlot类型的LHS的Assign指令");
            assertTrue(assign.getLhs() instanceof FrameSlot,
                "变量赋值的LHS应该是FrameSlot");
        }
    }
}
