package shop.itbug.salvorstool.tool

import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.model.SalvoApiItem

object SalvoApiActionHelper {
    fun getSelectItem(event: AnActionEvent) : SalvoApiItem? {
        return event.getData(MyDataKey.JListSelectItemDataKey)
    }
}