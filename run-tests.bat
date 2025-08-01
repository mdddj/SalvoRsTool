@echo off
setlocal enabledelayedexpansion

REM SalvoRsTool Test Runner Script for Windows
REM 用于运行插件测试的便捷脚本

set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM 打印带颜色的消息
:print_info
echo %BLUE%[INFO]%NC% %~1
goto :eof

:print_success
echo %GREEN%[SUCCESS]%NC% %~1
goto :eof

:print_warning
echo %YELLOW%[WARNING]%NC% %~1
goto :eof

:print_error
echo %RED%[ERROR]%NC% %~1
goto :eof

REM 显示帮助信息
:show_help
echo SalvoRsTool 测试运行器 (Windows)
echo.
echo 用法: %~nx0 [选项] [测试类/方法]
echo.
echo 选项:
echo   -h, --help          显示此帮助信息
echo   -a, --all           运行所有测试
echo   -p, --pattern       运行PSI模式匹配测试
echo   -t, --tool          运行工具管理器测试
echo   -s, --service       运行服务层测试
echo   -i, --integration   运行集成测试
echo   -c, --clean         清理并重新构建
echo   -v, --verbose       详细输出
echo   --stats             显示测试统计
echo   --report            生成测试报告
echo.
echo 示例:
echo   %~nx0 -a                               # 运行所有测试
echo   %~nx0 -p                               # 运行PSI模式测试
echo   %~nx0 PsiElementPatternTest            # 运行特定测试类
echo   %~nx0 testRustFunctionPattern          # 运行特定测试方法
echo   %~nx0 -c -a                            # 清理构建并运行所有测试
goto :eof

REM 检查依赖
:check_dependencies
java -version >nul 2>&1
if errorlevel 1 (
    call :print_error "Java 未安装或不在 PATH 中"
    exit /b 1
)

if not exist "gradlew.bat" (
    call :print_error "未找到 gradlew.bat 脚本，请确保在项目根目录中运行此脚本"
    exit /b 1
)

call :print_info "依赖检查通过"
goto :eof

REM 清理构建
:clean_build
call :print_info "清理构建..."
gradlew.bat clean
if errorlevel 1 (
    call :print_error "构建清理失败"
    exit /b 1
)
call :print_success "构建清理完成"
goto :eof

REM 运行所有测试
:run_all_tests
call :print_info "运行所有测试..."
gradlew.bat test %VERBOSE_FLAG%
if errorlevel 1 (
    call :print_error "测试失败"
    exit /b 1
)
call :print_success "所有测试通过！"
goto :eof

REM 运行特定测试类
:run_test_class
set "test_class=%~1"
set "full_class_name=shop.itbug.salvorstool.%test_class%"

call :print_info "运行测试类: %test_class%"
gradlew.bat test --tests "%full_class_name%" %VERBOSE_FLAG%

if errorlevel 1 (
    call :print_error "测试类 %test_class% 失败"
    exit /b 1
)
call :print_success "测试类 %test_class% 通过！"
goto :eof

REM 运行特定测试方法
:run_test_method
set "test_method=%~1"

call :print_info "搜索并运行测试方法: %test_method%"

REM 尝试在各个测试类中找到方法
set "test_classes=PsiElementPatternTest ToolManagerTest ServiceTest IntegrationTest"
set "found=false"

for %%c in (%test_classes%) do (
    set "full_name=shop.itbug.salvorstool.%%c.%test_method%"

    REM 尝试运行测试方法
    gradlew.bat test --tests "!full_name!" %VERBOSE_FLAG% >nul 2>&1
    if not errorlevel 1 (
        call :print_info "在 %%c 中找到方法 %test_method%"
        gradlew.bat test --tests "!full_name!" %VERBOSE_FLAG%
        set "found=true"
        goto :method_found
    )
)

:method_found
if "%found%"=="false" (
    call :print_warning "未找到测试方法 %test_method%，尝试模糊匹配..."
    gradlew.bat test --tests "*%test_method%*" %VERBOSE_FLAG%
)
goto :eof

