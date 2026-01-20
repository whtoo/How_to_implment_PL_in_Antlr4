import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.teachfx.antlr4.ep21.*;

public class ArrayAnalysis {
    public static void main(String[] args) throws Exception {
        // 测试简单变量声明
        String simpleCode = "int main() { int x; return 0; }";
        
        // 测试数组声明  
        String arrayCode = "int main() { int[5] arr; return 0; }";
        
        System.out.println("=== 测试简单变量声明 ===");
        testCode(simpleCode);
        
        System.out.println("\n=== 测试数组变量声明 ===");
        testCode(arrayCode);
    }
    
    static void testCode(String code) {
        try {
            CharStream input = CharStreams.fromString(code);
            CymbolLexer lexer = new CymbolLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CymbolParser parser = new CymbolParser(tokens);
            
            // 启用诊断
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg, RecognitionException e) {
                    System.err.println("语法错误: line " + line + ":" + charPositionInLine + " " + msg);
                }
            });
            
            ParseTree tree = parser.file();
            System.out.println("解析树: " + tree.toStringTree(parser));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}