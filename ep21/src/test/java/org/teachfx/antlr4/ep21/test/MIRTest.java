package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.lir.*;
import org.teachfx.antlr4.ep21.ir.mir.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * MIR/LIR体系的单元测试
 * 测试MIR和LIR核心类的基本功能
 */
@DisplayName("MIR/LIR体系测试套件")
@Tag("ir")
class MIRTest {

    private MIRFunction testFunction;
    private MIRExpr mockExpr;

    @BeforeEach
    void setUp() {
        testFunction = new MIRFunction("testFunc");
        mockExpr = createMockMIRExpr(3, Set.of("x"), Set.of());
    }

    /**
     * MIRFunction测试
     */
    @Nested
    @DisplayName("MIRFunction功能测试")
    class MIRFunctionTests {
        
        @Test
        @DisplayName("应该正确设置和获取函数名称")
        void testMIRFunctionName() {
            assertEquals("testFunc", testFunction.getName());
        }
        
        @Test
        @DisplayName("函数复杂度级别应该为0（最高级抽象）")
        void testMIRFunctionComplexityLevel() {
            assertEquals(0, testFunction.getComplexityLevel());
        }
        
        @Test
        @DisplayName("函数应该能接受访问者模式调用")
        void testMIRFunctionAcceptVisitor() {
            assertDoesNotThrow(() -> testFunction.accept(visitor -> null));
        }
        
        @Test
        @DisplayName("函数应该能够添加参数")
        void testAddParameter() {
            testFunction.addParameter("param1");
            testFunction.addParameter("param2");
            
            Set<String> usedVars = testFunction.getUsedVariables();
            assertTrue(usedVars.contains("param1"));
            assertTrue(usedVars.contains("param2"));
        }
        
        @Test
        @DisplayName("函数应该能够添加局部变量")
        void testAddLocalVariable() {
            testFunction.addLocalVariable("local1");
            testFunction.addLocalVariable("local2");
            
            Set<String> definedVars = testFunction.getDefinedVariables();
            assertTrue(definedVars.contains("local1"));
            assertTrue(definedVars.contains("local2"));
        }
        
        @Test
        @DisplayName("新创建的函数应该有空的语句列表")
        void testInitialStatementsEmpty() {
            assertNotNull(testFunction.getStatements());
            assertTrue(testFunction.getStatements().isEmpty());
        }
        
        @Test
        @DisplayName("函数应该能够添加语句")
        void testAddStatement() {
            MIRStmt mockStmt = new MIRStmt() {
                @Override
                public int getComplexityLevel() { return 1; }
                @Override
                public Set<String> getUsedVariables() { return Set.of("x"); }
                @Override
                public Set<String> getDefinedVariables() { return Set.of("y"); }
                @Override
                public void accept(MIRVisitor<?> visitor) {}
            };
            
            testFunction.addStatement(mockStmt);
            assertEquals(1, testFunction.getStatements().size());
        }
    }

    /**
     * MIRAssignStmt测试
     */
    @Nested
    @DisplayName("MIRAssignStmt赋值语句测试")
    class MIRAssignStmtTests {
        
        private MIRAssignStmt assignStmt;
        
        @BeforeEach
        void setUpAssign() {
            assignStmt = new MIRAssignStmt("result", mockExpr);
        }
        
        @Test
        @DisplayName("应该正确设置目标变量")
        void testTarget() {
            assertEquals("result", assignStmt.getTarget());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"x", "y", "tempVar", "_underscore", "camelCase"})
        @DisplayName("应该支持各种合法的变量名")
        void testVariousTargetNames(String targetName) {
            MIRAssignStmt assign = new MIRAssignStmt(targetName, mockExpr);
            assertEquals(targetName, assign.getTarget());
        }
        
        @Test
        @DisplayName("应该正确返回使用的变量集合")
        void testUsedVariables() {
            Set<String> usedVars = assignStmt.getUsedVariables();
            assertEquals(Set.of("x"), usedVars);
        }
        
        @Test
        @DisplayName("应该正确定义目标变量")
        void testDefinedVariables() {
            Set<String> definedVars = assignStmt.getDefinedVariables();
            assertEquals(1, definedVars.size());
            assertTrue(definedVars.contains("result"));
        }
        
