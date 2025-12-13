# ep18 struct实现统一规划

## 背景
ep18模块是Cymbol语言的虚拟机实现，目前存在两个不同的struct实现：
1. `CymbolStackVM` - 使用int[]堆存储struct，每个字段一个整数槽位
2. `VMInterpreter` - 使用`StructSpace`对象（Object[] fields）存储struct

## 现状分析

### 不一致之处
1. **内存表示**：
   - CymbolStackVM：int[] heap，连续整数槽位
   - VMInterpreter：StructSpace对象，Object[] fields数组

2. **指令实现**：
   - CymbolStackVM.executeStruct()：在堆中分配连续空间
   - VMInterpreter：创建StructSpace对象

3. **字段访问**：
   - CymbolStackVM：通过堆地址偏移（structRef + fieldOffset）
   - VMInterpreter：通过StructSpace.fields[fieldOffset]

4. **类型系统**：
   - 当前实现是弱类型，缺乏字段类型信息
   - 无编译时类型检查
   - 无字段名到偏移的映射

### 优先级要求
1. **代码一致性** - 最高优先级
2. **完全向后兼容性** - 必须保持现有接口和测试通过
3. **功能增强偏好**：类型安全 > 嵌套支持 > 字段映射

## 设计目标

### 核心目标
1. **统一内存表示**：建立一致的struct内存模型
2. **保持向后兼容**：现有测试必须全部通过
3. **增强类型安全**：添加字段类型信息
4. **支持嵌套struct**：允许struct包含其他struct
5. **实现字段映射**：支持字段名到偏移的映射

### 约束条件
1. **测试兼容**：现有测试（CymbolStackVMTest和VMInterpreterTest）必须全部通过
2. **字节码兼容**：现有字节码程序（.vm文件）必须能继续运行，指令格式不变
3. **API兼容**：公开API接口不变，包括构造方法和访问方法
4. **与ep20类型系统集成**：复用ep20的StructType、StructSymbol等类型系统组件

## 技术方案

### 总体架构
采用**统一运行时表示**模式，创建`StructValue`作为ep18中结构体的唯一运行时表示：

```
统一结构体运行时表示（StructValue）
        │
        ├── CymbolStackVM适配层（结构体ID映射）
        │       └── 保持int[]栈和堆，内部使用StructValue表
        │
        └── VMInterpreter适配层（直接对象引用）
                └── 保持Object[]操作数栈，直接使用StructValue
```

**设计原则**：
1. **统一表示**：`StructValue`替代`StructSpace`和`int[]`堆表示
2. **兼容性适配**：通过索引映射机制保持与现有字节码完全兼容
3. **渐进式集成**：分阶段集成ep20类型系统

### 核心组件设计

#### 1. StructValue（统一结构体表示）
```java
package org.teachfx.antlr4.ep18.stackvm;

public class StructValue {
    private final Object[] fields;
    private StructType type;  // 初始可为null，阶段2填充类型信息

    public StructValue(int fieldCount) {
        this.fields = new Object[fieldCount];
        this.type = null;
    }

    public StructValue(StructType type) {
        this.type = type;
        this.fields = new Object[type.getFieldCount()];
    }

    // 基于偏移量的访问（保持兼容性）
    public Object getField(int offset) {
        return fields[offset];
    }

    public void setField(int offset, Object value) {
        fields[offset] = value;
    }

    // 基于字段名的访问（未来扩展）
    public Object getField(String name) {
        if (type == null) throw new IllegalStateException("No type information");
        Integer offset = type.getFieldOffset(name);
        return fields[offset];
    }

    // 类型检查（阶段3）
    public void validateFieldType(int offset, Class<?> expected) {
        // 类型安全检查实现
    }
}
```

