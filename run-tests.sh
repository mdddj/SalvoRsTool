#!/bin/bash

# SalvoRsTool Test Runner Script
# 用于运行插件测试的便捷脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "SalvoRsTool 测试运行器"
    echo ""
    echo "用法: $0 [选项] [测试类/方法]"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示此帮助信息"
    echo "  -a, --all           运行所有测试"
    echo "  -p, --pattern       运行PSI模式匹配测试"
    echo "  -t, --tool          运行工具管理器测试"
    echo "  -s, --service       运行服务层测试"
    echo "  -i, --integration   运行集成测试"
    echo "  -c, --clean         清理并重新构建"
    echo "  -v, --verbose       详细输出"
    echo "  -w, --watch         监视模式（文件变化时自动重新运行）"
    echo ""
    echo "示例:"
    echo "  $0 -a                               # 运行所有测试"
    echo "  $0 -p                               # 运行PSI模式测试"
    echo "  $0 PsiElementPatternTest            # 运行特定测试类"
    echo "  $0 testRustFunctionPattern          # 运行特定测试方法"
    echo "  $0 -c -a                            # 清理构建并运行所有测试"
}

# 检查依赖
check_dependencies() {
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装或不在 PATH 中"
        exit 1
    fi

    if [ ! -f "./gradlew" ]; then
        print_error "未找到 gradlew 脚本，请确保在项目根目录中运行此脚本"
        exit 1
    fi

    print_info "依赖检查通过"
}

# 清理构建
clean_build() {
    print_info "清理构建..."
    ./gradlew clean
    print_success "构建清理完成"
}

# 运行所有测试
run_all_tests() {
    print_info "运行所有测试..."
    ./gradlew test ${VERBOSE_FLAG}
    if [ $? -eq 0 ]; then
        print_success "所有测试通过！"
    else
        print_error "测试失败"
        exit 1
    fi
}

# 运行特定测试类
run_test_class() {
    local test_class=$1
    local full_class_name="shop.itbug.salvorstool.${test_class}"

    print_info "运行测试类: ${test_class}"
    ./gradlew test --tests "${full_class_name}" ${VERBOSE_FLAG}

    if [ $? -eq 0 ]; then
        print_success "测试类 ${test_class} 通过！"
    else
        print_error "测试类 ${test_class} 失败"
        exit 1
    fi
}

# 运行特定测试方法
run_test_method() {
    local test_method=$1

    print_info "搜索并运行测试方法: ${test_method}"

    # 尝试在各个测试类中找到方法
    local test_classes=("PsiElementPatternTest" "ToolManagerTest" "ServiceTest" "IntegrationTest")
    local found=false

    for class in "${test_classes[@]}"; do
        local full_name="shop.itbug.salvorstool.${class}.${test_method}"

        # 检查测试方法是否存在
        if ./gradlew test --tests "${full_name}" --dry-run 2>/dev/null | grep -q "${test_method}"; then
            print_info "在 ${class} 中找到方法 ${test_method}"
            ./gradlew test --tests "${full_name}" ${VERBOSE_FLAG}
            found=true
            break
        fi
    done

    if [ "$found" = false ]; then
        print_warning "未找到测试方法 ${test_method}，尝试模糊匹配..."
        ./gradlew test --tests "*${test_method}*" ${VERBOSE_FLAG}
    fi
}

# 监视模式
watch_mode() {
    print_info "启动监视模式..."
    print_info "监视 src/main/kotlin 和 src/test/kotlin 目录的变化"
    print_info "按 Ctrl+C 退出监视模式"

    if ! command -v fswatch &> /dev/null; then
        print_warning "fswatch 未安装，使用简单的轮询模式"
        while true; do
            print_info "运行测试..."
            run_all_tests
            print_info "等待 30 秒后重新运行..."
            sleep 30
        done
    else
        fswatch -o src/main/kotlin src/test/kotlin | while read f; do
            print_info "检测到文件变化，重新运行测试..."
            run_all_tests
        done
    fi
}

# 生成测试报告
generate_test_report() {
    print_info "生成测试报告..."
    ./gradlew test jacocoTestReport

    local report_file="build/reports/jacoco/test/html/index.html"
    if [ -f "$report_file" ]; then
        print_success "测试报告已生成: $report_file"

        # 尝试在浏览器中打开报告
        if command -v open &> /dev/null; then
            open "$report_file"
        elif command -v xdg-open &> /dev/null; then
            xdg-open "$report_file"
        else
            print_info "请手动打开测试报告: $report_file"
        fi
    else
        print_warning "测试报告未生成"
    fi
}

# 显示测试统计
show_test_stats() {
    print_info "测试统计信息:"

    local test_dir="src/test/kotlin/shop/itbug/salvorstool"
    if [ -d "$test_dir" ]; then
        local test_files=$(find "$test_dir" -name "*.kt" | wc -l)
        local test_methods=$(grep -r "fun test" "$test_dir" | wc -l)

        echo "  测试文件数量: $test_files"
        echo "  测试方法数量: $test_methods"

        echo "  测试文件列表:"
        find "$test_dir" -name "*.kt" -exec basename {} \; | sed 's/^/    - /'
    else
        print_warning "测试目录不存在: $test_dir"
    fi
}

# 主函数
main() {
    local CLEAN_BUILD=false
    local VERBOSE_FLAG=""
    local WATCH_MODE=false
    local SHOW_STATS=false
    local GENERATE_REPORT=false

    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -a|--all)
                check_dependencies
                if [ "$CLEAN_BUILD" = true ]; then
                    clean_build
                fi
                run_all_tests
                exit 0
                ;;
            -p|--pattern)
                check_dependencies
                run_test_class "PsiElementPatternTest"
                exit 0
                ;;
            -t|--tool)
                check_dependencies
                run_test_class "ToolManagerTest"
                exit 0
                ;;
            -s|--service)
                check_dependencies
                run_test_class "ServiceTest"
                exit 0
                ;;
            -i|--integration)
                check_dependencies
                run_test_class "IntegrationTest"
                exit 0
                ;;
            -c|--clean)
                CLEAN_BUILD=true
                shift
                ;;
            -v|--verbose)
                VERBOSE_FLAG="--info"
                shift
                ;;
            -w|--watch)
                WATCH_MODE=true
                shift
                ;;
            --stats)
                SHOW_STATS=true
                shift
                ;;
            --report)
                GENERATE_REPORT=true
                shift
                ;;
            -*)
                print_error "未知选项: $1"
                show_help
                exit 1
                ;;
            *)
                check_dependencies
                if [ "$CLEAN_BUILD" = true ]; then
                    clean_build
                fi

                # 判断是测试类还是测试方法
                if [[ $1 == *"Test" ]]; then
                    run_test_class "$1"
                else
                    run_test_method "$1"
                fi
                exit 0
                ;;
        esac
    done

    # 处理特殊模式
    if [ "$SHOW_STATS" = true ]; then
        show_test_stats
        exit 0
    fi

    if [ "$GENERATE_REPORT" = true ]; then
        check_dependencies
        generate_test_report
        exit 0
    fi

    if [ "$WATCH_MODE" = true ]; then
        check_dependencies
        watch_mode
        exit 0
    fi

    # 默认显示帮助
    show_help
}

# 捕获中断信号
trap 'print_info "测试运行被中断"; exit 0' INT

# 运行主函数
main "$@"
