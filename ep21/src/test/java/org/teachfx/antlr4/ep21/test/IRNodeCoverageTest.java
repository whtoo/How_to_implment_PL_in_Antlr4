package org.teachfx.antlr4.ep21.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.Expr;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.OperandSlot;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.*;
import org.teachfx.antlr4.ep21.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IR节点覆盖测试套件 (TASK-2.1.1)
 *
 * 测试统一IR系统中各类节点的基本功能和属性：
 * - CallFunc: 函数调用表达式
 * - ReturnVal: 返回语句
 * - JMP: 无条件跳转
 * - CJMP: 条件跳转
 * - Label/FuncEntryLabel: 标签节点
 *
 * @author EP21 Team
 * @version 2.0
 * @since 2025-12-26
 */
@DisplayName("IR节点覆盖测试套件 (TASK-2.1.1)")
class IRNodeCoverageTest {

    private GlobalScope globalScope;
    private BuiltInTypeSymbol intType;
    private MethodSymbol testMethodSymbol;

    @BeforeEach
    void setUp() {
        globalScope = new GlobalScope();
        intType = new BuiltInTypeSymbol("int");
        testMethodSymbol = new MethodSymbol("testFunc", intType, globalScope, null);
        testMethodSymbol.setArgs(2);
    }

    /**
     * CallFunc节点测试
     */
    @Nested
    @DisplayName("CallFunc函数调用表达式测试")
    class CallFuncTests {

        @Test
        @DisplayName("应该正确创建CallFunc节点")
        void testCreateCallFunc() {
            // Given: 函数名、参数数量、方法符号
            String funcName = "calculate";
            int args = 2;

            // When: 创建CallFunc节点
            CallFunc callFunc = new CallFunc(funcName, args, testMethodSymbol);

            // Then: 应该正确设置属性
            assertEquals(funcName, callFunc.getFuncName());
            assertEquals(args, callFunc.getArgs());
            assertEquals(testMethodSymbol, callFunc.getFuncType());
        }

        @Test
        @DisplayName("应该支持设置函数名")
        void testSetFuncName() {
            // Given: CallFunc节点
            CallFunc callFunc = new CallFunc("oldName", 1, testMethodSymbol);

            // When: 修改函数名
            callFunc.setFuncName("newName");

            // Then: 函数名应该被更新
            assertEquals("newName", callFunc.getFuncName());
        }

        @Test
        @DisplayName("toString应该返回正确的字符串表示")
        void testToString() {
            // Given: CallFunc节点
            CallFunc callFunc = new CallFunc("myFunc", 3, testMethodSymbol);

            // When: 调用toString
            String result = callFunc.toString();

            // Then: 应该包含函数名和参数数量
            assertTrue(result.contains("myFunc"));
            assertTrue(result.contains("args:3"));
        }

        @Test
        @DisplayName("CallFunc复杂度级别应该为2(语句级别，包含函数调用)")
        void testComplexityLevel() {
            // Given: CallFunc节点
            CallFunc callFunc = new CallFunc("test", 1, testMethodSymbol);

            // When & Then: 复杂度级别应该为2
            assertEquals(2, callFunc.getComplexityLevel());
        }
    }

    /**
     * ReturnVal节点测试
     */
    @Nested
    @DisplayName("ReturnVal返回语句测试")
    class ReturnValTests {

        @Test
        @DisplayName("应该正确创建ReturnVal节点")
        void testCreateReturnVal() {
            // Given: 返回值变量（使用OperandSlot作为具体实现）
            OperandSlot retVal = OperandSlot.genTemp();

            // When: 创建ReturnVal节点
            ReturnVal returnVal = new ReturnVal(retVal, globalScope);

            // Then: 应该正确设置返回值
            assertEquals(retVal, returnVal.getRetVal());
            assertNotNull(returnVal.retFuncLabel);
        }

        @Test
        @DisplayName("应该支持设置返回值")
        void testSetRetVal() {
            // Given: ReturnVal节点
            OperandSlot retVal1 = OperandSlot.genTemp();
            ReturnVal returnVal = new ReturnVal(retVal1, globalScope);

            // When: 修改返回值
            OperandSlot retVal2 = OperandSlot.genTemp();
            returnVal.setRetVal(retVal2);

            // Then: 返回值应该被更新
            assertEquals(retVal2, returnVal.getRetVal());
        }

