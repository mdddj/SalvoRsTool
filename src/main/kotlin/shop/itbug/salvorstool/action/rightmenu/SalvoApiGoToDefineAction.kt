package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.MyDataKey.JListSelectItemDataKey

/**
 * 跳转到接口定义为止
 */
class SalvoApiGoToDefineAction: AnAction() {
    override fun actionPerformed(p0: AnActionEvent) {
        val item = p0.getData(JListSelectItemDataKey)!!
        item.navTo()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(JListSelectItemDataKey) !=null
        e.presentation.text = "${MyI18n.getMessage("nav_to_psi")} Api Define"
        e.presentation.icon = AllIcons.Actions.FindForward
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}