REM 生成测试报告
:generate_test_report
call :print_info "生成测试报告..."
gradlew.bat test jacocoTestReport

set "report_file=build\reports\jacoco\test\html\index.html"
if exist "%report_file%" (
    call :print_success "测试报告已生成: %report_file%"

    REM 尝试在浏览器中打开报告
    start "" "%report_file%"
) else (
    call :print_warning "测试报告未生成"
)
goto :eof

REM 显示测试统计
:show_test_stats
call :print_info "测试统计信息:"

set "test_dir=src\test\kotlin\shop\itbug\salvorstool"
if exist "%test_dir%" (
    REM 计算测试文件数量
    set "test_files=0"
    for /r "%test_dir%" %%f in (*.kt) do (
        set /a test_files+=1
    )

    echo   测试文件数量: !test_files!

    echo   测试文件列表:
    for /r "%test_dir%" %%f in (*.kt) do (
        echo     - %%~nxf
    )
) else (
    call :print_warning "测试目录不存在: %test_dir%"
)
goto :eof

REM 主函数
:main
set "CLEAN_BUILD=false"
set "VERBOSE_FLAG="
set "SHOW_STATS=false"
set "GENERATE_REPORT=false"

REM 解析命令行参数
:parse_args
if "%~1"=="" goto :no_args
if "%~1"=="-h" goto :help
if "%~1"=="--help" goto :help
if "%~1"=="-a" goto :all_tests
if "%~1"=="--all" goto :all_tests
if "%~1"=="-p" goto :pattern_tests
if "%~1"=="--pattern" goto :pattern_tests
if "%~1"=="-t" goto :tool_tests
if "%~1"=="--tool" goto :tool_tests
if "%~1"=="-s" goto :service_tests
if "%~1"=="--service" goto :service_tests
if "%~1"=="-i" goto :integration_tests
if "%~1"=="--integration" goto :integration_tests
if "%~1"=="-c" goto :set_clean
if "%~1"=="--clean" goto :set_clean
if "%~1"=="-v" goto :set_verbose
if "%~1"=="--verbose" goto :set_verbose
if "%~1"=="--stats" goto :set_stats
if "%~1"=="--report" goto :set_report

REM 处理未知参数（可能是测试类或方法名）
call :check_dependencies
if "%CLEAN_BUILD%"=="true" call :clean_build

REM 判断是测试类还是测试方法
echo %~1 | findstr "Test" >nul
if not errorlevel 1 (
    call :run_test_class "%~1"
) else (
    call :run_test_method "%~1"
)
exit /b 0

:help
call :show_help
exit /b 0

:all_tests
call :check_dependencies
if "%CLEAN_BUILD%"=="true" call :clean_build
call :run_all_tests
exit /b 0

:pattern_tests
call :check_dependencies
call :run_test_class "PsiElementPatternTest"
exit /b 0

:tool_tests
call :check_dependencies
call :run_test_class "ToolManagerTest"
exit /b 0

:service_tests
call :check_dependencies
call :run_test_class "ServiceTest"
exit /b 0

:integration_tests
call :check_dependencies
call :run_test_class "IntegrationTest"
exit /b 0

:set_clean
set "CLEAN_BUILD=true"
shift
goto :parse_args

:set_verbose
set "VERBOSE_FLAG=--info"
shift
goto :parse_args

:set_stats
set "SHOW_STATS=true"
shift
goto :parse_args

:set_report
set "GENERATE_REPORT=true"
shift
goto :parse_args

:no_args
REM 处理特殊模式
if "%SHOW_STATS%"=="true" (
    call :show_test_stats
    exit /b 0
)

if "%GENERATE_REPORT%"=="true" (
    call :check_dependencies
    call :generate_test_report
    exit /b 0
)

REM 默认显示帮助
call :show_help
exit /b 0

REM 启动主函数
call :main %*
