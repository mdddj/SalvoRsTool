package shop.itbug.salvorstool

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.impl.RsFunctionImpl
import shop.itbug.salvorstool.tool.myManager

/** PSI元素模式匹配测试类 测试各种Rust PSI元素的匹配模式，特别是Salvo框架相关的元素 */
class PsiElementPatternTest : BasePlatformTestCase() {

    override fun getTestDataPath(): String = "src/test/testData"

    /** 测试Rust函数模式匹配 */
    fun testRustFunctionPattern() {
        // 创建匹配Rust函数的模式
        val functionPattern: PsiElementPattern<RsFunction, *> =
                PlatformPatterns.psiElement(RsFunction::class.java).withName("router")

        val code =
                """
            fn router() -> Router {
                Router::new()
            }

            fn hello() -> String {
                "Hello".to_string()
            }
        """.trimIndent()

        val file = myFixture.configureByText("test.rs", code)
        val functions = findElementsByPattern(file, functionPattern)

        assertEquals("应该匹配到名为'router'的函数", 1, functions.size)
        assertEquals("函数名应该是'router'", "router", functions.first().name)
    }

    /** 测试Router返回类型的函数模式匹配 */
    fun testRouterReturnTypePattern() {
        val routerFunctionPattern: PsiElementPattern<RsFunction, *> =
                PlatformPatterns.psiElement(RsFunction::class.java)
                        .with(
                                object :
                                        com.intellij.patterns.PatternCondition<RsFunction>(
                                                "hasRouterReturnType"
                                        ) {
                                    override fun accepts(
                                            function: RsFunction,
                                            context: com.intellij.util.ProcessingContext?
                                    ): Boolean {
                                        return function.retType?.typeReference?.text == "Router"
                                    }
                                }
                        )

        val code =
                """
            use salvo::prelude::*;

            fn create_router() -> Router {
                Router::new()
            }

            fn get_user() -> Result<User, Error> {
                Ok(User::default())
            }

            fn api_router() -> Router {
                Router::with_path("/api")
            }
        """.trimIndent()

        val file = myFixture.configureByText("router_test.rs", code)
        val routerFunctions = findElementsByPattern(file, routerFunctionPattern)

        assertEquals("应该找到2个返回Router的函数", 2, routerFunctions.size)
        assertTrue("应该包含create_router函数", routerFunctions.any { it.name == "create_router" })
        assertTrue("应该包含api_router函数", routerFunctions.any { it.name == "api_router" })
    }

    /** 测试Struct模式匹配 */
    fun testStructPattern() {
        val structPattern: PsiElementPattern<RsStructItem, *> =
                PlatformPatterns.psiElement(RsStructItem::class.java).withName("User")

        val code =
                """
            #[derive(Debug, Serialize, Deserialize)]
            struct User {
                id: i64,
                name: String,
                email: String,
            }

            struct Product {
                id: i64,
                title: String,
            }
        """.trimIndent()

        val file = myFixture.configureByText("structs.rs", code)
        val structs = findElementsByPattern(file, structPattern)

        assertEquals("应该找到名为User的结构体", 1, structs.size)
        assertEquals("结构体名应该是User", "User", structs.first().name)
    }

    /** 测试带有特定属性的Struct模式匹配 */
    fun testStructWithAttributesPattern() {
        val serializableStructPattern: PsiElementPattern<RsStructItem, *> =
                PlatformPatterns.psiElement(RsStructItem::class.java)
                        .with(
                                object :
                                        com.intellij.patterns.PatternCondition<RsStructItem>(
                                                "hasSerializeAttribute"
                                        ) {
                                    override fun accepts(
                                            struct: RsStructItem,
                                            context: com.intellij.util.ProcessingContext?
                                    ): Boolean {
                                        return struct.outerAttrList.any { attr ->
                                            attr.text.contains("Serialize")
                                        }
                                    }
                                }
                        )

        val code =
                """
            #[derive(Debug, Serialize, Deserialize)]
            struct ApiResponse {
                success: bool,
                data: String,
            }

            #[derive(Debug)]
            struct InternalData {
                value: i32,
            }

            #[derive(Serialize, Deserialize)]
            struct UserDto {
                name: String,
                age: u32,
            }
        """.trimIndent()

        val file = myFixture.configureByText("attributed_structs.rs", code)
        val serializableStructs = findElementsByPattern(file, serializableStructPattern)

        assertEquals("应该找到2个带有Serialize属性的结构体", 2, serializableStructs.size)
        assertTrue("应该包含ApiResponse", serializableStructs.any { it.name == "ApiResponse" })
        assertTrue("应该包含UserDto", serializableStructs.any { it.name == "UserDto" })
    }

