#!/bin/bash

# 设置工作目录为项目根目录
cd "$(dirname "$0")/.." || exit 1

# 显示帮助信息
show_help() {
    echo "用法: $0 <命令> <模块名>"
    echo "命令:"
    echo "  compile  - 编译指定模块"
    echo "  run      - 运行指定模块"
    echo "  test     - 运行指定模块的测试"
    echo "  clean    - 清理指定模块的编译文件"
    echo "  help     - 显示此帮助信息"
    echo "模块名:"
    echo "  ep1 到 ep21 中的任意一个，例如: ep1, ep2, ..., ep21"
    echo "示例:"
    echo "  $0 compile ep1  # 编译ep1模块"
    echo "  $0 run ep2      # 运行ep2模块"
    echo "  $0 test ep3     # 运行ep3模块的测试"
}

# 验证模块名是否有效
validate_module() {
    if [[ ! "$1" =~ ^ep[0-9]+$ ]] || [ ! -d "$1" ]; then
        echo "错误: 无效的模块名 '$1'"
        echo "请使用 ep1 到 ep21 之间的有效模块名"
        exit 1
    fi
}

# 主逻辑
if [ "$#" -lt 1 ] || [ "$1" = "help" ]; then
    show_help
    exit 0
fi

command=$1
module=$2

case $command in
    compile)
        if [ -z "$module" ]; then
            echo "错误: 请指定要编译的模块"
            exit 1
        fi
        validate_module "$module"
        echo "正在编译 $module..."
        mvn clean compile -pl "$module"
        ;;
    run)
        if [ -z "$module" ]; then
            echo "错误: 请指定要运行的模块"
            exit 1
        fi
        validate_module "$module"
        echo "正在运行 $module..."
        # 获取除了命令和模块名之外的所有参数
        shift 2
        extra_args="$*"
        mvn compile exec:java -pl "$module" -Dexec.mainClass="$(mvn help:evaluate -Dexpression=run.main.entry -pl "$module" -q -DforceStdout)" -Dexec.args="$extra_args"
        ;;
    test)
        if [ -z "$module" ]; then
            echo "错误: 请指定要测试的模块"
            exit 1
        fi
        validate_module "$module"
        echo "正在运行 $module 的测试..."
        mvn test -pl "$module"
        ;;
    clean)
        if [ -z "$module" ]; then
            echo "错误: 请指定要清理的模块"
            exit 1
        fi
        validate_module "$module"
        echo "正在清理 $module..."
        mvn clean -pl "$module"
        ;;
    *)
        echo "错误: 未知的命令 '$command'"
        show_help
        exit 1
        ;;
esac