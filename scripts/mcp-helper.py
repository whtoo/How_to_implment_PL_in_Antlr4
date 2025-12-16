#!/usr/bin/env python3
"""
ANTLR4 Compiler Project MCP Helper
提供MCP服务器管理的辅助工具
"""

import os
import sys
import json
import subprocess
import time
from pathlib import Path

def load_mcp_config():
    """加载MCP项目配置"""
    config_path = "mcp-project-config.json"
    if os.path.exists(config_path):
        with open(config_path, 'r') as f:
            return json.load(f)
    return {}

def check_maven():
    """检查Maven环境"""
    try:
        result = subprocess.run(['mvn', '--version'], 
                              capture_output=True, text=True)
        if result.returncode == 0:
            print(f"✅ Maven found: {result.stdout.split()[2]}")
            return True
        else:
            print("❌ Maven not found")
            return False
    except FileNotFoundError:
        print("❌ Maven command not found")
        return False

def check_java():
    """检查Java环境"""
    try:
        result = subprocess.run(['java', '-version'], 
                              capture_output=True, text=True)
        if result.returncode == 0:
            print(f"✅ Java found")
            return True
        else:
            print("❌ Java not found")
            return False
    except FileNotFoundError:
        print("❌ Java command not found")
        return False

def check_antlr():
    """检查ANTLR4工具"""
    try:
        result = subprocess.run(['antlr4'], 
                              capture_output=True, text=True)
        if result.returncode == 0:
            print("✅ ANTLR4 found")
            return True
        else:
            print("❌ ANTLR4 not found")
            return False
    except FileNotFoundError:
        print("❌ ANTLR4 command not found")
        return False

def list_ep_modules():
    """列出所有EP模块"""
    ep_dirs = [d for d in os.listdir('.') if d.startswith('ep') and d[2:].isdigit()]
    ep_dirs.sort(key=lambda x: int(x[2:]))
    
    print("\n📁 Available EP Modules:")
    for ep_dir in ep_dirs:
        if os.path.exists(f"{ep_dir}/pom.xml"):
            print(f"  ✅ {ep_dir}/")
        else:
            print(f"  ❌ {ep_dir}/ (no pom.xml)")

def show_project_info():
    """显示项目信息"""
    print("🔧 ANTLR4 Compiler Project Environment Check")
    print("=" * 50)
    
    # 检查环境
    maven_ok = check_maven()
    java_ok = check_java()
    antlr_ok = check_antlr()
    
    # 列出模块
    list_ep_modules()
    
    # 加载MCP配置
    mcp_config = load_mcp_config()
    if mcp_config:
        print(f"\n🔌 MCP Servers configured: {len(mcp_config.get('mcpServers', {}))}")
        for server_name in mcp_config['mcpServers'].keys():
            print(f"  - {server_name}")
    
    # 总结
    print("\n📋 Environment Status:")
    print(f"  Maven: {'✅' if maven_ok else '❌'}")
    print(f"  Java: {'✅' if java_ok else '❌'}")
    print(f"  ANTLR4: {'✅' if antlr_ok else '❌'}")
    
    if all([maven_ok, java_ok, antlr_ok]):
        print("\n🎉 Environment ready for compiler development!")
    else:
        print("\n⚠️  Some dependencies are missing. Please install them first.")

def start_mcp_server(server_name):
    """启动指定的MCP服务器"""
    mcp_config = load_mcp_config()
    servers = mcp_config.get('mcpServers', {})
    
    if server_name not in servers:
        print(f"❌ MCP server '{server_name}' not found")
        return
    
    server_config = servers[server_name]
    command = server_config['command']
    args = server_config.get('args', [])
    cwd = server_config.get('cwd', '.')
    
    print(f"🚀 Starting MCP server '{server_name}'...")
    print(f"Command: {command} {' '.join(args)}")
    print(f"Working directory: {cwd}")
    
    try:
        # 启动服务器
        process = subprocess.Popen(
            [command] + args,
            cwd=cwd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        
        print(f"✅ Server started with PID: {process.pid}")
        print("Press Ctrl+C to stop the server")
        
        # 保持服务器运行
        try:
            process.wait()
        except KeyboardInterrupt:
            print("\n🛑 Stopping server...")
            process.terminate()
            process.wait()
            
    except Exception as e:
        print(f"❌ Failed to start server: {e}")

def main():
    if len(sys.argv) < 2:
        show_project_info()
        return
    
    command = sys.argv[1]
    
    if command == "check":
        show_project_info()
    elif command == "list":
        list_ep_modules()
    elif command == "start" and len(sys.argv) > 2:
        server_name = sys.argv[2]
        start_mcp_server(server_name)
    else:
        print("Usage:")
        print("  python mcp-helper.py check      # Check environment")
        print("  python mcp-helper.py list       # List EP modules") 
        print("  python mcp-helper.py start <server> # Start MCP server")

if __name__ == "__main__":
    main()