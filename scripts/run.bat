@echo off
setlocal enabledelayedexpansion

:: 设置工作目录为项目根目录
cd /d "%~dp0.." || exit /b 1

:: 获取命令行参数
set command=%1
set module=%2

:: 如果没有参数或第一个参数是help，显示帮助信息
if "%command%"=="" goto :show_help
if "%command%"=="help" goto :show_help

:: 根据命令执行相应操作
if "%command%"=="compile" goto :compile
if "%command%"=="run" goto :run
if "%command%"=="test" goto :test
if "%command%"=="clean" goto :clean

:: 未知命令
echo 错误: 未知的命令 '%command%'
goto :show_help

:show_help
echo 用法: %~nx0 <命令> <模块名>
echo 命令:
echo   compile  - 编译指定模块 (依赖 clean)
echo   run      - 运行指定模块
echo   test     - 运行指定模块的测试 (依赖 compile)
echo   clean    - 清理指定模块的编译文件
echo   help     - 显示此帮助信息
echo 模块名:
echo   ep1 到 ep21 中的任意一个，例如: ep1, ep2, ..., ep21
echo 示例:
echo   %~nx0 compile ep1  # 编译ep1模块
echo   %~nx0 run ep2      # 运行ep2模块
echo   %~nx0 test ep3     # 运行ep3模块的测试
exit /b 0

:validate_module
:: 验证模块名是否有效
set module_name=%1
if not defined module_name (
    echo 错误: 请指定模块名
    exit /b 1
)

:: 检查模块名格式是否正确 (ep + 数字)
echo %module_name% | findstr /r "^ep[0-9][0-9]*$" >nul
if errorlevel 1 (
    echo 错误: 无效的模块名 '%module_name%'
    echo 请使用 ep1 到 ep21 之间的有效模块名
    exit /b 1
)

:: 检查模块目录是否存在
if not exist "%module_name%\" (
    echo 错误: 模块目录 '%module_name%' 不存在
    exit /b 1
)
exit /b 0

:clean_module
:: 清理模块
set module_name=%1
echo 正在清理 %module_name%...
call mvn clean -pl "%module_name%"
if errorlevel 1 exit /b 1
exit /b 0

:compile_module
:: 编译模块 (依赖 clean)
set module_name=%1
:: 先清理
call :clean_module "%module_name%"
if errorlevel 1 exit /b 1
echo 正在编译 %module_name%...
call mvn compile -pl "%module_name%"
if errorlevel 1 exit /b 1
exit /b 0

:compile
if "%module%"=="" (
    echo 错误: 请指定要编译的模块
    exit /b 1
)
call :validate_module %module%
if errorlevel 1 exit /b 1
call :compile_module "%module%"
if errorlevel 1 exit /b 1
exit /b 0

:run
if "%module%"=="" (
    echo 错误: 请指定要运行的模块
    exit /b 1
)
call :validate_module %module%
if errorlevel 1 exit /b 1
echo 正在运行 %module%...

:: 获取除了命令和模块名之外的所有参数
set extra_args=
:parse_args
shift
if "%1"=="" goto :run_module
set extra_args=%extra_args% %1
goto :parse_args

:run_module
:: 获取主类名
for /f "delims=" %%i in ('mvn help:evaluate -Dexpression^=run.main.entry -pl "%module%" -q -DforceStdout') do set mainClass=%%i

if not defined mainClass (
    echo 错误: 无法获取主类名，请检查模块的pom.xml配置
    exit /b 1
)

:: 运行模块
if defined extra_args (
    call mvn compile exec:java -pl "%module%" -Dexec.mainClass="%mainClass%" -Dexec.args="%extra_args:~1%"
) else (
    call mvn compile exec:java -pl "%module%" -Dexec.mainClass="%mainClass%"
)
if errorlevel 1 exit /b 1
exit /b 0

:test
if "%module%"=="" (
    echo 错误: 请指定要测试的模块
    exit /b 1
)
call :validate_module %module%
if errorlevel 1 exit /b 1
:: 先编译 (编译会自动清理)
call :compile_module "%module%"
if errorlevel 1 exit /b 1
echo 正在运行 %module% 的测试...
call mvn test -pl "%module%"
if errorlevel 1 exit /b 1
exit /b 0

:clean
if "%module%"=="" (
    echo 错误: 请指定要清理的模块
    exit /b 1
)
call :validate_module %module%
if errorlevel 1 exit /b 1
call :clean_module "%module%"
if errorlevel 1 exit /b 1
exit /b 0