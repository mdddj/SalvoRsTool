package shop.itbug.salvorstool.tool

import com.intellij.psi.util.PsiTreeUtil
import org.rust.lang.core.psi.RsMetaItem
import org.rust.lang.core.psi.RsOuterAttr

///宏帮助类 例子: #[derive(Clone, Debug, PartialEq, DeriveEntityModel, Eq, Serialize, Deserialize)]
class RustOuterAttrManager(val psi: RsOuterAttr) {

    /**
     * 获取宏名称:
     * 例子: 返回 derive
     * */
    val name: String?
        get() {
            return psi.metaItem.path?.text
        }

    /**
     * 返回: ["Clone", "Debug", "PartialEq", "DeriveEntityModel", "Eq", "Serialize", "Deserialize"]
     */
    val argNames: List<String>
        get() {
            val itemList = psi.metaItem.metaItemArgs?.metaItemList
            if (!itemList.isNullOrEmpty()) {
                return itemList.map { it.text }.toList()
            }

            //处理类似这种类型#[sea_orm(primary_key)] 获取primary_key
            val item = PsiTreeUtil.findChildOfType(psi.metaItem.metaItemArgs, RsMetaItem::class.java)
            if (item != null) {
                return listOf(item.text)
            }
            return emptyList()
        }

}

fun RsOuterAttr.myManager() = RustOuterAttrManager(this)