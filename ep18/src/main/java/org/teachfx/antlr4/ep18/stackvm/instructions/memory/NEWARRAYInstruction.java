package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 数组分配指令
 * 显式创建并初始化数组对象
 *
 * <p>指令格式：newarray type, size</p>
 * <p>操作数格式：</p>
 * <pre>
 *     operand = (type << 16) | size
 *     其中：
 *       - type (15-0): 数组元素类型（0=int, 1=float, 2=string等）
 *       - size (15-0): 数组大小
 * </pre>
 *
 * <p>支持类型：</p>
 * <ul>
 *   <li>0: int[] - 整型数组</li>
 *   <li>1: float[] - 浮点数组</li>
 *   <li>2: String[] - 字符串数组</li>
 * </ul>
 *
 * @author EP21数组功能深度实现
 */
public class NEWARRAYInstruction extends BaseInstruction {
    public static final int OPCODE = 45;

    // 类型常量
    public static final int TYPE_INT = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_STRING = 2;

    public NEWARRAYInstruction() {
        super("newarray", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        // 解码操作数
        int type = (operand >>> 16) & 0x0F;  // 低16位：类型
        int size = operand >>> 16;                 // 高16位：大小

        // 验证类型
        if (type < TYPE_INT || type > TYPE_STRING) {
            throw new Exception("NEWARRAY: Invalid array type: " + type);
        }

        // 验证大小
        if (size <= 0) {
            throw new Exception("NEWARRAY: Array size must be positive: " + size);
        }

        // 计算所需堆空间：每个元素4字节
        int arraySize = size * 4;

        // 在堆中分配数组空间
        int arrayRef = context.heapAlloc(arraySize);

        // 初始化数组为0
        for (int i = 0; i < size; i++) {
            int offset = i * 4;
            context.heapWrite(arrayRef + offset, 0);
        }

        // 将数组引用（堆地址）压入栈顶
        context.push(arrayRef);

        if (context.isTraceEnabled()) {
            String typeName = switch (type) {
                case TYPE_INT -> "int[]";
                case TYPE_FLOAT -> "float[]";
                case TYPE_STRING -> "String[]";
                default -> "unknown[]";
            };
            System.out.println("NEWARRAY: " + typeName + " size=" + size + " at heap=" + arrayRef);
        }
    }
}