        @Test
        @DisplayName("赋值语句复杂度级别应该为2")
        void testComplexityLevel() {
            assertEquals(2, assignStmt.getComplexityLevel());
        }
        
        @Test
        @DisplayName("应该支持访问者模式")
        void testAcceptVisitor() {
            assertDoesNotThrow(() -> assignStmt.accept(visitor -> null));
        }
        
        @Test
        @DisplayName("处理null表达式应该抛出异常")
        void testNullExpression() {
            assertThrows(NullPointerException.class, () -> 
                new MIRAssignStmt("result", null));
        }
        
        @Test
        @DisplayName("处理空目标变量名应该抛出异常")
        void testEmptyTargetName() {
            assertThrows(IllegalArgumentException.class, () -> 
                new MIRAssignStmt("", mockExpr));
        }
    }
    
    /**
     * MIRAssignStmt测试
     */
    @Test
    void testMIRAssignStmtTarget() {
        // Arrange
        MIRExpr mockExpr = createMockMIRExpr(3, Set.of("x"), Set.of());
        MIRAssignStmt assign = new MIRAssignStmt("result", mockExpr);
        
        // Act & Assert
        assertEquals("result", assign.getTarget(), 
                     "目标变量应该正确设置");
    }
    
    @Test
    void testMIRAssignStmtUsedVariables() {
        // Arrange
        Set<String> usedVars = new HashSet<>();
        usedVars.add("x");
        MIRExpr mockExpr = createMockMIRExpr(3, usedVars, Set.of());
        MIRAssignStmt assign = new MIRAssignStmt("result", mockExpr);
        
        // Act & Assert
        assertEquals(usedVars, assign.getUsedVariables(), 
                     "使用的变量应该正确返回");
    }
    
    @Test
    void testMIRAssignStmtDefinedVariables() {
        // Arrange
        MIRExpr mockExpr = createMockMIRExpr(3, Set.of("x"), Set.of());
        MIRAssignStmt assign = new MIRAssignStmt("result", mockExpr);
        
        // Act
        Set<String> defined = assign.getDefinedVariables();
        
        // Assert
        assertEquals(1, defined.size(), "应该定义一个变量");
        assertTrue(defined.contains("result"), "应该定义目标变量");
    }
    
    @Test
    void testMIRAssignStmtComplexityLevel() {
        // Arrange
        MIRExpr mockExpr = createMockMIRExpr(3, Set.of("x"), Set.of());
        MIRAssignStmt assign = new MIRAssignStmt("result", mockExpr);
        
        // Act & Assert
        assertEquals(2, assign.getComplexityLevel(), 
                     "赋值语句复杂度级别应该是2");
    }
    
    @Test
    void testMIRAssignStmtAcceptVisitor() {
        // Arrange
        MIRExpr mockExpr = createMockMIRExpr(3, Set.of("x"), Set.of());
        MIRAssignStmt assign = new MIRAssignStmt("result", mockExpr);
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            assign.accept(visitor -> null);
        }, "访问者模式调用应该不抛出异常");
    }
    
    /**
     * LIRAssign测试
     */
    @Nested
    @DisplayName("LIRAssign低层指令测试")
    class LIRAssignTests {
        
        private Operand mockTarget;
        private Operand mockSource;
        
        @BeforeEach
        void setUpOperands() {
            mockTarget = createMockOperand("t0");
            mockSource = createMockOperand("1");
        }
        
        @Test
        @DisplayName("指令类型应该是DATA_TRANSFER")
        void testInstructionType() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.REGISTER);
            assertEquals(LIRNode.InstructionType.DATA_TRANSFER, assign.getInstructionType());
        }
        
        @Test
        @DisplayName("寄存器类型应该有寄存器操作")
        void testRegisterOperation() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.REGISTER);
            assertTrue(assign.hasRegisterOperation());
            assertFalse(assign.hasMemoryAccess());
        }
        
        @Test
        @DisplayName("字符串表示应该正确")
        void testToString() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.REGISTER);
            assertEquals("t0 = 1", assign.toString());
        }
        
        @ParameterizedTest
        @EnumSource(LIRAssign.RegisterType.class)
        @DisplayName("所有寄存器类型都应该有正确的成本评估")
        void testCostForAllTypes(LIRAssign.RegisterType type) {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, type);
            
            int expectedCost = switch (type) {
                case REGISTER -> 1;
                case MEMORY -> 2;
                case IMMEDIATE -> 0;
            };
            
            assertEquals(expectedCost, assign.getCost(), 
                       "类型 " + type + " 的成本应该是 " + expectedCost);
        }
        
        @Test
        @DisplayName("内存类型应该有内存访问标记")
        void testMemoryAccess() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.MEMORY);
            assertTrue(assign.hasMemoryAccess());
            assertFalse(assign.hasRegisterOperation());
        }
        
        @Test
        @DisplayName("立即数类型不应该有寄存器操作")
        void testImmediateHasNoRegisterOp() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.IMMEDIATE);
            assertFalse(assign.hasRegisterOperation());
            assertFalse(assign.hasMemoryAccess());
        }
        
        @Test
        @DisplayName("应该正确获取源和目标操作数")
        void testOperandsAccess() {
            LIRAssign assign = new LIRAssign(mockTarget, mockSource, 
                                           LIRAssign.RegisterType.REGISTER);
            
            assertEquals(mockTarget, assign.getTarget());
            assertEquals(mockSource, assign.getSource());
        }
        
        @Test
        @DisplayName("当处理null操作数时应该抛出NPE")
        void testNullOperands() {
            assertThrows(NullPointerException.class, () -> 
                new LIRAssign(null, mockSource, LIRAssign.RegisterType.REGISTER));
            
            assertThrows(NullPointerException.class, () -> 
                new LIRAssign(mockTarget, null, LIRAssign.RegisterType.REGISTER));
        }
    }
    
    @Nested
    @DisplayName("边界条件和性能测试")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("处理空变量名集合时的行为")
        void testEmptyVariableSet() {
            MIRExpr emptyExpr = createMockMIRExpr(1, Set.of(), Set.of());
            MIRAssignStmt assign = new MIRAssignStmt("x", emptyExpr);
            
            assertTrue(assign.getUsedVariables().isEmpty());
        }
        
        @Test
        @DisplayName("处理大量变量的性能")
        @Timeout(1) // 1秒内完成
        void testManyVariables() {
            Set<String> manyVars = new HashSet<>();
            for (int i = 0; i < 1000; i++) {
                manyVars.add("var" + i);
            }
            
            MIRExpr manyVarsExpr = createMockMIRExpr(3, manyVars, Set.of());
            MIRAssignStmt assign = new MIRAssignStmt("result", manyVarsExpr);
            
            assertEquals(1000, assign.getUsedVariables().size());
        }
        
        @ParameterizedTest
        @ValueSource(ints = {0, 1, 10, Integer.MAX_VALUE})
        @DisplayName("各种复杂度级别都应该被正确处理")
        void testComplexityLevels(int complexity) {
            MIRExpr expr = createMockMIRExpr(complexity, Set.of("x"), Set.of("y"));
            assertEquals(complexity, expr.getComplexityLevel());
        }
        
        @Test
        @DisplayName("嵌套函数应该正确管理变量作用域")
        void testNestedFunctions() {
            MIRFunction func1 = new MIRFunction("outer");
            func1.addLocalVariable("var1");
            
            MIRFunction func2 = new MIRFunction("inner");
            func2.addLocalVariable("var2");
            
            // 确保每个函数独立管理自己的变量
            assertFalse(func1.getDefinedVariables().contains("var2"));
            assertFalse(func2.getDefinedVariables().contains("var1"));
        }
    }
    
    /**
     * 辅助方法：创建模拟MIRExpr
     */
    private MIRExpr createMockMIRExpr(int complexityLevel, Set<String> usedVariables, Set<String> definedVariables) {
        return new MIRExpr() {
            @Override
            public int getComplexityLevel() {
                return complexityLevel;
            }
            
            @Override
            public Set<String> getUsedVariables() {
                return usedVariables;
            }
            
            @Override
            public Set<String> getDefinedVariables() {
                return definedVariables;
            }
            
            @Override
            public void accept(MIRVisitor<?> visitor) {
                visitor.visit(this);
            }
        };
    }
    
    /**
     * 辅助方法：创建模拟Operand
     */
    private Operand createMockOperand(String value) {
        return new Operand() {
            @Override
            public String toString() {
                return value;
            }
            
            @Override
            public <S, E> E accept(org.teachfx.antlr4.ep21.ir.IRVisitor<S, E> visitor) {
                return null;
            }
        };
    }
}