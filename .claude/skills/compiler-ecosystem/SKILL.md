---
name: compiler-ecosystem
description: 提供完整的编译器开发生态系统支持，包括ANTLR4语法设计、项目构建、测试、调试和部署。整合编译器开发技能、构建脚本、MCP服务器配置和跨平台开发工具。当用户需要进行编译器项目开发、调试、优化或部署时使用此技能。
allowed-tools: Read, Grep, Glob, Bash, Edit, Write
---

# 编译器开发生态系统技能

## 生态概览

**Compiler Ecosystem** 是一个完整的编译器开发生态系统，整合了项目开发所需的所有工具、技能和配置：

### 核心组件
- **专业技能**: ANTLR4编译器开发专业知识
- **构建系统**: 跨平台自动化构建脚本
- **MCP集成**: 模型上下文协议服务器配置
- **开发工具**: 项目管理和环境检查工具

### 项目架构

How to implement PL in ANTLR4/
├── .claude/skills/
│   ├── compiler-development/        # 原始编译器开发技能
│   └── antlr4-compiler-development/ # 优化版ANTLR4技能
├── scripts/
│   ├── build.sh                     # Linux/macOS构建脚本
│   ├── build.ps1                    # Windows PowerShell脚本
│   ├── build.bat                    # Windows CMD脚本
│   └── mcp-helper.py               # MCP管理工具
├── mcp-project-config.json         # 项目MCP配置
└── ep1-ep21/                       # 21章节编译器实现


## 核心工作流程

### 1. 项目初始化和检查
当开始新的编译器开发任务时：

**环境检查**:
bash
# 使用MCP辅助工具检查环境
python scripts/mcp-helper.py check

# 查看项目信息
python scripts/mcp-helper.py list
./scripts/build.sh info


**环境要求**:
- Java 11+ (推荐Java 21)
- Maven 3.6+
- ANTLR4 (可选，Maven依赖已包含)

### 2. 编译器开发流程

**语法设计和实现**:
1. 编辑 ep20/src/main/antlr4/.../Cymbol.g4
2. 生成解析器代码: ./scripts/build.sh generate ep20
3. 实现AST节点: ep20/src/main/java/org/teachfx/antlr4/ep20/ast/
4. 构建测试: mvn test -pl ep20 -Dtest="*ParserTest"

**语义分析和类型系统**:
1. 实现符号表: ep20/src/main/java/org/teachfx/antlr4/ep20/symtab/
2. 开发语义检查: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/sematic/
3. 类型检查测试: mvn test -pl ep17 -Dtest="*TypeCheckTest"

**中间代码生成**:
1. 设计IR结构: ep20/src/main/java/org/teachfx/antlr4/ep20/ir/
2. 实现代码生成: ep20/src/main/java/org/teachfx/antlr4/ep20/pass/cfg/
3. 运行编译器: ./scripts/build.sh run ep20 program.cymbol

**虚拟机实现**:
1. 开发字节码解释器: ep18/src/main/java/org/teachfx/antlr4/ep18/stackvm/
2. 运行虚拟机: ./scripts/build.sh vm ep18 program.cymbol

### 3. 测试和质量保证

**全面测试策略**:
bash
# 运行所有测试
./scripts/build.sh test

# 运行特定模块测试
./scripts/build.sh test ep20

# 生成覆盖率报告
./scripts/build.sh coverage ep20

# 清理和重新构建
./scripts/build.sh clean && ./scripts/build.sh compile


**测试最佳实践**:
- 单元测试覆盖率 ≥85%
- 核心模块覆盖率 ≥90%
- 新功能测试覆盖率 100%
- 集成测试验证完整编译流程

### 4. 调试和故障排除

**调试工具链**:
1. **语法调试**: ANTLR4 TestRig
   bash
   java -cp "antlr-4.13.2-complete.jar:target/classes" \
     org.antlr.v4.gui.TestRig Cymbol file -tokens program.cymbol
   

