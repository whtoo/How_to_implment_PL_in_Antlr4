package org.teachfx.antlr4.ep18r.stackvm.memory;

/**
 * 内存管理器接口
 * 定义了虚拟机内存管理的抽象接口
 */
public interface IMemoryManager {

    /**
     * 读取指定地址的内存值
     * @param address 内存地址
     * @return 内存值
     * @throws IndexOutOfBoundsException 如果地址越界
     */
    int readMemory(int address);

    /**
     * 写入值到指定地址的内存
     * @param address 内存地址
     * @param value 要写入的值
     * @throws IndexOutOfBoundsException 如果地址越界
     */
    void writeMemory(int address, int value);

    /**
     * 读取堆内存
     * @param address 堆内存地址
     * @return 堆内存值
     * @throws IndexOutOfBoundsException 如果地址越界
     */
    int readHeap(int address);

    /**
     * 写入值到堆内存
     * @param address 堆内存地址
     * @param value 要写入的值
     * @throws IndexOutOfBoundsException 如果地址越界
     */
    void writeHeap(int address, int value);

    /**
     * 获取堆分配指针
     * @return 当前堆分配指针位置
     */
    int getHeapAllocPointer();

    /**
     * 设置堆分配指针
     * @param pointer 新的堆分配指针位置
     */
    void setHeapAllocPointer(int pointer);

    /**
     * 获取代码数组
     * @return 代码字节数组
     */
    byte[] getCode();

    /**
     * 获取代码大小
     * @return 代码大小（字节）
     */
    int getCodeSize();

    /**
     * 读取全局变量
     * @param address 全局变量地址
     * @return 全局变量值
     */
    Object readGlobal(int address);

    /**
     * 写入全局变量
     * @param address 全局变量地址
     * @param value 要写入的值
     */
    void writeGlobal(int address, Object value);
}
