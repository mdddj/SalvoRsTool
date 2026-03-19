package shop.itbug.salvorstool.tool

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsExpr
import org.rust.lang.core.psi.RsMethodCall
import org.rust.lang.core.psi.ext.block
import org.rust.lang.core.psi.impl.RsBlockExprImpl
import org.rust.lang.core.psi.impl.RsCallExprImpl
import org.rust.lang.core.psi.impl.RsExprStmtImpl
import org.rust.lang.core.psi.impl.RsFunctionImpl
import org.rust.lang.core.psi.impl.RsLetDeclImpl
import org.rust.lang.core.psi.impl.RsLitExprImpl
import org.rust.lang.core.psi.impl.RsMethodCallExprImpl
import org.rust.lang.core.psi.impl.RsPathExprImpl
import org.rust.lang.core.psi.impl.RsRetExprImpl
import org.rust.lang.core.stubs.index.RsNamedElementIndex
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.model.SalvoApiItemMethod
import java.util.Collections
import java.util.IdentityHashMap

class SalvoRouterParser(private val project: Project) {

    private data class RouteEntry(
        val path: String,
        val method: SalvoApiItemMethod,
        val methodCall: RsMethodCall,
    )

    private data class RouterTree(
        val prefix: String = "",
        val routes: MutableList<RouteEntry> = mutableListOf(),
    ) {
        fun copyTree(): RouterTree = RouterTree(prefix = prefix, routes = routes.toMutableList())
    }

    private data class ParseState(
        val visitingLets: MutableSet<RsLetDeclImpl> =
            Collections.newSetFromMap(IdentityHashMap<RsLetDeclImpl, Boolean>()),
        val visitingFunctions: MutableSet<RsFunctionImpl> =
            Collections.newSetFromMap(IdentityHashMap<RsFunctionImpl, Boolean>()),
    )

    fun parseFile(file: PsiFile): List<SalvoApiItem> {
        val state = ParseState()
        val result = mutableListOf<SalvoApiItem>()
        PsiTreeUtil.findChildrenOfType(file, RsLetDeclImpl::class.java).forEach { letDecl ->
            result.addAll(parseLet(letDecl, state))
        }
        PsiTreeUtil.findChildrenOfType(file, RsFunctionImpl::class.java).forEach { function ->
            result.addAll(parseFunction(function, state))
        }
        return deduplicate(result)
    }

    fun parseLet(letDecl: RsLetDeclImpl): List<SalvoApiItem> {
        return parseLet(letDecl, ParseState())
    }

    fun parseFunction(function: RsFunctionImpl): List<SalvoApiItem> {
        return parseFunction(function, ParseState())
    }

    private fun parseLet(letDecl: RsLetDeclImpl, state: ParseState): List<SalvoApiItem> {
        val tree = parseLetTree(letDecl, state) ?: return emptyList()
        return tree.toApiItems()
    }

    private fun parseFunction(function: RsFunctionImpl, state: ParseState): List<SalvoApiItem> {
        val tree = parseFunctionTree(function, state) ?: return emptyList()
        return tree.toApiItems()
    }

    private fun parseLetTree(letDecl: RsLetDeclImpl, state: ParseState): RouterTree? {
        if (!state.visitingLets.add(letDecl)) {
            return null
        }
        return try {
            parseRouterExpr(letDecl.expr, state)
        } finally {
            state.visitingLets.remove(letDecl)
        }
    }

    private fun parseFunctionTree(function: RsFunctionImpl, state: ParseState): RouterTree? {
        if (!state.visitingFunctions.add(function)) {
            return null
        }
        return try {
            val resultExpr = findFunctionResultExpr(function) ?: return null
            parseRouterExpr(resultExpr, state)
        } finally {
            state.visitingFunctions.remove(function)
        }
    }

    private fun parseRouterExpr(expr: PsiElement?, state: ParseState): RouterTree? {
        return when (expr) {
            is RsMethodCallExprImpl -> parseMethodCallExpr(expr, state)
            is RsCallExprImpl -> parseCallExpr(expr, state)
            is RsPathExprImpl -> parsePathExpr(expr, state)
            is RsBlockExprImpl -> parseBlockExpr(expr, state)
            else -> unwrapNestedExpr(expr)?.let { parseRouterExpr(it, state) }
        }
    }

