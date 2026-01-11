package org.teachfx.antlr4.ep18r.stackvm.memory;

/**
 * 寄存器文件接口
 * 定义了16个通用寄存器的访问接口
 * 
 * 寄存器分配：
 *   r0: 零寄存器（恒为0，只读）
 *   r1-r12: 通用目的寄存器
 *   r13: 栈指针 (SP)
 *   r14: 帧指针 (FP)
 *   r15: 链接寄存器 (LR)
 */
public interface IRegisterFile {

    /**
     * 重置所有寄存器为0
     */
    void reset();

    /**
     * 读取寄存器值
     * @param regNum 寄存器编号 (0-15)
     * @return 寄存器值
     * @throws IllegalArgumentException 如果寄存器编号无效
     */
    int read(int regNum);

    /**
     * 写入寄存器值
     * @param regNum 寄存器编号 (0-15)
     * @param value 要写入的值
     * @throws IllegalArgumentException 如果寄存器编号无效或是只读寄存器
     */
    void write(int regNum, int value);

    /**
     * 批量读取多个寄存器值
     * @param regNums 寄存器编号数组
     * @return 寄存器值数组
     */
    int[] readMultiple(int[] regNums);

    /**
     * 批量写入多个寄存器值
     * @param regNums 寄存器编号数组
     * @param values 值数组
     * @throws IllegalArgumentException 如果数组长度不匹配
     */
    void writeMultiple(int[] regNums, int[] values);

    /**
     * 获取栈指针 (r13)
     * @return 栈指针值
     */
    int getStackPointer();

    /**
     * 设置栈指针 (r13)
     * @param value 新的栈指针值
     */
    void setStackPointer(int value);

    /**
     * 获取帧指针 (r14)
     * @return 帧指针值
     */
    int getFramePointer();

    /**
     * 设置帧指针 (r14)
     * @param value 新的帧指针值
     */
    void setFramePointer(int value);

    /**
     * 获取链接寄存器 (r15) - 存储返回地址
     * @return 链接寄存器值
     */
    int getLinkRegister();

    /**
     * 设置链接寄存器 (r15)
     * @param value 新的链接寄存器值
     */
    void setLinkRegister(int value);

    /**
     * 获取所有寄存器的快照（用于调试）
     * @return 寄存器值副本数组
     */
    int[] snapshot();

    /**
     * 获取寄存器名称
     * @param regNum 寄存器编号
     * @return 寄存器名称字符串
     */
    static String getRegisterName(int regNum) {
        switch (regNum) {
            case 0: return "r0 (zero)";
            case 13: return "r13 (SP)";
            case 14: return "r14 (FP)";
            case 15: return "r15 (LR)";
            default: return "r" + regNum;
        }
    }
}
