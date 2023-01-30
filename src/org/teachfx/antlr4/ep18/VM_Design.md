# Cymbol的虚拟机设计

## 字节码汇编示例
```asm
.globls 《全局变量的个数》

《函数定义》
.def main《函数名》 args=0《参数个数》 locals=0 《本地变量数目》 ; void main()
    halt
```

## 指令集

> 默认的i前缀指的是有符号32位整数

> 默认的f前缀指的是有符号32位浮点

| 指令 | 格式 | 说明 |
| - | - | - |
| iadd | iadd | 连续从栈顶弹出2个整数，并相加。然后，将结果押入栈顶。|
| isub | isub | 连续从栈顶弹出2个整数，并相减。然后，将结果押入栈顶。|
| iconst| iconst int | 向栈顶压入一个整数 |
| sconst | sconst string | 向栈顶压入一个字符串常量 |
| ret | ret | 函数调用返回到调用点 |
