# ActixApiWindow - Actix-Web API Endpoints Explorer

## 概述

ActixApiWindow 是 RustX 插件中的一个功能模块，专门为 Actix-Web 项目提供 API 端点的可视化管理界面。它模仿了 IntelliJ IDEA 内置的 Endpoints 窗口，为 Rust 开发者提供了一个直观、高效的 API 管理工具。

## 功能特性

### 🚀 核心功能

- **自动 API 发现**: 自动扫描项目中的 Actix-Web 路由定义
- **实时搜索**: 支持按 URL、HTTP 方法、注释内容进行实时过滤
- **可视化展示**: 清晰显示 API 端点的方法、路径、源文件信息
- **代码导航**: 双击或右键菜单快速跳转到对应的处理函数
- **HTTP 客户端集成**: 生成 HTTP 请求模板，便于测试

### 🎨 用户界面

- **工具栏**: 模块选择、类型过滤、框架选择、刷新按钮
- **搜索栏**: 支持多种搜索条件的实时过滤
- **API 列表**: 树形结构显示所有 API 端点
- **详情面板**: HTTP Client、OpenAPI、Examples、Documentation 多标签页
- **右键菜单**: 快速操作菜单（跳转定义、复制 URL、生成请求等）

### 🔍 智能识别

支持识别以下 Actix-Web 路由模式：

```rust
// 基础路由宏
#[get("/users")]
#[post("/users")]
#[put("/users/{id}")]
#[delete("/users/{id}")]
#[patch("/users/{id}")]
#[head("/api/info")]

// 通用路由宏
#[route("/users", method = "GET")]
#[route("/users", method = "POST")]

// 带参数的路由
#[get("/users/{id}/posts")]
#[post("/api/v1/users/{user_id}/profile")]
```

## 安装和使用

### 前置条件

1. IntelliJ IDEA 2023.1 或更高版本
2. 已安装 Rust 插件
3. 项目包含 `actix-web` 依赖

### 打开窗口

**方法一：通过菜单**
```
Tools → Actix Tools → Show Actix Endpoints
```

**方法二：通过工具窗口**
- 在 IDE 底部找到 "Actix Endpoints" 工具窗口标签

### 基本操作

1. **查看 API 列表**: 窗口会自动扫描并显示项目中的所有 Actix-Web 端点
2. **搜索过滤**: 在搜索框中输入关键词进行实时过滤
3. **查看详情**: 选择任意 API 端点，在下方查看 HTTP 请求模板
4. **跳转代码**: 双击端点或使用右键菜单跳转到源码
5. **复制 URL**: 右键选择 "Copy URL" 快速复制完整的 API 地址

## 技术架构

### 核心组件

```
ActixApiWindow (主窗口)
├── ActixApiWindowFactory (工具窗口工厂)
├── ShowActixApiWindowAction (显示窗口动作)
├── ActixProjectService (项目服务)
├── ActixApiModelParser (API 解析器)
└── ActixApiModel (API 数据模型)
```

### 数据流

```
项目文件 → PSI解析 → ActixApiModelParser → ActixApiModel → UI显示
```

### 依赖关系

- **IntelliJ Platform SDK**: 插件开发框架
- **Rust PSI**: Rust 语言程序结构接口
- **Kotlin Coroutines**: 异步处理
- **Swing**: 用户界面组件

## HTTP 方法颜色标识

| 方法 | 颜色 | 十六进制 | 描述 |
|------|------|----------|------|
| GET | 蓝色 | #61AFFE | 查询操作 |
| POST | 绿色 | #49CC90 | 创建操作 |
| PUT | 橙色 | #FCA130 | 更新操作 |
| DELETE | 红色 | #F93E3E | 删除操作 |
| PATCH | 青色 | #50E3C2 | 部分更新 |
| HEAD | 紫色 | #9013FE | 头部信息 |

## 示例项目

在 `examples/actix-example/` 目录中提供了一个完整的示例项目，包含：

- 用户管理 API (CRUD 操作)
- 搜索功能
- 嵌套资源 (用户的文章)
- 健康检查端点
- API 版本控制

运行示例：
```bash
cd examples/actix-example
cargo run
```

## 配置和自定义

### 项目检测

插件会自动检测包含以下条件的项目：
- 项目根目录存在 `Cargo.toml` 文件
- `Cargo.toml` 中包含 `actix-web` 依赖

### 手动刷新

如果添加了新的路由但窗口没有更新，可以：
1. 点击工具栏的刷新按钮
2. 重新打开工具窗口
3. 重启 IDE (极少数情况)

## 故障排除

### 常见问题

**Q: 工具窗口不显示**
A: 确保项目是 Rust 项目且包含 `actix-web` 依赖

**Q: API 端点列表为空**
A: 检查路由是否使用了标准的 Actix-Web 宏，点击刷新按钮重新扫描

**Q: 搜索功能不工作**
A: 确认搜索关键词拼写正确，支持部分匹配

**Q: 跳转到代码失败**
A: 确保 IDE 的索引已完成，代码文件没有语法错误

### 调试信息

启用调试日志：
```
Help → Diagnostic Tools → Debug Log Settings
添加：shop.itbug.salvorstool
```

## 开发计划

### 已完成 ✅
- [x] 基础 UI 界面
- [x] API 自动发现
- [x] 实时搜索过滤
- [x] 代码导航功能
- [x] HTTP 请求模板生成
- [x] 右键菜单操作

### 开发中 🚧
- [ ] 实际 HTTP 请求执行
- [ ] OpenAPI 规范生成
- [ ] API 文档自动生成

### 计划中 📋
- [ ] API 测试功能
- [ ] 请求/响应示例
- [ ] API 性能分析
- [ ] 批量操作功能

## 贡献指南

### 开发环境设置

1. 克隆项目仓库
2. 导入到 IntelliJ IDEA
3. 配置 Kotlin 和 Gradle
4. 运行测试确保环境正常

### 代码结构

```
src/main/kotlin/shop/itbug/salvorstool/
├── window/              # 窗口组件
│   ├── ActixApiWindow.kt
│   └── ActixApiWindowFactory.kt
├── action/              # 用户操作
│   └── ShowActixApiWindowAction.kt
├── service/             # 业务服务
│   └── ActixProjectService.kt
├── model/               # 数据模型
│   └── ActixApiModel.kt
└── parser/              # 代码解析
    └── ActixApiModelParser.kt
```

### 提交规范

- 使用 conventional commits 格式
- 添加适当的测试用例
- 更新相关文档

## 许可证

本项目采用与 RustX 插件相同的许可证。

## 联系方式

- 项目主页: [SalvoRsToolDocument](https://mdddj.github.io/SalvoRsToolDocument)
- 作者邮箱: hlxsmail@gmail.com
- 问题反馈: 通过 GitHub Issues 提交

---

**版本**: 1.0.0  
**最后更新**: 2024-12-19  
**兼容性**: IntelliJ IDEA 2023.1+, Rust Plugin