package org.teachfx.antlr4.ep18.stackvm;

import org.teachfx.antlr4.ep18.symtab.type.Type;
import org.teachfx.antlr4.ep18.symtab.type.StructType;

/**
 * StructValue - 统一的结构体运行时表示
 * 替代StructSpace和int[]堆表示，提供一致的struct内存模型
 */
public class StructValue {
    protected final Object[] fields;
    protected Type type;  // 初始可为null，阶段2填充类型信息

    /**
     * 创建具有指定字段数量的结构体（无类型信息）
     * @param fieldCount 字段数量
     */
    public StructValue(int fieldCount) {
        this.fields = new Object[fieldCount];
        this.type = null;
    }

    /**
     * 创建具有类型信息的结构体
     * @param type 结构体类型
     */
    public StructValue(Type type) {
        if (type instanceof StructType) {
            this.type = type;
            this.fields = new Object[((StructType) type).getFieldCount()];
        } else {
            throw new IllegalArgumentException("Type must be StructType");
        }
    }

    /**
     * 基于偏移量的字段访问（保持兼容性）
     * @param offset 字段偏移量
     * @return 字段值
     */
    public Object getField(int offset) {
        if (offset < 0 || offset >= fields.length) {
            throw new IndexOutOfBoundsException("Field offset out of bounds: " + offset + ", struct has " + fields.length + " fields");
        }
        return fields[offset];
    }

    /**
     * 基于偏移量的字段存储（保持兼容性）
     * @param offset 字段偏移量
     * @param value 字段值
     */
    public void setField(int offset, Object value) {
        if (offset < 0 || offset >= fields.length) {
            throw new IndexOutOfBoundsException("Field offset out of bounds: " + offset + ", struct has " + fields.length + " fields");
        }
        fields[offset] = value;
    }

    /**
     * 基于字段名的访问（未来扩展）
     * @param name 字段名
     * @return 字段值
     */
    public Object getField(String name) {
        if (type == null) throw new IllegalStateException("No type information available");
        // 阶段2实现：通过类型获取字段偏移
        // Integer offset = type.getFieldOffset(name);
        // return fields[offset];
        throw new UnsupportedOperationException("Field access by name not implemented yet");
    }

    /**
     * 基于字段名的存储（未来扩展）
     * @param name 字段名
     * @param value 字段值
     */
    public void setField(String name, Object value) {
        if (type == null) throw new IllegalStateException("No type information available");
        // 阶段2实现：通过类型获取字段偏移
        // Integer offset = type.getFieldOffset(name);
        // fields[offset] = value;
        throw new UnsupportedOperationException("Field access by name not implemented yet");
    }

    /**
     * 获取字段数量
     * @return 字段数量
     */
    public int getFieldCount() {
        return fields.length;
    }

    /**
     * 获取类型信息
     * @return 类型信息（可能为null）
     */
    public Type getType() {
        return type;
    }

    /**
     * 设置类型信息
     * @param type 结构体类型
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 类型检查（阶段3）
     * @param offset 字段偏移
     * @param expected 期望类型
     */
    public void validateFieldType(int offset, Class<?> expected) {
        // 阶段3实现：运行时类型检查
        // 暂时不实现，保持兼容性
    }

    @Override
    public String toString() {
        return "StructValue{fields=" + java.util.Arrays.toString(fields) +
               (type != null ? ", type=" + type : "") + "}";
    }
}