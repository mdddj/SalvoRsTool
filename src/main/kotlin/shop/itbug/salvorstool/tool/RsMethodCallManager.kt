package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsMethodCall
import org.rust.lang.core.psi.impl.RsDotExprImpl
import org.rust.lang.core.psi.impl.RsLetDeclImpl
import org.rust.lang.core.psi.impl.RsMethodCallImpl
import shop.itbug.salvorstool.model.SalvoApiItem
import shop.itbug.salvorstool.model.SalvoApiItemMethod
import java.util.*

val RsMethodCall.methodManager: RsMethodCallManager get() = RsMethodCallManager(this)

/// method call
class RsMethodCallManager(val psiElement: RsMethodCall) {


    private val myIdText: String get() = psiElement.identifier.text

    //是否有push函数
    private val hasPush: Boolean get() = dtoChild != null

    //找到dot
    private val dtoChild: RsDotExprImpl?
        get() {
            val va = psiElement.valueArgumentList
            return PsiTreeUtil.findChildOfType(va, RsDotExprImpl::class.java)
        }

    // method
    val myApiMethod: SalvoApiItemMethod?
        get() {
            return SalvoApiItemMethod.entries.find { it.name.lowercase(Locale.getDefault()) == myIdText.lowercase(Locale.getDefault()) }
        }


    // 生成item
    fun getApiItem(): SalvoApiItem? {
        val m = myApiMethod ?: return null
        val dots = getParentList(psiElement).reversed()
        val finalUrl = dots.map { it.myManager.startApi }.joinToString("/")
        return SalvoApiItem(finalUrl, m,psiElement)
    }
}

private fun getParentList(element: RsMethodCall): List<RsDotExprImpl> {
    val parentList = mutableListOf<RsDotExprImpl>()
    var currentElement: PsiElement? = element.parent
    while (currentElement != null) {
        if (currentElement is RsDotExprImpl && currentElement.myManager.startApi != null && currentElement.myManager.isRouterWith && parentList.find { it.myManager.startApi == (currentElement as RsDotExprImpl).myManager.startApi } == null) {
            parentList.add(currentElement)
        }
        currentElement = currentElement.parent
        if (currentElement is RsLetDeclImpl) {
            break
        }
    }
    return parentList
}