#!/usr/bin/env python3
"""测试tree-sitter解析Java代码"""

import sys
import os
import time
from pathlib import Path

# 尝试导入tree_sitter
try:
    import tree_sitter
    from tree_sitter import Language, Parser
    print("✓ tree_sitter导入成功")
except ImportError as e:
    print(f"✗ 无法导入tree_sitter: {e}")
    sys.exit(1)

# 检查是否安装了tree-sitter-language-pack
try:
    import tree_sitter_language_pack
    print("✓ tree_sitter_language_pack导入成功")

    # 尝试获取Java语言
    # tree_sitter_language_pack可能提供get_language函数
    # 或者通过Language.build_library加载
except ImportError as e:
    print(f"✗ 无法导入tree_sitter_language_pack: {e}")
    print("尝试通过其他方式加载语言...")

# 尝试加载Java语言
def load_java_language():
    """加载Java语言解析器"""
    try:
        # tree_sitter_language_pack可能提供便捷方式
        # 如果没有，我们需要手动查找语言库
        import tree_sitter_language_pack as tspack

        # 检查可用语言
        if hasattr(tspack, 'get_language'):
            java_lang = tspack.get_language('java')
            if java_lang:
                print("✓ 通过tree_sitter_language_pack获取Java语言")
                return java_lang

        # 尝试通过Language.load加载
        # 库文件通常位于包目录中
        import site
        packages = site.getsitepackages()
        for pkg_dir in packages:
            lib_path = os.path.join(pkg_dir, 'tree_sitter_language_pack', 'languages.so')
            if os.path.exists(lib_path):
                java_lang = Language(lib_path, 'java')
                print(f"✓ 从 {lib_path} 加载Java语言")
                return java_lang

    except Exception as e:
        print(f"加载Java语言时出错: {e}")

    return None

# 测试解析Java文件
def test_parse_java_file(file_path):
    """解析Java文件并提取基本信息"""
    if not os.path.exists(file_path):
        print(f"文件不存在: {file_path}")
        return None

    java_lang = load_java_language()
    if not java_lang:
        print("无法加载Java语言解析器")
        return None

    # 创建解析器
    parser = Parser()
    parser.set_language(java_lang)

    # 读取文件内容
    with open(file_path, 'rb') as f:
        source_code = f.read()

    # 解析
    start_time = time.time()
    tree = parser.parse(source_code)
    parse_time = time.time() - start_time

    print(f"✓ 解析完成: {file_path}")
    print(f"  解析时间: {parse_time:.4f}秒")
    print(f"  根节点类型: {tree.root_node.type}")
    print(f"  子节点数量: {tree.root_node.child_count}")

    return tree

if __name__ == "__main__":
    # 测试解析一个Java文件
    # 查找ep18r中的Java文件
    ep18r_dir = Path("ep18r/src/main/java")
    if ep18r_dir.exists():
        java_files = list(ep18r_dir.rglob("*.java"))
        if java_files:
            test_file = java_files[0]
            print(f"测试文件: {test_file}")
            tree = test_parse_java_file(str(test_file))

            if tree:
                # 简单统计
                root = tree.root_node
                print("\n=== 语法树分析 ===")
                print(f"总节点数: {count_nodes(root)}")

                # 提取类声明
                classes = extract_classes(root)
                print(f"类声明数量: {len(classes)}")
                for cls in classes[:3]:  # 显示前3个
                    print(f"  - {cls['name']} (第{cls['line']}行)")
        else:
            print("未找到Java文件")
    else:
        print(f"目录不存在: {ep18r_dir}")

def count_nodes(node):
    """递归统计节点数量"""
    count = 1
    for child in node.children:
        count += count_nodes(child)
    return count

def extract_classes(root):
    """提取类声明"""
    classes = []

    # 简单遍历查找类声明
    # 在实际应用中应使用tree-sitter查询
    def visit(node):
        if node.type == 'class_declaration':
            # 查找类名
            name_node = None
            for child in node.children:
                if child.type == 'identifier':
                    name_node = child
                    break

            if name_node:
                classes.append({
                    'name': name_node.text.decode('utf-8'),
                    'line': node.start_point[0] + 1,
                    'node': node
                })

        for child in node.children:
            visit(child)

    visit(root)
    return classes