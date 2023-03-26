package org.teachfx.antlr4.ep18.stackvm;

import java.util.ArrayList;
import java.util.List;

public class DisAssembler {
    protected Object[] constPool;
    byte[] code;
    int codeSize;
    BytecodeDefinition def;

    public DisAssembler(byte[] code, int codeSize, Object[] constPool) {
        this.code = code;
        this.codeSize = codeSize;
        this.constPool = constPool;
    }

    public void disassemble() {
        System.out.println("Disassembly:");
        int i = 0;
        while (i < codeSize) {
            i = disassembleInstruction(i);
            System.out.println();
        }
        System.out.println();
    }

    public int disassembleInstruction(int ip) {
        int opcode = code[ip];
        BytecodeDefinition.Instruction I = BytecodeDefinition.instructions[opcode];
        String instrName = I.name;
        System.out.printf("%04d:\t%-11s", ip, instrName);
        ip++;
        if (I.n == 0) {
            System.out.print("  ");
            return ip;
        }
        List<String> operands = new ArrayList<String>();
        for (int i = 0; i < I.n; i++) {
            int opnd = ByteCodeAssembler.getInt(code, ip);
            ip += 4;
            switch (I.type[i]) {
                case BytecodeDefinition.REG:
                    operands.add("r" + opnd);
                    break;
                case BytecodeDefinition.FUNC:
                case BytecodeDefinition.POOL:
                    operands.add(showConstPoolOperand(opnd));
                    break;
                case BytecodeDefinition.INT:
                    operands.add(String.valueOf(opnd));
            }
        }
        for (int i = 0; i < operands.size(); i++) {
            String s = operands.get(i);
            if (i > 0) System.out.print(", ");
            System.out.print(s);
        }
        return ip;
    }

    private String showConstPoolOperand(int poolIndex) {
        StringBuilder buf = new StringBuilder();
        buf.append("#");
        buf.append(poolIndex);
        String s = constPool[poolIndex].toString();
        if (constPool[poolIndex] instanceof String) s = '"' + s + '"';
        else if (constPool[poolIndex] instanceof FunctionSymbol fs) {
            s = fs.name + "()@" + fs.address;
        }
        buf.append(":");
        buf.append(s);

        return buf.toString();
    }
}
