package shop.itbug.salvorstool.action.rightmenu

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.i18n.MyI18n
import shop.itbug.salvorstool.window.ApiScanWindow.Companion.JListSelectItemDataKey

///跳转到router的实现
class SalvoApiNavigatorToRouteImplAction: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val item = e.getData(JListSelectItemDataKey)!!
        item.navToRouterImpl()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(JListSelectItemDataKey) !=null
        e.presentation.text = "${MyI18n.getMessage("nav_to_psi")} Router Handle"
        e.presentation.icon = AllIcons.Actions.FindForward
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}