#### 2. CymbolStackVM适配层
```java
// 添加结构体管理
private List<StructValue> structTable = new ArrayList<>();
private int nextStructId = 1; // 0保留给null

private void executeStruct(int instruction) {
    int nfields = extractOperand(instruction);
    StructValue struct = new StructValue(nfields);
    structTable.add(struct);
    int structId = nextStructId++;
    push(structId);  // 压入结构体ID（兼容现有代码）
}

private void executeFload(int instruction) {
    int fieldOffset = extractOperand(instruction);
    int structId = pop();
    StructValue struct = structTable.get(structId - 1); // ID到索引转换
    Object value = struct.getField(fieldOffset);
    push(valueToInt(value));  // 值类型转换
}

private void executeFstore(int instruction) {
    int fieldOffset = extractOperand(instruction);
    int value = pop();
    int structId = pop();
    StructValue struct = structTable.get(structId - 1);
    struct.setField(fieldOffset, intToValue(value));
}
```

#### 3. VMInterpreter适配层
```java
// 直接使用StructValue替换StructSpace
case BytecodeDefinition.INSTR_STRUCT:
    int nfields = getIntOperand();
    operands[++sp] = new StructValue(nfields);
    break;

case BytecodeDefinition.INSTR_FLOAD:
    StructValue struct = (StructValue) operands[sp--];
    int fieldOffset = getIntOperand();
    operands[++sp] = struct.getField(fieldOffset);
    break;
```

#### 4. 类型系统集成（从ep20复制）
- `StructType.java`：结构体类型定义，添加字段偏移计算
- `StructSymbol.java`：结构体符号表项
- `Type.java`、`Symbol.java`：基类接口

#### 5. 嵌套struct支持
```java
public class StructType implements Type {
    // 添加嵌套支持
    public boolean hasNestedStruct(String fieldName) {
        Symbol field = fields.get(fieldName);
        return field != null && field.getType() instanceof StructType;
    }

    // 计算嵌套结构体的内存布局
    public Map<String, Integer> calculateNestedOffsets() {
        Map<String, Integer> offsets = new HashMap<>();
        int currentOffset = 0;
        for (Map.Entry<String, Symbol> entry : fields.entrySet()) {
            offsets.put(entry.getKey(), currentOffset);
            Type fieldType = entry.getValue().getType();
            if (fieldType instanceof StructType) {
                currentOffset += ((StructType) fieldType).getTotalSize();
            } else {
                currentOffset += getTypeSize(fieldType);
            }
        }
        return offsets;
    }
}
```

### 实现步骤

#### 阶段1：统一结构体表示（3-4天）
**目标**：创建StructValue作为统一运行时表示

1. **创建StructValue类**（`ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructValue.java`）
   - 继承或替换现有StructSpace
   - 实现基本字段访问方法

2. **修改VMInterpreter使用StructValue**
   - 替换所有StructSpace引用
   - 更新struct指令处理逻辑
   - 验证现有测试通过

3. **修改CymbolStackVM适配StructValue**
   - 添加structTable管理结构体实例
   - 实现结构体ID映射机制
   - 修改executeStruct/Fload/Fstore方法
   - 验证现有测试通过

4. **统一测试验证**
   - 确保两个VM实现行为一致
   - 验证所有现有测试通过

#### 阶段2：集成ep20类型系统（2-3天）
**目标**：引入类型信息，为类型安全做准备

1. **复制类型系统组件**
   - 从ep20复制StructType、StructSymbol及相关基类到ep18
   - 位置：`ep18/src/main/java/org/teachfx/antlr4/ep18/symtab/`

2. **增强ByteCodeAssembler**
   - 在常量池中支持结构体类型描述符
   - 扩展STRUCT指令支持可选类型索引

3. **增强StructValue类型感知**
   - 添加类型字段和构造方法
   - 实现基于字段名的访问方法

4. **测试类型系统集成**
   - 验证类型信息加载正确性
   - 确保向后兼容性

#### 阶段3：功能增强（2-3天）
**目标**：实现类型安全、嵌套支持和字段映射

1. **类型安全字段访问**
   - 在executeFload/Fstore中添加运行时类型检查
   - 实现类型验证异常
   - 添加边界检查

