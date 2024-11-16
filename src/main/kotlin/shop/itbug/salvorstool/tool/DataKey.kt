package shop.itbug.salvorstool.tool

import com.intellij.openapi.actionSystem.DataKey
import shop.itbug.salvorstool.model.SalvoApiItem

object MyDataKey {
    val JListSelectItemDataKey = DataKey.create<SalvoApiItem>("listSelectItemDataKey")
}