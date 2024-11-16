package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.MyDataKey
import shop.itbug.salvorstool.tool.copy

class CopyUrlAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val item = e.getData(MyDataKey.JListSelectItemDataKey) ?: return
        item.api.copy()
    }

    override fun update(e: AnActionEvent) {
        with(e.presentation) {
            val item = e.getData(MyDataKey.JListSelectItemDataKey)
            isEnabled = item != null
            text = "${MyI18n.getMessage("copy")}URL"
            icon = AllIcons.Actions.Copy
        }
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