2. **嵌套结构体支持**
   - 扩展StructType支持嵌套类型
   - 实现递归内存布局计算
   - 修改字段访问逻辑处理嵌套路径

3. **字段名映射支持**
   - 添加字段名到偏移量的映射表
   - 为调试和错误信息提供支持
   - 实现按字段名访问的API

4. **全面测试**
   - 新功能单元测试
   - 集成测试验证功能完整性
   - 性能基准测试

### 关键文件修改清单

#### 新增文件
1. `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructValue.java`
   - 统一结构体运行时表示
   - 包含字段数组和类型信息
   - 支持基于偏移和字段名的访问

2. `ep18/src/main/java/org/teachfx/antlr4/ep18/symtab/` - 类型系统包（从ep20复制并适配）
   - `type/Type.java`
   - `type/StructType.java`（增强：添加字段偏移计算）
   - `type/TypeTable.java`
   - `symbol/Symbol.java`
   - `symbol/StructSymbol.java`

3. `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructLayout.java`（可选）
   - 结构体内存布局计算
   - 支持嵌套结构体布局

#### 修改文件
1. `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/StructSpace.java`
   - **选项A**：重命名为StructValue并增强功能
   - **选项B**：保留为兼容层，内部使用StructValue
   - 建议采用选项A，简化架构

2. `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/CymbolStackVM.java`
   - 添加`structTable: List<StructValue>`管理结构体实例
   - 修改`executeStruct()`：创建StructValue，分配ID
   - 修改`executeFload()`/`executeFstore()`：通过ID查找StructValue
   - 添加值类型转换方法（int/Object转换）

3. `ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java`
   - 替换所有`StructSpace`为`StructValue`
   - 更新struct指令处理逻辑
   - 保持Object[]操作数栈不变

4. `ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/ByteCodeAssembler.java`
   - 增强支持结构体类型描述符
   - 扩展STRUCT指令编码支持类型信息

5. `ep18/src/test/java/org/teachfx/antlr4/ep18/CymbolStackVMTest.java`
   - 更新`testStructCreationAndFieldAccess()`测试
   - 添加类型安全测试用例
   - 验证ID映射正确性

6. `ep18/src/test/java/org/teachfx/antlr4/ep18/VMInterpreterTest.java`
   - 启用并修复`testStructCreationAndFieldAccess()`测试
   - 添加嵌套struct测试
   - 验证两个VM实现一致性

### 测试策略

#### 保持向后兼容性
1. **现有测试验证**：确保所有现有测试通过
2. **字节码兼容性测试**：运行现有的.vm程序验证功能
3. **API兼容性测试**：验证公开API不变

#### 新功能测试
1. **类型安全测试**：验证类型检查正确性
2. **字段映射测试**：测试字段名到偏移的映射
3. **嵌套struct测试**：验证嵌套结构体功能
4. **边界条件测试**：越界访问、空指针等
5. **性能测试**：验证新实现不影响性能

### 风险评估与缓解

#### 风险1：破坏现有功能
- **缓解**：分阶段实施，每个阶段完成后运行完整测试套件
- **缓解**：保持向后兼容模式，逐步迁移

#### 风险2：性能下降
- **缓解**：添加性能基准测试，监控关键指标
- **缓解**：优化类型检查，避免不必要的开销

#### 风险3：复杂度增加
- **缓解**：清晰的架构分层，保持代码可维护性
- **缓解**：充分文档和注释

### 验收标准
1. ✅ 所有现有测试通过
2. ✅ 现有.vm程序正常运行
3. ✅ 公开API保持不变
4. ✅ 类型系统与ep20兼容
5. ✅ 支持字段名映射
6. ✅ 支持嵌套struct（可选）
7. ✅ 运行时类型检查有效

### 文档更新
本规划文档已保存到`ep18/docs/struct-unification-plan.md`，后续实施过程中应更新：
- 实施进度记录
- 遇到的问题和解决方案
- 测试结果和性能数据