    private fun parseBlockExpr(blockExpr: RsBlockExprImpl, state: ParseState): RouterTree? {
        val resultExpr = findLastExpression(blockExpr) ?: return null
        return parseRouterExpr(resultExpr, state)
    }

    private fun parseCallExpr(expr: RsCallExprImpl, state: ParseState): RouterTree? {
        val pathExpr = expr.expr as? RsPathExprImpl ?: return null
        return when (pathExpr.text) {
            "Router::new" -> RouterTree()
            "Router::with_path" -> {
                val path = extractStringValue(expr.valueArgumentList.exprList.firstOrNull()) ?: ""
                RouterTree(prefix = normalizePath(path))
            }

            else -> resolveFunction(expr, pathExpr.text)?.let { parseFunctionTree(it, state)?.copyTree() }
        }
    }

    private fun parsePathExpr(expr: RsPathExprImpl, state: ParseState): RouterTree? {
        val letDecl = resolveLet(expr, expr.text)
        if (letDecl != null) {
            return parseLetTree(letDecl, state)?.copyTree()
        }
        return resolveFunction(expr, expr.text)?.let { parseFunctionTree(it, state)?.copyTree() }
    }

    private fun parseMethodCallExpr(expr: RsMethodCallExprImpl, state: ParseState): RouterTree? {
        val methodCall = expr.methodCall
        val methodName = methodCall.identifier.text

        if (methodName == "into_router") {
            val path = extractStringValue(methodCall.valueArgumentList.exprList.firstOrNull()) ?: ""
            return RouterTree(prefix = normalizePath(path))
        }

        val receiverTree = parseRouterExpr(expr.receiver, state) ?: return null

        when (methodName) {
            "get", "post", "put", "delete", "patch", "update" -> {
                val method = methodName.toApiMethod() ?: return receiverTree
                val routePath = extractRoutePath(methodCall)
                receiverTree.routes.add(
                    RouteEntry(
                        path = joinPaths(receiverTree.prefix, routePath),
                        method = method,
                        methodCall = methodCall,
                    )
                )
            }

            "push", "unshift", "merge_router" -> {
                methodCall.valueArgumentList.exprList.forEach { arg ->
                    val childTree = parseRouterExpr(arg, state) ?: return@forEach
                    mergeTree(receiverTree, childTree)
                }
            }

            "hoop" -> Unit
            else -> Unit
        }

        return receiverTree
    }

    private fun mergeTree(parent: RouterTree, child: RouterTree) {
        child.routes.forEach { route ->
            parent.routes.add(route.copy(path = joinPaths(parent.prefix, route.path)))
        }
    }

    private fun findFunctionResultExpr(function: RsFunctionImpl): RsExpr? {
        val block = function.block ?: return null
        val directExprStmts = PsiTreeUtil.getChildrenOfTypeAsList(block, RsExprStmtImpl::class.java)
        directExprStmts.lastOrNull()?.let { stmt ->
            getDirectExpr(stmt)?.let { return it }
        }
        val returns = PsiTreeUtil.findChildrenOfType(block, RsRetExprImpl::class.java).toList()
        returns.lastOrNull()?.let { retExpr ->
            getDirectExpr(retExpr)?.let { return it }
        }
        return null
    }

    private fun findLastExpression(element: PsiElement): RsExpr? {
        val directExprStmts = PsiTreeUtil.getChildrenOfTypeAsList(element, RsExprStmtImpl::class.java)
        directExprStmts.lastOrNull()?.let { stmt ->
            getDirectExpr(stmt)?.let { return it }
        }
        return null
    }

    private fun getDirectExpr(element: PsiElement): RsExpr? {
        return PsiTreeUtil.getChildrenOfTypeAsList(element, RsExpr::class.java).firstOrNull()
    }

    private fun unwrapNestedExpr(element: PsiElement?): PsiElement? {
        if (element == null) {
            return null
        }
        return PsiTreeUtil.getChildrenOfTypeAsList(element, RsExpr::class.java)
            .firstOrNull { it != element }
    }

