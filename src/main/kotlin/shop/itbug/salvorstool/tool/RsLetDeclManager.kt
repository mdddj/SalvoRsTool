package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsCallExpr
import org.rust.lang.core.psi.RsDotExpr
import org.rust.lang.core.psi.ext.stringValue
import org.rust.lang.core.psi.impl.RsCallExprImpl
import org.rust.lang.core.psi.impl.RsDotExprImpl
import org.rust.lang.core.psi.impl.RsLetDeclImpl
import org.rust.lang.core.psi.impl.RsLitExprImpl
import org.rust.lang.core.psi.impl.RsPathExprImpl
import org.rust.lang.core.psi.impl.RsPathImpl
import shop.itbug.salvorstool.model.SalvoApiItem

val RsLetDeclImpl.myManager get() = RsLetDeclManager(this)

//let表达式相关操作
class RsLetDeclManager(val psi: RsLetDeclImpl) {

    //api
    val apiStartString: String?
        get() {
            val dot = firstDotExprImpl ?: return null
            return DotExprManager(dot).startApi
        }

    // first dot
    private val firstDotExprImpl: RsDotExprImpl?
        get() {
            return PsiTreeUtil.findChildrenOfAnyType(psi, RsDotExprImpl::class.java).firstOrNull() as? RsDotExprImpl
        }

    // api list
    val apiList: List<SalvoApiItem>
        get() {
            val dot = firstDotExprImpl ?: return emptyList()
            val list = dot.myManager.getApiItemList()
//            println("\n$list\n")
            return list
        }

}
