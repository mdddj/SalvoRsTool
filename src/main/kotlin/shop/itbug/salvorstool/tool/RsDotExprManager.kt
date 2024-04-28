package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.toMutableSmartList
import org.rust.lang.core.psi.RsDotExpr
import org.rust.lang.core.psi.ext.stringValue
import org.rust.lang.core.psi.impl.RsCallExprImpl
import org.rust.lang.core.psi.impl.RsDotExprImpl
import org.rust.lang.core.psi.impl.RsLitExprImpl
import org.rust.lang.core.psi.impl.RsMethodCallImpl
import org.rust.lang.core.psi.impl.RsPathExprImpl
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.model.SalvoApiItemMethod

val RsDotExprImpl.myManager: DotExprManager get() = DotExprManager(this)

//表达式
class DotExprManager(private val dotExpr: RsDotExprImpl) {

    ///常见函数
    private val salvoMethod: SalvoApiItemMethod? get() {
        return dotExpr.methodCall?.methodManager?.myApiMethod
    }

    private val isPushDot: Boolean
        get() {
            return dotExpr.methodCall?.identifier?.text == "push"
        }

    //是否为路由请求path
     val isRouterWith: Boolean
        get() {
            val call = rsCallExpr ?: return false
            return PsiTreeUtil.findChildOfType(call, RsPathExprImpl::class.java)?.text == "Router::with_path"
        }

    //获取api
    val startApi: String?
        get() {
            if (isRouterWith.not()) return null
            if (rsCallExpr == null) return null
            val arg = rsCallExpr!!.valueArgumentList
            val lit = PsiTreeUtil.findChildOfType(arg, RsLitExprImpl::class.java) ?: return null
            return lit.stringValue
        }

    private val rsCallExpr: RsCallExprImpl?
        get() {
            return PsiTreeUtil.findChildOfType(getFinalDotExpr, RsCallExprImpl::class.java)
        }

    private val getFinalDotExpr: RsDotExprImpl
        get() {
            val find = PsiTreeUtil.findChildOfType(dotExpr, RsDotExprImpl::class.java)
            return find ?: dotExpr
        }

    private val getPushDotExpr: RsDotExprImpl?
        get() {
            val arg = dotExpr.methodCall?.valueArgumentList ?: return null
            return PsiTreeUtil.findChildrenOfType(arg, RsDotExprImpl::class.java).firstOrNull()
        }


    fun getApiItemList(): List<SalvoApiItem> {
        return getSimpleApiItemList()
    }


    private fun getSimpleApiItemList(): List<SalvoApiItem> {
        val list = PsiTreeUtil.findChildrenOfType(dotExpr, RsMethodCallImpl::class.java)
            .filter { it.methodManager.myApiMethod != null }.mapNotNull { it.methodManager.getApiItem() }.toList()
        return list
    }

}

