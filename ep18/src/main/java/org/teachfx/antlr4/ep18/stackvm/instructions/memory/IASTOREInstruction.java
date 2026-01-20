package org.teachfx.antlr4.ep18.stackvm.instructions.memory;

import org.teachfx.antlr4.ep18.stackvm.VMExecutionContext;
import org.teachfx.antlr4.ep18.stackvm.instructions.BaseInstruction;

/**
 * 数组存储指令
 * 从栈顶弹出值并存储到局部变量数组指定偏移量
 *
 * <p>指令格式：iastore base_slot, offset</p>
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
public class IASTOREInstruction extends BaseInstruction {
    public static final int OPCODE = 44;

    public IASTOREInstruction() {
        super("iastore", OPCODE, true);
    }

    @Override
    public void execute(VMExecutionContext context, int operand) throws Exception {
        int baseSlot = operand >>> 16;
        int offset = operand & 0xFFFF;

        int value = context.pop();

        Object arrayObj = null;
        int[] locals = context.getLocals();

        if (baseSlot >= 0 && baseSlot < locals.length) {
            int arrayRef = locals[baseSlot];
            arrayObj = context.intToValue(arrayRef);
        }

        if (arrayObj == null) {
            throw new Exception("IASTORE: Array at local slot " + baseSlot + " is null");
        }

        int index = offset / 4;

        if (arrayObj instanceof int[]) {
            int[] array = (int[]) arrayObj;
            if (index < 0 || index >= array.length) {
                throw new Exception("IASTORE: Array index out of bounds: " + index + " (length: " + array.length + ")");
            }

            array[index] = value;

            if (context.isTraceEnabled()) {
                System.out.println("IASTORE: local[" + baseSlot + "][" + index + "] = " + value);
            }
        } else {
            throw new Exception("IASTORE: Array at local slot " + baseSlot + " is not an int[]");
        }
    }
}
