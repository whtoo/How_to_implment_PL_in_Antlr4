package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMMemoryAccessException - 内存访问异常
 * 用于处理内存边界检查失败、无效地址访问等错误
 */
public class VMMemoryAccessException extends VMMemoryException {

    public VMMemoryAccessException(String message, int pc, String instruction, 
                                 long address, int size, MemoryAccessType accessType) {
        super(message, pc, instruction, address, size, accessType);
    }

    public VMMemoryAccessException(int pc, String instruction, long address, int size, MemoryAccessType accessType) {
        this("Invalid memory access", pc, instruction, address, size, accessType);
    }

    public VMMemoryAccessException(int pc, String instruction, long address, MemoryAccessType accessType) {
        this("Invalid memory access", pc, instruction, address, 0, accessType);
    }

    public static VMMemoryAccessException outOfBounds(int pc, String instruction, long address,
                                                    long validStart, long validEnd, MemoryAccessType accessType) {
        String message = String.format("Memory access out of bounds: address 0x%x is outside valid range [0x%x, 0x%x]",
                                     address, validStart, validEnd);
        return new VMMemoryAccessException(message, pc, instruction, address, 0, accessType);
    }

    public static VMMemoryAccessException nullPointer(int pc, String instruction, MemoryAccessType accessType) {
        return new VMMemoryAccessException("Null pointer access", pc, instruction, 0, 0, accessType);
    }

    public static VMMemoryAccessException alignmentError(int pc, String instruction, long address,
                                                       int requiredAlignment, MemoryAccessType accessType) {
        String message = String.format("Memory alignment error: address 0x%x is not aligned to %d bytes",
                                     address, requiredAlignment);
        return new VMMemoryAccessException(message, pc, instruction, address, 0, accessType);
    }
}