    /** 测试Let声明模式匹配 */
    fun testLetDeclarationPattern() {
        val routerLetPattern: PsiElementPattern<RsLetDecl, *> =
                PlatformPatterns.psiElement(RsLetDecl::class.java)
                        .with(
                                object :
                                        com.intellij.patterns.PatternCondition<RsLetDecl>(
                                                "isRouterBinding"
                                        ) {
                                    override fun accepts(
                                            letDecl: RsLetDecl,
                                            context: com.intellij.util.ProcessingContext?
                                    ): Boolean {
                                        return letDecl.expr?.text?.contains("Router::") == true
                                    }
                                }
                        )

        val code =
                """
            fn setup_routes() -> Router {
                let router = Router::new();
                let api_router = Router::with_path("/api");
                let user_name = "test";
                let count = 42;

                router
            }
        """.trimIndent()

        val file = myFixture.configureByText("let_decl_test.rs", code)
        val routerLets = findElementsByPattern(file, routerLetPattern)

        assertEquals("应该找到2个Router相关的let声明", 2, routerLets.size)
    }

    /** 测试方法调用模式匹配 - 特别是Salvo的路由方法 */
    fun testMethodCallPattern() {
        val salvoMethodPattern: PsiElementPattern<RsMethodCall, *> =
                PlatformPatterns.psiElement(RsMethodCall::class.java)
                        .with(
                                object :
                                        com.intellij.patterns.PatternCondition<RsMethodCall>(
                                                "isSalvoRouteMethod"
                                        ) {
                                    override fun accepts(
                                            methodCall: RsMethodCall,
                                            context: com.intellij.util.ProcessingContext?
                                    ): Boolean {
                                        val methodName = methodCall.identifier?.text
                                        return methodName in
                                                listOf(
                                                        "get",
                                                        "post",
                                                        "put",
                                                        "delete",
                                                        "patch",
                                                        "push"
                                                )
                                    }
                                }
                        )

        val code =
                """
            fn create_api_router() -> Router {
                Router::new()
                    .get("/users", get_users)
                    .post("/users", create_user)
                    .put("/users/:id", update_user)
                    .delete("/users/:id", delete_user)
                    .push(Router::with_path("/admin").get("/stats", get_stats))
            }
        """.trimIndent()

        val file = myFixture.configureByText("method_calls.rs", code)
        val salvoMethods = findElementsByPattern(file, salvoMethodPattern)

        assertTrue("应该找到多个Salvo路由方法调用", salvoMethods.size >= 4)
    }

    /** 测试复合模式：在Router返回类型的函数内部的let声明 */
    fun testCompositePattern() {
        val letInRouterFunctionPattern: PsiElementPattern<RsLetDecl, *> =
                PlatformPatterns.psiElement(RsLetDecl::class.java)
                        .inside(
                                PlatformPatterns.psiElement(RsFunction::class.java)
                                        .with(
                                                object :
                                                        com.intellij.patterns.PatternCondition<
                                                                RsFunction>("returnsRouter") {
                                                    override fun accepts(
                                                            function: RsFunction,
                                                            context:
                                                                    com.intellij.util.ProcessingContext?
                                                    ): Boolean {
                                                        return function.retType
                                                                ?.typeReference
                                                                ?.text == "Router"
                                                    }
                                                }
                                        )
                        )

        val code =
                """
            fn create_user_router() -> Router {
                let base_path = "/users";
                let router = Router::with_path(base_path);
                router.get("/", list_users)
            }

            fn helper_function() -> String {
                let helper_var = "not in router function";
                helper_var.to_string()
            }
        """.trimIndent()

        val file = myFixture.configureByText("composite_test.rs", code)
        val letsInRouterFunction = findElementsByPattern(file, letInRouterFunctionPattern)

        assertEquals("应该找到Router函数内的2个let声明", 2, letsInRouterFunction.size)
    }