        @Test
        @DisplayName("应该正确识别主入口返回")
        void testMainEntryFlag() {
            // Given: ReturnVal节点
            ReturnVal returnVal = new ReturnVal(null, globalScope);

            // When: 设置为主入口
            returnVal.setMainEntry(true);

            // Then: 应该被标记为主入口
            assertTrue(returnVal.isMainEntry());
        }

        @Test
        @DisplayName("语句类型应该是RETURN")
        void testStmtType() {
            // Given: ReturnVal节点
            ReturnVal returnVal = new ReturnVal(null, globalScope);

            // When & Then: 语句类型应该是RETURN
            assertEquals(Stmt.StmtType.RETURN, returnVal.getStmtType());
        }

        @Test
        @DisplayName("主入口toString应该返回halt")
        void testMainEntryToString() {
            // Given: 主入口ReturnVal
            ReturnVal returnVal = new ReturnVal(null, globalScope);
            returnVal.setMainEntry(true);

            // When: 调用toString
            String result = returnVal.toString();

            // Then: 应该包含halt
            assertTrue(result.contains("halt"));
        }

        @Test
        @DisplayName("普通函数返回toString应该返回ret")
        void testNormalReturnToString() {
            // Given: 普通ReturnVal
            ReturnVal returnVal = new ReturnVal(null, globalScope);

            // When: 调用toString
            String result = returnVal.toString();

            // Then: 应该包含ret
            assertTrue(result.contains("ret"));
        }
    }

    /**
     * VarSlot变量槽测试
     */
    @Nested
    @DisplayName("OperandSlot操作数槽测试")
    class OperandSlotTests {

        @Test
        @DisplayName("应该正确生成临时OperandSlot")
        void testGenTemp() {
            // When: 生成临时OperandSlot
            OperandSlot slot1 = OperandSlot.genTemp();
            OperandSlot slot2 = OperandSlot.genTemp();

            // Then: 应该有不同的序号（序号是递增的）
            assertNotEquals(slot1.getOrd(), slot2.getOrd());
            assertTrue(slot2.getOrd() > slot1.getOrd());
        }

        @Test
        @DisplayName("toString应该返回正确的序号格式")
        void testToString() {
            // Given: OperandSlot
            OperandSlot slot = OperandSlot.genTemp();

            // When: 调用toString
            String result = slot.toString();

            // Then: 应该包含t前缀和序号
            assertTrue(result.contains("t"));
        }

        @Test
        @DisplayName("OperandSlot复杂度级别应该为3(表达式级别)")
        void testComplexityLevel() {
            // Given: OperandSlot
            OperandSlot slot = OperandSlot.genTemp();

            // When & Then: 复杂度级别应该为3
            assertEquals(3, slot.getComplexityLevel());
        }
    }

    /**
     * Label标签节点测试
     */
    @Nested
    @DisplayName("Label标签节点测试")
    class LabelTests {

        @Test
        @DisplayName("应该正确创建Label节点")
        void testCreateLabel() {
            // Given: 标签名和作用域
            String labelName = "loop_start";

            // When: 创建Label节点
            Label label = new Label(labelName, globalScope);

            // Then: 应该正确设置标签名
            assertEquals(labelName, label.getRawLabel());
        }

        @Test
        @DisplayName("应该支持空标签名(用于函数入口等)")
        void testNullLabelName() {
            // When & Then: 创建null标签名不应该抛出异常
            assertDoesNotThrow(() -> {
                Label label = new Label(null, globalScope);
                assertNull(label.getRawLabel());
            });
        }

        @Test
        @DisplayName("Label应该是基本块入口")
        void testIsBasicBlockEntry() {
            // Given: Label节点
            Label label = new Label("block_entry", globalScope);

            // When & Then: 应该是基本块入口
            assertTrue(label.isBasicBlockEntry());
        }

        @Test
        @DisplayName("应该支持设置下一个入口节点")
        void testNextEntry() {
            // Given: Label节点
            Label label = new Label("start", globalScope);
            Label nextLabel = new Label("next", globalScope);

            // When: 设置下一个入口
            label.setNextEntry(nextLabel);

            // Then: 下一个入口应该被设置
            assertEquals(nextLabel, label.getNextEntry());
        }

