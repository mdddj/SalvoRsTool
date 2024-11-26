package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.tool.SalvoApiActionHelper

///跳转到router的实现
class SalvoApiNavigatorToRouteImplAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        SalvoApiActionHelper.getSelectItem(e)?.navToRouterImpl()
    }

    override fun update(e: AnActionEvent) {
        val item = SalvoApiActionHelper.getSelectItem(e)
        e.presentation.isEnabled = item !=null
        e.presentation.text = "${MyI18n.getMessage("nav_to_psi")} Service Function"
        e.presentation.icon = AllIcons.Nodes.Function
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}