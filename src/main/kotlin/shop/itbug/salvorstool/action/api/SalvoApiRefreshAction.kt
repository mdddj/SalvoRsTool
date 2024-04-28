package shop.itbug.salvorstool.action.api

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import shop.itbug.salvorstool.service.SalvoApiService

class SalvoApiRefreshAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let {
            SalvoApiService.getInstance(it).doRefresh()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
        e.presentation.icon = AllIcons.General.InlineRefresh
        e.presentation.text = "Salvo API Refresh"
        super.update(e)
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
