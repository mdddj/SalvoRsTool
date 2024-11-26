package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsExpr
import org.rust.lang.core.psi.impl.*
import shop.itbug.salvorstool.model.SalvoApiItemFunction
import shop.itbug.salvorstool.model.SalvoApiItemMethod

fun RsMethodCallExprImpl.methodCallExprManager() = RsMethodCallExprImplFactory(this)

 class RsMethodCallExprImplFactory(private val methodPsiElement: RsMethodCallExprImpl) {


    ///获取接口路径
    val rootApiPath: String?
        get() {
            val rec: RsCallExprImpl? = PsiTreeUtil.findChildOfType(methodPsiElement,RsCallExprImpl::class.java)
            if (rec != null) {
                val exCall = rec
                if (exCall.firstChild is RsPathExprImpl && exCall.firstChild.text == "Router::with_path") {
                    var apiText = (exCall.valueArgumentList.exprList.first() as? RsLitExprImpl)?.stringLiteral?.text ?: "<unknown>"
                    if (apiText.startsWith("\"")) {
                        apiText = apiText.removePrefix("\"")
                    }
                    if (apiText.endsWith("\"")) {
                        apiText = apiText.removeSuffix("\"")
                    }
                    return apiText
                }
            }
            return null
        }


    val getAllApiMethods: List<SalvoApiItemFunction>
        get() {
            val list = mutableListOf<SalvoApiItemFunction>()
            val children = PsiTreeUtil.findChildrenOfAnyType(methodPsiElement, RsMethodCallImpl::class.java)
            children.forEach {
                val manager = RsMethodCallImplFactory(it)
                val methodType = manager.getApiType
                list.add(SalvoApiItemFunction(methodType,it))
            }
            return list.filter { it.method != SalvoApiItemMethod.Unknown }
        }


    private class RsMethodCallImplFactory(private val methodCallPsiElement: RsMethodCallImpl) {
        val getApiType: SalvoApiItemMethod
            get() {
                return when (methodCallPsiElement.identifier.text) {
                    "put" -> SalvoApiItemMethod.Put
                    "get" -> SalvoApiItemMethod.Get
                    "delete" -> SalvoApiItemMethod.Delete
                    "post" -> SalvoApiItemMethod.Post
                    "update" -> SalvoApiItemMethod.Update
                    "patch" -> SalvoApiItemMethod.Patch
                    else -> SalvoApiItemMethod.Unknown
                }
            }
    }

}