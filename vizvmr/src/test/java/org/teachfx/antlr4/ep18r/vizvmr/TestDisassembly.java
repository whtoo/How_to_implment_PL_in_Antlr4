package org.teachfx.antlr4.ep18r.vizvmr;

import org.teachfx.antlr4.ep18r.stackvm.RegisterDisAssembler;
import org.teachfx.antlr4.ep18r.stackvm.interpreter.RegisterVMInterpreter;
import org.teachfx.antlr4.ep18r.stackvm.config.VMConfig;

import java.io.FileInputStream;

public class TestDisassembly {
    public static void main(String[] args) {
        try {
            VMConfig config = new VMConfig.Builder()
                    .setHeapSize(1024 * 1024)
                    .setStackSize(1024)
                    .setMaxStackDepth(100)
                    .setDebugMode(true)
                    .build();

            RegisterVMInterpreter vm = new RegisterVMInterpreter(config);

            FileInputStream fis = new FileInputStream("src/main/resources/fib.vmr");
            boolean hasErrors = RegisterVMInterpreter.load(vm, fis);
            fis.close();

            if (hasErrors) {
                System.out.println("代码加载失败");
                return;
            }

            System.out.println("代码大小: " + vm.getCodeSize());

            byte[] code = vm.getCode();
            int codeSize = vm.getCodeSize();
            Object[] constPool = vm.getConstantPool();

            if (code != null && codeSize > 0) {
                RegisterDisAssembler disAssembler = new RegisterDisAssembler(code, codeSize, constPool);
                String disassembly = disAssembler.disassembleToString();

                System.out.println("反汇编结果:");
                System.out.println(disassembly);

                String[] lines = disassembly.split("\n");
                System.out.println("行数: " + lines.length);
            } else {
                System.out.println("代码为空或大小为0");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
