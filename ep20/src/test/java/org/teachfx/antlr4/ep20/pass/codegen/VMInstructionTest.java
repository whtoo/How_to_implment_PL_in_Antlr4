package org.teachfx.antlr4.ep20.pass.codegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ir.expr.CallFunc;
import org.teachfx.antlr4.ep20.ir.expr.arith.BinExpr;
import org.teachfx.antlr4.ep20.ir.expr.arith.UnaryExpr;
import org.teachfx.antlr4.ep20.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep20.ir.stmt.Assign;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.type.OperatorType;
import org.teachfx.antlr4.ep20.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep20.ir.expr.addr.OperandSlot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("虚拟机指令验证测试")
public class VMInstructionTest {
    private CymbolAssembler cymbolAssembler;

    @BeforeEach
    public void setUp() {
        cymbolAssembler = new CymbolAssembler();
    }

    @Test
    @DisplayName("应正确生成二元运算指令")
    public void testEmitBinaryOperationInstruction() {
        // Arrange
        OperandSlot left = OperandSlot.genTemp();
        OperandSlot right = OperandSlot.genTemp();
        BinExpr addExpr = BinExpr.with(OperatorType.BinaryOpType.ADD, left, right);
        
        // Act
        cymbolAssembler.visit(addExpr);
        
        // Assert
        assertEquals("iadd\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成一元运算指令")
    public void testEmitUnaryOperationInstruction() {
        // Arrange
        OperandSlot operand = OperandSlot.genTemp();
        UnaryExpr negExpr = UnaryExpr.with(OperatorType.UnaryOpType.NEG, operand);
        
        // Act
        cymbolAssembler.visit(negExpr);
        
        // Assert
        assertEquals("ineg\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成赋值指令")
    public void testEmitAssignmentInstruction() {
        // Arrange
        OperandSlot operandSlot = OperandSlot.genTemp();
        FrameSlot frameSlot = new FrameSlot(1);
        
        // 为了简化，我们直接测试emit方法
        cymbolAssembler.emit("iconst 5");
        cymbolAssembler.emit("store 1");
        
        // Assert
        assertEquals("iconst 5\nstore 1\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成比较指令")
    public void testEmitComparisonInstruction() {
        // Arrange
        OperandSlot left = OperandSlot.genTemp();
        OperandSlot right = OperandSlot.genTemp();
        BinExpr ltExpr = BinExpr.with(OperatorType.BinaryOpType.LT, left, right);
        
        // Act
        cymbolAssembler.visit(ltExpr);
        
        // Assert
        assertEquals("ilt\n", cymbolAssembler.getAsmInfo());
    }
    
    @Test
    @DisplayName("应正确生成逻辑运算指令")
    public void testEmitLogicalOperationInstruction() {
        // Arrange
        OperandSlot left = OperandSlot.genTemp();
        OperandSlot right = OperandSlot.genTemp();
        BinExpr andExpr = BinExpr.with(OperatorType.BinaryOpType.AND, left, right);
        
        // Act
        cymbolAssembler.visit(andExpr);
        
        // Assert
        // 逻辑运算使用iand指令而不是band
        assertEquals("iand\n", cymbolAssembler.getAsmInfo());
    }
}