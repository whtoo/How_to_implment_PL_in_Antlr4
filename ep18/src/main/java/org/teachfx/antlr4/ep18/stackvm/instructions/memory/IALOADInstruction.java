package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 数组加载指令
 * 从局部变量数组指定偏移量加载值到栈顶
 *
 * <p>指令格式：iaload base_slot, offset</p>
 * <p>操作数格式：</p>
 * <pre>
 *     operand = (base_slot << 16) | (offset & 0xFFFF)
 *     其中：
 *       - base_slot (15-0): 数组变量在局部变量表中的索引（高15位）
 *       - offset (15-0): 数组访问的偏移量（低16位）
 * </pre>
 *
 * @author EP21数组功能实现
 */
public class IALOADInstruction extends BaseInstruction {
    public static final int OPCODE = 43;

    public IALOADInstruction() {
        super("iaload", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        // 解码操作数
        int baseSlot = operand >>> 16;
        int offset = operand & 0xFFFF;

        // 从局部变量表获取数组对象
        Object arrayObj = null;
        int[] locals = context.getLocals();

        if (baseSlot >= 0 && baseSlot < locals.length) {
            int arrayRef = locals[baseSlot];
            arrayObj = context.intToValue(arrayRef);
        }

        if (arrayObj == null) {
            throw new Exception("IALOAD: Array at local slot " + baseSlot + " is null");
        }

        // 计算实际地址：base + offset
        int index = offset / 4;

        if (arrayObj instanceof int[]) {
            int[] array = (int[]) arrayObj;
            if (index < 0 || index >= array.length) {
                throw new Exception("IALOAD: Array index out of bounds: " + index + " (length: " + array.length + ")");
            }

            int value = array[index];
            context.push(value);

            if (context.isTraceEnabled()) {
                System.out.println("IALOAD: local[" + baseSlot + "][" + index + "] = " + value);
            }
        } else {
            throw new Exception("IALOAD: Array at local slot " + baseSlot + " is not an int[]");
        }
    }
}
