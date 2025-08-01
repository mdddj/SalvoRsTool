package shop.itbug.salvorstool

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsStructItem
import org.rust.lang.core.psi.impl.RsFunctionImpl
import org.rust.lang.core.psi.impl.RsStructItemImpl
import shop.itbug.salvorstool.tool.*

/** 工具管理器测试类 测试插件中的各种PSI管理器和工具类的功能 */
class ToolManagerTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    /** 测试RsFunctionManager - 检测Router返回类型 */
    fun testRsFunctionManagerRouterDetection() {
        val code =
                """
            use salvo::prelude::*;

            fn create_api_router() -> Router {
                let router = Router::new();
                let users_router = Router::with_path("/users");
                router.push(users_router)
            }

            fn get_user_name() -> String {
                "user".to_string()
            }

            fn another_router() -> Router {
                Router::with_path("/api")
            }
        """.trimIndent()

        val file = myFixture.configureByText("function_manager_test.rs", code)
        val functions = mutableListOf<RsFunction>()

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunction) {
                            functions.add(element)
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertEquals("应该找到3个函数", 3, functions.size)

        // 测试Router返回类型检测
        val routerFunctions =
                functions.filterIsInstance<RsFunctionImpl>().filter { it.myManager.isReturnRouter }

        assertEquals("应该找到2个返回Router的函数", 2, routerFunctions.size)
        assertTrue("应该包含create_api_router", routerFunctions.any { it.name == "create_api_router" })
        assertTrue("应该包含another_router", routerFunctions.any { it.name == "another_router" })
    }

    /** 测试RsFunctionManager - Let声明管理 */
    fun testRsFunctionManagerLetDeclarations() {
        val code =
                """
            fn setup_routes() -> Router {
                let base_router = Router::new();
                let api_router = Router::with_path("/api");
                let user_name = "admin";
                let port = 8080;

                base_router.push(api_router)
            }
        """.trimIndent()

        val file = myFixture.configureByText("let_decl_test.rs", code)
        var setupFunction: RsFunctionImpl? = null

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunctionImpl && element.name == "setup_routes") {
                            setupFunction = element
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertNotNull("应该找到setup_routes函数", setupFunction)

        val allLets = setupFunction!!.myManager.allLet
        assertEquals("应该找到4个let声明", 4, allLets.size)

        // 验证let声明的变量名
        val letNames = allLets.mapNotNull { it.pat?.text }
        assertTrue("应该包含base_router", letNames.contains("base_router"))
        assertTrue("应该包含api_router", letNames.contains("api_router"))
        assertTrue("应该包含user_name", letNames.contains("user_name"))
        assertTrue("应该包含port", letNames.contains("port"))
    }

    /** 测试MyRsStructManager */
    fun testMyRsStructManager() {
        val code =
                """
            #[derive(Debug, Serialize, Deserialize)]
            struct User {
                id: i64,
                name: String,
                email: Option<String>,
            }

            #[derive(Clone)]
            struct Product {
                id: u64,
                title: String,
                price: f64,
            }
        """.trimIndent()

        val file = myFixture.configureByText("struct_manager_test.rs", code)
        val structs = mutableListOf<RsStructItem>()

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsStructItem) {
                            structs.add(element)
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertEquals("应该找到2个结构体", 2, structs.size)

        // 测试User结构体
        val userStruct = structs.find { it.name == "User" }
        assertNotNull("应该找到User结构体", userStruct)

        if (userStruct is RsStructItemImpl) {
            val userManager = userStruct.structItemManager

            // 测试TypeScript接口生成
            val tsInterface = userManager.getTSInterface
            assertTrue("TypeScript接口应该包含interface User", tsInterface.contains("interface User"))
            assertTrue("应该包含id字段", tsInterface.contains("id"))
            assertTrue("应该包含name字段", tsInterface.contains("name"))
            assertTrue("应该包含email字段", tsInterface.contains("email"))
        }
    }

    /** 测试RsLetDeclManager */
    fun testRsLetDeclManager() {
        val code =
                """
            fn create_router() -> Router {
                let router = Router::new()
                    .get("/users", get_users)
                    .post("/users", create_user);

                let admin_router = Router::with_path("/admin")
                    .get("/stats", get_stats);

                router.push(admin_router)
            }
        """.trimIndent()

        val file = myFixture.configureByText("let_manager_test.rs", code)
        var routerFunction: RsFunctionImpl? = null

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunctionImpl && element.name == "create_router") {
                            routerFunction = element
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertNotNull("应该找到create_router函数", routerFunction)

        val allLets = routerFunction!!.myManager.allLet
        assertEquals("应该找到2个let声明", 2, allLets.size)

        // 测试每个let声明的管理器
        allLets.forEach { letDecl ->
            val manager = letDecl.rsLetDeclImplManager
            assertNotNull("每个let声明都应该有管理器", manager)

            // 测试API提取（假设rsLetDeclImplManager有allApi属性）
            // val apis = manager.allApi
            // 这里可以根据实际的manager实现来添加更多测试
        }
    }

    /** 测试RsPsiElementTools */
    fun testRsPsiElementTools() {
        val code =
                """
            /// 获取所有用户
            /// 返回用户列表
            #[handler]
            async fn get_users(req: &mut Request, res: &mut Response) {
                res.render(Json(vec!["user1", "user2"]));
            }

            /// 创建新用户
            fn create_user_handler() -> String {
                "handler".to_string()
            }
        """.trimIndent()

        val file = myFixture.configureByText("psi_tools_test.rs", code)
        val functions = mutableListOf<RsFunction>()

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: com.intellij.psi.PsiElement) {
                        if (element is RsFunction) {
                            functions.add(element)
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertEquals("应该找到2个函数", 2, functions.size)

        // 测试文档注释提取
        functions.filterIsInstance<RsFunctionImpl>().forEach { func ->
            val docText = RsPsiElementTools.findDocumentWithRsFunction(func)

            when (func.name) {
                "get_users" -> {
                    assertNotNull("get_users应该有文档注释", docText)
                    assertTrue("文档应该包含'获取所有用户'", docText?.contains("获取所有用户") == true)
                }
                "create_user_handler" -> {
                    assertNotNull("create_user_handler应该有文档注释", docText)
                    assertTrue("文档应该包含'创建新用户'", docText?.contains("创建新用户") == true)
                }
            }
        }
    }

    /** 测试API扫描服务相关功能 */
    fun testSalvoApiServiceScanning() {
        val code =
                """
            use salvo::prelude::*;

            /// API路由器
            fn api_router() -> Router {
                let users = Router::with_path("/users")
                    .get("/", list_users)
                    .post("/", create_user)
                    .get("/:id", get_user)
                    .put("/:id", update_user)
                    .delete("/:id", delete_user);

                let admin = Router::with_path("/admin")
                    .get("/stats", admin_stats);

                Router::new()
                    .push(users)
                    .push(admin)
            }

            #[handler]
            async fn list_users(req: &mut Request, res: &mut Response) {}

            #[handler]
            async fn create_user(req: &mut Request, res: &mut Response) {}
        """.trimIndent()

        val file = myFixture.configureByText("api_service_test.rs", code)

        // 查找所有返回Router的函数
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

        assertEquals("应该找到1个Router函数", 1, routerFunctions.size)

        val apiRouter = routerFunctions.first()
        assertEquals("函数名应该是api_router", "api_router", apiRouter.name)

        // 测试let声明提取
        val allLets = apiRouter.myManager.allLet
        assertTrue("应该找到至少2个let声明", allLets.size >= 2)

        // 验证包含users和admin相关的let声明
        val letTexts = allLets.map { it.text }
        assertTrue("应该包含users相关的let声明", letTexts.any { it.contains("users") })
        assertTrue("应该包含admin相关的let声明", letTexts.any { it.contains("admin") })
    }

    /** 测试复杂的路由结构解析 */
    fun testComplexRouterStructure() {
        val code =
                """
            fn create_app_router() -> Router {
                let api_v1 = Router::with_path("/api/v1")
                    .push(create_user_router())
                    .push(create_product_router());

                let api_v2 = Router::with_path("/api/v2")
                    .get("/health", health_check);

                let static_files = Router::with_path("/static")
                    .get("/<**>", serve_static_files);

                Router::new()
                    .push(api_v1)
                    .push(api_v2)
                    .push(static_files)
                    .get("/", home_handler)
            }

            fn create_user_router() -> Router {
                Router::with_path("/users")
                    .get("/", list_users)
                    .post("/", create_user)
            }

            fn create_product_router() -> Router {
                Router::with_path("/products")
                    .get("/", list_products)
            }
        """.trimIndent()

        val file = myFixture.configureByText("complex_router_test.rs", code)

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

        // 验证函数名
        val functionNames = routerFunctions.map { it.name }
        assertTrue("应该包含create_app_router", functionNames.contains("create_app_router"))
        assertTrue("应该包含create_user_router", functionNames.contains("create_user_router"))
        assertTrue("应该包含create_product_router", functionNames.contains("create_product_router"))

        // 测试主函数的复杂性
        val mainRouter = routerFunctions.find { it.name == "create_app_router" }
        assertNotNull("应该找到主路由函数", mainRouter)

        val mainRouterLets = mainRouter!!.myManager.allLet
        assertEquals("主路由应该有3个let声明", 3, mainRouterLets.size)
    }
}
