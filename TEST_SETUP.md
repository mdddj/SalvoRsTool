# SalvoRsTool 测试环境配置指南

本文档提供了 SalvoRsTool IDEA 插件测试环境的完整配置指南。

## 📋 环境要求

### 基础环境
- **Java**: JDK 21 或更高版本
- **IntelliJ IDEA**: 2025.1 或更高版本
- **Gradle**: 8.0+ (使用项目自带的 gradlew)
- **操作系统**: Windows, macOS, Linux

### 插件依赖
- **RustRover/Rust Plugin**: 用于 Rust 语言支持
- **Kotlin Plugin**: 用于测试代码编写
- **JavaScript Plugin**: 用于 TypeScript 接口生成测试

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone <your-repo-url>
cd SalvoRsTool
```

### 2. 验证环境
```bash
# Linux/Mac
./run-tests.sh --stats

# Windows
run-tests.bat --stats
```

### 3. 运行基础验证测试
```bash
# Linux/Mac
./run-tests.sh BasicValidationTest

# Windows
run-tests.bat BasicValidationTest
```

## 🔧 项目结构

```
SalvoRsTool/
├── src/
│   ├── main/kotlin/shop/itbug/salvorstool/     # 插件源码
│   └── test/kotlin/shop/itbug/salvorstool/     # 测试代码
│       ├── PsiElementPatternTest.kt            # PSI模式匹配测试
│       ├── ToolManagerTest.kt                  # 工具管理器测试  
│       ├── ServiceTest.kt                      # 服务层测试
│       ├── TestPsiPatterns.kt                  # PSI工具类
│       ├── IntegrationTest.kt                  # 集成测试
│       ├── BasicValidationTest.kt              # 基础验证测试
│       └── README.md                           # 测试文档
├── run-tests.sh                                # Linux/Mac测试脚本
├── run-tests.bat                               # Windows测试脚本
└── build.gradle.kts                            # 构建配置
```

## 🧪 测试类说明

### 1. BasicValidationTest.kt
**用途**: 验证测试环境和基础功能
**运行**: `./run-tests.sh BasicValidationTest`
- 验证导入和API可用性
- 检查服务实例化
- 测试PSI元素查找
- 验证工具类扩展方法

### 2. PsiElementPatternTest.kt  
**用途**: 测试PSI元素模式匹配
**运行**: `./run-tests.sh -p`
- Rust函数模式匹配
- 结构体模式匹配
- Let声明模式匹配
- 方法调用模式匹配
- 复合模式匹配

### 3. ToolManagerTest.kt
**用途**: 测试现有工具管理器
**运行**: `./run-tests.sh -t`
- RsFunctionManager测试
- MyRsStructManager测试
- RsLetDeclManager测试
- RsPsiElementTools测试

### 4. ServiceTest.kt
**用途**: 测试服务层功能
**运行**: `./run-tests.sh -s`
- SalvoApiService测试
- RustProjectService测试
- 异步API扫描测试
- 消息总线测试

### 5. IntegrationTest.kt
**用途**: 综合集成测试
**运行**: `./run-tests.sh -i`
- 完整Salvo应用模拟
- 多文件项目结构测试
- 性能和稳定性测试

### 6. TestPsiPatterns.kt
**用途**: PSI模式工具类
- 预定义匹配模式
- 自定义匹配器
- PSI遍历工具
- 验证工具方法

## 🎯 运行测试

### 使用测试脚本 (推荐)

#### Linux/Mac
```bash
# 运行所有测试
./run-tests.sh -a

# 运行特定测试类
./run-tests.sh -p              # PSI模式测试
./run-tests.sh -t              # 工具管理器测试
./run-tests.sh -s              # 服务层测试
./run-tests.sh -i              # 集成测试

# 运行特定测试方法
./run-tests.sh testRustFunctionPattern

# 清理构建并运行
./run-tests.sh -c -a

# 详细输出
./run-tests.sh -v -a

# 显示帮助
./run-tests.sh -h
```

#### Windows
```cmd
REM 运行所有测试
run-tests.bat -a

REM 运行特定测试类
run-tests.bat -p

REM 生成测试报告
run-tests.bat --report

REM 显示统计信息
run-tests.bat --stats
```

### 使用Gradle直接运行
```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "shop.itbug.salvorstool.PsiElementPatternTest"

# 运行特定测试方法
./gradlew test --tests "*.testRustFunctionPattern"

