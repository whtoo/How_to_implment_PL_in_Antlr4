# ANTLR4 Compiler Project Build Script (PowerShell)
# Windows PowerShell版本的构建脚本

param(
    [string]$Command = "help",
    [string]$Module = "ep20",
    [string]$File = "program.cymbol",
    [string]$Pattern = "*"
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    
    $originalColor = $Host.UI.RawUI.ForegroundColor
    $Host.UI.RawUI.ForegroundColor = $Color
    Write-Output $Message
    $Host.UI.RawUI.ForegroundColor = $originalColor
}

function Write-Info {
    param([string]$Message)
    Write-ColorOutput "[INFO] $Message" "Cyan"
}

function Write-Success {
    param([string]$Message)
    Write-ColorOutput "[SUCCESS] $Message" "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-ColorOutput "[WARNING] $Message" "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-ColorOutput "[ERROR] $Message" "Red"
}

# 检查依赖
function Test-Dependencies {
    Write-Info "Checking dependencies..."
    
    # 检查Java
    try {
        $javaVersion = java -version 2>&1 | Out-String
        Write-Success "Java found"
    }
    catch {
        Write-Error "Java is not installed"
        exit 1
    }
    
    # 检查Maven
    try {
        $mavenVersion = mvn --version 2>&1 | Out-String
        Write-Success "Maven found"
    }
    catch {
        Write-Error "Maven is not installed"
        exit 1
    }
    
    # 检查ANTLR4
    try {
        $antlrVersion = antlr4 2>&1 | Out-String
        Write-Success "ANTLR4 found"
    }
    catch {
        Write-Warning "ANTLR4 not found in PATH, will use Maven dependencies"
    }
    
    Write-Success "Dependencies check passed"
}

# 生成ANTLR4代码
function Invoke-GenerateSources {
    param([string]$Module)
    
    Write-Info "Generating ANTLR4 sources for $Module..."
    
    Set-Location $Module
    try {
        mvn generate-sources -DskipTests
        Write-Success "Sources generated successfully for $Module"
    }
    catch {
        Write-Error "Failed to generate sources for $Module"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 编译项目
function Invoke-Compile {
    param([string]$Module)
    
    Write-Info "Compiling $Module..."
    
    Set-Location $Module
    try {
        mvn compile
        Write-Success "$Module compiled successfully"
    }
    catch {
        Write-Error "Failed to compile $Module"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 运行测试
function Invoke-Test {
    param([string]$Module, [string]$Pattern)
    
    Write-Info "Running tests for $Module (pattern: $Pattern)..."
    
    Set-Location $Module
    try {
        if ($Pattern -eq "*") {
            mvn test
        } else {
            mvn test -Dtest="$Pattern"
        }
        Write-Success "Tests passed for $Module"
    }
    catch {
        Write-Error "Tests failed for $Module"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 运行编译器
function Invoke-RunCompiler {
    param([string]$Module, [string]$File)
    
    if (-not (Test-Path $File)) {
        Write-Error "Input file '$File' not found"
        exit 1
    }
    
    Write-Info "Running compiler ($Module) on $File..."
    
    Set-Location $Module
    try {
        mvn compile exec:java -Dexec.args="$File"
        Write-Success "Compiler executed successfully"
    }
    catch {
        Write-Error "Compiler execution failed"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 运行虚拟机
function Invoke-RunVM {
    param([string]$Module, [string]$File)
    
    Write-Info "Running virtual machine ($Module) on $File..."
    
    Set-Location $Module
    try {
        mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM" -Dexec.args="$File"
        Write-Success "Virtual machine executed successfully"
    }
    catch {
        Write-Error "Virtual machine execution failed"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 清理项目
function Invoke-Clean {
    param([string]$Module)
    
    Write-Info "Cleaning $Module..."
    
    Set-Location $Module
    try {
        mvn clean
        Write-Success "$Module cleaned successfully"
    }
    catch {
        Write-Error "Failed to clean $Module"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 生成覆盖率报告
function Invoke-Coverage {
    param([string]$Module)
    
    Write-Info "Generating coverage report for $Module..."
    
    Set-Location $Module
    try {
        mvn test jacoco:report
        Write-Success "Coverage report generated for $Module"
        Write-Info "Report location: $Module\target\site\jacoco\index.html"
    }
    catch {
        Write-Error "Failed to generate coverage report"
        exit 1
    }
    finally {
        Set-Location ..
    }
}

# 显示项目信息
function Show-ProjectInfo {
    Write-Info "ANTLR4 Compiler Project Information"
    Write-Output "===================================="
    
    # 列出EP模块
    Write-Info "Available EP modules:"
    Get-ChildItem -Directory -Name "ep*" | ForEach-Object {
        if (Test-Path "$_\pom.xml") {
            Write-Output "  ✅ $_"
        } else {
            Write-Output "  ❌ $_ (no pom.xml)"
        }
    }
    
    # 检查Maven版本
    Write-Info "Maven version:"
    try {
        $mvnVersion = mvn --version | Select-Object -First 1
        Write-Output $mvnVersion
    }
    catch {
        Write-Error "Maven not found"
    }
    
    # 检查Java版本
    Write-Info "Java version:"
    try {
        $javaVersion = java -version 2>&1 | Select-Object -First 1
        Write-Output $javaVersion
    }
    catch {
        Write-Error "Java not found"
    }
}

# 显示帮助信息
function Show-Help {
    Write-Output "ANTLR4 Compiler Project Build Script (PowerShell)"
    Write-Output "================================================="
    Write-Output ""
    Write-Output "Usage: .\build.ps1 <command> [options]"
    Write-Output ""
    Write-Output "Commands:"
    Write-Output "  check                     Check project dependencies"
    Write-Output "  generate [module]         Generate ANTLR4 sources"
    Write-Output "  compile [module]          Compile project"
    Write-Output "  test [module] [pattern]   Run tests"
    Write-Output "  run [module] [file]       Run compiler on file"
    Write-Output "  vm [module] [file]        Run virtual machine"
    Write-Output "  clean [module]            Clean project"
    Write-Output "  coverage [module]         Generate coverage report"
    Write-Output "  info                      Show project information"
    Write-Output "  help                      Show this help message"
    Write-Output ""
    Write-Output "Examples:"
    Write-Output "  .\build.ps1 generate ep20          # Generate sources for ep20"
    Write-Output "  .\build.ps1 test ep20 ArraysTest   # Run specific test"
    Write-Output "  .\build.ps1 run ep20 program.cymbol # Compile program.cymbol"
    Write-Output "  .\build.ps1 vm ep18 program.cymbol # Run program in VM"
    Write-Output ""
    Write-Output "Default module: ep20"
}

# 主函数
switch ($Command.ToLower()) {
    "check" {
        Test-Dependencies
    }
    "generate" {
        Invoke-GenerateSources $Module
    }
    "compile" {
        Invoke-Compile $Module
    }
    "test" {
        Invoke-Test $Module $Pattern
    }
    "run" {
        Invoke-RunCompiler $Module $File
    }
    "vm" {
        Invoke-RunVM $Module $File
    }
    "clean" {
        Invoke-Clean $Module
    }
    "coverage" {
        Invoke-Coverage $Module
    }
    "info" {
        Show-ProjectInfo
    }
    "help" {
        Show-Help
    }
    default {
        Write-Error "Unknown command: $Command"
        Show-Help
        exit 1
    }
}