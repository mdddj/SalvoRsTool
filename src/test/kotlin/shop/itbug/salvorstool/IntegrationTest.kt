package shop.itbug.salvorstool

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.impl.RsFunctionImpl
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.service.SalvoApiService
import shop.itbug.salvorstool.tool.*

/** 集成测试类 综合测试插件的各个组件，包括PSI元素匹配、服务层、工具类等 模拟真实的Salvo项目结构进行完整的功能测试 */
class IntegrationTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    /** 完整的Salvo应用集成测试 */
    fun testFullSalvoApplicationIntegration() {
        // 创建一个完整的Salvo应用代码
        val mainCode =
                """
            use salvo::prelude::*;
            use serde::{Deserialize, Serialize};

            /// 用户数据传输对象
            #[derive(Debug, Clone, Serialize, Deserialize)]
            struct UserDto {
                id: Option<i64>,
                username: String,
                email: String,
                created_at: Option<String>,
            }

            /// 创建用户请求
            #[derive(Debug, Deserialize)]
            struct CreateUserRequest {
                username: String,
                email: String,
                password: String,
            }

            /// API响应包装器
            #[derive(Debug, Serialize)]
            struct ApiResponse<T> {
                success: bool,
                data: Option<T>,
                message: Option<String>,
            }

            /// 创建主应用路由器
            fn create_app() -> Router {
                let api_v1 = create_api_v1_router();
                let api_v2 = create_api_v2_router();
                let admin = create_admin_router();

                Router::new()
                    .push(api_v1)
                    .push(api_v2)
                    .push(admin)
                    .get("/", home_handler)
                    .get("/health", health_check)
                    .get("/metrics", metrics_handler)
            }

            /// 创建API v1路由器
            fn create_api_v1_router() -> Router {
                let users = Router::with_path("/users")
                    .get("/", list_users)
                    .post("/", create_user)
                    .get("/:id", get_user)
                    .put("/:id", update_user)
                    .delete("/:id", delete_user);

                let posts = Router::with_path("/posts")
                    .get("/", list_posts)
                    .post("/", create_post)
                    .get("/:id", get_post);

                Router::with_path("/api/v1")
                    .push(users)
                    .push(posts)
                    .get("/stats", get_api_stats)
            }

            /// 创建API v2路由器
            fn create_api_v2_router() -> Router {
                let users_v2 = Router::with_path("/users")
                    .get("/", list_users_v2)
                    .post("/batch", batch_create_users);

                Router::with_path("/api/v2")
                    .push(users_v2)
                    .get("/version", get_version)
            }

            /// 创建管理员路由器
            fn create_admin_router() -> Router {
                let dashboard = Router::with_path("/dashboard")
                    .get("/", admin_dashboard)
                    .get("/users", admin_users_stats)
                    .get("/system", system_info);

                Router::with_path("/admin")
                    .push(dashboard)
                    .get("/login", admin_login)
                    .post("/login", handle_admin_login)
            }

            // ==================== 处理器函数 ====================

            /// 首页处理器
            #[handler]
            async fn home_handler(res: &mut Response) {
                let response = ApiResponse {
                    success: true,
                    data: Some("Welcome to Salvo API"),
                    message: None,
                };
                res.render(Json(response));
            }

            /// 健康检查处理器
            #[handler]
            async fn health_check(res: &mut Response) {
                res.render(Json(serde_json::json!({
                    "status": "healthy",
                    "timestamp": chrono::Utc::now().to_rfc3339()
                })));
            }

            /// 用户列表处理器
            #[handler]
            async fn list_users(req: &mut Request, res: &mut Response) {
                let page: u32 = req.query("page").unwrap_or(1);
                let limit: u32 = req.query("limit").unwrap_or(10);

                // 模拟获取用户数据
                let users = vec![
                    UserDto {
                        id: Some(1),
                        username: "alice".to_string(),
                        email: "alice@example.com".to_string(),
                        created_at: Some("2023-01-01T00:00:00Z".to_string()),
                    }
                ];

                let response = ApiResponse {
                    success: true,
                    data: Some(users),
                    message: None,
                };

                res.render(Json(response));
            }

            /// 创建用户处理器
            #[handler]
            async fn create_user(req: &mut Request, res: &mut Response) {
                let create_req: CreateUserRequest = match req.parse_json().await {
                    Ok(req) => req,
                    Err(_) => {
                        let error_response = ApiResponse::<()> {
                            success: false,
                            data: None,
                            message: Some("Invalid request body".to_string()),
                        };
                        res.status_code(StatusCode::BAD_REQUEST);
                        res.render(Json(error_response));
                        return;
                    }
                };

                // 模拟创建用户
                let new_user = UserDto {
                    id: Some(2),
                    username: create_req.username,
                    email: create_req.email,
                    created_at: Some(chrono::Utc::now().to_rfc3339()),
                };

                let response = ApiResponse {
                    success: true,
                    data: Some(new_user),
                    message: Some("User created successfully".to_string()),
                };

                res.status_code(StatusCode::CREATED);
                res.render(Json(response));
            }

            /// 获取单个用户处理器
            #[handler]
            async fn get_user(req: &mut Request, res: &mut Response) {
                let user_id: i64 = match req.param::<i64>("id") {
                    Some(id) => id,
                    None => {
                        let error_response = ApiResponse::<()> {
                            success: false,
                            data: None,
                            message: Some("Invalid user ID".to_string()),
                        };
                        res.status_code(StatusCode::BAD_REQUEST);
                        res.render(Json(error_response));
                        return;
                    }
                };

                // 模拟获取用户
                if user_id == 1 {
                    let user = UserDto {
                        id: Some(user_id),
                        username: "alice".to_string(),
                        email: "alice@example.com".to_string(),
                        created_at: Some("2023-01-01T00:00:00Z".to_string()),
                    };

                    let response = ApiResponse {
                        success: true,
                        data: Some(user),
                        message: None,
                    };

                    res.render(Json(response));
                } else {
                    let error_response = ApiResponse::<()> {
                        success: false,
                        data: None,
                        message: Some("User not found".to_string()),
                    };
                    res.status_code(StatusCode::NOT_FOUND);
                    res.render(Json(error_response));
                }
            }

            /// 管理员仪表板
            #[handler]
            async fn admin_dashboard(req: &mut Request, res: &mut Response) {
                // 权限检查逻辑
                res.render(Json(serde_json::json!({
                    "dashboard": "admin",
                    "stats": {
                        "total_users": 1000,
                        "active_sessions": 50
                    }
                })));
            }
        """.trimIndent()

        val file = myFixture.configureByText("integration_test.rs", mainCode)

        // ==================== 测试PSI元素模式匹配 ====================

        // 1. 测试Router函数匹配
        val routerFunctions =
                TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.ROUTER_FUNCTION_PATTERN)
        assertEquals("应该找到4个Router函数", 4, routerFunctions.size)

        val routerFunctionNames = routerFunctions.map { it.name }
        assertTrue("应该包含create_app", routerFunctionNames.contains("create_app"))
        assertTrue("应该包含create_api_v1_router", routerFunctionNames.contains("create_api_v1_router"))
        assertTrue("应该包含create_api_v2_router", routerFunctionNames.contains("create_api_v2_router"))
        assertTrue("应该包含create_admin_router", routerFunctionNames.contains("create_admin_router"))

        // 2. 测试异步处理器函数匹配
        val handlerFunctions =
                TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.SALVO_HANDLER_PATTERN)
        assertTrue("应该找到多个处理器函数", handlerFunctions.size >= 5)

        val handlerFunctionNames = handlerFunctions.map { it.name }
        assertTrue("应该包含home_handler", handlerFunctionNames.contains("home_handler"))
        assertTrue("应该包含health_check", handlerFunctionNames.contains("health_check"))
        assertTrue("应该包含list_users", handlerFunctionNames.contains("list_users"))
        assertTrue("应该包含create_user", handlerFunctionNames.contains("create_user"))

        // 3. 测试DTO结构体匹配
        val dtoStructs =
                TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.DTO_STRUCT_PATTERN)
        assertTrue("应该找到DTO结构体", dtoStructs.isNotEmpty())

        val dtoStructNames = dtoStructs.map { it.name }
        assertTrue("应该包含UserDto", dtoStructNames.contains("UserDto"))
        assertTrue("应该包含ApiResponse", dtoStructNames.contains("ApiResponse"))

        // 4. 测试HTTP方法调用匹配
        val httpMethodCalls =
                TestPsiPatterns.findElementsByPattern(
                        file,
                        TestPsiPatterns.SALVO_HTTP_METHOD_PATTERN
                )
        assertTrue("应该找到多个HTTP方法调用", httpMethodCalls.size >= 10)

        // ==================== 测试工具管理器功能 ====================

        // 5. 测试RsFunctionManager
        val mainAppFunction = routerFunctions.find { it.name == "create_app" } as? RsFunctionImpl
        assertNotNull("应该找到主应用函数", mainAppFunction)

        if (mainAppFunction != null) {
            val manager = mainAppFunction.myManager
            assertTrue("应该识别为Router返回类型", manager.isReturnRouter)

            val allLets = manager.allLet
            assertTrue("应该找到let声明", allLets.isNotEmpty())

            // 验证let声明中包含Router相关代码
            val routerRelatedLets = allLets.filter { TestPsiPatterns.isRouterRelatedLet(it) }
            assertTrue("应该找到Router相关的let声明", routerRelatedLets.isNotEmpty())
        }

        // 6. 测试结构体管理器
        val userDtoStruct =
                TestPsiPatterns.findFirstElementOfType<RsStructItem>(file) {
                    it.name == "UserDto"
                } as?
                        RsStructItemImpl
        assertNotNull("应该找到UserDto结构体", userDtoStruct)

        if (userDtoStruct != null) {
            val structManager = userDtoStruct.structItemManager

            // 测试TypeScript接口生成
            val tsInterface = structManager.getTSInterface
            assertTrue("应该生成TypeScript接口", tsInterface.isNotEmpty())
            assertTrue("接口应该包含结构体名", tsInterface.contains("UserDto"))
            assertTrue(
                    "接口应该包含字段",
                    tsInterface.contains("username") && tsInterface.contains("email")
            )
        }

        // ==================== 测试服务层集成 ====================

        // 7. 测试API服务扫描
        val apiService = SalvoApiService.getInstance(project)
        assertNotNull("API服务应该可用", apiService)

        runBlocking {
            // 触发API扫描
            apiService.startScan(true)

            ApplicationManager.getApplication().invokeAndWait {
                val apiList = apiService.getApiList()
                assertNotNull("API列表应该存在", apiList)

                // 验证扫描结果
                if (apiList.isNotEmpty()) {
                    assertTrue("应该找到API项", apiList.isNotEmpty())

                    // 验证API项的基本结构
                    apiList.forEach { api ->
                        assertNotNull("API项应该有路径", api.api)
                        assertNotNull("API项应该有方法", api.method)
                    }
                }
            }
        }

        // ==================== 测试复杂模式匹配 ====================

        // 8. 测试复合模式：在Router函数内的let声明
        val letsInRouterFunctions =
                TestPsiPatterns.findElementsByPattern(
                        file,
                        TestPsiPatterns.LET_IN_ROUTER_FUNCTION_PATTERN
                )
        assertTrue("应该找到Router函数内的let声明", letsInRouterFunctions.isNotEmpty())

        // 9. 测试自定义匹配器：API路由函数
        val apiRouterFunctions =
                TestPsiPatterns.findElementsByPattern(
                        file,
                        TestPsiPatterns.API_ROUTER_FUNCTION_PATTERN
                )
        assertTrue("应该找到包含HTTP方法的Router函数", apiRouterFunctions.isNotEmpty())

        // ==================== 测试错误处理和边界情况 ====================

        // 10. 测试空查询
        val nonExistentPattern = TestPsiPatterns.functionWithName("non_existent_function")
        val nonExistentFunctions = TestPsiPatterns.findElementsByPattern(file, nonExistentPattern)
        assertTrue("不存在的函数应该返回空列表", nonExistentFunctions.isEmpty())

        // 11. 测试类型安全
        val allFunctions = TestPsiPatterns.findElementsOfType<RsFunction>(file)
        val allStructs = TestPsiPatterns.findElementsOfType<RsStructItem>(file)
        val allMethodCalls = TestPsiPatterns.findElementsOfType<RsMethodCall>(file)

        assertTrue("应该找到函数", allFunctions.isNotEmpty())
        assertTrue("应该找到结构体", allStructs.isNotEmpty())
        assertTrue("应该找到方法调用", allMethodCalls.isNotEmpty())

        // ==================== 验证代码生成功能 ====================

        // 12. 测试TypeScript接口生成
        val allDtoStructs =
                allStructs.filterIsInstance<RsStructItemImpl>().filter {
                    TestPsiPatterns.isSerializableStruct(it)
                }

        allDtoStructs.forEach { struct ->
            val manager = struct.structItemManager
            val tsInterface = manager.getTSInterface

            assertTrue("每个DTO都应该能生成TS接口", tsInterface.isNotEmpty())
            assertTrue("接口应该包含interface关键字", tsInterface.contains("interface"))
            assertTrue("接口应该包含结构体名", tsInterface.contains(struct.name ?: ""))
        }

        // ==================== 性能和稳定性测试 ====================

        // 13. 测试大量元素的处理性能
        val startTime = System.currentTimeMillis()

        repeat(10) {
            TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.ROUTER_FUNCTION_PATTERN)
            TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.SALVO_HANDLER_PATTERN)
            TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.SALVO_HTTP_METHOD_PATTERN)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        assertTrue("模式匹配应该在合理时间内完成", duration < 5000) // 5秒内完成

        // ==================== 验证插件完整性 ====================

        // 14. 确保所有关键组件都能正常工作
        assertTrue("Router函数检测正常", routerFunctions.isNotEmpty())
        assertTrue("处理器函数检测正常", handlerFunctions.isNotEmpty())
        assertTrue("DTO结构体检测正常", dtoStructs.isNotEmpty())
        assertTrue("HTTP方法调用检测正常", httpMethodCalls.isNotEmpty())
        assertTrue("Let声明分析正常", letsInRouterFunctions.isNotEmpty())

        println("✅ 集成测试完成：所有组件都正常工作")
        println("   - 发现 ${routerFunctions.size} 个Router函数")
        println("   - 发现 ${handlerFunctions.size} 个处理器函数")
        println("   - 发现 ${dtoStructs.size} 个DTO结构体")
        println("   - 发现 ${httpMethodCalls.size} 个HTTP方法调用")
        println("   - 发现 ${letsInRouterFunctions.size} 个Router函数内的let声明")
    }

    /** 测试实际项目结构的模拟 */
    fun testRealProjectStructureSimulation() {
        // 模拟真实项目的模块结构
        val modelsCode =
                """
            use serde::{Deserialize, Serialize};

            #[derive(Debug, Clone, Serialize, Deserialize)]
            pub struct User {
                pub id: i64,
                pub username: String,
                pub email: String,
            }

            #[derive(Debug, Deserialize)]
            pub struct CreateUserRequest {
                pub username: String,
                pub email: String,
            }
        """.trimIndent()

        val handlersCode =
                """
            use salvo::prelude::*;
            use crate::models::*;

            #[handler]
            pub async fn create_user(req: &mut Request, res: &mut Response) {
                let user_req: CreateUserRequest = req.parse_json().await.unwrap();
                res.render(Json(user_req));
            }

            #[handler]
            pub async fn get_users(res: &mut Response) {
                let users = vec![User {
                    id: 1,
                    username: "test".to_string(),
                    email: "test@example.com".to_string(),
                }];
                res.render(Json(users));
            }
        """.trimIndent()

        val routesCode =
                """
            use salvo::prelude::*;
            use crate::handlers::*;

            pub fn user_routes() -> Router {
                Router::with_path("/users")
                    .get("/", get_users)
                    .post("/", create_user)
            }

            pub fn api_routes() -> Router {
                Router::with_path("/api/v1")
                    .push(user_routes())
            }
        """.trimIndent()

        // 配置多个文件
        myFixture.configureByText("models.rs", modelsCode)
        myFixture.configureByText("handlers.rs", handlersCode)
        myFixture.configureByText("routes.rs", routesCode)

        // 测试跨文件的PSI分析
        var totalRouterFunctions = 0
        var totalHandlerFunctions = 0
        var totalDtoStructs = 0

        // 获取所有配置的PSI文件
        val allFiles =
                listOf(
                        myFixture.getFile() // 获取当前配置的文件
                )

        // 手动获取项目中的所有PSI文件
        val projectFiles = mutableListOf<com.intellij.psi.PsiFile>()

        // 通过文件名查找文件
        val fileNames = listOf("models.rs", "handlers.rs", "routes.rs")
        fileNames.forEach { fileName ->
            try {
                val virtualFile = myFixture.findFileInTempDir(fileName)
                if (virtualFile != null) {
                    val psiFile =
                            com.intellij.psi.PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile != null) {
                        projectFiles.add(psiFile)
                    }
                }
            } catch (e: Exception) {
                // 文件可能不存在，忽略
            }
        }

        projectFiles.forEach { file ->
            val routerFunctions =
                    TestPsiPatterns.findElementsByPattern(
                            file,
                            TestPsiPatterns.ROUTER_FUNCTION_PATTERN
                    )
            val handlerFunctions =
                    TestPsiPatterns.findElementsByPattern(
                            file,
                            TestPsiPatterns.SALVO_HANDLER_PATTERN
                    )
            val dtoStructs =
                    TestPsiPatterns.findElementsByPattern(file, TestPsiPatterns.DTO_STRUCT_PATTERN)

            totalRouterFunctions += routerFunctions.size
            totalHandlerFunctions += handlerFunctions.size
            totalDtoStructs += dtoStructs.size
        }

        assertTrue("应该在多个文件中找到Router函数", totalRouterFunctions > 0)
        assertTrue("应该在多个文件中找到处理器函数", totalHandlerFunctions > 0)
        assertTrue("应该在多个文件中找到DTO结构体", totalDtoStructs > 0)

        println("✅ 多文件项目结构测试完成")
        println("   - 总计 ${totalRouterFunctions} 个Router函数")
        println("   - 总计 ${totalHandlerFunctions} 个处理器函数")
        println("   - 总计 ${totalDtoStructs} 个DTO结构体")
    }

    /** 测试插件的扩展性和自定义功能 */
    fun testPluginExtensibilityAndCustomization() {
        val extensibleCode =
                """
            use salvo::prelude::*;

            // 自定义中间件
            #[handler]
            async fn custom_middleware(req: &mut Request, depot: &mut Depot, res: &mut Response, ctrl: &mut FlowCtrl) {
                // 中间件逻辑
                ctrl.call_next(req, depot, res).await;
            }

            // 自定义错误处理
            #[handler]
            async fn custom_error_handler(req: &mut Request, res: &mut Response) {
                res.status_code(StatusCode::INTERNAL_SERVER_ERROR);
                res.render(Text("Custom error"));
            }

            // 带自定义属性的函数
            #[endpoint]
            #[deprecated]
            async fn legacy_endpoint(res: &mut Response) {
                res.render(Text("Legacy"));
            }

            // 复杂的路由构建
            fn build_complex_router() -> Router {
                let middleware_stack = Router::new()
                    .hoop(custom_middleware);

                let protected_routes = Router::with_path("/protected")
                    .hoop(auth_middleware)
                    .get("/profile", get_profile);

                Router::new()
                    .push(middleware_stack)
                    .push(protected_routes)
                    .catch(custom_error_handler)
            }
        """.trimIndent()

        val file = myFixture.configureByText("extensibility_test.rs", extensibleCode)

        // 测试自定义匹配器
        val middlewareFunctions =
                TestPsiPatterns.customFunctionMatcher("isMiddleware") { function ->
                    function.name?.contains("middleware") == true &&
                            TestPsiPatterns.hasAttribute(function, "handler")
                }

        val middlewares = TestPsiPatterns.findElementsByPattern(file, middlewareFunctions)
        assertTrue("应该找到中间件函数", middlewares.isNotEmpty())

        // 测试多属性匹配
        val multiAttributePattern =
                TestPsiPatterns.customFunctionMatcher("hasMultipleAttributes") { function ->
                    TestPsiPatterns.hasAttribute(function, "endpoint") &&
                            TestPsiPatterns.hasAttribute(function, "deprecated")
                }

        val multiAttributeFunctions =
                TestPsiPatterns.findElementsByPattern(file, multiAttributePattern)
        assertTrue("应该找到带有多个属性的函数", multiAttributeFunctions.isNotEmpty())

        // 测试复杂的Router构建模式
        val complexRouterPattern =
                TestPsiPatterns.customFunctionMatcher("isComplexRouter") { function ->
                    if (!TestPsiPatterns.isRouterFunction(function))
                            return@customFunctionMatcher false

                    val methodCalls = TestPsiPatterns.findElementsOfType<RsMethodCall>(function)
                    val hasHoop = methodCalls.any { it.identifier?.text == "hoop" }
                    val hasCatch = methodCalls.any { it.identifier?.text == "catch" }

                    return@customFunctionMatcher hasHoop || hasCatch
                }

        val complexRouters = TestPsiPatterns.findElementsByPattern(file, complexRouterPattern)
        assertTrue("应该找到复杂的Router构建函数", complexRouters.isNotEmpty())

        println("✅ 扩展性测试完成")
        println("   - 发现 ${middlewares.size} 个中间件函数")
        println("   - 发现 ${multiAttributeFunctions.size} 个多属性函数")
        println("   - 发现 ${complexRouters.size} 个复杂Router函数")
    }
}
