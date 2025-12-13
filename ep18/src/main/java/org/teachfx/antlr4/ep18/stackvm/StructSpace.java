package org.teachfx.antlr4.ep18.stackvm;

import java.util.Arrays;

/**
 * StructSpace - 结构体空间类，现在作为StructValue的兼容层
 * 为了保持向后兼容，提供public fields字段访问
 * 建议新代码使用StructValue类
 */
public class StructSpace extends StructValue {
    // 兼容性字段，指向父类的fields数组
    public Object[] fields;

    public StructSpace(int nfields) {
        super(nfields);
        this.fields = super.fields; // 指向同一个数组
    }

    @Override
    public String toString() {
        return "StructSpace" + Arrays.toString(fields);
    }
}
