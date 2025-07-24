package org.teachfx.antlr4.ep20.pass.codegen;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DebugOutputTest {

    @Test
    public void testDebugSimpleProgramOutput() throws IOException {
        String sourceCode = """
            int main() {
                int x = 42;
                return x;
            }
            """;
        
        CymbolAssembler assembler = compileSource(sourceCode);
        String asmCode = assembler.getAsmInfo();
        
        System.out.println("=== Simple Program Assembly Output ===");
        System.out.println(asmCode);
        System.out.println("=== End of Output ===");
        
        // 验证基本内容存在
        assertNotNull(asmCode);
        assertFalse(asmCode.isEmpty());
        assertTrue(asmCode.contains("main"));
        assertTrue(asmCode.contains("iconst 42"));
    }
    
    @Test
    public void testDebugConditionalProgramOutput() throws IOException {
        String sourceCode = """
            int main() {
                int x = 10;
                if (x > 5) {
                    return 1;
                } else {
                    return 0;
                }
            }
            """;
        
        CymbolAssembler assembler = compileSource(sourceCode);
        String asmCode = assembler.getAsmInfo();
        
        System.out.println("=== Conditional Program Assembly Output ===");
        System.out.println(asmCode);
        System.out.println("=== End of Output ===");
        
        // 验证基本内容存在
        assertNotNull(asmCode);
        assertFalse(asmCode.isEmpty());
        assertTrue(asmCode.contains("main"));
        assertTrue(asmCode.contains("iconst 10"));
    }
    
    /**
     * 编译源代码并返回汇编器
     * @param sourceCode 源代码
     * @return CymbolAssembler 汇编器
     * @throws IOException IO异常
     */
    private CymbolAssembler compileSource(String sourceCode) throws IOException {
        // 创建字符流
        CharStream charStream = CharStreams.fromString(sourceCode);
        
        // 词法分析
        CymbolLexer lexer = new CymbolLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        
        // 语法分析
        CymbolParser parser = new CymbolParser(tokenStream);
        ParseTree parseTree = parser.file();
        
        // 构建AST
        CymbolASTBuilder astBuilder = new CymbolASTBuilder();
        ASTNode astRoot = parseTree.accept(astBuilder);
        
        // 符号表定义
        astRoot.accept(new LocalDefine());
        
        // 构建IR
        CymbolIRBuilder irBuilder = new CymbolIRBuilder();
        astRoot.accept(irBuilder);
        
        // 优化基本块
        irBuilder.prog.optimizeBasicBlock();
        
        // 获取IR节点列表
        List<IRNode> irNodeList = irBuilder.prog.linearInstrs();
        
        // 生成汇编代码
        CymbolAssembler assembler = new CymbolAssembler();
        assembler.visit(irNodeList);
        
        return assembler;
    }
}