#!/bin/bash

# 清理VSCode缓存文件
echo "🧹 清理VSCode缓存..."

# 清理隐藏的VSCode文件
find . -name ".vscode" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name ".cache" -type d -exec rm -rf {} + 2>/dev/null || true

# 清理Java语言服务器缓存
find . -name ".jdt" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "*.classpath" -delete 2>/dev/null || true

# 清理Maven生成的缓存
find . -name ".mvn" -type d -exec rm -rf {} + 2>/dev/null || true
find . -name "target" -type d -exec rm -rf {} + 2>/dev/null || true

echo "✅ 缓存清理完成！"
echo "📋 请执行以下步骤："
echo "1. 重启VSCode (Cmd+Q 或 Ctrl+Q)"
echo "2. 打开工作区后等待Java语言服务器初始化"
echo "3. 如果还有错误提示，尝试 'Developer: Reload Window'"