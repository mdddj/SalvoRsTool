package shop.itbug.salvorstool.tool.funs

import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.createSmartPointer
import com.intellij.sql.dialects.redshift.RsTokens
import com.intellij.sql.dialects.redshift.RsTypes
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.psi.RsPath
import org.rust.lang.core.psi.impl.RsStructItemImpl

abstract class RsStructItemFunBase(val element: RsStructItemImpl) {

    private val myProject = element.project


    /**
     * 获取struct名称
     * ```rs
     * pub struct Model {}
     * ```
     * 返回: Model
     */
    fun getMyStructName(): String? {
        return element.node.findChildByType(RsTypes.RS_IDENTIFIER)?.text
    }

    /**
     * todo:
     * 获取宏列表
     */
    fun getMetes(): Map<String, List<Meta>> {
        val map = mutableMapOf<String, List<Meta>>()
        for (outerAttr in element.outerAttrList) {
            val item = outerAttr.metaItem
            val args = item.metaItemArgs
            val name = item.path?.text ?: continue
            val mts = mutableListOf<Meta>()


            fun getMeta(metaItem: RsMetaItem) : Meta? {
                val rsPath = metaItem.path
                if (rsPath != null && metaItem.children.size == 1) {
                    return SimpleMete(rsPath.text, rsPath.createSmartPointer())
                }
                val hasEq = metaItem.node.findChildByType(RsTokens.RS_OP_EQ) != null
                if (hasEq) {
                    val key = metaItem.firstChild?.text ?: ""
                    val value = metaItem.lastChild?.text ?: ""
                    return KeyValueMete(key, value, metaItem.createSmartPointer())
                }
                return null
            }

            args?.metaItemList?.forEach { metaItem ->
                getMeta(metaItem)?.let {
                    
                }
            }
            map.put(name, mts)
        }
        return map
    }


    sealed class Meta()
    data class SimpleMete(val name: String, val psiElement: SmartPsiElementPointer<RsPath>) : Meta()
    data class KeyValueMete(val key: String, val value: String, val psiElement: SmartPsiElementPointer<RsMetaItem>) :
        Meta()


}