package shop.itbug.salvorstool.tool

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.psi.RsNamedFieldDecl
import org.rust.lang.core.psi.RsOuterAttr
import org.rust.lang.core.psi.ext.stringValue
import org.rust.lang.core.psi.impl.RsNamedFieldDeclImpl
import org.rust.lang.core.psi.impl.RsStructItemImpl


/// psi操作管理
class MyRsPsiElementManager(context: PsiElement) {
    val isStruct = context is RsStructItemImpl
}

/// struct操作管理
class MyRsStructManager(private val psiElement: RsStructItemImpl) {


    ///属性列表
    val fieldList: List<RsNamedFieldDecl> =
        psiElement.blockFields?.namedFieldDeclList
            ?: emptyList<RsNamedFieldDeclImpl>()

    ///获取struct名称
    val structName: String? = psiElement.name

    ///获取表明
    val getTableName : String?  get() {
        val outerAttr = psiElement.outerAttrList.find { it.myManager.getSeaOrmTabName != null }
            ?: return null
        val tabName = outerAttr.myManager.getSeaOrmTabName
        return tabName
    }

    ///主键字段
    val primaryField = fieldList.find { it.myManager.isPrimaryKey }

}


///


///属性处理
class MyFieldPsiElementManager(private val psiElement: RsNamedFieldDecl) {


    ///是否为主键的字段
    val isPrimaryKey = hasMetaItem("sea_orm") { it.text == "primary_key" }

    ///参数字段
    val name: String? = psiElement.name

    val typeString: String? = psiElement.typeReference?.text

    ///获取字段文本(除了宏以外)
    val getSimpleText: String
        get() {
            var text = psiElement.text
            val metas = psiElement.outerAttrList
            if (metas.isNotEmpty()) {
                metas.forEach { meta -> text = text.replace(meta.text, "").replace("\n", "").trim() }
            }
            return text
        }

    ///查找meta
    fun hasMetaItem(filter: (item: RsMetaItem) -> Boolean): Boolean {
        val outerAttrList = psiElement.outerAttrList
        outerAttrList.forEach { metas ->
            val args = metas.metaItem.metaItemArgs
            args?.metaItemList?.forEach { meta ->
                run {
                    val result = filter(meta)
                    if (result) {
                        return true
                    }
                }
            }
        }
        return false
    }

    ///查找meta,比较精确的查找
    fun hasMetaItem(name: String, filter: (item: RsMetaItem) -> Boolean): Boolean {
        val find = psiElement.outerAttrList.find { it.myManager.isMeta(name) } ?: return false
        val args = find.metaItem.metaItemArgs
        args?.metaItemList?.forEach { meta ->
            run {
                val result = filter(meta)
                if (result) {
                    return true
                }
            }
        }
        return false
    }
}

class MyRsOuterAttrPsiElementManager(private val psiElement: RsOuterAttr) {


    val args = psiElement.metaItem.metaItemArgs
    val argItems = args?.metaItemList ?: emptyList()
    val getSeaOrmTabName = getArgString("sea_orm","table_name")

    ///判断是不是某个宏
    fun isMeta(name: String): Boolean {
        return psiElement.metaItem.path?.text == name
    }


    ///查找meta,比较精确的查找
    fun hasMetaItem(name: String, filter: (item: RsMetaItem) -> Boolean): Boolean {
        if (!isMeta(name)) return false
        argItems.forEach { meta ->
            run {
                val result = filter(meta)
                if (result) {
                    return true
                }
            }
        }
        return false
    }

    fun getArgPsiElement(name: String, attr: String): RsMetaItem? {
        if (!hasMetaItem(name) { it.path?.text == attr }) return null
        argItems.forEach {
            if (it.path?.text == attr) {
                return it
            }
        }
        return null
    }

    /**
     * 获取属性文本值
     *
     * 例子: #[sea_orm(table_name = "users")]
     * 传参: "sea_orm","table_name"
     * 返回: "users"
     */
    fun getArgString(name: String, attr: String): String? {
        val psi = getArgPsiElement(name, attr)
        if (psi != null) {
            return psi.litExpr?.stringValue
        }
        return null
    }
}