2. **AST可视化**: 使用ep19/ep20中的可视化工具
3. **符号表调试**: 启用DEBUG级别日志
4. **IR调试**: 输出中间代码进行验证
5. **VM调试**: 使用虚拟机调试模式

**常见问题解决**:
- **语法冲突**: 使用ANTLR4诊断工具分析
- **类型检查失败**: 检查符号表和类型转换规则
- **IR生成错误**: 验证AST到IR的转换逻辑
- **VM执行错误**: 检查字节码生成和栈帧管理

## MCP集成和工具

### MCP服务器配置
项目提供优化的MCP服务器配置：

**antlr4-compiler**: 文件系统访问
**project-structure**: 项目结构管理
**maven-build**: Maven构建工具
**grammar-parser**: 语法树解析
**memory**: 记忆功能
**context7**: 上下文管理

### MCP工具使用
bash
# 启动特定MCP服务器
python scripts/mcp-helper.py start antlr4-compiler

# 查看MCP配置
python scripts/mcp-helper.py check


## 跨平台开发支持

### 支持的平台和脚本
- **Linux/macOS**: scripts/build.sh
- **Windows PowerShell**: scripts/build.ps1
- **Windows CMD**: scripts/build.bat
- **跨平台Python**: scripts/mcp-helper.py

### 统一命令接口
所有脚本支持相同的命令集：
bash
generate [module]    # 生成ANTLR4源文件
compile [module]     # 编译项目
test [module]        # 运行测试
run [module] [file]  # 运行编译器
vm [module] [file]   # 运行虚拟机
clean [module]       # 清理项目
coverage [module]    # 生成覆盖率报告
info                 # 显示项目信息


## 项目扩展和定制

### 添加新的EP模块
1. 复制现有模块结构: cp -r ep20 ep22
2. 更新 pom.xml 中的模块名称
3. 修改包名和类名前缀
4. 更新构建脚本配置

### 集成外部工具
1. **静态分析**: 添加SpotBugs、CheckStyle配置
2. **性能分析**: 集成JProfiler、JFR
3. **文档生成**: 使用Javadoc、PlantUML
4. **持续集成**: 配置GitHub Actions、Jenkins

### 自定义构建流程
修改 scripts/build.sh 添加新命令：
bash
# 添加新命令
lint)
    # 添加代码检查逻辑
    ;;


## 最佳实践

### 开发工作流
1. **测试驱动开发**: 先写测试再实现功能
2. **渐进式实现**: 按EP1-EP21顺序理解
3. **文档同步**: 修改代码时更新文档
4. **版本控制**: 使用Git进行版本管理

### 代码质量
1. **代码风格**: 遵循Java编码规范
2. **错误处理**: 提供有意义的错误信息
3. **日志记录**: 使用适当的日志级别
4. **性能优化**: 关注关键路径的性能

### 项目维护
1. **依赖管理**: 定期更新依赖版本
2. **安全扫描**: 检查安全漏洞
3. **文档维护**: 保持文档与代码同步
4. **测试维护**: 确保测试的稳定性

## 技能使用指南

### 何时使用此技能
- 开始新的编译器开发项目
- 遇到编译器开发技术问题
- 需要项目构建和测试支持
- 要求跨平台开发解决方案
- 需要MCP服务器配置帮助

### 与其他技能的配合
- **Skill**: compiler-development - 深度技术指导
- **Skill**: mcp-builder - MCP服务器开发
- **Tool**: bash - 脚本执行和系统操作
- **Tool**: read_file - 代码和文档分析

### 技能使用流程
1. 评估项目需求和当前状态
2. 选择合适的开发阶段和工具
3. 使用对应的脚本和配置
4. 参考技能文档解决技术问题
5. 验证结果和优化工作流程

---

*技能版本: 3.0.0 | 最后更新: 2025-12-16*
*项目: How to implement PL in ANTLR4*
*技能ID: compiler-ecosystem*