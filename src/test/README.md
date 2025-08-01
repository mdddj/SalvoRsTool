# SalvoRsTool 测试套件

这是 SalvoRsTool IDEA 插件的测试套件，专门用于测试 PSI 元素模式匹配和验证功能。

## 测试结构

### 核心测试类

#### 1. `PsiElementPatternTest.kt`
- **功能**: 测试各种 Rust PSI 元素的匹配模式
- **测试内容**:
  - Rust 函数模式匹配
  - Router 返回类型的函数匹配
  - Struct 模式匹配（包括带属性的结构体）
  - Let 声明模式匹配
  - 方法调用模式匹配（特别是 Salvo 的路由方法）
  - 复合模式匹配
  - Salvo 属性宏模式匹配

#### 2. `ToolManagerTest.kt`
- **功能**: 测试插件中的各种 PSI 管理器和工具类
- **测试内容**:
  - `RsFunctionManager` - Router 返回类型检测和 Let 声明管理
  - `MyRsStructManager` - 结构体管理和 TypeScript 接口生成
  - `RsLetDeclManager` - Let 声明管理和 API 提取
  - `RsPsiElementTools` - PSI 元素工具和文档注释提取
  - 复杂路由结构解析

#### 3. `ServiceTest.kt`
- **功能**: 测试服务层功能，包括 API 扫描和项目服务
- **测试内容**:
  - `SalvoApiService` - API 扫描功能
  - `RustProjectService` - 依赖检测
  - `SalvoApiItem` 模型验证
  - 复杂嵌套路由扫描
  - 异步 API 扫描
  - 服务生命周期管理
  - 错误处理和边界情况
  - API 消息传递机制

#### 4. `TestPsiPatterns.kt`
- **功能**: PSI 元素模式测试工具类
- **提供功能**:
  - 预定义的常用 PSI 匹配模式
  - 自定义匹配器创建方法
  - PSI 元素验证工具方法
  - PSI 树遍历工具方法
  - 常用组合模式

#### 5. `IntegrationTest.kt`
- **功能**: 综合集成测试
- **测试内容**:
  - 完整 Salvo 应用集成测试
  - 多文件项目结构模拟
  - 插件扩展性和自定义功能测试
  - 性能和稳定性测试

## 主要测试功能

### PSI 元素模式匹配

#### 函数相关模式
```kotlin
// 匹配返回 Router 类型的函数
val ROUTER_FUNCTION_PATTERN

// 匹配指定名称的函数
fun functionWithName(name: String)

// 匹配异步函数
val ASYNC_FUNCTION_PATTERN

// 匹配带有特定属性的函数
fun functionWithAttribute(attributeName: String)
```

#### 结构体相关模式
```kotlin
// 匹配可序列化的结构体
val SERIALIZABLE_STRUCT_PATTERN

// 匹配可反序列化的结构体
val DESERIALIZABLE_STRUCT_PATTERN

// 匹配带有 derive 属性的结构体
val DERIVED_STRUCT_PATTERN
```

#### 方法调用相关模式
```kotlin
// 匹配 Salvo HTTP 方法调用
val SALVO_HTTP_METHOD_PATTERN

// 匹配 Router 构建方法调用
val ROUTER_BUILDER_METHOD_PATTERN
```

### 验证工具方法

```kotlin
// 验证函数是否返回 Router 类型
fun isRouterFunction(function: RsFunction): Boolean

// 验证结构体是否可序列化
fun isSerializableStruct(struct: RsStructItem): Boolean

// 验证 let 声明是否与 Router 相关
fun isRouterRelatedLet(letDecl: RsLetDecl): Boolean

// 验证方法调用是否为 HTTP 方法
fun isHttpMethodCall(methodCall: RsMethodCall): Boolean
```

### PSI 遍历工具

```kotlin
// 查找符合条件的元素
inline fun <reified T : PsiElement> findElementsOfType(
    root: PsiElement,
    crossinline predicate: (T) -> Boolean = { true }
): List<T>

// 根据模式查找元素
fun <T : PsiElement> findElementsByPattern(
    root: PsiElement,
    pattern: ElementPattern<T>
): List<T>
```

## 运行测试

### 使用 IntelliJ IDEA
1. 右键点击测试类或测试方法
2. 选择 "Run" 或 "Debug"

### 使用 Gradle
```bash
# 运行所有测试
./gradlew test

# 运行特定测试类
./gradlew test --tests "shop.itbug.salvorstool.PsiElementPatternTest"

# 运行特定测试方法
./gradlew test --tests "shop.itbug.salvorstool.PsiElementPatternTest.testRustFunctionPattern"
```

## 测试数据

测试使用内联的 Rust 代码作为测试数据，模拟真实的 Salvo 项目结构：

- **Router 函数**: 返回 `Router` 类型的函数
- **Handler 函数**: 带有 `#[handler]` 属性的异步函数
- **DTO 结构体**: 带有 `Serialize` 和 `Deserialize` 属性的结构体
- **API 路由**: 包含 HTTP 方法调用的路由定义

## 最佳实践

### 编写新测试
1. 使用描述性的测试方法名
2. 遵循 AAA 模式（Arrange, Act, Assert）
3. 使用 `TestPsiPatterns` 工具类提供的预定义模式
4. 为复杂的匹配逻辑创建可重用的自定义匹配器

### 测试数据管理
1. 使用内联代码字符串作为测试数据
2. 保持测试代码简洁但具有代表性
3. 测试边界情况和错误处理

### 性能考虑
1. 使用缓存机制避免重复计算
2. 测试大量元素时考虑性能影响
3. 为耗时操作设置合理的超时时间

## 扩展测试

要添加新的测试功能：

1. **新的 PSI 模式**: 在 `TestPsiPatterns.kt` 中添加新的模式定义
2. **新的工具方法**: 在对应的测试类中添加验证方法
3. **新的集成场景**: 在 `IntegrationTest.kt` 中添加完整的使用场景

## 依赖关系

测试依赖于以下组件：
- IntelliJ Platform Test Framework
- Rust Plugin PSI API
- Kotlin Coroutines（用于异步测试）
- 插件的核心业务逻辑类

## 故障排除

### 常见问题
1. **PSI 元素未找到**: 检查测试代码语法是否正确
2. **模式匹配失败**: 验证模式定义是否与实际 PSI 结构匹配
3. **异步测试超时**: 增加超时时间或检查异步逻辑

### 调试技巧
1. 使用 `println` 输出 PSI 元素信息
2. 利用 IntelliJ 的 PSI 查看器检查元素结构
3. 在测试中设置断点检查中间状态

## 贡献指南

1. 为新功能编写对应的测试
2. 确保测试覆盖率达到要求
3. 遵循现有的代码风格和命名约定
4. 更新文档说明新增的测试功能