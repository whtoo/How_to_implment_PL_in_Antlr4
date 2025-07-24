package org.teachfx.antlr4.ep20.pass.codegen;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("端到端编译测试")
public class EndToEndCompilationTest {
    @BeforeEach
    public void setUp() {
        // 不需要初始化Compiler实例，因为我们直接调用静态方法
    }

    @Test
    @DisplayName("应成功编译简单程序并生成正确的汇编代码")
    public void testCompileSimpleProgram() {
        // Arrange
        String sourceCode = """
            int main() {
                int x = 42;
                return x;
            }
            """;
            
        // Act & Assert
        assertDoesNotThrow(() -> {
            CymbolAssembler assembler = compileSource(sourceCode);
            String asmCode = assembler.getAsmInfo();
            
            // Assert
            assertNotNull(asmCode);
            assertFalse(asmCode.isEmpty());
            assertTrue(asmCode.contains("main"));
            assertTrue(asmCode.contains("iconst 42"));
            assertTrue(asmCode.contains("store"));
            assertTrue(asmCode.contains("halt"));
        });
    }
    
    @Test
    @DisplayName("应成功编译带函数调用的程序")
    public void testCompileProgramWithFunctionCall() {
        // Arrange
        String sourceCode = """
            int add(int a, int b) {
                return a + b;
            }
            
            int main() {
                int result = add(1, 2);
                return result;
            }
            """;
            
        // Act & Assert
        assertDoesNotThrow(() -> {
            CymbolAssembler assembler = compileSource(sourceCode);
            String asmCode = assembler.getAsmInfo();
            
            // Assert
            assertNotNull(asmCode);
            assertFalse(asmCode.isEmpty());
            assertTrue(asmCode.contains("add:"));
            assertTrue(asmCode.contains("main:"));
            assertTrue(asmCode.contains("call add()"));
        });
    }
    
    @Test
    @DisplayName("应成功编译带条件语句的程序")
    public void testCompileProgramWithConditional() {
        // Arrange
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
            
        // Act & Assert
        assertDoesNotThrow(() -> {
            CymbolAssembler assembler = compileSource(sourceCode);
            String asmCode = assembler.getAsmInfo();
            
            // Assert
            assertNotNull(asmCode);
            assertFalse(asmCode.isEmpty());
            assertTrue(asmCode.contains("main"));
            assertTrue(asmCode.contains("iconst 10"));
            assertTrue(asmCode.contains("igt")); // 比较指令 (greater than)
            assertTrue(asmCode.contains("brf")); // 条件跳转指令
        });
    }
    
    @Test
    @DisplayName("应成功编译带循环语句的程序")
    public void testCompileProgramWithLoop() {
        // Arrange
        String sourceCode = """
            int main() {
                int i = 0;
                while (i < 10) {
                    i = i + 1;
                }
                return i;
            }
            """;
            
        // Act & Assert
        assertDoesNotThrow(() -> {
            CymbolAssembler assembler = compileSource(sourceCode);
            String asmCode = assembler.getAsmInfo();
            
            // Assert
            assertNotNull(asmCode);
            assertFalse(asmCode.isEmpty());
            assertTrue(asmCode.contains("main:"));
            assertTrue(asmCode.contains("iconst 0"));
            assertTrue(asmCode.contains("ilt")); // 比较指令
            assertTrue(asmCode.contains("brf")); // 条件跳转指令
            assertTrue(asmCode.contains("br")); // 无条件跳转指令
        });
    }
    
    @Test
    @DisplayName("应成功编译复杂程序")
    public void testCompileComplexProgram() {
        // Arrange
        String sourceCode = """
            int factorial(int n) {
                if (n <= 1) {
                    return 1;
                } else {
                    return n * factorial(n - 1);
                }
            }
            
            int main() {
                int result = factorial(5);
                return result;
            }
            """;
            
        // Act & Assert
        assertDoesNotThrow(() -> {
            CymbolAssembler assembler = compileSource(sourceCode);
            String asmCode = assembler.getAsmInfo();
            
            // Assert
            assertNotNull(asmCode);
            assertFalse(asmCode.isEmpty());
            assertTrue(asmCode.contains("factorial:"));
            assertTrue(asmCode.contains("main:"));
            assertTrue(asmCode.contains("call factorial()"));
            assertTrue(asmCode.contains("imul")); // 乘法指令
            assertTrue(asmCode.contains("isub")); // 减法指令
        });
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