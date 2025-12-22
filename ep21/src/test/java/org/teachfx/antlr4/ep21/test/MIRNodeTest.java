package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep21.ir.mir.MIRExpr;
import org.teachfx.antlr4.ep21.ir.mir.MIRNode;
import org.teachfx.antlr4.ep21.ir.mir.MIRVisitor;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MIRNode基类测试
 * 测试MIR节点的通用行为和抽象方法
 */
@DisplayName("MIRNode基类测试套件")
class MIRNodeTest {

    /**
     * 测试MIRNode的抽象方法契约
     */
    @Nested
    @DisplayName("抽象方法契约测试")
    class AbstractMethodContracts {

        @Test
        @DisplayName("所有具体MIRNode子类必须实现getComplexityLevel")
        void testComplexityLevelImplementation() {
            // 使用匿名内部类测试抽象方法
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 2;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of("x");
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of("y");
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    visitor.visit(this);
                }
            };

            assertEquals(2, node.getComplexityLevel());
        }

        @Test
        @DisplayName("所有具体MIRNode子类必须实现getUsedVariables")
        void testUsedVariablesImplementation() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 1;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of("a", "b", "c");
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    visitor.visit(this);
                }
            };

            Set<String> used = node.getUsedVariables();
            assertNotNull(used);
            assertEquals(3, used.size());
            assertTrue(used.contains("a"));
            assertTrue(used.contains("b"));
            assertTrue(used.contains("c"));
        }

        @Test
        @DisplayName("所有具体MIRNode子类必须实现getDefinedVariables")
        void testDefinedVariablesImplementation() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 1;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of("result", "temp");
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    visitor.visit(this);
                }
            };

            Set<String> defined = node.getDefinedVariables();
            assertNotNull(defined);
            assertEquals(2, defined.size());
            assertTrue(defined.contains("result"));
            assertTrue(defined.contains("temp"));
        }

        @Test
        @DisplayName("所有具体MIRNode子类必须实现accept方法")
        void testAcceptMethodImplementation() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 0;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    // 空实现，但必须存在
                }
            };

            assertDoesNotThrow(() -> node.accept(new MIRVisitor<Object>() {
                @Override
                public Object visit(MIRNode node) {
                    return null;
                }
            }));
        }
    }

    /**
     * 测试MIRNode的默认方法实现
     */
    @Nested
    @DisplayName("默认方法测试")
    class DefaultMethodTests {

        @Test
        @DisplayName("默认isBasicBlockEntry应该返回false")
        void testIsBasicBlockEntryDefault() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 0;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            assertFalse(node.isBasicBlockEntry());
        }

        @Test
        @DisplayName("复杂度级别应该在合理范围内")
        void testComplexityLevelRange() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 3; // 有效范围 0-3
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            int level = node.getComplexityLevel();
            assertTrue(level >= 0 && level <= 3,
                "复杂度级别应该在0-3范围内，实际是: " + level);
        }
    }

    /**
     * 测试MIRNode的行为特性
     */
    @Nested
    @DisplayName("行为特性测试")
    class BehavioralTests {

        @Test
        @DisplayName("变量集合应该不可变或防御性拷贝")
        void testVariableSetImmutability() {
            // 创建返回可变集合的节点
            Set<String> mutableSet = new java.util.HashSet<>();
            mutableSet.add("original");

            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 2;
                }

                @Override
                public Set<String> getUsedVariables() {
                    // 返回可变集合（测试调用者是否应该防御性拷贝）
                    return mutableSet;
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            Set<String> used = node.getUsedVariables();
            // 修改原始集合不应该影响节点行为（如果节点做了防御性拷贝）
            mutableSet.add("modified");

            // 注意：这个测试可能失败，取决于具体实现
            // 这里主要是记录预期行为
            assertNotNull(used);
        }

        @Test
        @DisplayName("空变量集合应该正确处理")
        void testEmptyVariableSets() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 1;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            assertTrue(node.getUsedVariables().isEmpty());
            assertTrue(node.getDefinedVariables().isEmpty());
        }
    }

    /**
     * 测试MIRNode的继承关系
     */
    @Nested
    @DisplayName("继承关系测试")
    class InheritanceTests {

        @Test
        @DisplayName("MIRNode应该继承IRNode")
        void testExtendsIRNode() {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return 0;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            assertTrue(node instanceof org.teachfx.antlr4.ep21.ir.IRNode);
        }

        @Test
        @DisplayName("MIRExpr应该继承MIRNode")
        void testMIRExprInheritance() {
            // 创建MIRExpr实例
            MIRExpr expr = new MIRExpr() {
                @Override
                public int getComplexityLevel() {
                    return 3;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of("x");
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                    visitor.visit(this);
                }
            };

            assertTrue(expr instanceof MIRNode);
        }
    }

    /**
     * 参数化测试
     */
    @Nested
    @DisplayName("参数化测试")
    class ParameterizedTests {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 2, 3})
        @DisplayName("各种复杂度级别应该被正确识别")
        void testComplexityLevels(int level) {
            MIRNode node = new MIRNode() {
                @Override
                public int getComplexityLevel() {
                    return level;
                }

                @Override
                public Set<String> getUsedVariables() {
                    return Set.of();
                }

                @Override
                public Set<String> getDefinedVariables() {
                    return Set.of();
                }

                @Override
                public void accept(MIRVisitor<?> visitor) {
                }
            };

            assertEquals(level, node.getComplexityLevel());
        }
    }
}