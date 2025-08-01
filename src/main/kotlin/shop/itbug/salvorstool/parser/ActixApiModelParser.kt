package shop.itbug.salvorstool.parser

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.RsFunction
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.ext.childrenOfType
import org.rust.lang.core.psi.ext.name
import org.rust.lang.core.psi.ext.stringValue
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

        return apiModels
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
}
