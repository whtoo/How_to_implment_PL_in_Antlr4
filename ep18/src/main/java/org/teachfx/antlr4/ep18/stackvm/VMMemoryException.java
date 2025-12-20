package org.teachfx.antlr4.ep18.stackvm;

/**
 * VMMemoryException - 内存相关异常的基类
 * 用于处理所有内存访问相关的错误
 */
public class VMMemoryException extends VMException {
    protected final long address;
    protected final int size;
    protected final MemoryAccessType accessType;

    public enum MemoryAccessType {
        READ("read"),
        WRITE("write"),
        ALLOCATE("allocate"),
        FREE("free");

        private final String description;

        MemoryAccessType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public VMMemoryException(String message, int pc, String instruction, 
                           long address, int size, MemoryAccessType accessType) {
        super(message, pc, instruction, formatMemoryDetails(address, size, accessType));
        this.address = address;
        this.size = size;
        this.accessType = accessType;
    }

    public VMMemoryException(String message, int pc, String instruction, long address, MemoryAccessType accessType) {
        this(message, pc, instruction, address, 0, accessType);
    }

    private static String formatMemoryDetails(long address, int size, MemoryAccessType accessType) {
        StringBuilder sb = new StringBuilder();
        sb.append("Memory access violation\n");
        sb.append("  Address: 0x").append(Long.toHexString(address));
        if (size > 0) {
            sb.append("\n  Size: ").append(size).append(" bytes");
        }
        sb.append("\n  Access type: ").append(accessType.getDescription());
        return sb.toString();
    }

    public long getAddress() {
        return address;
    }

    public int getSize() {
        return size;
    }

    public MemoryAccessType getAccessType() {
        return accessType;
    }
}