    private fun resolveLet(anchor: PsiElement, text: String): RsLetDeclImpl? {
        val name = text.substringAfterLast("::")
        val function = PsiTreeUtil.getParentOfType(anchor, RsFunctionImpl::class.java)
        if (function != null) {
            val localLets = PsiTreeUtil.findChildrenOfType(function, RsLetDeclImpl::class.java)
                .filter { it.pat?.text == name && it.textOffset <= anchor.textOffset }
            localLets.maxByOrNull { it.textOffset }?.let { return it }
        }

        val fileLets = PsiTreeUtil.findChildrenOfType(anchor.containingFile, RsLetDeclImpl::class.java)
            .filter { it.pat?.text == name && it.textOffset <= anchor.textOffset }
        return fileLets.maxByOrNull { it.textOffset }
    }

    private fun resolveFunction(anchor: PsiElement, text: String): RsFunctionImpl? {
        val name = text.substringAfterLast("::")
        val localFunctions = PsiTreeUtil.findChildrenOfType(anchor.containingFile, RsFunctionImpl::class.java)
            .filter { it.name == name }
        if (localFunctions.isNotEmpty()) {
            return localFunctions.minByOrNull { kotlin.math.abs(it.textOffset - anchor.textOffset) }
        }

        val namedElements = RsNamedElementIndex.Helper.findElementsByName(
            project,
            name,
            GlobalSearchScope.projectScope(project),
        )

        return namedElements.filterIsInstance<RsFunctionImpl>().firstOrNull()
    }

    private fun extractRoutePath(methodCall: RsMethodCall): String {
        val firstArg = methodCall.valueArgumentList.exprList.firstOrNull()
        return extractStringValue(firstArg) ?: ""
    }

    private fun extractStringValue(expr: PsiElement?): String? {
        return when (expr) {
            is RsLitExprImpl -> expr.stringLiteral?.text?.trim('"')?.let(::normalizePath)
            is RsPathExprImpl -> {
                val letDecl = resolveLet(expr, expr.text) ?: return null
                extractStringValue(letDecl.expr)
            }

            else -> unwrapNestedExpr(expr)?.let { extractStringValue(it) }
        }
    }

    private fun RouterTree.toApiItems(): List<SalvoApiItem> {
        return routes.map { route ->
            SalvoApiItem(
                api = route.path,
                method = route.method,
                rsMethodPsiElement = SmartPointerManager.createPointer(route.methodCall),
                routerFileName = route.methodCall.containingFile?.name ?: "",
            )
        }
    }

    private fun deduplicate(items: List<SalvoApiItem>): List<SalvoApiItem> {
        val map = linkedMapOf<String, SalvoApiItem>()
        items.forEach { item ->
            val offset = item.rsMethodPsiElement.element?.textOffset ?: -1
            val key = "${item.method}:${item.api}:$offset"
            map[key] = item
        }
        return map.values.toList()
    }

    private fun String.toApiMethod(): SalvoApiItemMethod? {
        return when (this) {
            "get" -> SalvoApiItemMethod.Get
            "post" -> SalvoApiItemMethod.Post
            "put" -> SalvoApiItemMethod.Put
            "delete" -> SalvoApiItemMethod.Delete
            "patch" -> SalvoApiItemMethod.Patch
            "update" -> SalvoApiItemMethod.Update
            else -> null
        }
    }

    private fun joinPaths(base: String, path: String): String {
        val normalizedBase = normalizePath(base)
        val normalizedPath = normalizePath(path)
        if (normalizedBase.isEmpty()) {
            return normalizedPath.ifEmpty { "/" }
        }
        if (normalizedPath.isEmpty() || normalizedPath == "/") {
            return normalizedBase
        }
        val baseValue = normalizedBase.trimEnd('/')
        val pathValue = normalizedPath.trimStart('/')
        return normalizePath("$baseValue/$pathValue")
    }

    private fun normalizePath(path: String): String {
        if (path.isBlank()) {
            return ""
        }
        var value = path.trim()
        value = value.replace(Regex(":(\\w+)")) { match ->
            "{${match.groupValues[1]}}"
        }
        value = value.replace(Regex("<([^>]+)>")) { match ->
            "{${match.groupValues[1]}}"
        }
        value = if (value.startsWith("/")) value else "/$value"
        if (value.length > 1) {
            value = value.trimEnd('/')
        }
        return value
    }
}

fun RsLetDeclImpl.parseSalvoApis(): List<SalvoApiItem> {
    return SalvoRouterParser(project).parseLet(this)
}

fun RsFunctionImpl.parseSalvoApis(): List<SalvoApiItem> {
    return SalvoRouterParser(project).parseFunction(this)
}