# 详细输出
./gradlew test --info
```

### 使用IntelliJ IDEA
1. 打开项目
2. 右键点击测试类或方法
3. 选择 "Run" 或 "Debug"
4. 查看测试结果

## 🔍 调试测试

### 1. 查看测试输出
```bash
# 运行单个测试并查看详细输出
./run-tests.sh -v BasicValidationTest
```

### 2. 使用IDEA调试器
1. 在测试方法中设置断点
2. 右键选择 "Debug"
3. 检查变量值和执行流程

### 3. 查看PSI结构
在测试中添加以下代码查看PSI结构:
```kotlin
println("PSI结构: ${element.javaClass.simpleName}")
println("文本内容: ${element.text}")
```

### 4. 检查日志输出
测试运行时会输出详细信息:
```
✅ 基础导入 - 通过
✅ 服务实例化 - 通过  
❌ PSI元素查找 - 失败
```

## ⚠️ 常见问题

### 1. 测试失败：找不到PSI元素
**原因**: Rust代码语法错误或PSI解析失败
**解决**:
- 检查测试代码的Rust语法
- 确保使用正确的PSI元素类型
- 添加调试输出查看PSI结构

### 2. 测试失败：服务不可用
**原因**: 服务未正确初始化或依赖缺失
**解决**:
- 运行BasicValidationTest检查环境
- 确保所有必需的插件已安装
- 检查服务实例化代码

### 3. 测试失败：方法不存在
**原因**: 使用了不存在的API或方法名错误
**解决**:
- 检查方法名拼写
- 查看源码确认API存在
- 使用BasicValidationTest验证导入

### 4. 构建失败：依赖问题
**原因**: Gradle依赖配置错误
**解决**:
```bash
# 清理构建
./gradlew clean

# 重新构建
./gradlew build

# 检查依赖
./gradlew dependencies
```

### 5. PSI模式匹配失败
**原因**: 模式定义错误或条件不匹配
**解决**:
- 使用TestPsiPatterns中的预定义模式
- 添加调试输出检查匹配条件
- 使用IDEA的PSI查看器检查结构

## 📊 测试报告

### 生成测试报告
```bash
# 生成HTML测试报告
./gradlew test jacocoTestReport

# 打开报告（自动在浏览器中打开）
./run-tests.sh --report
```

### 报告位置
- **HTML报告**: `build/reports/tests/test/index.html`
- **覆盖率报告**: `build/reports/jacoco/test/html/index.html`
- **构建报告**: `build/reports/`

## 🛠️ 开发新测试

### 1. 创建测试类模板
```kotlin
package shop.itbug.salvorstool

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class MyNewTest : BasePlatformTestCase() {
    
    override fun getTestDataPath(): String = "src/test/testData"
    
    fun testMyFeature() {
        val code = """
            // Rust code here
        """.trimIndent()
        
        val file = myFixture.configureByText("test.rs", code)
        
        // 测试逻辑
        assertNotNull("文件应该存在", file)
    }
}
```

### 2. 使用TestPsiPatterns工具
```kotlin
// 使用预定义模式
val routerFunctions = TestPsiPatterns.findElementsByPattern(
    file, 
    TestPsiPatterns.ROUTER_FUNCTION_PATTERN
)

// 使用自定义匹配器
val customPattern = TestPsiPatterns.customFunctionMatcher("isSpecialFunction") { function ->
    function.name?.startsWith("special_") == true
}
```

### 3. 添加验证
```kotlin
// 基础验证
assertNotNull("元素应该存在", element)
assertTrue("条件应该满足", condition)
assertEquals("值应该相等", expected, actual)

// 集合验证
assertTrue("应该找到元素", list.isNotEmpty())
assertEquals("数量应该匹配", expectedCount, list.size)
```

## 🔄 持续集成

### GitHub Actions配置
```yaml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run Tests
        run: ./gradlew test
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results
          path: build/reports/tests/
```

## 📚 参考资料

- [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Testing Plugins](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html)
- [PSI Elements](https://plugins.jetbrains.com/docs/intellij/psi-elements.html)
- [Kotlin Test Framework](https://kotlinlang.org/docs/jvm-test-using-junit.html)

## 🤝 贡献指南

1. 为新功能编写对应的测试
2. 确保所有测试通过
3. 添加必要的文档说明
4. 使用一致的命名约定
5. 提交前运行完整测试套件

---

**最后更新**: 2024年
**维护者**: SalvoRsTool开发团队

如有问题，请查看测试输出或创建Issue。