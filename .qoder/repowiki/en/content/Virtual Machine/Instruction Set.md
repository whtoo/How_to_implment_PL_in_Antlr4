# Instruction Set

<cite>
**Referenced Files in This Document**   
- [BytecodeDefinition.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java)
- [ByteCodeAssembler.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/ByteCodeAssembler.java)
- [VMAssemblerParser.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/parser/VMAssemblerParser.java)
- [VMInterpreter.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java)
- [c.vm](file://ep18/src/main/resources/c.vm)
- [t.vm](file://ep18/src/main/resources/t.vm)
- [VM_Design.md](file://ep18/VM_Design.md)
</cite>

## Table of Contents
1. [Introduction](#introduction)
2. [Instruction Set Architecture](#instruction-set-architecture)
3. [Bytecode Definition and Encoding](#bytecode-definition-and-encoding)
4. [Assembly Process and Syntax](#assembly-process-and-syntax)
5. [Instruction Reference](#instruction-reference)
6. [Assembly File Format](#assembly-file-format)
7. [Encoding Examples](#encoding-examples)
8. [Virtual Machine Execution Model](#virtual-machine-execution-model)

## Introduction
This document provides comprehensive documentation for the virtual machine's instruction set architecture. It details the complete set of bytecode operations, their encoding, and execution semantics. The virtual machine implements a stack-based architecture with a rich instruction set supporting arithmetic operations, control flow, function calls, and memory management. The documentation covers the definition of instructions in BytecodeDefinition.java, the assembly process through ByteCodeAssembler.java, and the mapping from human-readable assembly to binary bytecode. Two example programs (c.vm and t.vm) demonstrate the practical application of the instruction set for complex computational tasks involving function calls, loops, and conditional branching.

## Instruction Set Architecture
The virtual machine implements a stack-based architecture where operations manipulate values on an operand stack. Each instruction consists of an opcode followed by zero or more operands, with operands encoded as 4-byte signed integers. The instruction set is designed to support a wide range of operations including arithmetic computations, logical operations, control flow, function calls, and memory access. The architecture follows a register-indirect addressing model for local and global variables, using integer indices to reference locations in the current stack frame or global memory space. The virtual machine maintains separate memory regions for code, data, and stack, with the stack growing downward during function calls. The execution model is based on a fetch-decode-execute cycle where the program counter advances through the bytecode stream, dispatching to appropriate handlers for each opcode.

**Section sources**
- [VM_Design.md](file://ep18/VM_Design.md#L1-L130)
- [VMInterpreter.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java#L1-L424)

## Bytecode Definition and Encoding
The complete set of supported instructions is defined in BytecodeDefinition.java, which establishes the opcode mapping and operand types for each bytecode operation. Each instruction is assigned a unique 16-bit opcode value, with the instruction set organized to accommodate future expansion. The encoding scheme uses variable-length instructions where the opcode is stored as a single byte, followed by zero or more 4-byte operands depending on the instruction type. Operand types are categorized as immediate integers (INT), constant pool references (POOL), function symbols (FUNC), or register identifiers (REG). The Instruction class within BytecodeDefinition encapsulates the metadata for each opcode, including its mnemonic name and the expected types of its operands. This metadata is used by the assembler to validate operand types during the assembly process and by the disassembler to properly interpret binary bytecode.

```mermaid
classDiagram
class BytecodeDefinition {
+short INSTR_IADD
+short INSTR_ISUB
+short INSTR_IMUL
+short INSTR_IDIV
+short INSTR_ILT
+short INSTR_ILE
+short INSTR_IGT
+short INSTR_IGE
+short INSTR_IEQ
+short INSTR_INE
+short INSTR_INEG
+short INSTR_INOT
+short INSTR_IAND
+short INSTR_IOR
+short INSTR_IXOR
+short INSTR_FADD
+short INSTR_FSUB
+short INSTR_FMUL
+short INSTR_FLT
+short INSTR_FEQ
+short INSTR_ITOF
+short INSTR_CALL
+short INSTR_RET
+short INSTR_BR
+short INSTR_BRT
+short INSTR_BRF
+short INSTR_CCONST
+short INSTR_ICONST
+short INSTR_FCONST
+short INSTR_SCONST
+short INSTR_LOAD
+short INSTR_GLOAD
+short INSTR_FLOAD
+short INSTR_STORE
+short INSTR_GSTORE
+short INSTR_FSTORE
+short INSTR_PRINT
+short INSTR_STRUCT
+short INSTR_NULL
+short INSTR_POP
+short INSTR_HALT
+Instruction[] instructions
}
class Instruction {
+String name
+int[] type
+int n
+Instruction(String name)
+Instruction(String name, int a)
+Instruction(String name, int a, int b)
+Instruction(String name, int a, int b, int c)
}
BytecodeDefinition "1" -- "41" Instruction : contains
```

**Diagram sources**
- [BytecodeDefinition.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java#L1-L137)

**Section sources**
- [BytecodeDefinition.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java#L1-L137)

## Assembly Process and Syntax
The assembly process is implemented by ByteCodeAssembler.java, which translates human-readable assembly code into binary bytecode. The assembler operates as an ANTLR4 parse tree listener, processing the abstract syntax tree generated from the assembly source. It maintains several key data structures during assembly: a byte array for the generated machine code, a constant pool for literals and symbols, a map of instruction opcodes, and a symbol table for labels and functions. The assembly process involves several phases: first, instruction mnemonics are mapped to their corresponding opcodes using a lookup table initialized from BytecodeDefinition.instructions; second, operands are encoded according to their type, with integers and characters converted directly, while strings, floats, and symbols are stored in the constant pool and referenced by index; third, forward references to labels are resolved by patching jump targets after all labels have been defined. The VMAssemblerParser defines the grammar for the assembly language, supporting directives like .def for function declarations, labels ending with a colon, and instruction lines with optional operands separated by commas.

```mermaid
sequenceDiagram
participant Source as Assembly Source
participant Parser as VMAssemblerParser
participant Assembler as ByteCodeAssembler
participant Code as Machine Code
Source->>Parser : Read .vm file
Parser->>Parser : Tokenize input
Parser->>Parser : Build parse tree
Parser->>Assembler : Walk parse tree
Assembler->>Assembler : Process globals directive
Assembler->>Assembler : Process function declarations
Assembler->>Assembler : Process instructions
Assembler->>Assembler : Process labels
Assembler->>Code : Generate bytecode
Code->>Code : Resolve forward references
Code-->>Source : Return binary bytecode
```

**Diagram sources**
- [ByteCodeAssembler.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/ByteCodeAssembler.java#L1-L271)
- [VMAssemblerParser.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/parser/VMAssemblerParser.java#L1-L614)

**Section sources**
- [ByteCodeAssembler.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/ByteCodeAssembler.java#L1-L271)

## Instruction Reference
The virtual machine supports 41 distinct bytecode instructions categorized into arithmetic, logical, control flow, memory access, and special operations. Each instruction is identified by a unique opcode and may accept zero to three operands. The following table provides a complete reference of all instructions, including their opcode, operand types, and semantic meaning.

```mermaid
flowchart TD
A[Instruction Categories] --> B[Arithmetic Operations]
A --> C[Logical Operations]
A --> D[Control Flow]
A --> E[Memory Access]
A --> F[Special Operations]
B --> B1[iadd, isub, imul, idiv]
B --> B2[fadd, fsub, fmul]
B --> B3[itof]
C --> C1[ilt, ile, igt, ige, ieq, ine]
C --> C2[flt, feq]
C --> C3[ineg, inot, iand, ior, ixor]
D --> D1[call, ret]
D --> D2[br, brt, brf]
D --> D3[halt]
E --> E1[load, store]
E --> E2[gload, gstore]
E --> E3[fload, fstore]
F --> F1[iconst, fconst, sconst]
F --> F2[print, pop, null]
F --> F3[struct]
```

**Diagram sources**
- [BytecodeDefinition.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java#L1-L137)

**Section sources**
- [BytecodeDefinition.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/BytecodeDefinition.java#L1-L137)

### Complete Instruction Set Reference

| Opcode | Mnemonic | Operands | Description |
|--------|----------|---------|-------------|
| 1 | iadd | - | Integer addition: pops two integers, pushes their sum |
| 2 | isub | - | Integer subtraction: pops two integers, pushes their difference |
| 3 | imul | - | Integer multiplication: pops two integers, pushes their product |
| 4 | idiv | - | Integer division: pops two integers, pushes their quotient |
| 5 | ilt | - | Integer less than: pops two integers, pushes boolean result |
| 6 | ile | - | Integer less or equal: pops two integers, pushes boolean result |
| 7 | igt | - | Integer greater than: pops two integers, pushes boolean result |
| 8 | ige | - | Integer greater or equal: pops two integers, pushes boolean result |
| 9 | ieq | - | Integer equal: pops two integers, pushes boolean result |
| 10 | ine | - | Integer not equal: pops two integers, pushes boolean result |
| 11 | ineg | - | Integer negate: pops one integer, pushes its negation |
| 12 | inot | - | Integer not: pops one boolean, pushes its logical negation |
| 13 | iand | - | Integer and: pops two booleans, pushes their logical AND |
| 14 | ior | - | Integer or: pops two booleans, pushes their logical OR |
| 15 | ixor | - | Integer xor: pops two booleans, pushes their logical XOR |
| 16 | fadd | - | Float addition: pops two floats, pushes their sum |
| 17 | fsub | - | Float subtraction: pops two floats, pushes their difference |
| 18 | fmul | - | Float multiplication: pops two floats, pushes their product |
| 19 | flt | - | Float less than: pops two floats, pushes boolean result |
| 20 | feq | - | Float equal: pops two floats, pushes boolean result |
| 21 | itof | - | Integer to float: pops one integer, pushes its float conversion |
| 22 | call | FUNC | Call function: calls function at specified constant pool index |
| 23 | ret | - | Return from function: returns to caller |
| 24 | br | INT | Branch unconditionally to address |
| 25 | brt | INT | Branch if true: pops boolean, branches if true |
| 26 | brf | INT | Branch if false: pops boolean, branches if false |
| 27 | cconst | INT | Push character constant |
| 28 | iconst | INT | Push integer constant |
| 29 | fconst | POOL | Push float constant from constant pool |
| 30 | sconst | POOL | Push string constant from constant pool |
| 31 | load | INT | Load from local variable |
| 32 | gload | INT | Load from global variable |
| 33 | fload | INT | Load from struct field |
| 34 | store | INT | Store to local variable |
| 35 | gstore | INT | Store to global variable |
| 36 | fstore | INT | Store to struct field |
| 37 | print | - | Print top of stack |
| 38 | struct | INT | Create struct with specified number of fields |
| 39 | null | - | Push null reference |
| 40 | pop | - | Pop and discard top of stack |
| 41 | halt | - | Halt execution |

## Assembly File Format
The virtual machine assembly files (.vm files) follow a structured format that begins with optional global data declarations followed by function definitions and instructions. The file format supports several key elements: the .globals directive specifies the size of global memory, the .def directive declares a function with its parameter and local variable counts, labels provide symbolic names for instruction addresses, and instruction lines contain opcodes with optional operands. Comments are supported using semicolon (;) and extend to the end of the line. The assembly syntax is line-oriented, with each instruction occupying a separate line. Operands are separated by commas when multiple operands are present. The assembler processes the file sequentially, first collecting all symbol definitions and then generating the corresponding bytecode. Forward references to labels are allowed and resolved during the assembly process. The constant pool is built dynamically as literals and symbols are encountered, with duplicate entries eliminated.

```mermaid
erDiagram
ASSEMBLY_FILE {
string globals_size
array functions
array instructions
array labels
array comments
}
FUNCTION {
string name
int args
int locals
array instructions
}
INSTRUCTION {
string opcode
array operands
int address
}
LABEL {
string name
int address
}
CONSTANT_POOL {
int index
object value
}
ASSEMBLY_FILE ||--o{ FUNCTION : contains
ASSEMBLY_FILE ||--o{ INSTRUCTION : contains
ASSEMBLY_FILE ||--o{ LABEL : contains
ASSEMBLY_FILE ||--o{ CONSTANT_POOL : uses
FUNCTION ||--o{ INSTRUCTION : contains
```

**Diagram sources**
- [VM_Design.md](file://ep18/VM_Design.md#L1-L130)
- [c.vm](file://ep18/src/main/resources/c.vm#L1-L29)
- [t.vm](file://ep18/src/main/resources/t.vm#L1-L40)

**Section sources**
- [c.vm](file://ep18/src/main/resources/c.vm#L1-L29)
- [t.vm](file://ep18/src/main/resources/t.vm#L1-L40)

## Encoding Examples
The encoding process transforms human-readable assembly into binary bytecode according to the rules defined in BytecodeDefinition.java and implemented in ByteCodeAssembler.java. For arithmetic operations, the encoding is straightforward: the opcode is written as a single byte, followed by any operands as 4-byte integers. For example, the instruction "iconst 10" is encoded as byte 28 (the opcode for iconst) followed by the 4-byte representation of integer 10. Control flow instructions use symbolic labels that are resolved to absolute addresses during assembly. Function calls encode the function name as an index into the constant pool, where the function symbol is stored. The following examples demonstrate the encoding of common operations:

**Arithmetic Operation Example:**
```assembly
iconst 10
iconst 20
iadd
```
This sequence pushes two integers onto the stack and adds them. The encoding would be: 28 (iconst) + 4-byte 10 + 28 (iconst) + 4-byte 20 + 1 (iadd).

**Control Flow Example:**
```assembly
load 0
iconst 0
igt
brf L6
```
This sequence loads a variable, compares it with zero, and branches if it's not greater. The encoding would resolve the label L6 to an absolute address during assembly.

**Function Call Example:**
```assembly
.def f: args=2, locals=1
    load 0
    load 1
    call ck()
    ret
```
This function definition shows how parameters are accessed via load instructions and how function calls are made using the call instruction with a function symbol.

**Section sources**
- [c.vm](file://ep18/src/main/resources/c.vm#L1-L29)
- [t.vm](file://ep18/src/main/resources/t.vm#L1-L40)

## Virtual Machine Execution Model
The virtual machine executes bytecode through a fetch-decode-execute cycle implemented in VMInterpreter.java. The execution model maintains several key components: the program counter (ip) that points to the current instruction, the operand stack (operands) that stores values being manipulated, the call stack (calls) that manages function invocations, and the constant pool (constPool) that stores literals and symbols. When executing an instruction, the virtual machine first fetches the opcode from the code memory at the current program counter position, then decodes the instruction to determine its operation and operand count, and finally executes the corresponding operation. Arithmetic and logical operations pop their operands from the stack, perform the computation, and push the result back onto the stack. Memory access instructions use integer indices to reference locations in the current stack frame or global memory. Control flow instructions modify the program counter to implement branching and function calls. The execution continues until the halt instruction is encountered or an error occurs.

```mermaid
stateDiagram-v2
[*] --> Fetch
Fetch --> Decode : Read opcode
Decode --> Execute : Determine operation
Execute --> Fetch : Advance PC
Execute --> Halt : On halt instruction
Execute --> Error : On invalid opcode
Fetch --> Halt : PC >= codeSize
Error --> [*]
Halt --> [*]
state Execute {
[*] --> Arithmetic
[*] --> Logical
[*] --> ControlFlow
[*] --> MemoryAccess
[*] --> Special
Arithmetic --> Fetch : iadd, isub, etc.
Logical --> Fetch : iand, ior, etc.
ControlFlow --> Fetch : br, call, ret
MemoryAccess --> Fetch : load, store, etc.
Special --> Fetch : print, pop, etc.
}
```

**Diagram sources**
- [VMInterpreter.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java#L1-L424)

**Section sources**
- [VMInterpreter.java](file://ep18/src/main/java/org/teachfx/antlr4/ep18/VMInterpreter.java#L1-L424)