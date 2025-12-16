@echo off
REM ANTLR4 Compiler Project Build Script (Windows CMD)
REM Windows批处理版本的构建脚本

setlocal enabledelayedexpansion

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="--help" goto :help
if "%1"=="-h" goto :help

set COMMAND=%1
set MODULE=%2
if "%MODULE%"=="" set MODULE=ep20
set FILE=%3
if "%FILE%"=="" set FILE=program.cymbol

echo [INFO] ANTLR4 Compiler Project Build Script
echo ==========================================
echo.

goto :%COMMAND%

:check
echo [INFO] Checking dependencies...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed
    exit /b 1
) else (
    echo [SUCCESS] Java found
)

mvn --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed
    exit /b 1
) else (
    echo [SUCCESS] Maven found
)

antlr4 >nul 2>&1
if errorlevel 1 (
    echo [WARNING] ANTLR4 not found in PATH, will use Maven dependencies
) else (
    echo [SUCCESS] ANTLR4 found
)

echo [SUCCESS] Dependencies check passed
goto :end

:generate
echo [INFO] Generating ANTLR4 sources for %MODULE%...
cd %MODULE%
mvn generate-sources -DskipTests
if errorlevel 1 (
    echo [ERROR] Failed to generate sources for %MODULE%
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] Sources generated successfully for %MODULE%
goto :end

:compile
echo [INFO] Compiling %MODULE%...
cd %MODULE%
mvn compile
if errorlevel 1 (
    echo [ERROR] Failed to compile %MODULE%
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] %MODULE% compiled successfully
goto :end

:test
echo [INFO] Running tests for %MODULE%...
cd %MODULE%
mvn test
if errorlevel 1 (
    echo [ERROR] Tests failed for %MODULE%
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] Tests passed for %MODULE%
goto :end

:run
if not exist "%FILE%" (
    echo [ERROR] Input file '%FILE%' not found
    exit /b 1
)
echo [INFO] Running compiler (%MODULE%) on %FILE%...
cd %MODULE%
mvn compile exec:java -Dexec.args="%FILE%"
if errorlevel 1 (
    echo [ERROR] Compiler execution failed
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] Compiler executed successfully
goto :end

:vm
echo [INFO] Running virtual machine (%MODULE%) on %FILE%...
cd %MODULE%
mvn compile exec:java -Dexec.mainClass="org.teachfx.antlr4.ep18.stackvm.CymbolStackVM" -Dexec.args="%FILE%"
if errorlevel 1 (
    echo [ERROR] Virtual machine execution failed
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] Virtual machine executed successfully
goto :end

:clean
echo [INFO] Cleaning %MODULE%...
cd %MODULE%
mvn clean
if errorlevel 1 (
    echo [ERROR] Failed to clean %MODULE%
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] %MODULE% cleaned successfully
goto :end

:coverage
echo [INFO] Generating coverage report for %MODULE%...
cd %MODULE%
mvn test jacoco:report
if errorlevel 1 (
    echo [ERROR] Failed to generate coverage report
    cd ..
    exit /b 1
)
cd ..
echo [SUCCESS] Coverage report generated for %MODULE%
echo [INFO] Report location: %MODULE%\target\site\jacoco\index.html
goto :end

:info
echo [INFO] ANTLR4 Compiler Project Information
echo ========================================
echo.
echo [INFO] Available EP modules:
for /d %%d in (ep*) do (
    if exist "%%d\pom.xml" (
        echo   ✅ %%d
    ) else (
        echo   ❌ %%d ^(no pom.xml^)
    )
)
echo.
echo [INFO] Maven version:
mvn --version | findstr "Apache Maven"
echo.
echo [INFO] Java version:
java -version 2>&1 | findstr "version"
goto :end

:help
echo ANTLR4 Compiler Project Build Script ^(Windows CMD^)
echo ===================================================
echo.
echo Usage: build.bat ^<command^> [options]
echo.
echo Commands:
echo   check                     Check project dependencies
echo   generate [module]         Generate ANTLR4 sources
echo   compile [module]          Compile project
echo   test [module]             Run tests
echo   run [module] [file]       Run compiler on file
echo   vm [module] [file]        Run virtual machine
echo   clean [module]            Clean project
echo   coverage [module]         Generate coverage report
echo   info                      Show project information
echo   help                      Show this help message
echo.
echo Examples:
echo   build.bat generate ep20          # Generate sources for ep20
echo   build.bat test ep20              # Run tests for ep20
echo   build.bat run ep20 program.cymbol # Compile program.cymbol
echo   build.bat vm ep18 program.cymbol  # Run program in VM
echo.
echo Default module: ep20
goto :end

:end
endlocal