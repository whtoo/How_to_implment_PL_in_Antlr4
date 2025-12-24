# 如何为AI助手添加MCP服务

## 什么是MCP服务

MCP（Model Context Protocol）是一种协议，允许AI助手与外部工具和服务进行集成。通过MCP服务，您可以扩展AI助手的能力，使其能够访问数据库、文件系统、API服务等各种外部资源。

## MCP配置文件格式

MCP服务的配置通常存储在项目根目录的 `.mcp.json` 文件中。配置文件的结构如下：

```json
{
  "mcpServers": {
    "server-name": {
      "command": "executable-command",
      "args": ["arg1", "arg2", ...],
      "env": {
        "ENV_VAR1": "value1"
      }
    }
  }
}
```

**配置字段说明：**
- `command`: 要执行的命令（通常是可执行文件的路径或包管理器命令如 `npx`、`uvx`）
- `args`: 命令的参数列表
- `env`: （可选）环境变量配置

## 添加MCP服务的步骤

### 步骤一：创建或编辑配置文件

在项目根目录创建 `.mcp.json` 文件（如果不存在）：

```bash
touch .mcp.json
```

### 步骤二：添加MCP服务器配置

根据您要添加的MCP服务，按照其文档提供正确的配置。以下是几个常用MCP服务的配置示例。

### 步骤三：验证配置

配置完成后，重新启动AI助手会话以加载新的MCP服务。

## 常用MCP服务示例

### 1. Context7 MCP服务

用于访问文档和知识库：

```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": [
        "-y",
        "@upstash/context7-mcp",
        "--api-key", "your-api-key-here"
      ]
    }
  }
}
```

### 2. 文件系统MCP服务

提供文件读写能力：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/path/to/allowed/directory"
      ]
    }
  }
}
```

### 3. 数据库MCP服务

支持数据库查询操作：

```json
{
  "mcpServers": {
    "postgres": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-postgres",
        "postgresql://user:password@localhost:5432/database"
      ]
    }
  }
}
```

### 4. GitHub MCP服务

提供GitHub API访问能力：

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-github"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "your-token-here"
      }
    }
  }
}
```

### 5. Puppeteer MCP服务

用于浏览器自动化：

```json
{
  "mcpServers": {
    "puppeteer": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-puppeteer"
      ]
    }
  }
}
```

## 添加自定义MCP服务

### 1. 使用官方MCP服务器

访问 [MCP官方文档](https://modelcontextprotocol.io/) 获取官方支持的MCP服务器列表。

### 2. 开发自定义MCP服务器

如果您需要自定义功能，可以开发自己的MCP服务器：

```python
# server.py 示例
from mcp.server import Server
from mcp.server.stdio import stdio_server

app = Server("my-custom-server")

@app.list_tools()
async def list_tools():
    return [
        {
            "name": "my_tool",
            "description": "我的自定义工具",
            "inputSchema": {
                "type": "object",
                "properties": {
                    "param1": {"type": "string"}
                }
            }
        }
    ]

async def main():
    async with stdio_server() as (read_stream, write_stream):
        await app.run(read_stream, write_stream, app.create_initialization_options())

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
```

### 3. 配置自定义服务器

```json
{
  "mcpServers": {
    "custom-server": {
      "command": "python",
      "args": ["/path/to/your/server.py"]
    }
  }
}
```

## 注意事项

### 1. 安全考虑

- **敏感信息**：不要在配置文件中硬编码API密钥，应使用环境变量
- **路径限制**：文件系统MCP服务应限制可访问的目录范围
- **权限控制**：仔细审查第三方MCP服务的权限请求

### 2. 性能优化

- 仅添加您实际需要的MCP服务
- 定期更新MCP服务器以获取最新功能和修复
- 监控MCP服务的响应时间和资源使用

### 3. 故障排除

- **服务无法启动**：检查命令路径和参数是否正确
- **权限错误**：确保执行权限和文件访问权限正确
- **连接失败**：验证网络配置和API密钥
- **查看日志**：启用详细日志模式进行调试

## 常用MCP服务资源

- **官方MCP服务器列表**：https://github.com/modelcontextprotocol/servers
- **MCP Python SDK**：https://github.com/anthropics/mcp
- **MCP JavaScript SDK**：https://github.com/anthropics/mcp

## 配置示例集合

以下是多个MCP服务同时配置的综合示例：

```json
{
  "mcpServers": {
    "context7": {
      "command": "npx",
      "args": [
        "-y",
        "@upstash/context7-mcp",
        "--api-key", "your-api-key"
      ]
    },
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/allowed/path1",
        "/allowed/path2"
      ]
    },
    "github": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-github"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "your-github-token"
      }
    }
  }
}
```

## 总结

通过MCP服务，您可以显著扩展AI助手的能力。关键步骤包括：

1. 了解所需的MCP服务功能
2. 获取正确的配置信息
3. 按照规范格式编辑 `.mcp.json` 文件
4. 验证配置并重启会话
5. 定期维护和更新MCP服务

建议从简单的MCP服务开始，逐步添加更复杂的服务，以熟悉整个配置和管理流程。
