# Cymbol的虚拟机设计

## 字节码汇编示例

```asm
.globls 《全局变量的个数》

《函数定义》
.def main《函数名》 args=0《参数个数》 locals=0 《本地变量数目》 ; void main()
    xxx
    xxx
    xx...
    halt
    
```

## 指令集

> 默认的i前缀指的是有符号32位整数

> 默认的f前缀指的是有符号32位浮点

| 指令     | 格式                      | 说明                                  |
|--------|-------------------------|-------------------------------------|
| imult  | imult                   | 连续从栈顶弹出2个整数，并相乘。然后，将结果押入栈顶。         |
| idiv   | idiv                    | 连续从栈顶弹出2个整数，并相除。然后，将结果押入栈顶。         |
| iadd   | iadd                    | 连续从栈顶弹出2个整数，并相加。然后，将结果押入栈顶。         |
| isub   | isub                    | 连续从栈顶弹出2个整数，并相减。然后，将结果押入栈顶。         |
| iconst | iconst int              | 向栈顶压入一个整数                           |
| fmult  | fmult                   | 连续从栈顶弹出2个整数，并相乘。然后，将结果押入栈顶。         |
| fdiv   | fdiv                    | 连续从栈顶弹出2个整数，并相除。然后，将结果押入栈顶。         |
| fadd   | fadd                    | 连续从栈顶弹出2个整数，并相加。然后，将结果押入栈顶。         |
| fsub   | fsub                    | 连续从栈顶弹出2个整数，并相减。然后，将结果押入栈顶。         |
| fconst | fconst float            | 向栈顶压入一个字符串常量                        |
| sconst | sconst string           | 向栈顶压入一个字符串常量                        |
| ret    | ret                     | 函数调用返回到调用点                          |
| gload  | gload operands-Index    |                                     |
| gstore | gstore   operands-Index |                                     |
| iload  | load operands-Index     | 将栈顶元素弹出然后写入Frame的`operands-Index`位置 |
| istore | store   operands-Index  | 将Frame的`operands-Index`位置元素压入栈      |
| br     | br   pc                 | 无条件跳转到`pc`指向的指令位置                   |
| brt    | br   code-addr          | 当操作数栈顶为`true`时，跳转到`pc`指向的指令位置       |
| brf    | br code-addr            | 当操作数栈顶为`false`时，跳转到`pc`指向的指令位置      |
| call   | call funcname'()'       | 调用`funcname`                        |
| halt   | halt                    | 程序终止                                |



