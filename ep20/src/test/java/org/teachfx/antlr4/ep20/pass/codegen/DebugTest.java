package org.teachfx.antlr4.ep20.pass.codegen;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.teachfx.antlr4.ep20.ast.ASTNode;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.parser.CymbolLexer;
import org.teachfx.antlr4.ep20.parser.CymbolParser;
import org.teachfx.antlr4.ep20.pass.ast.CymbolASTBuilder;
import org.teachfx.antlr4.ep20.pass.ir.CymbolIRBuilder;
import org.teachfx.antlr4.ep20.pass.symtab.LocalDefine;

import java.io.IOException;
import java.util.List;

public class DebugTest {
    public static void main(String[] args) throws IOException {
        // Arrange
        String sourceCode = """
            int main() {
                int x = 42;
                return x;
            }
            """;
        
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
        
        // 打印生成的汇编代码
        System.out.println("Generated assembly code:");
        System.out.println(assembler.getAsmInfo());
    }
}