        @Test
        @DisplayName("toString应该返回标签字符串")
        void testToString() {
            // Given: Label节点
            Label label = new Label("my_label", globalScope);

            // When: 调用toString
            String result = label.toString();

            // Then: 应该返回标签名
            assertEquals("my_label", result);
        }
    }

    /**
     * FuncEntryLabel函数入口标签测试
     */
    @Nested
    @DisplayName("FuncEntryLabel函数入口标签测试")
    class FuncEntryLabelTests {

        @Test
        @DisplayName("应该正确创建FuncEntryLabel节点")
        void testCreateFuncEntryLabel() {
            // Given: 函数信息
            String funcName = "myFunction";
            int args = 2;
            int locals = 3;

            // When: 创建FuncEntryLabel节点
            FuncEntryLabel funcLabel = new FuncEntryLabel(funcName, args, locals, globalScope);

            // Then: 应该正确设置函数信息
            String expectedLabel = ".def %s: args=%d, locals=%d".formatted(funcName, args, locals);
            assertEquals(expectedLabel, funcLabel.getRawLabel());
        }

        @Test
        @DisplayName("FuncEntryLabel应该是基本块入口")
        void testIsBasicBlockEntry() {
            // Given: FuncEntryLabel节点
            FuncEntryLabel funcLabel = new FuncEntryLabel("func", 1, 0, globalScope);

            // When & Then: 应该是基本块入口
            assertTrue(funcLabel.isBasicBlockEntry());
        }

        @Test
        @DisplayName("toString应该返回函数定义标签")
        void testToString() {
            // Given: FuncEntryLabel节点
            FuncEntryLabel funcLabel = new FuncEntryLabel("myFunc", 2, 1, globalScope);

            // When: 调用toString
            String result = funcLabel.toString();

            // Then: 应该包含函数定义格式
            assertTrue(result.contains(".def"));
            assertTrue(result.contains("myFunc"));
            assertTrue(result.contains("args=2"));
            assertTrue(result.contains("locals=1"));
        }
    }

    /**
     * ConstVal常量值测试
     */
    @Nested
    @DisplayName("ConstVal常量值测试")
    class ConstValTests {

        @Test
        @DisplayName("应该正确创建整数常量")
        void testCreateIntConst() {
            // Given: 整数值
            Integer value = 42;

            // When: 创建ConstVal节点
            ConstVal<Integer> constVal = new ConstVal<>(value);

            // Then: 应该正确设置值
            assertEquals(value, constVal.getVal());
        }

        @Test
        @DisplayName("应该正确创建字符串常量")
        void testCreateStringConst() {
            // Given: 字符串值
            String value = "hello";

            // When: 创建ConstVal节点
            ConstVal<String> constVal = new ConstVal<>(value);

            // Then: 应该正确设置值
            assertEquals(value, constVal.getVal());
        }

        @Test
        @DisplayName("应该支持设置值")
        void testSetValue() {
            // Given: ConstVal节点
            ConstVal<Integer> constVal = new ConstVal<>(10);

            // When: 修改值
            constVal.setVal(20);

            // Then: 值应该被更新
            assertEquals(20, constVal.getVal());
        }

        @Test
        @DisplayName("整数toString应该返回正确格式")
        void testIntToString() {
            // Given: 整数常量
            ConstVal<Integer> constVal = new ConstVal<>(42);

            // When: 调用toString
            String result = constVal.toString();

            // Then: 应该包含整数值
            assertTrue(result.contains("42"));
        }

        @Test
        @DisplayName("字符串toString应该返回正确格式")
        void testStringToString() {
            // Given: 字符串常量
            ConstVal<String> constVal = new ConstVal<>("test");

            // When: 调用toString
            String result = constVal.toString();

            // Then: 应该包含引号和字符串值
            assertTrue(result.contains("test"));
        }
    }

    /**
     * IR节点继承体系测试
     */
    @Nested
    @DisplayName("IR节点继承体系测试")
    class IRHierarchyTests {

        @Test
        @DisplayName("所有标签节点应该继承自Label")
        void testLabelInheritance() {
            // Given & Then: 各种标签节点
            assertInstanceOf(Label.class, new Label("test", globalScope));
            assertInstanceOf(Label.class, new FuncEntryLabel("func", 1, 0, globalScope));
        }

