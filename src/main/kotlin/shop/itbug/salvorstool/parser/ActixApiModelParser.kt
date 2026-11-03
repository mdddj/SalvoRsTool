package shop.itbug.salvorstool.parser

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.firstLeaf
import com.intellij.sql.isNullOr
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsLitExpr
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.psi.ext.name
import org.rust.lang.core.psi.ext.stringValue
import org.rust.lang.core.psi.impl.RsCallExprImpl
import org.rust.lang.core.psi.impl.RsLitExprImpl
import org.rust.lang.core.psi.impl.RsMethodCallExprImpl
import org.rust.lang.core.psi.impl.RsMethodCallImpl
import org.rust.lang.core.psi.impl.RsPathExprImpl
import shop.itbug.salvorstool.model.ActixApiModel
import shop.itbug.salvorstool.model.ActixApiModelMethod
import shop.itbug.salvorstool.tool.RsPsiElementTools

class ActixApiModelParser {

    fun parse(file: PsiFile): List<ActixApiModel> {
        val apiModels = mutableListOf<ActixApiModel>()

        val functions = file.childrenOfType<RsFunction>()

        for (function in functions) {
            val outerAttrs = function.outerAttrList
            for (attr in outerAttrs) {
                val apiModel = parseApiModelFromAttribute(attr, function)
                if (apiModel != null) {
                    apiModels.add(apiModel)
                }
            }
        }

        /**
         * 处理这种类型的
         *             .route("/hey", web::get().to(manual_hello))
         *             .route("/echo", web::post().to(manual_echo))
         *             .route("/users", web::get().to(get_all_users))
         *             .route("/options", web::get().to(options_handler))
         */
        val methodCalls = PsiTreeUtil.findChildrenOfType(file, RsMethodCallImpl::class.java).filter(this::isRouter)
        for (methodCall in methodCalls) {
            val path = methodCallGetPath(methodCall)
            val type = methodCallGetMethodType(methodCall)
            if (path == null || type == null) {
                break
            }
            apiModels.add(ActixApiModel(
                path,
                type,
                methodElement = SmartPointerManager.createPointer(methodCall),
                psiFile = file as RsFile,
                module = ModuleUtilCore.findModuleForPsiElement(methodCall)
            ))
        }

        return apiModels
    }


    //获取 path
    private fun methodCallGetPath(method: RsMethodCallImpl): String? {
        val expr = method.valueArgumentList.exprList
        val find = expr.find { it is RsLitExprImpl } as? RsLitExprImpl
        return find?.stringValue
    }

    private fun methodCallGetMethodType(method: RsMethodCallImpl): ActixApiModelMethod? {
        val expr = method.valueArgumentList.exprList
        val find = expr.find { it is RsMethodCallExprImpl } as? RsMethodCallExprImpl ?: return null
        if (find.methodCall.firstLeaf().text != "to") return null
        val callType = find.receiver as? RsCallExprImpl ?: return null
        fun findId(): String? {
            val c = callType.expr as? RsPathExprImpl ?: return null
            return c.path.lastChild.text
        }

        return when (findId()) {
            "get" -> ActixApiModelMethod.Get
            "post" -> ActixApiModelMethod.Post
            "put" -> ActixApiModelMethod.Put
            "delete" -> ActixApiModelMethod.Delete
            "patch" -> ActixApiModelMethod.Patch
            "head" -> ActixApiModelMethod.Head
            else -> ActixApiModelMethod.Unknown
        }
    }

    private fun isRouter(method: RsMethodCallImpl): Boolean {
        val expr = method.valueArgumentList.exprList
        return method.firstLeaf().text == "route" && expr.isNotEmpty() && expr.size == 2 && expr.any { it is RsLitExprImpl } && expr.any { it is RsMethodCallExprImpl }
    }

    private fun parseApiModelFromAttribute(
        attr: RsOuterAttr,
        function: RsFunction
    ): ActixApiModel? {
        val metaItem = attr.metaItem

        val methodName = metaItem.name ?: return null
        val method =
            when (methodName.lowercase()) {
                "get" -> ActixApiModelMethod.Get
                "post" -> ActixApiModelMethod.Post
                "put" -> ActixApiModelMethod.Put
                "delete" -> ActixApiModelMethod.Delete
                "patch" -> ActixApiModelMethod.Patch
                "head" -> ActixApiModelMethod.Head
                else -> ActixApiModelMethod.Unknown
            }

        if (method == ActixApiModelMethod.Unknown) {
            return null
        }

        val args = metaItem.metaItemArgs ?: return null
        val litExpr = args.litExprList.firstOrNull() ?: return null
        val url = litExpr.stringValue ?: return null

        val comment = RsPsiElementTools.findDocumentWithRsFunction(function)
        val module = ModuleUtilCore.findModuleForPsiElement(function)
        return ActixApiModel(
            url = url,
            method = method,
            functionElement =
                SmartPointerManager.getInstance(function.project).createSmartPsiElementPointer(function),
            comment = comment,
            psiFile = function.containingFile as RsFile,
            module = module
        )
    }

    private fun parseApiModelFromMethodCall() {

    }
}
