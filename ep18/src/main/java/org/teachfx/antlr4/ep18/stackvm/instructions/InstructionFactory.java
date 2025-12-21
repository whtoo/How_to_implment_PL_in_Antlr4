package org.teachfx.antlr4.ep18.stackvm.instructions;

import org.teachfx.antlr4.ep18.stackvm.instructions.arithmetic.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.comparison.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.constant.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.controlflow.*;
import org.teachfx.antlr4.ep18.stackvm.instructions.memory.*;

/**
 * 指令工厂
 * 负责创建和缓存指令实例
 * 使用单例模式确保指令实例的复用
 */
public class InstructionFactory {
    private static InstructionFactory instance;
    private final Instruction[] instructionCache;

    /**
     * 私有构造函数
     */
    private InstructionFactory() {
        instructionCache = new Instruction[256]; // 支持0-255操作码
        initializeInstructions();
    }

    /**
     * 获取单例实例
     */
    public static synchronized InstructionFactory getInstance() {
        if (instance == null) {
            instance = new InstructionFactory();
        }
        return instance;
    }

    /**
     * 初始化指令缓存
     */
    private void initializeInstructions() {
        // 算术指令
        registerInstruction(new IAddInstruction());
        registerInstruction(new ISubInstruction());
        registerInstruction(new IMulInstruction());
        registerInstruction(new IDivInstruction());
        registerInstruction(new INegInstruction());
        registerInstruction(new INotInstruction());
        registerInstruction(new IAndInstruction());
        registerInstruction(new IOrInstruction());
        registerInstruction(new IXorInstruction());
        // 浮点算术指令
        registerInstruction(new FAddInstruction());
        registerInstruction(new FSubInstruction());
        registerInstruction(new FMulInstruction());
        registerInstruction(new FDivInstruction());
        registerInstruction(new IToFInstruction());

        // 比较指令
        registerInstruction(new ILtInstruction());
        registerInstruction(new ILeInstruction());
        registerInstruction(new IGtInstruction());
        registerInstruction(new IGeInstruction());
        registerInstruction(new IEqInstruction());
        registerInstruction(new INeInstruction());
        // 浮点比较指令
        registerInstruction(new FLtInstruction());
        registerInstruction(new FEqInstruction());

        // 常量指令
        registerInstruction(new IConstInstruction());
        registerInstruction(new FConstInstruction());
        registerInstruction(new NullInstruction());

        // 控制流指令
        registerInstruction(new BrInstruction());
        registerInstruction(new BrtInstruction());
        registerInstruction(new BrfInstruction());
        registerInstruction(new CallInstruction());
        registerInstruction(new RetInstruction());
        registerInstruction(new HaltInstruction());
        registerInstruction(new PrintInstruction());
        registerInstruction(new PopInstruction());

        // 内存指令
        registerInstruction(new LoadInstruction());
        registerInstruction(new StoreInstruction());
        registerInstruction(new GLoadInstruction());
        registerInstruction(new GStoreInstruction());
        registerInstruction(new FLoadInstruction());
        registerInstruction(new FStoreInstruction());
        registerInstruction(new StructInstruction());
    }

    /**
     * 注册指令到缓存
     */
    private void registerInstruction(Instruction instruction) {
        if (instruction != null && instruction.getOpcode() >= 0 && instruction.getOpcode() < 256) {
            instructionCache[instruction.getOpcode()] = instruction;
        }
    }

    /**
     * 根据操作码获取指令实例
     * @param opcode 操作码
     * @return 指令实例，如果未找到则返回null
     */
    public Instruction getInstruction(int opcode) {
        if (opcode < 0 || opcode >= 256) {
            throw new IllegalArgumentException("Invalid opcode: " + opcode);
        }
        return instructionCache[opcode];
    }

    /**
     * 根据操作码获取指令实例，如果不存在则抛出异常
     * @param opcode 操作码
     * @return 指令实例
     * @throws UnsupportedOperationException 如果指令未实现
     */
    public Instruction getRequiredInstruction(int opcode) throws UnsupportedOperationException {
        Instruction instruction = getInstruction(opcode);
        if (instruction == null) {
            throw new UnsupportedOperationException(
                "Unsupported opcode: 0x" + Integer.toHexString(opcode)
            );
        }
        return instruction;
    }

    /**
     * 检查操作码是否已实现
     * @param opcode 操作码
     * @return true如果已实现，false否则
     */
    public boolean isSupported(int opcode) {
        return getInstruction(opcode) != null;
    }

    /**
     * 获取已支持的指令数量
     * @return 支持的指令数量
     */
    public int getSupportedInstructionCount() {
        int count = 0;
        for (Instruction instr : instructionCache) {
            if (instr != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取所有支持的指令列表
     * @return 支持的指令列表
     */
    public java.util.List<Instruction> getSupportedInstructions() {
        java.util.List<Instruction> supported = new java.util.ArrayList<>();
        for (Instruction instr : instructionCache) {
            if (instr != null) {
                supported.add(instr);
            }
        }
        return supported;
    }
}
