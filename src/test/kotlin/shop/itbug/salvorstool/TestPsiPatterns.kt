package shop.itbug.salvorstool

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.ext.RsOuterAttributeOwner
import org.rust.lang.core.psi.ext.isAsync

/** PSI元素模式测试工具类 提供常用的PSI元素匹配模式和验证方法，用于测试IDEA插件的PSI元素处理功能 */
object TestPsiPatterns {

    // ==================== 函数相关模式 ====================

    /** 匹配返回Router类型的函数 */
    val ROUTER_FUNCTION_PATTERN: PsiElementPattern<RsFunction, *> =
            PlatformPatterns.psiElement(RsFunction::class.java)
                    .with(
                            object : PatternCondition<RsFunction>("returnsRouter") {
                                override fun accepts(
                                        function: RsFunction,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return function.retType?.typeReference?.text == "Router"
                                }
                            }
                    )

    /** 匹配指定名称的函数 */
    fun functionWithName(name: String): PsiElementPattern<RsFunction, *> =
            PlatformPatterns.psiElement(RsFunction::class.java).withName(name)

    /** 匹配异步函数 */
    val ASYNC_FUNCTION_PATTERN: PsiElementPattern<RsFunction, *> =
            PlatformPatterns.psiElement(RsFunction::class.java)
                    .with(
                            object : PatternCondition<RsFunction>("isAsync") {
                                override fun accepts(
                                        function: RsFunction,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return function.isAsync
                                }
                            }
                    )

    /** 匹配带有特定属性的函数 */
    fun functionWithAttribute(attributeName: String): PsiElementPattern<RsFunction, *> =
            PlatformPatterns.psiElement(RsFunction::class.java)
                    .with(
                            object : PatternCondition<RsFunction>("hasAttribute") {
                                override fun accepts(
                                        function: RsFunction,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return function.outerAttrList.any { attr ->
                                        attr.text.contains(attributeName)
                                    }
                                }
                            }
                    )

    // ==================== 结构体相关模式 ====================

    /** 匹配带有Serialize属性的结构体 */
    val SERIALIZABLE_STRUCT_PATTERN: PsiElementPattern<RsStructItem, *> =
            PlatformPatterns.psiElement(RsStructItem::class.java)
                    .with(
                            object : PatternCondition<RsStructItem>("isSerializable") {
                                override fun accepts(
                                        struct: RsStructItem,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return struct.outerAttrList.any { attr ->
                                        attr.text.contains("Serialize")
                                    }
                                }
                            }
                    )

    /** 匹配带有Deserialize属性的结构体 */
    val DESERIALIZABLE_STRUCT_PATTERN: PsiElementPattern<RsStructItem, *> =
            PlatformPatterns.psiElement(RsStructItem::class.java)
                    .with(
                            object : PatternCondition<RsStructItem>("isDeserializable") {
                                override fun accepts(
                                        struct: RsStructItem,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return struct.outerAttrList.any { attr ->
                                        attr.text.contains("Deserialize")
                                    }
                                }
                            }
                    )

    /** 匹配指定名称的结构体 */
    fun structWithName(name: String): PsiElementPattern<RsStructItem, *> =
            PlatformPatterns.psiElement(RsStructItem::class.java).withName(name)

    /** 匹配带有derive属性的结构体 */
    val DERIVED_STRUCT_PATTERN: PsiElementPattern<RsStructItem, *> =
            PlatformPatterns.psiElement(RsStructItem::class.java)
                    .with(
                            object : PatternCondition<RsStructItem>("hasDeriveAttribute") {
                                override fun accepts(
                                        struct: RsStructItem,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return struct.outerAttrList.any { attr ->
                                        attr.text.contains("derive")
                                    }
                                }
                            }
                    )

    // ==================== Let声明相关模式 ====================

    /** 匹配Router相关的let声明 */
    val ROUTER_LET_PATTERN: PsiElementPattern<RsLetDecl, *> =
            PlatformPatterns.psiElement(RsLetDecl::class.java)
                    .with(
                            object : PatternCondition<RsLetDecl>("isRouterBinding") {
                                override fun accepts(
                                        letDecl: RsLetDecl,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return letDecl.expr?.text?.contains("Router::") == true
                                }
                            }
                    )

