#!/bin/bash
# ANTLR4 Compiler Project Build Script
# 跨平台编译器开发构建脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印彩色消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    print_info "Checking dependencies..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed"
        exit 1
    fi
    
    # 检查ANTLR4
    if ! command -v antlr4 &> /dev/null; then
        print_warning "ANTLR4 not found in PATH, will use Maven dependencies"
    fi
    
    print_success "Dependencies check passed"
}

# 生成ANTLR4代码
generate_sources() {
    local module=${1:-"ep20"}
    print_info "Generating ANTLR4 sources for $module..."
    
    cd "$module" || exit 1
    
    mvn generate-sources -DskipTests
    
    if [ $? -eq 0 ]; then
        print_success "Sources generated successfully for $module"
    else
        print_error "Failed to generate sources for $module"
        exit 1
    fi
    
    cd ..
}

# 编译项目
compile() {
    local module=${1:-"ep20"}
    print_info "Compiling $module..."
    
    cd "$module" || exit 1
    
    mvn compile
    
    if [ $? -eq 0 ]; then
        print_success "$module compiled successfully"
    else
        print_error "Failed to compile $module"
        exit 1
    fi
    
    cd ..
}

# 运行测试
test() {
    local module=${1:-"ep20"}
    local test_pattern=${2:-"*"}
    
    print_info "Running tests for $module (pattern: $test_pattern)..."
    
    cd "$module" || exit 1
    
    if [ "$test_pattern" = "*" ]; then
        mvn test
    else
        mvn test -Dtest="$test_pattern"
    fi
    
    if [ $? -eq 0 ]; then
        print_success "Tests passed for $module"
    else
        print_error "Tests failed for $module"
        exit 1
    fi
    
    cd ..
}

# 运行编译器
run_compiler() {
    local module=${1:-"ep20"}
    local input_file=${2:-"program.cymbol"}
    
    if [ ! -f "$input_file" ]; then
        print_error "Input file '$input_file' not found"
        exit 1
    fi
    
    print_info "Running compiler ($module) on $input_file..."
    
    cd "$module" || exit 1
    
    mvn compile exec:java -Dexec.args="$input_file"
    
    if [ $? -eq 0 ]; then
        print_success "Compiler executed successfully"
    else
        print_error "Compiler execution failed"
        exit 1
    fi
    
    cd ..
}

# 运行虚拟机
run_vm() {
    local module=${1:-"ep18"}
    local input_file=${2:-"program.cymbol"}
    
    print_info "Running virtual machine ($module) on $input_file..."
    
    cd "$module" || exit 1
    
    mvn compile exec:java \
        -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM" \
        -Dexec.args="$input_file"
    
    if [ $? -eq 0 ]; then
        print_success "Virtual machine executed successfully"
    else
        print_error "Virtual machine execution failed"
        exit 1
    fi
    
    cd ..
}

# 清理项目
clean() {
    local module=${1:-"ep20"}
    print_info "Cleaning $module..."
    
    cd "$module" || exit 1
    
    mvn clean
    
    if [ $? -eq 0 ]; then
        print_success "$module cleaned successfully"
    else
        print_error "Failed to clean $module"
        exit 1
    fi
    
    cd ..
}

# 生成覆盖率报告
coverage() {
    local module=${1:-"ep20"}
    print_info "Generating coverage report for $module..."
    
    cd "$module" || exit 1
    
    mvn test jacoco:report
    
    if [ $? -eq 0 ]; then
        print_success "Coverage report generated for $module"
        print_info "Report location: $module/target/site/jacoco/index.html"
    else
        print_error "Failed to generate coverage report"
        exit 1
    fi
    
    cd ..
}

# 显示项目信息
project_info() {
    print_info "ANTLR4 Compiler Project Information"
    echo "===================================="
    
    # 列出EP模块
    print_info "Available EP modules:"
    for dir in ep*/; do
        if [ -d "$dir" ] && [ -f "$dir/pom.xml" ]; then
            echo "  ✅ $dir"
        else
            echo "  ❌ $dir (no pom.xml)"
        fi
    done
    
    # 检查Maven版本
    print_info "Maven version:"
    mvn --version | head -1
    
    # 检查Java版本
    print_info "Java version:"
    java -version 2>&1 | head -1
}

# 显示帮助信息
show_help() {
    echo "ANTLR4 Compiler Project Build Script"
    echo "===================================="
    echo ""
    echo "Usage: $0 <command> [options]"
    echo ""
    echo "Commands:"
    echo "  check                     Check project dependencies"
    echo "  generate [module]         Generate ANTLR4 sources"
    echo "  compile [module]          Compile project"
    echo "  test [module] [pattern]   Run tests"
    echo "  run [module] [file]       Run compiler on file"
    echo "  vm [module] [file]        Run virtual machine"
    echo "  clean [module]            Clean project"
    echo "  coverage [module]         Generate coverage report"
    echo "  info                      Show project information"
    echo "  help                      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 generate ep20          # Generate sources for ep20"
    echo "  $0 test ep20 ArraysTest   # Run specific test"
    echo "  $0 run ep20 program.cymbol # Compile program.cymbol"
    echo "  $0 vm ep18 program.cymbol # Run program in VM"
    echo ""
    echo "Default module: ep20"
}

# 主函数
main() {
    case "${1:-help}" in
        check)
            check_dependencies
            ;;
        generate)
            generate_sources "${2:-ep20}"
            ;;
        compile)
            compile "${2:-ep20}"
            ;;
        test)
            test "${2:-ep20}" "${3:-*}"
            ;;
        run)
            run_compiler "${2:-ep20}" "${3:-program.cymbol}"
            ;;
        vm)
            run_vm "${2:-ep18}" "${3:-program.cymbol}"
            ;;
        clean)
            clean "${2:-ep20}"
            ;;
        coverage)
            coverage "${2:-ep20}"
            ;;
        info)
            project_info
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"