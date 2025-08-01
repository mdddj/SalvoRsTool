package shop.itbug.salvorstool.tool

import com.intellij.openapi.application.runReadAction
import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RS_EOL_COMMENTS
import org.rust.lang.core.psi.RsFunction
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
    fun findDocumentWithRsFunction(func: RsFunction): String? {
        val doc = runReadAction {  PsiTreeUtil.findChildOfType(func, RsDocCommentImpl::class.java) }
        if(doc!=null){
            return findDocumentText(doc) ?: findSimpleDocument(func)
        }
        return findSimpleDocument(func)

    }

    /**
     * 获取简单类型的注释
     */
    fun findSimpleDocument(func: RsFunction): String? {
        val findChild = runReadAction { func.node.findChildByType(RS_EOL_COMMENTS) }
        return findChild?.text?.trimStart()?.removePrefix("//")?.trimStart()?.trimEnd()
    }

}