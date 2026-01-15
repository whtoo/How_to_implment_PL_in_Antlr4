package org.teachfx.antlr4.ep18r;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerLexer;
import org.teachfx.antlr4.ep18r.parser.VMAssemblerParser;
import org.teachfx.antlr4.ep18r.stackvm.RegisterByteCodeAssembler;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.instructions.model.RegisterBytecodeDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VMRunner {

    public static void main(String[] args) throws Exception {
        String fileName = "t.vmr";
        boolean trace = false;
        
        // 解析命令行参数
        for (int i = 0; i < args.length; i++) {
            if ("--trace".equals(args[i])) {
                trace = true;
            } else {
                fileName = args[i];
                // 假设文件名参数后没有其他参数
                break;
            }
        }
        
        if (trace) {
            System.err.println("fileName = " + fileName);
        }
        InputStream is = null;
        try {
            // Try to load from resources first
            is = VMRunner.class.getClassLoader().getResourceAsStream(fileName);
            if (is == null) {
                // Fallback to file system path if not found in resources
                File file = new File(fileName);
                if (file.exists()) {
                    is = new FileInputStream(file);
                } else {
                    System.err.println("Error: File not found in resources or filesystem: " + fileName);
                    System.exit(1);
                    return;
                }
            }
            
            // 创建虚拟机实例并加载程序
            VMConfig config = new VMConfig.Builder().build();
            RegisterVMInterpreter vm = new RegisterVMInterpreter(config);
            vm.setTrace(trace);
            
            boolean hasErrors = RegisterVMInterpreter.load(vm, is);
            if (hasErrors) {
                System.err.println("Errors occurred during assembly. Execution aborted.");
                System.exit(1);
            }
            
            // 执行加载的程序
            vm.exec();
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
        }
    }
}
