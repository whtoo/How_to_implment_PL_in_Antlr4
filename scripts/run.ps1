#!/usr/bin/env pwsh

<#
.SYNOPSIS
  项目构建和运行脚本（PowerShell版本）
.DESCRIPTION
  提供compile、run、test、clean等功能，与Linux下的run.sh功能一致
.EXAMPLE
  .\run.ps1 compile ep1    # 编译ep1模块
  .\run.ps1 run ep2        # 运行ep2模块
  .\run.ps1 test ep3       # 测试ep3模块
  .\run.ps1 clean ep4      # 清理ep4模块
#>

# 设置错误处理，遇到错误即停止
$ErrorActionPreference = "Stop"

# 获取脚本所在目录的绝对路径
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
# 项目根目录是脚本所在目录的上一级
$projectRoot = Join-Path $scriptDir ".."

# 将当前工作目录设置为项目根目录，以确保mvn命令在正确的位置执行
Set-Location $projectRoot

# 显示帮助信息
function Show-Help {
    param(
        # 脚本名称，自动从调用信息中获取
        [string]$ScriptName = $MyInvocation.MyCommand.Name
    )
    
    Write-Host "用法: $ScriptName <命令> <模块名> [额外参数...]"
    Write-Host "命令:"
    Write-Host "  compile   - 编译指定模块 (此操作前会先执行 clean)"
    Write-Host "  run       - 运行指定模块"
    Write-Host "  test      - 运行指定模块的测试 (此操作前会先执行 compile)"
    Write-Host "  clean     - 清理指定模块的编译文件"
    Write-Host "  help      - 显示此帮助信息"
    Write-Host "模块名:"
    Write-Host "  ep1 到 ep21 中的任意一个，例如: ep1, ep2, ..., ep21"
    Write-Host "示例:"
    Write-Host "  $ScriptName compile ep1    # 编译ep1模块"
    Write-Host "  $ScriptName run ep2        # 运行ep2模块"
    Write-Host "  $ScriptName test ep3       # 运行ep3模块的测试"
    Write-Host "  $ScriptName clean ep4      # 清理ep4模块"
}

# 验证模块名是否有效，以及模块目录是否存在
function Validate-Module {
    param(
        [string]$ModuleName
    )
    
    # 使用正则表达式验证模块名格式是否为 "ep" + 数字
    if ($ModuleName -notmatch '^ep\d+$') {
        Write-Host "错误: 无效的模块名 '$ModuleName'" -ForegroundColor Red
        Write-Host "请使用 ep1 到 ep21 之间的有效模块名。" -ForegroundColor Yellow
        exit 1
    }
    
    $modulePath = Join-Path $projectRoot $ModuleName
    # 检查模块对应的目录是否存在
    if (-not (Test-Path $modulePath -PathType Container)) {
        Write-Host "错误: 模块目录 '$ModuleName' 不存在于 '$projectRoot'。" -ForegroundColor Red
        exit 1
    }
}

# 执行测试
function Invoke-Test {
    param(
        [string]$ModuleName
    )
    
    # 测试前先编译
    Build-Module -ModuleName $ModuleName
    Write-Host "正在运行 $ModuleName 的测试..." -ForegroundColor Magenta
    mvn test -pl $ModuleName
}

# 清理模块
function Clear-Module {
    param(
        [string]$ModuleName
    )
    
    Write-Host "正在清理 $ModuleName..." -ForegroundColor Cyan
    mvn clean -pl $ModuleName
}

# 编译模块 (依赖 clean)
function Build-Module {
    param(
        [string]$ModuleName
    )
    
    # 编译前先清理
    Clear-Module -ModuleName $ModuleName
    
    Write-Host "正在编译 $ModuleName..." -ForegroundColor Green
    mvn compile -pl $ModuleName
}

# 运行模块
function Start-Module {
    param(
        [string]$ModuleName,
        [string[]]$ExtraArgs
    )
    
    Write-Host "正在运行 $ModuleName..." -ForegroundColor Blue
    
    # 从模块的pom.xml中动态获取主类名
    # 使用 -q (quiet) 和 -DforceStdout 确保只输出结果
    $mainClass = mvn help:evaluate -Dexpression=run.main.entry -pl $ModuleName -q -DforceStdout
    
    if ([string]::IsNullOrEmpty($mainClass)) {
        Write-Host "错误: 无法获取主类名，请检查模块 '$ModuleName' 的 pom.xml 中是否正确配置了 'run.main.entry' 属性。" -ForegroundColor Red
        exit 1
    }
    
    # 构建要传递给Java程序的参数
    $execArgs = ""
    if ($ExtraArgs) {
        $execArgs = $ExtraArgs -join " "
    }
    
    # 使用 mvn compile exec:java 来编译并执行
    # -Dexec.args 将参数传递给 main 方法
    mvn compile exec:java -pl $ModuleName "-Dexec.mainClass=$mainClass" "-Dexec.args=$execArgs"
}

# --- 脚本主逻辑 ---

# 如果没有参数或第一个参数是 'help'，则显示帮助信息并退出
if ($args.Count -eq 0 -or $args[0] -eq "help") {
    Show-Help
    exit 0
}

$command = $args[0]
$module = $args[1]
# 获取从第三个开始的所有剩余参数
$extraArgs = $args[2..($args.Count - 1)]

# 使用 switch 处理不同的命令
switch ($command) {
    "compile" {
        if ([string]::IsNullOrEmpty($module)) {
            Write-Host "错误: 请指定要编译的模块。" -ForegroundColor Red
            exit 1
        }
        Validate-Module -ModuleName $module
        Build-Module -ModuleName $module
    }
    "run" {
        if ([string]::IsNullOrEmpty($module)) {
            Write-Host "错误: 请指定要运行的模块。" -ForegroundColor Red
            exit 1
        }
        Validate-Module -ModuleName $module
        Start-Module -ModuleName $module -ExtraArgs $extraArgs
    }
    "test" {
        if ([string]::IsNullOrEmpty($module)) {
            Write-Host "错误: 请指定要测试的模块。" -ForegroundColor Red
            exit 1
        }
        Validate-Module -ModuleName $module
        Invoke-Test -ModuleName $module
    }
    "clean" {
        if ([string]::IsNullOrEmpty($module)) {
            Write-Host "错误: 请指定要清理的模块。" -ForegroundColor Red
            exit 1
        }
        Validate-Module -ModuleName $module
        Clear-Module -ModuleName $module
    }
    default {
        Write-Host "错误: 未知的命令 '$command'。" -ForegroundColor Red
        Show-Help
        exit 1
    }
}