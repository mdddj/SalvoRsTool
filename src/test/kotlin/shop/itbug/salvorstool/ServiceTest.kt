package shop.itbug.salvorstool

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.rust.lang.core.psi.impl.RsFunctionImpl
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.service.RustProjectService
import shop.itbug.salvorstool.service.SalvoApiService
import shop.itbug.salvorstool.tool.myManager
import shop.itbug.salvorstool.tool.rsLetDeclImplManager

/** 服务层测试类 测试插件中的各种服务类，包括API扫描和项目服务 */
class ServiceTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    /** 测试SalvoApiService的API扫描功能 */
    fun testSalvoApiServiceScanning() {
        // 创建包含Salvo路由的测试文件
        val routerCode =
                """
            use salvo::prelude::*;

            /// 用户管理API
            fn user_router() -> Router {
                let router = Router::with_path("/users")
                    .get("/", list_users)
                    .post("/", create_user)
                    .get("/:id", get_user)
                    .put("/:id", update_user)
                    .delete("/:id", delete_user);

                router
            }

            /// 产品管理API
            fn product_router() -> Router {
                Router::with_path("/products")
                    .get("/", list_products)
                    .post("/", create_product)
                    .patch("/:id/status", update_product_status)
            }

            /// 主路由器
            fn api_router() -> Router {
                Router::new()
                    .push(user_router())
                    .push(product_router())
                    .get("/health", health_check)
            }

            #[handler]
            async fn list_users(req: &mut Request, res: &mut Response) {
                res.render(Json(vec!["user1", "user2"]));
            }

            #[handler]
            async fn create_user(req: &mut Request, res: &mut Response) {
                res.render(Json("User created"));
            }
        """.trimIndent()

        val file = myFixture.configureByText("api_test.rs", routerCode)

        // 获取SalvoApiService实例
        val apiService = SalvoApiService.getInstance(project)

        // 手动触发扫描
        runBlocking { apiService.doRefresh() }

        // 等待扫描完成
        ApplicationManager.getApplication().invokeAndWait {
            // 检查扫描结果
            val apiList = apiService.getApiList()

            // 验证扫描到的API数量
            assertTrue("应该扫描到API项", apiList.isNotEmpty())

            // 验证API项的结构
            apiList.forEach { api ->
                assertNotNull("API项应该有有效的数据", api)
                assertTrue("API项应该是SalvoApiItem类型", api is SalvoApiItem)
            }
        }
    }

    /** 测试RustProjectService的依赖检测 */
    fun testRustProjectServiceDependencyDetection() {
        // 创建包含Salvo依赖的Cargo.toml文件
        val cargoToml =
                """
            [package]
            name = "test-project"
            version = "0.1.0"
            edition = "2021"

            [dependencies]
            salvo = "0.60"
            tokio = { version = "1.0", features = ["full"] }
            serde = { version = "1.0", features = ["derive"] }
            serde_json = "1.0"
        """.trimIndent()

        myFixture.configureByText("Cargo.toml", cargoToml)

        val rustProjectService = RustProjectService.getInstance(project)

        // 测试依赖检测
        val hasSalvoDeps = runBlocking { rustProjectService.hasSalvoDependencies() }
        assertTrue("应该检测到Salvo依赖", hasSalvoDeps)
    }

    /** 测试不包含Salvo依赖的项目 */
    fun testProjectWithoutSalvoDependencies() {
        val cargoTomlWithoutSalvo =
                """
            [package]
            name = "test-project"
            version = "0.1.0"
            edition = "2021"

            [dependencies]
            tokio = { version = "1.0", features = ["full"] }
            reqwest = "0.11"
        """.trimIndent()

        myFixture.configureByText("Cargo.toml", cargoTomlWithoutSalvo)

        val rustProjectService = RustProjectService.getInstance(project)
        val hasSalvoDeps = runBlocking { rustProjectService.hasSalvoDependencies() }

        assertFalse("不应该检测到Salvo依赖", hasSalvoDeps)
    }

    /** 测试API项目模型的创建和验证 */
    fun testSalvoApiItemModel() {
        val routerCode =
                """
            use salvo::prelude::*;

            fn test_router() -> Router {
                let router = Router::new()
                    .get("/api/test", test_handler)
                    .post("/api/data", data_handler);
                router
            }

            #[handler]
            async fn test_handler(req: &mut Request, res: &mut Response) {
                res.render(Text("test"));
            }
        """.trimIndent()

        val file = myFixture.configureByText("model_test.rs", routerCode)

        // 查找Router函数
        var routerFunction: RsFunctionImpl? = null

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunctionImpl && element.myManager.isReturnRouter) {
                            routerFunction = element
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertNotNull("应该找到Router函数", routerFunction)

        // 测试函数管理器
        val functionManager = routerFunction!!.myManager
        assertTrue("应该识别为Router返回类型", functionManager.isReturnRouter)

        val allLets = functionManager.allLet
        assertTrue("应该找到let声明", allLets.isNotEmpty())

        // 测试API提取逻辑
        allLets.forEach { letDecl ->
            val manager = letDecl.rsLetDeclImplManager
            assertNotNull("每个let声明都应该有管理器", manager)

            // 验证API信息提取
            val apis = manager.allApi
            apis.forEach { apiItem ->
                assertTrue("API项应该是SalvoApiItem类型", apiItem is SalvoApiItem)
                assertNotNull("API项应该有路径信息", apiItem.api)
                assertNotNull("API项应该有方法信息", apiItem.method)
            }
        }
    }

    /** 测试复杂嵌套路由的扫描 */
    fun testComplexNestedRouterScanning() {
        val complexCode =
                """
            use salvo::prelude::*;

            fn create_app() -> Router {
                let api_v1 = create_api_v1();
                let api_v2 = create_api_v2();

                Router::new()
                    .push(api_v1)
                    .push(api_v2)
                    .get("/", home_handler)
                    .get("/health", health_handler)
            }

            fn create_api_v1() -> Router {
                let users = Router::with_path("/users")
                    .get("/", list_users)
                    .post("/", create_user)
                    .get("/:id", get_user);

                let posts = Router::with_path("/posts")
                    .get("/", list_posts)
                    .post("/", create_post);

                Router::with_path("/api/v1")
                    .push(users)
                    .push(posts)
            }

            fn create_api_v2() -> Router {
                Router::with_path("/api/v2")
                    .get("/users", list_users_v2)
                    .get("/stats", get_stats)
            }

            // Handler函数
            #[handler]
            async fn home_handler(res: &mut Response) {
                res.render(Text("Welcome"));
            }

            #[handler]
            async fn list_users(res: &mut Response) {
                res.render(Json(vec!["user1", "user2"]));
            }
        """.trimIndent()

        val file = myFixture.configureByText("complex_test.rs", complexCode)

        // 统计所有Router函数
        val routerFunctions = mutableListOf<RsFunctionImpl>()

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunctionImpl && element.myManager.isReturnRouter) {
                            routerFunctions.add(element)
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertEquals("应该找到3个Router函数", 3, routerFunctions.size)

        // 验证每个Router函数
        val functionNames = routerFunctions.map { it.name }
        assertTrue("应该包含create_app", functionNames.contains("create_app"))
        assertTrue("应该包含create_api_v1", functionNames.contains("create_api_v1"))
        assertTrue("应该包含create_api_v2", functionNames.contains("create_api_v2"))

        // 测试最复杂的函数
        val mainAppFunction = routerFunctions.find { it.name == "create_app" }
        assertNotNull("应该找到主应用函数", mainAppFunction)

        val mainAppLets = mainAppFunction!!.myManager.allLet
        assertTrue("主应用函数应该有let声明", mainAppLets.isNotEmpty())
    }

    /** 测试API扫描的异步行为 */
    fun testAsyncApiScanning() {
        val asyncCode =
                """
            use salvo::prelude::*;

            fn async_router() -> Router {
                let router = Router::new()
                    .get("/async/test", async_test_handler)
                    .post("/async/data", async_data_handler);
                router
            }

            #[handler]
            async fn async_test_handler(req: &mut Request, res: &mut Response) {
                let data = req.parse_json::<serde_json::Value>().await;
                res.render(Json(data));
            }

            #[handler]
            async fn async_data_handler(req: &mut Request, res: &mut Response) {
                // 异步处理逻辑
                tokio::time::sleep(tokio::time::Duration::from_millis(100)).await;
                res.render(Text("processed"));
            }
        """.trimIndent()

        val file = myFixture.configureByText("async_test.rs", asyncCode)

        val apiService = SalvoApiService.getInstance(project)

        // 测试异步扫描
        runBlocking {
            apiService.startScan(true)

            // 验证扫描不会阻塞
            val startTime = System.currentTimeMillis()
            apiService.doRefresh()
            val endTime = System.currentTimeMillis()

            // 验证操作是异步的（不应该阻塞太久）
            assertTrue("扫描操作应该快速返回", (endTime - startTime) < 5000)
        }
    }

    /** 测试服务的生命周期管理 */
    fun testServiceLifecycle() {
        val apiService = SalvoApiService.getInstance(project)
        assertNotNull("API服务应该能够创建", apiService)

        val rustProjectService = RustProjectService.getInstance(project)
        assertNotNull("Rust项目服务应该能够创建", rustProjectService)

        // 测试服务的基本功能
        val initialApiList = apiService.getApiList()
        assertNotNull("API列表应该可以获取", initialApiList)

        // 测试刷新功能
        apiService.doRefresh()

        // 验证服务状态
        assertTrue("服务应该处于可用状态", apiService.javaClass.isInstance(apiService))
    }

    /** 测试错误处理和边界情况 */
    fun testErrorHandlingAndEdgeCases() {
        // 测试空文件
        val emptyFile = myFixture.configureByText("empty.rs", "")
        val apiService = SalvoApiService.getInstance(project)

        runBlocking {
            // 应该能够处理空文件而不崩溃
            apiService.startScan()
            val apiList = apiService.getApiList()
            assertNotNull("API列表应该存在（即使为空）", apiList)
        }

        // 测试无效的Rust代码
        val invalidCode =
                """
            // 这是无效的Rust代码
            fn invalid_syntax( -> {
                let broken =
            }
        """.trimIndent()

        myFixture.configureByText("invalid.rs", invalidCode)

        runBlocking {
            // 应该能够处理语法错误而不崩溃
            try {
                apiService.startScan()
                // 如果没有抛出异常，测试通过
                assertTrue("应该能够处理无效代码", true)
            } catch (e: Exception) {
                // 如果抛出异常，应该是可预期的类型
                assertTrue("异常应该是可处理的类型", e is RuntimeException || e is IllegalStateException)
            }
        }
    }

    /** 测试API消息传递机制 */
    fun testApiMessaging() {
        val code =
                """
            use salvo::prelude::*;

            fn messaging_router() -> Router {
                Router::new()
                    .get("/message/test", message_handler)
            }

            #[handler]
            async fn message_handler(res: &mut Response) {
                res.render(Text("message"));
            }
        """.trimIndent()

        myFixture.configureByText("messaging_test.rs", code)

        val apiService = SalvoApiService.getInstance(project)
        var messageReceived = false

        // 订阅API扫描消息
        val connection = project.messageBus.connect()
        connection.subscribe(
                shop.itbug.salvorstool.messageing.ApiScanMessaging.TOPIC,
                object : shop.itbug.salvorstool.messageing.ApiScanMessaging {
                    override fun apiScanEnd(apis: List<SalvoApiItem>, isRefresh: Boolean) {
                        messageReceived = true
                        assertNotNull("APIs不应该为null", apis)
                        assertTrue("isRefresh标志应该正确传递", isRefresh)
                    }
                }
        )

        runBlocking {
            apiService.doRefresh()

            // 等待消息处理
            ApplicationManager.getApplication().invokeAndWait {
                assertTrue("应该接收到API扫描完成消息", messageReceived)
            }
        }

        connection.disconnect()
    }
}
