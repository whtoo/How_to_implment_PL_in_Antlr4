package org.teachfx.antlr4.ep21.test;

import org.teachfx.antlr4.ep21.ir.mir.*;
import org.teachfx.antlr4.ep21.ir.lir.*;
import org.teachfx.antlr4.ep21.ir.expr.Operand;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MIR/LIR体系的单元测试
 * 测试MIR和LIR核心类的基本功能
 */
class MIRTest {

    /**
     * MIRFunction测试
     */
    @Test
    void testMIRFunctionName() {
        // Arrange
        MIRFunction func = new MIRFunction("testFunc");
        
        // Act & Assert
        assertEquals("testFunc", func.getName(), 
                     "函数名称应该正确设置");
    }
    
    @Test
    void testMIRFunctionComplexityLevel() {
        // Arrange
        MIRFunction func = new MIRFunction("testFunc");
        
        // Act & Assert
        assertEquals(0, func.getComplexityLevel(), 
                     "函数复杂度级别应该是0（最高级抽象）");
    }
    
    @Test
    void testMIRFunctionAcceptVisitor() {
        // Arrange
        MIRFunction func = new MIRFunction("testFunc");
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            func.accept(visitor -> null);
        }, "访问者模式调用应该不抛出异常");
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
    @Test
    void testLIRAssignInstructionType() {
        // Arrange
        Operand mockTarget = createMockOperand("t0");
        Operand mockSource = createMockOperand("1");
        LIRAssign lirAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
        
        // Act & Assert
        assertEquals(LIRNode.InstructionType.DATA_TRANSFER, lirAssign.getInstructionType(), 
                     "指令类型应该是DATA_TRANSFER");
    }
    
    @Test
    void testLIRAssignRegisterOperation() {
        // Arrange
        Operand mockTarget = createMockOperand("t0");
        Operand mockSource = createMockOperand("1");
        LIRAssign lirAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
        
        // Act & Assert
        assertTrue(lirAssign.hasRegisterOperation(), 
                   "寄存器类型应该有寄存器操作");
    }
    
    @Test
    void testLIRAssignCost() {
        // Arrange
        Operand mockTarget = createMockOperand("t0");
        Operand mockSource = createMockOperand("1");
        LIRAssign lirAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
        
        // Act & Assert
        assertEquals(1, lirAssign.getCost(), 
                     "寄存器类型成本应该是1");
    }
    
    @Test
    void testLIRAssignToString() {
        // Arrange
        Operand mockTarget = createMockOperand("t0");
        Operand mockSource = createMockOperand("1");
        LIRAssign lirAssign = new LIRAssign(mockTarget, mockSource, LIRAssign.RegisterType.REGISTER);
        
        // Act & Assert
        assertEquals("t0 = 1", lirAssign.toString(), 
                     "字符串表示应该正确");
    }
    
    /**
     * 寄存器类型测试
     */
    @Test
    void testRegisterTypeCost() {
        // Arrange
        Operand mockOperand = createMockOperand("test");
        
        // Act & Assert - REGISTER类型
        LIRAssign registerAssign = new LIRAssign(mockOperand, mockOperand, LIRAssign.RegisterType.REGISTER);
        assertEquals(1, registerAssign.getCost(), 
                     "寄存器类型成本应该是1");
        
        // Act & Assert - MEMORY类型
        LIRAssign memoryAssign = new LIRAssign(mockOperand, mockOperand, LIRAssign.RegisterType.MEMORY);
        assertEquals(2, memoryAssign.getCost(), 
                     "内存类型成本应该是2");
        assertTrue(memoryAssign.hasMemoryAccess(), 
                   "内存类型应该有内存访问");
        
        // Act & Assert - IMMEDIATE类型
        LIRAssign immediateAssign = new LIRAssign(mockOperand, mockOperand, LIRAssign.RegisterType.IMMEDIATE);
        assertEquals(0, immediateAssign.getCost(), 
                     "立即数类型成本应该是0");
        assertFalse(immediateAssign.hasRegisterOperation(), 
                    "立即数类型不应该有寄存器操作");
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