package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsMethodCall
import org.rust.lang.core.psi.impl.RsCallExprImpl
import org.rust.lang.core.psi.impl.RsLitExprImpl
import org.rust.lang.core.psi.impl.RsMethodCallExprImpl
import org.rust.lang.core.psi.impl.RsPathExprImpl
import shop.itbug.salvorstool.model.SalvoApiItemFunction

val RsMethodCall.methodCallManager: RsMethodCallManager get() = RsMethodCallManager(this)

/**
 *
 * push(
 *             Router::with_path("<id>")
 *                 .put(put_update_post)
 *                 .delete(delete_post)
 *                 .get(get_post_by_id),
 *         )
 */


class RsMethodCallManager(val psiElement: RsMethodCall) {

    ///返回 push
    private val myIdText: String get() = psiElement.identifier.text

    ///是否 .push
    val isPushMethodCall: Boolean get() = myIdText == "push"

    /// 返回<id>
    val withPathString : String get() {
       val eles = PsiTreeUtil.findChildrenOfType(psiElement.valueArgumentList,RsCallExprImpl::class.java).filter { it.firstChild is RsPathExprImpl && it.firstChild.text == "Router::with_path" }
        if(eles.isNotEmpty()){
            val first = eles.first()
            val lit = first.valueArgumentList.exprList.firstOrNull() as? RsLitExprImpl
            return lit?.stringLiteral?.text?.replace("\"","") ?: "<unknown>"
        }
        return ""
    }

    /// 获取全部的get,post....
    val allMethods : List<SalvoApiItemFunction> get()  {
        val expr: RsMethodCallExprImpl? = psiElement.valueArgumentList.exprList.firstOrNull() as? RsMethodCallExprImpl
        if(expr != null){
            return expr.methodCallExprManager().getAllApiMethods
        }
        return emptyList()
    }

}