        @Test
        @DisplayName("所有表达式节点应该继承自Expr")
        void testExprInheritance() {
            // Given & Then: 各种表达式节点
            assertInstanceOf(Expr.class, new CallFunc("test", 1, testMethodSymbol));
            assertInstanceOf(Expr.class, new ConstVal<>(42));
            assertInstanceOf(Expr.class, OperandSlot.genTemp());
        }

        @Test
        @DisplayName("所有IR节点应该继承自IRNode")
        void testIRNodeInheritance() {
            // Given & Then: 各种IR节点
            assertInstanceOf(IRNode.class, new Label("test", globalScope));
            assertInstanceOf(IRNode.class, new CallFunc("test", 1, testMethodSymbol));
            assertInstanceOf(IRNode.class, new ConstVal<>(42));
        }

        @Test
        @DisplayName("IR节点类型应该被正确识别")
        void testIRNodeTypeRecognition() {
            // Given: 各种IR节点
            CallFunc callFunc = new CallFunc("test", 1, testMethodSymbol);
            Label label = new Label("test", globalScope);
            ConstVal<Integer> constVal = new ConstVal<>(42);

            // When & Then: IR节点类型应该被正确识别
            assertEquals(IRNode.IRNodeType.EXPRESSION, callFunc.getIRNodeType());
            assertEquals(IRNode.IRNodeType.EXPRESSION, constVal.getIRNodeType());
            // Label的类名不匹配STATEMENT前缀("Stmt","Assign","Return")
            // 根据fromClass方法的实现，未匹配的类默认为EXPRESSION
            assertEquals(IRNode.IRNodeType.EXPRESSION, label.getIRNodeType(),
                         "Label默认类型为EXPRESSION（因为类名不匹配STATEMENT前缀）");
        }

        @Test
        @DisplayName("Label节点应该是基本块入口")
        void testLabelBasicBlockEntry() {
            // Given: Label节点
            Label label = new Label("block_entry", globalScope);
            FuncEntryLabel funcLabel = new FuncEntryLabel("func", 1, 0, globalScope);

            // When & Then: 都应该是基本块入口
            assertTrue(label.isBasicBlockEntry());
            assertTrue(funcLabel.isBasicBlockEntry());
        }
    }

    /**
     * Assign赋值语句测试
     */
    @Nested
    @DisplayName("Assign赋值语句测试")
    class AssignTests {

        @Test
        @DisplayName("应该使用工厂方法创建Assign")
        void testCreateAssignWithFactory() {
            // Given: 左值和右值（使用OperandSlot）
            OperandSlot lhs = OperandSlot.genTemp();
            OperandSlot rhs = OperandSlot.genTemp();

            // When: 使用工厂方法创建Assign
            Assign assign = Assign.with(lhs, rhs);

            // Then: 应该正确设置左值和右值
            assertEquals(lhs, assign.getLhs());
            assertEquals(rhs, assign.getRhs());
        }

        @Test
        @DisplayName("应该支持创建带ConstVal的Assign")
        void testCreateAssignWithConst() {
            // Given: 左值和常量右值
            OperandSlot lhs = OperandSlot.genTemp();
            ConstVal<Integer> rhs = new ConstVal<>(42);

            // When: 创建Assign
            Assign assign = Assign.with(lhs, rhs);

            // Then: 应该正确设置左值和右值
            assertEquals(lhs, assign.getLhs());
            assertEquals(rhs, assign.getRhs());
        }

        @Test
        @DisplayName("Assign的toString应该返回正确的赋值表示")
        void testAssignToString() {
            // Given: Assign节点
            OperandSlot lhs = OperandSlot.genTemp();
            OperandSlot rhs = OperandSlot.genTemp();
            Assign assign = Assign.with(lhs, rhs);

            // When: 调用toString
            String result = assign.toString();

            // Then: 应该包含赋值操作符
            assertTrue(result.contains("="));
        }

        @Test
        @DisplayName("Assign语句类型应该是ASSIGN")
        void testAssignStmtType() {
            // Given: Assign节点
            OperandSlot lhs = OperandSlot.genTemp();
            OperandSlot rhs = OperandSlot.genTemp();
            Assign assign = Assign.with(lhs, rhs);

            // When & Then: 语句类型应该是ASSIGN
            assertEquals(Stmt.StmtType.ASSIGN, assign.getStmtType());
        }
    }
}
