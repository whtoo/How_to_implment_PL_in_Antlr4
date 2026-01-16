package org.teachfx.antlr4.ep18r;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.stackvm.RegisterByteCodeAssembler;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import static org.assertj.core.api.Assertions.*;

@DisplayName("汇编器/反汇编器对齐性测试")
class DisassemblerAlignmentTest {

    @Test
    @DisplayName("R类型指令：add r3, r1, r2 应该正确编解码")
    void testRTypeInstructionEncoding() throws Exception {
        String asm = """
            .def main: args=0, locals=0
                add r3, r1, r2
                halt
            """;

        VMAssemblerLexer lexer = new VMAssemblerLexer(CharStreams.fromString(asm));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
        ParseTree parseTree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        RegisterByteCodeAssembler assembler = new RegisterByteCodeAssembler(RegisterBytecodeDefinition.instructions);
        walker.walk(assembler, parseTree);

        byte[] code = assembler.getMachineCode();
        Object[] constPool = assembler.getConstantPool();

        RegisterDisAssembler disassembler = new RegisterDisAssembler(code, code.length, constPool);
        String disassembled = disassembler.disassembleToString();

        System.out.println("=== 反汇编结果 ===");
        System.out.println(disassembled);

        assertThat(disassembled).contains("add");
    }

    @Test
    @DisplayName("I类型指令：li r1, 100 应该正确编解码")
    void testITypeInstructionEncoding() throws Exception {
        String asm = """
            .def main: args=0, locals=0
                li r1, 100
                halt
            """;

        VMAssemblerLexer lexer = new VMAssemblerLexer(CharStreams.fromString(asm));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
        ParseTree parseTree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        RegisterByteCodeAssembler assembler = new RegisterByteCodeAssembler(RegisterBytecodeDefinition.instructions);
        walker.walk(assembler, parseTree);

        byte[] code = assembler.getMachineCode();
        Object[] constPool = assembler.getConstantPool();

        RegisterDisAssembler disassembler = new RegisterDisAssembler(code, code.length, constPool);
        String disassembled = disassembler.disassembleToString();

        System.out.println("=== 反汇编结果 ===");
        System.out.println(disassembled);

        assertThat(disassembled).contains("li").contains("r1").contains("100");
    }

    @Test
    @DisplayName("条件跳转指令：jt r1, label 应该正确编解码")
    void testConditionalJumpEncoding() throws Exception {
        String asm = """
            .def main: args=0, locals=0
                li r1, 1
                jt r1, skip
                li r2, 999
            skip:
                halt
            """;

        VMAssemblerLexer lexer = new VMAssemblerLexer(CharStreams.fromString(asm));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        VMAssemblerParser parser = new VMAssemblerParser(tokenStream);
        ParseTree parseTree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        RegisterByteCodeAssembler assembler = new RegisterByteCodeAssembler(RegisterBytecodeDefinition.instructions);
        walker.walk(assembler, parseTree);

        byte[] code = assembler.getMachineCode();
        Object[] constPool = assembler.getConstantPool();

        RegisterDisAssembler disassembler = new RegisterDisAssembler(code, code.length, constPool);
        String disassembled = disassembler.disassembleToString();

        System.out.println("=== 反汇编结果 ===");
        System.out.println(disassembled);

        assertThat(disassembled).contains("jt");
    }

    @Test
    @DisplayName("验证指令字编码：操作码在bits 31-26")
    void testOpcodeBitPosition() {
        int expectedInstruction = (1 << 26) | (3 << 21) | (1 << 16) | (2 << 11);
        System.out.printf("期望的指令字: 0x%08x\n", expectedInstruction);

        byte[] code = new byte[4];
        code[0] = (byte) ((expectedInstruction >> 24) & 0xFF);
        code[1] = (byte) ((expectedInstruction >> 16) & 0xFF);
        code[2] = (byte) ((expectedInstruction >> 8) & 0xFF);
        code[3] = (byte) (expectedInstruction & 0xFF);

        System.out.printf("字节码: [0x%02x, 0x%02x, 0x%02x, 0x%02x]\n",
            code[0], code[1], code[2], code[3]);

        RegisterDisAssembler disassembler = new RegisterDisAssembler(code, 4, new Object[0]);
        String result = disassembler.disassembleInstructionToString(0);

        System.out.println("反汇编结果: " + result);

        assertThat(result).contains("add");
    }
}
