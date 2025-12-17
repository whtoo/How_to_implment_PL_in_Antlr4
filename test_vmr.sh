#!/bin/bash

# 测试脚本 - 执行所有.vmr文件
cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18r

VMR_FILES=(
    "test_loop.vmr"
    "fib.vmr"
    "mov_test.vmr"
    "neg_test.vmr"
    "sub_test.vmr"
    "c.vmr"
    "t.vmr"
)

echo "=== 开始测试 .vmr 文件 ==="
echo ""

for vmr_file in "${VMR_FILES[@]}"; do
    echo "测试文件: $vmr_file"
    echo "-----------------------------------"

    # 创建临时Java测试文件，类名与文件名匹配
    TEST_CLASS="TestVMR_${vmr_file%.vmr}"
    cat > /tmp/${TEST_CLASS}.java << EOF
import org.teachfx.antlr4.ep18r.stackvm.RegisterVMInterpreter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ${TEST_CLASS} {
    public static void main(String[] args) throws Exception {
        String vmrFile = args[0];
        System.out.println("加载文件: " + vmrFile);

        // 读取VMR文件内容
        String program = new String(Files.readAllBytes(Paths.get(vmrFile)));
        System.out.println("程序内容:");
        System.out.println(program);
        System.out.println("\\n执行结果:");

        RegisterVMInterpreter interpreter = new RegisterVMInterpreter();
        InputStream input = new ByteArrayInputStream(program.getBytes());

        boolean hasErrors = RegisterVMInterpreter.load(interpreter, input);
        if (hasErrors) {
            System.err.println("加载程序时出现错误!");
            System.exit(1);
        }

        try {
            interpreter.exec();
            System.out.println("程序执行完成");
            System.out.println("寄存器状态: r1=" + interpreter.getRegister(1) +
                             ", r2=" + interpreter.getRegister(2));
        } catch (Exception e) {
            System.err.println("执行错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
EOF

    # 编译并运行测试
    cd /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18r
    javac -cp "target/classes:target/test-classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
         /tmp/${TEST_CLASS}.java

    java -cp "/tmp:target/classes:target/test-classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
         ${TEST_CLASS} /Users/blitz/pl-dev/How_to_implment_PL_in_Antlr4/ep18r/src/main/resources/$vmr_file

    echo ""
    echo ""
done

echo "=== 所有测试完成 ==="

# 清理临时文件
rm -f /tmp/TestVMR_*.java /tmp/TestVMR_*.class
