#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import subprocess
import re

def show_help(script_name="run.py"):
    """显示帮助信息"""
    print(f"用法: python {script_name} <命令> <模块名>")
    print("命令:")
    print("  compile  - 编译指定模块 (依赖 clean)")
    print("  run      - 运行指定模块")
    print("  test     - 运行指定模块的测试 (依赖 compile)")
    print("  clean    - 清理指定模块的编译文件")
    print("  help     - 显示此帮助信息")
    print("模块名:")
    print("  ep1 到 ep21 中的任意一个，例如: ep1, ep2, ..., ep21")
    print("示例:")
    print(f"  python {script_name} compile ep1  # 编译ep1模块")
    print(f"  python {script_name} run ep2      # 运行ep2模块")
    print(f"  python {script_name} test ep3     # 运行ep3模块的测试")

def validate_module(module_name):
    """验证模块名是否有效"""
    # 检查模块名格式是否正确 (ep + 数字)
    if not re.match(r'^ep\d+$', module_name):
        print(f"错误: 无效的模块名 '{module_name}'")
        print("请使用 ep1 到 ep21 之间的有效模块名")
        sys.exit(1)
    
    # 检查模块目录是否存在
    if not os.path.isdir(module_name):
        print(f"错误: 模块目录 '{module_name}' 不存在")
        sys.exit(1)

def clean_module(module_name):
    """清理模块"""
    print(f"正在清理 {module_name}...")
    subprocess.run(["mvn", "clean", "-pl", module_name], check=True)

def compile_module(module_name):
    """编译模块 (依赖 clean)"""
    # 先清理
    clean_module(module_name)
    print(f"正在编译 {module_name}...")
    subprocess.run(["mvn", "compile", "-pl", module_name], check=True)

def run_module(module_name, extra_args=[]):
    """运行模块"""
    print(f"正在运行 {module_name}...")
    
    # 获取主类名
    result = subprocess.run([
        "mvn", "help:evaluate", 
        "-Dexpression=run.main.entry", 
        "-pl", module_name, 
        "-q", "-DforceStdout"
    ], capture_output=True, text=True, check=True)
    
    main_class = result.stdout.strip()
    
    if not main_class:
        print("错误: 无法获取主类名，请检查模块的pom.xml配置")
        sys.exit(1)
    
    # 构建参数
    cmd = [
        "mvn", "compile", "exec:java", 
        "-pl", module_name, 
        f"-Dexec.mainClass={main_class}"
    ]
    
    if extra_args:
        cmd.append(f"-Dexec.args={' '.join(extra_args)}")
    
    subprocess.run(cmd, check=True)

def test_module(module_name):
    """测试模块"""
    # 先编译 (编译会自动清理)
    compile_module(module_name)
    print(f"正在运行 {module_name} 的测试...")
    subprocess.run(["mvn", "test", "-pl", module_name], check=True)

def main():
    """主逻辑"""
    # 设置工作目录为项目根目录
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.join(script_dir, "..")
    os.chdir(project_root)
    
    # 获取命令行参数
    args = sys.argv[1:]
    
    # 如果没有参数或第一个参数是help，显示帮助信息
    if len(args) < 1 or args[0] == "help":
        show_help(os.path.basename(sys.argv[0]))
        sys.exit(0)
    
    command = args[0]
    module = args[1] if len(args) > 1 else None
    extra_args = args[2:] if len(args) > 2 else []
    
    # 根据命令执行相应操作
    if command == "compile":
        if not module:
            print("错误: 请指定要编译的模块")
            sys.exit(1)
        validate_module(module)
        compile_module(module)
    elif command == "run":
        if not module:
            print("错误: 请指定要运行的模块")
            sys.exit(1)
        validate_module(module)
        run_module(module, extra_args)
    elif command == "test":
        if not module:
            print("错误: 请指定要测试的模块")
            sys.exit(1)
        validate_module(module)
        test_module(module)
    elif command == "clean":
        if not module:
            print("错误: 请指定要清理的模块")
            sys.exit(1)
        validate_module(module)
        clean_module(module)
    else:
        print(f"错误: 未知的命令 '{command}'")
        show_help(os.path.basename(sys.argv[0]))
        sys.exit(1)

if __name__ == "__main__":
    main()