package org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.VMDivisionByZeroException;
import org.teachfx.antlr4.ep18.stackvm.VMOverflowException;

/**
 * 算术指令执行器
 * 提供高效的算术运算实现，包括溢出检测、性能优化和类型转换
 */
public class ArithmeticExecutor {

    /**
     * 整数加法
     * @param context 执行上下文
     * @param a 操作数A
     * @param b 操作数B
     * @return 结果
     * @throws VMOverflowException 如果发生溢出
     */
    public static int add(VMExecutionContext context, int a, int b) throws VMOverflowException {
        long result = (long) a + (long) b;

        // 溢出检测
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Integer overflow: %d + %d = %d", a, b, result)
            );
        }

        return (int) result;
    }

    /**
     * 整数减法
     * @param context 执行上下文
     * @param a 操作数A
     * @param b 操作数B
     * @return 结果
     * @throws VMOverflowException 如果发生溢出
     */
    public static int subtract(VMExecutionContext context, int a, int b) throws VMOverflowException {
        long result = (long) a - (long) b;

        // 溢出检测
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Integer overflow: %d - %d = %d", a, b, result)
            );
        }

        return (int) result;
    }

    /**
     * 整数乘法
     * @param context 执行上下文
     * @param a 操作数A
     * @param b 操作数B
     * @return 结果
     * @throws VMOverflowException 如果发生溢出
     */
    public static int multiply(VMExecutionContext context, int a, int b) throws VMOverflowException {
        long result = (long) a * (long) b;

        // 溢出检测
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Integer overflow: %d * %d = %d", a, b, result)
            );
        }

        return (int) result;
    }

    /**
     * 整数除法
     * @param context 执行上下文
     * @param a 操作数A
     * @param b 操作数B
     * @return 结果
     * @throws VMDivisionByZeroException 如果除以零
     * @throws VMOverflowException 如果发生溢出
     */
    public static int divide(VMExecutionContext context, int a, int b) throws VMDivisionByZeroException {
        if (b == 0) {
            throw new VMDivisionByZeroException(
                context.getProgramCounter(),
                "IDIV"
            );
        }

        // 检查Integer.MIN_VALUE / -1的情况（会溢出）
        if (a == Integer.MIN_VALUE && b == -1) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Integer overflow: %d / %d", a, b)
            );
        }

        return a / b;
    }

    /**
     * 整数取模
     * @param context 执行上下文
     * @param a 操作数A
     * @param b 操作数B
     * @return 结果
     * @throws ArithmeticException 如果除以零
     */
    public static int modulo(VMExecutionContext context, int a, int b) throws ArithmeticException {
        if (b == 0) {
            throw new ArithmeticException("Modulo by zero: " + a + " % " + b);
        }
        return a % b;
    }

    /**
     * 整数取负
     * @param context 执行上下文
     * @param a 操作数
     * @return 结果
     * @throws VMOverflowException 如果发生溢出
     */
    public static int negate(VMExecutionContext context, int a) throws VMOverflowException {
        // 检查Integer.MIN_VALUE取负的情况
        if (a == Integer.MIN_VALUE) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Integer overflow: -%d", a)
            );
        }
        return -a;
    }

    /**
     * 整数按位与
     */
    public static int and(int a, int b) {
        return a & b;
    }

    /**
     * 整数按位或
     */
    public static int or(int a, int b) {
        return a | b;
    }

    /**
     * 整数按位异或
     */
    public static int xor(int a, int b) {
        return a ^ b;
    }

    /**
     * 整数按位非
     */
    public static int not(int a) {
        return ~a;
    }

    /**
     * 整数左移
     * @param context 执行上下文
     * @param a 操作数
     * @param shift 移位量
     * @return 结果
     * @throws VMOverflowException 如果移位导致溢出
     */
    public static int shiftLeft(VMExecutionContext context, int a, int shift) throws VMOverflowException {
        if (shift < 0 || shift > 31) {
            throw new IllegalArgumentException("Invalid shift amount: " + shift);
        }

        int result = a << shift;

        // 检查移位是否导致符号位变化（可能的溢出）
        if (shift > 0 && ((a > 0 && result < 0) || (a < 0 && result > 0))) {
            // 这不是错误，只是警告
            if (context.isTraceEnabled()) {
                System.out.println("WARNING: Sign change in shift operation: " + a + " << " + shift + " = " + result);
            }
        }

        return result;
    }

    /**
     * 整数右移（算术右移，保留符号位）
     */
    public static int shiftRight(int a, int shift) {
        if (shift < 0 || shift > 31) {
            throw new IllegalArgumentException("Invalid shift amount: " + shift);
        }
        return a >> shift;
    }

    /**
     * 整数无符号右移
     */
    public static int unsignedShiftRight(int a, int shift) {
        if (shift < 0 || shift > 31) {
            throw new IllegalArgumentException("Invalid shift amount: " + shift);
        }
        return a >>> shift;
    }

    // 浮点运算优化

    /**
     * 浮点加法（优化版）
     */
    public static float add(float a, float b) {
        return a + b;
    }

    /**
     * 浮点减法（优化版）
     */
    public static float subtract(float a, float b) {
        return a - b;
    }

    /**
     * 浮点乘法（优化版）
     */
    public static float multiply(float a, float b) {
        return a * b;
    }

    /**
     * 浮点除法（优化版）
     */
    public static float divide(float a, float b) {
        if (b == 0.0f) {
            throw new ArithmeticException("Float division by zero: " + a + " / " + b);
        }
        return a / b;
    }

    /**
     * 整数转浮点数（优化版）
     */
    public static float intToFloat(int a) {
        return (float) a;
    }

    /**
     * 浮点数转整数（带溢出检查）
     * @param context 执行上下文
     * @param a 浮点值
     * @return 整数结果
     * @throws VMOverflowException 如果转换结果超出整数范围
     */
    public static int floatToInt(VMExecutionContext context, float a) throws VMOverflowException {
        if (a > Integer.MAX_VALUE || a < Integer.MIN_VALUE) {
            throw new VMOverflowException(
                context.getProgramCounter(),
                String.format("Float to int overflow: %f", a)
            );
        }
        return (int) a;
    }

    // 类型转换辅助方法

    /**
     * 安全地检查加法是否溢出
     */
    public static boolean willAdditionOverflow(int a, int b) {
        // 使用位运算检查溢出，避免实际执行加法
        if (b > 0) {
            return a > Integer.MAX_VALUE - b;
        } else {
            return a < Integer.MIN_VALUE - b;
        }
    }

    /**
     * 安全地检查减法是否溢出
     */
    public static boolean willSubtractionOverflow(int a, int b) {
        // 使用位运算检查溢出，避免实际执行减法
        if (b > 0) {
            return a < Integer.MIN_VALUE + b;
        } else {
            return a > Integer.MAX_VALUE + b;
        }
    }

    /**
     * 安全地检查乘法是否溢出
     */
    public static boolean willMultiplicationOverflow(int a, int b) {
        // 检查特殊情况
        if (a == 0 || b == 0) {
            return false;
        }

        if (a == Integer.MIN_VALUE || b == Integer.MIN_VALUE) {
            return true;
        }

        // 使用long类型进行安全检查
        long result = (long) a * (long) b;
        return result > Integer.MAX_VALUE || result < Integer.MIN_VALUE;
    }

    /**
     * 获取算术操作的统计信息
     */
    public static class ArithmeticStatistics {
        private int addCount = 0;
        private int subtractCount = 0;
        private int multiplyCount = 0;
        private int divideCount = 0;
        private int overflowCount = 0;

        public void recordAdd() { addCount++; }
        public void recordSubtract() { subtractCount++; }
        public void recordMultiply() { multiplyCount++; }
        public void recordDivide() { divideCount++; }
        public void recordOverflow() { overflowCount++; }

        public int getAddCount() { return addCount; }
        public int getSubtractCount() { return subtractCount; }
        public int getMultiplyCount() { return multiplyCount; }
        public int getDivideCount() { return divideCount; }
        public int getOverflowCount() { return overflowCount; }

        public int getTotalOperations() {
            return addCount + subtractCount + multiplyCount + divideCount;
        }

        public double getOverflowRate() {
            int total = getTotalOperations();
            return total > 0 ? (double) overflowCount / total : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "Arithmetic Statistics:\n" +
                "  Add: %d\n" +
                "  Subtract: %d\n" +
                "  Multiply: %d\n" +
                "  Divide: %d\n" +
                "  Total: %d\n" +
                "  Overflows: %d (%.2f%%)",
                addCount, subtractCount, multiplyCount, divideCount,
                getTotalOperations(), overflowCount, getOverflowRate() * 100
            );
        }
    }
}