    /** 匹配指定变量名的let声明 */
    fun letWithVariableName(varName: String): PsiElementPattern<RsLetDecl, *> =
            PlatformPatterns.psiElement(RsLetDecl::class.java)
                    .with(
                            object : PatternCondition<RsLetDecl>("hasVariableName") {
                                override fun accepts(
                                        letDecl: RsLetDecl,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return letDecl.pat?.text == varName
                                }
                            }
                    )

    // ==================== 方法调用相关模式 ====================

    /** 匹配Salvo HTTP方法调用（get, post, put, delete等） */
    val SALVO_HTTP_METHOD_PATTERN: PsiElementPattern<RsMethodCall, *> =
            PlatformPatterns.psiElement(RsMethodCall::class.java)
                    .with(
                            object : PatternCondition<RsMethodCall>("isSalvoHttpMethod") {
                                override fun accepts(
                                        methodCall: RsMethodCall,
                                        context: ProcessingContext?
                                ): Boolean {
                                    val methodName = methodCall.identifier?.text
                                    return methodName in
                                            listOf(
                                                    "get",
                                                    "post",
                                                    "put",
                                                    "delete",
                                                    "patch",
                                                    "head",
                                                    "options"
                                            )
                                }
                            }
                    )

    /** 匹配Router构建方法调用（push, with_path等） */
    val ROUTER_BUILDER_METHOD_PATTERN: PsiElementPattern<RsMethodCall, *> =
            PlatformPatterns.psiElement(RsMethodCall::class.java)
                    .with(
                            object : PatternCondition<RsMethodCall>("isRouterBuilderMethod") {
                                override fun accepts(
                                        methodCall: RsMethodCall,
                                        context: ProcessingContext?
                                ): Boolean {
                                    val methodName = methodCall.identifier?.text
                                    return methodName in
                                            listOf("push", "with_path", "with_hoop", "nest")
                                }
                            }
                    )

    /** 匹配指定方法名的方法调用 */
    fun methodCallWithName(methodName: String): PsiElementPattern<RsMethodCall, *> =
            PlatformPatterns.psiElement(RsMethodCall::class.java)
                    .with(
                            object : PatternCondition<RsMethodCall>("hasMethodName") {
                                override fun accepts(
                                        methodCall: RsMethodCall,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return methodCall.identifier?.text == methodName
                                }
                            }
                    )

    // ==================== 复合模式 ====================

    /** 匹配在Router函数内部的let声明 */
    val LET_IN_ROUTER_FUNCTION_PATTERN: PsiElementPattern<RsLetDecl, *> =
            PlatformPatterns.psiElement(RsLetDecl::class.java).inside(ROUTER_FUNCTION_PATTERN)

    /** 匹配在异步函数内部的let声明 */
    val LET_IN_ASYNC_FUNCTION_PATTERN: PsiElementPattern<RsLetDecl, *> =
            PlatformPatterns.psiElement(RsLetDecl::class.java).inside(ASYNC_FUNCTION_PATTERN)

    /** 匹配带有handler属性的异步函数 */
    val ASYNC_HANDLER_FUNCTION_PATTERN: PsiElementPattern<RsFunction, *> =
            ASYNC_FUNCTION_PATTERN.and(functionWithAttribute("handler"))

    // ==================== 验证工具方法 ====================

    /** 验证函数是否返回Router类型 */
    fun isRouterFunction(function: RsFunction): Boolean {
        return function.retType?.typeReference?.text == "Router"
    }

    /** 验证函数是否为异步函数 */
    fun isAsyncFunction(function: RsFunction): Boolean {
        return function.isAsync
    }

    /** 验证函数是否带有指定属性 */
    fun hasAttribute(element: RsOuterAttributeOwner, attributeName: String): Boolean {
        return element.outerAttrList.any { attr -> attr.text.contains(attributeName) }
    }

    /** 验证结构体是否可序列化 */
    fun isSerializableStruct(struct: RsStructItem): Boolean {
        return hasAttribute(struct, "Serialize")
    }

    /** 验证结构体是否可反序列化 */
    fun isDeserializableStruct(struct: RsStructItem): Boolean {
        return hasAttribute(struct, "Deserialize")
    }

    /** 验证let声明是否与Router相关 */
    fun isRouterRelatedLet(letDecl: RsLetDecl): Boolean {
        return letDecl.expr?.text?.contains("Router::") == true
    }

