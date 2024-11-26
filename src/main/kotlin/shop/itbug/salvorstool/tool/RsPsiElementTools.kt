package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.impl.RsFunctionImpl
import org.rust.lang.doc.psi.impl.RsDocCommentImpl


/**
 * rs操作节点工具类
 */
object RsPsiElementTools {

    /**
     * 获取注释文本
     */
    fun findDocumentText(element: RsDocCommentImpl): String? {
        val comments = element.lastChild.node
        println(comments::class.java)
        return comments?.text
    }


    /**
     * 获取函数的注释
     */
    fun findDocumentWithRsFunction(func: RsFunctionImpl): String? {
        val doc = PsiTreeUtil.findChildOfType(func, RsDocCommentImpl::class.java) ?: return null
        return findDocumentText(doc)
    }


}