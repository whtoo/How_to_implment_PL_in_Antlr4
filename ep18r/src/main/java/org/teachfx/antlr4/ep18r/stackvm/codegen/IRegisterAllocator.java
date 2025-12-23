package org.teachfx.antlr4.ep18r.stackvm.codegen;

import java.util.Map;
import java.util.Set;

/**
 * 寄存器分配器接口
 *
 * <p>负责将虚拟寄存器（变量）映射到物理寄存器，并在物理寄存器不足时处理溢出到栈的情况。</p>
 *
 * <p>EP18R 寄存器规范：</p>
 * <ul>
 *   <li>R0 (zero): 始终为0，不可分配</li>
 *   <li>R1 (ra): 返回地址/临时寄存器，调用者保存</li>
 *   <li>R2-R7 (a0-a5): 参数寄存器/返回值，调用者保存</li>
 *   <li>R8-R12 (s0-s4): 被调用者保存寄存器</li>
 *   <li>R13 (sp): 栈指针，不可分配</li>
 *   <li>R14 (fp): 帧指针，不可分配</li>
 *   <li>R15 (lr): 链接寄存器，调用者保存</li>
 * </ul>
 *
 * @author EP18R Register VM Team
 * @version 1.0
 */
public interface IRegisterAllocator {

    /**
     * 为变量分配一个物理寄存器
     *
     * @param varName 变量名称
     * @return 分配的物理寄存器编号 (0-15)
     * @throws IllegalStateException 如果没有可用的物理寄存器
     */
    int allocate(String varName);

    /**
     * 释放变量占用的物理寄存器
     *
     * @param varName 变量名称
     */
    void free(String varName);

    /**
     * 获取变量对应的物理寄存器
     *
     * @param varName 变量名称
     * @return 物理寄存器编号，如果变量未分配寄存器则返回-1
     */
    int getRegister(String varName);

    /**
     * 获取当前所有变量到寄存器的映射关系
     *
     * @return 不可修改的映射表（变量名 -> 寄存器编号）
     */
    Map<String, Integer> getAllocation();

    /**
     * 重置分配器状态，清空所有分配
     */
    void reset();

    /**
     * 将变量溢出到栈
     *
     * <p>当物理寄存器不足时，将某些变量溢出到栈上保存。
     * 溢出的变量会被分配一个栈槽位，后续访问时需要通过内存操作。</p>
     *
     * @param varName 变量名称
     * @return 分配的栈槽位偏移量（相对于帧指针fp）
     * @throws IllegalStateException 如果变量未分配寄存器
     */
    int spillToStack(String varName);

    /**
     * 检查变量是否已溢出到栈
     *
     * @param varName 变量名称
     * @return true如果变量已溢出到栈
     */
    boolean isSpilled(String varName);

    /**
     * 获取变量的栈槽位偏移量
     *
     * @param varName 变量名称
     * @return 栈槽位偏移量（相对于帧指针fp），如果变量未溢出则返回-1
     */
    int getSpillSlot(String varName);

    /**
     * 获取可分配的寄存器数量
     *
     * @return 可分配的寄存器数量
     */
    int getAvailableRegisterCount();

    /**
     * 获取已分配的寄存器数量
     *
     * @return 已分配的寄存器数量
     */
    int getAllocatedRegisterCount();

    /**
     * 获取所有已分配寄存器的变量名集合
     *
     * @return 已分配寄存器的变量名集合（不可修改）
     */
    Set<String> getAllocatedVariables();

    /**
     * 获取所有已溢出到栈的变量名集合
     *
     * @return 已溢出到栈的变量名集合（不可修改）
     */
    Set<String> getSpilledVariables();
}