    /** 测试自定义的PSI元素匹配验证函数 */
    fun testCustomValidationFunction() {
        val code =
                """
            fn api_router() -> Router {
                let router = Router::new();
                let user_router = Router::with_path("/users");
                router.push(user_router)
            }
        """.trimIndent()

        val file = myFixture.configureByText("validation_test.rs", code)

        // 测试自定义验证函数
        val routerFunction = findRustFunction(file, "api_router")
        assertNotNull("应该找到api_router函数", routerFunction)
        assertTrue("应该验证为Router返回类型", validateRouterFunction(routerFunction!!))

        val routerLets = findRouterRelatedLets(routerFunction)
        assertEquals("应该找到2个Router相关的let声明", 2, routerLets.size)
    }

    /** 测试Salvo属性宏模式匹配 */
    fun testSalvoAttributePattern() {
        val handlerAttributePattern: PsiElementPattern<RsFunction, *> =
                PlatformPatterns.psiElement(RsFunction::class.java)
                        .with(
                                object :
                                        com.intellij.patterns.PatternCondition<RsFunction>(
                                                "hasHandlerAttribute"
                                        ) {
                                    override fun accepts(
                                            function: RsFunction,
                                            context: com.intellij.util.ProcessingContext?
                                    ): Boolean {
                                        return function.outerAttrList.any { attr ->
                                            attr.text.contains("handler") ||
                                                    attr.text.contains("endpoint")
                                        }
                                    }
                                }
                        )

        val code =
                """
            #[handler]
            async fn get_users(req: &mut Request, res: &mut Response) {
                res.render(Json(vec!["user1", "user2"]));
            }

            #[endpoint]
            async fn create_user(req: &mut Request, res: &mut Response) {
                // implementation
            }

            async fn regular_function() {
                // no attribute
            }
        """.trimIndent()

        val file = myFixture.configureByText("handler_test.rs", code)
        val handlerFunctions = findElementsByPattern(file, handlerAttributePattern)

        assertEquals("应该找到2个带有handler/endpoint属性的函数", 2, handlerFunctions.size)
    }

    // 辅助方法：根据模式查找元素
    private fun <T : PsiElement> findElementsByPattern(
            file: PsiElement,
            pattern: ElementPattern<T>
    ): List<T> {
        val elements = mutableListOf<T>()
        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        @Suppress("UNCHECKED_CAST")
                        if (pattern.accepts(element)) {
                            elements.add(element as T)
                        }
                        super.visitElement(element)
                    }
                }
        )
        return elements
    }

    // 辅助方法：查找Rust函数
    private fun findRustFunction(file: PsiElement, name: String): RsFunction? {
        var result: RsFunction? = null
        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is RsFunction && element.name == name) {
                            result = element
                            return
                        }
                        super.visitElement(element)
                    }
                }
        )
        return result
    }

    // 自定义验证函数：验证是否为Router返回类型的函数
    private fun validateRouterFunction(function: RsFunction): Boolean {
        return function.retType?.typeReference?.text == "Router"
    }

    // 自定义验证函数：查找Router相关的let声明
    private fun findRouterRelatedLets(function: RsFunction): List<RsLetDecl> {
        if (function !is RsFunctionImpl) return emptyList()

        return function.myManager.allLet.filter { letDecl ->
            letDecl.expr?.text?.contains("Router::") == true
        }
    }

    // 验证PSI元素是否匹配特定条件
    private fun validatePsiElement(
            element: PsiElement,
            condition: (PsiElement) -> Boolean
    ): Boolean {
        return condition(element)
    }

    /** 测试PSI树遍历和元素查找 */
    fun testPsiTreeTraversal() {
        val code =
                """
            struct User {
                id: i64,
                name: String,
            }

            fn get_user_router() -> Router {
                let router = Router::new();
                router.get("/user/:id", get_user_handler)
            }

            #[handler]
            async fn get_user_handler(req: &mut Request, res: &mut Response) {
                // handler implementation
            }
        """.trimIndent()

        val file = myFixture.configureByText("traversal_test.rs", code)

        // 统计不同类型的PSI元素
        var structCount = 0
        var functionCount = 0
        var letDeclCount = 0
        var methodCallCount = 0

        file.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        when (element) {
                            is RsStructItem -> structCount++
                            is RsFunction -> functionCount++
                            is RsLetDecl -> letDeclCount++
                            is RsMethodCall -> methodCallCount++
                        }
                        super.visitElement(element)
                    }
                }
        )

        assertEquals("应该找到1个结构体", 1, structCount)
        assertEquals("应该找到2个函数", 2, functionCount)
        assertEquals("应该找到1个let声明", 1, letDeclCount)
        assertTrue("应该找到至少1个方法调用", methodCallCount >= 1)
    }
}
