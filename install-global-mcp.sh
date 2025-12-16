#!/bin/bash

# Global MCP Installation Script for ANTLR4 Project
# This script installs the required MCP servers globally using npm and uv

echo "🚀 Installing Global MCP Servers for ANTLR4 Project"
echo "=================================================="

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install Node.js and npm first."
    exit 1
fi

# Check if uv is installed
if ! command -v uv &> /dev/null; then
    echo "⚠️  uv is not installed. Python-based MCP servers will not be available."
    echo "   Install uv from: https://github.com/astral-sh/uv"
else
    echo "✅ uv is installed"
fi

echo ""
echo "📦 Installing npm-based MCP servers..."

# Install tree-sitter MCP
echo "Installing @nendo/tree-sitter-mcp..."
npm install -g @nendo/tree-sitter-mcp

# Install filesystem MCP
echo "Installing @modelcontextprotocol/server-filesystem..."
npm install -g @modelcontextprotocol/server-filesystem

# Install git MCP
echo "Installing @modelcontextprotocol/server-git..."
npm install -g @modelcontextprotocol/server-git

# Install postgres MCP
echo "Installing @modelcontextprotocol/server-postgres..."
npm install -g @modelcontextprotocol/server-postgres

echo ""
echo "🐍 Installing Python-based MCP servers with uv..."

# Install Context7 MCP
if command -v uv &> /dev/null; then
    echo "Installing context7-mcp-server..."
    uvx context7-mcp-server --help > /dev/null 2>&1 || echo "   Note: context7-mcp-server may not be available via uvx"
else
    echo "⚠️  Skipping uv installation (uv not available)"
fi

echo ""
echo "✅ Installation complete!"
echo ""
echo "📋 Installed MCP servers:"
echo "  - tree-sitter: @nendo/tree-sitter-mcp"
echo "  - filesystem: @modelcontextprotocol/server-filesystem"  
echo "  - git: @modelcontextprotocol/server-git"
echo "  - postgres: @modelcontextprotocol/server-postgres"
echo "  - context7: context7-mcp-server (if available)"
echo ""
echo "🔧 Configuration: .roo/mcp.json"
echo "📖 Documentation: CLAUDE.md"
echo ""
echo "🎉 Your ANTLR4 project is now ready with global MCP support!"