    /** 验证方法调用是否为HTTP方法 */
    fun isHttpMethodCall(methodCall: RsMethodCall): Boolean {
        val methodName = methodCall.identifier?.text
        return methodName in listOf("get", "post", "put", "delete", "patch", "head", "options")
    }

    /** 验证方法调用是否为Router构建方法 */
    fun isRouterBuilderMethodCall(methodCall: RsMethodCall): Boolean {
        val methodName = methodCall.identifier?.text
        return methodName in listOf("push", "with_path", "with_hoop", "nest")
    }

    // ==================== PSI遍历工具方法 ====================

    /** 在PSI元素中查找符合条件的子元素 */
    inline fun <reified T : PsiElement> findElementsOfType(
            root: PsiElement,
            crossinline predicate: (T) -> Boolean = { true }
    ): List<T> {
        val elements = mutableListOf<T>()
        root.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is T && predicate(element)) {
                            elements.add(element)
                        }
                        super.visitElement(element)
                    }
                }
        )
        return elements
    }

    /** 根据模式查找元素 */
    fun <T : PsiElement> findElementsByPattern(
            root: PsiElement,
            pattern: ElementPattern<T>
    ): List<T> {
        val elements = mutableListOf<T>()
        root.accept(
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

    /** 查找第一个符合条件的元素 */
    inline fun <reified T : PsiElement> findFirstElementOfType(
            root: PsiElement,
            crossinline predicate: (T) -> Boolean = { true }
    ): T? {
        var result: T? = null
        root.accept(
                object : com.intellij.psi.PsiRecursiveElementVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (result == null && element is T && predicate(element)) {
                            result = element
                            return
                        }
                        super.visitElement(element)
                    }
                }
        )
        return result
    }

    // ==================== 自定义匹配器 ====================

    /** 创建自定义函数匹配器 */
    fun customFunctionMatcher(
            description: String,
            matcher: (RsFunction) -> Boolean
    ): PsiElementPattern<RsFunction, *> =
            PlatformPatterns.psiElement(RsFunction::class.java)
                    .with(
                            object : PatternCondition<RsFunction>(description) {
                                override fun accepts(
                                        function: RsFunction,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return matcher(function)
                                }
                            }
                    )

    /** 创建自定义结构体匹配器 */
    fun customStructMatcher(
            description: String,
            matcher: (RsStructItem) -> Boolean
    ): PsiElementPattern<RsStructItem, *> =
            PlatformPatterns.psiElement(RsStructItem::class.java)
                    .with(
                            object : PatternCondition<RsStructItem>(description) {
                                override fun accepts(
                                        struct: RsStructItem,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return matcher(struct)
                                }
                            }
                    )

    /** 创建自定义方法调用匹配器 */
    fun customMethodCallMatcher(
            description: String,
            matcher: (RsMethodCall) -> Boolean
    ): PsiElementPattern<RsMethodCall, *> =
            PlatformPatterns.psiElement(RsMethodCall::class.java)
                    .with(
                            object : PatternCondition<RsMethodCall>(description) {
                                override fun accepts(
                                        methodCall: RsMethodCall,
                                        context: ProcessingContext?
                                ): Boolean {
                                    return matcher(methodCall)
                                }
                            }
                    )

    // ==================== 常用组合模式 ====================

    /** Salvo处理器函数模式：异步 + handler属性 */
    val SALVO_HANDLER_PATTERN = ASYNC_FUNCTION_PATTERN.and(functionWithAttribute("handler"))

    /** 数据传输对象结构体模式：可序列化 + 可反序列化 */
    val DTO_STRUCT_PATTERN = SERIALIZABLE_STRUCT_PATTERN.and(DESERIALIZABLE_STRUCT_PATTERN)

    /** API路由函数模式：返回Router + 包含HTTP方法调用 */
    val API_ROUTER_FUNCTION_PATTERN =
            customFunctionMatcher("isApiRouterFunction") { function ->
                if (!isRouterFunction(function)) return@customFunctionMatcher false

                // 检查函数体内是否包含HTTP方法调用
                val httpMethodCalls =
                        findElementsOfType<RsMethodCall>(function) { methodCall ->
                            isHttpMethodCall(methodCall)
                        }

                return@customFunctionMatcher httpMethodCalls.isNotEmpty()
            }
}
