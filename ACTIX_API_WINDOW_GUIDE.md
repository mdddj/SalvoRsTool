# Actix API Window 使用指南

## 概述

ActixApiWindow 是一个为 Actix-Web 项目设计的 API 端点查看器，类似于 IntelliJ IDEA 内置的 Endpoints 窗口。它可以帮助开发者快速浏览、搜索和管理项目中的 API 端点。

## 功能特性

### 1. API 端点列表显示
- 自动扫描项目中的 Actix-Web 路由
- 显示 HTTP 方法（GET、POST、PUT、DELETE 等）
- 显示 API 路径和对应的处理函数
- 支持颜色区分不同的 HTTP 方法

### 2. 搜索和过滤
- 实时搜索功能
- 支持按 URL 路径搜索
- 支持按 HTTP 方法搜索
- 支持按注释内容搜索

### 3. 工具栏功能
- **Module**: 选择要显示的模块（默认显示所有模块）
- **Type**: 显示服务器类型（HTTP-Server）
- **Framework**: 显示框架类型（Actix-Web）
- **刷新按钮**: 重新扫描项目中的 API 端点

### 4. HTTP Client 集成
- 生成 HTTP 请求模板
- 支持不同 HTTP 方法的请求格式
- 提供 Submit Request 和 Open in Editor 功能

### 5. 右键菜单
- **Go to Definition**: 跳转到 API 处理函数定义
- **Copy URL**: 复制 API 完整 URL 到剪贴板
- **Generate HTTP Request**: 生成 HTTP 请求（计划功能）

## 使用方法

### 打开窗口
1. 通过菜单：`Tools` → `Actix Tools` → `Show Actix Endpoints`
2. 通过工具窗口：在底部工具栏找到 "Actix Endpoints" 标签

### 浏览 API 端点
1. 窗口会自动扫描项目中的 Actix-Web 路由
2. API 端点按照发现顺序显示在列表中
3. 每个端点显示：
   - HTTP 方法（带颜色标识）
   - API 路径
   - 源文件名
   - 注释（如果有）

### 搜索 API
1. 在搜索框中输入关键词
2. 支持搜索内容：
   - URL 路径：如 `/users`、`/api/v1`
   - HTTP 方法：如 `GET`、`POST`
   - 注释内容

### 生成 HTTP 请求
1. 选择一个 API 端点
2. 在底部 "HTTP Client" 标签页查看生成的请求
3. 点击 "Submit Request" 执行请求（功能开发中）
4. 点击 "Open in Editor" 跳转到函数定义

## HTTP 方法颜色标识

- **GET**: 蓝色 (#61AFFE)
- **POST**: 绿色 (#49CC90)
- **PUT**: 橙色 (#FCA130)
- **DELETE**: 红色 (#F93E3E)
- **PATCH**: 青色 (#50E3C2)
- **HEAD**: 紫色 (#9013FE)

## 支持的 Actix-Web 路由格式

窗口可以识别以下 Actix-Web 路由定义格式：

```rust
// 基本路由
#[get("/users")]
async fn get_users() -> impl Responder {
    // ...
}

// 带参数的路由
#[post("/users/{id}")]
async fn create_user(path: web::Path<u32>) -> impl Responder {
    // ...
}

// 组合路由
#[route("/users", method = "GET")]
#[route("/users", method = "POST")]
async fn users_handler() -> impl Responder {
    // ...
}
```

## 注意事项

1. **项目要求**: 只有包含 `actix-web` 依赖的 Rust 项目才会显示此工具窗口
2. **自动刷新**: 窗口会在项目启动时自动扫描，也可以手动点击刷新按钮
3. **性能**: 大型项目的初次扫描可能需要一些时间
4. **文件更改**: 添加新的路由后，需要手动刷新窗口来更新列表

## 故障排除

### 窗口不显示
- 确认项目的 `Cargo.toml` 中包含 `actix-web` 依赖
- 确认项目根目录存在 `Cargo.toml` 文件

### API 端点不显示
- 点击刷新按钮重新扫描
- 确认路由使用了标准的 Actix-Web 宏（如 `#[get]`、`#[post]` 等）
- 检查 IDE 的索引是否完成

### 搜索不工作
- 确认搜索关键词拼写正确
- 尝试使用部分匹配而不是完整匹配

## 开发计划

未来版本计划添加的功能：
- [ ] 实际的 HTTP 请求执行
- [ ] OpenAPI 规范生成
- [ ] API 文档生成
- [ ] 请求/响应示例
- [ ] API 测试功能

## 技术实现

窗口基于以下技术实现：
- **Kotlin**: 主要开发语言
- **IntelliJ Platform SDK**: IDE 插件框架
- **Rust PSI**: Rust 语言的程序结构接口
- **Swing**: 用户界面框架

## 贡献

如果您发现问题或有改进建议，请：
1. 在项目仓库提交 Issue
2. 提交 Pull Request
3. 联系开发者

---

**版本**: 1.0.0  
**最后更新**: 2024-12-19  
**兼容性**: IntelliJ IDEA 2023.1+, Rust Plugin