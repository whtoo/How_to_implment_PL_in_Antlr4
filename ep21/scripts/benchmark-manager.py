#!/usr/bin/env python3
"""
EP21基准测试管理器
用于管理和运行基准测试套件
"""

import os
import sys
import time
import subprocess
import json
from pathlib import Path

class BenchmarkManager:
    def __init__(self, benchmarks_dir="benchmarks"):
        self.benchmarks_dir = Path(benchmarks_dir)
        self.results_dir = self.benchmarks_dir / "results"
        self.results_dir.mkdir(exist_ok=True)

        # 基准测试分类
        self.categories = {
            "stanford": "Stanford基准测试",
            "spec": "SPEC CPU风格测试",
            "optimization": "编译器优化测试",
            "validation": "验证测试"
        }

    def list_benchmarks(self):
        """列出所有基准测试"""
        benchmarks = {}
        for category, description in self.categories.items():
            category_dir = self.benchmarks_dir / category
            if category_dir.exists():
                benchmarks[category] = {
                    "description": description,
                    "tests": [f.name for f in category_dir.glob("*.cym")]
                }
        return benchmarks

    def print_summary(self):
        """打印基准测试集摘要"""
        benchmarks = self.list_benchmarks()

        print("=" * 60)
        print("EP21 基准测试集摘要")
        print("=" * 60)

        total_tests = 0
        for category, info in benchmarks.items():
            count = len(info["tests"])
            total_tests += count
            print(f"\n{self.categories[category]} ({category}/):")
            print(f"  描述: {info['description']}")
            print(f"  数量: {count} 个测试")
            for test in info["tests"]:
                print(f"    - {test}")

        print(f"\n总计: {total_tests} 个基准测试")
        print("=" * 60)

    def run_compiler(self, source_file, output_dir=None):
        """运行编译器编译基准测试"""
        if output_dir is None:
            output_dir = self.results_dir

        source_path = Path(source_file)
        if not source_path.exists():
            print(f"错误: 文件不存在 {source_file}")
            return None

        # 这里调用实际的编译器命令
        # 暂时用模拟实现
        print(f"编译: {source_path.name}")

        # 模拟编译过程
        time.sleep(0.1)

        # 返回模拟结果
        return {
            "source": str(source_path),
            "compile_time": 0.1,
            "success": True,
            "output": str(output_dir / f"{source_path.stem}.output")
        }

    def run_benchmark(self, test_name, category=None):
        """运行单个基准测试"""
        # 查找测试文件
        test_path = None
        if category:
            test_path = self.benchmarks_dir / category / f"{test_name}.cym"
        else:
            for cat in self.categories:
                candidate = self.benchmarks_dir / cat / f"{test_name}.cym"
                if candidate.exists():
                    test_path = candidate
                    category = cat
                    break

        if not test_path or not test_path.exists():
            print(f"错误: 找不到基准测试 {test_name}")
            return None

        print(f"运行基准测试: {test_name} ({self.categories[category]})")

        # 编译
        compile_result = self.run_compiler(test_path)
        if not compile_result or not compile_result["success"]:
            print("编译失败")
            return None

        # 这里可以添加执行生成的代码并测量性能
        # 暂时返回模拟结果
        return {
            "name": test_name,
            "category": category,
            "compile_time": compile_result["compile_time"],
            "execution_time": 1.0,  # 模拟执行时间
            "memory_usage": 1024,   # 模拟内存使用(KB)
            "code_size": 2048       # 模拟代码大小(bytes)
        }

    def run_category(self, category):
        """运行特定类别的所有基准测试"""
        if category not in self.categories:
            print(f"错误: 未知类别 {category}")
            return []

        category_dir = self.benchmarks_dir / category
        if not category_dir.exists():
            print(f"类别目录不存在: {category_dir}")
            return []

        results = []
        for test_file in category_dir.glob("*.cym"):
            result = self.run_benchmark(test_file.stem, category)
            if result:
                results.append(result)

        return results

    def generate_report(self, results, output_file="benchmark_report.json"):
        """生成基准测试报告"""
        report = {
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
            "total_tests": len(results),
            "results": results,
            "summary": {
                "total_compile_time": sum(r["compile_time"] for r in results),
                "total_execution_time": sum(r["execution_time"] for r in results),
                "avg_memory_usage": sum(r["memory_usage"] for r in results) / len(results) if results else 0,
                "avg_code_size": sum(r["code_size"] for r in results) / len(results) if results else 0
            }
        }

        output_path = self.results_dir / output_file
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(report, f, indent=2, ensure_ascii=False)

        print(f"报告已生成: {output_path}")
        return output_path

def main():
    """主函数"""
    manager = BenchmarkManager()

    if len(sys.argv) < 2:
        manager.print_summary()
        print("\n使用方法:")
        print("  python benchmark-manager.py list         # 列出所有测试")
        print("  python benchmark-manager.py run <test>   # 运行单个测试")
        print("  python benchmark-manager.py category <category>  # 运行类别所有测试")
        return

    command = sys.argv[1]

    if command == "list":
        manager.print_summary()

    elif command == "run" and len(sys.argv) >= 3:
        test_name = sys.argv[2]
        result = manager.run_benchmark(test_name)
        if result:
            print(f"\n测试结果:")
            for key, value in result.items():
                print(f"  {key}: {value}")

    elif command == "category" and len(sys.argv) >= 3:
        category = sys.argv[2]
        results = manager.run_category(category)
        if results:
            manager.generate_report(results)
            print(f"\n完成 {len(results)} 个测试")

    else:
        print("未知命令")

if __name__ == "__main__":
    main()